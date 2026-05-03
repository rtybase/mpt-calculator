<?php
function connect($db) {
	$link = mysqli_connect("host", "user", "pwd", $db);
	if (!$link) {
		die('Could not connect: ' . mysql_error());
	}

	mysqli_set_charset($link, "utf8");
	return $link;
}

function getCount($query, $link) {
	$cPages = 0;

	$res = mysqli_query($link, $query);
	if (!$res) die("Invalid query: ". mysql_error());

	while ($row = mysqli_fetch_row($res)) $cPages = $row[0];
	mysqli_free_result($res);
	return $cPages;
}

function getSingleValyeByPK($Table, $PKField, $Value, $link) {
	$ret = array();
	$res = mysqli_query($link, "select * from $Table where $PKField='$Value'");
	if (!$res) die("Invalid query: ". mysql_error());

	while ($row = mysqli_fetch_array($res)) $ret = $row;
	mysqli_free_result($res);
	return $ret;
}
?>