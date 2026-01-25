<?php
// Show financial score details script
	include_once("../lib/mysql.php");
	include_once("../lib/utils.php");
	include_once("./funcs.php");
	header("Content-Type:text/html; charset=UTF-8");

function loadFinancialData($assetSymbol, $link) {
	$query = "SELECT dtm_date,";
	$query.= "  dbl_total_current_assets, dbl_total_current_liabilities,";
	$query.= "  dbl_total_assets, dbl_total_liabilities,";
	$query.= "  dbl_total_equity, dbl_net_cash_flow_operating,";
	$query.= "  dbl_capital_expenditures, dbl_share_issued,";
	$query.= "  dbl_total_current_assets / dbl_total_current_liabilities,";
	$query.= "  dbl_total_assets / dbl_total_liabilities,";
	$query.= "  dbl_total_liabilities / (dbl_total_assets - dbl_total_liabilities),";
	$query.= "  IF(dbl_total_equity is null, dbl_total_liabilities / (dbl_total_assets - dbl_total_liabilities), dbl_total_liabilities / dbl_total_equity),";
	$query.= "  IF(dbl_capital_expenditures is NULL, dbl_net_cash_flow_operating, dbl_net_cash_flow_operating - ABS(dbl_capital_expenditures)),";
	$query.= "  IF(dbl_capital_expenditures is NULL, (dbl_net_cash_flow_operating * 1000) / dbl_share_issued, ((dbl_net_cash_flow_operating - ABS(dbl_capital_expenditures)) * 1000) / dbl_share_issued)";
	$query.= " FROM tbl_finances_quarter";
	$query.= " WHERE vchr_symbol='$assetSymbol'";
	$query.= " ORDER BY dtm_date ASC";

	$res = mysql_query($query, $link);
	if (!$res) die("Invalid query: ". mysql_error());

	$financialData = array();
	while ($row = mysql_fetch_array($res)) {
		$financialData[$row[0]] = array();

		$financialData[$row[0]]["total_current_assets"] = $row[1];
		$financialData[$row[0]]["total_current_liabilities"] = $row[2];
		$financialData[$row[0]]["total_assets"] = $row[3];
		$financialData[$row[0]]["total_liabilities"] = $row[4];
		$financialData[$row[0]]["total_equity"] = $row[5];
		$financialData[$row[0]]["net_cash_flow_operating"] = $row[6];
		$financialData[$row[0]]["capital_expenditures"] = $row[7];
		$financialData[$row[0]]["share_issued"] = $row[8];
		$financialData[$row[0]]["current_ratio"] = $row[9];
		$financialData[$row[0]]["total_ratio"] = $row[10];
		$financialData[$row[0]]["d_e_calc"] = $row[11];
		$financialData[$row[0]]["d_e_rep"] = $row[12];
		$financialData[$row[0]]["fcf"] = $row[13];
		$financialData[$row[0]]["fcfps"] = $row[14];
	}

	mysql_free_result($res);
	return $financialData;
}

function extractFinancialsFrom($financialData) {
	$tableResult = "";
	$roundPrecision = 2;
	$i = 0;

	foreach ($financialData as $key => $value) {
		if ($i == 0) $tableResult.= "[";
		else $tableResult.= ",[";

		$tableResult.= "'$key',";
		$tableResult.= toChartNumber(roundOrNull($value["total_current_assets"], $roundPrecision)).",";
		$tableResult.= toChartNumber(roundOrNull($value["total_current_liabilities"], $roundPrecision)).",";
		$tableResult.= toChartNumber(roundOrNull($value["total_assets"], $roundPrecision)).",";
		$tableResult.= toChartNumber(roundOrNull($value["total_liabilities"], $roundPrecision)).",";
		$tableResult.= toChartNumber(roundOrNull($value["total_equity"], $roundPrecision)).",";
		$tableResult.= toChartNumber(roundOrNull($value["net_cash_flow_operating"], $roundPrecision)).",";
		$tableResult.= toChartNumber(roundOrNull($value["capital_expenditures"], $roundPrecision)).",";
		$tableResult.= toChartNumber(roundOrNull($value["share_issued"], $roundPrecision))."]";

		$i++;
	}

	return $tableResult;
}

