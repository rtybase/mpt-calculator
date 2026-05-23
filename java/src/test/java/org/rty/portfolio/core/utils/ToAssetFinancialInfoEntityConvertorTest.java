package org.rty.portfolio.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rty.portfolio.core.AssetFinancialInfo;

public class ToAssetFinancialInfoEntityConvertorTest extends CommonTestRoutines {
	private static final String[] HEADER_LINE = new String[] { "Symbol", "Quarterly Ending:", "Total Current Assets",
			"Total Current Liabilities", "Total Liabilities", "Total Equity", "Total Assets", "Net Cash Flow-Operating",
			"Capital Expenditures" };

	private static final String[] TEST_LINE = new String[] { TEST_ASSET, "7/17/2025", "$1,000", "$2,000", "$3,000",
			"-$4,000", "$5,000", "$6,000", "--" };
	private static final String[] TEST_LINE_DATE_CORECTION = new String[] { TEST_ASSET, "7/29/2025", "$1,000", "$2,000",
			"$3,000", "-$4,000", "$5,000", "$6,000", "--" };
	private static final String[] TEST_LINE_NO_DATE_CORECTION = new String[] { TEST_ASSET, "7/31/2025", "$1,000",
			"$2,000", "$3,000", "-$4,000", "$5,000", "$6,000", "--" };
	private static final String[] TEST_LINE_WITH_POSITIVE_CAPEX = new String[] { TEST_ASSET, "7/17/2025", "$1,000", "$2,000",
			"$3,000", "-$4,000", "$5,000", "$6,000", "1.2E+2" };
	private static final String[] TEST_LINE_WITH_NEGATIVE_CAPEX = new String[] { TEST_ASSET, "7/17/2025", "$1,000",
			"$2,000", "$3,000", "-$4,000", "$5,000", "$6,000", "-120" };
	private static final String[] TEST_LINE_WITH_DIV_1000 = new String[] { TEST_ASSET, "7/17/2025", "$1,000,000",
			"$2,000,000", "$3,000,000", "-$4,000,000", "$5,000,000", "$6,000,000", "-120000" };

	private ToAssetFinancialInfoEntityConvertor convertor;

	@BeforeEach
	void setup() {
		convertor = new ToAssetFinancialInfoEntityConvertor(false);
	}

	@Test
	void testToEntity() {
		convertor.updateHeadersFrom("test_file.csv", HEADER_LINE);

		assertEquals(TEST_ASSET, convertor.assetNameFrom(TEST_LINE));

		final AssetFinancialInfo entity = convertor.toEntity(TEST_ASSET, TEST_LINE);
		verifyEntityContent(entity, D_2025_07_17);
		assertNull(entity.shareIssued);
	}

	@Test
	void testToEntityWithDateCorrection() {
		convertor.updateHeadersFrom("test_file.csv", HEADER_LINE);

		assertEquals(TEST_ASSET, convertor.assetNameFrom(TEST_LINE_DATE_CORECTION));

		final AssetFinancialInfo entity = convertor.toEntity(TEST_ASSET, TEST_LINE_DATE_CORECTION);
		verifyEntityContent(entity, dateFrom(31));
		assertNull(entity.shareIssued);
	}

	@Test
	void testToEntityWithoutDateCorrection() {
		convertor.updateHeadersFrom("test_file.csv", HEADER_LINE);

		assertEquals(TEST_ASSET, convertor.assetNameFrom(TEST_LINE_NO_DATE_CORECTION));

		final AssetFinancialInfo entity = convertor.toEntity(TEST_ASSET, TEST_LINE_NO_DATE_CORECTION);
		verifyEntityContent(entity, dateFrom(31));
		assertNull(entity.shareIssued);
	}

	@Test
	void testToEntityWithShareIssuedColumn() {
		final String[] headerIssuedColumn = arrayWithOneMoreElement(HEADER_LINE, "Share Issued");
		convertor.updateHeadersFrom("test_file.csv", headerIssuedColumn);

		final String[] lineWithIssuedColumn = arrayWithOneMoreElement(TEST_LINE, "$8,000");
		assertEquals(TEST_ASSET, convertor.assetNameFrom(lineWithIssuedColumn));

		final AssetFinancialInfo entity = convertor.toEntity(TEST_ASSET, lineWithIssuedColumn);
		verifyEntityContent(entity, D_2025_07_17);
		assertEquals(8000D, entity.shareIssued, ERROR_TOLERANCE);
	}

