package market.data.server;

import java.io.FileReader;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestConfig {

	private static TestConfig INSTANCE;
	private TestConfig() {
		try (FileReader fileReader = new FileReader(CONFINI)) {
		    iniConfiguration.read(fileReader);
		    sections = iniConfiguration.getSections();
		    logger.info("Profiles: {}", sections);
		    Iterator<String> keyItr = iniConfiguration.getKeys();
		    while (keyItr.hasNext()) {
		    	String key = keyItr.next();
		    	String value = iniConfiguration.getString(key);
		    	logger.info("config: key<{}> value<{}>", key, value);
		    }
		    
		} catch (Exception ex) {
			logger.warn("Failed to load {} from {}", CONFINI, System.getProperty("user.dir"), ex);
		} 
	}
	
	public static TestConfig getInstance() {
		if (INSTANCE==null) {
			INSTANCE = new TestConfig();
		}
		return INSTANCE;
	}
	
	private static final Logger logger = LogManager.getLogger(TestConfig.class);
	private static final String CONFINI = "config.ini";
	private static final String NUM_THREAD_TAG = "numThread";
	private static final String THROTTLE_THRESHOLD_TAG = "throttleThreshold";
	private static final String NUM_FEED_TAG = "numFeed";
	private static final String INSTRUMENT_FILE_TAG = "instrumentFile";

	private INIConfiguration iniConfiguration = new INIConfiguration();

	private static final int default_threshold = 100;
	private static final int default_numDataFeed = 10000;
	private static final int default_numThread = 1;
	private static final String instrumentDataFile = "instdata.txt";
	
	private static Set<String> sections = null;
	
	private int get(String profile, String tag, int defaultValue) {
		try {
			logger.debug("get: profile<{}> tag<{}> defaultValue<{}>", profile, tag, defaultValue);
			return iniConfiguration.getInt(profile+"."+tag);
		}
		catch (Exception e) {
			logger.warn("Failed to get {} from [{}].  use defualt value {}", tag, profile, defaultValue);
		}
		return defaultValue;
	}
	
	private String get(String profile, String tag, String defaultValue) {
		try {
			SubnodeConfiguration sub = iniConfiguration.getSection(profile);
			if (sub!=null) {
				return sub.getString(tag);
			}
		}
		catch (Exception e) {
			logger.warn("Failed to get {} from [{}].  use defualt value {}", tag, profile, defaultValue);
		}
		return defaultValue;
	}

	public int getThreshold(String profile) {
		return get(profile, THROTTLE_THRESHOLD_TAG, default_threshold);
	}
	
	public int getNumDataFeed(String profile) {
		return get(profile, NUM_FEED_TAG, default_numDataFeed);
	}
	public int getNumThread(String profile) {
		return get(profile, NUM_THREAD_TAG, default_numThread);
	}
	public String getInstrumentDataFile(String profile) {
		return get(profile, INSTRUMENT_FILE_TAG, instrumentDataFile);
	}
	public boolean isSectionExists(String section) {
		return (sections.contains(section));
	}
	 
}
