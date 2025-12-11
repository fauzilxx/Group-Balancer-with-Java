package com.projectdaa.model;

public class Student {
    private String name;
    private double gpa; // IPK Terakhir
    private double previousGrade; // Nilai Mata Kuliah Semester Sebelumnya
    private double activityScore; // Keaktifan
    private boolean isExpert; // Status Expert/Jago
    private int clusterId = -1; // 0, 1, 2 (Low, Med, High)

    public Student(String name, double gpa, double previousGrade, double activityScore) {
        this(name, gpa, previousGrade, activityScore, false);
    }

    public Student(String name, double gpa, double previousGrade, double activityScore, boolean isExpert) {
        this.name = name;
        this.gpa = gpa;
        this.previousGrade = previousGrade;
        this.activityScore = activityScore;
        this.isExpert = isExpert;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getGpa() {
        return gpa;
    }

    public void setGpa(double gpa) {
        this.gpa = gpa;
    }

    public double getPreviousGrade() {
        return previousGrade;
    }

    public void setPreviousGrade(double previousGrade) {
        this.previousGrade = previousGrade;
    }

    public double getActivityScore() {
        return activityScore;
    }

    public void setActivityScore(double activityScore) {
        this.activityScore = activityScore;
    }

    public boolean isExpert() {
        return isExpert;
    }

    public void setExpert(boolean expert) {
        isExpert = expert;
    }

    public int getClusterId() {
        return clusterId;
    }

    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

    public double[] getVector() {
        return new double[]{gpa, previousGrade, activityScore};
    }

    @Override
    public String toString() {
        return name + " (GPA:" + gpa + ", Grade:" + previousGrade + ", Act:" + activityScore + ", Expert:" + isExpert + ")";
    }
}
