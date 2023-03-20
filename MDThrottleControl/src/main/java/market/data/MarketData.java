package market.data;

public class MarketData {
	String symbol;
	double price;
	String updateTime;
	
	public MarketData(String symbol, double price, String updateTime) {
		super();
		this.symbol = symbol;
		this.price = price;
		this.updateTime = updateTime;
	}
	@Override
	public String toString() {
		return "MarketData [symbol=" + symbol + ", price=" + price + ", updateTime=" + updateTime + "]";
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}	
	
}
