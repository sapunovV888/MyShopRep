# Інструкція з генерації документації

## ⚙️ Вимоги

- Встановлений **JDK 17 або вище**
- Наявність `javadoc` у системі (`JAVA_HOME` має бути налаштований)

> 💡 Якщо працюєш в IntelliJ IDEA — `javadoc.exe` вже вбудовано, окрема установка не потрібна.

> ⚠️ **Увага:** Шлях до проєкту **не повинен містити кирилиці** (наприклад: `C:\Користувачі\...`), інакше `javadoc` може завершити роботу з помилкою. Використовуйте лише латинські символи в назвах папок.

## 🔨 Генерація документації в IntelliJ IDEA

1. Перейдіть до меню `Tools` → `Generate JavaDoc...`
2. У полі `Output directory` вкажіть:  
   `<шлях_до_проекту>/docs/api`
3. У секції `Scope` оберіть:  
   `src/main/java`
4. За бажанням, поставте галочки:
    - `Include protected and public`
    - `Use standard doclet`
    - `Split index`
    - `UTF-8 encoding`
5. Натисніть **OK**

## 📁 Результат

Документація буде збережена в директорії `docs/api/index.html`.

Відкрий у браузері `index.html`, щоб переглянути результат.
