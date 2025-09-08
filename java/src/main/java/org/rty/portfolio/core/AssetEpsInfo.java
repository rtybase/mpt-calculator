package org.rty.portfolio.core;

import java.util.Date;

import org.rty.portfolio.core.utils.DatesAndSetUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AssetEpsInfo implements CsvWritable, EntryWithAssetNameAndDate {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public final String assetName;
	public final double eps;
	public final Double epsPredicted;
	public final Date date;

	public AssetEpsInfo(String assetName, double eps, Date date) {
		this.assetName = assetName;
		this.eps = eps;
		this.epsPredicted = null;
		this.date = date;

	}

	public AssetEpsInfo(String assetName, double eps, Double epsPredicted, Date date) {
		this.assetName = assetName;
		this.eps = eps;
		this.epsPredicted = epsPredicted;
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
				"" + eps,
				"" + epsPredicted,
				DatesAndSetUtil.dateToStr(date)
		};
	}

	@Override
	public String getAssetName() {
		return assetName;
	}

	@Override
	public Date getDate() {
		if (date instanceof java.sql.Date) {
			return new Date(date.getTime());
		}

		return date;
	}
}
