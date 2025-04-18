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
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    // Створення логера для запису подій
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    /**
     * Метод, який викликається при запуску JavaFX-додатку.
     * Завантажує інтерфейс з FXML-файлу та встановлює контролер {@link SellController}.
     *
     * @param stage головна сцена додатку
     * @throws IOException якщо виникає помилка при завантаженні FXML
     */
    @Override
    public void start(Stage stage) throws IOException {
        logger.info("Запуск додатку...");

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Shop.fxml"));
        fxmlLoader.setControllerFactory(c -> new SellController(conn));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.show();

        logger.info("Головна сцена відображена.");
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
            logger.info("Успішне підключення до бази даних.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Помилка підключення до бази даних: " + e.getMessage(), e);

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
        // Налаштування логера на виведення в консоль
        ConsoleHandler consoleHandler = new ConsoleHandler();
        logger.addHandler(consoleHandler);
        logger.setLevel(Level.ALL);  // Налаштовуємо рівень логування на ALL, щоб бачити всі повідомлення

        logger.info("Програма запускається...");
        connectToDb();
        launch();
    }
}
