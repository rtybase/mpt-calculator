package org.rty.portfolio.core;

import java.util.Optional;

import org.rty.portfolio.core.utils.DataHandlingUtil;
import org.rty.portfolio.core.utils.DatesAndSetUtil;
import org.rty.portfolio.core.utils.HistoricalRowGenerator;
import org.rty.portfolio.core.utils.HistoricalRowGenerator.ColumnKind;
import org.rty.portfolio.core.utils.HistoricalRowGenerator.Row;
import org.rty.portfolio.core.utils.HistoricalRowGenerator.RowHeader;
import org.rty.portfolio.math.Calculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AssetEpsHistoricalInfo implements CsvWritable {
	private static final Logger LOGGER = LoggerFactory.getLogger(AssetEpsHistoricalInfo.class.getSimpleName());

	public static final RowHeader HEADER = RowHeader
			.startWithHeader("asset_id", ColumnKind.CURRENT_VALUE_ONLY)
			.addHeader("sector", ColumnKind.CURRENT_VALUE_ONLY)
			.addHeader("industry", ColumnKind.CURRENT_VALUE_ONLY)
			.addHeader("eps_date", ColumnKind.CURRENT_VALUE_ONLY)
			.addHeader("month", ColumnKind.CURRENT_VALUE_ONLY)

			.addHeader("after_market_close", ColumnKind.CURRENT_AND_PREVIOUS_VALUES)
			.addHeader("no_analysts", ColumnKind.CURRENT_AND_PREVIOUS_VALUES)
			.addHeader("f_score", ColumnKind.CURRENT_AND_PREVIOUS_VALUES)
			.addHeader("eps_spr", ColumnKind.CURRENT_AND_PREVIOUS_VALUES)
			.addHeader("ngaap_eps_spr", ColumnKind.CURRENT_AND_PREVIOUS_VALUES)

			.addHeader("revenue_spr", ColumnKind.CURRENT_AND_PREVIOUS_VALUES)
			.addHeader("revenue_ch_r", ColumnKind.CURRENT_VALUE_ONLY)

			.addHeader("pred_eps", ColumnKind.CURRENT_PREVIOUS_AND_CHANGE_RATE_VALUES)
			.addHeader("eps", ColumnKind.CURRENT_PREVIOUS_AND_CHANGE_RATE_VALUES)
			.addHeader("ngaap_pred_eps", ColumnKind.CURRENT_PREVIOUS_AND_CHANGE_RATE_VALUES)
			.addHeader("ngaap_eps", ColumnKind.CURRENT_PREVIOUS_AND_CHANGE_RATE_VALUES)

			.addHeader("p_e", ColumnKind.CURRENT_PREVIOUS_AND_CHANGE_RATE_VALUES)
			.addHeader("p_b", ColumnKind.CURRENT_PREVIOUS_AND_CHANGE_RATE_VALUES)
			.addHeader("div_yld", ColumnKind.CURRENT_PREVIOUS_AND_CHANGE_RATE_VALUES)
			.addHeader("cu_ratio", ColumnKind.CURRENT_PREVIOUS_AND_CHANGE_RATE_VALUES)
			.addHeader("to_ratio", ColumnKind.CURRENT_PREVIOUS_AND_CHANGE_RATE_VALUES)
			.addHeader("d_e_calc", ColumnKind.CURRENT_PREVIOUS_AND_CHANGE_RATE_VALUES)
			.addHeader("d_e_rep", ColumnKind.CURRENT_PREVIOUS_AND_CHANGE_RATE_VALUES)

			.addHeader("fcf_ps", ColumnKind.CURRENT_PREVIOUS_AND_CHANGE_RATE_VALUES)
			.addHeader("fcf_ch_r", ColumnKind.CURRENT_VALUE_ONLY)

			// as-of "before" or "after" EPS announcement
			// "m" for minus, "p" for plus
			.addHeader("rate_before_m_1d", ColumnKind.CURRENT_VALUE_ONLY)
			.addHeader("v_chng_before_m_1d", ColumnKind.CURRENT_VALUE_ONLY)
			.addHeader("rate_before", ColumnKind.CURRENT_VALUE_ONLY)
			.addHeader("v_chng_before", ColumnKind.CURRENT_VALUE_ONLY)
			.addHeader("rate_after", ColumnKind.CURRENT_VALUE_ONLY)
			.addHeader("v_chng_after", ColumnKind.CURRENT_VALUE_ONLY)
			.addHeader("rate_after_p_1d", ColumnKind.CURRENT_VALUE_ONLY)
			.addHeader("v_chng_after_p_1d", ColumnKind.CURRENT_VALUE_ONLY)
			.build();

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

	public double getPreviousFreeCashFlow() {
		return Optional.ofNullable(previousFinancialInfo).map(AssetFinancialInfo::freeCashFlow).orElse(0D);
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

	public double getCurrentFreeCashFlow() {
		return Optional.ofNullable(currentFinancialInfo).map(AssetFinancialInfo::freeCashFlow).orElse(0D);
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
		final double previousEpsSurprise = Calculator.calculateEpsSurprise(previousEps.eps, previousPredictedEpsValue);

		final double currentPredictedEpsValue = getCurrentPredictedEps();
		final double currentNonGaapPredictedEpsValue = getCurrentNonGaapPredictedEps();
		final double currentNonGaapEpsValue = getCurrentNonGaapEps();
		final double currentEpsSurprise = Calculator.calculateEpsSurprise(currentEps.eps, currentPredictedEpsValue);

		final Row row = HEADER.newRow();

		row.add("asset_id", assetName);
		row.add("sector", sectorIndex);
		row.add("industry", industryIndex);
		row.add("eps_date", DatesAndSetUtil.dateToStr(currentEps.date));
		row.add("month", getMonthIndex());

		row.add("after_market_close", getCurrentAfterMarketClose(), getPreviousAfterMarketClose());
		row.add("no_analysts", numberOfAnalystsFrom(currentEps), numberOfAnalystsFrom(previousEps));
		row.add("f_score", currentFScore, previousFScore);
		row.add("eps_spr", currentEpsSurprise, previousEpsSurprise);
		row.add("ngaap_eps_spr",
				Calculator.calculateEpsSurprise(currentNonGaapEpsValue, currentNonGaapPredictedEpsValue),
				Calculator.calculateEpsSurprise(previousNonGaapEpsValue, previousNonGaapPredictedEpsValue));

		row.add("revenue_spr", revenueSurprise(currentNonGaapEps, currentEpsSurprise),
				revenueSurprise(previousNonGaapEps, previousEpsSurprise));
		row.add("revenue_ch_r", revenueChange(currentNonGaapEps, previousNonGaapEps));

		row.add("pred_eps", currentPredictedEpsValue, previousPredictedEpsValue, true);
		row.add("eps", currentEps.eps, previousEps.eps, true);
		row.add("ngaap_pred_eps", currentNonGaapPredictedEpsValue, previousNonGaapPredictedEpsValue, true);
		row.add("ngaap_eps", currentNonGaapEpsValue, previousNonGaapEpsValue, true);

		row.add("p_e", getCurrentPOverE(), getPreviousPOverE(), true);
		row.add("p_b", getCurrentPOverB(), getPreviousPOverB(), true);
		row.add("div_yld", getCurrentDividendYield(), getPreviousDividendYield(), true);
		row.add("cu_ratio", getCurrentCurrentRatio(), getPreviousCurrentRatio(), true);
		row.add("to_ratio", getCurrentTotalRatio(), getPreviousTotalRatio(), true);
		row.add("d_e_calc", getCurrentDebtOverEquityCalculated(), getPreviousDebtOverEquityCalculated(), true);
		row.add("d_e_rep", getCurrentDebtOverEquityReported(), getPreviousDebtOverEquityReported(), true);

		row.add("fcf_ps", getCurrentFreeCashFlowPerShare(), getPreviousFreeCashFlowPerShare(), true);
		row.add("fcf_ch_r", Calculator.calculateEpsSurprise(getCurrentFreeCashFlow(), getPreviousFreeCashFlow()));

		row.add("rate_before_m_1d", rateIfAvailable(getInfoBeforeMinusOneDayEpsAnnouncement()));
		row.add("v_chng_before_m_1d", volumeChangeRateIfAvailable(getInfoBeforeMinusOneDayEpsAnnouncement()));
		row.add("rate_before", rateIfAvailable(getInfoBeforeEpsAnnouncement()));
		row.add("v_chng_before", volumeChangeRateIfAvailable(getInfoBeforeEpsAnnouncement()));
		row.add("rate_after", rateIfAvailable(getInfoAfterEpsAnnouncement()));
		row.add("v_chng_after", volumeChangeRateIfAvailable(getInfoAfterEpsAnnouncement()));
		row.add("rate_after_p_1d", rateIfAvailable(getInfoAfterPlusOneDayEpsAnnouncement()));
		row.add("v_chng_after_p_1d", volumeChangeRateIfAvailable(getInfoAfterPlusOneDayEpsAnnouncement()));

		return row.toCsvLine();
	}

	private static String rateIfAvailable(AssetPriceInfo priceInfo) {
		if (priceInfo == null) {
			return "";
		}

		return HistoricalRowGenerator.toStringValue(priceInfo.rate);
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

		return HistoricalRowGenerator.toStringValue(priceInfo.volumeChangeRate);
	}

	private static int numberOfAnalystsFrom(AssetEpsInfo eps) {
		final Integer noOfAnalysts = eps.noOfAnalysts;
		final Double predictedEps = eps.epsPredicted;

		if (noOfAnalysts == null) {
			if (predictedEps != null) {
				LOGGER.info("Number of analysts is null for '{}' on '{}', assuming 1.",
						eps.assetName,
						DatesAndSetUtil.dateToStr(eps.date));
			}
			return 1;
		}

		return noOfAnalysts;
	}

	private static Double revenueFrom(AssetNonGaapEpsInfo nonGaapEps) {
		if (nonGaapEps == null || nonGaapEps.revenue == null) {
			return null;
		}

		return nonGaapEps.revenue;
	}

	private static double revenueSurprise(AssetNonGaapEpsInfo nonGaapEps, double defaultValue) {
		final Double revenue = revenueFrom(nonGaapEps);

		if (revenue == null || nonGaapEps.revenuePredicted == null) {
			return defaultValue;
		}

		return Calculator.calculateEpsSurprise(revenue, nonGaapEps.revenuePredicted);
	}

	private static double revenueChange(AssetNonGaapEpsInfo currentNonGaapEps,
			AssetNonGaapEpsInfo previoussNonGaapEps) {
		final Double currentRevenue = revenueFrom(currentNonGaapEps);
		final Double previousRevenue = revenueFrom(previoussNonGaapEps);

		if (currentRevenue == null || previousRevenue == null) {
			return 0D;
		}

		return Calculator.calculateEpsSurprise(currentRevenue, previousRevenue);
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
}
