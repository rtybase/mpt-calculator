package org.rty.portfolio.core;

import java.util.Date;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AssetPriceInfo {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

	@Override
	public String toString() {
		try {
			return OBJECT_MAPPER.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return "";
		}
	}
}
