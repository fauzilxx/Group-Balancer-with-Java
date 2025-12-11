package com.projectdaa.algorithm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.projectdaa.model.Student;

public class GroupBalancer {

    public Map<Integer, List<Student>> balanceGroups(List<Student> allStudents, int numGroups) {
        // 1. Run K-Means
        KMeansClustering kMeans = new KMeansClustering(3, allStudents);
        kMeans.run();

        // 2. Separate into clusters
        List<Student> high = new ArrayList<>();
        List<Student> med = new ArrayList<>();
        List<Student> low = new ArrayList<>();

        for (Student s : allStudents) {
            if (s.isExpert()) {
                high.add(s); // Force expert to high bucket for distribution
            } else if (s.getClusterId() == 2) high.add(s);
            else if (s.getClusterId() == 1) med.add(s);
            else low.add(s);
        }

        // Sort within clusters by score descending (optional, but helps deterministic behavior)
        Comparator<Student> scoreComp = (s1, s2) -> Double.compare(getScore(s2), getScore(s1));
        high.sort(scoreComp);
        med.sort(scoreComp);
        low.sort(scoreComp);

        // 3. Initialize Groups
        Map<Integer, List<Student>> groups = new HashMap<>();
        double[] groupScores = new double[numGroups];
        for (int i = 0; i < numGroups; i++) {
            groups.put(i, new ArrayList<>());
            groupScores[i] = 0.0;
        }

        // 4. Distribute - High, then Med, then Low
        distributeCluster(high, groups, groupScores, numGroups);
        distributeCluster(med, groups, groupScores, numGroups);
        distributeCluster(low, groups, groupScores, numGroups);

        return groups;
    }

    private void distributeCluster(List<Student> clusterStudents, Map<Integer, List<Student>> groups, double[] groupScores, int numGroups) {
        int studentIdx = 0;
        while (studentIdx < clusterStudents.size()) {
            // Take a batch of students (up to numGroups)
            int batchSize = Math.min(numGroups, clusterStudents.size() - studentIdx);
            List<Student> batch = new ArrayList<>();
            for (int i = 0; i < batchSize; i++) {
                batch.add(clusterStudents.get(studentIdx + i));
            }

            // Create Cost Matrix
            // Rows: Students in batch
            // Cols: Groups
            // Cost[i][j] = (groupScores[j] + studentScore)^2
            // We want to minimize the sum of squares of the resulting group scores to keep them balanced.
            
            double[][] costMatrix = new double[batchSize][numGroups];
            for (int i = 0; i < batchSize; i++) {
                double sScore = getScore(batch.get(i));
                for (int j = 0; j < numGroups; j++) {
                    // We use the square of the new total score as the cost.
                    // Minimizing sum(new_score^2) is equivalent to minimizing variance.
                    double newScore = groupScores[j] + sScore;
                    costMatrix[i][j] = newScore * newScore;
                }
            }

            // Solve Hungarian
            HungarianAlgorithm hungarian = new HungarianAlgorithm(costMatrix);
            int[] assignment = hungarian.execute();

            // Assign
            for (int i = 0; i < batchSize; i++) {
                int groupIdx = assignment[i];
                if (groupIdx != -1 && groupIdx < numGroups) {
                    Student s = batch.get(i);
                    groups.get(groupIdx).add(s);
                    groupScores[groupIdx] += getScore(s);
                } else {
                    // Should not happen if batchSize <= numGroups, unless Hungarian fails or pads weirdly.
                    // Fallback: assign to group with min score
                    int minGroup = getMinScoreGroup(groupScores);
                    Student s = batch.get(i);
                    groups.get(minGroup).add(s);
                    groupScores[minGroup] += getScore(s);
                }
            }

            studentIdx += batchSize;
        }
    }

    private double getScore(Student s) {
        if (s.isExpert()) return 1000.0; // Give experts a very high score to prioritize them
        
        // Simple weighted score. Since we don't have the normalized values from KMeans accessible here easily 
        // (unless we store them in Student), we'll do a quick normalization or just use raw values if they are comparable.
        // User said: IPK (0-4), Grade (0-100?), Activity (0-100?).
        // Let's normalize roughly: IPK*25, Grade, Activity.
        // Or better, just sum them if we assume user inputs 0-100 for all or we handle it.
        // Let's assume inputs are: GPA (0-4.0), Grade (0-100), Activity (0-100).
        
        double normGpa = (s.getGpa() / 4.0) * 100;
        return (normGpa + s.getPreviousGrade() + s.getActivityScore()) / 3.0;
    }

    private int getMinScoreGroup(double[] scores) {
        int minIdx = 0;
        double minVal = scores[0];
        for(int i=1; i<scores.length; i++) {
            if(scores[i] < minVal) {
                minVal = scores[i];
                minIdx = i;
            }
        }
        return minIdx;
    }
}
