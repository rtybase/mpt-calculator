package org.rty.portfolio.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AssetPriceInfoAccumulatorTest {
	private static final double ERROR_TOLERANCE = 0.00001D;

	private static final String ASSET_NAME = "asset";

	private AssetPriceInfoAccumulator accumulator;

	@BeforeEach
	void setup() {
		accumulator = new AssetPriceInfoAccumulator(ASSET_NAME);
	}

	@Test
	void testHistory() {
		accumulator.add(new Date(101, 1, 4), 1.0D);
		accumulator.add(new Date(101, 1, 5), 1.5D);
		accumulator.add(new Date(101, 1, 1), 0.1D);
		accumulator.add(new Date(101, 1, 3), 0.6D);
		accumulator.add(new Date(101, 1, 2), 0.3D);

		List<AssetPriceInfo> history = accumulator.getChangeHistory();
		assertEquals(history.size(), 4);

		assertEquals(new Date(101, 1, 2), history.get(0).date);
		assertEquals(0.3D, history.get(0).price, ERROR_TOLERANCE);
		assertEquals(0.2D, history.get(0).change, ERROR_TOLERANCE);
		assertEquals(ASSET_NAME, history.get(3).assetName);
		assertEquals(200.0D, history.get(0).rate, ERROR_TOLERANCE);

		assertEquals(new Date(101, 1, 3), history.get(1).date);
		assertEquals(0.6D, history.get(1).price, ERROR_TOLERANCE);
		assertEquals(0.3D, history.get(1).change, ERROR_TOLERANCE);
		assertEquals(ASSET_NAME, history.get(3).assetName);
		assertEquals(100.0D, history.get(1).rate, ERROR_TOLERANCE);

		assertEquals(new Date(101, 1, 4), history.get(2).date);
		assertEquals(1.0D, history.get(2).price, ERROR_TOLERANCE);
		assertEquals(0.4D, history.get(2).change, ERROR_TOLERANCE);
		assertEquals(ASSET_NAME, history.get(3).assetName);
		assertEquals(66.66666D, history.get(2).rate, ERROR_TOLERANCE);

		assertEquals(new Date(101, 1, 5), history.get(3).date);
		assertEquals(1.5D, history.get(3).price, ERROR_TOLERANCE);
		assertEquals(0.5D, history.get(3).change, ERROR_TOLERANCE);
		assertEquals(ASSET_NAME, history.get(3).assetName);
		assertEquals(50.0D, history.get(3).rate, ERROR_TOLERANCE);
	}

	@Test
	void testEmptyHistory() {
		List<AssetPriceInfo> history = accumulator.getChangeHistory();
		assertTrue(history.isEmpty());
	}
}
