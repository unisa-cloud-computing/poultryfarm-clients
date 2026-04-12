package com.poultryfarm.application.multitenancy;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.connections.spi.DatabaseConnectionInfo;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class SchemaMultiTenantConnectionProvider implements MultiTenantConnectionProvider<String> {

    private final DataSource dataSource;
    private final CatalogRepository catalogRepository;
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(SchemaMultiTenantConnectionProvider.class);

    @Value("${app.datasource.server-name}")
    private String serverName;

    // Cache dei DataSource già creati: tenantId → DataSource
    private final java.util.concurrent.ConcurrentHashMap<String, DataSource> tenantCache = new java.util.concurrent.ConcurrentHashMap<>();

    public SchemaMultiTenantConnectionProvider(
            @Qualifier("catalogDataSource") DataSource catalogDataSource,
            CatalogRepository catalogRepository
    ) {
        this.dataSource = catalogDataSource;
        this.catalogRepository = catalogRepository;
    }

    @Override
    public Connection getAnyConnection() {
        try {
            logger.info("########## CLIENTI SERVICE - Getting any connection for catalog database");
            return dataSource.getConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public DatabaseConnectionInfo getDatabaseConnectionInfo(Dialect dialect) {
        return MultiTenantConnectionProvider.super.getDatabaseConnectionInfo(dialect);
    }

    @Override
    public Connection getConnection(String tenantId) {
        try {
            logger.info("########## CLIENTI SERVICE - Getting connection for tenant: {}", tenantId);
            var ds = tenantCache.computeIfAbsent(tenantId, id -> {
                var tenantInfo = catalogRepository.findTenantInfo(id);
                return buildDataSource(tenantInfo.databaseName());
            });
            Connection conn = ds.getConnection();
            logger.info("########## CLIENTI SERVICE - Connection obtained for tenant: {}", tenantId);
            return conn;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void releaseConnection(String tenant, Connection connection) {
        try {
            logger.info("########## CLIENTI SERVICE - Releasing connection for tenant: {}", tenant);
            connection.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }

    private DataSource buildDataSource(String databaseName) {
        logger.info("########## CLIENTI SERVICE - Building DataSource for database: {}", databaseName);
        SQLServerDataSource ds = new SQLServerDataSource();
        ds.setServerName(serverName);
        ds.setDatabaseName(databaseName);
        ds.setEncrypt("true");
        ds.setTrustServerCertificate(false);
        ds.setAuthentication("ActiveDirectoryManagedIdentity");
        return ds;
    }
}
