<?php
// All pairs script
	include_once("../lib/mysql.php");
	include_once("../lib/utils.php");
	include_once("./funcs.php");
	header("Content-Type:text/html; charset=UTF-8");

function getAllCorrelations($assetId, $assetName, $link) {
	$query = "SELECT C.fk_asset1ID, C.fk_asset2ID, A.vchr_name, C.dbl_correlation, ";
	$query.= "C.dbl_weight1, C.dbl_weight2, C.dbl_portret, C.dbl_portvar ";
	$query.= "FROM  tbl_correlations as C, tbl_assets as A ";
	$query.= "WHERE ((C.fk_asset1ID=$assetId) OR (C.fk_asset2ID=$assetId)) ";
	$query.= "AND C.fk_asset2ID=A.int_assetID ";
	$query.= "ORDER BY C.dbl_portret DESC ";

	return getCollection($query, $assetId, $assetName, $link);
}

	$id = (int) $_GET["id"];
	if ($id < 1) $id = 1;

	$link = connect("portfolio");
	$mainAsset = getName($id, $link);

	$allCorrelation = getAllCorrelations($id, $mainAsset, $link);
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
	google.charts.load('current', {'packages':['orgchart', 'table']});
	google.charts.setOnLoadCallback(generateTable);

	function generateTable() {
		var data = generateData();
		data.addRows([<?php showData($allCorrelation); ?>]);
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
      <td valign="top"><a href="./?id=<?php echo $id; ?>"><i>Back</i></a><br/>
		<?php showMenu(); ?>
      </td>
      <td><table align="center" border="0">
	<tr><td><font face="verdana">All <?php echo count($allCorrelation); ?> correlations for : <?php echo $mainAsset; ?></font></td></tr>
	<tr><td><hr/></td></tr>
	<tr><td><div id='table_div' style="width: 1044px;"></div></td></tr>
      </table></td>
    </tr></table>
  </body>
</html>
<?php mysql_close($link); ?>