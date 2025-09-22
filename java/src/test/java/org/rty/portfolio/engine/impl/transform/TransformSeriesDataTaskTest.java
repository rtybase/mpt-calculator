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

class TransformSeriesDataTaskTest {
	private static final String INPUT_FILE = "src/test/resources/data-to-transform/data.csv";
	private static final String DIR = "src/test/resources/csv-test/";
	private static final String OUTPUT_FILE = DIR + "prices.csv";

	private static final String ASSET_NAME = "asset";

	private TransformSeriesDataTask task;

	@BeforeEach
	void setup() throws IOException {
		Files.createDirectories(Paths.get(DIR));

		task = new TransformSeriesDataTask();
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
				AbstractTask.PRICE_VALUE_INDEX_PARAM, "4",
				AbstractTask.DATE_FORMAT_PARAM, "MMM d, yyyy"));

		verifyFileContent();
	}

	@Test
	void testTransformationWithVolumes() throws Exception {
		task.execute(Map.of(AbstractTask.INPUT_FILE_PARAM, INPUT_FILE,
				AbstractTask.OUT_SYMBOL, ASSET_NAME,
				AbstractTask.OUTPUT_FILE_PARAM, OUTPUT_FILE,
				AbstractTask.DATE_VALUE_INDEX_PARAM, "0",
				AbstractTask.PRICE_VALUE_INDEX_PARAM, "4",
				AbstractTask.VOLUME_VALUE_INDEX_PARAM, "6",
				AbstractTask.DATE_FORMAT_PARAM, "MMM d, yyyy"));

		verifyFileContentWithVolumes();
	}

	private static void verifyFileContent() throws Exception {
		List<String> content = Files.readAllLines(Paths.get(OUTPUT_FILE));

		assertEquals(2, content.size());
		assertEquals("\"asset\",\"505.62\",\"52.49\",\"11.583872\",\"2025-07-16\",\"\",\"\"", content.get(0));
		assertEquals("\"asset\",\"511.7\",\"6.08\",\"1.202484\",\"2025-07-17\",\"\",\"\"", content.get(1));
	}

	private static void verifyFileContentWithVolumes() throws Exception {
		List<String> content = Files.readAllLines(Paths.get(OUTPUT_FILE));

		assertEquals(2, content.size());
		assertEquals("\"asset\",\"505.62\",\"52.49\",\"11.583872\",\"2025-07-16\",\"1.51544E7\",\"-31.092246\"", content.get(0));
		assertEquals("\"asset\",\"511.7\",\"6.08\",\"1.202484\",\"2025-07-17\",\"1.75031E7\",\"15.498469\"", content.get(1));
	}
}
