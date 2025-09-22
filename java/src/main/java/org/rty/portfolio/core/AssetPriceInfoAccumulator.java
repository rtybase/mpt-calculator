package org.rty.portfolio.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.apache.commons.math3.util.Pair;
import org.rty.portfolio.core.utils.DataHandlingUtil;
import org.rty.portfolio.math.Calculator;

/**
 * No thread safety.
 *
 */
public class AssetPriceInfoAccumulator {
	private final String assetName;
	private final TreeMap<Date, Pair<Double, Double>> prices = new TreeMap<>();

	public AssetPriceInfoAccumulator(String assetName) {
		this.assetName = Objects.requireNonNull(assetName, "assetName must not be null!");
	}

	public void add(Date date, double price, Double volume) {
		Objects.requireNonNull(date, "date must not be null!");
		prices.put(date, new Pair<>(price, volume));
	}

	public List<AssetPriceInfo> getChangeHistory() {
		if (prices.isEmpty()) {
			return Collections.emptyList();
		} else {
			List<AssetPriceInfo> result = new ArrayList<>(prices.size());

			Double lastPrice = null;
			Double lastVolume = null;

			for (Map.Entry<Date, Pair<Double, Double>> entry : prices.entrySet()) {
				Date date = entry.getKey();
				double price = entry.getValue().getFirst();
				Double volume = entry.getValue().getSecond();

				if (lastPrice != null) {
					double change = Calculator.calculateChange(price, lastPrice);
					double rate = Calculator.calculateRate(price, lastPrice);
					Double volumeChangeRate = calculateVolumeChangeRate(volume, lastVolume);
					result.add(new AssetPriceInfo(assetName, price, change, rate, volume, volumeChangeRate, date));
				}

				lastPrice = price;
				lastVolume = volume;
			}

			return result;
		}
	}

	private static Double calculateVolumeChangeRate(Double volume, Double lastVolume) {
		if (DataHandlingUtil.allNotNull(volume, lastVolume)) {
			return Calculator.calculateRate(volume, lastVolume);
		}

		return null;
	}
}
