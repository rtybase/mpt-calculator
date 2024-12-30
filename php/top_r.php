<?php
	include_once("../lib/mysql.php");
	include_once("../lib/utils.php");
	include_once("./funcs.php");
	header("Content-Type:text/html; charset=UTF-8");

	$period = periodTableFrom($_GET["period"]);

	$link = connect("portfolio");

	$query = "SELECT a.fk_assetID, b.vchr_name, a.dbl_avgreturn, a.dbl_varience ";
	$query.= "FROM tbl_avgreturns".$period." a, tbl_assets b ";
	$query.= "WHERE a.fk_assetID = b.int_assetID ";
	$query.= "ORDER BY a.dbl_avgreturn DESC ";
	$query.= "LIMIT 0, 60";

	$tableResult = "";
	$res = mysql_query($query, $link);

	if (!$res) die("Invalid query: ". mysql_error());
	$i = 0;
	while ($row = mysql_fetch_row($res)) {
		if ($i == 0) $tableResult.= "[";
		else $tableResult.= ",[";

		$tableResult.= "'".linkToAsset($row[0], $row[1])."',";
		$tableResult.= toChartNumber(round($row[2], $RETURN_ROUND_PRECISION)).",";
		$tableResult.= toChartNumber(round(sqrt(abs($row[3])), $VOLATILITY_ROUND_PRECISION))."]";
		$i++;
	}
	mysql_free_result($res);
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
		data.setProperty(0, 0, 'style', 'width:1000px');
		var table = new google.visualization.Table(document.getElementById(element));
		table.draw(data, {showRowNumber: false, allowHtml: true});
	}

	function generateData() {
		var dataTable = new google.visualization.DataTable();
		dataTable.addColumn('string', 'Asset');
		dataTable.addColumn('number', 'Average Return');
		dataTable.addColumn('number', 'Volatility');
		return dataTable;
	}
    </script>
  </head>
  <body>
    <table align="center" border="0"><tr>
      <td valign="top"><?php showMenu(); ?></td>
      <td><table align="center" border="0">
	<tr><td align="left">
		<form name="main" method="GET" action="./<?php echo basename($_SERVER['PHP_SELF']);?>">
		<font face="verdana">High returns for
			<select name="period" onchange="document.forms['main'].submit();">
				<option value="">Overall</option>
				<option value="1w"<?php if ($period === "_1w") echo " selected";?>>1 Week</option>
				<option value="1m"<?php if ($period === "_1m") echo " selected";?>>1 Month</option>
				<option value="6m"<?php if ($period === "_6m") echo " selected";?>>6 Months</option>
				<option value="1y"<?php if ($period === "_1y") echo " selected";?>>1 Year</option>
				<option value="2y"<?php if ($period === "_2y") echo " selected";?>>2 Years</option>
				<option value="5y"<?php if ($period === "_5y") echo " selected";?>>5 Years</option>
			</select>
		period:</font>
		</form>
	</td></tr>
	<tr><td><hr/></td></tr>
	<tr><td><div id='table_div' style="width: 1044px;"></div></td></tr>
      </table></td>
    </tr></table>
  </body>
</html>
<?php mysql_close($link); ?>