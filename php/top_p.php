<?php
// Top pairs script
	include_once("../lib/mysql.php");
	include_once("../lib/utils.php");
	include_once("./funcs.php");
	header("Content-Type:text/html; charset=UTF-8");

	$link = connect("portfolio");

	$query = "SELECT B.fk_asset1ID, ";
	$query.= "B.fk_asset2ID, A.vchr_name, ";
	$query.= "B.dbl_correlation, B.dbl_weight1, B.dbl_weight2, B.dbl_portret, B.dbl_portvar ";
	$query.= "FROM  tbl_correlations as B, tbl_assets as A ";
	$query.= "WHERE B.fk_asset2ID=A.int_assetID ";
	$query.= "ORDER BY B.dbl_portret DESC ";
	$query.= "LIMIT 0, 50";

	$tableResult = "";
	$res = mysql_query($query, $link);

	if (!$res) die("Invalid query: ". mysql_error());
	$i = 0;
	while ($row = mysql_fetch_row($res)) {
		if ($i == 0) $tableResult.= "[";
		else $tableResult.= ",[";

		$tableResult.= "'".linkToAsset($row[0], getName($row[0], $link))."<br/>"
			.linkToAsset($row[1], $row[2])."',";

		$tableResult.= toChartNumber(round($row[3], $VOLATILITY_ROUND_PRECISION)).",";
		$tableResult.= "'".percentWeightFrom($row[4])."&percnt;<br/>"
			.percentWeightFrom($row[5])."&percnt;',";

		$tableResult.= toChartNumber(round($row[6], $RETURN_ROUND_PRECISION)).",";
		$tableResult.= toChartNumber(volatilityFrom($row[7]))."]";
		$i++;
	}
	mysql_free_result($res);
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
		dataTable.addColumn('string', 'Portfolio');
		dataTable.addColumn('number', 'Correlation');
		dataTable.addColumn('string', 'Weights');
		dataTable.addColumn('number', 'Portfolio Return');
		dataTable.addColumn('number', 'Portfolio Volatility');
		return dataTable;
	}

    </script>
  </head>
  <body>
    <table align="center" border="0"><tr>
      <td valign="top"><?php showMenu(); ?></td>
      <td><table align="center" border="0">
	<tr><td><font face="verdana">High return pairs:</font></td></tr>
	<tr><td><hr/></td></tr>
	<tr><td><div id='table_div' style="width: 1044px;"></div></td></tr>
      </table></td>
    </tr></table>
  </body>
</html>
<?php mysql_close($link); ?>