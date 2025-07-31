<?php
// EPS by price script
	include_once("../lib/mysql.php");
	include_once("../lib/utils.php");
	include_once("./funcs.php");
	header("Content-Type:text/html; charset=UTF-8");

function getEps($link) {
	$query = "SELECT a.fk_assetID, c.vchr_name, a.dbl_eps, dbl_prd_eps, a.dtm_date ";
	$query.= "FROM tbl_eps a ";
	$query.= "INNER JOIN (";
	$query.= "  SELECT fk_assetID, MAX( dtm_date ) dtm_date ";
	$query.= "  FROM tbl_eps GROUP BY fk_assetID) b ";
	$query.= "ON a.fk_assetID = b.fk_assetID AND a.dtm_date = b.dtm_date ";
	$query.= "INNER JOIN tbl_assets c ON a.fk_assetID = c.int_assetID";

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
	$i = 0;
	foreach ($epsData as $key => $value) {
		if ($i == 0) $tableResult.= "[";
		else $tableResult.= ",[";

		$tableResult.= "'".linkToAsset($key, $value["asset"])."',";
		$tableResult.= "'".$value["d_date"]."',";
		$tableResult.= toChartNumber(round($value["eps"], 5)).",";
		$tableResult.= toChartNumber(round($value["prd_eps"], 5)).",";
		$tableResult.= toChartNumber(round($value["price"], 5)).",";
		$tableResult.= toChartNumber(round($value["p_by_e"], 5)).",";
		$tableResult.= "'<a href=\"./show_eps.php?id=".$key."\">details ...</a>']";
		$i++;
	}
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

	function generateTable() {
		var data = generateData();
		data.addRows([<?php echo $tableResult; ?>]);
		drawTable('table_div', data);
	}

	function drawTable(element, data) {
		data.setProperty(0, 0, 'style', 'width:500px');
		var table = new google.visualization.Table(document.getElementById(element));
		table.draw(data, {showRowNumber: false, allowHtml: true});
	}

	function generateData() {
		var dataTable = new google.visualization.DataTable();
		dataTable.addColumn('string', 'Asset');
		dataTable.addColumn('string', 'Last EPS Report Day');
		dataTable.addColumn('number', 'EPS');
		dataTable.addColumn('number', 'EPS Predicted');
		dataTable.addColumn('number', 'Last Price');
		dataTable.addColumn('number', 'P/E');
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
		<font face="verdana">EPS details:</font>
	</td></tr>
	<tr><td><hr/></td></tr>
	<tr><td><div id='table_div' style="width: 1044px;"></div></td></tr>
      </table></td>
    </tr></table>
  </body>
</html>
<?php mysql_close($link); ?>