<?php
// Show EPS by price script
	include_once("../lib/mysql.php");
	include_once("../lib/utils.php");
	include_once("./funcs.php");
	header("Content-Type:text/html; charset=UTF-8");

	function mergeDividendsEpsAndPricesFor($assetId, $link) {
		$divsAndEps = mergeDivsAndEps($assetId, $link);

		reset($divsAndEps);
		$minDate = key($divsAndEps);
		reset($divsAndEps);

		$query = "select dtm_date, dbl_price from tbl_prices where fk_assetID=$assetId ";
		$query.= " and dtm_date>=\"".$minDate."\" order by dtm_date asc";

		$res = mysql_query($query, $link);
		if (!$res) die("Invalid query: ". mysql_error());

		while ($row = mysql_fetch_array($res)) {
			if (!array_key_exists($row[0], $divsAndEps)) {
				$divsAndEps[$row[0]] = array();
			}

			$divsAndEps[$row[0]]["price"] = $row[1];
		}
		mysql_free_result($res);

		ksort($divsAndEps);
		return $divsAndEps;
	}

	function loadDividendsAndEpsFrom($dividendsEpsAndPrices) {
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

	function loadPricesFrom($dividendsEpsAndPrices) {
		$result = "";
		foreach ($dividendsEpsAndPrices as $key => $value) {
			$result .= ",['".$key."',null,null,null,null";
			$result .= ",".valueOrNullFrom($value["price"])."]";
		}

		return $result;
	}


	$id = (int) $_GET["id"];
	if ($id < 1) $id = 1;

	$link = connect("portfolio");

	$assetName = getName($id, $link);
	$dividendsEpsAndPrices = mergeDividendsEpsAndPricesFor($id, $link);

	$dividendsAndEpsDetails = loadDividendsAndEpsFrom($dividendsEpsAndPrices);
	$prices = loadPricesFrom($dividendsEpsAndPrices);
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
	google.load("visualization", "1.1", {packages:["corechart"]});
	google.setOnLoadCallback(drawChart);

	function drawChart() {
		drawChart1();
		drawChart2();
	}

	function drawChart1() {
		<?php if (!empty($dividendsAndEpsDetails)) { ?>
		var data = google.visualization.arrayToDataTable([
			[{label: 'Date', id: 'Date', type: 'string'},
			 {label: 'Dividend Pay', id: 'Dividend Pay', type: 'number'},
			 {label: 'EPS', id: 'EPS', type: 'number'},
			 {label: 'Predicted EPS', id: 'Predicted EPS', type: 'number'},
			 {label: 'End of Financial Period EPS', id: 'End of Financial Period EPS', type: 'number'},
			 {label: 'Price', id: 'Price', type: 'number'}]
			<?php echo $dividendsAndEpsDetails; ?>
		]);

		var options = {
			title: "<?php echo $assetName;?> - dividends and EPS",
			interpolateNulls: true,
		};
		var chart = new google.visualization.LineChart(document.getElementById('chart1_div'));
		chart.draw(data, options);
		<?php } ?>
	}

	function drawChart2() {
		<?php if (!empty($prices)) { ?>
		var data = google.visualization.arrayToDataTable([
			[{label: 'Date', id: 'Date', type: 'string'},
			 {label: 'Dividend Pay', id: 'Dividend Pay', type: 'number'},
			 {label: 'EPS', id: 'EPS', type: 'number'},
			 {label: 'Predicted EPS', id: 'Predicted EPS', type: 'number'},
			 {label: 'End of Financial Period EPS', id: 'End of Financial Period EPS', type: 'number'},
			 {label: 'Price', id: 'Price', type: 'number'}]
			<?php echo $prices; ?>
		]);

		var options = {
			title: "<?php echo $assetName;?> - prices",
			interpolateNulls: true,
		};
		var chart = new google.visualization.LineChart(document.getElementById('chart2_div'));
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
		<font face="verdana">EPS, dividends and prices: <?php echo linkToAsset($id, $assetName); ?></font>
	</td></tr>
	<tr><td><hr/></td></tr>
	<tr><td><div id="chart1_div" style="width: 1044px; height: 350px;"></div></td></tr>
	<tr><td><div id="chart2_div" style="width: 1044px; height: 350px;"></div></td></tr>
      </table></td>
    </tr></table>
  </body>
</html>
<?php mysql_close($link); ?>