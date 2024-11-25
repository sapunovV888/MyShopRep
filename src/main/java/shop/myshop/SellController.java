package shop.myshop;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import static shop.myshop.Main.conn;

public class SellController {

    private static final String API_KEY = "21b543dc06358232f8e12afc";
    private static final String API_URL = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/UAH";

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

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

    @FXML
    public void initialize() throws SQLException {

       nCol.setCellValueFactory(new PropertyValueFactory<>("id"));
       catCol.setCellValueFactory(new PropertyValueFactory<>("category"));
       nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
       priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
       inCol.setCellValueFactory(new PropertyValueFactory<>("num"));

        ObservableList<CatalogView> data = FXCollections.observableArrayList(getModels());
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
        String sql = "SELECT price, num FROM catalog";
        PreparedStatement stmt = Main.conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        double total = 0;
        while (rs.next()) {
            double price = rs.getDouble("price");
            int num = rs.getInt("num");
            total += price * num; // Обчислення (ціна × кількість)
        }

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
        PreparedStatement selectStmt = Main.conn.prepareStatement(sqlSelect);
        selectStmt.setString(1, nameInput);
        ResultSet resultSet = selectStmt.executeQuery();

        if (resultSet.next()) {
            // Знайдено відповідний запис
            int currentNum = resultSet.getInt("num");
            double price = resultSet.getDouble("price"); // Отримуємо ціну
            int id = resultSet.getInt("id"); // Отримуємо ID запису
            int updatedNum = currentNum + numToAdd;

            // Оновлення даних у базі
            String sqlUpdate = "UPDATE catalog SET num = ? WHERE id = ?";
            PreparedStatement updateStmt = Main.conn.prepareStatement(sqlUpdate);
            updateStmt.setInt(1, updatedNum);
            updateStmt.setInt(2, id);
            updateStmt.executeUpdate();

            System.out.println("Додано " + numToAdd + " до товару: " + nameInput);

            // Оновлення таблиці
            ObservableList<CatalogView> updatedData = FXCollections.observableArrayList(getModels());
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
        PreparedStatement selectStmt = Main.conn.prepareStatement(selectQuery);
        selectStmt.setString(1, productName);
        ResultSet rs = selectStmt.executeQuery();

        // Якщо товар знайдений
        if (rs.next()) {
            double price = rs.getDouble("price");
            int num = rs.getInt("num");

            // Оновлюємо значення num в таблиці catalog
            String updateQuery = "UPDATE catalog SET num = 0 WHERE name = ?";
            PreparedStatement updateStmt = Main.conn.prepareStatement(updateQuery);
            updateStmt.setString(1, productName);
            updateStmt.executeUpdate();

            // Отримуємо поточне значення з calcField
            String currentCalcText = calcField.getText();

            // Якщо є кома, замінюємо її на точку
            currentCalcText = currentCalcText.replace(',', '.');

            // Перетворюємо в число
            double currentCalcValue = 0.0;
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
            ObservableList<CatalogView> updatedData = FXCollections.observableArrayList(getModels());
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
        PreparedStatement resetStmt = Main.conn.prepareStatement(sqlReset);
        resetStmt.executeUpdate();

        System.out.println("Значення стовпця 'num' встановлено на 0.");

        updateCurrencyFields();

        // Оновлення таблиці після очищення
        ObservableList<CatalogView> updatedData = FXCollections.observableArrayList(getModels());
        sellTable.setItems(updatedData);
    }

    private List<CatalogView> getModels() throws SQLException {
        List<CatalogView> models = new ArrayList<>();

        String sql = """
                select id, category, name , price, num
                from catalog 
        """;

        try (PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                // Створюємо об'єкт CatalogView для кожного рядка результату
                CatalogView model = new CatalogView(
                        resultSet.getInt("id"),
                        resultSet.getString("category"),
                        resultSet.getString("name"),
                        resultSet.getDouble("price"),
                        resultSet.getInt("num")
                );
                models.add(model);
            }
        }

        return models;


    }

    private void updateCurrencyFields() {
        try {
            // Отримуємо курси валют
            JsonObject conversionRates = getExchangeRates();

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

    // Метод для отримання курсів валют через API
    private JsonObject getExchangeRates() throws Exception {
        // Створення запиту
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .build();

        // Отримуємо відповідь від API
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Перетворення відповіді на JSON
        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();

        // Перевірка на успішну відповідь від API
        if (jsonResponse.get("result").getAsString().equals("success")) {
            return jsonResponse.getAsJsonObject("conversion_rates");
        } else {
            throw new Exception("Помилка при отриманні курсів валют");
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
        List<CatalogView> allItems = getModels();

        // Якщо чекбокс активний, фільтруємо список
        List<CatalogView> filteredItems;
        if (showOnlyInCart) {
            filteredItems = allItems.stream()
                    .filter(item -> item.getNum() > 0) // Фільтруємо за умовою `num > 0`
                    .toList();
        } else {
            filteredItems = allItems; // Показуємо всі елементи
        }

        // Оновлюємо вміст таблиці
        ObservableList<CatalogView> data = FXCollections.observableArrayList(filteredItems);
        sellTable.setItems(data);
    }
    // Оновлення суми при зміні значення у catalog
   /* private void updateCalcField() throws SQLException {
        String sql = "SELECT price, num FROM catalog";
        PreparedStatement stmt = Main.conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        double total = 0;
        while (rs.next()) {
            double price = rs.getDouble("price");
            int num = rs.getInt("num");
            total += price * num; // Обчислення (ціна × кількість)
        }

        calcField.setText(String.format("%.2f", total)); // Виведення загальної суми у поле calcField
        updateCurrencyFields(); // Оновлюємо валютні поля після оновлення суми
    }*/

    // Ваша решта логіки (методи для addToCart, deleteProduct, resetCart, і т.д.)
    // ...


}
