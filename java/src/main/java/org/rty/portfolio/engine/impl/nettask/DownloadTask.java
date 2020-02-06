package org.rty.portfolio.engine.impl.nettask;

import java.util.Map;

import org.rty.portfolio.engine.AbstractTask;
import org.rty.portfolio.net.RtyHttpClient;

public class DownloadTask extends AbstractTask {
	private final RtyHttpClient httpClient = new RtyHttpClient();

	@Override
	public void execute(Map<String, String> parameters) throws Exception {
		String url = getValidParameterValue(parameters, URL_PARAM);
		String outFile = getValidParameterValue(parameters, OUTPUT_FILE_PARAM);

		say(String.format("Downloading '%s'", url));
		httpClient.get(url, outFile);
		say(DONE);
	}
}
