# flink-connector-jdbc-ext
这是一个扩展的flink-connector-jdbc，相比于官方，该版本新增了对clickhouse的支持，后续我将继续改造以支持更多的jdbc连接，例如：oracle\phoenix...

使用方法：
1、mvn 打包
进入flink-connector-jdbc-ext\flink-connectors\flink-connector-jdbc目录，执行mvn打包命令
clean install -DskipTests -Dfast
打包完成可以在flink-connector-jdbc-ext\flink-connectors\flink-connector-jdbc\target目录下找到jar
2、使用
将打包好的jar添加到你开发环境的maven库中，执行命令
mvn install:install-file -Dfile=/opt/flink-connector-jdbc-ext_2.11.1.14.0 -DgroupId=org.apache.flink -DartifactId=flink-connector-jdbc-ext_2.11 -Dversion=1.14.0 -Dpackaging=jar
maven引入
<dependency>
  <groupId>org.apache.flink</groupId>
  <artifactId>flink-connector-jdbc-ext_2.11</artifactId>
  <version>1.14.0</version>
</dependency>


