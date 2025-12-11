package com.projectdaa.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.projectdaa.algorithm.GroupBalancer;
import com.projectdaa.model.Student;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

public class MainView {

    private BorderPane root;
    private TextField nameField;
    private TextField gpaField;
    private TextField prevGradeField;
    private TextField activityField;
    private CheckBox expertCheckBox;
    private TableView<Student> studentTable;
    private Spinner<Integer> groupSpinner;
    private TextArea resultArea;
    private ObservableList<Student> studentList = FXCollections.observableArrayList();

    public MainView() {
        createView();
    }

    public Parent getView() {
        return root;
    }

    private void createView() {
        root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #E3F2FD;"); // Light Blue Background

        // --- Top: Header ---
        Label headerLabel = new Label("EquiTeam");
        headerLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        headerLabel.setStyle("-fx-text-fill: #1565C0;"); // Dark Blue Text
        BorderPane.setAlignment(headerLabel, Pos.CENTER);
        BorderPane.setMargin(headerLabel, new Insets(0, 0, 20, 0));
        root.setTop(headerLabel);

        // --- Center: Input, Table, Controls, Results ---
        VBox centerBox = new VBox(15);
        centerBox.setPadding(new Insets(15));
        centerBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        // 1. Input Fields
        HBox inputBox = new HBox(10);
        inputBox.setAlignment(Pos.CENTER_LEFT);

        nameField = styleTextField(new TextField(), "Name");
        gpaField = styleTextField(new TextField(), "GPA (0.0 - 4.0)");
        gpaField.setPrefWidth(120);
        prevGradeField = styleTextField(new TextField(), "Prev Grade (0-100)");
        prevGradeField.setPrefWidth(140);
        activityField = styleTextField(new TextField(), "Activity (0-100)");
        activityField.setPrefWidth(140);

        expertCheckBox = new CheckBox("Expert/Jago");
        expertCheckBox.setStyle("-fx-font-weight: bold; -fx-text-fill: #1565C0;");

        Button addButton = new Button("Add Student");
        styleButton(addButton, "#1976D2", "white"); // Blue Button
        addButton.setOnAction(e -> handleAddStudent());

        Button importButton = new Button("Import Excel");
        styleButton(importButton, "#2E7D32", "white"); // Green Button
        importButton.setOnAction(e -> handleImportExcel());

        inputBox.getChildren().addAll(nameField, gpaField, prevGradeField, activityField, expertCheckBox, addButton, importButton);

        // 2. Table View
        studentTable = new TableView<>();
        studentTable.setPrefHeight(250);
        studentTable.setStyle("-fx-border-color: #BBDEFB; -fx-border-radius: 5;");
        VBox.setVgrow(studentTable, Priority.ALWAYS);

        TableColumn<Student, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);

        TableColumn<Student, Double> gpaCol = new TableColumn<>("GPA");
        gpaCol.setCellValueFactory(new PropertyValueFactory<>("gpa"));
        gpaCol.setPrefWidth(100);

        TableColumn<Student, Double> prevGradeCol = new TableColumn<>("Previous Grade");
        prevGradeCol.setCellValueFactory(new PropertyValueFactory<>("previousGrade"));
        prevGradeCol.setPrefWidth(150);

        TableColumn<Student, Double> activityCol = new TableColumn<>("Activity Score");
        activityCol.setCellValueFactory(new PropertyValueFactory<>("activityScore"));
        activityCol.setPrefWidth(150);

        TableColumn<Student, Boolean> expertCol = new TableColumn<>("Expert");
        expertCol.setCellValueFactory(new PropertyValueFactory<>("expert"));
        expertCol.setPrefWidth(80);

        TableColumn<Student, Integer> clusterCol = new TableColumn<>("Cluster");
        clusterCol.setCellValueFactory(new PropertyValueFactory<>("clusterId"));
        clusterCol.setPrefWidth(100);

        studentTable.getColumns().addAll(nameCol, gpaCol, prevGradeCol, activityCol, expertCol, clusterCol);
        studentTable.setItems(studentList);

        // 3. Generation Controls
        HBox controlsBox = new HBox(15);
        controlsBox.setAlignment(Pos.CENTER_LEFT);
        controlsBox.setPadding(new Insets(10, 0, 0, 0));

        Label groupLabel = new Label("Number of Groups:");
        groupLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        
        groupSpinner = new Spinner<>(2, 20, 3);
        groupSpinner.setPrefWidth(80);
        groupSpinner.setStyle("-fx-body-color: #BBDEFB;");

        Button generateButton = new Button("Generate Groups");
        styleButton(generateButton, "#1565C0", "white"); // Darker Blue
        generateButton.setFont(Font.font("System", FontWeight.BOLD, 14));
        generateButton.setOnAction(e -> handleGenerateGroups());

        Button clearButton = new Button("Clear All");
        styleButton(clearButton, "#D32F2F", "white"); // Red
        clearButton.setOnAction(e -> handleClear());

        controlsBox.getChildren().addAll(groupLabel, groupSpinner, generateButton, clearButton);

        // 4. Results Area
        Label resultLabel = new Label("Results:");
        resultLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        resultLabel.setStyle("-fx-text-fill: #1565C0;");
        
        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setPrefHeight(200);
        resultArea.setFont(Font.font("Monospaced", 13));
        resultArea.setStyle("-fx-control-inner-background: #F5F5F5; -fx-border-color: #BBDEFB;");
        VBox.setVgrow(resultArea, Priority.ALWAYS);

