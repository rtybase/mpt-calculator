<?php
// Show shift correlation details for two assets script
	include_once("./lib/mysql.php");
	include_once("./lib/utils.php");
	include_once("./lib/funcs.php");
	include_once("./lib/stats_f.php");
	header("Content-Type:text/html; charset=UTF-8");

function ratesForDatesWithShift($dates, $rates, $shift, $predictor) {
	$absShift = abs($shift);
	$size = min(count($dates), count($rates));

	if ($predictor) {
		for ($i = 0; $i < $size; $i++) {
			if ($i < $size - $absShift) {
				echo ",['".$dates[$i]."',".$rates[$i].",".$rates[$i]."]";
			} else {
				echo ",['".$dates[$i]."',".$rates[$i].",null]";
			}
		}
	} else {
		for ($i = 0; $i < $size; $i++) {
			if ($i >= $absShift) {
				echo ",['".$dates[$i]."',".$rates[$i].",".$rates[$i]."]";
			} else {
				echo ",['".$dates[$i]."',".$rates[$i].",null]";
			}
		}
	}
}

function shiftCorrelationFor($asset1, $asset2, $link) {
	$query = "SELECT txt_json, int_continuous_updates, dtm_last_update_date, ";
	$query.= "dbl_min_rate_forecast, dbl_max_rate_forecast, dtm_last_common_date ";
	$query.= "FROM  tbl_shift_correlations ";
	$query.= "WHERE (fk_predictor_assetID=$asset1) AND (fk_predictand_assetID=$asset2) ";

	$res = mysqli_query($link, $query);
	if (!$res) die("Invalid query: ". mysqli_error());

	$correlationDetails = array();
	while ($row = mysqli_fetch_row($res)) {
		$correlationDetails["details"] = json_decode($row[0], true);
		$correlationDetails["continuousUpdates"] = $row[1];
		$correlationDetails["lastUpdateDate"] = $row[2];
		$correlationDetails["minRateForecast"] = $row[3];
		$correlationDetails["maxRateForecast"] = $row[4];
		$correlationDetails["lastCommonDate"] = $row[5];
	}
	mysqli_free_result($res);

	return $correlationDetails;
}

function shiftCorrelationHistoryFor($asset1, $asset2, $link) {
	$query = "SELECT a.dtm_forecast_date, a.dbl_correlation, ";
	$query.= "a.dbl_min_rate_forecast, a.dbl_max_rate_forecast, b.dbl_return, ";
	$query.= "c.dbl_price * (1 + a.dbl_min_rate_forecast/100) as min_price_forecast, ";
	$query.= "c.dbl_price * (1 + a.dbl_max_rate_forecast/100) as max_price_forecast, ";
	$query.= "b.dbl_price ";
	$query.= "FROM tbl_shift_correlations_history a ";
	$query.= "LEFT JOIN tbl_prices b ON a.fk_predictand_assetID=b.fk_assetID AND a.dtm_forecast_date=b.dtm_date ";
	$query.= "LEFT JOIN tbl_prices c ON a.fk_predictand_assetID=c.fk_assetID AND a.dtm_last_common_date=c.dtm_date ";
	$query.= "WHERE a.fk_predictor_assetID=$asset1 AND a.fk_predictand_assetID=$asset2 ";
	$query.= "ORDER by a.dtm_forecast_date ASC";

	$res = mysqli_query($link, $query);
	if (!$res) die("Invalid query: ". mysqli_error());

	$correlationDetails = array();
	$i = 0;
	while ($row = mysqli_fetch_row($res)) {
		$correlationDetails[$i] = array();
		$correlationDetails[$i]["forecast_date"] = $row[0];
		$correlationDetails[$i]["correlation"] = $row[1];

		$correlationDetails[$i]["forecast_min_rate"] = $row[2];
		$correlationDetails[$i]["forecast_max_rate"] = $row[3];
		$correlationDetails[$i]["actual_rate"] = $row[4];

		$correlationDetails[$i]["forecast_min_price"] = $row[5];
		$correlationDetails[$i]["forecast_max_price"] = $row[6];
		$correlationDetails[$i]["actual_price"] = $row[7];
		$i++;
	}
	mysqli_free_result($res);

	return $correlationDetails;
}

