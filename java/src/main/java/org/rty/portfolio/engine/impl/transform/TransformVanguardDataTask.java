package org.rty.portfolio.engine.impl.transform;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.rty.portfolio.core.AssetPriceInfo;
import org.rty.portfolio.engine.AbstractTask;
import org.rty.portfolio.io.CsvWriter;
import org.rty.portfolio.math.Calculator;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class TransformVanguardDataTask extends AbstractTask {

	private static final String PROVIDER_PREFIX = "Vanguard ";
	private static final SimpleDateFormat SCAN_INPUT_DATE_FORMAT = new SimpleDateFormat("dd MMM yy");
	private static final String JAVASCRIPT_START = "angular.callbacks._5(";
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
		if (content.startsWith(JAVASCRIPT_START) && content.length() > 2) {
			content = content.substring(JAVASCRIPT_START.length(), content.length() - 2);

			JsonNode node = MAPPER.readTree(content);
			Iterator<Entry<String, JsonNode>> fields = node.fields();

			CsvWriter writer = new CsvWriter(outputFile);
			while (fields.hasNext()) {
				Map.Entry<String, JsonNode> entry = fields.next();
				JsonNode dataNode = entry.getValue();
				if (isValidMarketPriceEntry(dataNode)) {
					++total;
					try {
						writer.write(buildPriceInfo(dataNode));
					} catch (Exception ex) {
						say(String.format("Failed to extract price info from '%s'.", dataNode.toString()));
						ex.printStackTrace();
					}
				}
			}

			writer.close();
		}
		say("Total processed " + total);
		say(DONE);
	}

	private static boolean isValidMarketPriceEntry(JsonNode dataNode) {
		return dataNode.has("name") && dataNode.has("navOrMktPrice")
				&& dataNode.has("navOrMktPriceAsOfDate")
				&& dataNode.has("navOrMktPercentChange");
	}

	private static AssetPriceInfo buildPriceInfo(JsonNode dataNode) {
		double rate = Double.parseDouble(dataNode.findValue("navOrMktPercentChange").asText().replace("%", ""));
		double currentPrice = Double.parseDouble(dataNode.findValue("navOrMktPrice").asText());
		return new AssetPriceInfo(
				PROVIDER_PREFIX + dataNode.findValue("name").asText().replace("&#174;", "").replace("&#38;", "&"),
				currentPrice,
				Calculator.calculateChangeFromRate(currentPrice, rate),
				rate,
				SCAN_INPUT_DATE_FORMAT
						.parse(dataNode.findValue("navOrMktPriceAsOfDate").asText(), new ParsePosition(0)));
	}
}
