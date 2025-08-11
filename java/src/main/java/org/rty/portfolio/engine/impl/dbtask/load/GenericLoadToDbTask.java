package org.rty.portfolio.engine.impl.dbtask.load;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rty.portfolio.db.DbManager;
import org.rty.portfolio.engine.AbstractDbTask;
import org.rty.portfolio.io.BulkCsvLoader;

public abstract class GenericLoadToDbTask<T> extends AbstractDbTask {
	protected final Set<String> errorAssets = new HashSet<String>();
	private final int expectedNumberOfColumns;

	public GenericLoadToDbTask(DbManager dbManager, int expectedNumberOfColumns) {
		super(dbManager);
		this.expectedNumberOfColumns = expectedNumberOfColumns;
	}

	@Override
	public final void execute(Map<String, String> parameters) throws Exception {
		final String inputFile = getValidParameterValue(parameters, INPUT_FILE_PARAM);

		final BulkCsvLoader<T> loader = new BulkCsvLoader<>(expectedNumberOfColumns) {

			@Override
			protected List<String> saveResults(List<T> dataToAdd) throws Exception {
				dbManager.setAutoCommit(false);

				List<String> result = GenericLoadToDbTask.this.saveResults(dataToAdd);

				dbManager.commit();
				dbManager.setAutoCommit(true);

				return result;
			}

			@Override
			protected T toEntity(String assetName, String[] line) {
				return GenericLoadToDbTask.this.toEntity(assetName, line);
			}
		};

		loader.load(inputFile);
		say(DONE);
	}

	/**
	 * Returns a list of assets that failed to save.
	 */
	protected abstract List<String> saveResults(List<T> dataToAdd) throws Exception;

	protected abstract T toEntity(String assetName, String[] line);
}
