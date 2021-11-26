package org.apache.flink.connector.jdbc.internal.converter;

import org.apache.flink.table.types.logical.RowType;

/**
 * Created with IntelliJ IDEA.
 * Copyright@ Apache Open Source Organization
 *
 * @Auther: lixz
 * @Date: 2021/11/26/16:02
 * @Description:
 */
public class PhoenixRowConverter extends AbstractJdbcRowConverter{

    public PhoenixRowConverter(RowType rowType) {
        super(rowType);
    }

    @Override
    public String converterName() {
        return "Phoenix";
    }
}
