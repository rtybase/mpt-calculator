<?php
// ML predictions
	include_once("../lib/mysql.php");
	include_once("../lib/utils.php");
	include_once("./funcs.php");
	header("Content-Type:text/html; charset=UTF-8");

function getPredictions($link) {
	$query = "SELECT a.fk_assetID, c.vchr_name, a.dtm_eps_date, a.int_days_after_eps, ";
	$query.= "    a.dtm_prd_date, a.vchr_model, a.dbl_prd_return, b.dbl_return ";
	$query.= "FROM tbl_predictions a ";
	$query.= "LEFT JOIN tbl_assets c ON a.fk_assetID=c.int_assetID ";
	$query.= "LEFT JOIN tbl_prices b ON a.fk_assetID=b.fk_assetID ";
	$query.= "    AND a.dtm_prd_date=b.dtm_date ";
	$query.= "WHERE  a.dtm_eps_date BETWEEN (NOW() - INTERVAL 60 DAY) AND NOW() ";
	$query.= "ORDER BY a.fk_assetID ASC, a.dtm_prd_date DESC, a.vchr_model ASC";

	$res = mysql_query($query, $link);
	if (!$res) die("Invalid query: ". mysql_error());

	$tableResult = "";
	$i = 0;
	while ($row = mysql_fetch_row($res)) {
		if ($i == 0) $tableResult.= "[";
		else $tableResult.= ",[";

		$tableResult.= "'".linkToAsset($row[0], $row[1])."',";
		$tableResult.= "'".$row[2]."',";
		$tableResult.= toChartNumber($row[3]).",";
		$tableResult.= "'".$row[4]."',";
		$tableResult.= "'".$row[5]."',";
		$tableResult.= toChartNumber(roundOrNull($row[6], 5)).",";
		$tableResult.= toChartNumber(roundOrNull($row[7], 5)).",";
		$tableResult.= "'<a href=\"./show_eps.php?id=".$row[0]."\">details...</a>']";
		$i++;
	}
	mysql_free_result($res);
	return $tableResult;
}

	$link = connect("portfolio");
	$tableResult = getPredictions($link);
?>
<!doctype html>
<html>
  <head>
    <meta charset="UTF-8">
    <style>
	a:link, a:visited, a:active { color:#000000; text-decoration: none; }
	a:hover { color:#000000; text-decoration: underline; }

	#filter_div .google-visualization-controls-categoryfilter,
		.google-visualization-controls-label {
		font-family: Arial, sans-serif;
		font-size: 12px
	}
    </style>

    <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
    <script type='text/javascript'>
	google.charts.load('current', {'packages':['corechart', 'table', 'controls']});
	google.charts.setOnLoadCallback(drawDashboard);

	function drawDashboard() {
		var data = generateDataHeaders();
		data.addRows([<?php echo $tableResult; ?>]);

		var dashboard = new google.visualization.Dashboard(
			document.getElementById('dashboard_div')
		);

		var categoryFilter = new google.visualization.ControlWrapper({
			controlType: 'CategoryFilter',
			containerId: 'filter_div',
			options: {
				filterColumnLabel: 'Model',
				ui: {
					label: 'Filter by model:',
					allowTyping: false,
					allowMultiple: true,
					labelStacking: 'vertical',
					selectedValuesLayout: 'belowWrapping'
				}
			}
		});

		var table = new google.visualization.ChartWrapper({
			chartType: 'Table',
			containerId: 'table_div',
			options: {
				showRowNumber: false,
				allowHtml: true,
				width: '100%',
				height: '100%'
			}
		});

		dashboard.bind(categoryFilter, table);
		dashboard.draw(data);
	}

	function generateDataHeaders() {
		var dataTable = new google.visualization.DataTable();
		dataTable.addColumn('string', 'Asset');
		dataTable.addColumn('string', 'EPS Rep. Date');
		dataTable.addColumn('number', 'Day(s) After EPS');
		dataTable.addColumn('string', 'Prediction Date (incl. hols)');
		dataTable.addColumn('string', 'Model');
		dataTable.addColumn('number', 'Predicted Return');
		dataTable.addColumn('number', 'Actual Return');
		dataTable.addColumn('string', 'More');
		return dataTable;
	}
    </script>
  </head>
  <body>
    <table align="center" border="0"><tr>
      <td valign="top"><?php showMenu(); ?></td>
      <td><div id="dashboard_div"><table align="center" border="0">
	<tr><td align="left"><font face="verdana">Returns predictions from EPS, check <a href="./ml_q.php">ML-Quality</a>:</font></td></tr>
	<tr><td><hr/></td></tr>
	<tr><td><div id="filter_div"></div></td></tr>
	<tr><td><div id="table_div" style="width: 1040px;"></div></td></tr>
      </table></div></td>
    </tr></table>
  </body>
</html>
<?php mysql_close($link); ?>