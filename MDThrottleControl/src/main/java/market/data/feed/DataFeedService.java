package market.data.feed;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import market.data.MarketData;
import market.data.MarketDataProcessor;

public class DataFeedService {

	private ExecutorService executorService = null;
	private int numThread = 1;
	//List<String> instruments = List.of("ALMIL", "AL2SI", "5PG", "AASB","AB","LTA","MLALE","MLAA","AFG","AREIT");
	private List<String> instruments = List.of("AA", "BB", "CC", "DD","EE","FF", "GG", "HH", "II", "JJ", "KK");
	
	private List<MarketDataProcessor> processors = new ArrayList<MarketDataProcessor>();
    private static final Logger logger = LogManager.getLogger(DataFeedService.class);

	private int dataFeedItem = 1000;
	
	public DataFeedService(int dataFeedItem, int numThread, List<String> instruments) {
		super();
		this.dataFeedItem = dataFeedItem;
		this.numThread = numThread;
		executorService = Executors.newFixedThreadPool(numThread);
		if (instruments!=null && instruments.size()>0) {
			this.instruments = instruments;
		}
	}

	public void addProcessor(MarketDataProcessor mdp) {
		processors.add(mdp);
	}
	
	public void beginDataFeed()  {
		
		Runnable r = () -> {
			logger.info("DataFeedService::Begin with instruments: "+instruments);
			
			try {
				for (int i=0; i<dataFeedItem; i++) {
					double price = Math.random() * 49 + 1; 
					MarketData d1 = new MarketData(instruments.get(i%instruments.size()), price, Instant.now().truncatedTo(ChronoUnit.NANOS).toString());
					for (MarketDataProcessor processor : processors) {
						processor.onMessage(d1);
						
							Thread.sleep(1);
					}
				}
			} catch (InterruptedException e) {
				logger.warn("DataFeedService::Failed to sleep");
				e.printStackTrace();
			}
			logger.info("DataFeedService: {} Finished", Thread.currentThread().getName());
		};
		
		for (int i=0; i<numThread; i++) {
			executorService.submit(r);
		}
		executorService.shutdown();
	}
}
