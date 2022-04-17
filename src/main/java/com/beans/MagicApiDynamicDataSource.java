package com.beans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.ssssssss.magicapi.datasource.model.MagicDynamicDataSource;

import javax.sql.DataSource;

@Component
public class MagicApiDynamicDataSource {

    @Autowired
    @Qualifier(value = "mysqlDataSource")
    private DataSource mysqlDataSource;

    @Bean
    public MagicDynamicDataSource magicDynamicDataSource(){
        MagicDynamicDataSource dynamicDataSource = new MagicDynamicDataSource();
        dynamicDataSource.setDefault(mysqlDataSource); // 设置默认数据源
        dynamicDataSource.add("mysql",mysqlDataSource);
        return dynamicDataSource;
    }
}
