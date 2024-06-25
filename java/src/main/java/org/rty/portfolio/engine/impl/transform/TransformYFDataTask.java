package org.rty.portfolio.engine.impl.transform;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URLEncoder;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;
import org.rty.portfolio.core.AssetPriceInfoAccumulator;
import org.rty.portfolio.core.utils.TimeUtils;
import org.rty.portfolio.engine.AbstractTask;
import org.rty.portfolio.io.CsvWriter;
import org.rty.portfolio.net.RtyHttpClient;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Yahoo Finance history prices data loader.
 * 
 */
public class TransformYFDataTask extends AbstractTask {
	private static final String TEMP_HTML = "temp.html";
	private static final String CLOSE_PRICE_COLUMN_NAME = "Close";
	private static final String URL_PRICE_TEMPLATE = "https://query1.finance.yahoo.com/v7/finance/download/%s?period1=%d&period2=%d&interval=1d&events=history&crumb=%s";
	private static final String URL_ASSET_TEMPLATE = "https://finance.yahoo.com/quote/%s/history/";
	private static final SimpleDateFormat SCAN_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public void execute(Map<String, String> parameters) throws Exception {
		String inSymbol = getValidParameterValue(parameters, INPUT_SYMBOL);
		String outSymbol = getValidParameterValue(parameters, OUT_SYMBOL);
		String outputFile = getValidParameterValue(parameters, OUTPUT_FILE_PARAM);

		RtyHttpClient client = new RtyHttpClient();
		String[] cookieAndCrumb = getCookieAndCrumb(client, inSymbol);

		String localDataFile = downloadDataFile(inSymbol, outSymbol, client, cookieAndCrumb);
		AssetPriceInfoAccumulator accumulator = populateRates(outSymbol, localDataFile);
		CsvWriter writer = new CsvWriter(outputFile);
		writer.write(accumulator.getChangeHistory());
		writer.close();
		say(DONE);
	}

	private String downloadDataFile(String inSymbol, String outSymbol, RtyHttpClient client, String[] cookieAndCrumb)
			throws Exception {
		String url = computeUrl(inSymbol, cookieAndCrumb[1]);
		say("Load data from " + url);
		String localDataFile = outSymbol + ".csv";
		client.get(url, localDataFile, new Header[] { new BasicHeader("Cookie", cookieAndCrumb[0]) });
		return localDataFile;
	}

	private AssetPriceInfoAccumulator populateRates(String assetName, String file) throws Exception {
		AssetPriceInfoAccumulator accumulator = new AssetPriceInfoAccumulator(assetName);

		CSVReader reader = new CSVReader(new FileReader(file));
		List<String[]> lines = reader.readAll();
		if (lines.size() > 0) {
			int closePriceColumn = getClosePriceColumnIndex(lines.get(0));
			int i = 0;
			for (String[] line : lines) {
				if (i > 0) {
					try {
						Date date = SCAN_DATE_FORMAT.parse(line[0], new ParsePosition(0));
						double price = Double.parseDouble(line[closePriceColumn]);
						accumulator.add(date, price);
					} catch (Exception ex) {
						say("parse error at line " + i);
					}
				}
				i++;
			}
		}
		reader.close();
		return accumulator;
	}

	private static int getClosePriceColumnIndex(String[] header) throws Exception {
		int i = 0;
		for (String column : header) {
			if (CLOSE_PRICE_COLUMN_NAME.equalsIgnoreCase(column)) {
				return i;
			}
			i++;
		}
		throw new Exception(String.format("Column '%s' not found.", CLOSE_PRICE_COLUMN_NAME));
	}

	private static String computeUrl(String inSymbol, String crumb) throws Exception {
		Calendar cal = Calendar.getInstance();
		long endDate = cal.getTimeInMillis() / 1000;

		cal.add(Calendar.YEAR, TimeUtils.yearsBack());
		long startDate = cal.getTimeInMillis() / 1000;

		return String.format(URL_PRICE_TEMPLATE, inSymbol, startDate, endDate, URLEncoder.encode(crumb, "UTF-8"));
	}

	private String[] getCookieAndCrumb(RtyHttpClient client, String symbol) throws Exception {
		String url = String.format(URL_ASSET_TEMPLATE, symbol);
		say("Collecting details from: " + url);
		HttpResponse response = client.get(url, TEMP_HTML);
		say("Status code: " + response.getStatusLine().getStatusCode());
		String cookie = "";
		String crumb = "";
		Header[] hdrs = response.getHeaders("Set-Cookie");
		for (Header hdr : hdrs) {
			String value = hdr.getValue();
			if (value != null && value.toLowerCase().startsWith("b=")) {
				cookie = value.substring(0, value.indexOf(";") + 1);
			}
		}

		String content = readTempFileContent();
		int startIndex = content.indexOf("\"CrumbStore\":{\"crumb\":\"");
		if (startIndex >= 0) {
			content = content.substring(startIndex + 23);
			int endIndex = content.indexOf("\"},");
			if (endIndex >= 0) {
				crumb = content.substring(0, endIndex);
			}
		}
		Thread.sleep(50);
		deleteTempFile();
		String unescapedCrumb = StringEscapeUtils.unescapeJava(crumb);
		say("With cookie: " + cookie);
		say("With crumb: " + crumb + " (unescaped " + unescapedCrumb + ")");
		return new String[] { cookie, unescapedCrumb };
	}

	private static String readTempFileContent() throws Exception {
		BufferedReader bufferedReader = new BufferedReader(new FileReader(TEMP_HTML));

		StringBuilder stringBuffer = new StringBuilder();
		String line = null;
		while ((line = bufferedReader.readLine()) != null) {
			stringBuffer.append(line).append("\n");
		}
		bufferedReader.close();
		return stringBuffer.toString();
	}

	private static void deleteTempFile() {
		File f = new File(TEMP_HTML);
		f.delete();
	}
}
