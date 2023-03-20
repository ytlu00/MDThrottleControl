package market.data.queue;

import market.data.MarketData;

public interface ThrottleEventQueue {

	public boolean offer(MarketData e);
	public MarketData poll();
	public void stopQueue();
	
}
