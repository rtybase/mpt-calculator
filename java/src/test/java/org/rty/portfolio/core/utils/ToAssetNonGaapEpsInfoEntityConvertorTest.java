package org.rty.portfolio.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.rty.portfolio.core.AssetNonGaapEpsInfo;

class ToAssetNonGaapEpsInfoEntityConvertorTest extends CommonTestRoutines {
	private static final String[] HEADER_LINE = new String[] { "currency_id", "date", "earning_date_type",
			"eps_forecast", "instrument_id", "market_phase", "report_month", "report_year", "revenue_forecast",
			"eps_revisions.eps_down_revisions_90d", "eps_revisions.eps_up_revisions_90d", "eps_actual", "eps_surprise",
			"fiscal_quarter", "fiscal_year", "revenue_actual", "revenue_surprise", "price_change.change_percent",
			"price_change.price_after", "price_change.price_before" };

	private static final String[] TEST_LINE = new String[] { "12", "2025-07-17", "OFFICIAL", "1.37", TEST_ASSET,
			"AFTER_HOURS", "7", "2025", "1670000000", "", "", "1.38", "0.0", "3.0", "2025.0", "1740000000.0", "4.19",
			"5.63", "124.96", "118.3" };

	@Test
	void testToEntity() {
		final ToAssetNonGaapEpsInfoEntityConvertor convertor = new ToAssetNonGaapEpsInfoEntityConvertor(List.of());

		convertor.updateHeadersFrom("test_file.csv", HEADER_LINE);

		assertEquals(TEST_ASSET, convertor.assetNameFrom(TEST_LINE));
		final AssetNonGaapEpsInfo entity = convertor.toEntity(TEST_ASSET, TEST_LINE);

		assertEquals(D_2025_07_17, entity.date);
		assertEquals(1.37D, entity.epsPredicted, ERROR_TOLERANCE);
		verifyEntityContent(entity);
	}

	@Test
	void testToEntityWithDateCorrection() {
		final ToAssetNonGaapEpsInfoEntityConvertor convertor = new ToAssetNonGaapEpsInfoEntityConvertor(
				List.of(assetEpsFrom(TEST_ASSET, "07/16/2025")));

		convertor.updateHeadersFrom("test_file.csv", HEADER_LINE);

		assertEquals(TEST_ASSET, convertor.assetNameFrom(TEST_LINE));
		final AssetNonGaapEpsInfo entity = convertor.toEntity(TEST_ASSET, TEST_LINE);

		assertEquals(dateFrom(16), entity.date);
		assertEquals(1.37D, entity.epsPredicted, ERROR_TOLERANCE);
		verifyEntityContent(entity);
	}

	@Test
	void testToEntityNoDateCorrection() {
		final ToAssetNonGaapEpsInfoEntityConvertor convertor = new ToAssetNonGaapEpsInfoEntityConvertor(
				List.of(assetEpsFrom(TEST_ASSET, "07/17/2025")));

		convertor.updateHeadersFrom("test_file.csv", HEADER_LINE);

		assertEquals(TEST_ASSET, convertor.assetNameFrom(TEST_LINE));
		final AssetNonGaapEpsInfo entity = convertor.toEntity(TEST_ASSET, TEST_LINE);

		assertEquals(D_2025_07_17, entity.date);
		assertEquals(1.37D, entity.epsPredicted, ERROR_TOLERANCE);
		verifyEntityContent(entity);
	}

	@Test
	void testToEntityOptionalColumnMissing() {
		final String[] headerWithoutEpsForecast = new String[] { "currency_id", "date", "earning_date_type",
				"instrument_id", "market_phase", "report_month", "report_year", "revenue_forecast",
				"eps_revisions.eps_down_revisions_90d", "eps_revisions.eps_up_revisions_90d", "eps_actual",
				"eps_surprise", "fiscal_quarter", "fiscal_year", "revenue_actual", "revenue_surprise",
				"price_change.change_percent", "price_change.price_after", "price_change.price_before" };

		final String[] line = new String[] { "12", "2025-07-17", "OFFICIAL", TEST_ASSET, "AFTER_HOURS", "7", "2025",
				"1670000000", "", "", "1.38", "0.0", "3.0", "2025.0", "1740000000.0", "4.19", "5.63", "124.96",
				"118.3" };

		final ToAssetNonGaapEpsInfoEntityConvertor convertor = new ToAssetNonGaapEpsInfoEntityConvertor(List.of());

		convertor.updateHeadersFrom("test_file.csv", headerWithoutEpsForecast);

		assertEquals(TEST_ASSET, convertor.assetNameFrom(line));
		final AssetNonGaapEpsInfo entity = convertor.toEntity(TEST_ASSET, line);

		assertEquals(D_2025_07_17, entity.date);
		assertNull(entity.epsPredicted);
		verifyEntityContent(entity);
	}

	@Test
	void testMandatoryColumnNotPresent() {
		final String[] headerWithoutDate = new String[] { "currency_id", "earning_date_type", "eps_forecast",
				"instrument_id", "market_phase", "report_month", "report_year", "revenue_forecast",
				"eps_revisions.eps_down_revisions_90d", "eps_revisions.eps_up_revisions_90d", "eps_actual",
				"eps_surprise", "fiscal_quarter", "fiscal_year", "revenue_actual", "revenue_surprise",
				"price_change.change_percent", "price_change.price_after", "price_change.price_before" };

		final ToAssetNonGaapEpsInfoEntityConvertor convertor = new ToAssetNonGaapEpsInfoEntityConvertor(List.of());
		assertThrows(IllegalArgumentException.class,
				() -> convertor.updateHeadersFrom("test_file.csv", headerWithoutDate));

	}

	private void verifyEntityContent(final AssetNonGaapEpsInfo entity) {
		assertEquals(TEST_ASSET, entity.assetName);
		assertTrue(entity.afterMarketClose);
		assertEquals(1.38D, entity.eps, ERROR_TOLERANCE);
		assertEquals(1740000000D, entity.revenue, ERROR_TOLERANCE);
		assertEquals(1670000000D, entity.revenuePredicted, ERROR_TOLERANCE);
	}

}
