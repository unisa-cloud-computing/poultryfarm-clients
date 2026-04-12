package com.poultryfarm.application.multitenancy;

/**
 * Tiene il tenantId corrente per il thread della request HTTP.
 * ThreadLocal garantisce che ogni thread (ogni request) abbia
 * il suo tenantId isolato dagli altri thread concorrenti.
 */
public class TenantContext {

    private static final ThreadLocal<String> tenantId = new ThreadLocal<>();

    public static void setTenantId(String id) {
        tenantId.set(id);
    }

    public static String getTenantId() {
        return tenantId.get();
    }

    public static void clear() {
        tenantId.remove();
    }
}