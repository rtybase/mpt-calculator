package org.rty.portfolio.engine.impl;

import java.net.URLEncoder;

import org.apache.commons.lang.StringEscapeUtils;
import org.junit.Test;

public class LoadYFDataTaskTest {
	@Test
	public void testHtmloDecode() throws Exception {
		System.out.println(URLEncoder.encode(
				StringEscapeUtils.unescapeJava(".JT\u002FCNl8DUi"), "UTF-8"));
	}
}
