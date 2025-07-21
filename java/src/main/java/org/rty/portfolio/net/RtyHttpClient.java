package org.rty.portfolio.net;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.Header;

public class RtyHttpClient extends HttpClient {

	@Override
	protected void get(String url, String outFile, Header[] headers) throws Exception {
		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);

		setHeaders(headers, request);

		CloseableHttpResponse response = client.execute(request);

		final int statusCode = response.getCode();
		if (statusCode == HTTP_OK) {
			saveToFile(outFile, response);
		} else {
			throw new Exception("HTTP Staus " + statusCode);
		}
	}

	private static void setHeaders(Header[] headers, HttpGet request) {
		request.setHeader(ACCEPT_HEADER, ACCEPT_HEADER_VALUE);
		request.setHeader(USER_AGENT_HEADER, USER_AGENT_VALUE);

		if (headers != null) {
			for (Header header : headers) {
				request.setHeader(header);
			}
		}
	}

	private static void saveToFile(String outFile, CloseableHttpResponse response) throws Exception {
		try (BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
			String line = "";

			try (FileWriter fw = new FileWriter(outFile)) {
				while ((line = rd.readLine()) != null) {
					fw.write(line);
					fw.write("\n");
				}
				fw.flush();
			}
		}
	}
}
