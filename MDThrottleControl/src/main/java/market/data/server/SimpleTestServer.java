package market.data.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import market.data.MarketDataProcessor;
import market.data.feed.DataFeedService;

public class SimpleTestServer {

	private static final Logger logger = LogManager.getLogger(SimpleTestServer.class);
	
	private int threshold = 100;
	private int dataFeedItem = 1000;
	private int numThread = 1;
	private List<String> instData = null;
	
	public void startTest() {
		
		MarketDataProcessor mdp = new MarketDataProcessor(threshold);
		
		DataFeedService dfs = new DataFeedService(dataFeedItem, numThread, instData);
		dfs.addProcessor(mdp);
		
		mdp.start();
		
		try {
			
			Thread.sleep(2000);
			dfs.beginDataFeed();
			mdp.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
	
	public void getProfile(String profile) {
		
		threshold = TestConfig.getInstance().getThreshold(profile);
		dataFeedItem = TestConfig.getInstance().getNumDataFeed(profile);;
		numThread = TestConfig.getInstance().getNumThread(profile);;
		String instFile = TestConfig.getInstance().getInstrumentDataFile(profile);
		
		instData = loadFileToList(instFile);
		
		
		logger.info("Market Data Throttle Control: threshold<"+threshold+"> NumberDataFeed<"+dataFeedItem+"> DataFeedThread<"+numThread+"> Data<"+instData+">");

	}
	
	public List<String> loadFileToList(String instrfile) {
		List<String> data = null;
		
		if (instrfile!=null && instrfile.isEmpty()==false) {
		    Path path = Paths.get(instrfile);
			logger.debug("loadFileToList: "+path);
		    	         
    	    Stream<String> lines;
			try {
				lines = Files.lines(path);
	    	    data = lines.collect(Collectors.toList());
	    	    lines.close();
	    	    return data;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return null;
	}
	
	public static void main(String[] args) {
		
		logger.info("Working Path:" + System.getProperty("user.dir"));
		String profile = "";
		if (args.length > 0) {
			profile = args[0];
			if (TestConfig.getInstance().isSectionExists(profile)==false) {
				if (args.length > 1) {
					profile = args[1];
				}
			}
			
			logger.info("user input: profile <{}>", profile);
		}
		SimpleTestServer server = new SimpleTestServer();
		
		server.getProfile(profile);
		server.startTest();
		
	}

}
