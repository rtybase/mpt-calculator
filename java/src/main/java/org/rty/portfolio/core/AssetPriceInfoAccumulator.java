package org.rty.portfolio.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.rty.portfolio.math.Calculator;

/**
 * No thread safety.
 *
 */
public class AssetPriceInfoAccumulator {
	private final String assetName;
	private final TreeMap<Date, Double> prices = new TreeMap<>();

	public AssetPriceInfoAccumulator(String assetName) {
		this.assetName = Objects.requireNonNull(assetName, "assetName must not be null!");
	}

	public void add(Date date, double price) {
		Objects.requireNonNull(date, "date must not be null!");
		prices.put(date, price);
	}

	public List<AssetPriceInfo> getChangeHistory() {
		if (prices.isEmpty()) {
			return Collections.emptyList();
		} else {
			List<AssetPriceInfo> result = new ArrayList<>(prices.size());

			Double lastPrice = null;
			for (Map.Entry<Date, Double> entry : prices.entrySet()) {
				Date date = entry.getKey();
				double price = entry.getValue();

				if (lastPrice != null) {
					double change = Calculator.calculateChange(price, lastPrice);
					double rate = Calculator.calculateRate(price, lastPrice);
					result.add(new AssetPriceInfo(assetName, price, change, rate, date));
				}

				lastPrice = price;
			}

			return result;
		}
	}
}
