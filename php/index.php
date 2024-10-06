<?php
	include_once("../lib/mysql.php");
	include_once("../lib/utils.php");
	include_once("./funcs.php");
	header("Content-Type:text/html; charset=UTF-8");

	$id = (int) $_GET["id"];
	if ($id < 1) $id = 1;

	$includeall = ($_GET["all"]==="true")? true: false;

	$link = connect("portfolio");

	$mainAsset = getName($id, $link);
	$lastPriceInfo = getLastPriceInfo($id, $link);
	$result = getSingleValyeByPK("tbl_avgreturns", "fk_assetID", $id, $link);

	$variance = $result["dbl_varience"];
	$expectedReturn = $result["dbl_avgreturn"];

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
		data.addColumn('string', 'Average Return');
		data.addColumn('number', 'Volatility');

		data.addRows([['<b><?php echo $lastPriceInfo["dtm_date"];?></b>',
		'<?php echo indicatorText(round($lastPriceInfo["dbl_price"], 4)." (".round($lastPriceInfo["dbl_change"], 4).")", $lastPriceInfo["dbl_change"]); ?>',
		'<?php echo indicatorText(round($lastPriceInfo["dbl_return"], $RETURN_ROUND_PRECISION), $lastPriceInfo["dbl_return"]); ?>',
		'<?php echo indicatorText(round($expectedReturn, $RETURN_ROUND_PRECISION), $expectedReturn) ;?>',
		<?php echo toChartNumber(round(sqrt($variance), $VOLATILITY_ROUND_PRECISION));?>]]);
		var table = new google.visualization.Table(document.getElementById('table_base_data_div'));
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
		var chart = new google.visualization.LineChart(document.getElementById('chart_div'));
		chart.draw(data, options);
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
	<tr><td><font face="verdana">Kelly fraction: <?php echo calculateKellyFraction($expectedReturn, $variance);?></font></td></tr>
	<tr><td><font face="verdana">Betas:</font><div id="table_betas_div" style="width: 1044px;"></div></td></tr>
	<tr><td><div id="chart_div" style="width: 1044px; height: 350px;"></div></td></tr>
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