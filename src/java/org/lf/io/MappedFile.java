package org.lf.io;

import org.lf.parser.ScrollableInputStream;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;


/**
 * User: jkff Date: Oct 13, 2009 Time: 3:24:53 PM
 */
public class MappedFile implements RandomAccessFileIO {

	private class Buffer {
		int refCount;
		MappedByteBuffer mBuf;
		long segmentPosition;

		private Buffer(MappedByteBuffer buf, long segmentPosition) {
			this.refCount = 1;
			this.mBuf = buf;
			this.segmentPosition = segmentPosition;
		}
    }

	private class BufferPool {
		// File is devided on parts of same size = bufSize (last part size may
		// differ from others,
		// so buffers are not overlap
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
			} // or create a new buffer with refCount = 1 || wait and check
			// whether it was made
			else {
				if (base2buf.size() >= maxBuffers) {
                    // This is incorrect: read JCIP and google about how to use
                    // wait/notify to see why. The reason is: so called "spurious wakeups".
					wait();
                    // Because of spurious wakeups, a call to wait() may finish even if
                    // noone called notify or notifyAll.

                    // Hm. On a second thought, the code actually may turn out to be correct,
                    // because the recursive call will also wait if there are not enough buffers,
                    // but it is misleading, so proper style of wait/notify usage should be used. 

					return getBuffer(position);
				} else {
                    FileChannel rafChannel = raf.getChannel();
					// surprisingly, rafChannel can be closed
					while (!rafChannel.isOpen()) {
						rafChannel.close();
						raf.close();
						raf = new RandomAccessFile(fileName,"r");		
						rafChannel = raf.getChannel();
					}
						
					Buffer b = new Buffer(rafChannel.map(FileChannel.MapMode.READ_ONLY, segmentPosition, curBufSize), segmentPosition);
					base2buf.put(segmentPosition, b);
					rafChannel.close();
					
                    notifyAll();
					return b;
				}
			}

		}

		synchronized void releaseBuffer(Buffer buf) {
			if (buf.refCount==1) {
				buf.refCount=0;
				base2buf.remove(buf.segmentPosition);
				if (base2buf.size() < maxBuffers)
					notifyAll();
			} else {
				buf.refCount--;
			}
		}

		// Tells that buffer 'buf' is no longer needed,
		// buf a buffer that contains 'newOffset' is.
		// Does nothing if 'newOffset' lies in 'buf',
		// otherwise frees 'buf' and allocates a new buffer.
		public Buffer move(Buffer buf, long newOffset) throws IOException,InterruptedException {
			if (buf.segmentPosition <= newOffset && newOffset <= buf.segmentPosition + buf.mBuf.capacity()) {
				return buf;
			} else {
				releaseBuffer(buf);
				return getBuffer(newOffset);
			}
		}

		BufferPool(long bufSize) throws FileNotFoundException {
			this.bufSize = bufSize;
			this.fileSize = new File(fileName).length();
			this.raf = new RandomAccessFile(fileName, "r");
			//equal to number of threads on current file 
			this.maxBuffers = 2;
			this.base2buf = new HashMap<Long, Buffer>();
		}
	}

	private BufferPool bufferPool;

	private String fileName;

	public MappedFile(String fileName) throws FileNotFoundException {
		this.fileName = fileName;
		this.bufferPool = new BufferPool(10000);
	}

	public ScrollableInputStream getInputStreamFrom(final long offset) throws IOException {
		return new ScrollableInputStream() {
			private Buffer buf;

			private long offsetInBuffer;

			private boolean isOpen;

			{
				try{
					this.buf = bufferPool.getBuffer(offset);
				}catch (InterruptedException e){
					throw new IOException("InterruptedException in bufferPool.getBuffer()");
				}
					this.offsetInBuffer = offset - this.buf.segmentPosition;
				
				this.isOpen = true;
			}
//absolute scroll
			public void scrollTo(long newOffset) throws IOException {
				try {
					this.buf = bufferPool.move(this.buf, newOffset);
					this.offsetInBuffer = newOffset - this.buf.segmentPosition;
				} catch (InterruptedException e){
					throw new IOException("InterruptedException in bufferPool.move()");
				}
			}
//relative scroll				
			@Override
			public long scrollBack(long offset) throws IOException {
				ensureOpen();
				long scrolled=0;
				long curFilePos = this.offsetInBuffer + this.buf.segmentPosition;
				try {
					if (curFilePos < offset) {
						scrolled = curFilePos;
						this.buf = bufferPool.move(buf, 0L);
						this.offsetInBuffer = 0L;
					} else {
						this.buf = bufferPool.move(buf, curFilePos - offset);
						this.offsetInBuffer = curFilePos - offset - this.buf.segmentPosition;
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
				long maxOffset = new File(fileName).length();
				long curFilePos = this.offsetInBuffer + this.buf.segmentPosition;
				long scrolled;
				try {
					if (curFilePos + offset > maxOffset) {
						scrolled = maxOffset - curFilePos;
						this.buf = bufferPool.move(buf, maxOffset);
						this.offsetInBuffer = this.buf.mBuf.capacity();
					} else {
						this.buf = bufferPool.move(buf, curFilePos + offset);
						this.offsetInBuffer = curFilePos + offset - this.buf.segmentPosition;
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
				byte[] res = new byte[1];
				if (read(res) == 0 )
					return -1;
				return (int)res[0];
			}
//relative read
			@Override
			public int read(byte[] b) throws IOException {				
				int needToRead = b.length;
				int readed=0;
				do {
					long avaliableInCurBuffer = this.buf.mBuf.capacity()- this.offsetInBuffer;
					if (avaliableInCurBuffer > needToRead) {
						for (; needToRead > 0; --needToRead){
							b[readed] = this.buf.mBuf.get((int)this.offsetInBuffer);
							this.offsetInBuffer++;
							readed++;
						}
					} else {
						for (; avaliableInCurBuffer > 0; --avaliableInCurBuffer){
							b[readed] = this.buf.mBuf.get((int)this.offsetInBuffer);
							this.offsetInBuffer++;
							needToRead--;
							readed++;
						}
						if (this.buf.segmentPosition + this.offsetInBuffer == new File(fileName).length())
							return 0;
					
						try {	
							this.buf = bufferPool.move(this.buf,this.buf.segmentPosition + this.offsetInBuffer + 1);
						}catch (InterruptedException e){
							throw new IOException("InterruptedException from bufferPool.move()");
						}
						this.offsetInBuffer=0;	
					}

				} while (needToRead > 0);
				
				return  readed;
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
}
