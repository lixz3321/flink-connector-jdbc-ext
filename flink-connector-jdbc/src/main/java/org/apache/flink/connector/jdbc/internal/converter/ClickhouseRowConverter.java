package org.apache.flink.connector.jdbc.internal.converter;

import org.apache.flink.table.types.logical.RowType;

/**
 * Created with IntelliJ IDEA. Copyright@ Apache Open Source Organization @Auther: Lixz @Date:
 * 2021/11/25/15:07 @Description: Row转换器
 */
public class ClickhouseRowConverter extends AbstractJdbcRowConverter {

    private static final long serialVersionUID = 1L;

    public ClickhouseRowConverter(RowType rowType) {
        super(rowType);
    }

    @Override
    public String converterName() {
        return "Clickhouse";
    }
}
