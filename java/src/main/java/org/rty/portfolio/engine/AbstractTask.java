package org.rty.portfolio.engine;

import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import au.com.bytecode.opencsv.CSVReader;

public abstract class AbstractTask implements Task {
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	public static final String INPUT_FILE_PARAM = "-file";
	public static final String URL_PARAM = "-url";
	public static final String OUTPUT_FILE_PARAM = "-outfile";
	public static final String HTTP_HEADERS_FILE_PARAM = "-headers";
	public static final String DATE_VALUE_INDEX_PARAM = "-date_value_index";
	public static final String DATE_FORMAT_PARAM = "-date_format";
	public static final String PRICE_VALUE_INDEX_PARAM = "-price_value_index";
	
	public static final String INPUT_SYMBOL = "-in_symbol";
	public static final String OUT_SYMBOL = "-out_symbol";

	public static final String ERROR_REPORT_FILE = "assets.err";
	public static final String DONE = "DONE";

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	protected void say(String text, Object... params) {
		logger.info(text, params);
	}

	protected static String getValidParameterValue(Map<String, String> parameters, String parameterName) {
		Objects.requireNonNull(parameters, "parameters must not be null!");
		Objects.requireNonNull(parameterName, "parameterName must not be null!");

		String paremeterValue = parameters.get(parameterName);

		if (Strings.isNullOrEmpty(paremeterValue)) {
			throw new IllegalArgumentException(String.format("'%s' parameter is empty!", paremeterValue));
		}
		return paremeterValue;
	}

	protected static List<String[]> readAllLinesFrom(String inputFile) throws Exception {
		CSVReader reader = new CSVReader(new FileReader(inputFile));
		List<String[]> lines = reader.readAll();
		reader.close();
		return lines;
	}
}
