package org.example.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 极简 K-Means 聚类实现
 * 用于演示风险人群识别
 */
public class ClusteringUtil {

    public static class Point {
        public double[] coords;
        public int clusterId = -1;

        public Point(double[] coords) {
            this.coords = coords;
        }
    }

    public static List<Point> cluster(List<double[]> data, int k, int maxIterations) {
        if (data == null || data.isEmpty())
            return new ArrayList<>();

        List<Point> points = new ArrayList<>();
        for (double[] d : data)
            points.add(new Point(d));

        // 1. Initialize centroids randomly
        List<double[]> centroids = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < k; i++) {
            centroids.add(data.get(random.nextInt(data.size())).clone());
        }

        for (int iter = 0; iter < maxIterations; iter++) {
            boolean changed = false;

            // 2. Assign points to nearest centroid
            for (Point p : points) {
                int nearest = 0;
                double minDist = distance(p.coords, centroids.get(0));
                for (int i = 1; i < k; i++) {
                    double dist = distance(p.coords, centroids.get(i));
                    if (dist < minDist) {
                        minDist = dist;
                        nearest = i;
                    }
                }
                if (p.clusterId != nearest) {
                    p.clusterId = nearest;
                    changed = true;
                }
            }

            if (!changed)
                break;

            // 3. Update centroids
            for (int i = 0; i < k; i++) {
                double[] sum = new double[centroids.get(0).length];
                int count = 0;
                for (Point p : points) {
                    if (p.clusterId == i) {
                        for (int j = 0; j < sum.length; j++)
                            sum[j] += p.coords[j];
                        count++;
                    }
                }
                if (count > 0) {
                    for (int j = 0; j < sum.length; j++)
                        centroids.get(i)[j] = sum[j] / count;
                }
            }
        }
        return points;
    }

    private static double distance(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += Math.pow(a[i] - b[i], 2);
        }
        return Math.sqrt(sum);
    }
}
