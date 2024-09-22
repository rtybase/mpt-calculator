<?php
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

		$tableResult.= "'<a href=\"./?id=".$row[0]."\">".getName($row[0], $link)."</a><br/>"
			."<a href=\"./?id=".$row[1]."\">".$row[2]."</a>',";

		$tableResult.= toChartNumber(round($row[3], $VOLATILITY_ROUND_PRECISION)).",";
		$tableResult.= "'".round($row[4] * 100, 3)."&percnt;<br/>"
			.round($row[5] * 100, 3)."&percnt;',";

		$tableResult.= toChartNumber(round($row[6], $RETURN_ROUND_PRECISION)).",";
		$tableResult.= toChartNumber(round(sqrt(abs($row[7])), $VOLATILITY_ROUND_PRECISION))."]";
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
      <td valign="top"><a href="./">Home</a><br/>
		<a href="./top_r.php">Top returns</a><br/>
		<a href="./top_p.php">Top pairs</a>
      </td>
      <td><table align="center" border="0">
	<tr><td><font face="verdana">High return pairs:</font></td></tr>
	<tr><td><hr/></td></tr>
	<tr><td><div id='table_div' style="width: 1044px;"></div></td></tr>
      </table></td>
    </tr></table>
  </body>
</html>
<?php mysql_close($link); ?>