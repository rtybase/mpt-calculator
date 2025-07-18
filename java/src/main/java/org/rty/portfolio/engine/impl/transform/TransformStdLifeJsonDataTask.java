package org.rty.portfolio.engine.impl.transform;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Map;

import org.rty.portfolio.core.AssetPriceInfo;
import org.rty.portfolio.engine.AbstractTask;
import org.rty.portfolio.io.CsvWriter;
import org.rty.portfolio.math.Calculator;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class TransformStdLifeJsonDataTask extends AbstractTask {
	private static final String NO_DATA = "-";
	private static final String MAIN_FIELD = "aaData";
	private static final SimpleDateFormat SCAN_INPUT_DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

	private static final int ASSET_NAME = 0;
	private static final int ASSET_PRICE = 2;
	private static final int PRICE_CHANGE = 4;
	private static final int PRICE_DATE = 6;

	private static final ObjectMapper MAPPER = new ObjectMapper();

	static {
		MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
		MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

	@Override
	public void execute(Map<String, String> parameters) throws Exception {
		String inputFile = getValidParameterValue(parameters, INPUT_FILE_PARAM);
		String outputFile = getValidParameterValue(parameters, OUTPUT_FILE_PARAM);

		say("Convert data... ");
		int total = 0;

		String content = Files.readString(Paths.get(inputFile));

		JsonNode node = MAPPER.readTree(content);

		if (node.has(MAIN_FIELD) && node.get(MAIN_FIELD).isArray()) {
			Iterator<JsonNode> lines = node.get(MAIN_FIELD).elements();

			CsvWriter writer = new CsvWriter(outputFile);
			while (lines.hasNext()) {
				JsonNode values = lines.next();

				if (isValidMarketPriceEntry(values)) {
					++total;
					try {
						writer.write(buildPriceInfo(values));
					} catch (Exception ex) {
						say("Failed to extract price info from '{}'.", values.toString());
						ex.printStackTrace();
					}
				}
			}

			writer.close();
		} else {
			say("Bad JSON format!");
		}

		say("Total processed {}", total);
		say(DONE);
	}

	private static boolean isValidMarketPriceEntry(JsonNode dataNode) {
		boolean hasColumns = dataNode.has(ASSET_NAME)
				&& dataNode.has(ASSET_PRICE)
				&& dataNode.has(PRICE_CHANGE)
				&& dataNode.has(PRICE_DATE) ;
		return hasColumns
				&& !NO_DATA.equals(dataNode.get(ASSET_NAME).asText())
				&& !NO_DATA.equals(dataNode.get(ASSET_PRICE).asText())
				&& !NO_DATA.equals(dataNode.get(PRICE_CHANGE).asText())
				&& !NO_DATA.equals(dataNode.get(PRICE_DATE).asText());
				
	}

	private static AssetPriceInfo buildPriceInfo(JsonNode dataNode) {
		double change = Double.parseDouble(dataNode.get(PRICE_CHANGE).asText()
				.replace("@3@", "")
				.replace("@4@", "")
				.replace("@5@", "")
				.replace("</span>", "")
				.replace("<span class='dls_fundUp'>", "")
				.replace("<span class='dls_fundDown'>", ""));
		double currentPrice = Double.parseDouble(dataNode.get(ASSET_PRICE).asText());
		double previousProce = currentPrice - change;

		return new AssetPriceInfo(
				dataNode.get(ASSET_NAME).asText()
					.replace("@17@", "")
					.replace("<span class='microText'>CLOSED TO NEW BUSINESS</span>", ""),
				currentPrice,
				change,
				Calculator.calculateRate(currentPrice, previousProce),
				SCAN_INPUT_DATE_FORMAT.parse(dataNode.get(PRICE_DATE).asText(), new ParsePosition(0)));
	}
}
