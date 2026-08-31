<?php
// Show predictions from shift correlations script
	include_once("./lib/mysql.php");
	include_once("./lib/utils.php");
	include_once("./lib/funcs.php");
	header("Content-Type:text/html; charset=UTF-8");

function get1DayShiftCorrelations($assetId, $link) {
	$query = "SELECT fk_predictor_assetID, int_shift, dbl_correlation, ";
	$query.= "txt_json, int_continuous_updates, dtm_last_update_date, ";
	$query.= "dbl_min_rate_forecast, dbl_max_rate_forecast, dtm_last_common_date ";
	$query.= "FROM tbl_shift_correlations USE INDEX (fk_predictand_assetID) ";
	$query.= "WHERE (fk_predictand_assetID=$assetId AND int_shift=1) ";
	$query.= "AND ABS(dbl_correlation) > 0.0 ";
	$query.= "ORDER BY ABS(dbl_correlation) DESC";

	$res = mysqli_query($link, $query);
	if (!$res) die("Invalid query: ". mysqli_error());

	$oneDayShiftCorrelations = array();
	while ($row = mysqli_fetch_array($res)) {
		$pAssetId = $row[0];
		$details = json_decode($row[3], true);

		$oneDayShiftCorrelations[$pAssetId] = array();
		$oneDayShiftCorrelations[$pAssetId]["assetName"] = getName($pAssetId, $link);
		$oneDayShiftCorrelations[$pAssetId]["shift"] = $row[1];
		$oneDayShiftCorrelations[$pAssetId]["correlation"] = $row[2];
		$oneDayShiftCorrelations[$pAssetId]["common_dates"] = count($details["dates"]);
		$oneDayShiftCorrelations[$pAssetId]["forecast_date"] = nextDateFrom($row[8]);
		$oneDayShiftCorrelations[$pAssetId]["continuousUpdates"] = $row[4];
		$oneDayShiftCorrelations[$pAssetId]["lastUpdateDate"] = $row[5];
		$oneDayShiftCorrelations[$pAssetId]["lastCommonDate"] = $row[8];
		$oneDayShiftCorrelations[$pAssetId]["rate1"] = $row[6];
		$oneDayShiftCorrelations[$pAssetId]["rate2"] = $row[7];
	}
	mysqli_free_result($res);

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

function mergeDataToTableFormat($assetId, $oneDayShiftCorrelations, $lastPriceInfo, $isForex) {
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
		$result .= rateAndPrice($lastPrice, $rate1, $isForex).",";

		if (!empty($value["rate2"])) {
			$rate2 = $value["rate2"];
			$result .= rateAndPrice($lastPrice, $rate2, $isForex).",";
		} else {
			$result .="null,null,";
			if ($isForex) {
				$result .="null,";
			}
		}

		$result .= toChartNumber($value["continuousUpdates"]).",";
		$result .= "'".$value["lastUpdateDate"]."',";
		$result .= "'".$value["lastCommonDate"]."',";
		$result .= "'<a href=\"./show_sc.php?asset1=".$key."&asset2=".$assetId."\">details...</a>']";

		$i++;
	}

	return $result;
}

