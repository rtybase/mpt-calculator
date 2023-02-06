package org.rty.portfolio.engine.impl.transform;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.rty.portfolio.core.AssetPriceInfoAccumulator;
import org.rty.portfolio.core.utils.TimeUtils;
import org.rty.portfolio.engine.AbstractTask;
import org.rty.portfolio.io.CsvWriter;
import org.rty.portfolio.net.RtyHttpClient;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Bank of England data loader.
 *
 */
public class TransformBoEDataTask extends AbstractTask {
	private static final String URL_TEMPLATE = "http://www.bankofengland.co.uk/boeapps/iadb/fromshowcolumns.asp?Travel=NIxIRxSUx&FromSeries=1&ToSeries=50&DAT=RNG&FD=%s&FM=%s&FY=%s&TD=%s&TM=%s&TY=%s&VFD=Y&xml.x=21&xml.y=19&CSVF=TT&C=%s&Filter=N";
	private static final SimpleDateFormat SCAN_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public void execute(Map<String, String> parameters) throws Exception {
		String inSymbol = getValidParameterValue(parameters, INPUT_SYMBOL);
		String outputFile = getValidParameterValue(parameters, OUTPUT_FILE_PARAM);
		String outSymbol = getValidParameterValue(parameters, OUT_SYMBOL);

		String outFileName = downloadDataFile(inSymbol);
		AssetPriceInfoAccumulator accumulator = populateRates(outSymbol, outFileName);

		CsvWriter writer = new CsvWriter(outputFile);
		writer.write(accumulator.getChangeHistory());
		writer.close();
		say(DONE);
	}

	private String downloadDataFile(String inSymbol) throws Exception {
		String url = computeUrl(inSymbol);
		say("Load data from " + url);
		RtyHttpClient client = new RtyHttpClient();
		String outFileName = inSymbol + ".xml";
		client.get(url, outFileName);
		return outFileName;
	}

	private AssetPriceInfoAccumulator populateRates(String assetName, String file) throws Exception {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();

		AssetPriceInfoAccumulator accumulator = new AssetPriceInfoAccumulator(assetName);

		saxParser.parse(file, new DefaultHandler() {
			public void startElement(String uri, String localName, String qName, Attributes attributes)
					throws SAXException {

				if (qName.equalsIgnoreCase("CUBE") && attributes.getLength() > 0) {
					String strDate = attributes.getValue("TIME");
					String strPrice = attributes.getValue("OBS_VALUE");

					if (strPrice != null && strDate != null) {
						try {
							Date date = SCAN_DATE_FORMAT.parse(strDate, new ParsePosition(0));
							double price = Double.parseDouble(strPrice);
							accumulator.add(date, price);
						} catch (Exception ex) {
							say("parse error for " + strDate + ", price=" + strPrice);
						}
					}
				}
			}
		});
		return accumulator;
	}

	private static String computeUrl(String inSymbol) {
		Calendar cal = Calendar.getInstance();
		String currentDay = "" + cal.get(Calendar.DAY_OF_MONTH);
		String currentYear = "" + cal.get(Calendar.YEAR);
		String currentMonth = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.UK);

		cal.add(Calendar.YEAR, TimeUtils.yearsBack());
		String startDay = "" + cal.get(Calendar.DAY_OF_MONTH);
		String startYear = "" + cal.get(Calendar.YEAR);
		String startMonth = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.UK);

		return String.format(URL_TEMPLATE, startDay, startMonth, startYear, currentDay, currentMonth, currentYear,
				inSymbol);
	}
}
