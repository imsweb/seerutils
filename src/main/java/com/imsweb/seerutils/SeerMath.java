/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.seerutils;

import java.util.List;

/**
 * This class contains all the math-related method provided by the SEER*Utils project.
 * <p/>
 * Created on Apr 14, 2011 by depryf
 * @author depryf
 */
public final class SeerMath {

    /**
     * No instanciation for this class!
     * <p/>
     * Created on Feb 9, 2011 by depryf
     */
    private SeerMath() {
    }

    /**
     * Calculates the next expected value based on the input values using a linear regression. If the correlation is not good enough
     * (r-square < .5) then the result is the average of the two last values.
     * <p/>
     * Code is based on <a href="http://www.cs.princeton.edu/introcs/97data/LinearRegression.java.html">http://www.cs.princeton.edu/introcs/97data/LinearRegression.java.html</a>
     * <p/>
     * Created on Nov 29, 2010 by depryf
     * @param input of input values, can be null or empty (in which case the result will be 0.0)
     * @return the next expected value based on a linear regression
     */
    public static Double calculateRegression(List<? extends Number> input) {
        return calculateRegressionResult(input).getResult();
    }

    /**
     * Calculates the next expected value based on the input values using a linear regression. If the correlation is not good enough
     * (r-square < .5) then the result is the average of the two last values and the result's useAverage flag is set to true.
     * <p/>
     * Code is based on <a href="http://www.cs.princeton.edu/introcs/97data/LinearRegression.java.html">http://www.cs.princeton.edu/introcs/97data/LinearRegression.java.html</a>
     * <p/>
     * Created on Nov 29, 2010 by depryf
     * @param input of input values, can be null or empty (in which case the result will be 0.0)
     * @return the next expected value based on a linear regression
     */
    public static RegressionResult calculateRegressionResult(List<? extends Number> input) {
        RegressionResult result = new RegressionResult();

        // special pre-conditions
        if (input == null || input.isEmpty())
            return result;

        // we are going to use the input size a lot, so let's get it now
        int n = input.size();

        // first pass: read in data, compute xbar and ybar        
        double[] x = new double[n];
        double[] y = new double[n];
        double sumx = 0.0, sumy = 0.0;
        for (int i = 0; i < n; i++) {
            x[i] = Integer.valueOf(i).doubleValue(); // we don't have the X variables so just use 0 to number-of-value...
            y[i] = input.get(i).doubleValue();
            sumx += x[i];
            sumy += y[i];
        }
        double xbar = sumx / n;
        double ybar = sumy / n;

        // second pass: compute summary statistics
        double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
        for (int i = 0; i < n; i++) {
            xxbar += (x[i] - xbar) * (x[i] - xbar);
            yybar += (y[i] - ybar) * (y[i] - ybar);
            xybar += (x[i] - xbar) * (y[i] - ybar);
        }
        double slope = xxbar == 0.0 ? 0.0 : xybar / xxbar;
        double intercept = ybar - slope * xbar;

        // the result is the "forecast" of the next value
        result.setResult(n * slope + intercept);

        // compute the R-square and see if the input values represent a good sample; if not take the average of the last 2 values
        double ssr = 0.0; // regression sum of squares
        for (int i = 0; i < n; i++) {
            double fit = slope * x[i] + intercept;
            ssr += (fit - ybar) * (fit - ybar);
        }
        double rSquare = yybar == 0.0 ? 0.0 : ssr / yybar;
        result.setRsquare(rSquare);
        if (rSquare < 0.5 && n > 1) {
            result.setResult((input.get(n - 1).doubleValue() + input.get(n - 2).doubleValue()) / 2.0);
            result.setUseAverage(true);
        }

        // if value is tiny (or negative), consider it as 0
        if (result.getResult() < 0.0)
            result.setResult(0.0);
        if (result.getResult() - Math.floor(result.getResult()) < 0.5)
            result.setResult(Math.floor(result.getResult()));

        return result;
    }

    /**
     * Encapsulates the result of calculating a regression.
     * <p/>
     * Created on Aug 21, 2011 by Fabian
     * @author Fabian
     */
    public static class RegressionResult {

        /**
         * Result of the regression
         */
        private Double _result;

        /**
         * True if the average on the two last values had to be used instead of the full regression
         */
        private Boolean _useAverage;

        /**
         * R-square value
         */
        private Double _rsquare;

        /**
         * Constructor.
         * <p/>
         * Created on Aug 21, 2011 by Fabian
         */
        public RegressionResult() {
            _result = 0.0;
            _useAverage = Boolean.FALSE;
            _rsquare = 0.0;
        }

        /**
         * Returns the result.
         * <p/>
         * Created on Aug 21, 2011 by Fabian
         * @return the result
         */
        public Double getResult() {
            return _result;
        }

        /**
         * Sets the result.
         * <p/>
         * Created on Aug 21, 2011 by Fabian
         * @param result the result
         */
        public void setResult(Double result) {
            this._result = result;
        }

        /**
         * Returns true if the average on the two last values had to be used instead of the full regression, false otherwise.
         * <p/>
         * Created on Aug 21, 2011 by Fabian
         * @return true if the average on the two last values had to be used instead of the full regression, false otherwise.
         */
        public Boolean getUseAverage() {
            return _useAverage;
        }

        /**
         * Sets whether the average had to be used.
         * <p/>
         * Created on Aug 21, 2011 by Fabian
         * @param useAverage whether the average had to be used
         */
        public void setUseAverage(Boolean useAverage) {
            this._useAverage = useAverage;
        }

        /**
         * Returns the rsquare.
         * <p/>
         * Created on Aug 21, 2011 by Fabian
         * @return the rsquare
         */
        public Double getRsquare() {
            return _rsquare;
        }

        /**
         * Sets the rsquare.
         * <p/>
         * Created on Aug 21, 2011 by Fabian
         * @param rsquare the r-square
         */
        public void setRsquare(Double rsquare) {
            this._rsquare = rsquare;
        }
    }
}
