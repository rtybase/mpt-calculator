package org.rty.portfolio.net;

import java.util.List;

import org.apache.hc.core5.http.Header;
import org.rty.portfolio.core.utils.RetryPolicy;

public abstract class HttpClient {
	protected static final int HTTP_OK = 200;

	protected static final String USER_AGENT_HEADER = "User-Agent";
	protected static final String ACCEPT_HEADER = "Accept";

	protected static final String USER_AGENT_VALUE = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36";
	protected static final String ACCEPT_HEADER_VALUE = "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7";

	public final void get(String url, String outFile) throws Throwable {
		RetryPolicy.execute(() -> {
			get(url, outFile, (Header[]) null);
			return null;
		});
	}

	public final void get(String url, String outFile, List<Header> headers) throws Throwable {
		RetryPolicy.execute(() -> {
			if (headers != null) {
				get(url, outFile, headers.toArray(new Header[0]));
			} else {
				get(url, outFile, (Header[]) null);
			}
			return null;
		});
	}

	protected abstract void get(String url, String outFile, Header[] headers) throws Throwable;
}
