package org.rty.portfolio;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.rty.portfolio.db.DbManager;
import org.rty.portfolio.engine.Task;
import org.rty.portfolio.engine.impl.dbtask.Calculate2AssetsPortfolioStatsTask;
import org.rty.portfolio.engine.impl.dbtask.CalculateAssetStatsTask;
import org.rty.portfolio.engine.impl.dbtask.CalculateAssetsShiftCorrelationTask;
import org.rty.portfolio.engine.impl.dbtask.CalculateMultiAssetsPortfolioStatsTask;
import org.rty.portfolio.engine.impl.dbtask.OptimalPortfolioFinderTask;
import org.rty.portfolio.engine.impl.dbtask.load.LoadDividendsToDbTask;
import org.rty.portfolio.engine.impl.dbtask.load.LoadEarningsToDbTask;
import org.rty.portfolio.engine.impl.dbtask.load.LoadEpsToDbTask;
import org.rty.portfolio.engine.impl.dbtask.load.LoadPricesToDbTask;
import org.rty.portfolio.engine.impl.nettask.DownloadTask;
import org.rty.portfolio.engine.impl.transform.TransformDividendsDataTask;
import org.rty.portfolio.engine.impl.transform.TransformEcbRatesTask;
import org.rty.portfolio.engine.impl.transform.TransformEpsDataForTrainingTask;
import org.rty.portfolio.engine.impl.transform.TransformSeriesDataTask;
import org.rty.portfolio.engine.impl.transform.TransformStdLifeJsonDataTask;

import com.google.common.base.Strings;

public class TaskExecutor {
	private static final Map<String, Task> tasksMap = new HashMap<>();
	private static final String PROP_FILE = "app.properties";
	private static final String CONN_STRING = "connection_string";
	private static DbManager dbManager;

	public static void main(String[] args) throws Throwable {
		if (args.length > 0) {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			initialiseDbManager(loadProperties());
			registerAllTasks();
			execute(args[0], extractParameters(args));
			dbManager.close();
		} else {
			throw new Exception(String.format("task is not specified!"));
		}
	}

	private static void initialiseDbManager(Properties props) throws Exception {
		String connectionString = props.getProperty(CONN_STRING);

		if (Strings.isNullOrEmpty(connectionString)) {
			throw new Exception(String.format("'%s' not found!", PROP_FILE));
		}

		Connection conn = DriverManager.getConnection(connectionString);
		dbManager = new DbManager(conn);
	}

	private static void registerAllTasks() {
		registerTask(new CalculateAssetStatsTask(dbManager));
		registerTask(new TransformStdLifeJsonDataTask());
		registerTask(new Calculate2AssetsPortfolioStatsTask(dbManager));
		registerTask(new CalculateMultiAssetsPortfolioStatsTask(dbManager));
		registerTask(new DownloadTask());
		registerTask(new TransformEcbRatesTask());
		registerTask(new TransformSeriesDataTask());
		registerTask(new TransformDividendsDataTask());
		registerTask(new LoadPricesToDbTask(dbManager));
		registerTask(new LoadDividendsToDbTask(dbManager));
		registerTask(new CalculateAssetsShiftCorrelationTask(dbManager));
		registerTask(new OptimalPortfolioFinderTask(dbManager));
		registerTask(new LoadEpsToDbTask(dbManager));
		registerTask(new LoadEarningsToDbTask(dbManager));
		registerTask(new TransformEpsDataForTrainingTask());
	}

	private static void registerTask(Task task) {
		tasksMap.put(task.getName(), task);
	}

	private static Properties loadProperties() throws Exception {
		Properties prop = new Properties();
		BufferedReader br = new BufferedReader(new FileReader(PROP_FILE));
		prop.load(br);
		br.close();
		return prop;
	}

	private static Map<String, String> extractParameters(String[] args) {
		Map<String, String> params = new HashMap<>();
		if (args.length > 1) {
			for (String param : args) {
				String name = "";
				String value = "";
				int index = param.indexOf("=");
				if (index == -1) {
					name = param;
				} else {
					name = param.substring(0, index);
					value = param.substring(index + 1);
				}

				params.put(name, value);
			}
		}
		return params;
	}

	private static void execute(String taskName, Map<String, String> taskParams) throws Throwable {
		Objects.requireNonNull(taskParams, "taskParams must not be null.");

		if (!Strings.isNullOrEmpty(taskName)) {
			Task task = tasksMap.get(taskName);
			if (task != null) {
				task.execute(taskParams);
			} else {
				throw new Exception(String.format("'%s' task not found!", taskName));
			}
		}
	}
}
