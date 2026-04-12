package com.poultryfarm.application.multitenancy;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
public class DataSourceConfig {

    @Value("${app.datasource.server-name}")
    private String serverName;


    // ── Hibernate Multi-Tenancy ─────────────────────────────────────────────

    @Bean
    public MultiTenantConnectionProvider<String> multiTenantConnectionProvider(DataSource dataSource) {
        return new SchemaMultiTenantConnectionProvider(dataSource);
    }

    @Bean
    public CurrentTenantIdentifierResolver<String> tenantIdentifierResolver() {
        return new SchemaTenantResolver();
    }

    // ── Catalog DataSource ────────────────────────────────────────────────────

    /**
     * DataSource del Catalog DB (db-catalog).
     * Usato solo da CatalogRepository per risolvere tenantId → dbName.
     */
    @Bean(name = "catalogDataSource")
    public DataSource catalogDataSource() {
        return buildDataSource("db-catalog");
    }

    @Bean(name = "catalogJdbcTemplate")
    public JdbcTemplate catalogJdbcTemplate(@Qualifier("catalogDataSource") DataSource catalogDataSource) {
        return new JdbcTemplate(catalogDataSource);
    }

    // ── Tenant Routing DataSource ─────────────────────────────────────────────

    /**
     * DataSource principale usato da Spring Data JPA.
     * @Primary lo rende il DataSource di default per tutti i repository.
     * Non ha una connessione fissa: ad ogni operazione JPA delega a
     * TenantRoutingDataSource che sceglie il DataSource del tenant corrente.
     */
    @Bean
    @Primary
    public DataSource dataSource(CatalogRepository catalogRepository) {

        TenantRoutingDataSource routingDs = new TenantRoutingDataSource() {
            // Cache dei DataSource già creati: tenantId → DataSource
            private final java.util.concurrent.ConcurrentHashMap<String, DataSource>
                    tenantCache = new java.util.concurrent.ConcurrentHashMap<>();

            @Override
            protected DataSource determineTargetDataSource() {
                String tenantId = TenantContext.getTenantId();

                if (tenantId == null) {
                    throw new IllegalStateException(
                            "Nessun tenantId nel contesto. " +
                                    "Verificare che JwtTenantFilter sia attivo."
                    );
                }

                // Prima volta per questo tenant: crea e metti in cache
                // Volte successive: restituisce quello già in cache
                return tenantCache.computeIfAbsent(tenantId, id -> {
                    var tenantInfo = catalogRepository.findTenantInfo(id);
                    TenantContext.setSchema(tenantInfo.schemaName());
                    return buildDataSource(tenantInfo.databaseName());
                });
            }
        };

        routingDs.setTargetDataSources(new HashMap<>());
        routingDs.setDefaultTargetDataSource(catalogDataSource());
        routingDs.afterPropertiesSet();

        return routingDs;
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private DataSource buildDataSource(String databaseName) {
        SQLServerDataSource ds = new SQLServerDataSource();
        ds.setServerName(serverName);
        ds.setDatabaseName(databaseName);
        ds.setEncrypt("true");
        ds.setTrustServerCertificate(false);
        ds.setAuthentication("ActiveDirectoryManagedIdentity");
        return ds;
    }
}
