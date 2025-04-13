package shop.myshop;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.io.IOException;
import java.sql.*;

public class Main extends Application {
    private static Connection conn;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Shop.fxml"));
        fxmlLoader.setControllerFactory(c -> new SellController(conn));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.show();
    }

    private static void connectToDb() {
        String url = "jdbc:sqlite:products";

        try {
            conn = DriverManager.getConnection(url);
            System.out.println("Connection succesfully");
        } catch (SQLException e) {
            System.out.println("Error connecting to a database: "
                    + e.getMessage() + "\nExiting...");

            Platform.exit();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        connectToDb();
        launch();
    }
}