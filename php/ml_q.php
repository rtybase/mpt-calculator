<?php
// ML quality metrics
	include_once("../lib/mysql.php");
	include_once("../lib/utils.php");
	include_once("./funcs.php");
	header("Content-Type:text/html; charset=UTF-8");

function chartHeaderFrom($models, $metric) {
	$result = "[{label: 'Date', id: 'Date', type: 'string'}";

	foreach ($models[$metric] as $model => $val) {
		$result .= ",{label: '".$model."', id: '".$model."', type: 'number'}";
	}
	$result .= "]";

	return $result;
}

function chartDataFrom($metrciValues) {
	$result = "";
	foreach ($metrciValues as $date => $modelValues) {
		$result.= ",['".$date."'";
		foreach ($modelValues as $metric => $val) {
			$result .= ",".valueOrNullFrom($val);
		}
		$result.= "]";
	}

	return $result;
}

	$link = connect("portfolio");

	$query = "SELECT int_dataset, vchr_metric, dtm_report_date, bln_after_retrain, vchr_model, dbl_result ";
	$query.= "FROM tbl_ml_quality ";
	$query.= "ORDER BY int_dataset, vchr_metric, dtm_report_date, bln_after_retrain, vchr_model, dbl_result";

	$res = mysql_query($query, $link);
	if (!$res) die("Invalid query: ". mysql_error());

	$models = array();
	$table = array();
	while ($row = mysql_fetch_row($res)) {
		$data_set = $row[0];
		$metric = $row[1];
		$date = $row[2];
		$model = $row[4];
		$result = $row[5];

		if (!array_key_exists($data_set, $table)) {
			$table[$data_set] = array();
		}

		if (!array_key_exists($metric, $table[$data_set])) {
			$table[$data_set][$metric] = array();
		}

		if (!array_key_exists($date, $table[$data_set][$metric])) {
			$table[$data_set][$metric][$date] = array();
		}

		$table[$data_set]["retrain"][$date]["all"] = $row[3];
		$table[$data_set][$metric][$date][$model] = $result;


		if (!array_key_exists($metric, $models)) {
			$models[$metric] = array();
		}
		
		$models["retrain"]["all"] = 1;
		$models[$metric][$model] = 1;
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
	google.charts.load('current', {'packages':['corechart']});
	google.charts.setOnLoadCallback(drawCharts);

	function drawCharts() {
<?php
	$i = 1;
	foreach ($table as $dataset => $dataset_value) {
		foreach ($dataset_value as $metric => $value) {
			echo "\t\tdrawChart".$i."();\n";
			$i++;
		}
	}
?>
	}


<?php
	$i = 1;
	foreach ($table as $dataset => $dataset_value) {
		foreach ($dataset_value as $metric => $value) {
			echo "\tfunction drawChart".$i."() {\n";

			if (!empty($value)) {
				echo "\t\tvar data = google.visualization.arrayToDataTable([\n";
				echo "\t\t".chartHeaderFrom($models, $metric)."\n";
				echo "\t\t".chartDataFrom($value)."\n";
				echo "\t\t]);\n";
				echo "\t\tvar options = {\n";
				echo "\t\t\ttitle: \"dataset = $dataset, metric = $metric\",\n";
				echo "\t\t\tinterpolateNulls: true,\n";
				echo "\t\t\texplorer: {\n";
				echo "\t\t\tactions: ['dragToZoom', 'rightClickToReset'],\n";
				echo "\t\t\tkeepInBounds: true\n";
				echo "\t\t\t}\n";
				echo "\t\t};\n";
				echo "\t\tvar chart = new google.visualization.LineChart(document.getElementById('chart".$i."_div'));\n";
				echo "\t\tchart.draw(data, options);\n";
			}

			echo "\t}\n\n";
			$i++;
		}
	}
?>
    </script>
  </head>
  <body>
    <table align="center" border="0"><tr>
      <td valign="top"><?php showMenu(); ?></td>
      <td><table align="center" border="0">
	<tr><td  align="left"><font face="verdana">ML quality metrics:</font></td></tr>
	<tr><td><hr/></td></tr>
<?php
	$i = 1;
	foreach ($table as $dataset => $dataset_value) {
		foreach ($dataset_value as $metric => $value) {
			echo "\t<tr><td><font face=\"verdana\">dataset=".$dataset.", metric=".$metric.":</font><div id=\"chart".$i."_div\" style=\"width: 1044px; height: 350px;\"></div></td></tr>\n";
			$i++;
		}
	}
?>
      </table></td>
    </tr></table>
  </body>
</html>
<?php mysql_close($link); ?>