package com.poultryfarm.application.multitenancy;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Legge dal Catalog DB il nome del database corrispondente a un tenantId.
 *
 * evita di interrogare il Catalog DB ad ogni request:
 * il nome del DB di un tenant non cambia mai a runtime.
 */
@Repository
public class CatalogRepository {

    private final JdbcTemplate catalogJdbcTemplate;

    public CatalogRepository(@Qualifier("catalogJdbcTemplate") JdbcTemplate catalogJdbcTemplate) {
        this.catalogJdbcTemplate = catalogJdbcTemplate;
    }

    public String findDatabaseNameByTenantId(String tenantId) {
        return catalogJdbcTemplate.queryForObject(
                "SELECT database_name FROM dbo.TENANTS WHERE tenant_id = ?",
                String.class,
                tenantId
        );
    }
}
