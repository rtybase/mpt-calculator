package org.rty.portfolio.io;

import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;

import org.rty.portfolio.core.CsvWritable;

import au.com.bytecode.opencsv.CSVWriter;

public class CsvWriter<T extends CsvWritable> implements Closeable {
	public static final SimpleDateFormat SCAN_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	private final CSVWriter writer;

	public CsvWriter(String fileName) throws IOException {
		Objects.requireNonNull(fileName, "fileName must not be null!");

		writer = new CSVWriter(new FileWriter(fileName),
				CSVWriter.DEFAULT_SEPARATOR,
				CSVWriter.DEFAULT_QUOTE_CHARACTER);
	}

	public void write(List<T> lines) {
		Objects.requireNonNull(lines, "lines must not be null!");
		lines.forEach(this::write);
	}

	public void write(T line) {
		writer.writeNext(line.toCsvLine());
	}

	@Override
	public void close() throws IOException {
		writer.flush();
		writer.close();
	}
}
