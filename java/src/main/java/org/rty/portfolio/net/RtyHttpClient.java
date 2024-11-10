package org.rty.portfolio.net;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class RtyHttpClient {
	private static final int HTTP_OK = 200;

	private static final String USER_AGENT_HEADER = "User-Agent";
	private static final String ACCEPT_HEADER = "Accept";

	private static final String USER_AGENT_VALUE = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36";
	private static final String ACCEPT_HEADER_VALUE = "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7";

	public HttpResponse get(String url, String outFile) throws Exception {
		return get(url, outFile, (Header[]) null);
	}

	public HttpResponse get(String url, String outFile, List<Header> headers) throws Exception {
		if (headers != null) {
			return get(url, outFile, headers.toArray(new Header[0]));
		} else {
			return get(url, outFile);
		}
	}

	public HttpResponse get(String url, String outFile, Header[] headers) throws Exception {
		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);

		boolean acceptSet = false;
		boolean userAgentSet = false;

		if (headers != null) {
			for (Header hdr : headers) {
				request.addHeader(hdr.getName(), hdr.getValue());

				if (ACCEPT_HEADER.equalsIgnoreCase(hdr.getName())) {
					acceptSet = true;
				}

				if (USER_AGENT_HEADER.equalsIgnoreCase(hdr.getName())) {
					userAgentSet = true;
				}
			}
		}

		if (!acceptSet) {
			request.addHeader(ACCEPT_HEADER, ACCEPT_HEADER_VALUE);
		}

		if (!userAgentSet) {
			request.addHeader(USER_AGENT_HEADER, USER_AGENT_VALUE);
		}

		HttpResponse response = client.execute(request);

		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode == HTTP_OK) {
			saveToFile(outFile, response);
		} else {
			throw new Exception("HTTP Staus " + statusCode);
		}
		return response;
	}

	private static void saveToFile(String outFile, HttpResponse response) throws Exception {
		BufferedReader rd = null;
		FileWriter fw = null;

		try {
			rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

			String line = "";
			fw = new FileWriter(outFile);
			while ((line = rd.readLine()) != null) {
				fw.write(line);
				fw.write("\n");
			}
			fw.flush();
		} finally {
			close(fw);
			close(rd);
		}
	}

	private static void close(Closeable handler) {
		if (handler != null) {
			try {
				handler.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
