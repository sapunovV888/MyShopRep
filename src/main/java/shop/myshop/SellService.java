package shop.myshop;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Клас {@code SellService} відповідає за бізнес-логіку продажу та
 * взаємодію з базою даних і зовнішнім валютним API.
 * <p>
 * Приклади використання кожного методу дивись у {@code SellServiceTest}.
 *
 * @see SellServiceTest
 */
public class SellService {

    /**
     * API-ключ для доступу до ExchangeRate API.
     */
    private static final String API_KEY = "21b543dc06358232f8e12afc";

    /**
     * Базовий URL для запиту до ExchangeRate API.
     */
    private static final String API_URL = "https://v6.exchangerate-api.com/v6/"
            + API_KEY + "/latest/UAH";

    /**
     * З'єднання з базою даних.
     */
    private final Connection conn;

    /**
     * Конструктор {@code SellService}.
     *
     * @param conn з'єднання з базою даних
     */
    public SellService(Connection conn) {
        this.conn = conn;
    }

    /**
     * Отримує список товарів з бази даних.
     *
     * @return список {@link CatalogView}, що містить інформацію про товари
     * @throws SQLException у разі помилки під час виконання SQL-запиту
     *
     * @see SellServiceTest#testGetModelsReturnsCorrectData()
     */
    public List<CatalogView> getModels() throws SQLException {
        List<CatalogView> models = new ArrayList<>();

        String sql = """
                select id, category, name , price, num
                from catalog 
                """;

        try (PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
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

    /**
     * Обчислює загальну вартість усіх товарів в кошику.
     *
     * @return загальна сума (ціна × кількість) усіх товарів
     * @throws SQLException у разі помилки під час роботи з базою даних
     *
     * @see SellServiceTest#testCalculateTotal()
     */
    public double calculateTotal() throws SQLException {
        String sql = "SELECT price, num FROM catalog";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            double total = 0;
            while (rs.next()) {
                double price = rs.getDouble("price");
                int num = rs.getInt("num");
                total += price * num;
            }
            return total;
        }
    }

    /**
     * Отримує актуальні курси валют за допомогою ExchangeRate API.
     *
     * @param client екземпляр {@link HttpClient} для виконання запиту
     * @return JSON-об'єкт з курсами валют
     * @throws Exception у разі помилки під час запиту або обробки відповіді
     *
     * @see SellServiceTest#testGetExchangeRatesSuccess()
     * @see SellServiceTest#testGetExchangeRatesFailure()
     */
    public JsonObject getExchangeRates(HttpClient client) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();

        if (jsonResponse.get("result").getAsString().equals("success")) {
            return jsonResponse.getAsJsonObject("conversion_rates");
        } else {
            throw new Exception("Помилка при отриманні курсів валют");
        }
    }
}
