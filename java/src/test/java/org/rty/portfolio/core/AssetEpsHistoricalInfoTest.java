package org.rty.portfolio.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.rty.portfolio.core.utils.CommonTestRoutines.ERROR_TOLERANCE;
import static org.rty.portfolio.core.utils.CommonTestRoutines.TEST_ASSET;
import static org.rty.portfolio.core.utils.CommonTestRoutines.newEpsInfo;
import static org.rty.portfolio.core.utils.CommonTestRoutines.newNonGaapEpsInfo;
import static org.rty.portfolio.core.utils.CommonTestRoutines.newPriceInfo;

import org.junit.jupiter.api.Test;

class AssetEpsHistoricalInfoTest {
	private static final AssetNonGaapEpsInfo WITH_AMC = newNonGaapEpsInfo(0D, null, true);
	private static final AssetNonGaapEpsInfo WITH_BMO = newNonGaapEpsInfo(0D, null, false);

	@Test
	void testGetMonthIndex() {
		final AssetEpsHistoricalInfo info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, newEpsInfo(0D, 0D), null, 9D,
				null, null, 9D, null, null, null, null, null, null, null);

		assertEquals(6, info.getMonthIndex());
	}

	@Test
	void testGetAfterMarketCloseWithNullData() {
		final AssetEpsHistoricalInfo info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, null, null, 9D, null, null, 9D,
				null, null, null, null, null, null, null);

		assertEquals(0, info.getPreviousAfterMarketClose());
		assertEquals(0, info.getCurrentAfterMarketClose());
	}

	@Test
	void testGetPreviousAfterMarketClose() {
		final AssetEpsHistoricalInfo info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, null, null, 9D, null,
				newNonGaapEpsInfo(0D, 0D, false), 9D, null, null, null, null, null, null, null);

		assertEquals(0, info.getPreviousAfterMarketClose());
		assertEquals(0, info.getCurrentAfterMarketClose());
	}

	@Test
	void testGetCurrentAfterMarketClose() {
		final AssetEpsHistoricalInfo info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, null,
				newNonGaapEpsInfo(0D, 0D, false), 9D, null, null, 9D, null, null, null, null, null, null, null);

		assertEquals(0, info.getPreviousAfterMarketClose());
		assertEquals(0, info.getCurrentAfterMarketClose());
	}

	@Test
	void tesGetPreviousPredictedEps() {
		AssetEpsHistoricalInfo info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, null, null, 9D, newEpsInfo(0D, 1D),
				newNonGaapEpsInfo(0D, 2D, false), 9D, null, null, null, null, null, null, null);

		assertEquals(1D, info.getPreviousPredictedEps(), ERROR_TOLERANCE);

		info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, null, null, 9D, newEpsInfo(0D, null),
				newNonGaapEpsInfo(0D, 2D, false), 9D, null, null, null, null, null, null, null);

		assertEquals(2D, info.getPreviousPredictedEps(), ERROR_TOLERANCE);

		info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, null, null, 9D, newEpsInfo(0.5D, null), WITH_BMO, 9D, null,
				null, null, null, null, null, null);

		assertEquals(0.5D, info.getPreviousPredictedEps(), ERROR_TOLERANCE);

		info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, null, null, 9D, newEpsInfo(0.5D, null), null, 9D, null,
				null, null, null, null, null, null);

		assertEquals(0.5D, info.getPreviousPredictedEps(), ERROR_TOLERANCE);
	}

	@Test
	void tesGetCurrentPredictedEps() {
		AssetEpsHistoricalInfo info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, newEpsInfo(0D, 1D),
				newNonGaapEpsInfo(0D, 2D, false), 9D, null, null, 9D, null, null, null, null, null, null, null);

		assertEquals(1D, info.getCurrentPredictedEps(), ERROR_TOLERANCE);

		info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, newEpsInfo(0D, null), newNonGaapEpsInfo(0D, 2D, false), 9D,
				null, null, 9D, null, null, null, null, null, null, null);

		assertEquals(2D, info.getCurrentPredictedEps(), ERROR_TOLERANCE);

		info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, newEpsInfo(0.5D, null), WITH_BMO, 9D, null, null, 9D, null,
				null, null, null, null, null, null);

		assertEquals(0.5D, info.getCurrentPredictedEps(), ERROR_TOLERANCE);

		info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, newEpsInfo(0.5D, null), null, 9D, null, null, 9D, null,
				null, null, null, null, null, null);

		assertEquals(0.5D, info.getCurrentPredictedEps(), ERROR_TOLERANCE);
	}

	@Test
	void testGetPreviousNonGaapPredictedEps() {
		AssetEpsHistoricalInfo info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, null, null, 9D, newEpsInfo(0D, 1D),
				newNonGaapEpsInfo(0D, 2D, false), 9D, null, null, null, null, null, null, null);

		assertEquals(2D, info.getPreviousNonGaapPredictedEps(), ERROR_TOLERANCE);

		info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, null, null, 9D, newEpsInfo(0D, 1D), WITH_BMO, 9D, null,
				null, null, null, null, null, null);

		assertEquals(1D, info.getPreviousNonGaapPredictedEps(), ERROR_TOLERANCE);

		info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, null, null, 9D, newEpsInfo(0D, null),
				newNonGaapEpsInfo(0.5D, null, false), 9D, null, null, null, null, null, null, null);

		assertEquals(0.5D, info.getPreviousNonGaapPredictedEps(), ERROR_TOLERANCE);

		info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, null, null, 9D, newEpsInfo(0.5D, null), null, 9D, null,
				null, null, null, null, null, null);

		assertEquals(0.5D, info.getPreviousNonGaapPredictedEps(), ERROR_TOLERANCE);
	}

	@Test
	void testGetCurrentNonGaapPredictedEps() {
		AssetEpsHistoricalInfo info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, newEpsInfo(0D, 1D),
				newNonGaapEpsInfo(0D, 2D, false), 9D, null, null, 9D, null, null, null, null, null, null, null);

		assertEquals(2D, info.getCurrentNonGaapPredictedEps(), ERROR_TOLERANCE);

		info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, newEpsInfo(0D, 1D), WITH_BMO, 9D, null, null, 9D, null,
				null, null, null, null, null, null);

		assertEquals(1D, info.getCurrentNonGaapPredictedEps(), ERROR_TOLERANCE);

		info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, newEpsInfo(0D, null), newNonGaapEpsInfo(0.5D, null, false),
				9D, null, null, 9D, null, null, null, null, null, null, null);

		assertEquals(0.5D, info.getCurrentNonGaapPredictedEps(), ERROR_TOLERANCE);

		info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, newEpsInfo(0.5D, null), null, 9D, null, null, 9D, null,
				null, null, null, null, null, null);

		assertEquals(0.5D, info.getCurrentNonGaapPredictedEps(), ERROR_TOLERANCE);
	}

	@Test
	void testGetPreviousNonGaapEps() {
		AssetEpsHistoricalInfo info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, null, null, 9D, newEpsInfo(1D, null),
				newNonGaapEpsInfo(2D, null, false), 9D, null, null, null, null, null, null, null);

		assertEquals(2D, info.getPreviousNonGaapEps(), ERROR_TOLERANCE);

		info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, null, null, 9D, newEpsInfo(1D, null), null, 9D, null, null,
				null, null, null, null, null);

		assertEquals(1D, info.getPreviousNonGaapEps(), ERROR_TOLERANCE);
	}

	@Test
	void testGetCurrentNonGaapEps() {
		AssetEpsHistoricalInfo info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, newEpsInfo(1D, null),
				newNonGaapEpsInfo(2D, null, false), 9D, null, null, 9D, null, null, null, null, null, null, null);

		assertEquals(2D, info.getCurrentNonGaapEps(), ERROR_TOLERANCE);

		info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, newEpsInfo(1D, null), null, 9D, null, null, 9D, null, null,
				null, null, null, null, null);

		assertEquals(1D, info.getCurrentNonGaapEps(), ERROR_TOLERANCE);
	}

	@Test
	void testGetPreviousPOverE() {
		final AssetPriceInfo beforePrevious = newPriceInfo(2D);
		final AssetPriceInfo previous = newPriceInfo(1D);

		AssetEpsHistoricalInfo info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, null, null, 9D, newEpsInfo(1D, null),
				WITH_BMO, 9D, previous, beforePrevious, null, null, null, null, null);

		assertEquals(2D, info.getPreviousPOverE(), ERROR_TOLERANCE);

		info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, null, null, 9D, newEpsInfo(1D, null), WITH_AMC, 9D,
				previous, beforePrevious, null, null, null, null, null);

		assertEquals(1D, info.getPreviousPOverE(), ERROR_TOLERANCE);

		info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, null, null, 9D, newEpsInfo(1D, null), WITH_BMO, 9D,
				previous, null, null, null, null, null, null);

		assertEquals(1D, info.getPreviousPOverE(), ERROR_TOLERANCE);
	}

	@Test
	void testGetCurrentPOverE() {
		final AssetPriceInfo beforeCurrent = newPriceInfo(2D);
		final AssetPriceInfo current = newPriceInfo(1D);

		AssetEpsHistoricalInfo info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, newEpsInfo(1D, null), WITH_BMO, 9D,
				null, null, 9D, null, null, null, beforeCurrent, current, null, null);

		assertEquals(2D, info.getCurrentPOverE(), ERROR_TOLERANCE);

		info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, newEpsInfo(1D, null), WITH_AMC, 9D, null, null, 9D, null,
				null, null, beforeCurrent, current, null, null);

		assertEquals(1D, info.getCurrentPOverE(), ERROR_TOLERANCE);
	}

	@Test
	void testGetInfoBeforeMinusOneDayEpsAnnouncement() {
		final AssetPriceInfo forBmo = newPriceInfo(2D);
		final AssetPriceInfo forAmc = newPriceInfo(1D);

		AssetEpsHistoricalInfo info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, null, WITH_BMO, 9D, null, null, 9D,
				null, null, forBmo, forAmc, null, null, null);

		assertSame(forBmo, info.getInfoBeforeMinusOneDayEpsAnnouncement());

		info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, null, WITH_AMC, 9D, null, null, 9D, null, null, forBmo,
				forAmc, null, null, null);

		assertSame(forAmc, info.getInfoBeforeMinusOneDayEpsAnnouncement());
	}

	@Test
	void testGetInfoBeforeEpsAnnouncement() {
		final AssetPriceInfo forBmo = newPriceInfo(2D);
		final AssetPriceInfo forAmc = newPriceInfo(1D);

		AssetEpsHistoricalInfo info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, null, WITH_BMO, 9D, null, null, 9D,
				null, null, null, forBmo, forAmc, null, null);

		assertSame(forBmo, info.getInfoBeforeEpsAnnouncement());

		info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, null, WITH_AMC, 9D, null, null, 9D, null, null, null,
				forBmo, forAmc, null, null);

		assertSame(forAmc, info.getInfoBeforeEpsAnnouncement());
	}

	@Test
	void testGetInfoAfterEpsAnnouncement() {
		final AssetPriceInfo forBmo = newPriceInfo(2D);
		final AssetPriceInfo forAmc = newPriceInfo(1D);

		AssetEpsHistoricalInfo info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, null, WITH_BMO, 9D, null, null, 9D,
				null, null, null, null, forBmo, forAmc, null);

		assertSame(forBmo, info.getInfoAfterEpsAnnouncement());

		info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, null, WITH_AMC, 9D, null, null, 9D, null, null, null, null,
				forBmo, forAmc, null);

		assertSame(forAmc, info.getInfoAfterEpsAnnouncement());
	}

	@Test
	void testGetInfoAfterPlusOneDayEpsAnnouncement() {
		final AssetPriceInfo forBmo = newPriceInfo(2D);
		final AssetPriceInfo forAmc = newPriceInfo(1D);

		AssetEpsHistoricalInfo info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, null, WITH_BMO, 9D, null, null, 9D,
				null, null, null, null, null, forBmo, forAmc);

		assertSame(forBmo, info.getInfoAfterPlusOneDayEpsAnnouncement());

		info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, null, WITH_AMC, 9D, null, null, 9D, null, null, null, null,
				null, forBmo, forAmc);

		assertSame(forAmc, info.getInfoAfterPlusOneDayEpsAnnouncement());
	}

	@Test
	void testIsGoodForNothing() {
		final AssetEpsHistoricalInfo info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, null, null, 9D, null, null, 9D,
				null, null, null, null, null, null, null);

		assertFalse(info.isGoodForAfterPlusOneDayEpsTraining());
		assertFalse(info.isGoodForAfterEpsTraining());
		assertFalse(info.isGoodForAfterEpsPrediction());
	}

	@Test
	void testIsGoodForAfterPlusOneDayEpsTraining() {
		final AssetPriceInfo forBmo = newPriceInfo(2D);
		final AssetPriceInfo forAmc = newPriceInfo(1D);

		AssetEpsHistoricalInfo info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, null, WITH_BMO, 9D, null, null, 9D,
				null, null, forBmo, forBmo, forBmo, forBmo, null);

		assertTrue(info.isGoodForAfterPlusOneDayEpsTraining());
		assertTrue(info.isGoodForAfterEpsTraining());
		assertTrue(info.isGoodForAfterEpsPrediction());

		info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, null, WITH_AMC, 9D, null, null, 9D, null, null, null,
				forAmc, forAmc, forAmc, forAmc);

		assertTrue(info.isGoodForAfterPlusOneDayEpsTraining());
		assertTrue(info.isGoodForAfterEpsTraining());
		assertTrue(info.isGoodForAfterEpsPrediction());
	}

	@Test
	void testIsGoodForAfterEpsTraining() {
		final AssetPriceInfo forBmo = newPriceInfo(2D);
		final AssetPriceInfo forAmc = newPriceInfo(1D);

		AssetEpsHistoricalInfo info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, null, WITH_BMO, 9D, null, null, 9D,
				null, null, forBmo, forBmo, forBmo, null, null);

		assertFalse(info.isGoodForAfterPlusOneDayEpsTraining());
		assertTrue(info.isGoodForAfterEpsTraining());
		assertTrue(info.isGoodForAfterEpsPrediction());

		info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, null, WITH_AMC, 9D, null, null, 9D, null, null, null,
				forAmc, forAmc, forAmc, null);

		assertFalse(info.isGoodForAfterPlusOneDayEpsTraining());
		assertTrue(info.isGoodForAfterEpsTraining());
		assertTrue(info.isGoodForAfterEpsPrediction());
	}

	@Test
	void testIsGoodForAfterEpsPrediction() {
		final AssetPriceInfo forBmo = newPriceInfo(2D);
		final AssetPriceInfo forAmc = newPriceInfo(1D);

		AssetEpsHistoricalInfo info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, null, WITH_BMO, 9D, null, null, 9D,
				null, null, forBmo, forBmo, null, null, null);

		assertFalse(info.isGoodForAfterPlusOneDayEpsTraining());
		assertFalse(info.isGoodForAfterEpsTraining());
		assertTrue(info.isGoodForAfterEpsPrediction());

		info = new AssetEpsHistoricalInfo(TEST_ASSET, 1, 1, null, WITH_AMC, 9D, null, null, 9D, null, null, null,
				forAmc, forAmc, null, null);

		assertFalse(info.isGoodForAfterPlusOneDayEpsTraining());
		assertFalse(info.isGoodForAfterEpsTraining());
		assertTrue(info.isGoodForAfterEpsPrediction());
	}
}
