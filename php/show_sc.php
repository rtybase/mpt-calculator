<?php
// Show shift correlation details for two assets script
	include_once("./lib/mysql.php");
	include_once("./lib/utils.php");
	include_once("./lib/funcs.php");
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

	$asset1Id = (int) $_GET["asset1"];
	if ($asset1Id < 1) $asset1Id = 1;

	$asset2Id = (int) $_GET["asset2"];
	if ($asset2Id < 1) $asset2Id = 2;

	$link = connect("portfolio");

	$query = "SELECT txt_json, int_continuous_updates, dtm_last_update_date, ";
	$query.= "dbl_min_rate_forecast, dbl_max_rate_forecast, dtm_last_common_date ";
	$query.= "FROM  tbl_shift_correlations ";
	$query.= "WHERE (fk_predictor_assetID=$asset1Id) AND (fk_predictand_assetID=$asset2Id) ";

	$res = mysqli_query($link, $query);
	if (!$res) die("Invalid query: ". mysqli_error());

	$details = array();
	$continuousUpdates = 0;
	$lastUpdateDate = "";
	$lastCommonDate = "";
	$minRateForecast = NULL;
	$maxRateForecast = NULL;
	while ($row = mysqli_fetch_row($res)) {
		$details = json_decode($row[0], true);
		$continuousUpdates = $row[1];
		$lastUpdateDate = $row[2];
		$minRateForecast = $row[3];
		$maxRateForecast = $row[4];
		$lastCommonDate = $row[5];
	}
	mysqli_free_result($res);

	$shift = $details["bestShift"];
	$asset1Name = getName($asset1Id, $link);
	$asset2Name = getName($asset2Id, $link);

	$tableResult = "['".linkToAsset($asset1Id, $asset1Name)."','";
	$tableResult.= linkToAsset($asset2Id, $asset2Name)."',";
	$tableResult.= toChartNumber($shift).",";
	$tableResult.= toChartNumber(round($details["bestCorrelation"], 5)).",";
	$tableResult.= toChartNumber(count($details["dates"])).",";

	$tableResult.= toChartNumber($continuousUpdates).",";
	$tableResult.= "'".$lastUpdateDate."',";
	$tableResult.= "'".$lastCommonDate."']";
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
		drawChart();
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

	function drawChart() {
		drawAsset1Chart();
		drawAsset2Chart();
	}

	function drawAsset1Chart() {
		var data = google.visualization.arrayToDataTable([
			['Date', 'rates', 'common date rates']
<?php
		ratesForDatesWithShift($details["dates"], $details["predictorRates"], $shift, true);
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
		ratesForDatesWithShift($details["dates"], $details["predictandRates"], $shift, false);
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
	if (!empty($minRateForecast) || !empty($maxRateForecast)) {
		$forecastDate = nextDateFrom($lastCommonDate);

		echo "<tr><td><hr></td></tr>";
		echo "<tr><td><font face=\"verdana\">Forecast return/price for $asset2Name on $forecastDate (more <a href=\"./all_sc.php?id=$asset2Id\">here...</a>):</font></td></tr>";

		$lastPriceInfo = getLastPriceInfo($asset2Id, $link);
		$lastPrice = (float) $lastPriceInfo["dbl_price"];

		if (!empty($minRateForecast)) {
			$price1 = nextPriceFrom($lastPrice, $minRateForecast);
			echo "<tr><td><font face=\"verdana\">Return = ".round($minRateForecast, 4).", Price = ".round($price1, 4)."</font></td></tr>";
		}

		if (!empty($maxRateForecast)) {
			$price2 = nextPriceFrom($lastPrice, $maxRateForecast);
			echo "<tr><td><font face=\"verdana\">Return = ".round($maxRateForecast, 4).", Price = ".round($price2, 4)."</font></td></tr>";
		}
	}
?>
      </table></td>
    </tr></table>
  </body>
</html>
<?php mysqli_close($link); ?>