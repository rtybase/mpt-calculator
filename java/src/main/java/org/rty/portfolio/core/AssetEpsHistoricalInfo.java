package org.rty.portfolio.core;

import org.rty.portfolio.core.utils.DataHandlingUtil;
import org.rty.portfolio.core.utils.DatesAndSetUtil;
import org.rty.portfolio.math.Calculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AssetEpsHistoricalInfo implements CsvWritable {
	private static final Logger LOGGER = LoggerFactory.getLogger(AssetEpsHistoricalInfo.class.getSimpleName());

	public static final String[] HEADER = new String[] { "asset_id",
			"sector",

			"industry",
			"eps_date",
			"month",

			"prev_after_market_close",
			"prev_pred_eps",
			"prev_eps",
			"prev_eps_spr",
			"prev_ngaap_pred_eps",
			"prev_ngaap_eps",
			"prev_ngaap_eps_spr",
			"prev_revenue_spr",
			"prev_p_e",

			"after_market_close",
			"pred_eps",
			"eps",
			"eps_spr",
			"ngaap_pred_eps",
			"ngaap_eps",
			"ngaap_eps_spr",
			"revenue_spr",
			"p_e",

			"spr_pred_eps_prev_pred_eps", "spr_eps_prev_eps",
			"spr_ngaap_pred_eps_prev_ngaap_pred_eps", "spr_ngaap_eps_prev_ngaap_eps",

			// as-of "before" or "after" EPS announcement
			// "m" for minus, "p" for plus
			"rate_before_m_1d",
			"v_chng_before_m_1d",

			"rate_before",
			"v_chng_before",

			"rate_after",
			"v_chng_after",

			"rate_after_p_1d",
			"v_chng_after_p_1d" };

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
	private final AssetPriceInfo priceBeforePreviousEps;

	private final AssetPriceInfo price2DaysBeforeCurrentEps;
	private final AssetPriceInfo priceBeforeCurrentEps;
	private final AssetPriceInfo priceAfterCurrentEps;
	private final AssetPriceInfo price2DaysAfterCurrentEps;

	public AssetEpsHistoricalInfo(String assetName, int sectorIndex, int industryIndex,
			AssetEpsInfo currentEps, AssetNonGaapEpsInfo currentNonGaapEps,
			AssetEpsInfo previousEps, AssetNonGaapEpsInfo previousNonGaapEps,
			AssetPriceInfo priceAtPreviousEps,
			AssetPriceInfo priceBeforePreviousEps,
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
		this.priceBeforePreviousEps = priceBeforePreviousEps;

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
		AssetPriceInfo info = DatesAndSetUtil.oneOrTheOther(getPreviousAfterMarketClose() == 1,
				priceAtPreviousEps,
				priceBeforePreviousEps);

		if (info == null) {
			info = priceAtPreviousEps;
		}

		return Calculator.calculatePriceOverEps(info.price, previousEps.eps);
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
		return Calculator.calculatePriceOverEps(getInfoBeforeEpsAnnouncement().price,
				currentEps.eps);
	}

	public AssetPriceInfo getInfoBeforeMinusOneDayEpsAnnouncement() {
		return DatesAndSetUtil.oneOrTheOther(getCurrentAfterMarketClose() == 1,
				priceBeforeCurrentEps,
				price2DaysBeforeCurrentEps);
	}

	public AssetPriceInfo getInfoBeforeEpsAnnouncement() {
		return DatesAndSetUtil.oneOrTheOther(getCurrentAfterMarketClose() == 1,
				priceAtCurrentEps,
				priceBeforeCurrentEps);
	}

	public AssetPriceInfo getInfoAfterEpsAnnouncement() {
		return DatesAndSetUtil.oneOrTheOther(getCurrentAfterMarketClose() == 1,
				priceAfterCurrentEps,
				priceAtCurrentEps);
	}

	public AssetPriceInfo getInfoAfterPlusOneDayEpsAnnouncement() {
		return DatesAndSetUtil.oneOrTheOther(getCurrentAfterMarketClose() == 1,
				price2DaysAfterCurrentEps,
				priceAfterCurrentEps);
	}

	public boolean isGoodForAfterPlusOneDayEpsTraining() {
		return DataHandlingUtil.allNotNull(getInfoBeforeMinusOneDayEpsAnnouncement(),
				getInfoBeforeEpsAnnouncement(),
				getInfoAfterEpsAnnouncement(),
				getInfoAfterPlusOneDayEpsAnnouncement());
	}

	public boolean isGoodForAfterEpsTraining() {
		return DataHandlingUtil.allNotNull(getInfoBeforeMinusOneDayEpsAnnouncement(),
				getInfoBeforeEpsAnnouncement(),
				getInfoAfterEpsAnnouncement());
	}

	public boolean isGoodForAfterEpsPrediction() {
		return DataHandlingUtil.allNotNull(getInfoBeforeMinusOneDayEpsAnnouncement(),
				getInfoBeforeEpsAnnouncement());
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
				"" + industryIndex,
				"" + DatesAndSetUtil.dateToStr(currentEps.date),
				"" + getMonthIndex(),

				"" + getPreviousAfterMarketClose(),
				"" + round(previousPredictedEpsValue),
				"" + round(previousEps.eps),
				"" + previousEpsSurprise,
				"" + round(previousNonGaapPredictedEpsValue),
				"" + round(previousNonGaapEpsValue),
				"" + surprise(previousNonGaapEpsValue, previousNonGaapPredictedEpsValue),
				"" + revenueSurprise(previousNonGaapEps, previousEpsSurprise),
				"" + round(getPreviousPOverE()),

				"" + getCurrentAfterMarketClose(),
				"" + round(currentPredictedEpsValue),
				"" + round(currentEps.eps),
				"" + currentEpsSurprise,
				"" + round(currentNonGaapPredictedEpsValue),
				"" + round(currentNonGaapEpsValue),
				"" + surprise(currentNonGaapEpsValue, currentNonGaapPredictedEpsValue),
				"" + revenueSurprise(currentNonGaapEps, currentEpsSurprise),
				"" + round(getCurrentPOverE()),

				"" + surprise(currentPredictedEpsValue, previousPredictedEpsValue),
				"" + surprise(currentEps.eps, previousEps.eps),
				"" + surprise(currentNonGaapPredictedEpsValue, previousNonGaapPredictedEpsValue),
				"" + surprise(currentNonGaapEpsValue, previousNonGaapEpsValue),

				rateIfAvailable(getInfoBeforeMinusOneDayEpsAnnouncement()),
				volumeChangeRateIfAvailable(getInfoBeforeMinusOneDayEpsAnnouncement()),

				rateIfAvailable(getInfoBeforeEpsAnnouncement()),
				volumeChangeRateIfAvailable(getInfoBeforeEpsAnnouncement()),

				rateIfAvailable(getInfoAfterEpsAnnouncement()),
				volumeChangeRateIfAvailable(getInfoAfterEpsAnnouncement()),

				rateIfAvailable(getInfoAfterPlusOneDayEpsAnnouncement()),
				volumeChangeRateIfAvailable(getInfoAfterPlusOneDayEpsAnnouncement())
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
