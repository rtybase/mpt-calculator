package org.rty.portfolio.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rty.portfolio.core.AssetFinancialInfo;

public class ToAssetFinancialInfoEntityConvertorTest extends CommonTestRoutines {
	private static final String[] HEADER_LINE = new String[] { "Symbol", "Quarterly Ending:", "Total Current Assets",
			"Total Current Liabilities", "Total Liabilities", "Total Equity", "Total Assets", "Net Cash Flow-Operating",
			"Capital Expenditures" };

	private static final String[] TEST_LINE = new String[] { TEST_ASSET, "7/17/2025", "$1,000", "$2,000", "$3,000",
			"-$4,000", "$5,000", "$6,000", "--" };

	private ToAssetFinancialInfoEntityConvertor convertor;

	@BeforeEach
	void setup() {
		convertor = new ToAssetFinancialInfoEntityConvertor();
	}

	@Test
	void testToEntity() {
		convertor.updateHeadersFrom("test_file.csv", HEADER_LINE);

		assertEquals(TEST_ASSET, convertor.assetNameFrom(TEST_LINE));

		final AssetFinancialInfo entity = convertor.toEntity(TEST_ASSET, TEST_LINE);
		verifyEntityContent(entity);
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

		assertThrows(IllegalArgumentException.class, () -> convertor.toEntity(TEST_ASSET, lineWithEmptyDate));
	}

	private void verifyEntityContent(final AssetFinancialInfo entity) {
		assertEquals(TEST_ASSET, entity.assetName);
		assertEquals(D_2025_07_17, entity.date);
		assertEquals(1000D, entity.totalCurrentAssets, ERROR_TOLERANCE);
		assertEquals(2000D, entity.totalCurrentLiabilities, ERROR_TOLERANCE);
		assertEquals(3000D, entity.totalLiabilities, ERROR_TOLERANCE);
		assertEquals(-4000D, entity.totalEquity, ERROR_TOLERANCE);
		assertEquals(5000D, entity.totalAssets, ERROR_TOLERANCE);
		assertEquals(6000D, entity.netCashFlowOperating, ERROR_TOLERANCE);
		assertNull(entity.capitalExpenditures);
	}
}
