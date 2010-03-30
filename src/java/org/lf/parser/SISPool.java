package org.lf.parser;

import java.io.IOException;
import java.util.Set;

import org.lf.io.RandomAccessFileIO;

import static org.lf.util.CollectionFactory.newHashSet;

public class SISPool {
	private final int poolSize;
	private  Set<ScrollableInputStream> freeSis = newHashSet();
	private final Set<ScrollableInputStream> busySis  = newHashSet();
	private final RandomAccessFileIO raf;
	
	
	public SISPool(RandomAccessFileIO raf, int poolSize) {
		this.raf = raf;
		this.poolSize = poolSize;
	}
	
	synchronized public ScrollableInputStream getSIS(long offset) throws InterruptedException, IOException {
		if (busySis.size() == poolSize) {
			wait();
			return getSIS(offset);
		}
		
		ScrollableInputStream sis = null;
		if (freeSis.size() == 0) 
			sis = raf.getInputStreamFrom(offset);
		else {
			sis = freeSis.iterator().next();
			freeSis.iterator().remove();
			sis.scrollTo(offset);
		}

		busySis.add(sis);
		return sis;
	}

	synchronized public void releaseSIS(ScrollableInputStream sis) {
		if (!busySis.contains(sis)) return;
		busySis.remove(sis);
		notifyAll();
	}
}
