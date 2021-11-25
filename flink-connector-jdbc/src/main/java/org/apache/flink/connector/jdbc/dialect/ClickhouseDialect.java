package org.apache.flink.connector.jdbc.dialect;

import org.apache.flink.connector.jdbc.internal.converter.ClickhouseRowConverter;
import org.apache.flink.connector.jdbc.internal.converter.JdbcRowConverter;
import org.apache.flink.table.types.logical.LogicalTypeRoot;
import org.apache.flink.table.types.logical.RowType;

import java.util.List;
import java.util.Optional;

/** JDBC dialect for MySQL. */
public class ClickhouseDialect extends AbstractDialect {
    @Override
    public int maxDecimalPrecision() {
        return 0;
    }

    @Override
    public int minDecimalPrecision() {
        return 0;
    }

    @Override
    public int maxTimestampPrecision() {
        return 0;
    }

    @Override
    public int minTimestampPrecision() {
        return 0;
    }

    @Override
    public List<LogicalTypeRoot> unsupportedTypes() {
        return null;
    }

    @Override
    public String dialectName() {
        return "ClickHouse";
    }

    @Override
    public boolean canHandle(String url) {
        return url.startsWith("jdbc:clickhouse:");
    }

    @Override
    public JdbcRowConverter getRowConverter(RowType rowType) {
        return new ClickhouseRowConverter(rowType);
    }

    @Override
    public String getLimitClause(long limit) {
        return null;
    }

    @Override
    public Optional<String> defaultDriverName() {
        return Optional.of("ru.yandex.clickhouse.ClickHouseDriver");
//        return Optional.of("com.github.housepower.jdbc.ClickHouseDriver");
    }

    @Override
    public String quoteIdentifier(String identifier) {
        return "`" + identifier + "`";
    }
}
