<?php
// Show EPS details
	include_once("../lib/mysql.php");
	include_once("../lib/utils.php");
	include_once("./funcs.php");
	header("Content-Type:text/html; charset=UTF-8");

function mergeDividendsEpsAndPricesFor($assetId, $link) {
	$divsAndEps = mergeDivsAndEps($assetId, $link);

	reset($divsAndEps);
	$minDate = key($divsAndEps);
	reset($divsAndEps);

	$query = "select dtm_date, dbl_price, dbl_vol_change_rate, dbl_return ";
	$query.= " from tbl_prices where fk_assetID=$assetId ";
	$query.= " and dtm_date>=\"".$minDate."\" order by dtm_date asc";

	$res = mysql_query($query, $link);
	if (!$res) die("Invalid query: ". mysql_error());

	while ($row = mysql_fetch_array($res)) {
		if (!array_key_exists($row[0], $divsAndEps)) {
			$divsAndEps[$row[0]] = array();
		}
		$divsAndEps[$row[0]]["price"] = $row[1];
		$divsAndEps[$row[0]]["vol_change_rate"] = $row[2];
		$divsAndEps[$row[0]]["return"] = $row[3];
	}

	mysql_free_result($res);

	ksort($divsAndEps);
	return $divsAndEps;
}

function extractDividendsAndEpsFrom($dividendsEpsAndPrices) {
	$result = "";
	foreach ($dividendsEpsAndPrices as $key => $value) {
		$result .= ",['".$key."',";
		$result .= valueOrNullFrom($value["dividend"]).",";
		$result .= valueOrNullFrom($value["eps"]).",";
		$result .= valueOrNullFrom($value["eps_predicted"]).",";
		$result .= valueOrNullFrom($value["eps_eofp"]).",null]";
	}

	return $result;
}

function extractPricesFrom($dividendsEpsAndPrices) {
	$result = "";
	foreach ($dividendsEpsAndPrices as $key => $value) {
		$result .= ",['".$key."',null,null,null,null";
		$result .= ",".valueOrNullFrom($value["price"])."]";
	}

	return $result;
}

function extractVolChangeRateFrom($dividendsEpsAndPrices) {
	$result = "";
	foreach ($dividendsEpsAndPrices as $key => $value) {
		$result .= ",['".$key."',null,null,null,null";
		$result .= ",".valueOrNullFrom($value["vol_change_rate"])."]";
	}

	return $result;
}

function extractReturnsFrom($dividendsEpsAndPrices) {
	$result = "";
	foreach ($dividendsEpsAndPrices as $key => $value) {
		$result .= ",['".$key."',null,null,null,null";
		$result .= ",".valueOrNullFrom($value["return"])."]";
	}

	return $result;
}

function getAllEpsDetails($assetId, $link) {
	$query = "select a.dtm_date, a.dbl_eps, a.dbl_prd_eps, a.int_no_of_analysts,";
	$query.= "  b.dbl_eps non_gaap_eps, b.dbl_prd_eps non_gaap_prd_eps,";
	$query.= "  b.bln_after_market_close,";
	$query.= "  b.dbl_revenue / 1000000000, b.dbl_prd_revenue / 1000000000 ";
	$query.= "from tbl_eps a ";
	$query.= "LEFT JOIN tbl_n_gaap_eps b ON a.fk_assetID = b.fk_assetID AND a.dtm_date=b.dtm_date ";
	$query.= "where a.fk_assetID=$assetId ";
	$query.= "order by a.dtm_date DESC";

	$res = mysql_query($query, $link);
	if (!$res) die("Invalid query: ". mysql_error());

	$tableResult = "";
	$roundPrecision = 2;
	$i = 0;

	while ($row = mysql_fetch_array($res)) {
		if ($i == 0) $tableResult.= "[";
		else $tableResult.= ",[";

		$tableResult.= "'$row[0]',";
		$tableResult.= toChartNumber(round($row[1], $roundPrecision)).",";
		$tableResult.= toChartNumber(roundOrNull($row[2], $roundPrecision)).",";
		$tableResult.= toChartNumber(roundOrNull($row[3], $roundPrecision)).",";
		$tableResult.= toChartNumber(roundOrNull($row[4], $roundPrecision)).",";
		$tableResult.= toChartNumber(roundOrNull($row[5], $roundPrecision)).",";
		$tableResult.= toChartNumber(roundOrNull($row[7], $roundPrecision)).",";
		$tableResult.= toChartNumber(roundOrNull($row[8], $roundPrecision)).",";
		$tableResult.= booleanValueOrNull($row[6])."]";

		$i++;
	}

	mysql_free_result($res);
	return $tableResult;
}

