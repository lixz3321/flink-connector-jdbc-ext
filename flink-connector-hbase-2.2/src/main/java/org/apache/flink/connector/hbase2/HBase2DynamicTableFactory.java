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

package org.apache.flink.connector.hbase2;

import org.apache.flink.annotation.Internal;
import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.ReadableConfig;
import org.apache.flink.connector.hbase.options.HBaseLookupOptions;
import org.apache.flink.connector.hbase.options.HBaseWriteOptions;
import org.apache.flink.connector.hbase.util.HBaseTableSchema;
import org.apache.flink.connector.hbase2.sink.HBaseDynamicTableSink;
import org.apache.flink.connector.hbase2.source.HBaseDynamicTableSource;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.connector.sink.DynamicTableSink;
import org.apache.flink.table.connector.source.DynamicTableSource;
import org.apache.flink.table.factories.DynamicTableSinkFactory;
import org.apache.flink.table.factories.DynamicTableSourceFactory;
import org.apache.flink.table.factories.FactoryUtil.TableFactoryHelper;

import org.apache.hadoop.conf.Configuration;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.apache.flink.connector.hbase.table.HBaseConnectorOptions.LOOKUP_ASYNC;
import static org.apache.flink.connector.hbase.table.HBaseConnectorOptions.LOOKUP_CACHE_MAX_ROWS;
import static org.apache.flink.connector.hbase.table.HBaseConnectorOptions.LOOKUP_CACHE_TTL;
import static org.apache.flink.connector.hbase.table.HBaseConnectorOptions.LOOKUP_MAX_RETRIES;
import static org.apache.flink.connector.hbase.table.HBaseConnectorOptions.NULL_STRING_LITERAL;
import static org.apache.flink.connector.hbase.table.HBaseConnectorOptions.SINK_BUFFER_FLUSH_INTERVAL;
import static org.apache.flink.connector.hbase.table.HBaseConnectorOptions.SINK_BUFFER_FLUSH_MAX_ROWS;
import static org.apache.flink.connector.hbase.table.HBaseConnectorOptions.SINK_BUFFER_FLUSH_MAX_SIZE;
import static org.apache.flink.connector.hbase.table.HBaseConnectorOptions.SINK_PARALLELISM;
import static org.apache.flink.connector.hbase.table.HBaseConnectorOptions.TABLE_NAME;
import static org.apache.flink.connector.hbase.table.HBaseConnectorOptions.ZOOKEEPER_QUORUM;
import static org.apache.flink.connector.hbase.table.HBaseConnectorOptions.ZOOKEEPER_ZNODE_PARENT;
import static org.apache.flink.connector.hbase.table.HBaseConnectorOptionsUtil.PROPERTIES_PREFIX;
import static org.apache.flink.connector.hbase.table.HBaseConnectorOptionsUtil.getHBaseConfiguration;
import static org.apache.flink.connector.hbase.table.HBaseConnectorOptionsUtil.getHBaseLookupOptions;
import static org.apache.flink.connector.hbase.table.HBaseConnectorOptionsUtil.getHBaseWriteOptions;
import static org.apache.flink.connector.hbase.table.HBaseConnectorOptionsUtil.validatePrimaryKey;
import static org.apache.flink.table.factories.FactoryUtil.createTableFactoryHelper;

/** HBase connector factory. */
@Internal
public class HBase2DynamicTableFactory
        implements DynamicTableSourceFactory, DynamicTableSinkFactory {

    private static final String IDENTIFIER = "hbase-2.2";

    /**
     * 用于创建source，并在创建前做一些前期连接准备的配置操作
     * @param context
     * @return
     */
    @Override
    public DynamicTableSource createDynamicTableSource(Context context) {
        //这个工具用于检验设置项和格式化设置项
        TableFactoryHelper helper = createTableFactoryHelper(this, context);
        //验证设置项参数，并忽略propertise前缀的设置项
        helper.validateExcept(PROPERTIES_PREFIX);
        //返回工厂当前使用的所有选项（设置项）
        final ReadableConfig tableOptions = helper.getOptions();

        //获取表模式
        TableSchema tableSchema = context.getCatalogTable().getSchema();
        //连接器设置项
        Map<String, String> options = context.getCatalogTable().getOptions();

        //检查Hbase表是否定义了行键，这个行键肯定是单字段的
        validatePrimaryKey(tableSchema);

        //获取表名称
        String tableName = tableOptions.get(TABLE_NAME);
        //创建一个hbase-site.xml配置对象，里面设置了hbase连接参数
        Configuration hbaseConf = getHBaseConfiguration(options);
        //获取lookup相关配置
        HBaseLookupOptions lookupOptions = getHBaseLookupOptions(tableOptions);
        //获取空值设置字面值参数
        String nullStringLiteral = tableOptions.get(NULL_STRING_LITERAL);
        //获取HbaseTableSchema
        HBaseTableSchema hbaseSchema = HBaseTableSchema.fromTableSchema(tableSchema);

        //返回Source
        return new HBaseDynamicTableSource(
                hbaseConf, tableName, hbaseSchema, nullStringLiteral, lookupOptions);
    }

    /**
     * 创建sink，并在创建前做一些前期准备工作
     * @param context
     * @return
     */
    @Override
    public DynamicTableSink createDynamicTableSink(Context context) {
        TableFactoryHelper helper = createTableFactoryHelper(this, context);
        helper.validateExcept(PROPERTIES_PREFIX);

        final ReadableConfig tableOptions = helper.getOptions();

        TableSchema tableSchema = context.getCatalogTable().getSchema();
        Map<String, String> options = context.getCatalogTable().getOptions();

        validatePrimaryKey(tableSchema);

        String tableName = tableOptions.get(TABLE_NAME);
        Configuration hbaseConf = getHBaseConfiguration(options);
        HBaseWriteOptions hBaseWriteOptions = getHBaseWriteOptions(tableOptions);
        String nullStringLiteral = tableOptions.get(NULL_STRING_LITERAL);
        HBaseTableSchema hbaseSchema = HBaseTableSchema.fromTableSchema(tableSchema);

        return new HBaseDynamicTableSink(
                tableName, hbaseSchema, hbaseConf, hBaseWriteOptions, nullStringLiteral);
    }

    /**
     * 连接器标识
     * @return
     */
    @Override
    public String factoryIdentifier() {
        return IDENTIFIER;
    }

    /**
     * 指定连接器的必要参数
     * @return
     */
    @Override
    public Set<ConfigOption<?>> requiredOptions() {
        Set<ConfigOption<?>> set = new HashSet<>();
        set.add(TABLE_NAME);
        return set;
    }

    /**
     * 指定连接器的可选参数
     * @return
     */
    @Override
    public Set<ConfigOption<?>> optionalOptions() {
        Set<ConfigOption<?>> set = new HashSet<>();
        set.add(ZOOKEEPER_ZNODE_PARENT);
        set.add(ZOOKEEPER_QUORUM);
        set.add(NULL_STRING_LITERAL);
        set.add(SINK_BUFFER_FLUSH_MAX_SIZE);
        set.add(SINK_BUFFER_FLUSH_MAX_ROWS);
        set.add(SINK_BUFFER_FLUSH_INTERVAL);
        set.add(SINK_PARALLELISM);
        set.add(LOOKUP_ASYNC);
        set.add(LOOKUP_CACHE_MAX_ROWS);
        set.add(LOOKUP_CACHE_TTL);
        set.add(LOOKUP_MAX_RETRIES);
        return set;
    }
}
