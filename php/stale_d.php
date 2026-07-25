<?php
	include_once("./lib/mysql.php");
	include_once("./lib/utils.php");
	include_once("./lib/funcs.php");
	header("Content-Type:text/html; charset=UTF-8");

function tableDataFrom($query, $link) {
	$tableResult = "";
	$res = mysqli_query($link, $query);

	if (!$res) die("Invalid query: ". mysqli_error());

	$i = 0;
	while ($row = mysqli_fetch_row($res)) {
		if ($i == 0) $tableResult.= "[";
		else $tableResult.= ",[";

		$tableResult.= "'".linkToAsset($row[0], $row[1])."','";
		$tableResult.= $row[2]."']";
		$i++;
	}

	mysqli_free_result($res);
	return $tableResult;
}

	$link = connect("portfolio");

	$queryStale = "SELECT b.fk_assetID, a.vchr_name, b.dtm_max_date ";
	$queryStale.= "FROM tbl_assets a, ( ";
	$queryStale.= "	SELECT fk_assetID,  MAX( dtm_date ) as dtm_max_date ";
	$queryStale.= "	FROM tbl_prices ";
	$queryStale.= "	GROUP BY fk_assetID ";
	$queryStale.= ") b ";
	$queryStale.= "WHERE a.int_assetID=b.fk_assetID ";
	$queryStale.= "AND a.bln_deleted = 0 ";
	$queryStale.= "AND DATEDIFF(NOW(), b.dtm_max_date) > 5 ";
	$queryStale.= "ORDER BY b.dtm_max_date DESC, a.vchr_name ASC ";

	$tableStaleResult = tableDataFrom($queryStale, $link);

	$queryDeleted = "SELECT b.fk_assetID, a.vchr_name, b.dtm_max_date ";
	$queryDeleted.= "FROM tbl_assets a, ( ";
	$queryDeleted.= "	SELECT fk_assetID,  MAX( dtm_date ) as dtm_max_date ";
	$queryDeleted.= "	FROM tbl_prices ";
	$queryDeleted.= "	GROUP BY fk_assetID ";
	$queryDeleted.= ") b ";
	$queryDeleted.= "WHERE a.int_assetID=b.fk_assetID ";
	$queryDeleted.= "AND a.bln_deleted = 1 ";
	$queryDeleted.= "ORDER BY b.dtm_max_date DESC, a.vchr_name ASC ";

	$tableDeletedResult = tableDataFrom($queryDeleted, $link);
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
		var dataForStale = generateData();
		dataForStale.addRows([<?php echo $tableStaleResult; ?>]);
		drawTable('table1_div', dataForStale);

		var dataForDeleted = generateData();
		dataForDeleted.addRows([<?php echo $tableDeletedResult; ?>]);
		drawTable('table2_div', dataForDeleted);
	}

	function drawTable(element, data) {
		data.setProperty(0, 0, 'style', 'width:1000px');
		var table = new google.visualization.Table(document.getElementById(element));
		table.draw(data, {showRowNumber: false, allowHtml: true});
	}

	function generateData() {
		var dataTable = new google.visualization.DataTable();
		dataTable.addColumn('string', 'Asset');
		dataTable.addColumn('string', 'Last Update');
		return dataTable;
	}

    </script>
  </head>
  <body>
    <table align="center" border="0"><tr>
      <td valign="top"><?php showMenu(); ?></td>
      <td><table align="center" border="0">
	<tr><td><font face="verdana">Assets with stale data</font></td></tr>
	<tr><td><hr/></td></tr>
	<tr><td><div id='table1_div' style="width: 1044px;"></div></td></tr>

	<tr><td><hr/></td></tr>
	<tr><td><font face="verdana">Deleted assets</font></td></tr>
	<tr><td><hr/></td></tr>
	<tr><td><div id='table2_div' style="width: 1044px;"></div></td></tr>
      </table></td>
    </tr></table>
  </body>
</html>
<?php mysqli_close($link); ?>