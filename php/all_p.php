<?php
// All pairs script
	include_once("../lib/mysql.php");
	include_once("../lib/utils.php");
	include_once("./funcs.php");
	header("Content-Type:text/html; charset=UTF-8");

function getAllCorrelations($assetId, $assetName, $link) {
	$query = "SELECT C.fk_asset1ID, C.fk_asset2ID, A.vchr_name, C.dbl_correlation, ";
	$query.= "C.dbl_weight1, C.dbl_weight2, C.dbl_portret, C.dbl_portvar ";
	$query.= "FROM  tbl_correlations as C, tbl_assets as A ";
	$query.= "WHERE ((C.fk_asset1ID=$assetId) OR (C.fk_asset2ID=$assetId)) ";
	$query.= "AND C.fk_asset2ID=A.int_assetID ";
	$query.= "ORDER BY C.dbl_portret DESC ";

	return getCollection($query, $assetId, $assetName, $link);
}

function buildRow($id, $parentId, $description) {
	return "[{'v':'$id', 'f':'$description'}, '$parentId', '']";
}

function buildDescription($id, $shift, $correlation, $predictionText, $link) {
	global $RETURN_ROUND_PRECISION;

	$r_corr = round($correlation, $RETURN_ROUND_PRECISION - 1);
	$description = linkToAsset($id, getName($id, $link));
	$description.= "<div style=\"color:red; font-style:italic\"><nobr>$predictionText: ".abs($shift)."d</nobr>,";
	$description.= " <nobr>cr.: $r_corr</nobr></div>";
	return $description;
}

function getAllShiftCorrelationsFor($query, $assetId, $assetName, $predictionText, $link) {
	$res = mysql_query($query, $link);
	if (!$res) die("Invalid query: ". mysql_error());

	$tableResult = buildRow($assetId, "", addslashes($assetName));
	while ($row = mysql_fetch_row($res)) {
		$id = $row[0];

		if ($id == $assetId) {
			$id = $row[1];
		}

		$description = buildDescription($id, $row[2], $row[3], $predictionText, $link);
		$tableResult.= ",".buildRow($id, $assetId, $description);
	}
	mysql_free_result($res);
	return $tableResult;
}

function getAllPredictingShiftCorrelations($assetId, $assetName, $link) {
	$query = "SELECT fk_asset1ID, fk_asset2ID, int_shift, dbl_correlation ";
	$query.= "FROM tbl_shift_correlations USE INDEX (PRIMARY,fk_asset2ID) ";
	$query.= "WHERE ((fk_asset1ID=$assetId AND (int_shift BETWEEN 1 AND 19)) OR ";
	$query.= "(fk_asset2ID=$assetId AND (int_shift BETWEEN -19 AND -1))) ";
	$query.= "AND ABS(dbl_correlation) > 0.15 ";
	$query.= "ORDER BY ABS(dbl_correlation) DESC ";
	$query.= "LIMIT 0, 15";

	return getAllShiftCorrelationsFor($query, $assetId, $assetName, "In", $link);
}

function getAllPredictedByShiftCorrelations($assetId, $assetName, $link) {
	$query = "SELECT fk_asset1ID, fk_asset2ID, int_shift, dbl_correlation ";
	$query.= "FROM tbl_shift_correlations USE INDEX (PRIMARY,fk_asset2ID) ";
	$query.= "WHERE ((fk_asset1ID=$assetId AND (int_shift BETWEEN -19 AND -1)) OR ";
	$query.= "(fk_asset2ID=$assetId AND (int_shift BETWEEN 1 AND 19))) ";
	$query.= "AND ABS(dbl_correlation) > 0.15 ";
	$query.= "ORDER BY ABS(dbl_correlation) DESC ";
	$query.= "LIMIT 0, 15 ";

	return getAllShiftCorrelationsFor($query, $assetId, $assetName, "After", $link);
}

	$id = (int) $_GET["id"];
	if ($id < 1) $id = 1;

	$link = connect("portfolio");

	$assetRecord = getSingleValyeByPK("tbl_assets", "int_assetID", $id, $link);
	$mainAsset = $assetRecord["vchr_name"];
	$assetSymbol = $assetRecord["vchr_symbol"];
	$stockDetails = getStockDetails($assetSymbol, $link);

	$allCorrelations = getAllCorrelations($id, $mainAsset, $link);
	$allPredictingShiftCorrelations = getAllPredictingShiftCorrelations($id, $mainAsset, $link);
	$allPredictedByShiftCorrelations = getAllPredictedByShiftCorrelations($id, $mainAsset, $link);
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
	google.charts.load('current', {'packages':['orgchart', 'table']});
	google.charts.setOnLoadCallback(generateTable);

	function generateTable() {
		var data = generateData();
		data.addRows([<?php showData($allCorrelations); ?>]);
		drawTable('table_div', data);
		drawChart1();
		drawChart2();
	}

	function drawTable(element, data) {
		data.setProperty(0, 0, 'style', 'width:1000px');
		var table = new google.visualization.Table(document.getElementById(element));
		table.draw(data, {showRowNumber: false, allowHtml: true});
	}

	function generateData() {
		var dataTable = new google.visualization.DataTable();
		dataTable.addColumn('string', 'Portfolio');
		dataTable.addColumn('number', 'Correlation');
		dataTable.addColumn('string', 'Weights');
		dataTable.addColumn('number', 'Portfolio Return');
		dataTable.addColumn('number', 'Portfolio Volatility');
		return dataTable;
	}

	function drawChart1() {
		var data = new google.visualization.DataTable();
		data.addColumn('string', 'Asset');
		data.addColumn('string', 'Parent Asset');
		data.addColumn('string', 'ToolTip');

		data.addRows([<?php echo $allPredictingShiftCorrelations; ?>]);

		var chart = new google.visualization.OrgChart(document.getElementById('chart1_div'));
		chart.draw(data, {allowHtml: true, compactRows: true, size: 'small'});
	}
	function drawChart2() {
		var data = new google.visualization.DataTable();
		data.addColumn('string', 'Asset');
		data.addColumn('string', 'Parent Asset');
		data.addColumn('string', 'ToolTip');

		data.addRows([<?php echo $allPredictedByShiftCorrelations; ?>]);

		var chart = new google.visualization.OrgChart(document.getElementById('chart2_div'));
		chart.draw(data, {allowHtml: true, compactRows: true, size: 'small'});
	}
    </script>
  </head>
  <body>
    <table align="center" border="0"><tr>
      <td valign="top"><?php showMenu(); ?></td>
      <td><table align="center" border="0">
<?php showSubMenu($id); ?>
	<tr><td><font face="verdana">All <?php echo count($allCorrelations); ?> correlations for: <?php
	echo linkToAsset($id, $mainAsset, false);
	linkToYF($assetRecord);
?></font></td></tr>
<?php showStockDetails($stockDetails); ?>
	<tr><td><hr/></td></tr>
	<tr><td><font face="verdana">Predicting:</font> <div id="chart1_div" style="width: 1044px;"></div></td></tr>
	<tr><td><font face="verdana">Predicted by (more <a href="./all_sc.php?id=<?php echo $id; ?>">here...</a>):</font> <div id="chart2_div" style="width: 1044px;"></div></td></tr>
	<tr><td><div id='table_div' style="width: 1044px;"></div></td></tr>
      </table></td>
    </tr></table>
  </body>
</html>
<?php mysql_close($link); ?>