	@Test
	void testToEntityWithPositiveCapEx() {
		convertor.updateHeadersFrom("test_file.csv", HEADER_LINE);

		assertEquals(TEST_ASSET, convertor.assetNameFrom(TEST_LINE_WITH_POSITIVE_CAPEX));

		final AssetFinancialInfo entity = convertor.toEntity(TEST_ASSET, TEST_LINE_WITH_POSITIVE_CAPEX);
		verifyEntityContent(entity, D_2025_07_17, -120D);
		assertNull(entity.shareIssued);
	}

	@Test
	void testToEntityWithNegativeCapEx() {
		convertor.updateHeadersFrom("test_file.csv", HEADER_LINE);

		assertEquals(TEST_ASSET, convertor.assetNameFrom(TEST_LINE_WITH_NEGATIVE_CAPEX));

		final AssetFinancialInfo entity = convertor.toEntity(TEST_ASSET, TEST_LINE_WITH_NEGATIVE_CAPEX);
		verifyEntityContent(entity, D_2025_07_17, -120D);
		assertNull(entity.shareIssued);
	}

	@Test
	void testToEntityWithDivisibilityBy1000() {
		convertor = new ToAssetFinancialInfoEntityConvertor(true);

		final String[] headerIssuedColumn = arrayWithOneMoreElement(HEADER_LINE, "Share Issued");
		convertor.updateHeadersFrom("test_file.csv", headerIssuedColumn);

		final String[] lineWithIssuedColumn = arrayWithOneMoreElement(TEST_LINE_WITH_DIV_1000, "$8,000,000");
		assertEquals(TEST_ASSET, convertor.assetNameFrom(lineWithIssuedColumn));

		final AssetFinancialInfo entity = convertor.toEntity(TEST_ASSET, lineWithIssuedColumn);
		verifyEntityContent(entity, D_2025_07_17, -120D);
		assertEquals(8000000D, entity.shareIssued, ERROR_TOLERANCE);
	}

	@Test
	void testMandatoryColumnNotPresent() {
		final String[] headerWithoutCapExp = new String[] { "Symbol", "Quarterly Ending:", "Total Current Assets",
				"Total Current Liabilities", "Total Liabilities", "Total Equity", "Total Assets",
				"Net Cash Flow-Operating" };

		assertThrows(IllegalArgumentException.class,
				() -> convertor.updateHeadersFrom("test_file.csv", headerWithoutCapExp));
	}

	@Test
	void testWithEmptyDate() {
		convertor.updateHeadersFrom("test_file.csv", HEADER_LINE);

		final String[] lineWithEmptyDate = new String[] { TEST_ASSET, "", "$1,000", "$2,000", "$3,000", "-$4,000",
				"$5,000", "$6,000", "--" };

		assertThrows(IllegalArgumentException.class,
				() -> convertor.toEntity(TEST_ASSET, lineWithEmptyDate));
	}

	private void verifyEntityContent(AssetFinancialInfo entity, Date expectedDate) {
		verifyEntityContent(entity, expectedDate, null);
	}

	private void verifyEntityContent(AssetFinancialInfo entity, Date expectedDate, Double expectedCapex) {
		assertEquals(TEST_ASSET, entity.assetName);
		assertEquals(expectedDate, entity.date);
		assertEquals(1000D, entity.totalCurrentAssets, ERROR_TOLERANCE);
		assertEquals(2000D, entity.totalCurrentLiabilities, ERROR_TOLERANCE);
		assertEquals(3000D, entity.totalLiabilities, ERROR_TOLERANCE);
		assertEquals(-4000D, entity.totalEquity, ERROR_TOLERANCE);
		assertEquals(5000D, entity.totalAssets, ERROR_TOLERANCE);
		assertEquals(6000D, entity.netCashFlowOperating, ERROR_TOLERANCE);

		if (expectedCapex == null) {
			assertNull(entity.capitalExpenditures);
		} else {
			assertEquals(expectedCapex, entity.capitalExpenditures, ERROR_TOLERANCE);
		}
	}
}
