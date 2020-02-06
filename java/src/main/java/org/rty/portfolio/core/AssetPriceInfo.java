package org.rty.portfolio.core;

import java.util.Date;

public class AssetPriceInfo {
	private final String assetName;
	private final double price;
	private final double change;
	private final double rate;
	private final Date date;

	public AssetPriceInfo(String assetName, double price, double change, double rate, Date date) {
		this.assetName = assetName;
		this.price = price;
		this.change = change;
		this.rate = rate;
		this.date = date;

	}

	public String getAssetName() {
		return assetName;
	}

	public double getPrice() {
		return price;
	}

	public double getChange() {
		return change;
	}

	public double getRate() {
		return rate;
	}

	public Date getDate() {
		return date;
	}

}
