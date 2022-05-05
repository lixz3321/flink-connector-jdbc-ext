#   flink-connector-jdbc-ext
这是一个扩展的flink-connector-jdbc，相比于官方连接器该版本实现了更多的数据库连接支持，现在支持的数据库有:  
```
MySql
PostgreSQL
Derby
ClickHouse
Phoenix
```
后面会继续改造以支持更多的jdbc连接。


# 使用方法
####    1、mvn 打包
进入flink-connector-jdbc-ext\flink-connectors\flink-connector-jdbc目录，执行mvn打包命令</br>
<pre>clean install -DskipTests -Dfast</pre>
打包完成可以在flink-connector-jdbc-ext\flink-connectors\flink-connector-jdbc\target目录下找到打包好的jar包
####    2、使用
将打包好的jar添加到你开发环境的maven本地库中，执行命令  
<pre>mvn install:install-file -Dfile=/opt/flink-connector-jdbc-ext_2.11.1.14.0 -DgroupId=org.apache.flink -DartifactId=flink-connector-jdbc-ext_2.11 -Dversion=1.14.0 -Dpackaging=jar</pre>
maven引入
```
<dependency>
  <groupId>org.apache.flink</groupId>  
  <artifactId>flink-connector-jdbc-ext_2.11</artifactId>  
  <version>1.14.0</version>  
</dependency> 
```
####  3、驱动兼容

 数据库  | 驱动 | 版本  
 ---- | ----- | -----
 Clickhouse  | clickhouse-jdbc | 0.2.4
 Phoenix  | phoenix-core | 5.0.0-HBase-2.0
####  4、示例

```
StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
EnvironmentSettings bsSettings = EnvironmentSettings.newInstance().useBlinkPlanner().inStreamingMode().build();
StreamTableEnvironment tEnv = StreamTableEnvironment.create(env, bsSettings);
//加载clickhouse表
tEnv.executeSql("create table test2(" +
        "`id` INT," +
        "`name` STRING" +
        ")WITH(" +
        "'connector' = 'jdbc'," +
        "'url' = 'jdbc:clickhouse://192.168.78.17:8123/default'," +
        "'table-name' = 'test2')");
tEnv.executeSql("select * from test2").print();
```
                
                
###### 希望老铁们给点个Start，我会继续解锁连接器技能，有问题一起交流，微信：z1224576376

 





