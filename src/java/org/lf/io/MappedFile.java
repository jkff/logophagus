package org.lf.io;

import org.lf.parser.ScrollableInputStream;


import java.io.File;
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

		long positionOfSegment;
	}

	private class BufferPool {
		// File is devided on parts of same size = bufSize (last part size may
		// differ from others,
		// that's why Buffer contain "long size" variable)
		// so buffers are not overlap
		private long bufSize;

		private RandomAccessFile raf;

		private FileChannel rafChannel;
		
		private int maxBuffers;

		private Map<Long, Buffer> base2buf;

		// Invariant : Every result of getBuffer() must be
		// consequently released by a call of releaseBuffer().

		// Returns a buffer such that 'position' is inside its extent.
		// Blocks until a buffer is available.
		synchronized Buffer getBuffer(long position) throws Exception {
			// ...Get an existing buffer if possible and increase its refCount
			long fileSize = new File(fileName).length();
			long segmentNumber = position / bufSize;
			//file is devided on segments
			long segmentPosition = segmentNumber * bufSize;
			//check if we make buffer that maps end of file(then its size differ from others )
			long curBufSize = bufSize;
			if (fileSize - segmentPosition < bufSize){
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
					wait();
					return getBuffer(position);
				} else {
					while (!rafChannel.isOpen()){
						rafChannel.close();
						raf.close();
						raf = new RandomAccessFile(fileName,"r");
						rafChannel = raf.getChannel();
					}
					Buffer b = new Buffer();
					b.mBuf = rafChannel.map(FileChannel.MapMode.READ_ONLY,segmentPosition, curBufSize);
					b.refCount = 1;
					b.positionOfSegment=segmentPosition;
					base2buf.put(segmentPosition, b);
					rafChannel.close();
					notifyAll();
					return b;
				}
			}

		}

		synchronized void releaseBuffer(Buffer buf) {
			if (buf.refCount == 1) {
				buf.refCount = 0;
				base2buf.remove(buf.positionOfSegment);
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
		public Buffer move(Buffer buf, long newOffset) throws Exception {
			if (buf.positionOfSegment <= newOffset && newOffset <= buf.positionOfSegment + buf.mBuf.capacity()) {
				return buf;
			} else {
				releaseBuffer(buf);
				return getBuffer(newOffset);
			}
		}

		BufferPool(long bufSize) throws Exception {
			this.bufSize = bufSize;
			this.raf = new RandomAccessFile(fileName, "r");
			rafChannel = raf.getChannel();
			//equal to number of threads on current file 
			this.maxBuffers = 2;
			this.base2buf = new HashMap<Long, Buffer>();
		}
	}

	private BufferPool bufferPool;

	private String fileName;

	public MappedFile(String fileName) throws Exception {
		this.fileName = fileName;
		this.bufferPool = new BufferPool(10000);
	}

	public ScrollableInputStream getInputStreamFrom(final long offset)
			throws Exception {
		return new ScrollableInputStream() {
			private Buffer buf;

			private long offsetInBuffer;

			private boolean isOpen;

			{
				this.buf = bufferPool.getBuffer(offset);
				this.offsetInBuffer = offset - this.buf.positionOfSegment;
				this.isOpen = true;
			}

			public void shiftTo(long newOffset) throws Exception {
				this.buf = bufferPool.move(this.buf, newOffset);
				this.offsetInBuffer = newOffset - this.buf.positionOfSegment;
			}
//				
			@Override
			public long scrollBack(long offset) throws Exception {
				ensureOpen();
				long scrolled;
				long curFilePos = this.offsetInBuffer + this.buf.positionOfSegment;
				
				if (curFilePos < offset) {
					scrolled = curFilePos;
					this.buf = bufferPool.move(buf, 0L);
					this.offsetInBuffer = 0L;
				} else {
					this.buf = bufferPool.move(buf, curFilePos - offset);
					this.offsetInBuffer = curFilePos - offset - this.buf.positionOfSegment;
					scrolled = offset;
				}
				return scrolled;
			}

			@Override
			public long scrollForward(long offset) throws Exception {
				ensureOpen();
				long maxOffset = new File(fileName).length();
				long curFilePos = this.offsetInBuffer + this.buf.positionOfSegment;
				long scrolled;
				if (curFilePos + offset > maxOffset) {
					scrolled = maxOffset - curFilePos;
					this.buf = bufferPool.move(buf, maxOffset);
					this.offsetInBuffer = this.buf.mBuf.capacity();
				} else {
					this.buf = bufferPool.move(buf, curFilePos + offset);
					this.offsetInBuffer = curFilePos + offset - this.buf.positionOfSegment;
					scrolled = offset;
				}
				return scrolled;
			}

			@Override
			public int read() {
				ensureOpen();
				byte[] res = new byte[1];
				if (read(res) == 0)
					return -1;
				return (int)res[0];
			}

			@Override
			public int read(byte[] b) {				
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
						try {
							if (this.buf.positionOfSegment + this.offsetInBuffer == new File(fileName).length()){
								return 0;
							}
							this.buf = bufferPool.move(this.buf,this.buf.positionOfSegment + this.offsetInBuffer + 1);
						} catch (Exception e) {
							System.out.print("Error in ScrollableInputStream :: read([]) : "+e.getMessage()+ ";");
							this.isOpen=false;
							return 0;
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
		return new File(fileName).length();
	}
}
