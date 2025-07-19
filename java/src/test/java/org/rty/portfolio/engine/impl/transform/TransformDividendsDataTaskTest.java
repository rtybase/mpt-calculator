package org.rty.portfolio.engine.impl.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rty.portfolio.engine.AbstractTask;

public class TransformDividendsDataTaskTest {
	private static final String INPUT_FILE = "src/test/resources/data-to-transform/data.csv";
	private static final String DIR = "src/test/resources/csv-test/";
	private static final String OUTPUT_FILE = DIR + "dividends.csv";

	private static final String ASSET_NAME = "asset";

	private TransformDividendsDataTask task;

	@BeforeEach
	void setup() throws IOException {
		Files.createDirectories(Paths.get(DIR));

		task = new TransformDividendsDataTask();
	}

	@AfterEach
	void cleanup() throws IOException {
		Files.deleteIfExists(Paths.get(OUTPUT_FILE));
	}

	@Test
	void testTransformation() throws Exception {
		task.execute(Map.of(AbstractTask.INPUT_FILE_PARAM, INPUT_FILE,
				AbstractTask.OUT_SYMBOL, ASSET_NAME,
				AbstractTask.OUTPUT_FILE_PARAM, OUTPUT_FILE,
				AbstractTask.DATE_VALUE_INDEX_PARAM, "0",
				AbstractTask.PRICE_VALUE_INDEX_PARAM, "1",
				AbstractTask.DATE_FORMAT_PARAM, "MMM d, yyyy"));

		verifyFileContent();
	}

	private static void verifyFileContent() throws Exception {
		List<String> content = Files.readAllLines(Paths.get(OUTPUT_FILE));

		assertEquals(1, content.size());
		assertEquals("\"asset\",\"0.83\",\"2025-05-15\"", content.get(0));
	}

}