function extractValues($shiftCorrelationHistory, ...$columns) {
	for ($i = 0; $i < count($shiftCorrelationHistory); $i++) {
		echo ",['".$shiftCorrelationHistory[$i]["forecast_date"]."'";
		foreach ($columns as $column) {
			echo ",".valueOrNullFrom($shiftCorrelationHistory[$i][$column]);
		}
		echo "]";
	}
}

function statsForSingleColumn($shiftCorrelationHistory, $column) {
	$data_array = array();
	$j = 0;
	for ($i = 0; $i < count($shiftCorrelationHistory); $i++) {
		$data_array[$j++] = $shiftCorrelationHistory[$i][$column];
	}

	return arrayStats($data_array);
}

function statsForTwoColumns($shiftCorrelationHistory, $column1, $column2) {
	$data1_array = array();
	$data2_array = array();
	$j = 0;
	for ($i = 0; $i < count($shiftCorrelationHistory); $i++) {
		$val1 = $shiftCorrelationHistory[$i][$column1];
		$val2 = $shiftCorrelationHistory[$i][$column2];

		if (is_numeric($val1) && is_numeric($val2)) {
			$data1_array[$j] = $val1;
			$data2_array[$j] = $val2;
			$j++;
		}
	}

	return arraysStats($data1_array, $data2_array);	
}

	$asset1Id = (int) $_GET["asset1"];
	if ($asset1Id < 1) $asset1Id = 1;

	$asset2Id = (int) $_GET["asset2"];
	if ($asset2Id < 1) $asset2Id = 2;

	$link = connect("portfolio");

	$shiftCorrelation = shiftCorrelationFor($asset1Id, $asset2Id, $link);
	$shiftCorrelationHistory = shiftCorrelationHistoryFor($asset1Id, $asset2Id, $link);

	$shift = $shiftCorrelation["details"]["bestShift"];
	$asset1Name = getName($asset1Id, $link);
	$asset2Name = getName($asset2Id, $link);

	$tableResult = "['".linkToAsset($asset1Id, $asset1Name)."','";
	$tableResult.= linkToAsset($asset2Id, $asset2Name)."',";
	$tableResult.= toChartNumber($shift).",";
	$tableResult.= toChartNumber(round($shiftCorrelation["details"]["bestCorrelation"], 5)).",";
	$tableResult.= toChartNumber(count($shiftCorrelation["details"]["dates"])).",";

	$tableResult.= toChartNumber($shiftCorrelation["continuousUpdates"]).",";
	$tableResult.= "'".$shiftCorrelation["lastUpdateDate"]."',";
	$tableResult.= "'".$shiftCorrelation["lastCommonDate"]."']";
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
	google.charts.load('current', {'packages':['table','corechart']});
	google.charts.setOnLoadCallback(generateTable);

	function generateTable() {
		var data = generateData();
		data.addRows([<?php echo $tableResult; ?>]);
		drawTable('table_div', data);
		drawChart1();
	}

	function drawTable(element, data) {
		data.setProperty(0, 0, 'style', 'width:330px');
		var table = new google.visualization.Table(document.getElementById(element));
		table.draw(data, {showRowNumber: false, allowHtml: true});
	}

	function generateData() {
		var dataTable = new google.visualization.DataTable();
		dataTable.addColumn('string', 'Predictor');
		dataTable.addColumn('string', 'Predictand');
		dataTable.addColumn('number', 'Shift (days)');
		dataTable.addColumn('number', 'Correlation');
		dataTable.addColumn('number', 'Cmn Dates');
		dataTable.addColumn('number', 'Cont Updates');
		dataTable.addColumn('string', 'Last Update');
		dataTable.addColumn('string', 'Last Cmn Date');
		return dataTable;
	}

	function drawChart1() {
		drawAsset1Chart();
		drawAsset2Chart();
		drawAsset3Chart();
		drawAsset4Chart();
		drawAsset5Chart();
	}

	function drawAsset1Chart() {
		var data = google.visualization.arrayToDataTable([
			['Date', 'rates', 'common date rates']
<?php
		ratesForDatesWithShift($shiftCorrelation["details"]["dates"], $shiftCorrelation["details"]["predictorRates"], $shift, true);
?>
		]);

		var options = {
			title: "<?php echo $asset1Name;?>",
			explorer: {
				actions: ['dragToZoom', 'rightClickToReset'],
				keepInBounds: true
			}
		};

		var chart = new google.visualization.LineChart(document.getElementById('chart1_div'));
		chart.draw(data, options);
	}

	function drawAsset2Chart() {
		var data = google.visualization.arrayToDataTable([
			['Date', 'rates', 'common date rates']
<?php
		ratesForDatesWithShift($shiftCorrelation["details"]["dates"], $shiftCorrelation["details"]["predictandRates"], $shift, false);
?>
		]);

		var options = {
			title: "<?php echo $asset2Name;?>",
			explorer: {
				actions: ['dragToZoom', 'rightClickToReset'],
				keepInBounds: true
			}
		};

		var chart = new google.visualization.LineChart(document.getElementById('chart2_div'));
		chart.draw(data, options);
	}

	function drawAsset3Chart() {
<?php if (!empty($shiftCorrelationHistory)) { ?>
		var data = google.visualization.arrayToDataTable([
			['Forecast Date', 'Correlation']
<?php
	extractValues($shiftCorrelationHistory, "correlation");
?>
		]);

		var options = {
			title: "Correlation history",
			explorer: {
				actions: ['dragToZoom', 'rightClickToReset'],
				keepInBounds: true
			}
		};

		var chart = new google.visualization.LineChart(document.getElementById('chart3_div'));
		chart.draw(data, options);
<?php } ?>
	}

	function drawAsset4Chart() {
<?php if (!empty($shiftCorrelationHistory)) { ?>
		var data = google.visualization.arrayToDataTable([
			['Forecast Date', 'Min rate', 'Max rate', 'Actual rate']
<?php
	extractValues($shiftCorrelationHistory, "forecast_min_rate", "forecast_max_rate", "actual_rate");
?>
		]);

		var options = {
			title: "Rates/returns forecast",
			explorer: {
				actions: ['dragToZoom', 'rightClickToReset'],
				keepInBounds: true
			}
		};

		var chart = new google.visualization.LineChart(document.getElementById('chart4_div'));
		chart.draw(data, options);
<?php } ?>
	}

	function drawAsset5Chart() {
<?php if (!empty($shiftCorrelationHistory)) { ?>
		var data = google.visualization.arrayToDataTable([
			['Forecast Date', 'Min price', 'Max price', 'Actual price']
<?php
	extractValues($shiftCorrelationHistory, "forecast_min_price", "forecast_max_price", "actual_price");
?>
		]);

		var options = {
			title: "Prices forecast",
			explorer: {
				actions: ['dragToZoom', 'rightClickToReset'],
				keepInBounds: true
			}
		};

		var chart = new google.visualization.LineChart(document.getElementById('chart5_div'));
		chart.draw(data, options);
<?php } ?>
	}

    </script>
  </head>
  <body>
    <table align="center" border="0"><tr>
      <td valign="top"><?php showMenu(); ?></td>
      <td><table align="center" border="0">
	<tr><td align="left">
		<font face="verdana">Shift correlation details:</font>
	</td></tr>
	<tr><td><hr/></td></tr>
	<tr><td><div id='table_div' style="width: 1044px;"></div></td></tr>
	<tr><td><div id="chart1_div" style="width: 1044px; height: 350px;"></div></td></tr>
	<tr><td><div id="chart2_div" style="width: 1044px; height: 350px;"></div></td></tr>
