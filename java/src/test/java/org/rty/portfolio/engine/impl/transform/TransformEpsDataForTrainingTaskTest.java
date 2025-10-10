package org.rty.portfolio.engine.impl.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.rty.portfolio.core.utils.CommonTestRoutines.D_2025_07_17;
import static org.rty.portfolio.core.utils.CommonTestRoutines.ERROR_TOLERANCE;
import static org.rty.portfolio.core.utils.CommonTestRoutines.TEST_ASSET;
import static org.rty.portfolio.core.utils.CommonTestRoutines.dateFrom;
import static org.rty.portfolio.core.utils.CommonTestRoutines.newEpsInfo;
import static org.rty.portfolio.core.utils.CommonTestRoutines.newNonGaapEpsInfo;
import static org.rty.portfolio.core.utils.CommonTestRoutines.newPriceInfo;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rty.portfolio.core.AssetEpsHistoricalInfo;
import org.rty.portfolio.core.AssetEpsInfo;
import org.rty.portfolio.core.AssetNonGaapEpsInfo;
import org.rty.portfolio.core.AssetPriceInfo;

class TransformEpsDataForTrainingTaskTest {
	private Map<String, NavigableMap<Date, AssetPriceInfo>> priceStore;
	private Map<String, NavigableMap<Date, AssetNonGaapEpsInfo>> nonGaapEpsStore;
	private Map<String, Pair<Integer, Integer>> stocksAndsectorsStore;
	private Map<String, NavigableMap<Date, Double>> stocksAndFScoreStore;
	private NavigableMap<Date, AssetEpsInfo> epsData;

	private List<AssetEpsHistoricalInfo> dataForTraining;
	private List<AssetEpsHistoricalInfo> dataFor2DPrediction;
	private List<AssetEpsHistoricalInfo> dataFor1DPrediction;

	private AssetEpsInfo previousEps;
	private AssetNonGaapEpsInfo previousNonGaapEps;
	private AssetPriceInfo priceAtPreviousEps;
	private AssetPriceInfo priceBeforePreviousEps;

	private AssetEpsInfo currentEps;
	private AssetNonGaapEpsInfo currentNonGaapEps;
	private AssetPriceInfo price2DaysBeforeCurrentEps;
	private AssetPriceInfo priceBeforeCurrentEps;
	private AssetPriceInfo priceAtCurrentEps;
	private AssetPriceInfo priceAfterCurrentEps;
	private AssetPriceInfo price2DaysAfterCurrentEps;

	@BeforeEach
	void setup() {
		priceStore = new HashMap<>();
		nonGaapEpsStore = new HashMap<>();
		stocksAndsectorsStore = new HashMap<>();
		stocksAndFScoreStore = new HashMap<>();
		epsData = new TreeMap<>();

		dataForTraining = new ArrayList<>();
		dataFor2DPrediction = new ArrayList<>();
		dataFor1DPrediction = new ArrayList<>();
	}

	@Test
	void testNothingCollected() {
		verifyNothingCollected();

		collectDataFor(addEpsDataToStore(D_2025_07_17));

		verifyNothingCollected();
	}

	@Test
	void testGoodForPredictionOnlyCollected() {
		setSectorDetails();
		setPreviousEpsData();
		setCurrentEpsBasicData();
		addFScoreToStore(9D, dateFrom(15));
		addFScoreToStore(8D, dateFrom(12));

		verifyNothingCollected();

		collectDataFor(currentEps);

		assertTrue(dataForTraining.isEmpty());
		assertTrue(dataFor2DPrediction.isEmpty());
		assertEquals(1, dataFor1DPrediction.size());

		final AssetEpsHistoricalInfo result = dataFor1DPrediction.get(0);

		verifyPreviousEpsData(result);
		verifyCurrentEpsBasicData(result);

		assertNull(result.price2DaysAfterCurrentEps);
		assertNull(result.priceAfterCurrentEps);
		assertNull(result.priceAtCurrentEps);

		assertEquals(9D, result.currentFScore, ERROR_TOLERANCE);
		assertEquals(8D, result.previousFScore, ERROR_TOLERANCE);
	}

	@Test
	void testGoodForPredictionAndTrainingCollected() {
		setSectorDetails();
		setPreviousEpsData();
		setCurrentEpsBasicData();
		addFScoreToStore(9D, dateFrom(15));
		priceAtCurrentEps = addPriceDataToStore(D_2025_07_17);

		verifyNothingCollected();

		collectDataFor(currentEps);

		assertTrue(dataForTraining.isEmpty());
		assertEquals(1, dataFor2DPrediction.size());
		assertTrue(dataFor1DPrediction.isEmpty());

		final AssetEpsHistoricalInfo result = dataFor2DPrediction.get(0);

		verifyPreviousEpsData(result);
		verifyCurrentEpsBasicData(result);
		assertSame(priceAtCurrentEps, result.priceAtCurrentEps);

		assertNull(result.price2DaysAfterCurrentEps);
		assertNull(result.priceAfterCurrentEps);
	}

