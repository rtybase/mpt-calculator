<?php

function getParamString($collection, $excludeCollection = NULL) {
	$ret = "";

	if ($excludeCollection == NULL) {
		foreach ($collection as $key => $val) {
			if (empty($ret)) $ret .= "$key=".$val;
			else $ret .= "&$key=".rawurlencode($val);
		}
	}
	else {
		foreach ($collection as $key => $val) {
			if (!array_key_exists($key, $excludeCollection)) {
				if (empty($ret)) $ret .= "$key=".$val;
				else $ret .= "&$key=".rawurlencode($val);
			}
		}
	}
	return $ret;
}

function printMenu() {
	global $menu_content;

	echo "<div id=\"page_menu\">\n";
	echo "\t<div style=\"float: left;\"><img src=\"/images/tandr.jpg\" border=\"0\"></div>\n";
	echo "\t<ul>\n";
	$i = 0;
	$hasForm = false;
	foreach ($menu_content as $key => $val) {
		if ($i < 6) {
			echo "\t<li><a href=\"$val\">$key</a></li>\n";
		}
		else {
			if (!$hasForm) {
				echo "\t<li><form name=\"menuform\"><select name=\"linktogo\" onchange=\"window.location = document.forms['menuform'].linktogo.value;\">\n";
				echo "\t\t<option value=\"/\">select...</option>\n";
				$hasForm = true;
			}
			echo "\t\t<option value=\"$val\">$key</option>\n";
		}
		$i++;
	}
	if ($hasForm) {
		echo "\t</select></form></li>\n";
	}

	echo "\t</ul>\n";
	echo "</div>\n";
}

function printUserInfo($user) {
	echo "<table border=\"0\" CELLPADDING=\"0\" cellspacing=\"0\">\n";
	echo "<tr><td valign=\"top\"><img src=\"/images/icon/secure.PNG\" border=\"0\" width=\"18\" />&nbsp;</td>\n";
	echo "<td valign=\"top\"><font class=\"smallfont\"><i>Secure material (accessed by <b>$user</b>)</i></font></td></tr>\n";
	echo "</table>\n";
}

function printMessage($message, $url) {
	echo "<table border=\"0\" CELLPADDING=\"0\" cellspacing=\"0\">\n";
	echo "<tr><td valign=\"top\"><font style=\"font-size: 10pt; color:#FF0000;\">$message</font></td></tr>\n";
	echo "<tr><td valign=\"top\"><a href=\"$url\" class=\"smallfont\">&lt;&lt;&nbsp;Back</a></td></tr>\n";
	echo "</table>\n";
}

function printRow($row, $bgcolor, $color) {
	echo "<tr>\r\n";
	foreach ($row as $key => $val) {
		echo "\t<td valign=\"top\"";
		if (is_numeric($val)) echo " align=\"right\"";
		echo empty($bgcolor)?"":" id=\"$bgcolor\"";
		echo ">";
		echo "<font class=\"smallfont\"";
		echo empty($color)?"":" style=\"color:$color;\"";
		echo ">";
		echo empty($val)?"&nbsp;":$val;
		echo "</font></td>\r\n";
	}
	echo "</tr>\r\n";
}

function printCategory($query, $id, $link) {
	$ret = $id;
	$res = mysql_query($query,$link);

	if (!$res) die("Invalid query: ". mysql_error());

	while ($row = mysql_fetch_row($res)) {
		if ($ret == $row[0] || empty($ret)) {
			echo "\t\t<option value=\"".htmlentities($row[0])."\" selected>".htmlentities($row[1]).(empty($row[2])?"":" (".$row[2].")")."</option>\r\n";
			$ret = htmlentities($row[0]);
		}
		else {
			echo "\t\t<option value=\"".htmlentities($row[0])."\">".htmlentities($row[1]).(empty($row[2])?"":" (".$row[2].")")."</option>\r\n";
		}
	}

	mysql_free_result($res);
	return $ret;
}

function printPageCounter($page, $items) {
	global $_GET, $_SERVER, $itemsPerPage;

	$cPages = 0;
	$url = $_SERVER['PHP_SELF'];
	$params = getParamString($_GET, array("page" => NULL, "v" => NULL));

	$cPages = $items * 1.0 / $itemsPerPage;
	
	if ($cPages > 1) {
		for ($i = 0; $i < $cPages; $i++) {
			if ( $i == ($page - 1)) {
				echo "<font class=\"smallfont\" color=\"#0F6BB2\"><b>".($i+1)."</b></font>&nbsp;\r\n";
			}
			else {
				echo "<a class=\"smallfont\" href=\"$url?".((empty($params))?"":$params."&")."page=".($i+1)."\">".($i+1)."</a>&nbsp;\r\n";
			}
		}	
	}
}

function fix_str($str) {
	$str = str_replace("'", "", $str);
	$str = str_replace("%", "", $str);
	$str = str_replace("\"", "", $str);
	return str_replace("\\", "", $str);
}

function makeLink($str) {
	$limit = 140;
	if (strlen($str) <= $limit) return $str;
	return substr($str, 0, $limit)."<br/>".makeLink(substr($str,$limit));
}

function calculateHash($val) {
	return hash("sha512", $val);
}

function validateUser($login, $pwd) {
	global $usr_array;

	$ret = false;

	if (!empty($login)) {
		foreach ($usr_array as $key => $val) {
			if (strcmp(strtolower($login), strtolower($key)) == 0) {
				$hash_pwd = calculateHash($pwd."|".$val["salt"]);
				$ret = (strcmp($hash_pwd, $val["passwordhash"]) == 0);
				break;
			}
		}
	}
	return $ret;
}

?>
