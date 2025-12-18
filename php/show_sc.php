<?php
// Show shift correlation details for two assets script
	include_once("../lib/mysql.php");
	include_once("../lib/utils.php");
	include_once("./funcs.php");
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

	$query = "SELECT txt_json, int_continuous_updates, dtm_last_update_date ";
	$query.= "FROM  tbl_shift_correlations ";
	$query.= "WHERE (fk_asset1ID=$asset1Id) AND (fk_asset2ID=$asset2Id) ";

	$res = mysql_query($query, $link);
	if (!$res) die("Invalid query: ". mysql_error());

	$details = array();
	$continuousUpdates = 0;
	$lastUpdateDate = "";
	while ($row = mysql_fetch_row($res)) {
		$details = json_decode($row[0], true);
		$continuousUpdates = $row[1];
		$lastUpdateDate = $row[2];
	}
	mysql_free_result($res);

	$shift = $details["bestShift"];
	$asset1Name = getName($asset1Id, $link);
	$asset2Name = getName($asset2Id, $link);

	$asset1Predictor = false;
	$asset2Predictor = false;

	$predictedAsset = $asset2Name;
	$predictedAssetId = $asset2Id;
	if ($shift > 0) {
		$asset1Name.= " (predictor)";
		$asset1Predictor = true;
	} else if ($shift < 0) {
		$asset2Name.= " (predictor)";
		$asset2Predictor = true;

		$predictedAsset = $asset1Name;
		$predictedAssetId = $asset1Id;
	}

	$tableResult = "['".linkToAsset($asset1Id, $asset1Name)."','";
	$tableResult.= linkToAsset($asset2Id, $asset2Name)."',";
	$tableResult.= toChartNumber($shift).",";
	$tableResult.= toChartNumber(round($details["bestCorrelation"], 5)).",";
	$tableResult.= toChartNumber(count($details["dates"])).",";

	$tableResult.= toChartNumber($continuousUpdates).",";
	$tableResult.= "'".$lastUpdateDate."']";
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
		dataTable.addColumn('string', 'Asset 1');
		dataTable.addColumn('string', 'Asset 2');
		dataTable.addColumn('number', 'Shift (days)');
		dataTable.addColumn('number', 'Correlation');
		dataTable.addColumn('number', 'Cmn Dates');
		dataTable.addColumn('number', 'Cont Updates');
		dataTable.addColumn('string', 'Last Update');
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
		ratesForDatesWithShift($details["dates"], $details["asset1Rates"], $shift, $asset1Predictor);
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
		ratesForDatesWithShift($details["dates"], $details["asset2Rates"], $shift, $asset2Predictor);
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
	if (!empty($details["forecast"])) {
		$lastDate = end($details["dates"]);
		$forecastDate = nextDateFrom($lastDate);

		$lastPriceInfo = getLastPriceInfo($predictedAssetId, $link);
		$return1 = $details["forecast"][0];
		$return2 = $details["forecast"][1];

		$lastPrice = (float) $lastPriceInfo["dbl_price"];
		$price1 = nextPriceFrom($lastPrice, $return1);
		$price2 = nextPriceFrom($lastPrice, $return2);

		echo "<tr><td><hr></td></tr>";
		echo "<tr><td><font face=\"verdana\">Forecast return/price for $predictedAsset on $forecastDate (more <a href=\"./all_sc.php?id=$predictedAssetId\">here...</a>):</font></td></tr>";
		echo "<tr><td><font face=\"verdana\">Return = ".round($return1, 4).", Price = ".round($price1, 4)."</font></td></tr>";
		echo "<tr><td><font face=\"verdana\">Return = ".round($return2, 4).", Price = ".round($price2, 4)."</font></td></tr>";
	}
?>
      </table></td>
    </tr></table>
  </body>
</html>
<?php mysql_close($link); ?>