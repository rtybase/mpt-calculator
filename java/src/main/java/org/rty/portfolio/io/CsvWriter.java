package org.rty.portfolio.io;

import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;

import org.rty.portfolio.core.AssetPriceInfo;

import au.com.bytecode.opencsv.CSVWriter;

public class CsvWriter implements Closeable {
	public static final SimpleDateFormat SCAN_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	private final CSVWriter writer;

	public CsvWriter(String fileName) throws IOException {
		Objects.requireNonNull(fileName, "fileName must not be null!");

		writer = new CSVWriter(new FileWriter(fileName),
				CSVWriter.DEFAULT_SEPARATOR,
				CSVWriter.DEFAULT_QUOTE_CHARACTER);
	}

	public void write(List<AssetPriceInfo> priceDetails) {
		Objects.requireNonNull(priceDetails, "priceDetails must not be null!");
		priceDetails.forEach(this::write);
	}

	public void write(AssetPriceInfo priceInfo) {
		writer.writeNext(new String[] {
				priceInfo.getAssetName(),
				"" + priceInfo.getPrice(),
				"" + priceInfo.getChange(),
				"" + priceInfo.getRate(), 
				SCAN_DATE_FORMAT.format(priceInfo.getDate())
		});
	}

	@Override
	public void close() throws IOException {
		writer.flush();
		writer.close();
	}
}