function getFScore($assetSymbol, $link) {
	$tableResult = "";
	if (!empty($assetSymbol)) {
		$query = "select dtm_date, dbl_fscore ";
		$query.= "from tbl_fscores ";
		$query.= "where vchr_symbol='$assetSymbol' ";
		$query.= "order by dtm_date asc";

		$res = mysql_query($query, $link);
		if (!$res) die("Invalid query: ". mysql_error());

		$roundPrecision = 2;
		while ($row = mysql_fetch_array($res)) {
			$tableResult.= ",['$row[0]',";
			$tableResult.= toChartNumber(round($row[1], $roundPrecision))."]";
		}

		mysql_free_result($res);

	}

	return $tableResult;
}

	$id = (int) $_GET["id"];
	if ($id < 1) $id = 1;

	$link = connect("portfolio");

	$assetRecord = getSingleValyeByPK("tbl_assets", "int_assetID", $id, $link);
	$assetName = $assetRecord["vchr_name"];
	$assetSymbol = $assetRecord["vchr_symbol"];
        $stockDetails = getStockDetails($assetSymbol, $link);

	$dividendsEpsAndPrices = mergeDividendsEpsAndPricesFor($id, $link);

	$dividendsAndEpsDetails = extractDividendsAndEpsFrom($dividendsEpsAndPrices);
	$prices = extractPricesFrom($dividendsEpsAndPrices);
	$volumeChanges = extractVolChangeRateFrom($dividendsEpsAndPrices);
	$returns = extractReturnsFrom($dividendsEpsAndPrices);

	$allEpsDetails = getAllEpsDetails($id, $link);
	$fscore = getFScore($assetSymbol, $link);
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
<?php
	if (!empty($allEpsDetails)) {
?>

		var data = generateHeader();
		data.addRows([<?php echo $allEpsDetails; ?>]);
		drawTable('table_div', data);

<?php
	}
