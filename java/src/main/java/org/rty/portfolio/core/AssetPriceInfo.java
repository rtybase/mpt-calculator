package org.rty.portfolio.core;

import java.util.Date;

import org.rty.portfolio.core.utils.DatesAndSetUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AssetPriceInfo implements CsvWritable {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public final String assetName;
	public final double price;
	public final double change;
	public final double rate;
	public final Date date;

	public AssetPriceInfo(String assetName, double price, double change, double rate, Date date) {
		this.assetName = assetName;
		this.price = price;
		this.change = change;
		this.rate = rate;
		this.date = date;

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

	@Override
	public String[] toCsvLine() {
		return new String[] {
				assetName,
				"" + price,
				"" + change,
				"" + rate,
				DatesAndSetUtil.dateToStr(date)
		};
	}
}
