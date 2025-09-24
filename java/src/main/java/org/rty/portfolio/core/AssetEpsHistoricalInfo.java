package org.rty.portfolio.core;

import org.rty.portfolio.core.utils.DatesAndSetUtil;
import org.rty.portfolio.math.Calculator;
import org.rty.portfolio.math.ZScoreCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AssetEpsHistoricalInfo implements CsvWritable {
	private static final Logger LOGGER = LoggerFactory.getLogger(AssetEpsHistoricalInfo.class.getSimpleName());

	public static final String[] HEADER = new String[] { "asset_id",
			"sector",
			"zs_sector",

			"industry",
			"zs_industry",
			"eps_date",

			"month",
			"zs_month",

			"prev_after_market_close",
			"zs_prev_after_market_close",

			"prev_pred_eps",
			"zs_prev_pred_eps",

			"prev_eps",
			"zs_prev_eps",

			"prev_eps_spr",

			"prev_ngaap_pred_eps",
			"zs_prev_ngaap_pred_eps",

			"prev_ngaap_eps",
			"zs_prev_ngaap_eps",

			"prev_ngaap_eps_spr",
			"prev_revenue_spr",

			"prev_p_e",
			"zs_prev_p_e",

			"after_market_close",
			"zs_after_market_close",

			"pred_eps",
			"zs_pred_eps",

			"eps",
			"zs_eps",

			"eps_spr",

			"ngaap_pred_eps",
			"zs_ngaap_pred_eps",

			"ngaap_eps",
			"zs_ngaap_eps",

			"ngaap_eps_spr",
			"revenue_spr",

			"p_e",
			"zs_p_e",

			"spr_pred_eps_prev_pred_eps", "spr_eps_prev_eps",
			"spr_ngaap_pred_eps_prev_ngaap_pred_eps", "spr_ngaap_eps_prev_ngaap_eps",

			"prev_2d_rate",
			"prev_2d_v_chng_rate",

			"prev_rate",
			"prev_v_chng_rate",

			"rate",
			"v_chng_rate",

			"next_rate",
			"next_v_chng_rate",

			"next_2d_rate",
			"next_2d_v_chng_rate" };

	private static final double PRECISION = 100D;
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private final String assetName;

	private final int sectorIndex;
	private final int industryIndex;
	
	private final AssetEpsInfo currentEps;
	private final AssetNonGaapEpsInfo currentNonGaapEps;

	private final AssetEpsInfo previousEps;
	private final AssetNonGaapEpsInfo previousNonGaapEps;

	private final AssetPriceInfo priceAtCurrentEps;
	private final AssetPriceInfo priceAtPreviousEps;

	private final AssetPriceInfo price2DaysBeforeCurrentEps;
	private final AssetPriceInfo priceBeforeCurrentEps;
	private final AssetPriceInfo priceAfterCurrentEps;
	private final AssetPriceInfo price2DaysAfterCurrentEps;

	private final ZScoreCalculator zScoreCalculatorForSector;
	private final ZScoreCalculator zScoreCalculatorForIndustry;
	private final ZScoreCalculator zScoreCalculatorForMoth;

	private final ZScoreCalculator zScoreCalculatorForPreviousAfterMarketClose;
	private final ZScoreCalculator zScoreCalculatorForPreviousPredictedEps;
	private final ZScoreCalculator zScoreCalculatorForPreviousEps;
	private final ZScoreCalculator zScoreCalculatorForPreviousNonGaapPredictedEps;
	private final ZScoreCalculator zScoreCalculatorForPreviousNonGaapEps;
	private final ZScoreCalculator zScoreCalculatorForPreviousPOverE;

	private final ZScoreCalculator zScoreCalculatorForCurrentAfterMarketClose;
	private final ZScoreCalculator zScoreCalculatorForCurrentPredictedEps;
	private final ZScoreCalculator zScoreCalculatorForCurrentEps;
	private final ZScoreCalculator zScoreCalculatorForCurrentNonGaapPredictedEps;
	private final ZScoreCalculator zScoreCalculatorForCurrentNonGaapEps;
	private final ZScoreCalculator zScoreCalculatorForCurrentPOverE;

	public AssetEpsHistoricalInfo(String assetName, int sectorIndex, int industryIndex,
			AssetEpsInfo currentEps, AssetNonGaapEpsInfo currentNonGaapEps,
			AssetEpsInfo previousEps, AssetNonGaapEpsInfo previousNonGaapEps,
			AssetPriceInfo priceAtPreviousEps,
			AssetPriceInfo price2DaysBeforeCurrentEps,
			AssetPriceInfo priceBeforeCurrentEps,
			AssetPriceInfo priceAtCurrentEps,
			AssetPriceInfo priceAfterCurrentEps,
			AssetPriceInfo price2DaysAfterCurrentEps,
			ZScoreCalculator zScoreCalculatorForSector,
			ZScoreCalculator zScoreCalculatorForIndustry,
			ZScoreCalculator zScoreCalculatorForMoth,
			ZScoreCalculator zScoreCalculatorForPreviousAfterMarketClose,
			ZScoreCalculator zScoreCalculatorForPreviousPredictedEps,
			ZScoreCalculator zScoreCalculatorForPreviousEps,
			ZScoreCalculator zScoreCalculatorForPreviousNonGaapPredictedEps,
			ZScoreCalculator zScoreCalculatorForPreviousNonGaapEps,
			ZScoreCalculator zScoreCalculatorForPreviousPOverE,
			ZScoreCalculator zScoreCalculatorForCurrentAfterMarketClose,
			ZScoreCalculator zScoreCalculatorForCurrentPredictedEps,
			ZScoreCalculator zScoreCalculatorForCurrentEps,
			ZScoreCalculator zScoreCalculatorForCurrentNonGaapPredictedEps,
			ZScoreCalculator zScoreCalculatorForCurrentNonGaapEps,
			ZScoreCalculator zScoreCalculatorForCurrentPOverE) {
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

		this.zScoreCalculatorForSector = zScoreCalculatorForSector;
		this.zScoreCalculatorForIndustry = zScoreCalculatorForIndustry;
		this.zScoreCalculatorForMoth = zScoreCalculatorForMoth;
		this.zScoreCalculatorForPreviousAfterMarketClose = zScoreCalculatorForPreviousAfterMarketClose;
		this.zScoreCalculatorForPreviousPredictedEps = zScoreCalculatorForPreviousPredictedEps;
		this.zScoreCalculatorForPreviousEps = zScoreCalculatorForPreviousEps;
		this.zScoreCalculatorForPreviousNonGaapPredictedEps = zScoreCalculatorForPreviousNonGaapPredictedEps;
		this.zScoreCalculatorForPreviousNonGaapEps = zScoreCalculatorForPreviousNonGaapEps;
		this.zScoreCalculatorForPreviousPOverE = zScoreCalculatorForPreviousPOverE;
		this.zScoreCalculatorForCurrentAfterMarketClose = zScoreCalculatorForCurrentAfterMarketClose;
		this.zScoreCalculatorForCurrentPredictedEps = zScoreCalculatorForCurrentPredictedEps;
		this.zScoreCalculatorForCurrentEps = zScoreCalculatorForCurrentEps;
		this.zScoreCalculatorForCurrentNonGaapPredictedEps = zScoreCalculatorForCurrentNonGaapPredictedEps;
		this.zScoreCalculatorForCurrentNonGaapEps = zScoreCalculatorForCurrentNonGaapEps;
		this.zScoreCalculatorForCurrentPOverE = zScoreCalculatorForCurrentPOverE;

		zScoreCalculatorForSector.add(sectorIndex);
		zScoreCalculatorForIndustry.add(industryIndex);
		zScoreCalculatorForMoth.add(getMonthIndex());
		zScoreCalculatorForPreviousAfterMarketClose.add(getPreviousAfterMarketClose());
		zScoreCalculatorForPreviousPredictedEps.add(getPreviousPredictedEps());
		zScoreCalculatorForPreviousEps.add(previousEps.eps);
		zScoreCalculatorForPreviousNonGaapPredictedEps.add(getPreviousNonGaapPredictedEps());
		zScoreCalculatorForPreviousNonGaapEps.add(getPreviousNonGaapEps());
		zScoreCalculatorForPreviousPOverE.add(getPreviousPOverE());
		zScoreCalculatorForCurrentAfterMarketClose.add(getCurrentAfterMarketClose());
		zScoreCalculatorForCurrentPredictedEps.add(getCurrentPredictedEps());
		zScoreCalculatorForCurrentEps.add(currentEps.eps);
		zScoreCalculatorForCurrentNonGaapPredictedEps.add(getCurrentNonGaapPredictedEps());
		zScoreCalculatorForCurrentNonGaapEps.add(getCurrentNonGaapEps());
		zScoreCalculatorForCurrentPOverE.add(getCurrentPOverE());
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

	public int getMonthIndex() {
		return currentEps.date.getMonth();
	}

	public int getPreviousAfterMarketClose() {
		return afterMarketClose(previousNonGaapEps);
	}

	public double getPreviousPredictedEps() {
		return predictedEpsFrom(previousEps, previousNonGaapEps);
	}

	public double getPreviousNonGaapPredictedEps() {
		return nonGaapPredictedEpsFrom(previousNonGaapEps, previousEps);
	}

	public double getPreviousNonGaapEps() {
		return nonGaapEpsFrom(previousNonGaapEps, previousEps);
	}

	public double getPreviousPOverE() {
		return Calculator.calculatePriceOverEps(priceAtPreviousEps.price, previousEps.eps);
	}

	public int getCurrentAfterMarketClose() {
		return afterMarketClose(currentNonGaapEps);
	}

	public double getCurrentPredictedEps() {
		return predictedEpsFrom(currentEps, currentNonGaapEps);
	}

	public double getCurrentNonGaapPredictedEps() {
		return nonGaapPredictedEpsFrom(currentNonGaapEps, currentEps);
	}

	public double getCurrentNonGaapEps() {
		return nonGaapEpsFrom(currentNonGaapEps, currentEps);
	}

	public double getCurrentPOverE() {
		return Calculator.calculatePriceOverEps(priceAtCurrentEps.price, currentEps.eps);
	}

	@Override
	public String[] toCsvLine() {
		final double previousPredictedEpsValue = getPreviousPredictedEps();
		final double previousNonGaapPredictedEpsValue = getPreviousNonGaapPredictedEps();
		final double previousNonGaapEpsValue = getPreviousNonGaapEps();
		final double previousEpsSurprise = surprise(previousEps.eps, previousPredictedEpsValue);

		final double currentPredictedEpsValue = getCurrentPredictedEps();
		final double currentNonGaapPredictedEpsValue = getCurrentNonGaapPredictedEps();
		final double currentNonGaapEpsValue = getCurrentNonGaapEps();
		final double currentEpsSurprise = surprise(currentEps.eps, currentPredictedEpsValue);

		return new String[] {
				assetName,
				"" + sectorIndex,
				"" + round(zScoreCalculatorForSector.calculateZScore(sectorIndex)),

				"" + industryIndex,
				"" + round(zScoreCalculatorForIndustry.calculateZScore(industryIndex)),

				"" + DatesAndSetUtil.dateToStr(currentEps.date),

				"" + getMonthIndex(),
				"" + round(zScoreCalculatorForMoth.calculateZScore(getMonthIndex())),

				"" + getPreviousAfterMarketClose(),
				"" + round(zScoreCalculatorForPreviousAfterMarketClose.calculateZScore(getPreviousAfterMarketClose())),

				"" + round(previousPredictedEpsValue),
				"" + round(zScoreCalculatorForPreviousPredictedEps.calculateZScore(previousPredictedEpsValue)),

				"" + round(previousEps.eps),
				"" + round(zScoreCalculatorForPreviousEps.calculateZScore(previousEps.eps)),

				"" + previousEpsSurprise,

				"" + round(previousNonGaapPredictedEpsValue),
				"" + round(zScoreCalculatorForPreviousNonGaapPredictedEps.calculateZScore(previousNonGaapPredictedEpsValue)),

				"" + round(previousNonGaapEpsValue),
				"" + round(zScoreCalculatorForPreviousNonGaapEps.calculateZScore(previousNonGaapEpsValue)),

				"" + surprise(previousNonGaapEpsValue, previousNonGaapPredictedEpsValue),
				"" + revenueSurprise(previousNonGaapEps, previousEpsSurprise),

				"" + round(getPreviousPOverE()),
				"" + round(zScoreCalculatorForPreviousPOverE.calculateZScore(getPreviousPOverE())),

				"" + getCurrentAfterMarketClose(),
				"" + round(zScoreCalculatorForCurrentAfterMarketClose.calculateZScore(getCurrentAfterMarketClose())),

				"" + round(currentPredictedEpsValue),
				"" + round(zScoreCalculatorForCurrentPredictedEps.calculateZScore(currentPredictedEpsValue)),

				"" + round(currentEps.eps),
				"" + round(zScoreCalculatorForCurrentEps.calculateZScore(currentEps.eps)),

				"" + currentEpsSurprise,

				"" + round(currentNonGaapPredictedEpsValue),
				"" + round(zScoreCalculatorForCurrentNonGaapPredictedEps.calculateZScore(currentNonGaapPredictedEpsValue)),

				"" + round(currentNonGaapEpsValue),
				"" + round(zScoreCalculatorForCurrentNonGaapEps.calculateZScore(currentNonGaapEpsValue)),

				"" + surprise(currentNonGaapEpsValue, currentNonGaapPredictedEpsValue),
				"" + revenueSurprise(currentNonGaapEps, currentEpsSurprise),

				"" + round(getCurrentPOverE()),
				"" + round(zScoreCalculatorForCurrentPOverE.calculateZScore(getCurrentPOverE())),

				"" + surprise(currentPredictedEpsValue, previousPredictedEpsValue),
				"" + surprise(currentEps.eps, previousEps.eps),
				"" + surprise(currentNonGaapPredictedEpsValue, previousNonGaapPredictedEpsValue),
				"" + surprise(currentNonGaapEpsValue, previousNonGaapEpsValue),

				rateIfAvailable(price2DaysBeforeCurrentEps),
				volumeChangeRateIfAvailable(price2DaysBeforeCurrentEps),

				rateIfAvailable(priceBeforeCurrentEps),
				volumeChangeRateIfAvailable(priceBeforeCurrentEps),

				rateIfAvailable(priceAtCurrentEps),
				volumeChangeRateIfAvailable(priceAtCurrentEps),

				rateIfAvailable(priceAfterCurrentEps),
				volumeChangeRateIfAvailable(priceAfterCurrentEps),

				rateIfAvailable(price2DaysAfterCurrentEps),
				volumeChangeRateIfAvailable(price2DaysAfterCurrentEps)
		};
	}

	private static String rateIfAvailable(AssetPriceInfo priceInfo) {
		if (priceInfo == null) {
			return "";
		}

		return "" + round(priceInfo.rate);
	}

	private String volumeChangeRateIfAvailable(AssetPriceInfo priceInfo) {
		if (priceInfo == null) {
			return "";
		}

		if (priceInfo.volumeChangeRate == null) {
			LOGGER.info("Volume change rate is null for '{}' on '{}', assuming 0.",
					assetName,
					DatesAndSetUtil.dateToStr(currentEps.date));
			return "0";
		}

		return "" + round(priceInfo.volumeChangeRate);
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
}
