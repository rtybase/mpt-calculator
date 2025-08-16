package org.rty.portfolio.core;

import org.rty.portfolio.math.Calculator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AssetEpsHistoricalInfo implements CsvWritable {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public final String assetName;

	public final int sectorIndex;
	public final int industryIndex;
	
	public final AssetEpsInfo currentEps;
	public final AssetEpsInfo previousEps;
	public final AssetPriceInfo price2DaysBeforeCurrentEps;
	public final AssetPriceInfo priceBeforeCurrentEps;
	public final AssetPriceInfo priceAtCurrentEps;
	public final AssetPriceInfo priceAfterCurrentEps;
	public final AssetPriceInfo price2DaysAfterCurrentEps;

	public AssetEpsHistoricalInfo(String assetName, int sectorIndex, int industryIndex,
			AssetEpsInfo currentEps, AssetEpsInfo previousEps,
			AssetPriceInfo price2DaysBeforeCurrentEps,
			AssetPriceInfo priceBeforeCurrentEps,
			AssetPriceInfo priceAtCurrentEps,
			AssetPriceInfo priceAfterCurrentEps,
			AssetPriceInfo price2DaysAfterCurrentEps) {
		this.assetName = assetName;
		this.sectorIndex = sectorIndex;
		this.industryIndex = industryIndex;

		this.currentEps = currentEps;
		this.previousEps = previousEps;

		this.price2DaysBeforeCurrentEps = price2DaysBeforeCurrentEps;
		this.priceBeforeCurrentEps = priceBeforeCurrentEps;
		this.priceAtCurrentEps = priceAtCurrentEps;
		this.priceAfterCurrentEps = priceAfterCurrentEps;
		this.price2DaysAfterCurrentEps = price2DaysAfterCurrentEps;
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
				"" + sectorIndex,
				"" + industryIndex,
				"" + currentEps.date.getMonth(),
				"" + previousEps.epsPredicted,
				"" + previousEps.eps,
				"" + Calculator.calculateEpsSurprise(previousEps.eps, previousEps.epsPredicted),
				"" + currentEps.epsPredicted,
				"" + currentEps.eps,
				"" + Calculator.calculateEpsSurprise(currentEps.eps, currentEps.epsPredicted),
				"" + Calculator.calculateDiffWithPrecision(previousEps.eps, previousEps.epsPredicted),
				"" + Calculator.calculateDiffWithPrecision(currentEps.eps, currentEps.epsPredicted),
				"" + Calculator.calculateDiffWithPrecision(currentEps.epsPredicted, previousEps.epsPredicted),
				"" + Calculator.calculateDiffWithPrecision(currentEps.eps, previousEps.eps),
				"" + price2DaysBeforeCurrentEps.rate,
				"" + priceBeforeCurrentEps.rate,
				"" + priceAtCurrentEps.rate,
				emptyRateIfPriceInfoIsNull(priceAfterCurrentEps),
				emptyRateIfPriceInfoIsNull(price2DaysAfterCurrentEps)
		};
	}

	private static String emptyRateIfPriceInfoIsNull(AssetPriceInfo priceInfo) {
		if (priceInfo == null) {
			return "";
		}

		return "" + priceInfo.rate;
	}
}
