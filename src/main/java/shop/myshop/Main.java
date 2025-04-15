package shop.myshop;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.IOException;
import java.sql.*;

/**
 * Головний клас програми {@code Main}, який є точкою входу в JavaFX-додаток.
 * <p>
 * Ініціалізує з'єднання з базою даних, завантажує FXML-інтерфейс та встановлює головну сцену.
 */
public class Main extends Application {

    /**
     * З'єднання з базою даних SQLite.
     */
    private static Connection conn;

    /**
     * Метод, який викликається при запуску JavaFX-додатку.
     * Завантажує інтерфейс з FXML-файлу та встановлює контролер {@link SellController}.
     *
     * @param stage головна сцена додатку
     * @throws IOException якщо виникає помилка при завантаженні FXML
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Shop.fxml"));
        fxmlLoader.setControllerFactory(c -> new SellController(conn));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Встановлює з'єднання з базою даних SQLite.
     * <p>
     * У разі помилки виводиться повідомлення та програма завершується.
     */
    private static void connectToDb() {
        String url = "jdbc:sqlite:products";

        try {
            conn = DriverManager.getConnection(url);
            System.out.println("Connection successfully");
        } catch (SQLException e) {
            System.out.println("Error connecting to a database: "
                    + e.getMessage() + "\nExiting...");

            Platform.exit();
            System.exit(1);
        }
    }

    /**
     * Головний метод запуску програми.
     * Ініціалізує базу даних та запускає JavaFX-додаток.
     *
     * @param args аргументи командного рядка (не використовуються)
     */
    public static void main(String[] args) {
        connectToDb();
        launch();
    }
}
