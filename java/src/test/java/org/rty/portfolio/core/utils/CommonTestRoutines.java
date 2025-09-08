package org.rty.portfolio.core.utils;

import java.util.Date;

class CommonTestRoutines {
	protected static final Date D_2025_07_17 = dateFrom(17);
	protected static final String TEST_ASSET = "MSFT";

	protected static Date dateFrom(int day) {
		return new Date(125, 6, day);
	}
}
