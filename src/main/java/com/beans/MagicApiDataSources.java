package com.beans;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;

@Component
public class MagicApiDataSources {

    @Bean(name = "mysqlDataSource")
    @ConfigurationProperties("spring.data-source.mysql")
    public DataSource mysqlDataSource() throws SQLException {
        DruidDataSource mysqlDataSource =  new DruidDataSource();
        mysqlDataSource.setName("mysql");
        mysqlDataSource.setFilters("stat");
        mysqlDataSource.setInitialSize(1);
        mysqlDataSource.setMinIdle(1);
        mysqlDataSource.setMaxActive(3);
        mysqlDataSource.setPoolPreparedStatements(false);
        mysqlDataSource.setValidationQuery("select * from vt.magic_api_file");
        mysqlDataSource.setTestOnBorrow(true);

        return mysqlDataSource;
    }
}
