<?php
// Top financial scores script
	include_once("../lib/mysql.php");
	include_once("../lib/utils.php");
	include_once("./funcs.php");
	header("Content-Type:text/html; charset=UTF-8");

	$link = connect("portfolio");

	$query = "SELECT c.int_assetID, c.vchr_name, c.vchr_symbol, a.dtm_date, ";
	$query.= "	a.dbl_total_current_assets / a.dbl_total_current_liabilities, ";
	$query.= "	a.dbl_total_assets / a.dbl_total_liabilities, ";
	$query.= "	a.dbl_total_liabilities / (a.dbl_total_assets - a.dbl_total_liabilities), ";
	$query.= "	IF(a.dbl_total_equity is null, a.dbl_total_liabilities / (a.dbl_total_assets - a.dbl_total_liabilities), a.dbl_total_liabilities / a.dbl_total_equity), ";
	$query.= "	IF(a.dbl_capital_expenditures is NULL, (a.dbl_net_cash_flow_operating * 1000) / a.dbl_share_issued, ((a.dbl_net_cash_flow_operating - ABS(a.dbl_capital_expenditures)) * 1000) / a.dbl_share_issued) ";
	$query.= "FROM tbl_assets c USE INDEX (idx_tbl_assets_assetID_name_symbol_type) ";
	$query.= "INNER JOIN ( ";
	$query.= "	SELECT f1.*  ";
	$query.= "	FROM tbl_finances_quarter f1 ";
	$query.= "	INNER JOIN ( ";
	$query.= "		SELECT vchr_symbol, max(dtm_date) dtm_date  ";
	$query.= "		FROM tbl_finances_quarter ";
	$query.= "		WHERE vchr_symbol in (SELECT vchr_symbol FROM tbl_assets WHERE vchr_type = 'Stock') ";
	$query.= "		GROUP BY vchr_symbol ";
	$query.= "	) f2 ";
	$query.= "	ON f1.vchr_symbol = f2.vchr_symbol AND f1.dtm_date = f2.dtm_date ";
	$query.= ") a ";
	$query.= "ON c.vchr_symbol = a.vchr_symbol ";
	$query.= "WHERE c.vchr_type = 'Stock'";

	$roundPrecision = 4;
	$tableResult = "";
	$res = mysql_query($query, $link);

	if (!$res) die("Invalid query: ". mysql_error());
	$i = 0;
	while ($row = mysql_fetch_row($res)) {
		if ($i == 0) $tableResult.= "[";
		else $tableResult.= ",[";

		$tableResult.= "'".linkToAsset($row[0], $row[1])."',";
		$tableResult.= "'".$row[2]."',";
		$tableResult.= "'".$row[3]."',";

		$tableResult.= toChartNumber(roundOrNull($row[4], $roundPrecision)).",";
		$tableResult.= toChartNumber(roundOrNull($row[5], $roundPrecision)).",";
		$tableResult.= toChartNumber(roundOrNull($row[6], $roundPrecision)).",";
		$tableResult.= toChartNumber(roundOrNull($row[7], $roundPrecision)).",";
		$tableResult.= toChartNumber(roundOrNull($row[8], $roundPrecision))."]";

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
		dataTable.addColumn('string', 'Asset');
		dataTable.addColumn('string', 'Symbol');
		dataTable.addColumn('string', 'Date Reported');
		dataTable.addColumn('number', 'Current Ratio');
		dataTable.addColumn('number', 'Total Ratio');
		dataTable.addColumn('number', 'Debt/Eq. Calculated');
		dataTable.addColumn('number', 'Debt/Eq. Reported');
		dataTable.addColumn('number', 'Free-Cash-Flow/Shares');
		return dataTable;
	}
    </script>
  </head>
  <body>
    <table align="center" border="0"><tr>
      <td valign="top"><?php showMenu(); ?></td>
      <td><table align="center" border="0">
	<tr><td align="left">
		<font face="verdana">Financial scores for <?php echo $i ?> assets:</font>
	</td></tr>
	<tr><td><hr/></td></tr>
	<tr><td><div id='table_div' style="width: 1044px;"></div></td></tr>
      </table></td>
    </tr></table>
  </body>
</html>
<?php mysql_close($link); ?>