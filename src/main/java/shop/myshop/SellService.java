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

public class SellService {
    private static final String API_KEY = "21b543dc06358232f8e12afc";

    private static final String API_URL = "https://v6.exchangerate-api.com/v6/"
            + API_KEY + "/latest/UAH";

    private final Connection conn;

    public SellService(Connection conn) {
        this.conn = conn;
    }

    public List<CatalogView> getModels() throws SQLException {
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

    public double calculateTotal() throws SQLException {
        String sql = "SELECT price, num FROM catalog";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            double total = 0;
            while (rs.next()) {
                double price = rs.getDouble("price");
                int num = rs.getInt("num");
                total += price * num; // Обчислення (ціна × кількість)
            }
            return total;
        }
    }

    // Метод для отримання курсів валют через API
    public JsonObject getExchangeRates(HttpClient client) throws Exception {
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
}
