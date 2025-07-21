package org.rty.portfolio.engine;

import java.util.Map;

public interface Task {
	String getName();

	void execute(Map<String, String> parameters) throws Throwable;
}
