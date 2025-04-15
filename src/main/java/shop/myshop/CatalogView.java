package shop.myshop;

/**
 * Клас {@code CatalogView} представляє модель товару в каталозі магазину.
 * Містить основні характеристики товару: ідентифікатор, категорію, назву, ціну та кількість.
 *
 * Цей клас може використовуватись для представлення товарів у UI
 * або для обміну даними між компонентами програми.
 */
public class CatalogView {
    private int id;

    private String category;

    private String name;

    private double price;

    private int num;

    /**
     * Конструктор класу {@code CatalogView}.
     *
     * @param id       унікальний ідентифікатор товару
     * @param category категорія товару
     * @param name     назва товару
     * @param price    ціна товару
     * @param num      кількість доступних одиниць
     */
    public CatalogView(int id, String category, String name, double price, int num) {
        this.id = id;
        this.category = category;
        this.name = name;
        this.price = price;
        this.num = num;
    }

    /**
     * Повертає ідентифікатор товару.
     *
     * @return id товару
     */
    public int getId() {
        return id;
    }

    /**
     * Встановлює ідентифікатор товару.
     *
     * @param id новий id товару
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Повертає категорію товару.
     *
     * @return категорія
     */
    public String getCategory() {
        return category;
    }

    /**
     * Встановлює категорію товару.
     *
     * @param category нова категорія
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Повертає назву товару.
     *
     * @return назва товару
     */
    public String getName() {
        return name;
    }

    /**
     * Встановлює назву товару.
     *
     * @param name нова назва
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Повертає ціну товару.
     *
     * @return ціна
     */
    public double getPrice() {
        return price;
    }

    /**
     * Встановлює ціну товару.
     *
     * @param price нова ціна
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Повертає кількість товару.
     *
     * @return кількість
     */
    public int getNum() {
        return num;
    }

    /**
     * Встановлює кількість товару.
     *
     * @param num нова кількість
     */
    public void setNum(int num) {
        this.num = num;
    }
}