function extractFinancialRatiosFrom($financialData) {
	$tableResult = "";
	$roundPrecision = 2;
	$i = 0;

	foreach ($financialData as $key => $value) {
		if ($i == 0) $tableResult.= "[";
		else $tableResult.= ",[";

		$tableResult.= "'$key',";
		$tableResult.= toChartNumber(roundOrNull($value["current_ratio"], $roundPrecision)).",";
		$tableResult.= toChartNumber(roundOrNull($value["total_ratio"], $roundPrecision)).",";
		$tableResult.= toChartNumber(roundOrNull($value["d_e_calc"], $roundPrecision)).",";
		$tableResult.= toChartNumber(roundOrNull($value["d_e_rep"], $roundPrecision)).",";
		$tableResult.= toChartNumber(roundOrNull($value["fcf"], $roundPrecision)).",";
		$tableResult.= toChartNumber(roundOrNull($value["fcfps"], $roundPrecision))."]";

		$i++;
	}

	return $tableResult;
}


function extractCurrentAndTotalRatiosFrom($financialData) {
	$result = "";
	foreach ($financialData as $key => $value) {
		$result .= ",['".$key."',";
		$result .= valueOrNullFrom($value["current_ratio"]).",";
		$result .= valueOrNullFrom($value["total_ratio"])."]";
	}

	return $result;
}

// DE = Debt over Equity
function extractDERatiosFrom($financialData) {
	$result = "";
	foreach ($financialData as $key => $value) {
		$result .= ",['".$key."',";
		$result .= valueOrNullFrom($value["d_e_calc"]).",";
		$result .= valueOrNullFrom($value["d_e_rep"])."]";
	}

	return $result;
}

