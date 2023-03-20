package market.data.queue;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import market.data.MarketData;

public class ThrottleEventQueueImpl extends Thread implements ThrottleEventQueue {

	private int threshold = 0;
	
	private static final Logger logger = LogManager.getLogger(ThrottleEventQueueImpl.class);

	AtomicInteger polledCount = new AtomicInteger(0);
	ConcurrentMap<String, MarketData> marketDataBuffer = new ConcurrentHashMap<String, MarketData>();
	ConcurrentLinkedQueue<String> marketDataEventQueue = new ConcurrentLinkedQueue<String>();
	
	ConcurrentHashMap<String, Integer> polledSymbolMap = new ConcurrentHashMap<>();
	Set<String> polledSymbolSet = polledSymbolMap.newKeySet();
	
	Object lock = new Object();
	
	private AtomicBoolean isExit = new AtomicBoolean(false);
	
	public ThrottleEventQueueImpl(int throttleTheshold) {
		setName("Throttle-Reset-Thread");
		this.threshold = throttleTheshold;
		logger.info("ThrottleEventQueueImpl::construct throttleTheshold: "+threshold);
		this.start();
	}


	public boolean offer(MarketData e) {
		if (logger.isTraceEnabled()) {
			logger.trace("offer: "+e);
		}
		
		marketDataBuffer.put(e.getSymbol(), e);
		marketDataEventQueue.offer(e.getSymbol());
		
		if (logger.isTraceEnabled()) {
			logger.trace("marketDataEventQueue: "+marketDataEventQueue);
		}
		return false;
	}

	public void run() {
		
		long currentTime = System.currentTimeMillis();

		if (logger.isDebugEnabled()) {
			logger.debug("ThrottleEventQueueImpl::Thread Start");
		}
		
		try {		
			while (isExit.get() == false) {
				long newTime = System.currentTimeMillis();
				if (newTime - currentTime > 1000) {
					currentTime = newTime;
					polledCount.set(1);
					polledSymbolSet.clear();
					if (logger.isDebugEnabled()) {
						logger.debug("ResetThread::Reset count and unblock, polledSymbolSet:"+polledSymbolSet);						
					}
					else {
						logger.info("ResetThread::Reset count and unblock");
					}
					unblock();
				}
				Thread.sleep(50);
			}
			
			if (logger.isDebugEnabled()) {
				logger.debug("ThrottleEventQueueImpl::Thread Exit");
			}
		} catch (InterruptedException e) {
			logger.warn("ThrottleEventQueueImpl::Thread Interrupted");
			e.printStackTrace();
		}
	}
	
	public MarketData poll() {
		if (logger.isTraceEnabled()) {
			logger.trace("Poll::enter");
		}
		
		// poll more than Threshold before ResetThread to reset, block it.
		if (polledCount.addAndGet(1) > threshold) {
			logger.info("poll::block polledCount<"+ polledCount.get()+">");
			polledCount.decrementAndGet();
			block();
		}
		MarketData result = null;
		while (marketDataBuffer.size()>0 ) {
			String symbol = marketDataEventQueue.poll();
			
			if (logger.isDebugEnabled()) {
				logger.debug("Poll::Real Polling get Symbol fromQueue "+symbol+" polledSymbolSet:"+polledSymbolSet);
			}
			
			if (symbol!=null && polledSymbolSet.contains(symbol)==false) {
				result = marketDataBuffer.remove(symbol);
				if (result!=null) {
			
					if (logger.isDebugEnabled()) {
						logger.debug("Poll::polledCount<"+polledCount.get()+"> result:"+result);
					}
					
					polledSymbolSet.add(symbol);
					
					if (logger.isDebugEnabled()) {
						logger.debug("Poll::polledCount<"+polledCount.get()+"> polledSymbolSet:"+polledSymbolSet);
					}
					return result;
				}
			}
			else {
				polledCount.decrementAndGet();

				if (logger.isTraceEnabled()) {
					logger.trace("Poll::END symbol<{}> in polledSymbolSet: {}",symbol, polledSymbolSet);
				}
				return null;
			}
		}
		polledCount.decrementAndGet();
		if (logger.isTraceEnabled()) {
			logger.trace("Poll::END marketDataBuffer is empty, marketDataEventQueue<{}> polledSymbolSet: {}", marketDataEventQueue, polledSymbolSet);
		}
		return null;
	}

	
	protected void block() {
		synchronized(lock) {
			try {
				lock.wait();
			}
			catch (InterruptedException e) {
				
			}
		}
	}
	
	protected void unblock() {
		synchronized(lock) {
			lock.notifyAll();
		}
	}

	public void stopQueue() {
		isExit.set(true);
	}

}
