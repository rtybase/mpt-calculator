<?php
// Dividends by price script
	include_once("../lib/mysql.php");
	include_once("../lib/utils.php");
	include_once("./funcs.php");
	header("Content-Type:text/html; charset=UTF-8");

function getDividends($link) {
	$query = "SELECT a.fk_assetID, c.vchr_name, a.dbl_pay, a.dtm_date ";
	$query.= "FROM tbl_dividends a ";
	$query.= "INNER JOIN (";
	$query.= "  SELECT fk_assetID, MAX( dtm_date ) dtm_date ";
	$query.= "  FROM tbl_dividends GROUP BY fk_assetID) b ";
	$query.= "ON a.fk_assetID = b.fk_assetID AND a.dtm_date = b.dtm_date ";
	$query.= "INNER JOIN tbl_assets c ON a.fk_assetID = c.int_assetID";

	$res = mysql_query($query, $link);
	if (!$res) die("Invalid query: ". mysql_error());

	$ret = array();
	while ($row = mysql_fetch_row($res)) {
		$assetId = $row[0];
		$ret[$assetId] = array();
		$ret[$assetId]["asset"] = $row[1];
		$ret[$assetId]["d_pay"] = $row[2];
		$ret[$assetId]["d_date"] = $row[3];
	}
	mysql_free_result($res);

	return $ret;
}

function addLatestPrices($link, $dividendsData) {
	$query = "SELECT a.fk_assetID, a.dbl_price FROM tbl_prices a ";
	$query.= "INNER JOIN ( ";
	$query.= "    SELECT fk_assetID, max(dtm_date) dtm_date ";
	$query.= "    FROM tbl_prices GROUP BY fk_assetID) b ";
	$query.= "ON a.fk_assetID = b.fk_assetID AND a.dtm_date = b.dtm_date";

	$res = mysql_query($query, $link);
	if (!$res) die("Invalid query: ". mysql_error());

	while ($row = mysql_fetch_row($res)) {
		$assetId = $row[0];
		if (array_key_exists($assetId, $dividendsData)) {
			$d = $dividendsData[$assetId]["d_pay"];
			$dividendsData[$assetId]["price"] = $row[1];
			$dividendsData[$assetId]["pay_by_price"] = percentWeightFrom($d / $row[1]);
		}
	}
	mysql_free_result($res);

	return $dividendsData;
}

	$link = connect("portfolio");
	$dividendsData = getDividends($link);
	$dividendsData = addLatestPrices($link, $dividendsData);

	$tableResult = "";
	$i = 0;
	foreach ($dividendsData as $key => $value) {
		if ($i == 0) $tableResult.= "[";
		else $tableResult.= ",[";

		$tableResult.= "'".linkToAsset($key, $value["asset"])."',";
		$tableResult.= "'".$value["d_date"]."',";
		$tableResult.= toChartNumber(round($value["d_pay"], 5)).",";
		$tableResult.= toChartNumber(round($value["price"], 5)).",";
		$tableResult.= toChartNumber(round($value["pay_by_price"], 5))."]";
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
		data.setProperty(0, 0, 'style', 'width:1000px');
		var table = new google.visualization.Table(document.getElementById(element));
		table.draw(data, {showRowNumber: false, allowHtml: true});
	}

	function generateData() {
		var dataTable = new google.visualization.DataTable();
		dataTable.addColumn('string', 'Asset');
		dataTable.addColumn('string', 'Last Dividend Day');
		dataTable.addColumn('number', 'Dividend Pay');
		dataTable.addColumn('number', 'Last Price');
		dataTable.addColumn('number', 'Div/Price %');
		return dataTable;
	}
    </script>
  </head>
  <body>
    <table align="center" border="0"><tr>
      <td valign="top"><?php showMenu(); ?></td>
      <td><table align="center" border="0">
	<tr><td align="left">
		<font face="verdana">Dividends details:</font>
	</td></tr>
	<tr><td><hr/></td></tr>
	<tr><td><div id='table_div' style="width: 1044px;"></div></td></tr>
      </table></td>
    </tr></table>
  </body>
</html>
<?php mysql_close($link); ?>