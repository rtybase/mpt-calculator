<?php
// EPS by price script
	include_once("../lib/mysql.php");
	include_once("../lib/utils.php");
	include_once("./funcs.php");
	header("Content-Type:text/html; charset=UTF-8");

function getEps($link) {
	$query = "SELECT a.fk_assetID, c.vchr_name, a.dbl_eps, a.dbl_prd_eps, a.dtm_date, ";
	$query.= "    d.dbl_eps n_gaap_eps, d.dbl_prd_eps n_gaap_prd_eps, d.bln_after_market_close, ";
	$query.= "    d.dbl_revenue / 1000000000, d.dbl_prd_revenue / 1000000000 ";
	$query.= "FROM tbl_eps a ";
	$query.= "INNER JOIN ( ";
	$query.= "  SELECT fk_assetID, MAX( dtm_date ) dtm_date ";
	$query.= "  FROM tbl_eps GROUP BY fk_assetID) b  ";
	$query.= "ON a.fk_assetID = b.fk_assetID AND a.dtm_date = b.dtm_date ";
	$query.= "INNER JOIN tbl_assets c ON a.fk_assetID = c.int_assetID ";
	$query.= "LEFT JOIN tbl_n_gaap_eps d ON a.fk_assetID = d.fk_assetID AND a.dtm_date = d.dtm_date";

	$res = mysql_query($query, $link);
	if (!$res) die("Invalid query: ". mysql_error());

	$ret = array();
	while ($row = mysql_fetch_row($res)) {
		$assetId = $row[0];
		$ret[$assetId] = array();
		$ret[$assetId]["asset"] = $row[1];
		$ret[$assetId]["eps"] = $row[2];
		$ret[$assetId]["prd_eps"] = $row[3];
		$ret[$assetId]["d_date"] = $row[4];
		$ret[$assetId]["n_gaap_eps"] = $row[5];
		$ret[$assetId]["n_gaap_prd_eps"] = $row[6];
		$ret[$assetId]["after_market_close"] = $row[7];
		$ret[$assetId]["revenue"] = $row[8];
		$ret[$assetId]["prd_revenue"] = $row[9];
	}
	mysql_free_result($res);

	return $ret;
}

function addLatestPrices($link, $epsData) {
	$query = "SELECT a.fk_assetID, a.dbl_price FROM tbl_prices a ";
	$query.= "INNER JOIN ( ";
	$query.= "    SELECT fk_assetID, max(dtm_date) dtm_date ";
	$query.= "    FROM tbl_prices GROUP BY fk_assetID) b ";
	$query.= "ON a.fk_assetID = b.fk_assetID AND a.dtm_date = b.dtm_date";

	$res = mysql_query($query, $link);
	if (!$res) die("Invalid query: ". mysql_error());

	while ($row = mysql_fetch_row($res)) {
		$assetId = $row[0];
		if (array_key_exists($assetId, $epsData)) {
			$eps = $epsData[$assetId]["eps"];
			$epsData[$assetId]["price"] = $row[1];
			if (abs($eps) < 0.00001) {
				$epsData[$assetId]["p_by_e"] = 0.0;
			} else {
				$epsData[$assetId]["p_by_e"] = $row[1] / $eps;
			}
		}
	}
	mysql_free_result($res);

	return $epsData;
}

	$link = connect("portfolio");
	$epsData = getEps($link);
	$epsData = addLatestPrices($link, $epsData);

	$tableResult = "";
	$roundPrecision = 2;
	$i = 0;
	foreach ($epsData as $key => $value) {
		if ($i == 0) $tableResult.= "[";
		else $tableResult.= ",[";

		$tableResult.= "'".linkToAsset($key, $value["asset"])."',";
		$tableResult.= "'".$value["d_date"]."',";
		$tableResult.= toChartNumber(round($value["eps"], $roundPrecision)).",";
		$tableResult.= toChartNumber(roundOrNull($value["prd_eps"], $roundPrecision)).",";
		$tableResult.= toChartNumber(round($value["price"], $roundPrecision)).",";
		$tableResult.= toChartNumber(round($value["p_by_e"], $roundPrecision)).",";
		$tableResult.= toChartNumber(roundOrNull($value["n_gaap_eps"], $roundPrecision)).",";
		$tableResult.= toChartNumber(roundOrNull($value["n_gaap_prd_eps"], $roundPrecision)).",";
		$tableResult.= toChartNumber(roundOrNull($value["revenue"], $roundPrecision)).",";
		$tableResult.= toChartNumber(roundOrNull($value["prd_revenue"], $roundPrecision)).",";
		$tableResult.= booleanValueOrNull($value["after_market_close"]).",";
		$tableResult.= "'<a href=\"./show_eps.php?id=".$key."\">details ...</a>']";
		$i++;
	}
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
	google.charts.load('current', {'packages':['table']});
	google.charts.setOnLoadCallback(generateTable);

	function generateTable() {
		var data = generateData();
		data.addRows([<?php echo $tableResult; ?>]);
		drawTable('table_div', data);
	}

	function drawTable(element, data) {
		data.setProperty(0, 0, 'style', 'width:280px');
		var table = new google.visualization.Table(document.getElementById(element));
		table.draw(data, {showRowNumber: false, allowHtml: true});
	}

	function generateData() {
		var dataTable = new google.visualization.DataTable();
		dataTable.addColumn('string', 'Asset');
		dataTable.addColumn('string', 'Lst Rep. D.');
		dataTable.addColumn('number', 'Eps');
		dataTable.addColumn('number', 'p-Eps');
		dataTable.addColumn('number', 'Lst Price');
		dataTable.addColumn('number', 'P/E');
		dataTable.addColumn('number', 'nGAAP Eps');
		dataTable.addColumn('number', 'nGAAP p-Eps');
		dataTable.addColumn('number', 'Revenue (B)');
		dataTable.addColumn('number', 'p-Rev. (B)');
		dataTable.addColumn('string', 'AMC?');
		dataTable.addColumn('string', 'More');
		return dataTable;
	}
    </script>
  </head>
  <body>
    <table align="center" border="0"><tr>
      <td valign="top"><?php showMenu(); ?></td>
      <td><table align="center" border="0">
	<tr><td align="left">
		<font face="verdana">EPS details on <?php echo count($epsData) ?> assets:</font>
	</td></tr>
	<tr><td><hr/></td></tr>
	<tr><td><div id='table_div' style="width: 1044px;"></div></td></tr>
      </table></td>
    </tr></table>
  </body>
</html>
<?php mysql_close($link); ?>