package org.rty.portfolio.core;

import static org.rty.portfolio.core.CsvWritable.emptyIfNull;

import java.util.Date;

import org.rty.portfolio.core.utils.DatesAndSetUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AssetFinancialInfo implements CsvWritable, EntryWithAssetNameAndDate {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public final String assetName;
	public final Date date;
	public final Double totalCurrentAssets;
	public final Double totalCurrentLiabilities;
	public final Double totalAssets;
	public final Double totalLiabilities;
	public final Double totalEquity;
	public final Double netCashFlowOperating;
	public final Double capitalExpenditures;
	public final Double shareIssued;

	public AssetFinancialInfo(String assetName, Date date, Double totalCurrentAssets, Double totalCurrentLiabilities,
			Double totalAssets, Double totalLiabilities, Double totalEquity, Double netCashFlowOperating,
			Double capitalExpenditures, Double shareIssued) {
		this.assetName = assetName;
		this.date = date;
		this.totalCurrentAssets = totalCurrentAssets;
		this.totalCurrentLiabilities = totalCurrentLiabilities;
		this.totalAssets = totalAssets;
		this.totalLiabilities = totalLiabilities;
		this.totalEquity = totalEquity;
		this.netCashFlowOperating = netCashFlowOperating;
		this.capitalExpenditures = capitalExpenditures;
		this.shareIssued = shareIssued;
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
		return new String[] { assetName,
				DatesAndSetUtil.dateToStr(date),
				emptyIfNull(totalCurrentAssets),
				emptyIfNull(totalCurrentLiabilities),
				emptyIfNull(totalAssets),
				emptyIfNull(totalLiabilities),
				emptyIfNull(totalEquity),
				emptyIfNull(netCashFlowOperating),
				emptyIfNull(capitalExpenditures),
				emptyIfNull(shareIssued)
			};
	}

	@Override
	public String getAssetName() {
		return assetName;
	}

	@Override
	public Date getDate() {
		return DatesAndSetUtil.toJavaDate(date);
	}
}
