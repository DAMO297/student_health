package org.example.util;

import java.util.List;

/**
 * 简单线性回归工具类 (OLS)
 * 用于趋势预测
 */
public class RegressionUtil {

    public static class Result {
        public double slope;
        public double intercept;
        public double r2;

        public double predict(double x) {
            return slope * x + intercept;
        }
    }

    /**
     * 计算简单线性回归 y = ax + b
     */
    public static Result calculate(List<Double> xData, List<Double> yData) {
        if (xData == null || yData == null || xData.size() != yData.size() || xData.size() < 2) {
            return null;
        }

        int n = xData.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            sumX += xData.get(i);
            sumY += yData.get(i);
            sumXY += xData.get(i) * yData.get(i);
            sumX2 += xData.get(i) * xData.get(i);
        }

        double denominator = (n * sumX2 - sumX * sumX);
        if (denominator == 0)
            return null;

        Result result = new Result();
        result.slope = (n * sumXY - sumX * sumY) / denominator;
        result.intercept = (sumY - result.slope * sumX) / n;

        // Calculate R²
        double yMean = sumY / n;
        double ssTot = 0, ssRes = 0;
        for (int i = 0; i < n; i++) {
            ssTot += Math.pow(yData.get(i) - yMean, 2);
            ssRes += Math.pow(yData.get(i) - result.predict(xData.get(i)), 2);
        }
        result.r2 = ssTot == 0 ? 0 : 1 - (ssRes / ssTot);

        return result;
    }
}
