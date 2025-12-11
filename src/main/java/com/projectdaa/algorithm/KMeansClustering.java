package com.projectdaa.algorithm;

import java.util.List;
import java.util.Random;

import com.projectdaa.model.Student;

public class KMeansClustering {

    private int k;
    private List<Student> students;
    private double[][] centroids;
    private double[][] normalizedData;

    public KMeansClustering(int k, List<Student> students) {
        this.k = k;
        this.students = students;
        this.centroids = new double[k][3]; // 3 features: GPA, Grade, Activity
    }

    public void run() {
        if (students.isEmpty()) return;

        normalizeData();
        initializeCentroids();

        boolean converged = false;
        int maxIterations = 100;
        int iter = 0;

        while (!converged && iter < maxIterations) {
            converged = true;
            
            // Assign students to nearest centroid
            for (int i = 0; i < students.size(); i++) {
                int nearestCluster = getNearestCentroid(normalizedData[i]);
                if (students.get(i).getClusterId() != nearestCluster) {
                    students.get(i).setClusterId(nearestCluster);
                    converged = false;
                }
            }

            // Update centroids
            double[][] newCentroids = new double[k][3];
            int[] counts = new int[k];

            for (int i = 0; i < students.size(); i++) {
                int cluster = students.get(i).getClusterId();
                for (int j = 0; j < 3; j++) {
                    newCentroids[cluster][j] += normalizedData[i][j];
                }
                counts[cluster]++;
            }

            for (int i = 0; i < k; i++) {
                if (counts[i] > 0) {
                    for (int j = 0; j < 3; j++) {
                        newCentroids[i][j] /= counts[i];
                    }
                } else {
                    // Re-initialize empty cluster to a random point to avoid empty clusters
                     newCentroids[i] = normalizedData[new Random().nextInt(students.size())].clone();
                }
            }
            
            // Check centroid movement (optional, but good for convergence check)
            // For now, we rely on cluster assignment stability
            centroids = newCentroids;
            iter++;
        }
        
        // Sort clusters by "quality" so 0=Low, 1=Med, 2=High (approx)
        // We can sum the centroid coordinates to estimate quality
        sortClusters();
    }

    private void normalizeData() {
        normalizedData = new double[students.size()][3];
        double[] min = {Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
        double[] max = {Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE};

        // Find min/max
        for (Student s : students) {
            double[] v = s.getVector();
            for (int j = 0; j < 3; j++) {
                if (v[j] < min[j]) min[j] = v[j];
                if (v[j] > max[j]) max[j] = v[j];
            }
        }

        // Normalize
        for (int i = 0; i < students.size(); i++) {
            double[] v = students.get(i).getVector();
            for (int j = 0; j < 3; j++) {
                if (max[j] - min[j] == 0) {
                    normalizedData[i][j] = 0.5; // Default if all values are same
                } else {
                    normalizedData[i][j] = (v[j] - min[j]) / (max[j] - min[j]);
                }
            }
        }
    }

    private void initializeCentroids() {
        Random rand = new Random();
        // K-Means++ or simple random initialization
        // Simple random for now
        for (int i = 0; i < k; i++) {
            centroids[i] = normalizedData[rand.nextInt(students.size())].clone();
        }
    }

    private int getNearestCentroid(double[] point) {
        int nearest = -1;
        double minDist = Double.MAX_VALUE;

        for (int i = 0; i < k; i++) {
            double dist = 0;
            for (int j = 0; j < 3; j++) {
                dist += Math.pow(point[j] - centroids[i][j], 2);
            }
            if (dist < minDist) {
                minDist = dist;
                nearest = i;
            }
        }
        return nearest;
    }
    
    private void sortClusters() {
        // Calculate magnitude of each centroid
        double[] magnitudes = new double[k];
        Integer[] indices = new Integer[k];
        for(int i=0; i<k; i++) {
            indices[i] = i;
            for(int j=0; j<3; j++) {
                magnitudes[i] += centroids[i][j];
            }
        }
        
        // Sort indices based on magnitudes
        java.util.Arrays.sort(indices, (a, b) -> Double.compare(magnitudes[a], magnitudes[b]));
        
        // Remap student cluster IDs
        // indices[0] is the index of the "lowest" cluster
        // We want indices[0] -> 0, indices[1] -> 1, etc.
        int[] map = new int[k];
        for(int i=0; i<k; i++) {
            map[indices[i]] = i;
        }
        
        for(Student s : students) {
            s.setClusterId(map[s.getClusterId()]);
        }
    }
}