<?php
	if (!empty($shiftCorrelation["minRateForecast"]) || !empty($shiftCorrelation["maxRateForecast"])) {
		$forecastDate = nextDateFrom($shiftCorrelation["lastCommonDate"]);

		echo "<tr><td><hr></td></tr>";
		echo "<tr><td><font face=\"verdana\">Forecast return/price for $asset2Name on $forecastDate (more <a href=\"./all_sc.php?id=$asset2Id\">here...</a>):</font></td></tr>";

		$lastPriceInfo = getLastPriceInfo($asset2Id, $link);
		$lastPrice = (float) $lastPriceInfo["dbl_price"];

		if (!empty($shiftCorrelation["minRateForecast"])) {
			$price1 = nextPriceFrom($lastPrice, $shiftCorrelation["minRateForecast"]);
			echo "<tr><td><font face=\"verdana\">Return = ".round($shiftCorrelation["minRateForecast"], 4).", Price = ".round($price1, 4)."</font></td></tr>";
		}

		if (!empty($shiftCorrelation["maxRateForecast"])) {
			$price2 = nextPriceFrom($lastPrice, $shiftCorrelation["maxRateForecast"]);
			echo "<tr><td><font face=\"verdana\">Return = ".round($shiftCorrelation["maxRateForecast"], 4).", Price = ".round($price2, 4)."</font></td></tr>";
		}
	}
