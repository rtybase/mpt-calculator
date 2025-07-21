package org.rty.portfolio.net;

import java.io.FileOutputStream;
import java.util.concurrent.Future;

import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.async.methods.SimpleRequestProducer;
import org.apache.hc.client5.http.async.methods.SimpleResponseConsumer;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.client5.http.entity.GzipDecompressingEntity;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RtyHttpClientWithHTTP2Support extends HttpClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(RtyHttpClientWithHTTP2Support.class.getSimpleName());

	@Override
	protected void get(String url, String outFile, Header[] headers) throws Exception {
		try (CloseableHttpAsyncClient client = HttpAsyncClients.customHttp2()
			      .setTlsStrategy(ClientTlsStrategyBuilder.create()
			              .setSslContext(SSLContexts.createSystemDefault())
			              .setTlsVersions(TLS.V_1_3)
			              .build())
			      .setIOReactorConfig(IOReactorConfig.custom()
			              .setSoTimeout(Timeout.ofMinutes(1))
			              .build())
			      .setDefaultConnectionConfig(ConnectionConfig.custom()
			              .setSocketTimeout(Timeout.ofMinutes(1))
			              .setConnectTimeout(Timeout.ofMinutes(1))
			              .setTimeToLive(TimeValue.ofMinutes(10))
			              .build())
			      .setDefaultRequestConfig(RequestConfig.custom()
			              .setCookieSpec(StandardCookieSpec.STRICT)
			              .build())
			      .build();) {
			client.start();

			SimpleRequestBuilder simpleRequestBuilder = SimpleRequestBuilder.get(url);
			setHeaders(headers, simpleRequestBuilder);

			final SimpleHttpRequest request = simpleRequestBuilder.build();

			final Future<SimpleHttpResponse> future = client.execute(SimpleRequestProducer.create(request),
					SimpleResponseConsumer.create(), new FutureCallback<SimpleHttpResponse>() {

						@Override
						public void completed(final SimpleHttpResponse response) {
							LOGGER.info("{} -> {}", request.getRequestUri(), response.getCode());
						}

						@Override
						public void failed(final Exception ex) {
							LOGGER.error("{}", request.getRequestUri(), ex);
						}

						@Override
						public void cancelled() {
							LOGGER.warn("{} cancelled", request.getRequestUri());
						}
					});

			SimpleHttpResponse response = future.get();
			
			final int statusCode = response.getCode();
			if (statusCode == HTTP_OK) {
				saveToFile(outFile, response);
			} else {
				throw new Exception("HTTP Staus " + statusCode);
			}

			client.close(CloseMode.GRACEFUL);
		}
	}

	private static void setHeaders(Header[] headers, SimpleRequestBuilder simpleRequestBuilder) {
		simpleRequestBuilder.setHeader(ACCEPT_HEADER, ACCEPT_HEADER_VALUE);
		simpleRequestBuilder.setHeader(USER_AGENT_HEADER, USER_AGENT_VALUE);

		if (headers != null) {
			for (Header header : headers) {
				simpleRequestBuilder.setHeader(header);
			}
		}
	}

	private static void saveToFile(String outFile, SimpleHttpResponse response) throws Exception {
		FileOutputStream outputStream = new FileOutputStream(outFile);
		outputStream.write(bytesFrom(response));
		outputStream.close();
	}

	private static byte[] bytesFrom(SimpleHttpResponse response) throws Exception {
		Header contentEncoding = response.getHeader("Content-Encoding");

		if (contentEncoding != null && "gzip".equalsIgnoreCase(contentEncoding.getValue())) {
			HttpEntity entity = new GzipDecompressingEntity(
					new ByteArrayEntity(response.getBodyBytes(), ContentType.APPLICATION_XML));
			return EntityUtils.toByteArray(entity);
		}

		return response.getBodyBytes();
	}
}
