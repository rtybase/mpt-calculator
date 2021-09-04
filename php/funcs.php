<?php
	$RETURN_ROUND_PRECISION = 5;
	$VOLATILITY_ROUND_PRECISION = 4;

	function indicatorText($text, $indicator) {
		if ($indicator < 0) {
			return "<font color=\"red\">".$text."</font>";
		} else if ($indicator > 0) {
			return "<font color=\"green\">".$text."</font>";
		}
		return $text;
	}

	function getName($assetId, $link) {
		$asset = getSingleValyeByPK("tbl_assets", "int_assetID", $assetId, $link);
		return $asset["vchr_name"];
	}

	function printAssets($assetId, $link) {
		$query = "select int_assetID, vchr_name from tbl_assets order by vchr_name asc";
		printCategory($query, $assetId, $link);
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
			$ret[$i]["portvol"] = round(sqrt(abs($row[7])), $VOLATILITY_ROUND_PRECISION);
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
		return "{v: ".$number.", f: '".$number."'}";
	}

	function showData($data) {
		$size = count($data);
		$link = basename($_SERVER['PHP_SELF']);
		for ($i = 0; $i < $size; $i++) {
			if ($i == 0) echo "[";
			else echo ",[";

			echo "'<i><font color=\"#5D6D7E\">"
				.$data[$i]["asset1"]."</font></i><br/>"
				."<a href=\"./".$link."?id=".$data[$i]["asset2Id"]."\">".$data[$i]["asset2"]."</a>',";
			echo toChartNumber($data[$i]["correlation"]).",";
			echo "'<i><font color=\"#5D6D7E\">"
				.round($data[$i]["weight1"] * 100, 3)."&percnt;</font></i><br/>"
				.round($data[$i]["weight2"] * 100, 3)."&percnt;',";
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
		$query.= "LIMIT 0,15";
		return getCollection($query, $id, $mainAsset, $link);
	}
?>