package org.rty.portfolio.engine.impl.transform;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.rty.portfolio.core.AssetPriceInfo;
import org.rty.portfolio.core.AssetPriceInfoAccumulator;
import org.rty.portfolio.engine.AbstractTask;
import org.rty.portfolio.io.CsvWriter;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * European Central Bank data loader.
 *
 */
public class TransformEcbRatesTask extends AbstractTask {
	private static final SimpleDateFormat SCAN_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private static final String BASE_CURRENCY = "EUR";
	private static final List<String> CURRENCIES = Arrays.asList("USD", "GBP");

	private final HashMap<String, AssetPriceInfoAccumulator> rates = new HashMap<>();

	public TransformEcbRatesTask() {
		CURRENCIES.forEach(c -> rates.put(c, new AssetPriceInfoAccumulator(String.format("%s/%s", BASE_CURRENCY, c))));
	}

	@Override
	public void execute(Map<String, String> parameters) throws Exception {
		String inputFile = getValidParameterValue(parameters, INPUT_FILE_PARAM);
		String outputFile = getValidParameterValue(parameters, OUTPUT_FILE_PARAM);

		say("Load data... ");
		populateRates(inputFile);

		CsvWriter<AssetPriceInfo> writer = new CsvWriter<>(outputFile);
		CURRENCIES.forEach(c -> writer.write(rates.get(c).getChangeHistory()));
		writer.close();

		say(DONE);
	}

	private void populateRates(String file) throws Exception {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();

		saxParser.parse(file, new DefaultHandler() {
			private Date date;

			public void startElement(String uri, String localName, String qName, Attributes attributes)
					throws SAXException {

				if (qName.equalsIgnoreCase("CUBE") && attributes.getLength() > 0) {
					String dateValue = attributes.getValue("time");
					String currency = attributes.getValue("currency");
					String rate = attributes.getValue("rate");

					if (dateValue != null) {
						date = SCAN_DATE_FORMAT.parse(dateValue, new ParsePosition(0));
					}

					if (currency != null && rate != null && date != null) {
						AssetPriceInfoAccumulator accumulator = rates.get(currency);
						if (accumulator != null) {
							try {
								double doubleRate = Double.parseDouble(rate);
								accumulator.add(date, doubleRate);
							} catch (Exception ex) {
								say("parse error for '{}', rate='{}'", currency, rate);
							}
						}
					}
				}
			}
		});
	}
}
