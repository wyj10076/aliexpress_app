package com.app.aliexpress;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    public void start(Stage stage) throws Exception {
  
    	String fxmlFile = "/view/main.fxml";
        Parent rootNode = (Parent) FXMLLoader.load(MainApp.class.getResource(fxmlFile));

        Scene scene = new Scene(rootNode);

        stage.setTitle("Ali Express");
        stage.setScene(scene);
        stage.show();
    }
}
