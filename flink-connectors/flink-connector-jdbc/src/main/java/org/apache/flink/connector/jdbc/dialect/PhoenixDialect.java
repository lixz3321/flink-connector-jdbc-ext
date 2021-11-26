package org.apache.flink.connector.jdbc.dialect;

import org.apache.flink.connector.jdbc.internal.converter.JdbcRowConverter;
import org.apache.flink.connector.jdbc.internal.converter.PhoenixRowConverter;
import org.apache.flink.table.types.logical.LogicalTypeRoot;
import org.apache.flink.table.types.logical.RowType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * Copyright@ Apache Open Source Organization
 *
 * @Auther: lixz
 * @Date: 2021/11/26/15:53
 * @Description:
 */
public class PhoenixDialect extends AbstractDialect{
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
        return "Phoenix";
    }

    @Override
    public boolean canHandle(String url) {
        return url.startsWith("jdbc:phoenix:");
    }

    @Override
    public JdbcRowConverter getRowConverter(RowType rowType) {
        return new PhoenixRowConverter(rowType);
    }

    @Override
    public String getLimitClause(long limit) {
        return null;
    }

    @Override
    public Optional<String> defaultDriverName() {
        return Optional.of("org.apache.phoenix.jdbc.PhoenixDriver");
    }

    @Override
    public String quoteIdentifier(String identifier) {
        return "\"" + identifier + "\"";
    }

    @Override
    public String getInsertIntoStatement(String tableName, String[] fieldNames) {
        String columns =
            Arrays.stream(fieldNames)
                .map(this::quoteIdentifier)
                .collect(Collectors.joining(", "));
        String placeholders =
            Arrays.stream(fieldNames).map(f -> ":" + f).collect(Collectors.joining(", "));
        return "UPSERT INTO "
            + quoteIdentifier(tableName)
            + "("
            + columns
            + ")"
            + " VALUES ("
            + placeholders
            + ")";
    }
}
