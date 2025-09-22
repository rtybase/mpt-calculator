package org.rty.portfolio.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
	void testHistoryWithNullVolumes() {
		accumulator.add(dateFrom(4), 1.0D, null);
		accumulator.add(dateFrom(5), 1.5D, null);
		accumulator.add(dateFrom(1), 0.1D, null);
		accumulator.add(dateFrom(3), 0.6D, null);
		accumulator.add(dateFrom(2), 0.3D, null);

		List<AssetPriceInfo> history = accumulator.getChangeHistory();
		assertEquals(history.size(), 4);

		assertEquals(dateFrom(2), history.get(0).date);
		verifyPriceDetails(history.get(0), 0.3D, 0.2D, 200.0D);
		verifyNameAndVolumes(history.get(0));

		assertEquals(dateFrom(3), history.get(1).date);
		verifyPriceDetails(history.get(1), 0.6D, 0.3D, 100.0D);
		verifyNameAndVolumes(history.get(1));

		assertEquals(dateFrom(4), history.get(2).date);
		verifyPriceDetails(history.get(2), 1.0D, 0.4D, 66.66666D);
		verifyNameAndVolumes(history.get(2));

		assertEquals(dateFrom(5), history.get(3).date);
		verifyPriceDetails(history.get(3), 1.5D, 0.5D, 50.0D);
		verifyNameAndVolumes(history.get(3));
	}

	@Test
	void testHistory() {
		accumulator.add(dateFrom(4), 1.0D, 1.0D);
		accumulator.add(dateFrom(5), 1.5D, 1.5D);
		accumulator.add(dateFrom(1), 0.1D, 0.1D);
		accumulator.add(dateFrom(3), 0.6D, 0.6D);
		accumulator.add(dateFrom(2), 0.3D, 0.3D);

		List<AssetPriceInfo> history = accumulator.getChangeHistory();
		assertEquals(history.size(), 4);

		assertEquals(dateFrom(2), history.get(0).date);
		verifyPriceDetails(history.get(0), 0.3D, 0.2D, 200.0D);
		verifyNameAndVolumes(history.get(0), 0.3D, 200.0D);

		assertEquals(dateFrom(3), history.get(1).date);
		verifyPriceDetails(history.get(1), 0.6D, 0.3D, 100.0D);
		verifyNameAndVolumes(history.get(1), 0.6D, 100.0D);

		assertEquals(dateFrom(4), history.get(2).date);
		verifyPriceDetails(history.get(2), 1.0D, 0.4D, 66.66666D);
		verifyNameAndVolumes(history.get(2), 1.0D, 66.66666D);

		assertEquals(dateFrom(5), history.get(3).date);
		verifyPriceDetails(history.get(3), 1.5D, 0.5D, 50.0D);
		verifyNameAndVolumes(history.get(3), 1.5D, 50.0D);
	}

	@Test
	void testEmptyHistory() {
		List<AssetPriceInfo> history = accumulator.getChangeHistory();
		assertTrue(history.isEmpty());
	}

	private static void verifyPriceDetails(AssetPriceInfo priceInfo, double expectedPrice, double expectedChange,
			double expectedRate) {
		assertEquals(expectedPrice, priceInfo.price, ERROR_TOLERANCE);
		assertEquals(expectedChange, priceInfo.change, ERROR_TOLERANCE);
		assertEquals(expectedRate, priceInfo.rate, ERROR_TOLERANCE);
	}

	private static void verifyNameAndVolumes(AssetPriceInfo priceInfo) {
		assertEquals(ASSET_NAME, priceInfo.assetName);
		assertNull(priceInfo.volume);
		assertNull(priceInfo.volumeChangeRate);
	}

	private static void verifyNameAndVolumes(AssetPriceInfo priceInfo, double expectedVolume,
			double expectedVolumeChangeRate) {
		assertEquals(ASSET_NAME, priceInfo.assetName);
		assertEquals(expectedVolume, priceInfo.volume, ERROR_TOLERANCE);
		assertEquals(expectedVolumeChangeRate, priceInfo.volumeChangeRate, ERROR_TOLERANCE);
	}

	private static Date dateFrom(int day) {
		return new Date(101, 1, day);
	}
}
