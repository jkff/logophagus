package org.lf.io;

import org.lf.parser.ScrollableInputStream;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class MappedFile implements RandomAccessFileIO {

	private class Buffer {
		private int refCount;
		byte[] buf;
		long segmentPosition;

        private Buffer(byte[] buf, long segmentPosition) {
			this.refCount = 1;
			this.buf = buf;
			this.segmentPosition = segmentPosition;
		}
    }

	private class BufferPool {
		// File is divided into parts of equal size = bufSize (last part size may
		// differ from others, so buffers do not overlap
		private long bufSize;
		private RandomAccessFile raf;
		private long fileSize;
		private int maxBuffers;

		private Map<Long, Buffer> base2buf;

		// Invariant : Every result of getBuffer() must be
		// consequently released by a call of releaseBuffer().

		// Returns a buffer such that 'position' is inside its extent.
		// Blocks until a buffer is available.
		synchronized Buffer getBuffer(long position) throws  InterruptedException,IOException {
			// ...Get an existing buffer if possible and increase its refCount
			
			long segmentNumber = position / bufSize;
			// file is divided into segments
			long segmentPosition = segmentNumber * bufSize;
			// check if the buffer we're going to return will span across the end of file
            // (in this case its size will be less than the maximal bufSize)
			long curBufSize = bufSize;
			if (fileSize - segmentPosition < bufSize) {
				curBufSize = fileSize - segmentPosition; 
			}
			if (base2buf.containsKey(segmentPosition)) {
				Buffer b = base2buf.get(segmentPosition);
				b.refCount++;
				return b;
			}

            // Wait till there's space in the buffer pool
            if (base2buf.size() >= maxBuffers) {
                //need reconstruct
            	while(true) {
                    garbageCollectBuffers();
                    if(base2buf.size() >= maxBuffers)
                        wait();
                    else
                        return getBuffer(position);
                }
            }

            // Create a new buffer with refCount = 1
            FileChannel rafChannel = raf.getChannel();
            // surprisingly, rafChannel can be closed
            while (!rafChannel.isOpen()) {
                rafChannel.close();
                raf.close();
                raf = new RandomAccessFile(file.getAbsolutePath() ,"r");
                rafChannel = raf.getChannel();
            }

            MappedByteBuffer mbb = rafChannel.map(FileChannel.MapMode.READ_ONLY, segmentPosition, curBufSize);
            byte[] buf = new byte[(int)curBufSize];
            mbb.get(buf);
            Buffer b = new Buffer(buf, segmentPosition);
            base2buf.put(segmentPosition, b);
            rafChannel.close();
            
            notifyAll();
            return b;

        }

        private synchronized void garbageCollectBuffers() {
            for (Iterator<Long> it = base2buf.keySet().iterator(); it.hasNext();) {
                Long offset = it.next();
                Buffer b = base2buf.get(offset);
                if(b.refCount == 0) {
                    it.remove();
                }
            }
        }

		synchronized void releaseBuffer(Buffer buf) {
            buf.refCount--;
            if(buf.refCount == 0) {
                notifyAll();
            }
		}

		// Tells that buffer 'buf' is no longer needed,
		// buf a buffer that contains 'newOffset' is.
		// Does nothing if 'newOffset' lies in 'buf',
		// otherwise frees 'buf' and allocates a new buffer.
		public Buffer move(Buffer buf, long newOffset) throws IOException,InterruptedException {
			if (buf.segmentPosition <= newOffset && newOffset < buf.segmentPosition + buf.buf.length) {
				return buf;
			} else {
				releaseBuffer(buf);
				return getBuffer(newOffset);
			}
		}

		BufferPool(long bufSize, int maxBuffers) throws FileNotFoundException {
			this.bufSize = bufSize;
			this.fileSize = file.length();
			this.raf = new RandomAccessFile(file.getAbsolutePath(), "r");
			this.maxBuffers = maxBuffers;
			this.base2buf = new HashMap<Long, Buffer>();
		}
	}

	private BufferPool bufferPool;

	private File file;

	public MappedFile(String fileName) throws FileNotFoundException {
		this.file = new File(fileName);
		this.bufferPool = new BufferPool(100000, 100);
	}

	public ScrollableInputStream getInputStreamFrom(final long offset) throws IOException {
		return new ScrollableInputStream() {
			private Buffer buf;

			private int offsetInBuffer;

			private boolean isOpen;

			{
				try{
					this.buf = bufferPool.getBuffer(offset);
				}catch (InterruptedException e){
					throw new IOException("InterruptedException in bufferPool.getBuffer()");
				}
                this.offsetInBuffer = (int)(offset - this.buf.segmentPosition);
				
				this.isOpen = true;
			}
            //absolute scroll
			public void scrollTo(long newOffset) throws IOException {
				try {
					this.buf = bufferPool.move(this.buf, newOffset);
				} catch (InterruptedException e){
					throw new IOException("InterruptedException in bufferPool.move()");
				}
				this.offsetInBuffer = (int)(newOffset - this.buf.segmentPosition);

			}
            //relative scroll
			@Override
			public long scrollBack(long offset) throws IOException {
				ensureOpen();
				long scrolled;
				long curFilePos = this.offsetInBuffer + this.buf.segmentPosition;
				try {
					if (curFilePos <= offset) {
						scrolled = curFilePos;
						this.buf = bufferPool.move(this.buf, 0L);
						this.offsetInBuffer = 0;
					} else {
						this.buf = bufferPool.move(this.buf, curFilePos - offset);
						this.offsetInBuffer = (int)(curFilePos - offset - this.buf.segmentPosition);
						scrolled = offset;
					}
				} catch (InterruptedException e){
					throw new IOException("InterruptedException in bufferPool.move()");
				}
				return scrolled;
				
			}
			
            //relative scroll
			@Override
			public long scrollForward(long offset) throws IOException{
				ensureOpen();
				long maxOffset = bufferPool.fileSize-1;
				long curFilePos = this.offsetInBuffer + this.buf.segmentPosition;
				long scrolled;
				try {
					if (curFilePos + offset > maxOffset) {
						scrolled = maxOffset - curFilePos;
						this.buf = bufferPool.move(buf, maxOffset);
						this.offsetInBuffer = this.buf.buf.length-1;
					} else {
						this.buf = bufferPool.move(buf, curFilePos + offset);
						this.offsetInBuffer = (int)(curFilePos + offset - this.buf.segmentPosition);
						scrolled = offset;
					}
				} catch(InterruptedException e) {
					throw new IOException("InterruptedException in bufferPool.move()");
				}
				return scrolled;
			}

			//relative read
			@Override
			public int read() throws IOException{
				ensureOpen();
                if (this.offsetInBuffer == this.buf.buf.length-1) {
                	int temp = this.buf.buf[this.offsetInBuffer];
                	if (!shiftNextBuffer()){
                		return -1;
                	}
                	return temp;
                }
            	return this.buf.buf[this.offsetInBuffer++];                
			}
            
			
            //relative read
			@Override
			public int read(byte[] b) throws IOException {				
				int needToRead = b.length;
				int bytesRead=0;
				do {
                 	int availableInCurBuffer = this.buf.buf.length - this.offsetInBuffer;
                    int delta = Math.min(needToRead, availableInCurBuffer);
                 	System.arraycopy(this.buf.buf, this.offsetInBuffer, b, bytesRead , delta);
                    bytesRead += delta;
                    this.offsetInBuffer += delta;
                    needToRead -= delta;
                    if (availableInCurBuffer < needToRead) {
                        if (!shiftNextBuffer()) { 
                        	this.offsetInBuffer = this.buf.buf.length-1;
                        	return 0;
                        }
                    }
                } while (needToRead > 0);
				
				return  bytesRead;

			}

            private boolean shiftNextBuffer() throws IOException {
                if (isAtEOF())
                    return false;
                try {
                    this.buf = bufferPool.move(this.buf, this.buf.segmentPosition + this.buf.buf.length );
                }catch (InterruptedException e){
                    throw new IOException("InterruptedException from bufferPool.move()");
                }
                this.offsetInBuffer=0;
                return true;
            }

            private boolean isAtEOF() {
                return this.buf.segmentPosition + this.offsetInBuffer == bufferPool.fileSize-1;
            }

            @Override
			public int read(byte[] b, int off, int len) {
				throw new UnsupportedOperationException("Who cares?");

			}

			@Override
			public long skip(long n) {
				long skipped;
				try {
					skipped = scrollForward(n);
				} catch (Exception e) {
					skipped = 0;
				}
				return skipped;
			}

			@Override
			public int available() throws IOException {
				throw new UnsupportedOperationException("Who cares?");
			}

			@Override
			public void close() throws IOException {
				ensureOpen();
				bufferPool.releaseBuffer(buf);
				this.isOpen = false;
			}

			private void ensureOpen() {
				if (!isOpen)
					throw new IllegalStateException("Stream closed");
			}
        };
	}

	public long length() {
		return this.bufferPool.fileSize;
	}

	public String getFileName() {
		return file.getName();
	}
}
