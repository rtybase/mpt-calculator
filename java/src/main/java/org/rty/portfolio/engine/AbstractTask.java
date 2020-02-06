package org.rty.portfolio.engine;

import java.util.Map;
import java.util.Objects;

import com.google.common.base.Strings;

public abstract class AbstractTask implements Task {
	public static final String INPUT_FILE_PARAM = "-file";
	public static final String URL_PARAM = "-url";
	public static final String OUTPUT_FILE_PARAM = "-outfile";

	public static final String INPUT_SYMBOL = "-in_symbol";
	public static final String OUT_SYMBOL = "-out_symbol";

	public static final String ERROR_REPORT_FILE = "assets.err";
	public static final String DONE = "DONE";

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	protected void say(String text) {
		System.out.println(getName() + ": " + text);
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
}
