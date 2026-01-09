package org.rty.portfolio.core;

import java.util.Date;

import org.rty.portfolio.core.utils.DatesAndSetUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AssetDividendInfo implements CsvWritable, EntryWithAssetNameAndDate {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public final String assetName;
	public final double pay;
	public final Date date;

	public AssetDividendInfo(String assetName, double pay, Date date) {
		this.assetName = assetName;
		this.pay = pay;
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
				"" + pay,
				DatesAndSetUtil.dateToStr(date)
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
