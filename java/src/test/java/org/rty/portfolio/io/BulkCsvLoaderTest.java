package org.rty.portfolio.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.rty.portfolio.core.AssetPriceInfo;
import org.rty.portfolio.core.utils.ToEntityConvertorsUtil;

class BulkCsvLoaderTest {
	private static final String DIR_FILES_WITHOUT_HEADERS = "src/test/resources/data-to-load/";
	private static final String DIR_FILES_WITH_HEADERS = "src/test/resources/data-to-load-with-header/";
	private static final int NO_OF_COLUMNS = 5;

	@Test
	void testLoadFromFile() throws Exception {
		final List<AssetPriceInfo> result = new ArrayList<>();
		final List<String> headers = new ArrayList<>();

		final BulkCsvLoader<AssetPriceInfo> loader = newLoader(false, result, headers);
		loader.load(DIR_FILES_WITHOUT_HEADERS + "prices.csv");

		verifyContent(3, result, 0, headers);
	}

	@Test
	void testLoadFromFolder() throws Exception {
		final List<AssetPriceInfo> result = new ArrayList<>();
		final List<String> headers = new ArrayList<>();

		final BulkCsvLoader<AssetPriceInfo> loader = newLoader(false, result, headers);
		loader.load(DIR_FILES_WITHOUT_HEADERS);

		verifyContent(3, result, 0, headers);
	}

	@Test
	void testLoadFromFileWithHeader() throws Exception {
		final List<AssetPriceInfo> result = new ArrayList<>();
		final List<String> headers = new ArrayList<>();

		final BulkCsvLoader<AssetPriceInfo> loader = newLoader(true, result, headers);
		loader.load(DIR_FILES_WITH_HEADERS + "prices.csv");

		verifyContent(3, result, 5, headers);
	}

	@Test
	void testLoadFromFolderWithHeaders() throws Exception {
		final List<AssetPriceInfo> result = new ArrayList<>();
		final List<String> headers = new ArrayList<>();

		final BulkCsvLoader<AssetPriceInfo> loader = newLoader(true, result, headers);
		loader.load(DIR_FILES_WITH_HEADERS);

		verifyContent(3, result, 5, headers);
	}

	private void verifyContent(int expectedResultSize, List<AssetPriceInfo> result, int expectedHeaderSize,
			List<String> headers) {
		assertEquals(expectedResultSize, result.size());
		assertEquals(expectedHeaderSize, headers.size());
	}

	private static BulkCsvLoader<AssetPriceInfo> newLoader(boolean hasHeader, List<AssetPriceInfo> entriesToBeAdded,
			List<String> headers) {
		return new BulkCsvLoader<>(NO_OF_COLUMNS, hasHeader) {

			@Override
			protected List<String> saveResults(List<AssetPriceInfo> dataToAdd) throws Exception {
				entriesToBeAdded.addAll(dataToAdd);
				return Collections.emptyList();
			}

			@Override
			protected AssetPriceInfo toEntity(String assetName, String[] line) {
				return ToEntityConvertorsUtil.toAssetPriceInfoEntity(assetName, line);
			}

			@Override
			protected void announceHeaders(String inputFile, String[] headerLine) {
				for (String header : headerLine) {
					headers.add(header);
				}
			}

			@Override
			protected String assetNameFrom(String[] line) {
				return line[0].trim();
			}
		};
	}
}
