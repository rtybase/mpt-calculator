<?php
// Show predictions from shift correlations script
	include_once("../lib/mysql.php");
	include_once("../lib/utils.php");
	include_once("./funcs.php");
	header("Content-Type:text/html; charset=UTF-8");

function nextDateFromDetails($details) {
	$lastDate = end($details["dates"]);
	return nextDateFrom($lastDate);
}

function get1DayShiftCorrelations($assetId, $link) {
	$query = "SELECT fk_asset1ID, fk_asset2ID, int_shift, dbl_correlation, txt_json ";
	$query.= "FROM tbl_shift_correlations USE INDEX (PRIMARY,fk_asset2ID) ";
	$query.= "WHERE ((fk_asset1ID=$assetId AND int_shift=-1) OR ";
	$query.= "(fk_asset2ID=$assetId AND int_shift=1)) ";
	$query.= "AND ABS(dbl_correlation) > 0.0 ";
	$query.= "ORDER BY ABS(dbl_correlation) DESC";

	$res = mysql_query($query, $link);
	if (!$res) die("Invalid query: ". mysql_error());

	$oneDayShiftCorrelations = array();
	while ($row = mysql_fetch_array($res)) {
		$pAssetId = $row[0];

		if ($row[0] == $assetId) {
			$pAssetId = $row[1];
		}

		$details = json_decode($row[4], true);
		if (!empty($details["forecast"])) {
			$oneDayShiftCorrelations[$pAssetId] = array();
			$oneDayShiftCorrelations[$pAssetId]["assetName"] = getName($pAssetId, $link);
			$oneDayShiftCorrelations[$pAssetId]["shift"] = $row[2];
			$oneDayShiftCorrelations[$pAssetId]["correlation"] = $row[3];
			$oneDayShiftCorrelations[$pAssetId]["common_dates"] = count($details["dates"]);
			$oneDayShiftCorrelations[$pAssetId]["forecast_date"] = nextDateFromDetails($details);
			$oneDayShiftCorrelations[$pAssetId]["rate1"] = $details["forecast"][0];
			$oneDayShiftCorrelations[$pAssetId]["rate2"] = $details["forecast"][1];
		}
	}
	mysql_free_result($res);

	return $oneDayShiftCorrelations;
}

function rateAndPrice($lastPrice, $rate, $isForex) {
	global $RETURN_ROUND_PRECISION, $VOLATILITY_ROUND_PRECISION;

	$price = nextPriceFrom($lastPrice, $rate);
	$result = toChartNumber(roundOrNull($rate, $RETURN_ROUND_PRECISION)).",";
	$result.= toChartNumber(roundOrNull($price, $VOLATILITY_ROUND_PRECISION));

	if ($isForex) {
		$result.= ",".toChartNumber(roundOrNull(1 / $price, $VOLATILITY_ROUND_PRECISION));
	}

	return $result;
}

