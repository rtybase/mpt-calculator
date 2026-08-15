package org.rty.portfolio.io;

import java.util.Arrays;
import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rty.portfolio.core.AssetPriceInfo;

class CsvWriterTest extends TestWithFiles {
	private static final String CSV_PRICE_CONTENT = "\"asset\",\"10.01\",\"0.1\",\"0.01\",\"2001-02-01\",\"\",\"\"";
	private static final String CSV_RAW_CONTENT = "\"asset\",\"10.01\"";

	private static final String ASSET_NAME = "asset";
	private static final double PRICE = 10.01D;
	private static final double CHANGE = 0.1D;
	private static final double RATE = 0.01D;
	private static final Date DATE = new Date(101, 1, 1);

	private AssetPriceInfo priceInfo;

	@BeforeEach
	void setup() throws Exception {
		super.setup();

		priceInfo = new AssetPriceInfo(ASSET_NAME, PRICE, CHANGE, RATE, null, null, DATE);
	}

	@AfterEach
	void cleanup() throws Exception {
		super.cleanup();
	}

	@Test
	void testWriteOne() throws Exception {
		CsvWriter<AssetPriceInfo> writer = new CsvWriter<>(FILE);
		writer.write(priceInfo);
		writer.close();

		verifyFileContent(CSV_PRICE_CONTENT);
	}

	@Test
	void testWriteList() throws Exception {
		CsvWriter<AssetPriceInfo> writer = new CsvWriter<>(FILE);
		writer.write(Arrays.asList(priceInfo));
		writer.close();

		verifyFileContent(CSV_PRICE_CONTENT);
	}

	@Test
	void testWriteRaw() throws Exception {
		CsvWriter<AssetPriceInfo> writer = new CsvWriter<>(FILE);
		writer.write(new String[] { ASSET_NAME, "" + PRICE });
		writer.close();

		verifyFileContent(CSV_RAW_CONTENT);
	}
}
