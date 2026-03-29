package org.rty.portfolio.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.rty.portfolio.core.utils.CommonTestRoutines.ERROR_TOLERANCE;
import static org.rty.portfolio.core.utils.CommonTestRoutines.TEST_ASSET;

import org.junit.jupiter.api.Test;

class AssetFinancialInfoTest {
	@Test
	void testWithNull() {
		final AssetFinancialInfo info = new AssetFinancialInfo(TEST_ASSET, null, null, null, null, null, null, null,
				null, null);

		assertEquals(0D, info.currentRatio(), ERROR_TOLERANCE);
		assertEquals(0D, info.totalRatio(), ERROR_TOLERANCE);
		assertEquals(0D, info.debtOverEquityCalculated(), ERROR_TOLERANCE);
		assertEquals(0D, info.debtOverEquityReported(), ERROR_TOLERANCE);
		assertEquals(0D, info.freeCashFlowPerShare(), ERROR_TOLERANCE);
		assertEquals(0D, info.bookValuePerShare(), ERROR_TOLERANCE);
	}

	@Test
	void testCurrentRatio() {
		AssetFinancialInfo info = new AssetFinancialInfo(TEST_ASSET, null, 1D, null, null, null, null, null, null,
				null);
		assertEquals(0D, info.currentRatio(), ERROR_TOLERANCE);

		info = new AssetFinancialInfo(TEST_ASSET, null, null, 2D, null, null, null, null, null, null);
		assertEquals(0D, info.currentRatio(), ERROR_TOLERANCE);

		info = new AssetFinancialInfo(TEST_ASSET, null, 1D, 0D, null, null, null, null, null, null);
		assertEquals(0D, info.currentRatio(), ERROR_TOLERANCE);

		info = new AssetFinancialInfo(TEST_ASSET, null, 1D, 2D, null, null, null, null, null, null);
		assertEquals(0.5D, info.currentRatio(), ERROR_TOLERANCE);
	}

	@Test
	void testTotalRatio() {
		AssetFinancialInfo info = new AssetFinancialInfo(TEST_ASSET, null, null, null, 1D, null, null, null, null,
				null);
		assertEquals(0D, info.totalRatio(), ERROR_TOLERANCE);

		info = new AssetFinancialInfo(TEST_ASSET, null, null, null, null, 2D, null, null, null, null);
		assertEquals(0D, info.totalRatio(), ERROR_TOLERANCE);

		info = new AssetFinancialInfo(TEST_ASSET, null, null, null, 1D, 0D, null, null, null, null);
		assertEquals(0D, info.totalRatio(), ERROR_TOLERANCE);

		info = new AssetFinancialInfo(TEST_ASSET, null, null, null, 1D, 2D, null, null, null, null);
		assertEquals(0.5D, info.totalRatio(), ERROR_TOLERANCE);
	}

	@Test
	void testDebtOverEquityCalculated() {
		AssetFinancialInfo info = new AssetFinancialInfo(TEST_ASSET, null, null, null, 1D, null, null, null, null,
				null);
		assertEquals(0D, info.debtOverEquityCalculated(), ERROR_TOLERANCE);

		info = new AssetFinancialInfo(TEST_ASSET, null, null, null, null, 2D, null, null, null, null);
		assertEquals(0D, info.debtOverEquityCalculated(), ERROR_TOLERANCE);

		info = new AssetFinancialInfo(TEST_ASSET, null, null, null, 1D, 1D, null, null, null, null);
		assertEquals(0D, info.debtOverEquityCalculated(), ERROR_TOLERANCE);

		info = new AssetFinancialInfo(TEST_ASSET, null, null, null, 3D, 1D, null, null, null, null);
		assertEquals(0.5D, info.debtOverEquityCalculated(), ERROR_TOLERANCE);
	}