        centerBox.getChildren().addAll(inputBox, studentTable, controlsBox, resultLabel, resultArea);
        root.setCenter(centerBox);
    }

    private TextField styleTextField(TextField tf, String prompt) {
        tf.setPromptText(prompt);
        tf.setStyle("-fx-background-radius: 5; -fx-border-color: #BBDEFB; -fx-border-radius: 5;");
        return tf;
    }

    private void styleButton(Button btn, String bgColor, String textColor) {
        btn.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: " + textColor + "; -fx-background-radius: 5; -fx-cursor: hand; -fx-font-weight: bold;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: derive(" + bgColor + ", 20%); -fx-text-fill: " + textColor + "; -fx-background-radius: 5; -fx-cursor: hand; -fx-font-weight: bold;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: " + textColor + "; -fx-background-radius: 5; -fx-cursor: hand; -fx-font-weight: bold;"));
    }

    private void handleAddStudent() {
        try {
            String name = nameField.getText();
            String gpaText = gpaField.getText();
            String prevGradeText = prevGradeField.getText();
            String activityText = activityField.getText();

            if (name.isEmpty() || gpaText.isEmpty() || prevGradeText.isEmpty() || activityText.isEmpty()) {
                showAlert("Error", "All fields must be filled.");
                return;
            }

            double gpa = Double.parseDouble(gpaText);
            double prevGrade = Double.parseDouble(prevGradeText);
            double activity = Double.parseDouble(activityText);

            if (gpa < 0 || gpa > 4.0) {
                showAlert("Error", "GPA must be between 0.0 and 4.0.");
                return;
            }

            boolean isExpert = expertCheckBox.isSelected();
            Student student = new Student(name, gpa, prevGrade, activity, isExpert);
            studentList.add(student);
            clearFields();

        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid numeric values for GPA, Grade, and Activity.");
        }
    }

    private void handleImportExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls"));
        File selectedFile = fileChooser.showOpenDialog(root.getScene().getWindow());
        if (selectedFile != null) {
            try (FileInputStream fis = new FileInputStream(selectedFile);
                 Workbook workbook = new XSSFWorkbook(fis)) {
                Sheet sheet = workbook.getSheetAt(0);
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue; // Skip header
                    try {
                        String name = "";
                        double gpa = 0.0;
                        double prevGrade = 0.0;
                        double activity = 0.0;

                        Cell nameCell = row.getCell(0);
                        if (nameCell != null) name = nameCell.getStringCellValue();

                        Cell gpaCell = row.getCell(1);
                        if (gpaCell != null) gpa = gpaCell.getNumericCellValue();

                        Cell prevGradeCell = row.getCell(2);
                        if (prevGradeCell != null) prevGrade = prevGradeCell.getNumericCellValue();

                        Cell activityCell = row.getCell(3);
                        if (activityCell != null) activity = activityCell.getNumericCellValue();

                        boolean isExpert = false;
                        Cell expertCell = row.getCell(4);
                        if (expertCell != null) {
                            if (expertCell.getCellType() == org.apache.poi.ss.usermodel.CellType.BOOLEAN) {
                                isExpert = expertCell.getBooleanCellValue();
                            } else if (expertCell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                                String val = expertCell.getStringCellValue().toLowerCase();
                                isExpert = val.equals("yes") || val.equals("true") || val.equals("y");
                            } else if (expertCell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                                isExpert = expertCell.getNumericCellValue() == 1.0;
                            }
                        }

                        if (!name.isEmpty()) {
                            studentList.add(new Student(name, gpa, prevGrade, activity, isExpert));
                        }
                    } catch (Exception e) {
                        System.out.println("Skipping invalid row: " + row.getRowNum() + " - " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                showAlert("Error", "Failed to load Excel file: " + e.getMessage());
            }
        }
    }

    private void handleGenerateGroups() {
        if (studentList.isEmpty()) {
            showAlert("Warning", "No students to group.");
            return;
        }

        int numGroups = groupSpinner.getValue();
        if (numGroups > studentList.size()) {
            showAlert("Error", "Number of groups cannot be greater than number of students.");
            return;
        }

        GroupBalancer balancer = new GroupBalancer();
        List<Student> students = new ArrayList<>(studentList);
        
        Map<Integer, List<Student>> groups = balancer.balanceGroups(students, numGroups);

        // Refresh table to show cluster IDs
        studentTable.refresh();

        // Display results
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, List<Student>> entry : groups.entrySet()) {
            sb.append("Group ").append(entry.getKey() + 1).append(":\n");
            for (Student s : entry.getValue()) {
                String expertMark = s.isExpert() ? " [EXPERT]" : "";
                sb.append(String.format("  - %-10s [GPA: %.2f, Grade: %.1f, Act: %.1f, Cluster: %d]%s\n", 
                    s.getName(), s.getGpa(), s.getPreviousGrade(), s.getActivityScore(), s.getClusterId(), expertMark));
            }
            sb.append("\n");
        }
        resultArea.setText(sb.toString());
    }

    private void handleClear() {
        studentList.clear();
        resultArea.clear();
    }

    private void clearFields() {
        nameField.clear();
        gpaField.clear();
        prevGradeField.clear();
        activityField.clear();
        expertCheckBox.setSelected(false);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
