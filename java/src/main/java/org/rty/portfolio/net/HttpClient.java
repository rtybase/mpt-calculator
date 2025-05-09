package org.rty.portfolio.net;

import java.util.List;

import org.apache.hc.core5.http.Header;

public abstract class HttpClient {
	protected static final int HTTP_OK = 200;

	protected static final String USER_AGENT_HEADER = "User-Agent";
	protected static final String ACCEPT_HEADER = "Accept";

	protected static final String USER_AGENT_VALUE = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36";
	protected static final String ACCEPT_HEADER_VALUE = "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7";

	public void get(String url, String outFile) throws Exception {
		get(url, outFile, (Header[]) null);
	}

	public void get(String url, String outFile, List<Header> headers) throws Exception {
		if (headers != null) {
			get(url, outFile, headers.toArray(new Header[0]));
		} else {
			get(url, outFile);
		}
	}

	public abstract void get(String url, String outFile, Header[] headers) throws Exception;
}
