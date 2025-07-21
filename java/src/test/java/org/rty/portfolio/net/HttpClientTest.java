package org.rty.portfolio.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rty.portfolio.core.utils.RetryPolicy;

class HttpClientTest {
	private static final String TEST_URL = "test_url";
	private static final String TEST_FILE = "test_file";
	private static final Header TEST_HEADER = new BasicHeader("test-header", "test_value");

	private TestHttpClient client;

	@BeforeEach
	void setup() {
		client = new TestHttpClient();
	}

	@Test
	void testRetryWithoutHeaders() {
		assertThrows(Exception.class, () -> client.get(TEST_URL, TEST_FILE));
		assertEquals(RetryPolicy.MAX_RETRY, client.totalCalls());
	}

	@Test
	void testRetryWithNullHeaders() {
		assertThrows(Exception.class, () -> client.get(TEST_URL, TEST_FILE, (List<Header>) null));
		assertEquals(RetryPolicy.MAX_RETRY, client.totalCalls());
	}

	@Test
	void testRetryWithHeaders() {
		assertThrows(Exception.class, () -> client.get(TEST_URL, TEST_FILE, List.of(TEST_HEADER)));
		assertEquals(RetryPolicy.MAX_RETRY, client.totalCalls());
	}

	private static class TestHttpClient extends HttpClient {
		private AtomicInteger count = new AtomicInteger(0);

		@Override
		protected void get(String url, String outFile, Header[] headers) throws Exception {
			count.incrementAndGet();
			throw new Exception("Test exception!");
		}

		public int totalCalls() {
			return count.get();
		}
	}
}
