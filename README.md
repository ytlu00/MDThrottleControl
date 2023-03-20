# MDThrottleControl
 
This is the simulation of a Market data Throttle control.  

## Assumption of Throttle control
- Accept market data feed from multiple channels.
- Publish the latest market data to one thread.
- publishArggregatedMarketData is to publish message.

## What does the Throttle control do?
- publishArggregatedMarketData will be called does not exceed the threshold (the parameter, throttleThreshold, in config.ini)
- Same symbol does not publish more than once per sliding window
- The latest market data on each symbol will be published
- 

SimpleTestServer: a simple server to read the config.ini file and create thrad pool which is to start market data feed.


## Build
mvn clean install

## Run
java -jar target/MDThrottleControl-0.0.1-SNAPSHOT-jar-with-dependencies.jar market.data.server.SimpleTestServer profile1

## Run test case
There is a test case which is to 
- Verify symbol does not update more than once per sliding window
- Verify the number of publishAggregatedMarketData calls does not exceed Threshold parameter

### config.ini
Profiles are stored in config.ini

- numThread: number of thread to feed market data
- throttleThreshold: The number of market data can be published in 1 second 
- number of feed: how many total number of market data update from a thread
- instrumentFile: the list of instruments

```
[profile1]
numThread=1
throttleThreshold=100
numFeed=100000
instrumentFile=instr.txt

[profile2]
numThread=1
throttleThreshold=10
numFeed=100
instrumentFile=instr.txt

[profile3]
numThread=1
throttleThreshold=10
numFeed=100
instrumentFile=
```

