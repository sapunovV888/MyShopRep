import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shop.myshop.SellService;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Живі тести-документація до {@link SellService}.
 * Кожен тест описує типовий сценарій використання.
 */
public class SellServiceTest {

    private SellService sellService;
    private Connection mockConnection;

    @BeforeEach
    void setUp() {
        // Створюємо мок-з'єднання з БД перед кожним тестом
        mockConnection = mock(Connection.class);
        sellService = new SellService(mockConnection);
    }

    @Test
    public void getModels_shouldReturnCatalogItemsFromDatabase() throws Exception {
        // Ціль: перевірити, що getModels() коректно читає список товарів з БД

        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        // Налаштування моків
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("category")).thenReturn("Electronics");
        when(mockResultSet.getString("name")).thenReturn("Laptop");
        when(mockResultSet.getDouble("price")).thenReturn(1000.0);
        when(mockResultSet.getInt("num")).thenReturn(2);

        // Виклик
        var models = sellService.getModels();

        // 🔍 Перевірки
        assertEquals(1, models.size());
        assertEquals("Electronics", models.getFirst().getCategory());
        assertEquals("Laptop", models.getFirst().getName());
        assertEquals(1000.0, models.getFirst().getPrice());
        assertEquals(2, models.getFirst().getNum());
    }

    @Test
    public void calculateTotal_shouldReturnSumOfAllItems() throws Exception {
        // Ціль: перевірити, що метод обчислює суму (ціна × кількість) всіх товарів

        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);

        // Тестові товари: 2 × 10 + 3 × 20 = 80
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getDouble("price")).thenReturn(10.0, 20.0);
        when(mockResultSet.getInt("num")).thenReturn(2, 3);

        double total = sellService.calculateTotal();

        assertEquals(80.0, total);
    }

    @Test
    public void getExchangeRates_shouldReturnParsedExchangeRates_whenApiSuccess() throws Exception {
        // Ціль: перевірити, що API повертає курси валют і вони коректно парсяться

        String jsonResponse = """
                {
                    "result": "success",
                    "conversion_rates": {
                        "USD": 0.027,
                        "EUR": 0.025
                    }
                }
                """;

        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse mockHttpResponse = mock(HttpResponse.class);

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockHttpResponse);
        when(mockHttpResponse.body()).thenReturn(jsonResponse);

        JsonObject exchangeRates = sellService.getExchangeRates(mockHttpClient);

        assertEquals(0.027, exchangeRates.get("USD").getAsDouble());
        assertEquals(0.025, exchangeRates.get("EUR").getAsDouble());

        verify(mockHttpResponse).body();
    }

    @Test
    void getExchangeRates_shouldThrowException_whenApiFails() throws Exception {
        // Ціль: перевірити, що метод кидає виняток при невдалій відповіді API

        String jsonResponse = """
                {
                    "result": "failure",
                    "error-type": "invalid-key"
                }
                """;

        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse mockHttpResponse = mock(HttpResponse.class);

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockHttpResponse);
        when(mockHttpResponse.body()).thenReturn(jsonResponse);

        Exception exception = assertThrows(Exception.class, () -> sellService.getExchangeRates(mockHttpClient));

        assertEquals("Помилка при отриманні курсів валют", exception.getMessage());

        verify(mockHttpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
        verify(mockHttpResponse).body();
    }
}
