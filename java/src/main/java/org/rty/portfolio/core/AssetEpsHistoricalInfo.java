package org.rty.portfolio.core;

import org.rty.portfolio.core.utils.DatesAndSetUtil;
import org.rty.portfolio.math.Calculator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AssetEpsHistoricalInfo implements CsvWritable {
	public static final String[] HEADER = new String[] { "asset_id", "sector", "industry", "eps_date", "month",
			"prev_after_market_close",
			"prev_pred_eps", "prev_eps", "prev_eps_spr",
			"prev_ngaap_pred_eps", "prev_ngaap_eps", "prev_ngaap_eps_spr",
			"prev_revenue_spr", "prev_p_e",

			"after_market_close",
			"pred_eps", "eps", "eps_spr",
			"ngaap_pred_eps", "ngaap_eps", "ngaap_eps_spr",
			"revenue_spr", "p_e",

			"spr_pred_eps_prev_pred_eps", "spr_eps_prev_eps",
			"spr_ngaap_pred_eps_prev_ngaap_pred_eps", "spr_ngaap_eps_prev_ngaap_eps",

			"prev_2d_rate", "prev_rate", "rate", "next_rate", "next_2d_rate" };

	private static final double PRECISION = 100D;
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public final String assetName;

	public final int sectorIndex;
	public final int industryIndex;
	
	public final AssetEpsInfo currentEps;
	public final AssetNonGaapEpsInfo currentNonGaapEps;

	public final AssetEpsInfo previousEps;
	public final AssetNonGaapEpsInfo previousNonGaapEps;

	public final AssetPriceInfo priceAtCurrentEps;
	public final AssetPriceInfo priceAtPreviousEps;

	public final AssetPriceInfo price2DaysBeforeCurrentEps;
	public final AssetPriceInfo priceBeforeCurrentEps;
	public final AssetPriceInfo priceAfterCurrentEps;
	public final AssetPriceInfo price2DaysAfterCurrentEps;

	public AssetEpsHistoricalInfo(String assetName, int sectorIndex, int industryIndex,
			AssetEpsInfo currentEps, AssetNonGaapEpsInfo currentNonGaapEps,
			AssetEpsInfo previousEps, AssetNonGaapEpsInfo previousNonGaapEps,
			AssetPriceInfo priceAtPreviousEps,
			AssetPriceInfo price2DaysBeforeCurrentEps,
			AssetPriceInfo priceBeforeCurrentEps,
			AssetPriceInfo priceAtCurrentEps,
			AssetPriceInfo priceAfterCurrentEps,
			AssetPriceInfo price2DaysAfterCurrentEps) {
		this.assetName = assetName;
		this.sectorIndex = sectorIndex;
		this.industryIndex = industryIndex;

		this.currentEps = currentEps;
		this.currentNonGaapEps = currentNonGaapEps;

		this.previousEps = previousEps;
		this.previousNonGaapEps = previousNonGaapEps;

		this.priceAtCurrentEps = priceAtCurrentEps;
		this.priceAtPreviousEps = priceAtPreviousEps;

		this.price2DaysBeforeCurrentEps = price2DaysBeforeCurrentEps;
		this.priceBeforeCurrentEps = priceBeforeCurrentEps;
		this.priceAfterCurrentEps = priceAfterCurrentEps;
		this.price2DaysAfterCurrentEps = price2DaysAfterCurrentEps;
	}

	@Override
	public String toString() {
		try {
			return OBJECT_MAPPER.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return "";
		}
	}

	@Override
	public String[] toCsvLine() {
		final double previousPredictedEpsValue = predictedEpsFrom(previousEps, previousNonGaapEps);
		final double previousNonGaapPredictedEpsValue = nonGaapPredictedEpsFrom(previousNonGaapEps, previousEps);
		final double previousNonGaapEpsValue = nonGaapEpsFrom(previousNonGaapEps, previousEps);
		final double previousEpsSurprise = surprise(previousEps.eps, previousPredictedEpsValue);

		final double currentPredictedEpsValue = predictedEpsFrom(currentEps, currentNonGaapEps);
		final double currentNonGaapPredictedEpsValue = nonGaapPredictedEpsFrom(currentNonGaapEps, currentEps);
		final double currentNonGaapEpsValue = nonGaapEpsFrom(currentNonGaapEps, currentEps);
		final double currentEpsSurprise = surprise(currentEps.eps, currentPredictedEpsValue);

		return new String[] {
				assetName,
				"" + sectorIndex,
				"" + industryIndex,
				"" + DatesAndSetUtil.dateToStr(currentEps.date),
				"" + currentEps.date.getMonth(),

				"" + afterMarketClose(previousNonGaapEps),
				"" + round(previousPredictedEpsValue),
				"" + round(previousEps.eps),
				"" + previousEpsSurprise,
				"" + round(previousNonGaapPredictedEpsValue),
				"" + round(previousNonGaapEpsValue),
				"" + surprise(previousNonGaapEpsValue, previousNonGaapPredictedEpsValue),
				"" + revenueSurprise(previousNonGaapEps, previousEpsSurprise),
				"" + pOverE(priceAtPreviousEps.price, previousEps.eps),

				"" + afterMarketClose(currentNonGaapEps),
				"" + round(currentPredictedEpsValue),
				"" + round(currentEps.eps),
				"" + currentEpsSurprise,
				"" + round(currentNonGaapPredictedEpsValue),
				"" + round(currentNonGaapEpsValue),
				"" + surprise(currentNonGaapEpsValue, currentNonGaapPredictedEpsValue),
				"" + revenueSurprise(currentNonGaapEps, currentEpsSurprise),
				"" + pOverE(priceAtCurrentEps.price, currentEps.eps),

				"" + surprise(currentPredictedEpsValue, previousPredictedEpsValue),
				"" + surprise(currentEps.eps, previousEps.eps),
				"" + surprise(currentNonGaapPredictedEpsValue, previousNonGaapPredictedEpsValue),
				"" + surprise(currentNonGaapEpsValue, previousNonGaapEpsValue),

				"" + round(price2DaysBeforeCurrentEps.rate),
				"" + round(priceBeforeCurrentEps.rate),
				"" + round(priceAtCurrentEps.rate),
				emptyRateIfPriceInfoIsNull(priceAfterCurrentEps),
				emptyRateIfPriceInfoIsNull(price2DaysAfterCurrentEps)
		};
	}

	private static String emptyRateIfPriceInfoIsNull(AssetPriceInfo priceInfo) {
		if (priceInfo == null) {
			return "";
		}

		return "" + round(priceInfo.rate);
	}

	private static double revenueSurprise(AssetNonGaapEpsInfo nonGaapEps, double defaultValue) {
		if (nonGaapEps == null || nonGaapEps.revenue == null || nonGaapEps.revenuePredicted == null) {
			return defaultValue;
		}

		return surprise(nonGaapEps.revenue, nonGaapEps.revenuePredicted);
	}

	private static double nonGaapPredictedEpsFrom(AssetNonGaapEpsInfo nonGaapEps, AssetEpsInfo eps) {
		if (nonGaapEps != null && nonGaapEps.epsPredicted != null) {
			return nonGaapEps.epsPredicted;
		}

		if (eps.epsPredicted != null) {
			return eps.epsPredicted;
		}

		if (nonGaapEps != null) {
			return nonGaapEps.eps;
		}

		return eps.eps;
	}

	private static double predictedEpsFrom(AssetEpsInfo eps, AssetNonGaapEpsInfo nonGaapEps) {
		if (eps.epsPredicted != null) {
			return eps.epsPredicted;
		}

		if (nonGaapEps != null && nonGaapEps.epsPredicted != null) {
			return nonGaapEps.epsPredicted;
		}

		return eps.eps;
	}

	private static double nonGaapEpsFrom(AssetNonGaapEpsInfo nonGaapEps, AssetEpsInfo eps) {
		if (nonGaapEps != null) {
			return nonGaapEps.eps;
		}
		return eps.eps;
	}

	private static int afterMarketClose(AssetNonGaapEpsInfo nonGaapEps) {
		if (nonGaapEps != null) {
			return nonGaapEps.afterMarketClose ? 1 : 0;
		}
		return 1;
	}

	private static double round(double v) {
		return Calculator.round(v, PRECISION);
	}

	private static double surprise(double v1, double v2) {
		return Calculator.round(Calculator.calculateEpsSurprise(v1, v2), PRECISION);
	}

	private static double pOverE(double price, double eps) {
		return Calculator.round(Calculator.calculatePriceOverEps(price, eps), PRECISION);
	}
}
