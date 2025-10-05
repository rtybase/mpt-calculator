package org.rty.portfolio.core.utils;

import java.util.Date;

import org.rty.portfolio.core.AssetEpsInfo;
import org.rty.portfolio.core.AssetNonGaapEpsInfo;
import org.rty.portfolio.core.AssetPriceInfo;

public class CommonTestRoutines {
	public static final Date D_2025_07_17 = dateFrom(17);
	public static final String TEST_ASSET = "MSFT";
	public static final double ERROR_TOLERANCE = 0.00001D;

	public static Date dateFrom(int day) {
		return new Date(125, 6, day);
	}

	public static AssetPriceInfo newPriceInfo(double price) {
		return newPriceInfo(price, D_2025_07_17);
	}

	public static AssetEpsInfo newEpsInfo(double eps, Double predictedEps) {
		return newEpsInfo(eps, predictedEps, D_2025_07_17);
	}

	public static AssetNonGaapEpsInfo newNonGaapEpsInfo(double eps, Double predictedEps, boolean afterMarketClose) {
		return newNonGaapEpsInfo(eps, predictedEps, afterMarketClose, D_2025_07_17);
	}

	public static AssetPriceInfo newPriceInfo(double price, Date date) {
		return new AssetPriceInfo(TEST_ASSET, price, 0.1D, 1D, 10D, 1D, date);
	}

	public static AssetEpsInfo newEpsInfo(double eps, Double predictedEps, Date date) {
		return new AssetEpsInfo(TEST_ASSET, eps, predictedEps, date);
	}

	public static AssetNonGaapEpsInfo newNonGaapEpsInfo(double eps, Double predictedEps, boolean afterMarketClose,
			Date date) {
		return new AssetNonGaapEpsInfo(TEST_ASSET, eps, predictedEps, afterMarketClose, 1D, 1D, date);
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