?>
		drawChart1();
		drawChart2();
		drawChart3();
		drawChart4();
		drawChart5();
	}

	function drawTable(element, data) {
		data.setProperty(0, 0, 'style', 'width:1000px');
		var table = new google.visualization.Table(document.getElementById(element));
		table.draw(data, {showRowNumber: false, allowHtml: true});
	}

	function generateHeader() {
		var dataTable = new google.visualization.DataTable();
		dataTable.addColumn('string', 'Report Date');
		dataTable.addColumn('number', 'Eps');
		dataTable.addColumn('number', 'predicted Eps');
		dataTable.addColumn('number', 'No. of Analysts');
		dataTable.addColumn('number', 'non-GAAP Eps');
		dataTable.addColumn('number', 'non-GAAP predicted Eps');
		dataTable.addColumn('number', 'Revenue (B)');
		dataTable.addColumn('number', 'predicted Revenue (B)');
		dataTable.addColumn('string', 'After Market Close?');
		return dataTable;
	}


	function drawChart1() {
		<?php if (!empty($dividendsAndEpsDetails)) { ?>
		var data = google.visualization.arrayToDataTable([
			[{label: 'Date', id: 'Date', type: 'string'},
			 {label: 'Div. Pay', id: 'Div. Pay', type: 'number'},
			 {label: 'EPS', id: 'EPS', type: 'number'},
			 {label: 'Prd. EPS', id: 'Prd. EPS', type: 'number'},
			 {label: 'End of Fin. Period EPS', id: 'End of Fin. Period EPS', type: 'number'},
			 {label: 'Price', id: 'Price', type: 'number'}]
			<?php echo $dividendsAndEpsDetails; ?>
		]);

		var options = {
			title: "<?php echo $assetName;?> - dividends and EPS",
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
		<?php if (!empty($prices)) { ?>
		var data = google.visualization.arrayToDataTable([
			[{label: 'Date', id: 'Date', type: 'string'},
			 {label: 'Div. Pay', id: 'Div. Pay', type: 'number'},
			 {label: 'EPS', id: 'EPS', type: 'number'},
			 {label: 'Prd. EPS', id: 'Prd. EPS', type: 'number'},
			 {label: 'End of Fin. Period EPS', id: 'End of Fin. Period EPS', type: 'number'},
			 {label: 'Price', id: 'Price', type: 'number'}]
			<?php echo $prices; ?>
		]);

		var options = {
			title: "<?php echo $assetName;?> - prices",
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
		<?php if (!empty($fscore)) { ?>
		var data = google.visualization.arrayToDataTable([
			[{label: 'Date', id: 'Date', type: 'string'},
			 {label: 'F-Score', id: 'F-Score', type: 'number'}]
			<?php echo $fscore; ?>
		]);

		var options = {
			title: "<?php echo $assetName;?> - F-Score",
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

	function drawChart4() {
		<?php if (!empty($returns)) { ?>
		var data = google.visualization.arrayToDataTable([
			[{label: 'Date', id: 'Date', type: 'string'},
			 {label: 'Div. Pay', id: 'Div. Pay', type: 'number'},
			 {label: 'EPS', id: 'EPS', type: 'number'},
			 {label: 'Prd. EPS', id: 'Prd. EPS', type: 'number'},
			 {label: 'End of Fin. Period EPS', id: 'End of Fin. Period EPS', type: 'number'},
			 {label: 'Return rates', id: 'Return rates', type: 'number'}]
			<?php echo $returns; ?>
		]);

		var options = {
			title: "<?php echo $assetName;?> - return rates",
			interpolateNulls: true,
			explorer: {
				actions: ['dragToZoom', 'rightClickToReset'],
				keepInBounds: true
			}
		};
		var chart = new google.visualization.LineChart(document.getElementById('chart4_div'));
		chart.draw(data, options);
		<?php } ?>
	}

	function drawChart5() {
		<?php if (!empty($volumeChanges)) { ?>
		var data = google.visualization.arrayToDataTable([
			[{label: 'Date', id: 'Date', type: 'string'},
			 {label: 'Div. Pay', id: 'Div. Pay', type: 'number'},
			 {label: 'EPS', id: 'EPS', type: 'number'},
			 {label: 'Prd. EPS', id: 'Prd. EPS', type: 'number'},
			 {label: 'End of Fin. Period EPS', id: 'End of Fin. Period EPS', type: 'number'},
			 {label: 'Vol. chng rate', id: 'Vol. chng rate', type: 'number'}]
			<?php echo $volumeChanges; ?>
		]);

		var options = {
			title: "<?php echo $assetName;?> - volume change rates",
			interpolateNulls: true,
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
	<tr><td  align="left">
		<font face="verdana">EPS, dividends and prices: <?php 
			echo linkToAsset($id, $assetName, false);
			if (!empty($assetRecord["vchr_price_symbol"])) {
				echo " or <a href=\"https://finance.yahoo.com/quote/".$assetRecord["vchr_price_symbol"]."/\">YF=".$assetRecord["vchr_price_symbol"]."</a>"; 
			}
		?></font>
	</td></tr>
<?php
	if (!empty($stockDetails)) {
			echo "<tr><td align=\"left\"><font face=\"verdana\">Sector: <i>".$stockDetails[1]."</i></font></td></tr>";
			echo "<tr><td align=\"left\"><font face=\"verdana\">Industry: <i>".$stockDetails[2]."</i></font></td></tr>";
	}
?>
	<tr><td><hr/></td></tr>
	<tr><td><font face="verdana">EPS (more <a href="./all_sc.php?id=<?php echo $id; ?>">here...</a>):</font><div id="table_div" style="width: 1044px;"></div></td></tr>
	<tr><td><font face="verdana">GAAP EPS:</font><div id="chart1_div" style="width: 1044px; height: 350px;"></div></td></tr>
	<tr><td><font face="verdana">Prices:</font><div id="chart2_div" style="width: 1044px; height: 350px;"></div></td></tr>
	<tr><td><font face="verdana">Returns:</font><div id="chart4_div" style="width: 1044px; height: 350px;"></div></td></tr>
	<tr><td><font face="verdana">Vol. change rate:</font><div id="chart5_div" style="width: 1044px; height: 350px;"></div></td></tr>
	<tr><td><font face="verdana">F-score:</font><div id="chart3_div" style="width: 1044px; height: 350px;"></div></td></tr>
      </table></td>
    </tr></table>
  </body>
</html>
<?php mysql_close($link); ?>