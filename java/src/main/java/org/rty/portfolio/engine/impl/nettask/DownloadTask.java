package org.rty.portfolio.engine.impl.nettask;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.rty.portfolio.engine.AbstractTask;
import org.rty.portfolio.net.RtyHttpClient;

public class DownloadTask extends AbstractTask {
	private final RtyHttpClient httpClient = new RtyHttpClient();

	@Override
	public void execute(Map<String, String> parameters) throws Exception {
		final String url = getValidParameterValue(parameters, URL_PARAM);
		final String outFile = getValidParameterValue(parameters, OUTPUT_FILE_PARAM);
		final String httpHeadersFile = headersFileValues(parameters);

		say(String.format("Downloading '%s' to '%s'", url, outFile));
		httpClient.get(url, outFile, extraHeadersFrom(httpHeadersFile));
		say(DONE);
	}

	private static String headersFileValues(Map<String, String> parameters) {
		if (parameters != null) {
			return parameters.get(HTTP_HEADERS_FILE_PARAM);
		}
		return null;
	}

	private static List<Header> extraHeadersFrom(String httpHeadersFile) throws Exception {
		if (httpHeadersFile == null) {
			return null;
		}

		final Properties headerValues = new Properties();
		headerValues.load(new FileInputStream(httpHeadersFile));

		List<Header> headers = new ArrayList<>(headerValues.size());

		headerValues.forEach((key, value) -> {
			headers.add(new BasicHeader(key.toString(), value.toString()));
		});

		return headers;
	}
}
