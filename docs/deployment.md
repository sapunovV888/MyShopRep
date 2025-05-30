# Розгортання у production

## 🖥️ Вимоги до апаратного забезпечення

- **Операційна система**: Windows 10+
- **Архітектура**: x64
- **Процесор**: мінімум 2 ядра, рекомендовано 4 ядра (Intel i5 або еквівалентний AMD)
- **Оперативна пам’ять**: рекомендовано 8 ГБ
- **Вільне місце на диску**: мінімум 15 ГБ для забезпечення швидкої роботи IntelliJ IDEA та проєкту 

## ⚙️ Необхідне програмне забезпечення

- **Java Development Kit (JDK) 21**
- **JavaFX SDK 21**
- **SQLite 3**
- **IntelliJ IDEA** або інше IDE для розробки (необхідне для налаштування та налагодження проєкту)
- **Java Runtime Environment (JRE) 21+** — на машині, де буде запускатися JAR-файл

## 🛠️ Налаштування бази даних (SQLite)

1. У кореневій директорії проєкту має бути файл `products` — це база даних у форматі SQLite.
2. Якщо база даних ще не створена, створіть її вручну за допомогою будь-якого SQLite-редактора або за допомогою наступного SQL-скрипта:
   ```sql
   CREATE TABLE IF NOT EXISTS products (
       id INTEGER PRIMARY KEY AUTOINCREMENT,
       category VARCHAR(255),
       name VARCHAR(255),
       price REAL,
       num INTEGER DEFAULT 0
   );
 3.  Додати кілька записів про продукти для функціонування програми (В подальшому це можна буде робити в середині самої програми)
Приклад запиту: 
- INSERT INTO products (category, name, price, num) VALUES ('Groceries', 'Banana', 1.29, 80);
## 🚀 Розгортання коду

1. Відкрийте проєкт у **IntelliJ IDEA**.
2. Перевірте, що підключено **JDK 21** та **JavaFX**.
3. Перейдіть до меню:  
   `Build → Build Artifacts → Jar → Build`
4. Після завершення збірки JAR-файл буде збережено у теці:  
   `out/artifacts/Ім’яПроєкту_jar/Ім’яПроєкту.jar`
5. Скопіюйте цей `.jar` файл на цільову машину.

## Для запуску `.jar` файлу:

- **Через консоль**:  
  Відкрийте командний рядок і виконайте команду:
   ```bash
   java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml -jar MyShopRep.jar
  ```
- **Через двійковий файл або ярлик**:  
     Щоб запустити програму без використання командного рядка, можна створити ярлик для запуску JAR-файлу:

   1. Клацніть правою кнопкою миші на порожньому місці в теці з програмою.
   2. Оберіть **"Створити ярлик"**.
   3. У властивостях ярлика додайте наступний шлях до JAR-файлу:
      ```text
      "C:\path\to\java\bin\java.exe" -jar "C:\path\to\your\project\MyShopRep.jar"
      ```
   4. Збережіть ярлик і запустіть програму через нього.

## ✅ Перевірка працездатності

Після запуску програми необхідно перевірити:

1. Додаток відкривається без помилок.
2. Інтерфейс завантажується і готовий до роботи.
3. Усі дані з бази даних `products` доступні для перегляду.
4. Основні функції, такі як продаж товарів, додавання в кошик, видалення з кошика, працюють стабільно.
5. Додаток коректно завершується без збоїв.

> **Примітка:**  
> Пункти "Налаштування мережі" та "Конфігурація серверів" не реалізовані, оскільки програма є десктопною та працює з локальною базою даних. Всі операції виконуються без підключення до мережі чи серверів.