function mergeDataToTableFormat($oneDayShiftCorrelations, $lastPriceInfo, $isForex) {
	global $VOLATILITY_ROUND_PRECISION;

	$lastPrice = (float) $lastPriceInfo["dbl_price"];
	$result = "";

	$i = 0;
	foreach ($oneDayShiftCorrelations as $key => $value) {
		if ($i == 0) $result .= "[";
		else $result .= ",[";

		$result .= "'".linkToAsset($key, $value["assetName"])."',";
		$result .= toChartNumber($value["shift"]).",";
		$result .= toChartNumber(roundOrNull($value["correlation"], $VOLATILITY_ROUND_PRECISION)).",";
		$result .= toChartNumber($value["common_dates"]).",";
		$result .= "'".$value["forecast_date"]."',";

		$rate1 = $value["rate1"];
		$rate2 = $value["rate2"];

		if (abs($rate1) <= abs($rate2)) {
			$result .= rateAndPrice($lastPrice, $rate1, $isForex).",";
			$result .= rateAndPrice($lastPrice, $rate2, $isForex)."]";
		} else {
			$result .= rateAndPrice($lastPrice, $rate2, $isForex).",";
			$result .= rateAndPrice($lastPrice, $rate1, $isForex)."]";
		}
  
		$i++;
	}

	return $result;
}

	$id = (int) $_GET["id"];
	if ($id < 1) $id = 1;

	$link = connect("portfolio");

	$assetRecord = getSingleValyeByPK("tbl_assets", "int_assetID", $id, $link);
	$assetName = $assetRecord["vchr_name"];
	$assetSymbol = $assetRecord["vchr_symbol"];
	$isForex = $assetRecord["vchr_type"] == "Forex";
	$stockDetails = getStockDetails($assetSymbol, $link);

	$oneDayShiftCorrelations = get1DayShiftCorrelations($id, $link);
	$lastPriceInfo = getLastPriceInfo($id, $link);

	$oneDayPredictions = mergeDataToTableFormat($oneDayShiftCorrelations, $lastPriceInfo, $isForex);
?>
<!doctype html>
<html>
  <head>
    <meta charset="UTF-8">
    <style>
	a:link, a:visited, a:active { color:#000000; text-decoration: none; }
	a:hover { color:#000000; text-decoration: underline; }
    </style>

    <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
    <script type='text/javascript'>
	google.charts.load('current', {'packages':['table']});
	google.charts.setOnLoadCallback(drawTableAndCharts);

	function drawTableAndCharts() {
		var data = generateHeader();
		data.addRows([<?php echo $oneDayPredictions; ?>]);
		drawTable('table_div', data);
	}

	function drawTable(element, data) {
		data.setProperty(0, 0, 'style', 'width:1000px');
		var table = new google.visualization.Table(document.getElementById(element));
		table.draw(data, {showRowNumber: false, allowHtml: true});
	}

	function generateHeader() {
		var dataTable = new google.visualization.DataTable();
		dataTable.addColumn('string', 'Predicting Asset');
		dataTable.addColumn('number', 'Shift');
		dataTable.addColumn('number', 'Corrltn');
		dataTable.addColumn('number', 'Cn.Dates');
		dataTable.addColumn('string', 'Frcst Date');
		dataTable.addColumn('number', 'R.min');
		dataTable.addColumn('number', 'P.min');
<?php 
	if ($isForex) {
?>
		dataTable.addColumn('number', '1/P.min');
<?php 
	}
?>
		dataTable.addColumn('number', 'R.max');
		dataTable.addColumn('number', 'P.max');
<?php 
	if ($isForex) {
?>
		dataTable.addColumn('number', '1/P.max');
<?php 
	}
?>

		return dataTable;
	}
    </script>
  </head>
  <body>
    <table align="center" border="0"><tr>
      <td valign="top"><?php showMenu(); ?></td>
      <td><table align="center" border="0">
	<tr><td  align="left">
		<font face="verdana">Predictions from 1 day shift correlations: <?php 
			echo linkToAsset($id, $assetName, false);
			if (!empty($assetSymbol))
			echo " or <a href=\"https://finance.yahoo.com/quote/".$assetSymbol."/\">YF=".$assetSymbol."</a>"; 
		?></font>
	</td></tr>
<?php
	if (!empty($stockDetails)) {
			echo "<tr><td align=\"left\"><font face=\"verdana\">Sector: <i>".$stockDetails[1]."</i></font></td></tr>";
			echo "<tr><td align=\"left\"><font face=\"verdana\">Industry: <i>".$stockDetails[2]."</i></font></td></tr>";
	}
?>
	<tr><td><hr/></td></tr>
	<tr><td><font face="verdana">Predictions:</font><div id="table_div" style="width: 1044px;"></div></td></tr>
      </table></td>
    </tr></table>
  </body>
</html>
<?php mysql_close($link); ?>