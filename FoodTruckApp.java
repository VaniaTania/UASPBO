import java.sql.*;
import java.text.DecimalFormat;
import java.util.*;

public class FoodTruckApp {

    public static void main(String[] args) {
        FoodTruckManager manager = new FoodTruckManager();
        manager.displayMenu();
    }
}

class FoodTruck {
    private String name;
    private String location;

    public FoodTruck(String name, String location) {
        this.name = name;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "FoodTruck: " + name + ", Location: " + location;
    }
}

class MenuItem {
    private String name;
    private double price;

    public MenuItem(String name, double price) {
        this.name = name;
        this.price = price;
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

    @Override
    public String toString() {
        // Format manual untuk harga dalam Rupiah
        DecimalFormat df = new DecimalFormat("#,###.00");
        return name + ": Rp " + df.format(price);
    }
}

interface FoodTruckInterface {
    void addMenuItem(MenuItem item);

    void removeMenuItem(String itemName);

    List<MenuItem> getMenuItems();
}

class FoodTruckManager implements FoodTruckInterface {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/foodtruck";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "Vntn061005";

    private Connection connection;

    public FoodTruckManager() {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Connected to the database.");
        } catch (SQLException e) {
            System.out.println("Failed to connect to the database.");
            e.printStackTrace();
        }
    }

    @Override
    public void addMenuItem(MenuItem item) {
        if (connection == null) {
            System.out.println("Database connection is null. Cannot add menu item.");
            return;
        }
        String sql = "INSERT INTO menu (name, price) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, item.getName());
            stmt.setDouble(2, item.getPrice());
            stmt.executeUpdate();
            System.out.println(item.getName() + " added to the database.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeMenuItem(String itemName) {
        String sql = "DELETE FROM menu WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, itemName);
            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println(itemName + " removed from the database.");
            } else {
                System.out.println("Item not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> menuItems = new ArrayList<>();
        String sql = "SELECT * FROM menu ORDER BY id ASC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                menuItems.add(new MenuItem(name, price));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return menuItems;
    }

    public void displayMenu() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== Food Truck Menu ===");
            System.out.println("1. Add Menu Item");
            System.out.println("2. Remove Menu Item");
            System.out.println("3. Display Menu");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    System.out.print("Enter item name: ");
                    String name = scanner.nextLine();
                    System.out.print("Enter item price: ");
                    double price = scanner.nextDouble();
                    addMenuItem(new MenuItem(name, price));
                    break;
                case 2:
                    System.out.print("Enter item name to remove: ");
                    String itemName = scanner.nextLine();
                    removeMenuItem(itemName);
                    break;
                case 3:
                    System.out.println("\nMenu Items:");
                    for (MenuItem item : getMenuItems()) {
                        System.out.println(item);
                    }
                    break;
                case 4:
                    System.out.println("Exiting...");
                    scanner.close();
                    try {
                        connection.close();
                        System.out.println("Database connection closed.");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return;
                default:
                    System.out.println("Invalid choice, please try again.");
            }
        }
    }
}
