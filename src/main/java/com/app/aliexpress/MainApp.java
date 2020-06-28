package com.app.aliexpress;

import java.io.IOException;

import com.app.util.Config;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static void main(String[] args) {
    	new Config();
        launch(args);
    }

    public void start(Stage stage) throws Exception {
    	String fxmlFile = "/view/Main.fxml";
        Parent rootNode = (Parent) FXMLLoader.load(MainApp.class.getResource(fxmlFile));

        Scene scene = new Scene(rootNode);

        stage.setTitle("알리 익스프레스");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.sizeToScene();

        stage.show();
    }
    
    @Override
    public void stop() throws Exception {
    	super.stop();
    	
    	try {
			Runtime.getRuntime().exec("taskkill /f /im chromedriver.exe /t");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
