import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

/**
 * Three seed tests. Coverage is intentionally light:
 *  - computeIncomeTax: only the middle slab branch is hit
 *  - computeVAT: only the 18% rate is hit
 *  - applyExemption: only the "under cap" branch is hit
 *  - roundToPaise: not exercised
 *  - isEligibleForReturn: not exercised at all
 */
public class TaxCalculatorTest {

    @Test
    public void incomeTax_middleSlab() {
        TaxCalculator c = new TaxCalculator();
        BigDecimal tax = c.computeIncomeTax(new BigDecimal("700000"));
        // 12500 (5% on 250k-500k) + 40000 (20% on 500k-700k) = 52500
        assertEquals(0, tax.compareTo(new BigDecimal("52500.00")));
    }

    @Test
    public void vat_eighteenPercent() {
        TaxCalculator c = new TaxCalculator();
        BigDecimal gst = c.computeVAT(new BigDecimal("1000"), 18);
        assertEquals(0, gst.compareTo(new BigDecimal("180.00")));
    }

    @Test
    public void exemption_underCap() {
        TaxCalculator c = new TaxCalculator();
        BigDecimal net = c.applyExemption(
                new BigDecimal("600000"), new BigDecimal("120000"));
        assertEquals(0, net.compareTo(new BigDecimal("480000")));
    }

    // ---------------------------------------------------------------
    // Session 5A Part B -- AI-generated tests for isEligibleForReturn
    // ---------------------------------------------------------------

    @Test
    public void isEligibleForReturn_nullGrossIncome_returnsFalse() {
        // Arrange
        TaxCalculator c = new TaxCalculator();
        // Act
        boolean eligible = c.isEligibleForReturn(null, 35);
        // Assert
        assertFalse(eligible);
    }

    @Test
    public void isEligibleForReturn_negativeAge_returnsFalse() {
        // Arrange
        TaxCalculator c = new TaxCalculator();
        // Act
        boolean eligible = c.isEligibleForReturn(new BigDecimal("900000"), -1);
        // Assert
        assertFalse(eligible);
    }

    @Test
    public void isEligibleForReturn_zeroIncome_returnsFalse() {
        // Arrange
        TaxCalculator c = new TaxCalculator();
        // Act
        boolean eligible = c.isEligibleForReturn(BigDecimal.ZERO, 35);
        // Assert
        assertFalse(eligible);
    }

    @Test
    public void isEligibleForReturn_generalAgeIncomeAtThreshold_returnsFalse() {
        // Arrange
        TaxCalculator c = new TaxCalculator();
        // Act
        boolean eligible = c.isEligibleForReturn(new BigDecimal("250000"), 35);
        // Assert
        assertFalse(eligible);
    }

    @Test
    public void isEligibleForReturn_generalAgeIncomeOneRupeeOverThreshold_returnsTrue() {
        // Arrange
        TaxCalculator c = new TaxCalculator();
        // Act
        boolean eligible = c.isEligibleForReturn(new BigDecimal("250001"), 35);
        // Assert
        assertTrue(eligible);
    }

    @Test
    public void isEligibleForReturn_ageFiftyNine_usesGeneralThreshold() {
        // Arrange
        TaxCalculator c = new TaxCalculator();
        // Act -- 260000 clears the 250000 general slab but not the 300000 senior slab
        boolean eligible = c.isEligibleForReturn(new BigDecimal("260000"), 59);
        // Assert
        assertTrue(eligible);
    }

    @Test
    public void isEligibleForReturn_seniorAtSixtyIncomeAtThreshold_returnsFalse() {
        // Arrange
        TaxCalculator c = new TaxCalculator();
        // Act
        boolean eligible = c.isEligibleForReturn(new BigDecimal("300000"), 60);
        // Assert
        assertFalse(eligible);
    }

    @Test
    public void isEligibleForReturn_seniorAtSixtyIncomeOneRupeeOverThreshold_returnsTrue() {
        // Arrange
        TaxCalculator c = new TaxCalculator();
        // Act
        boolean eligible = c.isEligibleForReturn(new BigDecimal("300001"), 60);
        // Assert
        assertTrue(eligible);
    }

    @Test
    public void isEligibleForReturn_ageSeventyNine_usesSeniorThreshold() {
        // Arrange
        TaxCalculator c = new TaxCalculator();
        // Act -- 310000 clears the 300000 senior slab but not the 500000 super-senior slab
        boolean eligible = c.isEligibleForReturn(new BigDecimal("310000"), 79);
        // Assert
        assertTrue(eligible);
    }

    @Test
    public void isEligibleForReturn_superSeniorAtEightyIncomeAtThreshold_returnsFalse() {
        // Arrange
        TaxCalculator c = new TaxCalculator();
        // Act
        boolean eligible = c.isEligibleForReturn(new BigDecimal("500000"), 80);
        // Assert
        assertFalse(eligible);
    }

    @Test
    public void isEligibleForReturn_superSeniorAtEightyIncomeOneRupeeOverThreshold_returnsTrue() {
        // Arrange
        TaxCalculator c = new TaxCalculator();
        // Act
        boolean eligible = c.isEligibleForReturn(new BigDecimal("500001"), 80);
        // Assert
        assertTrue(eligible);
    }

    @Test
    public void isEligibleForReturn_ageZero_usesGeneralThreshold() {
        // Arrange
        TaxCalculator c = new TaxCalculator();
        // Act
        boolean eligible = c.isEligibleForReturn(new BigDecimal("400000"), 0);
        // Assert
        assertTrue(eligible);
    }

    @Test
    public void isEligibleForReturn_salariedWellAboveThreshold_returnsTrue() {
        // Arrange
        TaxCalculator c = new TaxCalculator();
        // Act
        boolean eligible = c.isEligibleForReturn(new BigDecimal("900000"), 35);
        // Assert
        assertTrue(eligible);
    }

    // ---------------------------------------------------------------
    // Session 5A Part D -- targeted test to kill a surviving mutant.
    // Mutant: TaxCalculator.java:41, negated conditional
    //         `gstRatePercent != 0` -> `gstRatePercent == 0`
    // ---------------------------------------------------------------

    @Test
    public void computeVAT_zeroRatedSupply_returnsNoTax() {
        // Arrange
        TaxCalculator c = new TaxCalculator();
        // Act -- 0% is a supported GST rate (zero-rated supply), not an error
        BigDecimal gst = c.computeVAT(new BigDecimal("1000"), 0);
        // Assert
        assertEquals(0, gst.compareTo(BigDecimal.ZERO));
    }
}
