<?php
// Top f-scores and related P/E script
	include_once("../lib/mysql.php");
	include_once("../lib/utils.php");
	include_once("./funcs.php");
	header("Content-Type:text/html; charset=UTF-8");

	$link = connect("portfolio");

	$query = "SELECT a.int_assetID, a.vchr_name, a.vchr_symbol, ";
	$query.= "	b.dbl_fscore, b.dtm_date as score_date, ";
	$query.= "	c.dbl_price, c.dtm_date as price_date, ";
	$query.= "	d.dbl_eps, d.dtm_date as eps_date ";
	$query.= "FROM tbl_assets a USE INDEX (idx_tbl_assets_assetID_name_symbol_type) ";
	$query.= "INNER JOIN ( ";
	$query.= "	SELECT e.vchr_symbol, e.dbl_fscore, e.dtm_date FROM tbl_fscores e ";
	$query.= "	INNER JOIN ( ";
	$query.= "		SELECT vchr_symbol, max( dtm_date ) dtm_date ";
	$query.= "		FROM tbl_fscores GROUP BY vchr_symbol) f ";
	$query.= "	ON e.vchr_symbol = f.vchr_symbol AND e.dtm_date = f.dtm_date ";
	$query.= ") b ";
	$query.= "ON a.vchr_symbol = b.vchr_symbol ";
	$query.= "INNER JOIN ( ";
	$query.= "	SELECT g.fk_assetID, g.dbl_price, g.dtm_date FROM tbl_prices g ";
	$query.= "	INNER JOIN ( ";
	$query.= "		SELECT fk_assetID, max(dtm_date) dtm_date ";
	$query.= "		FROM tbl_prices GROUP BY fk_assetID) h ";
	$query.= "	ON g.fk_assetID = h.fk_assetID AND g.dtm_date = h.dtm_date ";
	$query.= ") c ";
	$query.= "ON a.int_assetID = c.fk_assetID ";
	$query.= "INNER JOIN ( ";
	$query.= "	SELECT i.fk_assetID, i.dbl_eps, i.dtm_date FROM tbl_eps i ";
	$query.= "	INNER JOIN ( ";
	$query.= "		SELECT fk_assetID, MAX( dtm_date ) dtm_date ";
	$query.= "		FROM tbl_eps GROUP BY fk_assetID) j ";
	$query.= "	ON i.fk_assetID = j.fk_assetID AND i.dtm_date = j.dtm_date ";
	$query.= ") d ";
	$query.= "ON a.int_assetID = d.fk_assetID ";
	$query.= "WHERE a.vchr_type = 'Stock'";

	$roundPrecision = 2;
	$tableResult = "";
	$res = mysql_query($query, $link);

	if (!$res) die("Invalid query: ". mysql_error());
	$i = 0;
	while ($row = mysql_fetch_row($res)) {
		if ($i == 0) $tableResult.= "[";
		else $tableResult.= ",[";

		$tableResult.= "'".linkToAsset($row[0], $row[1])."',";
		$tableResult.= "'".$row[2]."',";

		$price = $row[5];
		$eps = $row[7];
		$tableResult.= toChartNumber(round(priceOverEarnings($price, $eps), $roundPrecision)).",";

		$tableResult.= toChartNumber(round($row[3], $roundPrecision)).",";
		$tableResult.= "'".$row[4]."',";

		$tableResult.= toChartNumber(round($price, $roundPrecision)).",";
		$tableResult.= "'".$row[6]."',";

		$tableResult.= toChartNumber(round($eps, $roundPrecision)).",";
		$tableResult.= "'".$row[8]."']";

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
		dataTable.addColumn('number', 'P/E');

		dataTable.addColumn('number', 'F-Score');
		dataTable.addColumn('string', 'Score Date');

		dataTable.addColumn('number', 'Price');
		dataTable.addColumn('string', 'Price Date');

		dataTable.addColumn('number', 'EPS');
		dataTable.addColumn('string', 'EPS Date');
		return dataTable;
	}
    </script>
  </head>
  <body>
    <table align="center" border="0"><tr>
      <td valign="top"><?php showMenu(); ?></td>
      <td><table align="center" border="0">
	<tr><td align="left">
		<font face="verdana">F-Score and P/E details for <?php echo $i ?> assets:</font>
	</td></tr>
	<tr><td><hr/></td></tr>
	<tr><td><div id='table_div' style="width: 1044px;"></div></td></tr>
      </table></td>
    </tr></table>
  </body>
</html>
<?php mysql_close($link); ?>