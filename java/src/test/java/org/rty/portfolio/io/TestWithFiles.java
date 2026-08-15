package org.rty.portfolio.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class TestWithFiles {
	protected static final String DIR = "src/test/resources/csv-test/";
	protected static final String FILE = DIR + "out.csv";

	void setup() throws Exception {
		Files.createDirectories(Paths.get(DIR));
	}

	void cleanup() throws Exception {
		Files.deleteIfExists(Paths.get(FILE));
	}

	protected static List<String> readContent() throws Exception {
		return Files.readAllLines(Paths.get(FILE));
	}

	protected static void verifyFileContent(String expectedContent) throws Exception {
		List<String> content = readContent();
		assertEquals(1, content.size());
		assertEquals(expectedContent, content.get(0));
	}
}
