package com.poultryfarm.application.multitenancy;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

public class SchemaTenantResolver implements CurrentTenantIdentifierResolver<String> {

    @Override
    public String resolveCurrentTenantIdentifier() {
        return TenantContext.getSchema();
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
