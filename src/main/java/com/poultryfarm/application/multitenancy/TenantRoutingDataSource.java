package com.poultryfarm.application.multitenancy;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Spring chiama determineCurrentLookupKey() prima di ogni operazione
 * sul database per decidere quale DataSource usare.
 * Restituiamo il tenantId corrente: il routing datasource
 * lo usa come chiave per trovare il DataSource corretto nella mappa.
 */
public class TenantRoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return TenantContext.getTenantId();
    }
}
