package market.data.tests;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import market.data.MarketData;
import market.data.MarketDataProcessor;
import market.data.feed.DataFeedService;

class TestThrottle {

	private static final SimpleDateFormat sdf = new SimpleDateFormat(" YYYY-MM-dd HH:mm:ss.SSS ");
	

	@Test
	void testThrottleThreshold() {
		int threshold = 10;
		int numDataFeed = 100;
		int thread = 1;
		
		String logfile = "logs/logfile.log";
		try {
			if (Files.exists(Paths.get(logfile))) {
				Files.move(Paths.get(logfile), Paths.get(logfile+".bak"), StandardCopyOption.REPLACE_EXISTING);
			}
			
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
		
		Logger logger = LogManager.getLogger(TestThrottle.class);

		List<MarketData> resultList = new ArrayList<>(numDataFeed);
		MarketDataProcessor mdp = new MarketDataProcessor(threshold);
		mdp.setTesterList(resultList);
		
		DataFeedService dfs = new DataFeedService(numDataFeed, thread, null);
		dfs.addProcessor(mdp);
		
		mdp.start();
		
		try {
			
			Thread.sleep(2000);
			dfs.beginDataFeed();
			
			Thread.sleep(15000);
			mdp.exit();
			
			mdp.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Parse Market data throttle control log
		Long resetWindowTime =0L;
		Set publishedMarketData = new HashSet();
		
		try (BufferedReader br = new BufferedReader(new FileReader(logfile))) {
		    String line;
		    int marketDataPublishCountBeforeReset =0;
		    while ((line = br.readLine())!=null) {
		    	if (line.contains("publishAggregatedMarketData: MarketData")==true) {
		    		String symbol = getSymbol(line);
		    		
		    		// verify symbol does not update more than once per sliding window
		    		Assertions.assertTrue(!publishedMarketData.contains(symbol));
		    		
		    		publishedMarketData.add(symbol);
		    		
		    		Long publishTime = getTime(line);
		    		
		    		marketDataPublishCountBeforeReset++;
		    		
		    		if (resetWindowTime!=0L) {
		    			Long result = publishTime - resetWindowTime;
		    			System.out.println("PUBLISH "+symbol+" ("+publishTime+"): Time different from RESET time(msec)="+result);
		    		}
		    	}
		    	else if (line.contains("ResetThread::Reset")==true) {
		    		
		    		resetWindowTime = getTime(line);
		    		
		    		// verify the number of publishAggregatedMarketData calls doesn't not exceed Threshold
		    		System.out.println("marketDataPublishCountBeforeReset="+marketDataPublishCountBeforeReset);
		    		Assertions.assertTrue(marketDataPublishCountBeforeReset <= threshold);
		    		marketDataPublishCountBeforeReset=0;
		    		
		    		publishedMarketData.clear();
		    		System.out.println("RESET: resetWindowTime="+resetWindowTime);
		    		
		    		
 		    	}
		    }
			br.close();
		    
		} catch (Exception ex) {
			logger.warn("Failed to load {} from {}", "logs/logfile.log", System.getProperty("user.dir"), ex);
		} 

//		try {
//			Files.move(Paths.get(logfile), Paths.get(logfile+".bak"), StandardCopyOption.REPLACE_EXISTING);
//			FileWriter myWriter = new FileWriter(logfile);
//		    myWriter.write("[2023-03-17 18:04:33.917150000] [INFO   ] [main]::startup, polledSymbolSet:[]\n");
//			myWriter.close();
//			
//	    } catch (IOException e) {
//	    	e.printStackTrace();
//	    }

	}
	
	public static String getSymbol(String line) {
		String[] fields = line.split("=");
		String[] priceFields = fields[2].split(",");
		String[] subFields1 = fields[1].split(",");
		System.out.println("Instrument: "+subFields1[0]+"-"+priceFields[0]);
		return subFields1[0];
	}

	public static Long getTime(String line) {
		String[] fields = line.split("\\|");
		Long result = 0L;
		try {
			result  = sdf.parse(fields[1]).toInstant().toEpochMilli();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}
}