function getEpsPredictions($assetId, $link) {
	global $RETURN_ROUND_PRECISION;

	$query = "SELECT a.dtm_eps_date, a.int_days_after_eps, a.dtm_prd_date, ";
	$query.= "  a.vchr_model, a.dbl_prd_return, b.dbl_return ";
	$query.= "FROM tbl_predictions a ";
	$query.= "LEFT JOIN tbl_prices b ON a.fk_assetID=b.fk_assetID ";
	$query.= "  AND a.dtm_prd_date=b.dtm_date ";
	$query.= "WHERE  a.fk_assetID=$assetId ";
	$query.= "AND  a.dtm_eps_date BETWEEN (NOW() - INTERVAL 60 DAY) AND NOW() ";
	$query.= "ORDER BY a.dtm_prd_date DESC, a.vchr_model ASC";

	$res = mysqli_query($link, $query);
	if (!$res) die("Invalid query: ". mysqli_error());

	$tableResult = "";
	$i = 0;
	while ($row = mysqli_fetch_row($res)) {
		if ($i == 0) $tableResult.= "[";
		else $tableResult.= ",[";

		$tableResult.= "'".$row[0]."',";
		$tableResult.= toChartNumber($row[1]).",";
		$tableResult.= "'".$row[2]."',";
		$tableResult.= "'<nobr>".$row[3]."</nobr>',";
		$tableResult.= toChartNumber(roundOrNull($row[4], $RETURN_ROUND_PRECISION)).",";
		$tableResult.= toChartNumber(roundOrNull($row[5], $RETURN_ROUND_PRECISION))."]";
		$i++;
	}
	mysqli_free_result($res);
	return $tableResult;
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

	$oneDayPredictions = mergeDataToTableFormat($id, $oneDayShiftCorrelations, $lastPriceInfo, $isForex);
	$epsPredictions = getEpsPredictions($id, $link);
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
	google.charts.setOnLoadCallback(drawTables);

	function drawTables() {
		drawTable1();
		drawTable2();
	}

	function drawTable(element, data) {
		data.setProperty(0, 0, 'style', 'width:550px');
		var table = new google.visualization.Table(document.getElementById(element));
		table.draw(data, {showRowNumber: false, allowHtml: true});
	}

	function drawTable1() {
<?php
	if (!empty($oneDayPredictions)) {
?>
		var data = generateHeader1();
		data.addRows([<?php echo $oneDayPredictions; ?>]);
		drawTable('table_div1', data);
<?php
	}
?>
	}

	function generateHeader1() {
		var dataTable = new google.visualization.DataTable();
		dataTable.addColumn('string', 'Predictor');
		dataTable.addColumn('number', 'Shift');
		dataTable.addColumn('number', 'Corrltn');
		dataTable.addColumn('number', 'Cn.Dates');
		dataTable.addColumn('string', 'Frcst Date');
		dataTable.addColumn('number', 'Rt.min');
		dataTable.addColumn('number', 'Pr.min');
<?php 
	if ($isForex) {
?>
		dataTable.addColumn('number', '1/Pr.min');
<?php 
	}
?>
		dataTable.addColumn('number', 'Rt.max');
		dataTable.addColumn('number', 'Pr.max');
<?php 
	if ($isForex) {
?>
		dataTable.addColumn('number', '1/Pr.max');
<?php 
	}
?>

		dataTable.addColumn('number', 'Cnt.Upds');
		dataTable.addColumn('string', 'Lst.Upd');
		dataTable.addColumn('string', 'Lst.Cmn.D');
		dataTable.addColumn('string', 'More');
		return dataTable;
	}

	function drawTable2() {
<?php
	if (!empty($epsPredictions)) {
?>
		var data = generateHeader2();
		data.addRows([<?php echo $epsPredictions; ?>]);
		drawTable('table_div2', data);
<?php 
	}
?>
	}

	function generateHeader2() {
		var dataTable = new google.visualization.DataTable();
		dataTable.addColumn('string', 'EPS Report Date');
		dataTable.addColumn('number', 'Day(s) After EPS');
		dataTable.addColumn('string', 'Prediction Date (incl. holidays)');
		dataTable.addColumn('string', 'Model');
		dataTable.addColumn('number', 'Predicted Rate');
		dataTable.addColumn('number', 'Actual Rate');
		return dataTable;
	}
    </script>
  </head>
  <body>
    <table align="center" border="0"><tr>
      <td valign="top"><?php showMenu(); ?></td>
      <td><table align="center" border="0">
<?php showSubMenu($id); ?>
	<tr><td  align="left">
		<font face="verdana">Predictions: <?php 
			echo linkToAsset($id, $assetName, false);
			linkToYF($assetRecord);
		?></font>
	</td></tr>
<?php showStockDetails($stockDetails); ?>
	<tr><td><hr/></td></tr>
	<tr><td><font face="verdana">Predictions from 1 day shift correlations:</font><div id="table_div1" style="width: 1044px;"></div></td></tr>
	<tr><td>&nbsp;</td></tr>
	<tr><td><font face="verdana">Predictions from EPS:</font><div id="table_div2" style="width: 1044px;"></div></td></tr>
      </table></td>
    </tr></table>
  </body>
</html>
<?php mysqli_close($link); ?>