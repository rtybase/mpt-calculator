<?php
// Top shift correlations script
	include_once("../lib/mysql.php");
	include_once("../lib/utils.php");
	include_once("./funcs.php");
	header("Content-Type:text/html; charset=UTF-8");

function getTopShiftCorrelations($link) {
	$query = "SELECT a.fk_asset1ID, b.vchr_name, a.fk_asset2ID, a.int_shift, a.dbl_correlation ";
	$query.= "FROM tbl_shift_correlations a, tbl_assets b ";
	$query.= "WHERE a.fk_asset1ID = b.int_assetID ";
	$query.= "AND ((a.int_shift BETWEEN 1 AND 19) OR (a.int_shift BETWEEN -19 AND -1)) ";
	$query.= "AND ABS( a.dbl_correlation ) > 0.5 ";

	$res = mysql_query($query, $link);
	if (!$res) die("Invalid query: ". mysql_error());

	$ret = array();
	$i = 0;
	while ($row = mysql_fetch_row($res)) {
		$ret[$i] = array();

		$ret[$i]["asset1Id"] = $row[0];
		$ret[$i]["asset1Name"] = $row[1];
		addToNameCache($row[0], $row[1]);

		$ret[$i]["asset2Id"] = $row[2];
		$ret[$i]["asset2Name"] = getName($row[2], $link);
		$ret[$i]["shift"] = $row[3];
		$ret[$i]["correlation"] = $row[4];

		$i++;
	}
	mysql_free_result($res);

	return $ret;
}

	$link = connect("portfolio");
	$shiftCorrelations = getTopShiftCorrelations($link);

	$tableResult = "";
	$i = 0;
	foreach ($shiftCorrelations as $key => $value) {
		if ($i == 0) $tableResult.= "[";
		else $tableResult.= ",[";

		$tableResult.= "'".linkToAsset($value["asset1Id"], $value["asset1Name"])."',";
		$tableResult.= "'".linkToAsset($value["asset2Id"], $value["asset2Name"])."',";
		$tableResult.= toChartNumber($value["shift"]).",";
		$tableResult.= toChartNumber(round($value["correlation"], 5)).",";
		$tableResult.= "'<a href=\"./show_sc.php?asset1=".$value["asset1Id"]."&asset2=".$value["asset2Id"]."\">details ...</a>']";
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
		data.setProperty(0, 0, 'style', 'width:345px');
		var table = new google.visualization.Table(document.getElementById(element));
		table.draw(data, {showRowNumber: false, allowHtml: true});
	}

	function generateData() {
		var dataTable = new google.visualization.DataTable();
		dataTable.addColumn('string', 'Asset 1');
		dataTable.addColumn('string', 'Asset 2');
		dataTable.addColumn('number', 'Shift (days)');
		dataTable.addColumn('number', 'Correlation');
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
		<font face="verdana">Shift correlations details:</font>
	</td></tr>
	<tr><td><hr/></td></tr>
	<tr><td><div id='table_div' style="width: 1044px;"></div></td></tr>
      </table></td>
    </tr></table>
  </body>
</html>
<?php mysql_close($link); ?>