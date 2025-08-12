package org.rty.portfolio.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AssetEpsHistoricalInfo implements CsvWritable {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public final String assetName;

	public final int sectorIndex;
	public final String sector;

	public final int industryIndex;
	public final String industry;
	
	public final AssetEpsInfo currentEps;
	public final AssetEpsInfo previousEps;
	public final AssetPriceInfo priceBeforeCurrentEps;
	public final AssetPriceInfo priceAtCurrentEps;
	public final AssetPriceInfo priceAfterCurrentEps;

	public AssetEpsHistoricalInfo(String assetName, int sectorIndex, String sector,
			int industryIndex, String industry,
			AssetEpsInfo currentEps, AssetEpsInfo previousEps,
			AssetPriceInfo priceBeforeCurrentEps,
			AssetPriceInfo priceAtCurrentEps,
			AssetPriceInfo priceAfterCurrentEps) {
		this.assetName = assetName;

		this.sectorIndex = sectorIndex;
		this.sector = sector;

		this.industryIndex = industryIndex;
		this.industry = industry;

		this.currentEps = currentEps;
		this.previousEps = previousEps;

		this.priceBeforeCurrentEps = priceBeforeCurrentEps;
		this.priceAtCurrentEps = priceAtCurrentEps;
		this.priceAfterCurrentEps = priceAfterCurrentEps;
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
				"" + currentEps.epsPredicted,
				"" + currentEps.eps,
				"" + (previousEps.eps - previousEps.epsPredicted),
				"" + (currentEps.eps - currentEps.epsPredicted),
				"" + (currentEps.epsPredicted - previousEps.epsPredicted),
				"" + (currentEps.eps - previousEps.eps),
				"" + priceBeforeCurrentEps.rate,
				"" + priceAtCurrentEps.rate,
				"" + priceAfterCurrentEps.rate
		};
	}
}
