package org.rty.portfolio.core.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.rty.portfolio.core.CsvWritable;
import org.rty.portfolio.math.Calculator;

public class HistoricalRowGenerator {
	private static final double PRECISION = 100D;

	public enum ColumnKind {
		CURRENT_VALUE_ONLY {
			@Override
			List<String> generateColumns(String columnPattern) {
				return List.of(columnPattern);
			}
		},
		CURRENT_AND_PREVIOUS_VALUES {
			@Override
			List<String> generateColumns(String columnPattern) {
				return List.of(columnPattern, generatePreviousName(columnPattern));
			}
		},
		CURRENT_PREVIOUS_AND_CHANGE_RATE_VALUES {
			@Override
			List<String> generateColumns(String columnPattern) {
				return List.of(columnPattern, generatePreviousName(columnPattern),
						generateChangeRateName(columnPattern));
			}
		};

		public List<String> allColumnsFrom(String columnPattern) {
			Objects.requireNonNull(columnPattern, "columnPattern must not be null!");
			return generateColumns(columnPattern);
		}

		abstract List<String> generateColumns(String columnPattern);
	}

	public static class RowHeader implements CsvWritable {
		private final List<String> allHeaders;
		private final Map<String, Integer> headerIndeces;
		private final Map<String, Set<String>> allPatterns;

		private RowHeader(List<String> allHeaders, Map<String, Set<String>> allPatterns) {
			this.allHeaders = Objects.requireNonNull(allHeaders, "allHeaders must not be null!");
			this.allPatterns = Objects.requireNonNull(allPatterns, "allPatternsmust not be null!");

			this.headerIndeces = new HashMap<>();
			for (int i = 0; i < allHeaders.size(); i++) {
				final String columnName = allHeaders.get(i);
				if (headerIndeces.put(columnName, i) != null) {
					throw new IllegalArgumentException(
							"Multiple columns with name '%s' detected!".formatted(columnName));
				}
			}
		}

		@Override
		public String[] toCsvLine() {
			return allHeaders.toArray(new String[0]);
		}

		public int size() {
			return allHeaders.size();
		}

		public int indexOf(String columnName) {
			return headerIndeces.get(columnName);
		}

		public Row newRow() {
			return new Row(this);
		}

		public boolean hasAllColumns(String columnPattern, ColumnKind columnKind) {
			Objects.requireNonNull(columnKind, "columnKind must not be null!");

			if (allPatterns.containsKey(columnPattern)) {
				final List<String> detailedColumns = columnKind.allColumnsFrom(columnPattern);
				final Set<String> expectedColumns = allPatterns.get(columnPattern);

				if (!expectedColumns.containsAll(detailedColumns)) {
					return false;
				}

				if (!Set.copyOf(detailedColumns).containsAll(expectedColumns)) {
					return false;
				}

				return true;
			}

			return false;
		}

		public static Builder startWithHeader(String columnPattern, ColumnKind columnKind) {
			return new Builder(columnPattern, columnKind);
		}

		public static class Builder {
			private final List<String> allHeaders = new ArrayList<>();
			private final Map<String, Set<String>> allPatterns = new HashMap<>();

			private Builder(String columnPattern, ColumnKind columnKind) {
				Objects.requireNonNull(columnKind, "columnKind must not be null!");

				List<String> detailedColumns = columnKind.allColumnsFrom(columnPattern);
				allHeaders.addAll(detailedColumns);
				allPatterns.put(columnPattern, Set.copyOf(detailedColumns));
			}

			public Builder addHeader(String columnPattern, ColumnKind columnKind) {
				Objects.requireNonNull(columnKind, "columnKind must not be null!");

				if (allPatterns.containsKey(columnPattern)) {
					throw new IllegalArgumentException("'%s' already exists!".formatted(columnPattern));
				} else {
					List<String> detailedColumns = columnKind.allColumnsFrom(columnPattern);
					allHeaders.addAll(detailedColumns);
					allPatterns.put(columnPattern, Set.copyOf(detailedColumns));
				}

				return this;
			}

			public RowHeader build() {
				return new RowHeader(allHeaders, allPatterns);
			}
		}
	}

	public static class Row implements CsvWritable {
		private final RowHeader header;
		private final String[] values;

		private Row(RowHeader header) {
			this.header = Objects.requireNonNull(header, "header must not be null!");
			this.values = new String[header.size()];
		}

		public void add(String columnPattern, double value) {
			validateColumns(columnPattern, ColumnKind.CURRENT_VALUE_ONLY);

			addToValues(columnPattern, value);
		}

		public void add(String columnPattern, int value) {
			validateColumns(columnPattern, ColumnKind.CURRENT_VALUE_ONLY);

			addToValues(columnPattern, "" + value);
		}

		public void add(String columnPattern, String value) {
			validateColumns(columnPattern, ColumnKind.CURRENT_VALUE_ONLY);

			addToValues(columnPattern, "" + value);
		}

		public void add(String columnPattern, double currentValue, double previousValue) {
			add(columnPattern, currentValue, previousValue, false);
		}

		public void add(String columnPattern, int currentValue, int previousValue) {
			validateColumns(columnPattern, ColumnKind.CURRENT_AND_PREVIOUS_VALUES);

			addToValues(columnPattern, "" + currentValue);
			addToValues(generatePreviousName(columnPattern), "" + previousValue);
		}

		public void add(String columnPattern, double currentValue, double previousValue, boolean withChangeRate) {
			if (withChangeRate) {
				validateColumns(columnPattern, ColumnKind.CURRENT_PREVIOUS_AND_CHANGE_RATE_VALUES);

				addToValues(columnPattern, currentValue);
				addToValues(generatePreviousName(columnPattern), previousValue);
				addToValues(generateChangeRateName(columnPattern), toStringChangeRate(currentValue, previousValue));

			} else {
				validateColumns(columnPattern, ColumnKind.CURRENT_AND_PREVIOUS_VALUES);

				addToValues(columnPattern, currentValue);
				addToValues(generatePreviousName(columnPattern), previousValue);
			}
		}

		@Override
		public String[] toCsvLine() {
			return values;
		}

		private void addToValues(String columnName, double value) {
			addToValues(columnName, toStringValue(value));
		}

		private void addToValues(String columnName, String value) {
			values[header.indexOf(columnName)] = value;
		}

		private void validateColumns(String columnPattern, ColumnKind columnKind) {
			if (!header.hasAllColumns(columnPattern, columnKind)) {
				throw new IllegalArgumentException("'%s' doesn't match all columns!".formatted(columnPattern));
			}
		}
	}

	public static String toStringValue(double v) {
		return "" + Calculator.round(v, PRECISION);
	}

	private static String generatePreviousName(String columnPattern) {
		return "prev_" + columnPattern;
	}

	private static String generateChangeRateName(String columnPattern) {
		return "ch_r_" + columnPattern;
	}

	private static String toStringChangeRate(double v1, double v2) {
		return toStringValue(Calculator.calculateEpsSurprise(v1, v2));
	}
}
