package org.rty.portfolio.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rty.portfolio.core.AssetPriceInfo;

class CsvWriterTest {
	private static final String CSV_PRICE_CONTENT = "\"asset\",\"10.01\",\"0.1\",\"0.01\",\"2001-02-01\"";
	private static final String CSV_RAW_CONTENT = "\"asset\",\"10.01\"";
	private static final String DIR = "src/test/resources/csv-test/";
	private static final String FILE = DIR + "out.csv";

	private static final String ASSET_NAME = "asset";
	private static final double PRICE = 10.01D;
	private static final double CHANGE = 0.1D;
	private static final double RATE = 0.01D;
	private static final Date DATE = new Date(101, 1, 1);

	private AssetPriceInfo priceInfo;

	@BeforeEach
	void setup() throws IOException {
		Files.createDirectories(Paths.get(DIR));

		priceInfo = new AssetPriceInfo(ASSET_NAME, PRICE, CHANGE, RATE, DATE);
	}

	@AfterEach
	void cleanup() throws IOException {
		Files.deleteIfExists(Paths.get(FILE));
	}

	@Test
	void testWriteOne() throws IOException {
		CsvWriter<AssetPriceInfo> writer = new CsvWriter<>(FILE);
		writer.write(priceInfo);
		writer.close();

		verifyFileContent(CSV_PRICE_CONTENT);
	}

	@Test
	void testWriteList() throws IOException {
		CsvWriter<AssetPriceInfo> writer = new CsvWriter<>(FILE);
		writer.write(Arrays.asList(priceInfo));
		writer.close();

		verifyFileContent(CSV_PRICE_CONTENT);
	}

	@Test
	void testWriteRaw() throws IOException {
		CsvWriter<AssetPriceInfo> writer = new CsvWriter<>(FILE);
		writer.write(new String[] { ASSET_NAME, "" + PRICE });
		writer.close();

		verifyFileContent(CSV_RAW_CONTENT);
	}

	private static void verifyFileContent(String expectedContent) throws IOException {
		List<String> content = Files.readAllLines(Paths.get(FILE));
		assertEquals(1, content.size());
		assertEquals(expectedContent, content.get(0));
	}
}
