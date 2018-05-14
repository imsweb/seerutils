/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.seerutils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.imsweb.seerutils.SeerMath.RegressionResult;

/**
 * Created on Feb 15, 2011 by may
 * @author may
 */
public class SeerMathTest {

    @Test
    public void testCalculateRegression() {
        // null inputs
        assertRegressionResult(0.0, null);
        // empty inputs
        assertRegressionResult(0.0, new int[] {});
        // single input (testing division by 0)
        assertRegressionResult(10.0, new int[] {10});
        // any single value should give 0
        assertRegressionResult(1.0, new int[] {1});
        // one negative value
        assertRegressionResult(0.0, new int[] {-1});
        // several negative values
        assertRegressionResult(0.0, new int[] {-1, -2, -3});
        // one negative values, other positive
        assertRegressionResult(0.5, new int[] {1, -2, 3}, Boolean.TRUE);
        // one zero value
        assertRegressionResult(0.0, new int[] {0});
        // several zero values
        assertRegressionResult(0.0, new int[] {0, 0, 0});
        // all values the same
        assertRegressionResult(10.0, new int[] {10, 10, 10, 10, 10});
        // perfect linear regression
        assertRegressionResult(6.0, new int[] {1, 2, 3, 4, 5});
        // end result negative
        assertRegressionResult(0.0, new int[] {20, 9});
        // regular case
        assertRegressionResult(8.0, new int[] {10, 9});
        // another regular case
        assertRegressionResult(6.0, new int[] {12, 9});
        // another regular case
        assertRegressionResult(3.0, new int[] {23, 20, 9});
        // more complicated case from real application
        assertRegressionResult(23806.5, new int[] {23173, 23149, 23194, 23884, 24283, 23423, 23205, 23112, 23420, 24193});
        // more complicated case from real application
        assertRegressionResult(2151.5, new int[] {2110, 2193, 2184, 2344, 2449, 2097, 2037, 2204, 2161, 2142});
        // more complicated case from real application
        assertRegressionResult(21655.0, new int[] {23063, 20956, 21010, 21540, 21834, 21326, 21168, 20908, 21259, 22051});
        // random values taken from the real application (used to now work correctly)
        assertRegressionResult(5.0, new int[] {8, 4, 5, 2, 6, 5, 5, 7, 4, 6}, Boolean.TRUE);
        // another real test...
        assertRegressionResult(2927.8, new int[] {1950, 2119, 2257, 2385, 2471, 2457, 2557, 2679, 2756, 2785}, Boolean.FALSE);
    }

    private void assertRegressionResult(double expected, int[] values) {
        assertRegressionResult(expected, values, null);
    }

    private void assertRegressionResult(double expected, int[] values, Boolean expectedUseAverageFlag) {
        if (values == null)
            Assert.assertEquals(expected, SeerMath.calculateRegression(null), 0);
        else if (values.length == 0)
            Assert.assertEquals(expected, SeerMath.calculateRegression(Collections.<Long>emptyList()), 0);
        else {
            List<Long> input = new ArrayList<>();
            for (int i : values)
                input.add((long)i);
            Assert.assertEquals(expected, SeerMath.calculateRegression(input), 0);
            // test second flavor of the method
            RegressionResult result = SeerMath.calculateRegressionResult(input);
            Assert.assertEquals(expected, result.getResult(), 0);
            if (expectedUseAverageFlag != null)
                Assert.assertEquals(expectedUseAverageFlag, result.getUseAverage());
        }
    }
}
