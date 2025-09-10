package org.rty.portfolio.core.utils;

import java.util.Date;

import org.rty.portfolio.core.AssetEpsInfo;
import org.rty.portfolio.core.AssetPriceInfo;

class CommonTestRoutines {
	protected static final Date D_2025_07_17 = dateFrom(17);
	protected static final String TEST_ASSET = "MSFT";
	protected static final double ERROR_TOLERANCE = 0.00001D;

	protected static Date dateFrom(int day) {
		return new Date(125, 6, day);
	}

	protected static AssetPriceInfo assetPriceFrom(String assetName, String date) {
		final String[] line = new String[] { assetName, "421.53", "3.06", "0.731235", date };
		return ToEntityConvertorsUtil.toAssetPriceInfoEntity(assetName, line);
	}

	protected static AssetEpsInfo assetEpsFrom(String assetName, String date) {
		final String[] line = new String[] { assetName, "3.65", "3.35", date };
		return ToEntityConvertorsUtil.toAssetEpsInfoEntity(assetName, line);
	}
}
