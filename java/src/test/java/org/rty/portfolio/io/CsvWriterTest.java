package org.rty.portfolio.io;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rty.portfolio.core.AssetPriceInfo;

public class CsvWriterTest {
	private static final String CSV_CONTENT = "\"asset\",\"10.01\",\"0.1\",\"0.01\",\"2001-02-01\"";
	private static final String DIR = "src/test/resources/csv-test/";
	private static final String FILE = DIR + "out.csv";

	private static final String ASSET_NAME = "asset";
	private static final double PRICE = 10.01D;
	private static final double CHANGE = 0.1D;
	private static final double RATE = 0.01D;
	private static final Date DATE = new Date(101, 1, 1);

	private AssetPriceInfo priceInfo;

	@Before
	public void setup() throws IOException {
		Files.createDirectories(Paths.get(DIR));

		priceInfo = new AssetPriceInfo(ASSET_NAME, PRICE, CHANGE, RATE, DATE);
	}

	@After
	public void cleanup() throws IOException {
		Files.deleteIfExists(Paths.get(FILE));
	}

	@Test
	public void testWriteOne() throws IOException {
		CsvWriter writer = new CsvWriter(FILE);
		writer.write(priceInfo);
		writer.close();

		verifyFileContent();
	}

	@Test
	public void testWriteList() throws IOException {
		CsvWriter writer = new CsvWriter(FILE);
		writer.write(Arrays.asList(priceInfo));
		writer.close();

		verifyFileContent();
	}

	private static void verifyFileContent() throws IOException {
		List<String> content = Files.readAllLines(Paths.get(FILE));
		assertEquals(content.size(), 1);
		assertEquals(content.get(0), CSV_CONTENT);
	}
}
