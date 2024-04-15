/**
 * File: Main.java
 * Author: Kevin Tran
 * Created on: March, 13, 2024
 * Last Modified: April, 04, 2024
 * Description: Main Class of Currency Converter App
 */
package app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


import java.io.InputStream;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/app/app.fxml"));
        primaryStage.setTitle("Currency Converter");
        primaryStage.setScene(new Scene(root, 540, 760));
        primaryStage.setResizable(false);

        // Load Window Icon

        InputStream iconStream = getClass().getResourceAsStream("/resources/iconLogo.png");
        if (iconStream != null) {
            Image icon = new Image(iconStream);
            primaryStage.getIcons().add(icon);
        } else {
            System.out.println("Icon file not found");
        }


        // Ends the program when window is closed
        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });

        primaryStage.show();
    }


    public static void main(String[] args) {
       launch(args);
    }
}