// FCFPS = Free Cash Flow per Share
function extractFCFPSRatiosFrom($financialData) {
	$result = "";
	foreach ($financialData as $key => $value) {
		$result .= ",['".$key."',";
		$result .= valueOrNullFrom($value["fcfps"])."]";
	}

	return $result;
}


	$id = (int) $_GET["id"];
	if ($id < 1) $id = 1;

	$link = connect("portfolio");

	$assetRecord = getSingleValyeByPK("tbl_assets", "int_assetID", $id, $link);
	$assetName = $assetRecord["vchr_name"];
	$assetSymbol = $assetRecord["vchr_symbol"];
	$stockDetails = getStockDetails($assetSymbol, $link);

	$financialData = loadFinancialData($assetSymbol, $link);

	$financials = extractFinancialsFrom($financialData);
	$financialRatios = extractFinancialRatiosFrom($financialData);
	$ctRations = extractCurrentAndTotalRatiosFrom($financialData);
	$de = extractDERatiosFrom($financialData);
	$fcfps = extractFCFPSRatiosFrom($financialData);
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
	google.charts.setOnLoadCallback(drawTableAndCharts);

	function drawTableAndCharts() {
		drawTable1();
		drawTable2();

		drawChart1();
		drawChart2();
		drawChart3();
	}

	function drawTable(element, data) {
		data.setProperty(0, 0, 'style', 'width:1000px');
		var table = new google.visualization.Table(document.getElementById(element));
		table.draw(data, {showRowNumber: false, allowHtml: true});
	}

	function drawTable1() {
		<?php if (!empty($financials)) { ?>
		var data = new google.visualization.DataTable();
		data.addColumn('string', 'Report Date');
		data.addColumn('number', 'Tot. Curr. Assets (K)');
		data.addColumn('number', 'Tot. Curr. Liabilities (K)');
		data.addColumn('number', 'Tot. Assets (K)');
		data.addColumn('number', 'Tot. Liabilities (K)');
		data.addColumn('number', 'Tot. Equity (K)');
		data.addColumn('number', 'Net CashFlow Op. (K)');
		data.addColumn('number', 'CapEx (K)');
		data.addColumn('number', 'Share Issued');

		data.addRows([<?php echo $financials; ?>]);
		drawTable('table1_div', data);
		<?php } ?>
	}

	function drawTable2() {
		<?php if (!empty($financialRatios)) { ?>
		var data = new google.visualization.DataTable();
		data.addColumn('string', 'Report Date');
		data.addColumn('number', 'Current Ratio');
		data.addColumn('number', 'Total Ratio');
		data.addColumn('number', 'Debt/Eq. Calculated');
		data.addColumn('number', 'Debt/Eq. Reported');
		data.addColumn('number', 'Free-Cash-Flow (K)');
		data.addColumn('number', 'Free-Cash-Flow/Shares');

		data.addRows([<?php echo $financialRatios; ?>]);
		drawTable('table2_div', data);
		<?php } ?>
	}

	function drawChart1() {
		<?php if (!empty($ctRations)) { ?>
		var data = google.visualization.arrayToDataTable([
			[{label: 'Reported Date', id: 'Reported Date', type: 'string'},
			 {label: 'Current Ratio', id: 'Current Ratio', type: 'number'},
			 {label: 'Total Ratio', id: 'Total Ratio', type: 'number'}]
			<?php echo $ctRations; ?>
		]);

		var options = {
			title: "<?php echo $assetName;?> - Current and Total ratios",
			interpolateNulls: true,
			explorer: {
				actions: ['dragToZoom', 'rightClickToReset'],
				keepInBounds: true
			}
		};
		var chart = new google.visualization.LineChart(document.getElementById('chart1_div'));
		chart.draw(data, options);
		<?php } ?>
	}

	function drawChart2() {
		<?php if (!empty($de)) { ?>
		var data = google.visualization.arrayToDataTable([
			[{label: 'Reported Date', id: 'Reported Date', type: 'string'},
			 {label: 'Debt/Eq. Calculated', id: 'Debt/Eq. Calculated', type: 'number'},
			 {label: 'Debt/Eq. Reported', id: 'Debt/Eq. Reported', type: 'number'}]
			<?php echo $de; ?>
		]);

		var options = {
			title: "<?php echo $assetName;?> - Debt over Equity",
			interpolateNulls: true,
			explorer: {
				actions: ['dragToZoom', 'rightClickToReset'],
				keepInBounds: true
			}
		};
		var chart = new google.visualization.LineChart(document.getElementById('chart2_div'));
		chart.draw(data, options);
		<?php } ?>
	}

	function drawChart3() {
		<?php if (!empty($fcfps)) { ?>
		var data = google.visualization.arrayToDataTable([
			[{label: 'Reported Date', id: 'Reported Date', type: 'string'},
			 {label: 'Free-Cash-Flow/Shares', id: 'Free-Cash-Flow/Shares', type: 'number'}]
			<?php echo $fcfps; ?>
		]);

		var options = {
			title: "<?php echo $assetName;?> - Free Cash Flow per Share",
			interpolateNulls: true,
			explorer: {
				actions: ['dragToZoom', 'rightClickToReset'],
				keepInBounds: true
			}
		};
		var chart = new google.visualization.LineChart(document.getElementById('chart3_div'));
		chart.draw(data, options);
		<?php } ?>
	}
    </script>
  </head>
  <body>
    <table align="center" border="0"><tr>
      <td valign="top"><?php showMenu(); ?></td>
      <td><table align="center" border="0">
<?php showSubMenu($id); ?>
	<tr><td  align="left">
		<font face="verdana">Financial scores: <?php 
			echo linkToAsset($id, $assetName, false);
			linkToYF($assetRecord);
		?></font>
	</td></tr>
<?php showStockDetails($stockDetails); ?>
	<tr><td><hr/></td></tr>
	<tr><td><font face="verdana">Data from financial reports:</font><div id="table1_div" style="width: 1044px;"></div></td></tr>
	<tr><td><font face="verdana">Financial ratios:</font><div id="table2_div" style="width: 1044px;"></div></td></tr>
	<tr><td><font face="verdana">Current and Total ratios:</font><div id="chart1_div" style="width: 1044px; height: 350px;"></div></td></tr>
	<tr><td><font face="verdana">Debt over Equity:</font><div id="chart2_div" style="width: 1044px; height: 350px;"></div></td></tr>
	<tr><td><font face="verdana">Free Cash Flow per Share:</font><div id="chart3_div" style="width: 1044px; height: 350px;"></div></td></tr>
      </table></td>
    </tr></table>
  </body>
</html>
<?php mysql_close($link); ?>