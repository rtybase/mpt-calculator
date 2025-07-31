package org.rty.portfolio.core;

import java.util.Date;

import org.rty.portfolio.core.utils.DatesAndSetUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AssetEpsInfo implements CsvWritable {
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
}
