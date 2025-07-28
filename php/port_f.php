<?php
// Top rates script
	include_once("../lib/mysql.php");
	include_once("../lib/utils.php");
	include_once("./funcs.php");
	header("Content-Type:text/html; charset=UTF-8");

function printPortfolios($portfolioId, $link) {
	$query = "select int_portfolioID, vchr_name from tbl_custom_portfolios order by vchr_name asc";
	printCategory($query, $portfolioId, $link);
}

function getPortfolioInfo($portfolioId, $link) {
	$query = "SELECT vchr_name, txt_json_composition FROM tbl_custom_portfolios ";
	$query.= "WHERE int_portfolioID=$portfolioId";

	$res = mysql_query($query, $link);
	if (!$res) die("Invalid query: ". mysql_error());

	$details = array();
	while ($row = mysql_fetch_row($res)) {
		$details["name"] = $row[0];
		$details["composition"] = json_decode($row[1], true);
	}
	mysql_free_result($res);
	return $details;
}

function getAssetNamesFromIds($assetIds, $link) {
	$names = array();
	for ($i = 0; $i < count($assetIds); $i++) {
		$names[$i] = getName($assetIds[$i], $link);
	}
	return $names;
}

function getAssetNamesAsLinks($assetIds, $assetNames) {
	$size = min(count($assetIds), count($assetNames));

	$result = "";
	for ($i = 0; $i < $size; $i++) {
		$result.= linkToAsset($assetIds[$i], $assetNames[$i])." ";
	}
	return $result;
}

function getPortfolioData($portfolioId, $link) {
	$query = "SELECT dtm_date, txt_json_stats FROM tbl_custom_portfolios_data ";
	$query.= "WHERE fk_portfolioID=$portfolioId ORDER by dtm_date ASC";

	$res = mysql_query($query, $link);
	if (!$res) die("Invalid query: ". mysql_error());

	$data = array();
	$i = 0;
	while ($row = mysql_fetch_row($res)) {
		$data[$i] = array();
		$data[$i]["date"] = $row[0];
		$data[$i]["data"] = json_decode($row[1], true);

		$i++;
	}
	mysql_free_result($res);
	return $data;
}


	$id = (int) $_GET["id"];
	if ($id < 1) $id = 1;

	$link = connect("portfolio");

	$info = getPortfolioInfo($id, $link);
	$assetNames = getAssetNamesFromIds($info["composition"], $link);
	$data = getPortfolioData($id, $link);
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

	google.load("visualization", "1.1", {packages:["corechart"]});
	google.setOnLoadCallback(drawChart);

	function generateTable() {
		var data = generateData();
		data.addRows([
<?php echo "['".$info["name"]."','".getAssetNamesAsLinks($info["composition"], $assetNames)."']"; 
?>]);
		drawTable('table_div', data);
	}

	function drawTable(element, data) {
		data.setProperty(0, 0, 'style', 'width:345px');
		var table = new google.visualization.Table(document.getElementById(element));
		table.draw(data, {showRowNumber: false, allowHtml: true});
	}

	function generateData() {
		var dataTable = new google.visualization.DataTable();
		dataTable.addColumn('string', 'Portfolio name');
		dataTable.addColumn('string', 'Assets');
		return dataTable;
	}

	function drawChart() {
		drawChart1();
		drawChart2();
	}

	function drawChart1() {
		var data = google.visualization.arrayToDataTable([
			['Date', 'Return', 'Volatility']
<?php
	for ($i = 0; $i < count($data); $i++) {
		echo ",['".$data[$i]["date"]."',".$data[$i]["data"]["portfolioReturn"].",".volatilityFrom($data[$i]["data"]["porfolioVariance"])."]";
	}
?>
		]);

		var options = {
			title: "Evolution of optimal returns"
		};

		var chart = new google.visualization.LineChart(document.getElementById('chart1_div'));
		chart.draw(data, options);
	}

	function drawChart2() {
		var data = google.visualization.arrayToDataTable([
			['Date'
<?php
	for ($i = 0; $i < count($assetNames); $i++) {
		echo ",'".$assetNames[$i]."'";
	}
?>
			]

<?php
	for ($i = 0; $i < count($data); $i++) {
		echo ",['".$data[$i]["date"]."'";
		$weights = $data[$i]["data"]["portfolioWeights"];
		for ($j = 0; $j < count($weights); $j++) {
			echo ",".percentWeightFrom($weights[$j]);
		}
		echo "]";

	}
?>
		]);

		var options = {
			title: "Evolution of optimal allocations"
		};

		var chart = new google.visualization.LineChart(document.getElementById('chart2_div'));
		chart.draw(data, options);
	}
    </script>
  </head>
  <body>
    <table align="center" border="0"><tr>
      <td valign="top"><?php showMenu(); ?></td>
      <td><table align="center" border="0">
	<tr><td  align="right">
		<form name="main" method="GET" action="./<?php echo basename($_SERVER['PHP_SELF']);?>">
		<font face="verdana">Portfolios:
			<select name="id" onchange="document.forms['main'].submit();">
			<?php printPortfolios($id, $link);?>
			</select>
		period:</font>
		</form>
	</td></tr>
	<tr><td><hr/></td></tr>
	<tr><td><div id='table_div' style="width: 1044px;"></div></td></tr>
	<tr><td><div id="chart1_div" style="width: 1044px; height: 250px;"></div></td></tr>
	<tr><td><div id="chart2_div" style="width: 1044px; height: 1050px;"></div></td></tr>
      </table></td>
    </tr></table>
  </body>
</html>
<?php mysql_close($link); ?>