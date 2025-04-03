package com.czj.student.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
@PropertySource("classpath:application.properties")
public class DruidConfig {
    @Value("${jdbc.driver}")
    private String driverClassName;
    @Value("${jdbc.url}")
    private String url;
    @Value("${jdbc.username}")
    private String username;
    @Value("${jdbc.password}")
    private String password;
    
    @Value("${druid.initialSize}")
    private int initialSize;
    @Value("${druid.minIdle}")
    private int minIdle;
    @Value("${druid.maxActive}")
    private int maxActive;
    @Value("${druid.maxWait}")
    private long maxWait;
    @Value("${druid.timeBetweenEvictionRunsMillis}")
    private long timeBetweenEvictionRunsMillis;
    @Value("${druid.minEvictableIdleTimeMillis}")
    private long minEvictableIdleTimeMillis;
    @Value("${druid.validationQuery}")
    private String validationQuery;
    @Value("${druid.testWhileIdle}")
    private boolean testWhileIdle;
    @Value("${druid.testOnBorrow}")
    private boolean testOnBorrow;
    @Value("${druid.testOnReturn}")
    private boolean testOnReturn;
    @Value("${druid.poolPreparedStatements}")
    private boolean poolPreparedStatements;
    @Value("${druid.maxPoolPreparedStatementPerConnectionSize}")
    private int maxPoolPreparedStatementPerConnectionSize;
    @Value("${druid.filters}")
    private String filters;

    @Bean
    public DataSource druidDataSource() {
        DruidDataSource datasource = new DruidDataSource();
        
        // 基础配置
        datasource.setDriverClassName(driverClassName);
        datasource.setUrl(url);
        datasource.setUsername(username);
        datasource.setPassword(password);
        
        // 连接池配置
        datasource.setInitialSize(initialSize);
        datasource.setMinIdle(minIdle);
        datasource.setMaxActive(maxActive);
        datasource.setMaxWait(maxWait);
        datasource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        datasource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        datasource.setValidationQuery(validationQuery);
        datasource.setTestWhileIdle(testWhileIdle);
        datasource.setTestOnBorrow(testOnBorrow);
        datasource.setTestOnReturn(testOnReturn);
        datasource.setPoolPreparedStatements(poolPreparedStatements);
        datasource.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPerConnectionSize);
        
        try {
            datasource.setFilters(filters);
        } catch (SQLException e) {
            throw new RuntimeException("Druid配置初始化异常", e);
        }
        
        return datasource;
    }
}
