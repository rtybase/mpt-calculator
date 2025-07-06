package org.rty.portfolio.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AssetPriceInfoAccumulatorTest {
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

		assertEquals(history.get(0).getDate(), new Date(101, 1, 2));
		assertEquals(history.get(0).getPrice(), 0.3D, 0.00001D);
		assertEquals(history.get(0).getChange(), 0.2D, 0.00001D);
		assertEquals(history.get(0).getAssetName(), ASSET_NAME);
		assertEquals(history.get(0).getRate(), 200.0D, 0.00001D);

		assertEquals(history.get(1).getDate(), new Date(101, 1, 3));
		assertEquals(history.get(1).getPrice(), 0.6D, 0.00001D);
		assertEquals(history.get(1).getChange(), 0.3D, 0.00001D);
		assertEquals(history.get(1).getAssetName(), ASSET_NAME);
		assertEquals(history.get(1).getRate(), 100.0D, 0.00001D);

		assertEquals(history.get(2).getDate(), new Date(101, 1, 4));
		assertEquals(history.get(2).getPrice(), 1.0D, 0.00001D);
		assertEquals(history.get(2).getChange(), 0.4D, 0.00001D);
		assertEquals(history.get(2).getAssetName(), ASSET_NAME);
		assertEquals(history.get(2).getRate(), 66.66666D, 0.0001D);

		assertEquals(history.get(3).getDate(), new Date(101, 1, 5));
		assertEquals(history.get(3).getPrice(), 1.5D, 0.00001D);
		assertEquals(history.get(3).getChange(), 0.5D, 0.00001D);
		assertEquals(history.get(3).getAssetName(), ASSET_NAME);
		assertEquals(history.get(3).getRate(), 50.0D, 0.0001D);
	}

	@Test
	void testEmptyHistory() {
		List<AssetPriceInfo> history = accumulator.getChangeHistory();
		assertTrue(history.isEmpty());
	}
}
