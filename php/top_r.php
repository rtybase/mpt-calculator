<?php
// Top rates script
	include_once("../lib/mysql.php");
	include_once("../lib/utils.php");
	include_once("./funcs.php");
	header("Content-Type:text/html; charset=UTF-8");

	$period = periodTableFrom($_GET["period"]);
	$type = productTypeFrom($_GET["type"]);

	$link = connect("portfolio");

	$query = "SELECT a.fk_assetID, b.vchr_name, a.dbl_avgreturn, a.dbl_varience, b.vchr_type ";
	$query.= "FROM tbl_avgreturns".$period." a, tbl_assets b ";
	$query.= "WHERE a.fk_assetID = b.int_assetID ";

	if (!empty($type)) {
		$query.= "AND b.vchr_type = '".$type."' ";
	}

	$query.= "ORDER BY a.dbl_avgreturn DESC ";
	$query.= "LIMIT 0, 100";

	$tableResult = "";
	$res = mysql_query($query, $link);

	if (!$res) die("Invalid query: ". mysql_error());
	$i = 0;
	while ($row = mysql_fetch_row($res)) {
		if ($i == 0) $tableResult.= "[";
		else $tableResult.= ",[";

		$tableResult.= "'".linkToAsset($row[0], $row[1])."',";
		$tableResult.= "'".$row[4]."',";
		$tableResult.= toChartNumber(round($row[2], $RETURN_ROUND_PRECISION)).",";
		$tableResult.= toChartNumber(volatilityFrom($row[3]))."]";
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
		data.setProperty(0, 0, 'style', 'width:750px');
		var table = new google.visualization.Table(document.getElementById(element));
		table.draw(data, {showRowNumber: false, allowHtml: true});
	}

	function generateData() {
		var dataTable = new google.visualization.DataTable();
		dataTable.addColumn('string', 'Asset');
		dataTable.addColumn('string', 'Type');
		dataTable.addColumn('number', 'Average Return');
		dataTable.addColumn('number', 'Volatility');
		return dataTable;
	}
    </script>
  </head>
  <body>
    <table align="center" border="0"><tr>
      <td valign="top"><?php showMenu(); ?></td>
      <td valign="top"><table align="center" border="0">
	<tr><td align="left">
		<form name="main" method="GET" action="./<?php echo basename($_SERVER['PHP_SELF']);?>">
		<font face="verdana">High returns for
			<select name="period" onchange="document.forms['main'].submit();">
				<option value="">Overall</option>
<?php
	printOptions($PERIODS, str_replace("_", "", $period));
?>
			</select>
		period and type
			<select name="type" onchange="document.forms['main'].submit();">
				<option value="">All</option>
<?php
	printOptions($PRODUCT_TYPE, $_GET["type"]);
?>
			</select></font>
		</form>
	</td></tr>
	<tr><td><hr/></td></tr>
	<tr><td><div id='table_div' style="width: 1044px;"></div></td></tr>
      </table></td>
    </tr></table>
  </body>
</html>
<?php mysql_close($link); ?>