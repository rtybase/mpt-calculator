<?php
	include_once("../lib/mysql.php");
	include_once("../lib/utils.php");
	include_once("./funcs.php");
	header("Content-Type:text/html; charset=UTF-8");

function getRatesDataFor($assetId, $period, $link) {
	global $RETURN_ROUND_PRECISION;
	$result = array();
	$data = getSingleValyeByPK("tbl_avgreturns".periodTableFrom($period), "fk_assetID", $assetId, $link);

	if (empty($data)) {
		$result["expectedReturn"] = "N/A";
		$result["volatility"] = "N/A";
		$result["kelly"] = "N/A";
	} else {
		$variance = $data["dbl_varience"];
		$expectedReturn = $data["dbl_avgreturn"];

		$result["expectedReturn"] = indicatorText(round($expectedReturn, $RETURN_ROUND_PRECISION), $expectedReturn);
		$result["volatility"] = volatilityFrom($variance);

		if ($variance > 0.0) {
			$result["kelly"] = calculateKellyFraction($expectedReturn, $variance);
		} else {
			$result["kelly"] = "N/A";
		}
	}
	return $result;
}

function loadDividendsAndEpsFor($assetId, $link) {
	$divsAndEps = mergeDivsAndEps($assetId, $link);

	$result = "";
	foreach ($divsAndEps as $key => $value) {
		$result .= ",['".$key."',";
		$result .= valueOrNullFrom($value["dividend"]).",";
		$result .= valueOrNullFrom($value["eps"]).",";
		$result .= valueOrNullFrom($value["eps_predicted"]).",";
		$result .= valueOrNullFrom($value["eps_eofp"])."]";
	}

	return $result;
}

	$id = (int) $_GET["id"];
	if ($id < 1) $id = 1;

	$includeall = ($_GET["all"]==="true")? true: false;

	$link = connect("portfolio");

	$mainAsset = getName($id, $link);
	$lastPriceInfo = getLastPriceInfo($id, $link);

	$constraint = "ABS(C.dbl_correlation) >= 0.75";
	if (!$includeall) $constraint .= " and dbl_weight1 > 0 and dbl_weight2 > 0";
	$hiCorrelation  = getCollectionFor($constraint, "ABS(C.dbl_correlation) DESC", $id, $mainAsset, $link);

	$constraint = "ABS(C.dbl_correlation) <= 0.2";
	if (!$includeall) $constraint .= " and dbl_weight1 > 0 and dbl_weight2 > 0";
	$lowCorrelation = getCollectionFor($constraint, "ABS(C.dbl_correlation) ASC", $id, $mainAsset, $link);

	$constraint = "";
	if (!$includeall) $constraint = "dbl_weight1 > 0 and dbl_weight2 > 0";
	$lowRisk  = getCollectionFor($constraint, "C.dbl_portvar ASC", $id, $mainAsset, $link);

	$constraint = "";
	if (!$includeall) $constraint = "dbl_weight1 > 0 and dbl_weight2 > 0";
	$hiReturn = getCollectionFor($constraint, "C.dbl_portret DESC", $id, $mainAsset, $link);

	$rates = array();
	$rates["1w"] = getRatesDataFor($id, "1w", $link);
	$rates["1m"] = getRatesDataFor($id, "1m", $link);
	$rates["6m"] = getRatesDataFor($id, "6m", $link);
	$rates["1y"] = getRatesDataFor($id, "1y", $link);
	$rates["2y"] = getRatesDataFor($id, "2y", $link);
	$rates["5y"] = getRatesDataFor($id, "5y", $link);
	$rates["All"] = getRatesDataFor($id, "All", $link);

	$dividendsAndEpsDetails = loadDividendsAndEpsFor($id, $link);
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
	google.setOnLoadCallback(drawTables);

	google.load("visualization", "1.1", {packages:["corechart"]});
	google.setOnLoadCallback(drawChart);

	function drawTables() {
		drawBaseDataTable();
		drawRatesDataTable();
		drawBetasTable();
		drawTable2();
		drawTable3();
		drawTable4();
		drawTable5();
	}

	function drawTable(element, data) {
		data.setProperty(0, 0, 'style', 'width:1000px');
		var table = new google.visualization.Table(document.getElementById(element));
		table.draw(data, {showRowNumber: false, allowHtml: true});
	}

	function drawBaseDataTable() {
		var data = new google.visualization.DataTable();
		data.addColumn('string', 'Date');
		data.addColumn('string', 'Last Price');
		data.addColumn('string', 'Return');

		data.addRows([['<b><?php echo $lastPriceInfo["dtm_date"];?></b>',
		'<?php echo indicatorText(round($lastPriceInfo["dbl_price"], 4)." (".round($lastPriceInfo["dbl_change"], 4).")", $lastPriceInfo["dbl_change"]); ?>',
		'<?php echo indicatorText(round($lastPriceInfo["dbl_return"], $RETURN_ROUND_PRECISION), $lastPriceInfo["dbl_return"]); ?>']]);
		var table = new google.visualization.Table(document.getElementById('table_base_data_div'));
		table.draw(data, {showRowNumber: false, width: '100%', allowHtml: true});
	}

	function drawRatesDataTable() {
		var data = new google.visualization.DataTable();
		data.addColumn('string', 'Period');
		data.addColumn('string', 'Average Return');
		data.addColumn('string', 'Volatility');
		data.addColumn('string', 'Kelly Fraction');

		data.addRows([['1 week',
		'<?php echo $rates["1w"]["expectedReturn"]; ?>',
		'<?php echo $rates["1w"]["volatility"]; ?>',
		'<?php echo $rates["1w"]["kelly"]; ?>'],
		['1 month',
		'<?php echo $rates["1m"]["expectedReturn"]; ?>',
		'<?php echo $rates["1m"]["volatility"]; ?>',
		'<?php echo $rates["1m"]["kelly"]; ?>'],
		['6 months',
		'<?php echo $rates["6m"]["expectedReturn"]; ?>',
		'<?php echo $rates["6m"]["volatility"]; ?>',
		'<?php echo $rates["6m"]["kelly"]; ?>'],
		['1 year',
		'<?php echo $rates["1y"]["expectedReturn"]; ?>',
		'<?php echo $rates["1y"]["volatility"]; ?>',
		'<?php echo $rates["1y"]["kelly"]; ?>'],
		['2 years',
		'<?php echo $rates["2y"]["expectedReturn"]; ?>',
		'<?php echo $rates["2y"]["volatility"]; ?>',
		'<?php echo $rates["2y"]["kelly"]; ?>'],
		['5 years',
		'<?php echo $rates["5y"]["expectedReturn"]; ?>',
		'<?php echo $rates["5y"]["volatility"]; ?>',
		'<?php echo $rates["5y"]["kelly"]; ?>'],
		['Overall',
		'<?php echo $rates["All"]["expectedReturn"]; ?>',
		'<?php echo $rates["All"]["volatility"]; ?>',
		'<?php echo $rates["All"]["kelly"]; ?>']]);
		var table = new google.visualization.Table(document.getElementById('table_rates_data_div'));
		table.draw(data, {showRowNumber: false, width: '100%', allowHtml: true});
	}

	function drawBetasTable() {
		var data = new google.visualization.DataTable();
		data.addColumn('string', '<?php echo getName(105, $link);?>');
		data.addColumn('string', '<?php echo getName(104, $link);?>');
		data.addColumn('string', '<?php echo getName(102, $link);?>');
		data.addColumn('string', '<?php echo getName(103, $link);?>');

		data.addRows([['<?php echo calculateBeta($id, 105, $link);?>',
		'<?php echo calculateBeta($id, 104, $link);?>',
		'<?php echo calculateBeta($id, 102, $link);?>',
		'<?php echo calculateBeta($id, 103, $link);?>']]);
		var table = new google.visualization.Table(document.getElementById('table_betas_div'));
		table.draw(data, {showRowNumber: false, width: '100%', allowHtml: true});
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

	function drawTable2() {
		<?php if (!empty($hiCorrelation)) { ?>
		var data = generateData();
		data.addRows([<?php showData($hiCorrelation); ?>]);
		drawTable('table2_div', data);
		<?php } ?>
	}

	function drawTable3() {
		<?php if (!empty($lowCorrelation)) { ?>
		var data = generateData();
		data.addRows([<?php showData($lowCorrelation); ?>]);
		drawTable('table3_div', data);
		<?php } ?>
	}

	function drawTable4() {
		<?php if (!empty($lowRisk)) { ?>
		var data = generateData();
		data.addRows([<?php showData($lowRisk); ?>]);
		drawTable('table4_div', data);
		<?php } ?>
	}

	function drawTable5() {
		<?php if (!empty($hiReturn)) { ?>
		var data = generateData();
		data.addRows([<?php showData($hiReturn); ?>]);
		drawTable('table5_div', data);
		<?php } ?>
	}

	function drawChart() {
		drawPricesChart();
		drawDividendsAndEpsChart();
	}

	function drawPricesChart() {
		var data = google.visualization.arrayToDataTable([
			['Date', 'price']
<?php
		$res = mysql_query("select dtm_date, dbl_price from tbl_prices where fk_assetID=$id order by dtm_date asc", $link);
		if (!$res) die("Invalid query: ". mysql_error());
		while ($row = mysql_fetch_array($res)) {
			echo ",['".$row[0]."',".$row[1]."]";
		}
		mysql_free_result($res);
?>
		]);

		var options = {
			title: "<?php echo $mainAsset;?>"
		};
		var chart = new google.visualization.LineChart(document.getElementById('prices_chart_div'));
		chart.draw(data, options);
	}

	function drawDividendsAndEpsChart() {
		<?php if (!empty($dividendsAndEpsDetails)) { ?>
		var data = google.visualization.arrayToDataTable([
			[{label: 'Date', id: 'Date', type: 'string'},
			 {label: 'Dividend Pay', id: 'Dividend Pay', type: 'number'},
			 {label: 'EPS', id: 'EPS', type: 'number'},
			 {label: 'Predicted EPS', id: 'Predicted EPS', type: 'number'},
			 {label: 'End of Financial Period EPS', id: 'End of Financial Period EPS', type: 'number'}]
			<?php echo $dividendsAndEpsDetails; ?>
		]);

		var options = {
			title: "<?php echo $mainAsset;?> - Dividends and EPS",
			interpolateNulls: true,
		};
		var chart = new google.visualization.LineChart(document.getElementById('div_eps_chart_div'));
		chart.draw(data, options);
		<?php } ?>
	}
    </script>
  </head>
  <body>
    <table align="center" border="0"><tr>
      <td valign="top"><?php showMenu(); ?></td>
      <td><table align="center" border="0">
	<tr><td align="right">
		<form name="main" method="GET" action="./<?php echo basename($_SERVER['PHP_SELF']);?>">
		<font face="verdana">Asset:</font>
		<select name="id" onchange="document.forms['main'].submit();">
		<?php printAssets($id, $link);?>
		</select>
		<font face="verdana">Weights:</font>
		<input type="radio" name="all" value="true" <?php if ($includeall) echo "checked";?> onchange="document.forms['main'].submit();">All
		<input type="radio" name="all" value="false" <?php if (!$includeall) echo "checked";?> onchange="document.forms['main'].submit();">Positive only
		</form>
	</td></tr>

	<tr><td><div id="table_base_data_div" style="width: 1044px;"></div></td></tr>
	<tr><td><font face="verdana">Betas:</font><div id="table_betas_div" style="width: 1044px;"></div></td></tr>
	<tr><td><div id="prices_chart_div" style="width: 1044px; height: 350px;"></div></td></tr>
<?php if (!empty($dividendsAndEpsDetails)) { ?>
	<tr><td><div id="div_eps_chart_div" style="width: 1044px; height: 350px;"></div></td></tr>
<?php } ?>
	<tr><td><font face="verdana">Rates:</font><div id="table_rates_data_div" style="width: 1044px;"></div></td></tr>
	<tr><td><hr/></td></tr>
	<tr><td align="right"><a href="./all_p.php?id=<?php echo $id?>"><i>All correlations &gt;&gt;</i></a></td></tr>
	<tr><td><font face="verdana">Highly correlated:</font><div id='table2_div' style="width: 1044px;"></div></td></tr>
	<tr><td>&nbsp;</td></tr>
	<tr><td><font face="verdana">Low correlation:</font><div id='table3_div' style="width: 1044px;"></div></td></tr>
	<tr><td>&nbsp;</td></tr>
	<tr><td><font face="verdana">Low risk:</font><div id='table4_div' style="width: 1044px;"></div></td></tr>
	<tr><td>&nbsp;</td></tr>
	<tr><td><font face="verdana">High return:</font><div id='table5_div' style="width: 1044px;"></div></td></tr>
      </table></td>
    </tr></table>
  </body>
</html>
<?php mysql_close($link); ?>