	@Test
	void testDebtOverEquityReported() {
		AssetFinancialInfo info = new AssetFinancialInfo(TEST_ASSET, null, null, null, 1D, null, null, null, null,
				null);
		assertEquals(0D, info.debtOverEquityReported(), ERROR_TOLERANCE);

		info = new AssetFinancialInfo(TEST_ASSET, null, null, null, null, 2D, null, null, null, null);
		assertEquals(0D, info.debtOverEquityReported(), ERROR_TOLERANCE);

		info = new AssetFinancialInfo(TEST_ASSET, null, null, null, 1D, 1D, null, null, null, null);
		assertEquals(0D, info.debtOverEquityReported(), ERROR_TOLERANCE);

		info = new AssetFinancialInfo(TEST_ASSET, null, null, null, 3D, 1D, null, null, null, null);
		assertEquals(0.5D, info.debtOverEquityReported(), ERROR_TOLERANCE);

		info = new AssetFinancialInfo(TEST_ASSET, null, null, null, 3D, 1D, 0D, null, null, null);
		assertEquals(0D, info.debtOverEquityReported(), ERROR_TOLERANCE);

		info = new AssetFinancialInfo(TEST_ASSET, null, null, null, null, 1D, 4D, null, null, null);
		assertEquals(0.25D, info.debtOverEquityReported(), ERROR_TOLERANCE);
	}

	@Test
	void testFreeCashFlowPerShare() {
		AssetFinancialInfo info = new AssetFinancialInfo(TEST_ASSET, null, null, null, null, null, null, null, null,
				null);
		assertEquals(0D, info.freeCashFlowPerShare(), ERROR_TOLERANCE);

		info = new AssetFinancialInfo(TEST_ASSET, null, null, null, null, null, null, 1D, null, 0D);
		assertEquals(0D, info.freeCashFlowPerShare(), ERROR_TOLERANCE);

		info = new AssetFinancialInfo(TEST_ASSET, null, null, null, null, null, null, null, null, 1D);
		assertEquals(0D, info.freeCashFlowPerShare(), ERROR_TOLERANCE);

		info = new AssetFinancialInfo(TEST_ASSET, null, null, null, null, null, null, 1D, null, 1D);
		assertEquals(1000D, info.freeCashFlowPerShare(), ERROR_TOLERANCE);

		info = new AssetFinancialInfo(TEST_ASSET, null, null, null, null, null, null, null, 1D, 1D);
		assertEquals(0D, info.freeCashFlowPerShare(), ERROR_TOLERANCE);

		info = new AssetFinancialInfo(TEST_ASSET, null, null, null, null, null, null, 2D, 1D, 0D);
		assertEquals(0D, info.freeCashFlowPerShare(), ERROR_TOLERANCE);

		info = new AssetFinancialInfo(TEST_ASSET, null, null, null, null, null, null, 2D, 1D, 1D);
		assertEquals(1000D, info.freeCashFlowPerShare(), ERROR_TOLERANCE);

		info = new AssetFinancialInfo(TEST_ASSET, null, null, null, null, null, null, 2D, -1D, 1D);
		assertEquals(1000D, info.freeCashFlowPerShare(), ERROR_TOLERANCE);
	}

	@Test
	void testBookValuePerShare() {
		AssetFinancialInfo info = new AssetFinancialInfo(TEST_ASSET, null, null, null, 1D, null, null, null, null,
				null);
		assertEquals(0D, info.bookValuePerShare(), ERROR_TOLERANCE);

		info = new AssetFinancialInfo(TEST_ASSET, null, null, null, null, 2D, null, null, null, null);
		assertEquals(0D, info.bookValuePerShare(), ERROR_TOLERANCE);

		info = new AssetFinancialInfo(TEST_ASSET, null, null, null, 1D, 2D, null, null, null, 0D);
		assertEquals(0D, info.bookValuePerShare(), ERROR_TOLERANCE);

		info = new AssetFinancialInfo(TEST_ASSET, null, null, null, 1D, 2D, null, null, null, 2D);
		assertEquals(-500D, info.bookValuePerShare(), ERROR_TOLERANCE);
	}
}
