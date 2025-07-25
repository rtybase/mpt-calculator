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

	$query = "SELECT txt_json FROM  tbl_shift_correlations ";
	$query.= "WHERE (fk_asset1ID=$asset1Id) AND (fk_asset2ID=$asset2Id) ";

	$tableResult = "";
	$res = mysql_query($query, $link);
	if (!$res) die("Invalid query: ". mysql_error());

	$details = array();
	while ($row = mysql_fetch_row($res)) {
		$details = json_decode($row[0], true);
	}
	mysql_free_result($res);

	$shift = $details["bestShift"];
	$asset1Name = getName($asset1Id, $link);
	$asset2Name = getName($asset2Id, $link);

	$asset1Predictor = false;
	$asset2Predictor = false;

	if ($shift > 0) {
		$asset1Name.= " (predictor)";
		$asset1Predictor = true;
	} else if ($shift < 0) {
		$asset2Name.= " (predictor)";
		$asset2Predictor = true;
	}

	$tableResult.= "['".linkToAsset($asset1Id, $asset1Name)."','";
	$tableResult.= linkToAsset($asset2Id, $asset2Name)."',";
	$tableResult.= toChartNumber($shift).",";
	$tableResult.= toChartNumber(round($details["bestCorrelation"], 5))."]";
?>
<html>
  <head>
    <meta charset="UTF-8">
    <link href="https://developers.google.com/fusiontables/docs/samples/style/default.css" rel="stylesheet" type="text/css">
    <style>
	a:link, a:visited, a:active { color:#000000; text-decoration: none; }
	a:hover { color:#000000; text-decoration: underline; }
    </style>

    <script type='text/javascript' src='https://www.google.com/jsapi'></script>
    <script type='text/javascript'>
	google.load('visualization', '1.1', {packages:['table']});
	google.setOnLoadCallback(generateTable);

	google.load("visualization", "1.1", {packages:["corechart"]});
	google.setOnLoadCallback(drawChart);

	function generateTable() {
		var data = generateData();
		data.addRows([<?php echo $tableResult; ?>]);
		drawTable('table_div', data);
	}

	function drawTable(element, data) {
		data.setProperty(0, 0, 'style', 'width:345px');
		var table = new google.visualization.Table(document.getElementById(element));
		table.draw(data, {showRowNumber: false, allowHtml: true});
	}

	function generateData() {
		var dataTable = new google.visualization.DataTable();
		dataTable.addColumn('string', 'Asset 1');
		dataTable.addColumn('string', 'Asset 2');
		dataTable.addColumn('number', 'Shift (days)');
		dataTable.addColumn('number', 'Correlation');
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
			title: "<?php echo $asset1Name;?>"
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
			title: "<?php echo $asset2Name;?>"
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
      </table></td>
    </tr></table>
  </body>
</html>
<?php mysql_close($link); ?>