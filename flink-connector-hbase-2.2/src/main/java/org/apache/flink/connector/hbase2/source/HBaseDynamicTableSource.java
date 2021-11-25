/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.connector.hbase2.source;

import org.apache.flink.annotation.Internal;
import org.apache.flink.annotation.VisibleForTesting;
import org.apache.flink.api.common.io.InputFormat;
import org.apache.flink.connector.hbase.options.HBaseLookupOptions;
import org.apache.flink.connector.hbase.source.AbstractHBaseDynamicTableSource;
import org.apache.flink.connector.hbase.source.HBaseRowDataLookupFunction;
import org.apache.flink.connector.hbase.util.HBaseTableSchema;
import org.apache.flink.table.connector.source.AsyncTableFunctionProvider;
import org.apache.flink.table.connector.source.DynamicTableSource;
import org.apache.flink.table.connector.source.TableFunctionProvider;
import org.apache.flink.table.data.RowData;

import org.apache.hadoop.conf.Configuration;

import static org.apache.flink.util.Preconditions.checkArgument;

/** HBase table source implementation. */
@Internal
public class HBaseDynamicTableSource extends AbstractHBaseDynamicTableSource {

    /**
     * 构造器：接收配置参数、表名称、表模式等
     * @param conf
     * @param tableName
     * @param hbaseSchema
     * @param nullStringLiteral
     * @param lookupOptions
     */
    public HBaseDynamicTableSource(
            Configuration conf,
            String tableName,
            HBaseTableSchema hbaseSchema,
            String nullStringLiteral,
            HBaseLookupOptions lookupOptions) {
        super(conf, tableName, hbaseSchema, nullStringLiteral, lookupOptions);
    }

    @Override
    public LookupRuntimeProvider getLookupRuntimeProvider(LookupContext context) {
        checkArgument(
            //应该是检查索引key只能有一个
                context.getKeys().length == 1 && context.getKeys()[0].length == 1,
                "Currently, HBase table can only be lookup by single rowkey.");
        checkArgument(
            //检查表模式中是否有行键
                hbaseSchema.getRowKeyName().isPresent(),
                "HBase schema must have a row key when used in lookup mode.");
        checkArgument(
            //第一个校验里已经限制了context.getKeys只有一个元素，所以···
            //猜想：context.getKeys()可能会返回一些索引字段，这个校验规则里如果发现这个索引字段集中不包含行键，那么就不通过
                hbaseSchema
                        .convertsToTableSchema()
                        .getTableColumn(context.getKeys()[0][0])
                        .filter(f -> f.getName().equals(hbaseSchema.getRowKeyName().get()))
                        .isPresent(),
                "Currently, HBase table only supports lookup by rowkey field.");
        if (lookupOptions.getLookupAsync()) {//如果是异步查询
            return AsyncTableFunctionProvider.of(
                    new HBaseRowDataAsyncLookupFunction(
                            conf, tableName, hbaseSchema, nullStringLiteral, lookupOptions));
        } else {
            return TableFunctionProvider.of(
                    new HBaseRowDataLookupFunction(
                            conf, tableName, hbaseSchema, nullStringLiteral, lookupOptions));
        }
    }

    @Override
    public DynamicTableSource copy() {
        return new HBaseDynamicTableSource(
                conf, tableName, hbaseSchema, nullStringLiteral, lookupOptions);
    }

    @Override
    protected InputFormat<RowData, ?> getInputFormat() {
        return new HBaseRowDataInputFormat(conf, tableName, hbaseSchema, nullStringLiteral);
    }

    @VisibleForTesting
    public HBaseLookupOptions getLookupOptions() {
        return this.lookupOptions;
    }
}
