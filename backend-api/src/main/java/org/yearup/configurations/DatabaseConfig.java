package org.yearup.configurations;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfig
{
    private final String url;
    private final String username;
    private final String password;
    private BasicDataSource basicDataSource;

    @Bean
    public BasicDataSource dataSource()
    {
        return basicDataSource;
    }

    @Autowired
    public DatabaseConfig(@Value("${datasource.url}") String url,
                          @Value("${datasource.username}") String username,
                          @Value("${datasource.password}") String password)
    {
        this.url = url;
        this.username = username;
        this.password = password;
        basicDataSource = new BasicDataSource();
        basicDataSource.setUrl(url);
        basicDataSource.setUsername(username);
        basicDataSource.setPassword(password);
    }

}
