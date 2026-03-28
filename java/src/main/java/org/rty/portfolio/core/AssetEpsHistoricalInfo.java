package org.rty.portfolio.core;

import java.util.Optional;

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
			"prev_no_analysts",
			"prev_ngaap_pred_eps",
			"prev_ngaap_eps",
			"prev_ngaap_eps_spr",
			"prev_revenue_spr",
			"prev_p_e",
			"prev_f_score",
			"prev_div_yld",

			"prev_cu_ratio",
			"prev_to_ratio",
			"prev_d_e_calc",
			"prev_d_e_rep",
			"prev_fcf_ps",
			"prev_p_b",

			"after_market_close",
			"pred_eps",
			"eps",
			"eps_spr",
			"no_analysts",
			"ngaap_pred_eps",
			"ngaap_eps",
			"ngaap_eps_spr",
			"revenue_spr",
			"p_e",
			"f_score",
			"div_yld",

			"cu_ratio",
			"to_ratio",
			"d_e_calc",
			"d_e_rep",
			"fcf_ps",
			"p_b",

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

	public final String assetName;

	public final int sectorIndex;
	public final int industryIndex;
	
	public final AssetEpsInfo currentEps;
	public final AssetNonGaapEpsInfo currentNonGaapEps;
	public final double currentFScore;
	public final AssetDividendInfo currentDividend;
	public final AssetFinancialInfo currentFinancialInfo;

	public final AssetEpsInfo previousEps;
	public final AssetNonGaapEpsInfo previousNonGaapEps;
	public final double previousFScore;
	public final AssetDividendInfo previousDividend;
	public final AssetFinancialInfo previousFinancialInfo;

	public final AssetPriceInfo priceAtCurrentEps;
	public final AssetPriceInfo priceAtPreviousEps;
	public final AssetPriceInfo priceBeforePreviousEps;

	public final AssetPriceInfo price2DaysBeforeCurrentEps;
	public final AssetPriceInfo priceBeforeCurrentEps;
	public final AssetPriceInfo priceAfterCurrentEps;
	public final AssetPriceInfo price2DaysAfterCurrentEps;

	public AssetEpsHistoricalInfo(String assetName, int sectorIndex, int industryIndex,
			AssetEpsInfo currentEps, AssetNonGaapEpsInfo currentNonGaapEps, double currentFScore,
			AssetDividendInfo currentDividend, AssetFinancialInfo currentFinancialInfo,
			AssetEpsInfo previousEps, AssetNonGaapEpsInfo previousNonGaapEps,  double previousFScore,
			AssetDividendInfo previousDividend, AssetFinancialInfo previousFinancialInfo,
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
		this.currentFScore = currentFScore;
		this.currentDividend = currentDividend;
		this.currentFinancialInfo = currentFinancialInfo;

		this.previousEps = previousEps;
		this.previousNonGaapEps = previousNonGaapEps;
		this.previousFScore = previousFScore;
		this.previousDividend = previousDividend;
		this.previousFinancialInfo = previousFinancialInfo;

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
		return Calculator.calculatePriceOverEps(getInfoBeforePreviousEpsAnnouncement().price,
				previousEps.eps);
	}

	public double getPreviousDividendYield() {
		if (previousDividend == null) {
			return 0D;
		}

		return Calculator.calculateDividendYield(previousDividend.pay,
				getInfoBeforePreviousEpsAnnouncement().price);
	}

	public double getPreviousCurrentRatio() {
		return Optional.ofNullable(previousFinancialInfo).map(AssetFinancialInfo::currentRatio).orElse(0D);
	}

	public double getPreviousTotalRatio() {
		return Optional.ofNullable(previousFinancialInfo).map(AssetFinancialInfo::totalRatio).orElse(0D);
	}

	public double getPreviousDebtOverEquityCalculated() {
		return Optional.ofNullable(previousFinancialInfo).map(AssetFinancialInfo::debtOverEquityCalculated).orElse(0D);
	}

	public double getPreviousDebtOverEquityReported() {
		return Optional.ofNullable(previousFinancialInfo).map(AssetFinancialInfo::debtOverEquityReported).orElse(0D);
	}

	public double getPreviousFreeCashFlowPerShare() {
		return Optional.ofNullable(previousFinancialInfo).map(AssetFinancialInfo::freeCashFlowPerShare).orElse(0D);
	}

	public double getPreviousPOverB() {
		final double bookValuePerShare = Optional.ofNullable(previousFinancialInfo)
				.map(AssetFinancialInfo::bookValuePerShare).orElse(0D);
		if (Calculator.almostZero(bookValuePerShare)) {
			return 0D;
		}

		return getInfoBeforePreviousEpsAnnouncement().price / bookValuePerShare;
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

	public double getCurrentDividendYield() {
		if (currentDividend == null) {
			return 0D;
		}

		return Calculator.calculateDividendYield(currentDividend.pay,
				getInfoBeforeEpsAnnouncement().price);
	}

	public double getCurrentCurrentRatio() {
		return Optional.ofNullable(currentFinancialInfo).map(AssetFinancialInfo::currentRatio).orElse(0D);
	}

	public double getCurrentTotalRatio() {
		return Optional.ofNullable(currentFinancialInfo).map(AssetFinancialInfo::totalRatio).orElse(0D);
	}

	public double getCurrentDebtOverEquityCalculated() {
		return Optional.ofNullable(currentFinancialInfo).map(AssetFinancialInfo::debtOverEquityCalculated).orElse(0D);
	}

	public double getCurrentDebtOverEquityReported() {
		return Optional.ofNullable(currentFinancialInfo).map(AssetFinancialInfo::debtOverEquityReported).orElse(0D);
	}

	public double getCurrentFreeCashFlowPerShare() {
		return Optional.ofNullable(currentFinancialInfo).map(AssetFinancialInfo::freeCashFlowPerShare).orElse(0D);
	}

	public double getCurrentPOverB() {
		final double bookValuePerShare = Optional.ofNullable(currentFinancialInfo)
				.map(AssetFinancialInfo::bookValuePerShare).orElse(0D);
		if (Calculator.almostZero(bookValuePerShare)) {
			return 0D;
		}

		return getInfoBeforeEpsAnnouncement().price / bookValuePerShare;
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

	public AssetPriceInfo getInfoBeforePreviousEpsAnnouncement() {
		AssetPriceInfo info = DatesAndSetUtil.oneOrTheOther(getPreviousAfterMarketClose() == 1,
				priceAtPreviousEps,
				priceBeforePreviousEps);

		if (info == null) {
			return priceAtPreviousEps;
		}

		return info;
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
				numberOfAnalystsFrom(previousEps),
				"" + round(previousNonGaapPredictedEpsValue),
				"" + round(previousNonGaapEpsValue),
				"" + surprise(previousNonGaapEpsValue, previousNonGaapPredictedEpsValue),
				"" + revenueSurprise(previousNonGaapEps, previousEpsSurprise),
				"" + round(getPreviousPOverE()),
				"" + previousFScore,
				"" + round(getPreviousDividendYield()),

				"" + round(getPreviousCurrentRatio()),
				"" + round(getPreviousTotalRatio()),
				"" + round(getPreviousDebtOverEquityCalculated()),
				"" + round(getPreviousDebtOverEquityReported()),
				"" + round(getPreviousFreeCashFlowPerShare()),
				"" + round(getPreviousPOverB()),

				"" + getCurrentAfterMarketClose(),
				"" + round(currentPredictedEpsValue),
				"" + round(currentEps.eps),
				"" + currentEpsSurprise,
				numberOfAnalystsFrom(currentEps),
				"" + round(currentNonGaapPredictedEpsValue),
				"" + round(currentNonGaapEpsValue),
				"" + surprise(currentNonGaapEpsValue, currentNonGaapPredictedEpsValue),
				"" + revenueSurprise(currentNonGaapEps, currentEpsSurprise),
				"" + round(getCurrentPOverE()),
				"" + currentFScore,
				"" + round(getCurrentDividendYield()),

				"" + round(getCurrentCurrentRatio()),
				"" + round(getCurrentTotalRatio()),
				"" + round(getCurrentDebtOverEquityCalculated()),
				"" + round(getCurrentDebtOverEquityReported()),
				"" + round(getCurrentFreeCashFlowPerShare()),
				"" + round(getCurrentPOverB()),

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

	private static String numberOfAnalystsFrom(AssetEpsInfo eps) {
		final Integer noOfAnalysts = eps.noOfAnalysts;
		final Double predictedEps = eps.epsPredicted;

		if (noOfAnalysts == null) {
			if (predictedEps != null) {
				LOGGER.info("Number of analysts is null for '{}' on '{}', assuming 1.",
						eps.assetName,
						DatesAndSetUtil.dateToStr(eps.date));
			}
			return "1";
		}

		return "" + noOfAnalysts;
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
		return 0;
	}

	private static double round(double v) {
		return Calculator.round(v, PRECISION);
	}

	private static double surprise(double v1, double v2) {
		return Calculator.round(Calculator.calculateEpsSurprise(v1, v2), PRECISION);
	}
}
