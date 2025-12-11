package com.projectdaa.main;

import com.projectdaa.view.MainView;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        MainView mainView = new MainView();
        Scene scene = new Scene(mainView.getView(), 900, 600);
        
        primaryStage.setTitle("EquiTeam");
        
        // Set Application Icon
        try {
            // Ensure you have 'image.png' in src/main/resources/com/projectdaa/view/
            Image icon = new Image(getClass().getResourceAsStream("/com/projectdaa/view/image.png"));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("Icon not found: " + e.getMessage());
        }

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
