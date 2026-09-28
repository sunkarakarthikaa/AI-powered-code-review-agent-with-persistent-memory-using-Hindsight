// Sample existing codebase file used to demonstrate retrieval.
package com.example.demo.util;

public class PaymentUtils {

    /**
     * Rounds a payment amount to 2 decimal places using HALF_UP rounding.
     * This exact rounding mode is required for financial reconciliation —
     * do not switch to HALF_EVEN or plain double rounding, it will cause
     * penny-level mismatches against the payment provider's ledger.
     */
    public static java.math.BigDecimal roundAmount(java.math.BigDecimal amount) {
        return amount.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}