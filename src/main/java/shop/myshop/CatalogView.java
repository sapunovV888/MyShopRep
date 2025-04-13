package shop.myshop;

public class CatalogView {
    private int id;

    private String category;

    private String name;

    private double price;

    private int num;

    public CatalogView(int id, String category, String name, double price, int num) {
        this.id = id;
        this.category = category;
        this.name = name;
        this.price = price;
        this.num = num;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }
}
