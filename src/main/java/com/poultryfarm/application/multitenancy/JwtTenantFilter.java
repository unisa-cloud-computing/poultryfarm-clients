package com.poultryfarm.application.multitenancy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;

/**
 * Filtro eseguito una volta per ogni request HTTP.
 * Legge il JWT dall'header Authorization, estrae il claim tenantId
 * e lo mette nel TenantContext.
 *
 * Non valida la firma del JWT: quella la fa già APIM prima
 * di inoltrare la request al microservizio.
 */
@Component
public class JwtTenantFilter extends OncePerRequestFilter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String tenantId = extractTenantId(request);

            if (tenantId == null || tenantId.isBlank()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("tenantId claim mancante nel token.");
                return;
            }

            TenantContext.setTenantId(tenantId);
            filterChain.doFilter(request, response);

        } finally {
            TenantContext.clear();
        }
    }

    private String extractTenantId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        // Il JWT è formato da 3 parti separate da "." → header.payload.signature
        // Il payload è la seconda parte, codificata in Base64
        try {
            String jwt = authHeader.substring(7);
            String[] parts = jwt.split("\\.");
            if (parts.length != 3) return null;

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
            JsonNode payload = OBJECT_MAPPER.readTree(payloadJson);
            JsonNode claim = payload.get("tenantId");
            return claim != null ? claim.asText() : null;

        } catch (Exception e) {
            return null;
        }
    }
}
