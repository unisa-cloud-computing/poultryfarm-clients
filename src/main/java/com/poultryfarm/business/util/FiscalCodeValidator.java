package com.poultryfarm.business.util;

/**
 * Utility class for validating Italian fiscal codes (Codice Fiscale).
 * Validates the standard 16-character alphanumeric format.
 */
public final class FiscalCodeValidator {

    private static final String FISCAL_CODE_REGEX = "^[A-Z]{6}[0-9]{2}[A-EHLMPRST][0-9]{2}[A-Z][0-9]{3}[A-Z]$";

    private FiscalCodeValidator() {
        // utility class
    }

    /**
     * Validates whether the given fiscal code matches the expected Italian
     * Codice Fiscale format (16 alphanumeric characters).
     *
     * @param fiscalCode the fiscal code to validate
     * @return {@code true} if the fiscal code is valid; {@code false} otherwise
     */
    public static boolean isValid(String fiscalCode) {
        if (fiscalCode == null) {
            return false;
        }
        return fiscalCode.toUpperCase().matches(FISCAL_CODE_REGEX);
    }
}
