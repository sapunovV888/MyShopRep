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

public class SellController {

    @FXML
    private Button addButton;

    @FXML
    private TextField calcField;

    @FXML
    private TableColumn<CatalogView, String> catCol;

    @FXML
    private TableColumn<CatalogView, Integer> inCol;

    @FXML
    private TableColumn<CatalogView, Integer> nCol;

    @FXML
    private TableColumn<CatalogView, String> nameCol;

    @FXML
    private TextField nameField;

    @FXML
    private TextField numField;

    @FXML
    private TableColumn<CatalogView, Double> priceCol;

    @FXML
    private Button removeButton;

    @FXML
    private Button restartButton;

    @FXML
    private TableView<CatalogView> sellTable;

    @FXML
    private TextField usdField;

    @FXML
    private TextField eurField;

    @FXML
    private CheckBox inBag;

    private final Connection conn;
    private final SellService service;

    public SellController(Connection conn) {
        this.conn = conn;
        this.service = new SellService(conn);
    }

    @FXML
    public void initialize() throws SQLException {
        nCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        catCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        inCol.setCellValueFactory(new PropertyValueFactory<>("num"));

        ObservableList<CatalogView> data = FXCollections.observableArrayList(service.getModels());
        sellTable.setItems(data);

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
                applyFilter(newValue); // Оновлюємо таблицю залежно від стану чекбоксу
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        // Показуємо всі елементи за замовчуванням
        applyFilter(false);

    }

    private void updateCalcField() throws SQLException {
        double total = service.calculateTotal();

        calcField.setText(String.format("%.2f", total)); // Виведення загальної суми у поле calcField
        updateCurrencyFields();
    }

    private void addToCart() throws SQLException {
        // Отримуємо введені дані
        String nameInput = nameField.getText().trim(); // Тепер поле nameField відповідає полю "name" у базі даних
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

        // Пошук товару в базі даних за назвою
        String sqlSelect = "SELECT * FROM catalog WHERE name = ?";
        PreparedStatement selectStmt = conn.prepareStatement(sqlSelect);
        selectStmt.setString(1, nameInput);
        ResultSet resultSet = selectStmt.executeQuery();

        if (resultSet.next()) {
            // Знайдено відповідний запис
            int currentNum = resultSet.getInt("num");
            int id = resultSet.getInt("id"); // Отримуємо ID запису
            int updatedNum = currentNum + numToAdd;

            // Оновлення даних у базі
            String sqlUpdate = "UPDATE catalog SET num = ? WHERE id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(sqlUpdate);
            updateStmt.setInt(1, updatedNum);
            updateStmt.setInt(2, id);
            updateStmt.executeUpdate();

            System.out.println("Додано " + numToAdd + " до товару: " + nameInput);

            // Оновлення таблиці
            ObservableList<CatalogView> updatedData = FXCollections.observableArrayList(service.getModels());
            sellTable.setItems(updatedData);

            // Оновлення поля calcField
            updateCalcField();


        } else {
            System.out.println("Товар не знайдено!");
        }
    }

    private void deleteProduct() throws SQLException {
        String productName = nameField.getText().trim();
        if (productName.isEmpty()) {
            System.out.println("Введіть назву товару!");
            return;
        }

        // SQL-запит для вибору товару по назві
        String selectQuery = "SELECT price, num FROM catalog WHERE name = ?";
        PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
        selectStmt.setString(1, productName);
        ResultSet rs = selectStmt.executeQuery();

        // Якщо товар знайдений
        if (rs.next()) {
            double price = rs.getDouble("price");
            int num = rs.getInt("num");

            // Оновлюємо значення num в таблиці catalog
            String updateQuery = "UPDATE catalog SET num = 0 WHERE name = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            updateStmt.setString(1, productName);
            updateStmt.executeUpdate();

            // Отримуємо поточне значення з calcField
            String currentCalcText = calcField.getText();

            // Якщо є кома, замінюємо її на точку
            currentCalcText = currentCalcText.replace(',', '.');

            // Перетворюємо в число
            double currentCalcValue;
            try {
                currentCalcValue = Double.parseDouble(currentCalcText);
            } catch (NumberFormatException e) {
                System.out.println("Помилка при перетворенні значення calcField: " + e.getMessage());
                return;
            }

            // Оновлюємо calcField, віднімаючи num * price
            double newCalcValue = currentCalcValue - (num * price);
            calcField.setText(String.format("%.2f", newCalcValue));

            updateCurrencyFields();

            // Оновлюємо таблицю
            ObservableList<CatalogView> updatedData = FXCollections.observableArrayList(service.getModels());
            sellTable.setItems(updatedData);

            System.out.println("Товар '" + productName + "' був видалений, поле 'num' встановлено на 0.");
        } else {
            System.out.println("Товар з таким ім'ям не знайдено.");
        }
    }


    private void resetCart() throws SQLException {
        // Очищення поля calcField
        calcField.setText("");

        // SQL-запит для встановлення стовпця `num` в таблиці `catalog` на 0
        String sqlReset = "UPDATE catalog SET num = 0";
        PreparedStatement resetStmt = conn.prepareStatement(sqlReset);
        resetStmt.executeUpdate();

        System.out.println("Значення стовпця 'num' встановлено на 0.");

        updateCurrencyFields();

        // Оновлення таблиці після очищення
        ObservableList<CatalogView> updatedData = FXCollections.observableArrayList(service.getModels());
        sellTable.setItems(updatedData);
    }

    private void updateCurrencyFields() {
        try {
            // Отримуємо курси валют
            JsonObject conversionRates = service.getExchangeRates(HttpClient.newHttpClient());

            // Отримуємо курс UAH -> USD та UAH -> EUR
            double uahToUsd = conversionRates.get("USD").getAsDouble();
            double uahToEur = conversionRates.get("EUR").getAsDouble();

            // Отримуємо загальну суму у UAH з calcField
            double totalUAH = getTotalUAH();

            // Конвертуємо загальну суму в долари та євро
            double totalUSD = totalUAH * uahToUsd;
            double totalEUR = totalUAH * uahToEur;

            // Виводимо результат у відповідні поля
            usdField.setText(String.format("%.2f", totalUSD));
            eurField.setText(String.format("%.2f", totalEUR));

        } catch (Exception e) {
            e.printStackTrace();
            // Якщо виникає помилка, вивести повідомлення про помилку в поля
            usdField.setText("Помилка");
            eurField.setText("Помилка");
        }
    }

    // Метод для отримання загальної суми з поля calcField
    private double getTotalUAH() {
        // Перетворюємо значення з calcField в число
        String calcText = calcField.getText().replace(',', '.'); // Заміняємо кому на точку
        try {
            return Double.parseDouble(calcText); // Перетворюємо на число
        } catch (NumberFormatException e) {
            System.out.println("Помилка при конвертації суми з поля calcField: " + e.getMessage());
            return 0.0; // Якщо значення неправильне, повертаємо 0
        }
    }

    private void applyFilter(boolean showOnlyInCart) throws SQLException {
        // Отримуємо всі елементи з бази даних
        List<CatalogView> allItems = service.getModels();

        // Якщо чекбокс активний, фільтруємо список
        List<CatalogView> filteredItems;
        if (showOnlyInCart) {
            filteredItems = allItems.stream().filter(item -> item.getNum() > 0) // Фільтруємо за умовою `num > 0`
                .toList();
        } else {
            filteredItems = allItems; // Показуємо всі елементи
        }

        // Оновлюємо вміст таблиці
        ObservableList<CatalogView> data = FXCollections.observableArrayList(filteredItems);
        sellTable.setItems(data);
    }
}
