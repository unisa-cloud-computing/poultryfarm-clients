package com.poultryfarm.application.multitenancy;

/**
 * Tiene il tenantId corrente per il thread della request HTTP.
 * ThreadLocal garantisce che ogni thread (ogni request) abbia
 * il suo tenantId isolato dagli altri thread concorrenti.
 */
public class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    public static void setTenantId(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static String getTenantId() {
        return CURRENT_TENANT.get();
    }

    // Va chiamato alla fine di ogni request: i thread Tomcat vengono
    // riusati e non devono portarsi dietro il tenantId della request precedente
    public static void clear() {
        CURRENT_TENANT.remove();
    }
}