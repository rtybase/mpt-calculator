package org.rty.portfolio.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.rty.portfolio.core.AssetEpsInfo;
import org.rty.portfolio.core.AssetPriceInfo;

class ToEntityConvertorsUtilTest extends CommonTestRoutines {
	private static final String[] TEST_LINE = new String[] { TEST_ASSET, "3.65", ToEntityConvertorsUtil.NA_VALUE, "07/17/2025" };

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
		final AssetPriceInfo result = assetPriceFrom(TEST_ASSET,  "2025-07-17");

		assertEquals(TEST_ASSET, result.assetName);
		assertEquals(D_2025_07_17, result.date);
		assertEquals(0.731235D, result.rate, ERROR_TOLERANCE);
		assertEquals(3.06D, result.change, ERROR_TOLERANCE);
		assertEquals(421.53D, result.price, ERROR_TOLERANCE);
	}

	@Test
	void testToAssetEpsInfoEntity() {
		final AssetEpsInfo result = assetEpsFrom(TEST_ASSET, "07/17/2025");

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
		final AssetEpsInfo result = ToEntityConvertorsUtil.toAssetEpsInfoEntity(TEST_ASSET, TEST_LINE);

		assertEquals(TEST_ASSET, result.assetName);
		assertEquals(D_2025_07_17, result.date);
		assertEquals(3.65D, result.eps, ERROR_TOLERANCE);
		assertNull(result.epsPredicted);
	}

	@Test
	void testValueOrDefaultFrom() {
		Double result = ToEntityConvertorsUtil.valueOrDefaultFrom(TEST_LINE, 1, null);

		assertEquals(3.65D, result, ERROR_TOLERANCE);
	}

	@Test
	void testValueOrDefaultFromNAColumn() {
		Double result = ToEntityConvertorsUtil.valueOrDefaultFrom(TEST_LINE, 2, 0D);

		assertNull(result);
	}

	@Test
	void testValueOrDefaultFromNullLine() {
		Double result = ToEntityConvertorsUtil.valueOrDefaultFrom(null, 1, 0D);

		assertEquals(0D, result, ERROR_TOLERANCE);
	}

	@Test
	void testValueOrDefaultWithNegativeIndex() {
		Double result = ToEntityConvertorsUtil.valueOrDefaultFrom(TEST_LINE, -1, 0D);

		assertEquals(0D, result, ERROR_TOLERANCE);
	}

	@Test
	void testValueOrDefaultWithLargeIndex() {
		Double result = ToEntityConvertorsUtil.valueOrDefaultFrom(TEST_LINE, TEST_LINE.length + 1, 0D);

		assertEquals(0D, result, ERROR_TOLERANCE);
	}

	@Test
	void testValueOrDefaultFromNullColumn() {
		final String[] line = new String[] { null };
		Double result = ToEntityConvertorsUtil.valueOrDefaultFrom(line, 0, 0D);

		assertNull(result);
	}
}