?>
	<tr><td><div id="chart3_div" style="width: 1044px; height: 350px;"></div></td></tr>
<?php 
	if (!empty($shiftCorrelationHistory)) {
		$stats = statsForSingleColumn($shiftCorrelationHistory, "correlation");
		echo "<tr><td><font face=\"verdana\">Mean = ".round($stats["mean"], 4).", StdDev = ".round($stats["std_dev"], 6)."</font></td></tr>";
		echo "<tr><td><hr></td></tr>";
	}
?>
	<tr><td><div id="chart4_div" style="width: 1044px; height: 350px;"></div></td></tr>
<?php 
	if (!empty($shiftCorrelationHistory)) {
		$min_col_stats = statsForTwoColumns($shiftCorrelationHistory, "forecast_min_rate", "actual_rate");
		$max_col_stats = statsForTwoColumns($shiftCorrelationHistory, "forecast_max_rate", "actual_rate");

		echo "<tr><td><font face=\"verdana\">Min Rate Correlation = ".round($min_col_stats["correlation"], 4).", Abs Error Mean = ".round($min_col_stats["err_mean"], 6).", Abs Error StdDev = ".round($min_col_stats["err_std_dev"], 6)."</font></td></tr>";
		echo "<tr><td><font face=\"verdana\">Max Rate Correlation = ".round($max_col_stats["correlation"], 4).", Abs Error Mean = ".round($max_col_stats["err_mean"], 6).", Abs Error StdDev = ".round($max_col_stats["err_std_dev"], 6)."</font></td></tr>";
		echo "<tr><td><hr></td></tr>";
	}
?>
	<tr><td><div id="chart5_div" style="width: 1044px; height: 350px;"></div></td></tr>
<?php 
	if (!empty($shiftCorrelationHistory)) {
		$min_col_stats = statsForTwoColumns($shiftCorrelationHistory, "forecast_min_price", "actual_price");
		$max_col_stats = statsForTwoColumns($shiftCorrelationHistory, "forecast_max_price", "actual_price");

		echo "<tr><td><font face=\"verdana\">Min Price Correlation = ".round($min_col_stats["correlation"], 4).", Abs Error Mean = ".round($min_col_stats["err_mean"], 6).", Abs Error StdDev = ".round($min_col_stats["err_std_dev"], 6)."</font></td></tr>";
		echo "<tr><td><font face=\"verdana\">Max Price Correlation = ".round($max_col_stats["correlation"], 4).", Abs Error Mean = ".round($max_col_stats["err_mean"], 6).", Abs Error StdDev = ".round($max_col_stats["err_std_dev"], 6)."</font></td></tr>";
		echo "<tr><td><hr></td></tr>";
	}
?>
      </table></td>
    </tr></table>
  </body>
</html>
<?php mysqli_close($link); ?>