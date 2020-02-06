<?php
function connect($db) {
	$link = mysql_connect("localhost", "user", "pwd");
	if (!$link) {
		die('Could not connect: ' . mysql_error());
	}

	if (!mysql_select_db($db,$link)) {
		die('Could not connect: ' . mysql_error());
	}

	mysql_set_charset("utf8", $link);
	return $link;
}

function getCount($query, $link) {
	$cPages = 0;

	$res = mysql_query($query, $link);
	if (!$res) die("Invalid query: ". mysql_error());

	while ($row = mysql_fetch_row($res)) $cPages = $row[0];
	mysql_free_result($res);
	return $cPages;
}

function getSingleValyeByPK($Table, $PKField, $Value, $link) {
	$ret = array();
	$res = mysql_query("select * from $Table where $PKField='$Value'", $link);
	if (!$res) die("Invalid query: ". mysql_error());

	while ($row = mysql_fetch_array($res)) $ret = $row;
	mysql_free_result($res);
	return $ret;
}

?>