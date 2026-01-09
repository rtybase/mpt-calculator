package org.rty.portfolio.core;

import static org.rty.portfolio.core.CsvWritable.emptyIfNull;

import java.util.Date;

import org.rty.portfolio.core.utils.DatesAndSetUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AssetPriceInfo implements CsvWritable, EntryWithAssetNameAndDate {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public final String assetName;
	public final double price;
	public final double change;
	public final double rate;
	public final Double volume;
	public final Double volumeChangeRate;
	public final Date date;

	public AssetPriceInfo(String assetName, double price, double change, double rate, Double volume,
			Double volumeChangeRate, Date date) {
		this.assetName = assetName;
		this.price = price;
		this.change = change;
		this.rate = rate;
		this.volume = volume;
		this.volumeChangeRate = volumeChangeRate;
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
				DatesAndSetUtil.dateToStr(date),
				emptyIfNull(volume),
				emptyIfNull(volumeChangeRate)
		};
	}

	@Override
	public String getAssetName() {
		return assetName;
	}

	@Override
	public Date getDate() {
		return DatesAndSetUtil.toJavaDate(date);
	}
}
