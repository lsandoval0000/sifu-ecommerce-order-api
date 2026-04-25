package org.sifu.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * General constants used throughout the application.
 * Contains tax-related constants for IGV (General Sales Tax) calculations.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GeneralConstants {
    /** IGV tax rate (18%) */
    public static final BigDecimal IGV = BigDecimal.valueOf(0.18);
    /** Net value multiplier (1.18) for calculating amounts before tax */
    public static final BigDecimal NET_VALUE = BigDecimal.valueOf(1.18);
}
