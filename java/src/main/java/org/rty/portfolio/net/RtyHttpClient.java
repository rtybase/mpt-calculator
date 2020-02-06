package org.rty.portfolio.net;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileWriter;
import java.io.InputStreamReader;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class RtyHttpClient {
	private static final int HTTP_OK = 200;
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36";

	public HttpResponse get(String url, String outFile) throws Exception {
		return get(url, outFile, null);
	}

	public HttpResponse get(String url, String outFile, Header[] headers) throws Exception {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		request.addHeader("User-Agent", USER_AGENT);
		if (headers != null) {
			for (Header hdr : headers) {
				request.addHeader(hdr.getName(), hdr.getValue());
			}
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
