package com.procgrid.common.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * MyBatis configuration for database operations
 */
@Configuration
@EnableTransactionManagement
@MapperScan(basePackages = {
    "com.procgrid.auth.mapper",
    "com.procgrid.user.mapper",
    "com.procgrid.product.mapper",
    "com.procgrid.quote.mapper",
    "com.procgrid.order.mapper",
    "com.procgrid.payment.mapper",
    "com.procgrid.notification.mapper"
})
public class MyBatisConfig {
    
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        
        // Set mapper XML locations
        sessionFactory.setMapperLocations(
            new PathMatchingResourcePatternResolver()
                .getResources("classpath*:mapper/**/*.xml")
        );
        
        // Configure MyBatis settings
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setUseGeneratedKeys(true);
        configuration.setDefaultExecutorType(org.apache.ibatis.session.ExecutorType.REUSE);
        configuration.setDefaultStatementTimeout(30000);
        configuration.setCallSettersOnNulls(true);
        configuration.setReturnInstanceForEmptyRow(true);
        
        sessionFactory.setConfiguration(configuration);
        
        return sessionFactory.getObject();
    }
}