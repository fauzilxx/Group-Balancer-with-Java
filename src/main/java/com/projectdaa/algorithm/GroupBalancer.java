package com.projectdaa.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.projectdaa.model.Student;

public class GroupBalancer {

    public Map<Integer, List<Student>> balanceGroups(List<Student> allStudents, int numGroups) {
        // 1. Run K-Means for clustering analysis
        KMeansClustering kMeans = new KMeansClustering(3, allStudents);
        kMeans.run();

        // 2. Separate students by cluster AND expert status
        List<Student> cluster0 = new ArrayList<>(); // Low performers
        List<Student> cluster1 = new ArrayList<>(); // Medium performers
        List<Student> cluster2 = new ArrayList<>(); // High performers
        
        for (Student s : allStudents) {
            if (s.getClusterId() == 0) cluster0.add(s);
            else if (s.getClusterId() == 1) cluster1.add(s);
            else cluster2.add(s);
        }

        // Sort each cluster by score (ascending for balanced distribution)
        cluster0.sort((a, b) -> Double.compare(calculateScore(a), calculateScore(b)));
        cluster1.sort((a, b) -> Double.compare(calculateScore(a), calculateScore(b)));
        cluster2.sort((a, b) -> Double.compare(calculateScore(a), calculateScore(b)));

        // 3. Initialize Groups
        Map<Integer, List<Student>> groups = new HashMap<>();
        double[] groupScores = new double[numGroups];
        int[] clusterCount = new int[numGroups]; // Track cluster 2 (high) count per group
        
        for (int i = 0; i < numGroups; i++) {
            groups.put(i, new ArrayList<>());
            groupScores[i] = 0.0;
            clusterCount[i] = 0;
        }

        // 4. STRATEGY: Distribute HIGH performers (Cluster 2) FIRST using zigzag
        // This ensures each group gets balanced mix of high performers
        distributeZigZag(cluster2, groups, groupScores, clusterCount, numGroups);

        // 5. Then distribute MEDIUM performers (Cluster 1) to balance
        distributeBalanced(cluster1, groups, groupScores, numGroups);

        // 6. Finally distribute LOW performers (Cluster 0) to fill gaps
        distributeBalanced(cluster0, groups, groupScores, numGroups);

        return groups;
    }

    /**
     * ZigZag distribution: 1, 2, 3, 3, 2, 1, 1, 2, 3...
     * This prevents high performers from clustering together
     */
    private void distributeZigZag(List<Student> students, Map<Integer, List<Student>> groups, 
                                   double[] groupScores, int[] clusterCount, int numGroups) {
        int idx = 0;
        boolean forward = true;
        
        for (Student s : students) {
            groups.get(idx).add(s);
            groupScores[idx] += calculateScore(s);
            clusterCount[idx]++;
            
            // ZigZag pattern
            if (forward) {
                idx++;
                if (idx >= numGroups) {
                    idx = numGroups - 1;
                    forward = false;
                }
            } else {
                idx--;
                if (idx < 0) {
                    idx = 0;
                    forward = true;
                }
            }
        }
    }

    /**
     * Balanced distribution: Always add to group with lowest score
     */
    private void distributeBalanced(List<Student> students, Map<Integer, List<Student>> groups, 
                                     double[] groupScores, int numGroups) {
        for (Student s : students) {
            int targetGroup = findLowestScoreGroup(groupScores, numGroups);
            groups.get(targetGroup).add(s);
            groupScores[targetGroup] += calculateScore(s);
        }
    }

    private double calculateScore(Student s) {
        // Normalize all components to 0-100 scale
        double normGpa = (s.getGpa() / 4.0) * 100;
        double normGrade = s.getPreviousGrade(); // Already 0-100
        double normActivity = s.getActivityScore(); // Already 0-100
        
        // Weighted average: GPA (40%), Previous Grade (35%), Activity (25%)
        double baseScore = (normGpa * 0.40) + (normGrade * 0.35) + (normActivity * 0.25);
        
        // Expert gets multiplier to ensure they're distributed well
        if (s.isExpert()) {
            baseScore = baseScore * 1.20; // 20% boost for better distribution
        }
        
        return baseScore;
    }

    private int findLowestScoreGroup(double[] groupScores, int numGroups) {
        int minIdx = 0;
        double minScore = groupScores[0];
        
        for (int i = 1; i < numGroups; i++) {
            if (groupScores[i] < minScore) {
                minScore = groupScores[i];
                minIdx = i;
            }
        }
        
        return minIdx;
    }
}
