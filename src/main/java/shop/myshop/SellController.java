package shop.myshop;

import com.google.gson.JsonObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.http.HttpClient;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Контролер JavaFX для роботи з вікном продажів. Керує таблицею товарів,
 * додаванням, видаленням, фільтрацією, підрахунком вартості та конвертацією валют.
 */
public class SellController {

    // === Компоненти інтерфейсу ===

    @FXML private Button addButton;

    @FXML private TextField calcField;

    @FXML private TableColumn<CatalogView, String> catCol;

    @FXML private TableColumn<CatalogView, Integer> inCol;

    @FXML private TableColumn<CatalogView, Integer> nCol;

    @FXML private TableColumn<CatalogView, String> nameCol;

    @FXML private TextField nameField;

    @FXML private TextField numField;

    @FXML private TableColumn<CatalogView, Double> priceCol;

    @FXML private Button removeButton;

    @FXML private Button restartButton;

    @FXML private TableView<CatalogView> sellTable;

    @FXML private TextField usdField;

    @FXML private TextField eurField;

    @FXML private CheckBox inBag;

    private final Connection conn;

    private final SellService service;

    /**
     * Конструктор контролера. Приймає з'єднання з БД і ініціалізує сервіс.
     *
     * @param conn з'єднання з базою даних
     */
    public SellController(Connection conn) {
        this.conn = conn;
        this.service = new SellService(conn);
    }

    /**
     * Ініціалізація інтерфейсу після завантаження FXML.
     * Заповнює таблицю, налаштовує події кнопок та фільтр.
     *
     * @throws SQLException якщо виникає помилка при зверненні до БД
     */
    @FXML
    public void initialize() throws SQLException {
        // Налаштування колонок таблиці
        nCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        catCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        inCol.setCellValueFactory(new PropertyValueFactory<>("num"));

        // Завантаження початкових даних
        ObservableList<CatalogView> data = FXCollections.observableArrayList(service.getModels());
        sellTable.setItems(data);

        // Прив'язка подій до кнопок
        addButton.setOnAction(event -> {
            try {
                addToCart();
                nameField.clear();
                numField.clear();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        restartButton.setOnAction(event -> {
            try {
                resetCart();
                nameField.clear();
                numField.clear();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        removeButton.setOnAction(event -> {
            try {
                deleteProduct();
                nameField.clear();
                numField.clear();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        inBag.selectedProperty().addListener((observable, oldValue, newValue) -> {
            try {
                applyFilter(newValue);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        // Показуємо всі товари за замовчуванням
        applyFilter(false);
    }

    /**
     * Оновлює поле суми та курси валют.
     *
     * @throws SQLException якщо помилка при обчисленні суми
     */
    private void updateCalcField() throws SQLException {
        double total = service.calculateTotal();
        calcField.setText(String.format("%.2f", total));
        updateCurrencyFields();
    }

    /**
     * Додає вказану кількість товару до "корзини".
     *
     * @throws SQLException при помилці оновлення БД
     */
    private void addToCart() throws SQLException {
        String nameInput = nameField.getText().trim();
        String numInput = numField.getText().trim();

        if (nameInput.isEmpty() || numInput.isEmpty()) {
            System.out.println("Заповніть усі поля!");
            return;
        }

        int numToAdd;
        try {
            numToAdd = Integer.parseInt(numInput);
        } catch (NumberFormatException e) {
            System.out.println("Введіть коректне число у numField!");
            return;
        }

        // Пошук товару
        String sqlSelect = "SELECT * FROM catalog WHERE name = ?";
        PreparedStatement selectStmt = conn.prepareStatement(sqlSelect);
        selectStmt.setString(1, nameInput);
        ResultSet resultSet = selectStmt.executeQuery();

        if (resultSet.next()) {
            int currentNum = resultSet.getInt("num");
            int id = resultSet.getInt("id");
            int updatedNum = currentNum + numToAdd;

            // Оновлення запису
            String sqlUpdate = "UPDATE catalog SET num = ? WHERE id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(sqlUpdate);
            updateStmt.setInt(1, updatedNum);
            updateStmt.setInt(2, id);
            updateStmt.executeUpdate();

            System.out.println("Додано " + numToAdd + " до товару: " + nameInput);

            // Оновлення інтерфейсу
            sellTable.setItems(FXCollections.observableArrayList(service.getModels()));
            updateCalcField();
        } else {
            System.out.println("Товар не знайдено!");
        }
    }

    /**
     * Видаляє вибраний товар (встановлює кількість на 0).
     *
     * @throws SQLException при зверненні до БД
     */
    private void deleteProduct() throws SQLException {
        String productName = nameField.getText().trim();
        if (productName.isEmpty()) {
            System.out.println("Введіть назву товару!");
            return;
        }

        String selectQuery = "SELECT price, num FROM catalog WHERE name = ?";
        PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
        selectStmt.setString(1, productName);
        ResultSet rs = selectStmt.executeQuery();

        if (rs.next()) {
            double price = rs.getDouble("price");
            int num = rs.getInt("num");

            String updateQuery = "UPDATE catalog SET num = 0 WHERE name = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            updateStmt.setString(1, productName);
            updateStmt.executeUpdate();

            double currentCalcValue = getTotalUAH();
            double newCalcValue = currentCalcValue - (num * price);
            calcField.setText(String.format("%.2f", newCalcValue));

            updateCurrencyFields();
            sellTable.setItems(FXCollections.observableArrayList(service.getModels()));

            System.out.println("Товар '" + productName + "' був видалений.");
        } else {
            System.out.println("Товар з таким ім'ям не знайдено.");
        }
    }

    /**
     * Очищає корзину: скидає кількість усіх товарів та оновлює суму.
     *
     * @throws SQLException при помилці запиту
     */
    private void resetCart() throws SQLException {
        calcField.setText("");
        PreparedStatement resetStmt = conn.prepareStatement("UPDATE catalog SET num = 0");
        resetStmt.executeUpdate();

        updateCurrencyFields();
        sellTable.setItems(FXCollections.observableArrayList(service.getModels()));
        System.out.println("Корзина очищена.");
    }

    /**
     * Оновлює поля валют на основі поточного курсу та загальної суми.
     */
    private void updateCurrencyFields() {
        try {
            JsonObject rates = service.getExchangeRates(HttpClient.newHttpClient());
            double totalUAH = getTotalUAH();
            usdField.setText(String.format("%.2f", totalUAH * rates.get("USD").getAsDouble()));
            eurField.setText(String.format("%.2f", totalUAH * rates.get("EUR").getAsDouble()));
        } catch (Exception e) {
            e.printStackTrace();
            usdField.setText("Помилка");
            eurField.setText("Помилка");
        }
    }

    /**
     * Отримує загальну суму з поля calcField у гривнях.
     *
     * @return значення у вигляді double
     */
    private double getTotalUAH() {
        String calcText = calcField.getText().replace(',', '.');
        try {
            return Double.parseDouble(calcText);
        } catch (NumberFormatException e) {
            System.out.println("Помилка: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Фільтрує таблицю: показує всі товари або тільки ті, що в корзині.
     *
     * @param showOnlyInCart true – показати лише товари з num > 0
     * @throws SQLException при помилці доступу до БД
     */
    private void applyFilter(boolean showOnlyInCart) throws SQLException {
        List<CatalogView> allItems = service.getModels();
        List<CatalogView> filteredItems = showOnlyInCart
                ? allItems.stream().filter(item -> item.getNum() > 0).toList()
                : allItems;
        sellTable.setItems(FXCollections.observableArrayList(filteredItems));
    }
}
