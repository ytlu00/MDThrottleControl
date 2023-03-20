package market.data;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import market.data.queue.ThrottleEventQueue;
import market.data.queue.ThrottleEventQueueImpl;

public class MarketDataProcessor extends Thread {
	
	private static final Logger logger = LogManager.getLogger(MarketDataProcessor.class);
	
	public static final int DEFAULT_THROTTLE_THESHOLD = 3;
	
	public ThrottleEventQueue q = null;
	
	private AtomicBoolean isExit = new AtomicBoolean(false);
	
	public Map<String, Instant> mdReceiveTimeMap = new HashMap<>();
	
	public List<MarketData> testerList = null;
	
	public MarketDataProcessor() {
		this.setName("MDP-Publish-Thread");
		q = new ThrottleEventQueueImpl(DEFAULT_THROTTLE_THESHOLD);
	}
	
	public MarketDataProcessor(int threshold) {
		this.setName("MDP-Thread");
		q = new ThrottleEventQueueImpl(threshold);
	}
	
	public void run() {
		logger.info("MarketDataProcessor::Thread: start");
		try {
			while (isExit.get() == false) {
				if (logger.isTraceEnabled()) {
					logger.trace("start poll Queue");
				}
				MarketData d = q.poll();

				if (logger.isTraceEnabled()) {
					logger.trace("finish poll Queue: {}", d);
				}
				
				if (d!=null) {
					
					publishAggregatedMarketData(d);

				}
			}
			if (isExit.get()==true) {
				q.stopQueue();
				logger.info("MarketDataProcessor::Thread: Exit");
			}
		}
		catch(Exception e) {
			logger.error("Thread: Interrupted", e);
		}
	}
	
	public void onMessage(MarketData data) {
		if (data != null) {
			logger.info("OnMessage: {}", data);
			q.offer(data);
			if (logger.isTraceEnabled()) {
				logger.trace("OnMessage: finish");
			}
		}
	}
	
	public void publishAggregatedMarketData(MarketData data) {
		// do sth with market data
		logger.info("publishAggregatedMarketData: {}", data);
		
	}	
	
	public void exit() {
		isExit.set(true);
	}
	
	public void setTesterList(List<MarketData> resultList) {
		testerList = resultList;
	}

}
