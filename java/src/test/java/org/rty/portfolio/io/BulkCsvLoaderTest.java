package org.rty.portfolio.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.rty.portfolio.core.AssetPriceInfo;
import org.rty.portfolio.core.utils.ToEntityConvertorsUtil;

class BulkCsvLoaderTest {
	private static final String DIR = "src/test/resources/data-to-load/";
	private static final int NO_OF_COLUMNS = 5;

	@Test
	void testLoadFromFile() throws Exception {
		final List<AssetPriceInfo> result = new ArrayList<>();
		final BulkCsvLoader<AssetPriceInfo> loader = newLoader(result);

		loader.load(DIR + "prices.csv");
		assertEquals(5, result.size());
	}

	@Test
	void testLoadFromFolder() throws Exception {
		final List<AssetPriceInfo> result = new ArrayList<>();
		final BulkCsvLoader<AssetPriceInfo> loader = newLoader(result);

		loader.load(DIR);
		assertEquals(5, result.size());
	}

	private static BulkCsvLoader<AssetPriceInfo> newLoader(List<AssetPriceInfo> entriesToBeAdded) {
		return new BulkCsvLoader<>(NO_OF_COLUMNS) {

			@Override
			protected List<String> saveResults(List<AssetPriceInfo> dataToAdd) throws Exception {
				entriesToBeAdded.addAll(dataToAdd);
				return Collections.emptyList();
			}

			@Override
			protected AssetPriceInfo toEntity(String assetName, String[] line) {
				return ToEntityConvertorsUtil.toAssetPriceInfoEntity(assetName, line);
			}
		};
	}
}
