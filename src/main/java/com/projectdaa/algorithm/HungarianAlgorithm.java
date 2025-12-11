package com.projectdaa.algorithm;

import java.util.Arrays;

public class HungarianAlgorithm {
    private final double[][] costMatrix;
    private final int rows, cols, dim;
    private final double[] labelByWorker, labelByJob;
    private final int[] minSlackWorkerByJob;
    private final double[] minSlackValueByJob;
    private final int[] matchJobByWorker, matchWorkerByJob;
    private final int[] parentWorkerByJob;
    private final boolean[] committedWorkers;

    public HungarianAlgorithm(double[][] costMatrix) {
        this.dim = Math.max(costMatrix.length, costMatrix[0].length);
        this.rows = costMatrix.length;
        this.cols = costMatrix[0].length;
        this.costMatrix = new double[dim][dim];
        for (int w = 0; w < this.dim; w++) {
            if (w < costMatrix.length) {
                if (costMatrix[w].length != this.cols) {
                    throw new IllegalArgumentException("Irregular cost matrix");
                }
                for (int j = 0; j < this.cols; j++) {
                    this.costMatrix[w][j] = costMatrix[w][j];
                }
            } else {
                // Pad with 0 or large number? 
                // For assignment problem where we want min cost, padding with 0 might be tricky if we want to force assignment.
                // But usually padding with 0 is fine for dummy workers/jobs.
            }
        }
        labelByWorker = new double[
            this.dim
        ];
        labelByJob = new double[
            this.dim
        ];
        minSlackWorkerByJob = new int[
            this.dim
        ];
        minSlackValueByJob = new double[
            this.dim
        ];
        committedWorkers = new boolean[
            this.dim
        ];
        parentWorkerByJob = new int[
            this.dim
        ];
        matchJobByWorker = new int[
            this.dim
        ];
        Arrays.fill(matchJobByWorker, -1);
        matchWorkerByJob = new int[
            this.dim
        ];
        Arrays.fill(matchWorkerByJob, -1);
    }

    public int[] execute() {
        /*
         * Heuristics to improve performance: Reduce rows and columns by their smallest element, 
         * compute an initial non-zero dual feasible solution and 
         * create a greedy matching from workers to jobs of the cost matrix.
         */
        reduce();
        computeInitialFeasibleSolution();
        greedyMatch();

        int w = fetchUnmatchedWorker();
        while (w < dim) {
            initializePhase(w);
            executePhase();
            w = fetchUnmatchedWorker();
        }
        int[] result = Arrays.copyOf(matchJobByWorker, rows);
        for (int i = 0; i < result.length; i++) {
            if (result[i] >= cols) {
                result[i] = -1;
            }
        }
        return result;
    }

    protected void executePhase() {
        while (true) {
            int minSlackWorker = -1, minSlackJob = -1;
            double minSlackValue = Double.POSITIVE_INFINITY;
            for (int j = 0; j < dim; j++) {
                if (parentWorkerByJob[j] == -1) {
                    if (minSlackValueByJob[j] < minSlackValue) {
                        minSlackValue = minSlackValueByJob[j];
                        minSlackWorker = minSlackWorkerByJob[j];
                        minSlackJob = j;
                    }
                }
            }
            if (minSlackValue > 0) {
                updateLabeling(minSlackValue);
            }
            parentWorkerByJob[minSlackJob] = minSlackWorker;
            if (matchWorkerByJob[minSlackJob] == -1) {
                /*
                 * An augmenting path has been found.
                 */
                int committedJob = minSlackJob;
                int parentWorker = parentWorkerByJob[committedJob];
                while (true) {
                    int temp = matchJobByWorker[parentWorker];
                    match(parentWorker, committedJob);
                    committedJob = temp;
                    if (committedJob == -1) {
                        break;
                    }
                    parentWorker = parentWorkerByJob[committedJob];
                }
                return;
            } else {
                /*
                 * Update slack values since we increase the equality subgraph.
                 */
                int worker = matchWorkerByJob[minSlackJob];
                committedWorkers[worker] = true;
                for (int j = 0; j < dim; j++) {
                    if (parentWorkerByJob[j] == -1) {
                        double slack = costMatrix[worker][j] - labelByWorker[worker] - labelByJob[j];
                        if (minSlackValueByJob[j] > slack) {
                            minSlackValueByJob[j] = slack;
                            minSlackWorkerByJob[j] = worker;
                        }
                    }
                }
            }
        }
    }

    protected int fetchUnmatchedWorker() {
        int w;
        for (w = 0; w < dim; w++) {
            if (matchJobByWorker[w] == -1) {
                break;
            }
        }
        return w;
    }

    protected void greedyMatch() {
        for (int w = 0; w < dim; w++) {
            for (int j = 0; j < dim; j++) {
                if (matchJobByWorker[w] == -1 && matchWorkerByJob[j] == -1
                        && costMatrix[w][j] - labelByWorker[w] - labelByJob[j] == 0) {
                    match(w, j);
                }
            }
        }
    }

    protected void initializePhase(int w) {
        Arrays.fill(committedWorkers, false);
        Arrays.fill(parentWorkerByJob, -1);
        committedWorkers[w] = true;
        for (int j = 0; j < dim; j++) {
            minSlackValueByJob[j] = costMatrix[w][j] - labelByWorker[w]
                    - labelByJob[j];
            minSlackWorkerByJob[j] = w;
        }
    }

    protected void match(int w, int j) {
        matchJobByWorker[w] = j;
        matchWorkerByJob[j] = w;
    }

    protected void reduce() {
        for (int w = 0; w < dim; w++) {
            double min = Double.POSITIVE_INFINITY;
            for (int j = 0; j < dim; j++) {
                if (costMatrix[w][j] < min) {
                    min = costMatrix[w][j];
                }
            }
            for (int j = 0; j < dim; j++) {
                costMatrix[w][j] -= min;
            }
        }
        double[] min = new double[dim];
        Arrays.fill(min, Double.POSITIVE_INFINITY);
        for (int w = 0; w < dim; w++) {
            for (int j = 0; j < dim; j++) {
                if (costMatrix[w][j] < min[j]) {
                    min[j] = costMatrix[w][j];
                }
            }
        }
        for (int w = 0; w < dim; w++) {
            for (int j = 0; j < dim; j++) {
                costMatrix[w][j] -= min[j];
            }
        }
    }

    protected void updateLabeling(double slack) {
        for (int w = 0; w < dim; w++) {
            if (committedWorkers[w]) {
                labelByWorker[w] += slack;
            }
        }
        for (int j = 0; j < dim; j++) {
            if (parentWorkerByJob[j] != -1) {
                labelByJob[j] -= slack;
            } else {
                minSlackValueByJob[j] -= slack;
            }
        }
    }
    
    protected void computeInitialFeasibleSolution() {
        for (int j = 0; j < dim; j++) {
            labelByJob[j] = Double.POSITIVE_INFINITY;
        }
        for (int w = 0; w < dim; w++) {
            for (int j = 0; j < dim; j++) {
                if (costMatrix[w][j] < labelByJob[j]) {
                    labelByJob[j] = costMatrix[w][j];
                }
            }
        }
    }
}
