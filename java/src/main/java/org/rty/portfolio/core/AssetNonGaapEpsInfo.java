package org.rty.portfolio.core;

import java.util.Date;

import org.rty.portfolio.core.utils.DatesAndSetUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AssetNonGaapEpsInfo implements CsvWritable {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public final String assetName;
	public final double eps;
	public final Double epsPredicted;
	public final Date date;
	public final boolean afterMarketClose;
	public final double revenue;
	public final Double revenuePredicted;

	public AssetNonGaapEpsInfo(String assetName, double eps, Double epsPredicted, boolean afterMarketClose,
			double revenue, Double revenuePredicted, Date date) {
		this.assetName = assetName;
		this.eps = eps;
		this.epsPredicted = epsPredicted;
		this.afterMarketClose = afterMarketClose;
		this.revenuePredicted = revenuePredicted;
		this.revenue = revenue;
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
				"" + afterMarketClose,
				"" + revenue,
				"" + revenuePredicted,
				DatesAndSetUtil.dateToStr(date)
		};
	}
}