	@Test
	void testGoodForTrainingOnlyCollected() {
		setSectorDetails();
		setPreviousEpsData();
		setCurrentEpsBasicData();
		addFScoreToStore(9D, dateFrom(15));
		priceAtCurrentEps = addPriceDataToStore(D_2025_07_17);
		priceAfterCurrentEps = addPriceDataToStore(dateFrom(18));
		price2DaysAfterCurrentEps = addPriceDataToStore(dateFrom(19));

		verifyNothingCollected();

		collectDataFor(currentEps);

		assertEquals(1, dataForTraining.size());
		assertTrue(dataFor2DPrediction.isEmpty());
		assertTrue(dataFor1DPrediction.isEmpty());

		final AssetEpsHistoricalInfo result = dataForTraining.get(0);

		verifyPreviousEpsData(result);
		verifyCurrentEpsBasicData(result);
		assertSame(priceAtCurrentEps, result.priceAtCurrentEps);
		assertSame(priceAfterCurrentEps, result.priceAfterCurrentEps);
		assertSame(price2DaysAfterCurrentEps, result.price2DaysAfterCurrentEps);
	}

	private void setCurrentEpsBasicData() {
		currentEps = addEpsDataToStore(D_2025_07_17);
		currentNonGaapEps = addNonGaapEpsDataToStore(D_2025_07_17);
		price2DaysBeforeCurrentEps = addPriceDataToStore(dateFrom(15));
		priceBeforeCurrentEps = addPriceDataToStore(dateFrom(16));
	}

	private void verifyCurrentEpsBasicData(final AssetEpsHistoricalInfo result) {
		assertSame(currentEps, result.currentEps);
		assertSame(currentNonGaapEps, result.currentNonGaapEps);
		assertSame(price2DaysBeforeCurrentEps, result.price2DaysBeforeCurrentEps);
		assertSame(priceBeforeCurrentEps, result.priceBeforeCurrentEps);
	}

	private void setPreviousEpsData() {
		final Date previousEpsDate = dateFrom(13);

		previousEps = addEpsDataToStore(previousEpsDate);
		previousNonGaapEps = addNonGaapEpsDataToStore(previousEpsDate);
		priceAtPreviousEps = addPriceDataToStore(previousEpsDate);
		priceBeforePreviousEps = addPriceDataToStore(dateFrom(12));
	}

	private void verifyPreviousEpsData(final AssetEpsHistoricalInfo result) {
		assertSame(previousEps, result.previousEps);
		assertSame(previousNonGaapEps, result.previousNonGaapEps);
		assertSame(priceAtPreviousEps, result.priceAtPreviousEps);
		assertSame(priceBeforePreviousEps, result.priceBeforePreviousEps);
	}

	private void verifyNothingCollected() {
		assertTrue(dataForTraining.isEmpty());
		assertTrue(dataFor2DPrediction.isEmpty());
		assertTrue(dataFor1DPrediction.isEmpty());
	}

	private void collectDataFor(AssetEpsInfo epsInfo) {
		final Map.Entry<Date, AssetEpsInfo> entry = new AbstractMap.SimpleEntry<>(epsInfo.date, epsInfo);

		TransformEpsDataForTrainingTask.collectHistoricalData(priceStore, nonGaapEpsStore, stocksAndsectorsStore,
				stocksAndFScoreStore, dataForTraining, dataFor2DPrediction, dataFor1DPrediction, TEST_ASSET, epsData,
				entry);
	}

	private AssetNonGaapEpsInfo addNonGaapEpsDataToStore(Date date) {
		final AssetNonGaapEpsInfo info = newNonGaapEpsInfo(1D, null, false, date);

		final NavigableMap<Date, AssetNonGaapEpsInfo> details = nonGaapEpsStore.computeIfAbsent(TEST_ASSET,
				k -> new TreeMap<>());
		details.put(date, info);
		return info;
	}

	private AssetPriceInfo addPriceDataToStore(Date date) {
		final AssetPriceInfo info = newPriceInfo(1D, date);

		final NavigableMap<Date, AssetPriceInfo> details = priceStore.computeIfAbsent(TEST_ASSET, k -> new TreeMap<>());
		details.put(date, info);
		return info;
	}

	private AssetEpsInfo addEpsDataToStore(Date date) {
		final AssetEpsInfo info = newEpsInfo(1D, null, date);
		epsData.put(date, info);
		return info;
	}

	private void setSectorDetails() {
		stocksAndsectorsStore.put(TEST_ASSET, new Pair<>(1, 1));
	}

	private void addFScoreToStore(double fscore, Date date) {
		final NavigableMap<Date, Double> details = stocksAndFScoreStore.computeIfAbsent(TEST_ASSET,
				k -> new TreeMap<>());
		details.put(date, fscore);
	}
}
