package quanlythuvien;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public abstract class User implements Authenticator {
    private String id;
    private String name;
    private String username;
    private String password;
    private boolean isLoggedIn;

    protected static Map<String, User> users = new HashMap<>();

    public User(String id, String name, String username, String password) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.password = password;
        this.isLoggedIn = false;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public boolean isLoggedIn() { return isLoggedIn; }

    public abstract void displayRole();

    @Override
    public boolean login(String username, String password) {
        if (this.username.equals(username) && this.password.equals(password)) {
            isLoggedIn = true;
            return true;
        }
        return false;
    }

    @Override
    public void logout() {
        isLoggedIn = false;
    }

    public static void registerUser(User user) {
        users.put(user.getUsername(), user);
    }

    public static User getUserByUsername(String username) {
        return users.get(username);
    }

    public static int getTotalUsers() {
        return users.size();
    }

    public static void saveToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("users.txt"))) {
            for (User user : users.values()) {
                writer.println(user.getId() + "," + user.getName() + "," + user.getUsername() + "," + user.getPassword() + "," + (user instanceof Librarian ? "Librarian" : "Reader"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadFromFile() {
        users.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    String id = parts[0];
                    String name = parts[1];
                    String username = parts[2];
                    String password = parts[3];
                    String role = parts[4];
                    User user = role.equals("Librarian") ? new Librarian(id, name, username, password) : new Reader(id, name, username, password);
                    users.put(username, user);
                }
            }
        } catch (IOException e) {
            // File không tồn tại hoặc lỗi, khởi tạo mới
        }
    }
}