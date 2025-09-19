package quanlythuvien;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public abstract class Book {
    protected String id;
    protected String title;
    protected String author;
    protected boolean isAvailable;

    protected static Map<String, Book> books = new HashMap<>();

    public Book(String id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isAvailable = true;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { this.isAvailable = available; }

    public abstract void displayType();

    public static void addBook(Book book) {
        if (book != null && !books.containsKey(book.getId())) {
            books.put(book.getId(), book);
        }
    }

    public static void removeBook(String id) {
        books.remove(id);
    }

    public static Book getBookById(String id) {
        return books.get(id);
    }

    public static int getTotalBooks() {
        return books.size();
    }

    public static boolean isAvailable(String id) {
        Book book = getBookById(id);
        return book != null && book.isAvailable;
    }

    public static void saveToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("books.txt"))) {
            for (Book book : books.values()) {
                writer.println(book.getId() + "," + book.getTitle() + "," + book.getAuthor() + "," + (book instanceof FictionBook ? "Fiction" : "NonFiction") + "," + book.isAvailable);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadFromFile() {
        books.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader("books.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    String id = parts[0];
                    String title = parts[1];
                    String author = parts[2];
                    String type = parts[3];
                    boolean available = Boolean.parseBoolean(parts[4]);
                    Book book = type.equals("Fiction") ? new FictionBook(id, title, author) : new NonFictionBook(id, title, author);
                    book.setAvailable(available);
                    books.put(id, book);
                }
            }
        } catch (IOException e) {
            // File không tồn tại hoặc lỗi, khởi tạo mới
        }
    }
}