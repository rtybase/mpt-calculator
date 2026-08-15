package org.rty.portfolio.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TableParserTest extends TestWithFiles {
	private static final String INPUT_FILE_PATTERN = "src/test/resources/html-table-files/f-%d.html";

	@BeforeEach
	void setup() throws Exception {
		super.setup();
	}

	@AfterEach
	void cleanup() throws Exception {
		super.cleanup();
	}

	@Test
	void testParseToCsv1() throws Exception {
		runTest(INPUT_FILE_PATTERN.formatted(1));
	}

	@Test
	void testParseToCsv2() throws Exception {
		runTest(INPUT_FILE_PATTERN.formatted(2));
	}

	@Test
	void testParseToCsv3() throws Exception {
		runTest(INPUT_FILE_PATTERN.formatted(3));
	}

	@Test
	void testParseToCsv4() throws Exception {
		runTest(INPUT_FILE_PATTERN.formatted(4));
	}

	@Test
	void testDeleteContent() {
		assertEquals("", TableParser.deleteContent("", "<SCRIPR>", "</SCRIPR>"));
		assertEquals("48", TableParser.deleteContent("4<SCRIPT>call();</SCRIPT>8", "<SCRIPT>", "</SCRIPT>"));
	}

	@Test
	void testParseToCsvWithoutHeaders() throws Exception {
		TableParser.parseToCsv(INPUT_FILE_PATTERN.formatted(1), FILE, false);

		List<String> content = readContent();
		assertEquals(5, content.size());
		assertEquals("TABLE", content.get(0));
		assertEquals("", content.get(1));
		assertEquals("\"Emil\",\"Tobias\",\"Linus\",", content.get(2));
		assertEquals("\"16\",\"14\",\"10\",", content.get(3));
		assertEquals("", content.get(4));
	}

	@Test
	void testParseToCsv5WithHeaders() throws Exception {
		TableParser.parseToCsv(INPUT_FILE_PATTERN.formatted(5), FILE, true);

		List<String> content = readContent();
		assertEquals(5, content.size());
		assertEquals("TABLE", content.get(0));
		assertEquals("\"H1\",\"H2\",", content.get(1));
		assertEquals("\"A1\",\"A2\",", content.get(2));
		assertEquals("\"B1\",\"B2\",", content.get(3));
		assertEquals("", content.get(4));
	}

	@Test
	void testParseToCsv5WithoutHeaders() throws Exception {
		TableParser.parseToCsv(INPUT_FILE_PATTERN.formatted(5), FILE, false);

		List<String> content = readContent();
		assertEquals(5, content.size());
		assertEquals("TABLE", content.get(0));
		assertEquals("", content.get(1));
		assertEquals("\"A1\",\"A2\",", content.get(2));
		assertEquals("\"B1\",\"B2\",", content.get(3));
		assertEquals("", content.get(4));
	}

	private void runTest(String inputFile) throws Exception {
		TableParser.parseToCsv(inputFile, FILE, true);

		List<String> content = readContent();
		assertEquals(5, content.size());
		assertEquals("TABLE", content.get(0));
		assertEquals("\"Person 1\",\"Person 2\",\"Person 3\",", content.get(1));
		assertEquals("\"Emil\",\"Tobias\",\"Linus\",", content.get(2));
		assertEquals("\"16\",\"14\",\"10\",", content.get(3));
		assertEquals("", content.get(4));
	}
}
