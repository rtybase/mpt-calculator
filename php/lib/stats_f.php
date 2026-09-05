<?php

function mean($arr) {
	if (count($arr) > 0) {
		return array_sum($arr) / count($arr);
	}

	return 0;
}

function std_dev($arr) {
	$num_of_elements = count($arr);
	if ($num_of_elements > 0) {
		$variance = 0.0;
        	$average = mean($arr);

		foreach($arr as $i) {
			$variance += pow(($i - $average), 2);
		}

		return (float) sqrt($variance/$num_of_elements);
	}

	return 0;
}

function correlation($X, $Y, $n) {
	$corr = 0;

	if ($n > 0) {
		$sum_X = 0;
		$sum_Y = 0;
		$sum_XY = 0;
		$squareSum_X = 0;
		$squareSum_Y = 0;

		for ($i = 0; $i < $n; $i++) {
			$sum_X = $sum_X + $X[$i];
			$sum_Y = $sum_Y + $Y[$i];
			$sum_XY = $sum_XY + $X[$i] * $Y[$i];

			$squareSum_X = $squareSum_X + $X[$i] * $X[$i];
			$squareSum_Y = $squareSum_Y + $Y[$i] * $Y[$i];
		}

		$denom = sqrt(($n * $squareSum_X - $sum_X * $sum_X) * ($n * $squareSum_Y - $sum_Y * $sum_Y));

		if ($denom > 0) {
			$corr = (float) ($n * $sum_XY - $sum_X * $sum_Y) / $denom;
		}
	}
    return $corr;
}

function arrayStats($arr) {
	$res = array();
	$res["mean"] = mean($arr);
	$res["std_dev"] = std_dev($arr);
	return $res;
}

function arraysStats($values1, $values2) {
	$res = array();
	$res["correlation"] = correlation($values1, $values2, count($values1));

	$error_arr = array();
	for ($i = 0; $i < count($values1); $i++) {
		$error_arr[$i] = abs($values1[$i] - $values2[$i]);
	}

	$error_stats = arrayStats($error_arr);
	$res["err_mean"] = $error_stats["mean"];
	$res["err_std_dev"] = $error_stats["std_dev"];
	return $res;
}
?>
