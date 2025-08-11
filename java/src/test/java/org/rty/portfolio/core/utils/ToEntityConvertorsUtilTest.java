package org.rty.portfolio.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.rty.portfolio.core.AssetEpsInfo;
import org.rty.portfolio.core.AssetPriceInfo;

class ToEntityConvertorsUtilTest {
	private static final double ERROR_TOLERANCE = 0.00001D;
	private static final Date D_2025_07_17 = new Date(125, 6, 17);
	private static final String TEST_ASSET = "MSFT";

	@Test
	void testDefaultToDate() {
		final Date result = ToEntityConvertorsUtil.toDate("2025-07-17");
		assertEquals(D_2025_07_17, result);
	}

	@Test
	void testToDateWithFormat() {
		final Date result = ToEntityConvertorsUtil.toDate("07/17/2025", ToEntityConvertorsUtil.EPS_DATE_FORMAT);
		assertEquals(D_2025_07_17, result);
	}

	@Test
	void testToAssetPriceInfoEntity() {
		final String[] line = new String[] { TEST_ASSET, "421.53", "3.06", "0.731235", "2025-07-17" };
		final AssetPriceInfo result = ToEntityConvertorsUtil.toAssetPriceInfoEntity(TEST_ASSET, line);

		assertEquals(TEST_ASSET, result.assetName);
		assertEquals(D_2025_07_17, result.date);
		assertEquals(0.731235D, result.rate, ERROR_TOLERANCE);
		assertEquals(3.06D, result.change, ERROR_TOLERANCE);
		assertEquals(421.53D, result.price, ERROR_TOLERANCE);
	}

	@Test
	void testToAssetEpsInfoEntity() {
		final String[] line = new String[] { TEST_ASSET, "3.65", "3.35", "07/17/2025" };
		final AssetEpsInfo result = ToEntityConvertorsUtil.toAssetEpsInfoEntity(TEST_ASSET, line);

		assertEquals(TEST_ASSET, result.assetName);
		assertEquals(D_2025_07_17, result.date);
		assertEquals(3.65D, result.eps, ERROR_TOLERANCE);
		assertEquals(3.35D, result.epsPredicted, ERROR_TOLERANCE);
	}

	@Test
	void testToAssetEpsInfoEntityWithEmptyPrediction() {
		final String[] line = new String[] { TEST_ASSET, "3.65", "", "07/17/2025" };
		final AssetEpsInfo result = ToEntityConvertorsUtil.toAssetEpsInfoEntity(TEST_ASSET, line);

		assertEquals(TEST_ASSET, result.assetName);
		assertEquals(D_2025_07_17, result.date);
		assertEquals(3.65D, result.eps, ERROR_TOLERANCE);
		assertNull(result.epsPredicted);
	}

	@Test
	void testToAssetEpsInfoEntityWithNAPrediction() {
		final String[] line = new String[] { TEST_ASSET, "3.65", ToEntityConvertorsUtil.NA_VALUE, "07/17/2025" };
		final AssetEpsInfo result = ToEntityConvertorsUtil.toAssetEpsInfoEntity(TEST_ASSET, line);

		assertEquals(TEST_ASSET, result.assetName);
		assertEquals(D_2025_07_17, result.date);
		assertEquals(3.65D, result.eps, ERROR_TOLERANCE);
		assertNull(result.epsPredicted);
	}
}
