<?php
	$RETURN_ROUND_PRECISION = 5;
	$VOLATILITY_ROUND_PRECISION = 4;
	$ASSET_NAMES_CACHE = array();

	$PERIODS = array("1w", "1m", "6m", "1y", "2y", "5y");

	function periodTableFrom($period) {
		global $PERIODS;

		if (in_array($period, $PERIODS)) {
			return "_".$period;
		}
		return "";
	}

	function indicatorText($text, $indicator) {
		if ($indicator < 0) {
			return "<font color=\"red\">".$text."</font>";
		} else if ($indicator > 0) {
			return "<font color=\"green\">".$text."</font>";
		}
		return $text;
	}

	function getName($assetId, $link) {
		global $ASSET_NAMES_CACHE;
		if (array_key_exists($assetId, $ASSET_NAMES_CACHE)) {
			return $ASSET_NAMES_CACHE[$assetId];
		} else { 
			$assetRecord = getSingleValyeByPK("tbl_assets", "int_assetID", $assetId, $link);
			addToNameCache($assetId, $assetRecord["vchr_name"]);
			return $assetRecord["vchr_name"];
		}
	}

	function addToNameCache($assetId, $assetName) {
		global $ASSET_NAMES_CACHE;
		$ASSET_NAMES_CACHE[$assetId] = $assetName;
	}

	function printAssets($assetId, $link) {
		$query = "select int_assetID, vchr_name, vchr_type from tbl_assets order by vchr_type asc, vchr_name asc";
		$res = mysql_query($query,$link);

		if (!$res) die("Invalid query: ". mysql_error());

		$lastType = "";
		while ($row = mysql_fetch_row($res)) {
			if ($lastType != $row[2]) {
				echo "<option value=\"-1\">--------------------&lt;&lt;&lt; ".htmlentities($row[2])." &gt;&gt;&gt;--------------------</option>\r\n";
			}

			if ($assetId == $row[0]) {
				echo "<option value=\"$row[0]\" selected>".htmlentities($row[1])."</option>\r\n";
			} else {
				echo "<option value=\"$row[0]\">".htmlentities($row[1])."</option>\r\n";
			}

			$lastType = $row[2];
		}

		mysql_free_result($res);
	}

	function getLastPriceInfo($assetId, $link) {
		$ret = array();
		$res = mysql_query("select * from tbl_prices where fk_assetID=$assetId order by dtm_date desc limit 0,1", $link);
		if (!$res) die("Invalid query: ". mysql_error());
		while ($row = mysql_fetch_array($res)) $ret = $row;
		mysql_free_result($res);
		return $ret;
	}

	function getCollection($query, $id, $mainAsset, $link) {
		global $RETURN_ROUND_PRECISION, $VOLATILITY_ROUND_PRECISION;
		$ret = array();
		$res = mysql_query($query, $link);
		if (!$res) die("Invalid query: ". mysql_error());
		$i = 0;
		while ($row = mysql_fetch_row($res)) {
			$ret[$i] = array();
			$ret[$i]["correlation"] = round($row[3], $VOLATILITY_ROUND_PRECISION);
			$ret[$i]["portret"] = round($row[6], $RETURN_ROUND_PRECISION);
			$ret[$i]["portvol"] = volatilityFrom($row[7]);
			if ($row[0] == $id) {
				$ret[$i]["asset1"] = $mainAsset;
				$ret[$i]["asset2"] = $row[2];
				$ret[$i]["asset2Id"] = $row[1];
				$ret[$i]["weight1"] = round($row[4], 7);
				$ret[$i]["weight2"] = round($row[5], 7);
			} else {
				$ret[$i]["asset1"] = $row[2];
				$ret[$i]["asset2"] = getName($row[0], $link);
				$ret[$i]["asset2Id"] = $row[0];
				$ret[$i]["weight1"] = round($row[5], 7);
				$ret[$i]["weight2"] = round($row[4], 7);
			} 
			$i++;
		}
		mysql_free_result($res);
		return $ret;
	}

	function toChartNumber($number) {
		if (!is_numeric($number)) {
			return "null";
		}
		return "{v: ".$number.", f: '".$number."'}";
	}

	function valueOrNullFrom($value) {
		if (is_numeric($value)) {
			return "".$value;
		} else if (empty($value)) {
			return "null";
		}
		return "".$value;
	}

	function roundOrNull($number, $precision) {
		if (!is_numeric($number)) {
			return NULL;
		}
		return round($number, $precision);
	}

	function booleanValueOrNull($value) {
		if ($value) {
			return "'Y'";
		}

		return "'N'";
	}

	function showData($data) {
		$size = count($data);
		$link = basename($_SERVER['PHP_SELF']);
		for ($i = 0; $i < $size; $i++) {
			if ($i == 0) echo "[";
			else echo ",[";

			echo "'<i><font color=\"#5D6D7E\">"
				.addslashes($data[$i]["asset1"])."</font></i><br/>"
				."<a href=\"./".$link."?id=".addslashes($data[$i]["asset2Id"])."\">".addslashes($data[$i]["asset2"])."</a>',";
			echo toChartNumber($data[$i]["correlation"]).",";
			echo "'<i><font color=\"#5D6D7E\">"
				.percentWeightFrom($data[$i]["weight1"])."&percnt;</font></i><br/>"
				.percentWeightFrom($data[$i]["weight2"])."&percnt;',";
			echo toChartNumber($data[$i]["portret"]).",";
			echo toChartNumber($data[$i]["portvol"])."]";
		}
	}

	function getCollectionFor($criteria, $orderBy, $id, $mainAsset, $link) {
		$query = "SELECT C.fk_asset1ID, C.fk_asset2ID, A.vchr_name, C.dbl_correlation, ";
		$query.= "C.dbl_weight1, C.dbl_weight2, C.dbl_portret, C.dbl_portvar ";
		$query.= "FROM  tbl_correlations as C, tbl_assets as A ";
		$query.= "WHERE ((C.fk_asset1ID=$id) OR (C.fk_asset2ID=$id)) ";
		$query.= "AND C.fk_asset2ID=A.int_assetID ";
		if (!empty($criteria)) {
			$query.= "AND ".$criteria." ";
		}
		if (!empty($orderBy)) {
			$query.= "ORDER BY ".$orderBy." ";
		}
		$query.= "LIMIT 0,10";
		return getCollection($query, $id, $mainAsset, $link);
	}

	function getRelatedData($assetId1, $assetId2, $link) {
		$query = "SELECT * ";
		$query.= "FROM  tbl_correlations ";
		$query.= "WHERE ((fk_asset1ID=$assetId1 and fk_asset2ID=$assetId2) OR (fk_asset1ID=$assetId2 and fk_asset2ID=$assetId1)) ";

		$ret = array();
		$res = mysql_query($query, $link);
		if (!$res) die("Invalid query: ". mysql_error());

		while ($row = mysql_fetch_array($res)) $ret = $row;
		mysql_free_result($res);
		return $ret;
	}

	function calculateBeta($assetId, $marketIndexId, $link) {
		global $RETURN_ROUND_PRECISION;
		if ($assetId != $marketIndexId) {
			$marketIndexData = getSingleValyeByPK("tbl_avgreturns", "fk_assetID", $marketIndexId, $link);
			$relatedData = getRelatedData($assetId, $marketIndexId, $link);
			if (!empty($relatedData)) {
				$covariance = $relatedData["dbl_covariance"];
				return round($covariance / $marketIndexData["dbl_varience"], $RETURN_ROUND_PRECISION);
			}
		}
		return "n/a";
	}

	function calculateKellyFraction($expectedReturn, $variance) {
		global $RETURN_ROUND_PRECISION;
		return round($expectedReturn / $variance, $RETURN_ROUND_PRECISION);
	}

	function showMenu() {
 ?>
	<a href="./">Home</a><br/>
	<a href="./top_r.php">Top returns</a><br/>
	<a href="./top_p.php">Top pairs</a><br/>
	<a href="./top_d.php">Dividends</a><br/>
	<a href="./top_eps.php">EPS</a><br/>
	<a href="./port_f.php">Custom portfolios</a><br/>
	<a href="./top_sc.php">Shift correlations</a><br/>
	<a href="./preds.php">Predictions</a><br/>
	<a href="./stale_d.php">Stale data</a>
<?php	}

	function linkToAsset($id, $name, $escape = true) {
                if ($escape) {
			return "<a href=\"./?id=".$id."\">".addslashes($name)."</a>";
		}
		return "<a href=\"./?id=".$id."\">".$name."</a>";
	}

	function volatilityFrom($variance) {
		global $VOLATILITY_ROUND_PRECISION;
		return round(sqrt(abs($variance)), $VOLATILITY_ROUND_PRECISION);
	}

	function percentWeightFrom($value) {
		return round($value * 100, 3);
	}

	function nextDateFrom($fromDate) {
		return date('Y-m-d', strtotime("+1 day", strtotime($fromDate)));
	}

	function nextPriceFrom($lastPrice, $returnRate) {
		return $lastPrice * (1 + $returnRate/100.0);
	}

	function getStockDetails($stock, $link) {
		$ret = array();
		if (!empty($stock)) {
			$query = "SELECT a.vchr_symbol, b.vchr_name, c.vchr_name ";
			$query.= "FROM tbl_stocks a, tbl_sectors b, tbl_industries c ";
			$query.= "WHERE a.fk_sectorID=b.int_sectorID AND ";
			$query.= "a.fk_industryID=c.int_industryID AND ";
			$query.= "a.vchr_symbol='".$stock."'";

			$res = mysql_query($query, $link);
			if (!$res) die("Invalid query: ". mysql_error());

			while ($row = mysql_fetch_array($res)) $ret = $row;
			mysql_free_result($res);
		}
		return $ret;
	}

	function loadDividendsFor($assetId, $link) {
		$res = mysql_query("select dtm_date, dbl_pay from tbl_dividends where fk_assetID=$assetId order by dtm_date asc", $link);

		if (!$res) die("Invalid query: ". mysql_error());

		$result = array();
		while ($row = mysql_fetch_array($res)) {
			$result[$row[0]] = $row[1];
		}
		mysql_free_result($res);

		return $result;
	}

	function loadEpsFor($assetId, $link) {
		$res = mysql_query("select dtm_date, dbl_eps, dbl_prd_eps from tbl_eps where fk_assetID=$assetId order by dtm_date asc", $link);

		if (!$res) die("Invalid query: ". mysql_error());

		$result = array();
		while ($row = mysql_fetch_array($res)) {
			$result[$row[0]] = array();
			$result[$row[0]]["eps"] = $row[1];
			$result[$row[0]]["eps_predicted"] = $row[2];
		}
		mysql_free_result($res);

		return $result;
	}

	function loadEarningsFor($assetId, $link) {
		$res = mysql_query("select dtm_date, dbl_eps from tbl_earnings where fk_assetID=$assetId order by dtm_date asc", $link);

		if (!$res) die("Invalid query: ". mysql_error());

		$result = array();
		while ($row = mysql_fetch_array($res)) {
			$result[$row[0]] = $row[1];
		}
		mysql_free_result($res);

		return $result;
	}

	function mergeDivsAndEps($assetId, $link) {
		$eps = loadEpsFor($assetId, $link);
		$dividends = loadDividendsFor($assetId, $link);
		$earnings = loadEarningsFor($assetId, $link);

		$result = $eps;

		foreach ($dividends as $key => $value) {
			if (!array_key_exists($key, $result)) {
				$result[$key] = array();
			}

			$result[$key]["dividend"] = $value;
		}

		foreach ($earnings as $key => $value) {
			if (!array_key_exists($key, $result)) {
				$result[$key] = array();
			}

			$result[$key]["eps_eofp"] = $value;
		}

		ksort($result);
		return $result;
	}
?>