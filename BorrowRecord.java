package quanlythuvien;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class BorrowRecord implements Borrowable {
    private static Map<String, String> borrowRecords = new HashMap<>(); // bookId -> userId

    public static void addRecord(String bookId, String userId) {
        borrowRecords.put(bookId, userId);
    }

    public static void removeRecord(String bookId) {
        borrowRecords.remove(bookId);
    }

    @Override
    public void borrowBook(String bookId, String userId) {
        Book book = Book.getBookById(bookId);
        if (book != null && book.isAvailable()) {
            book.setAvailable(false);
            addRecord(bookId, userId);
            Book.saveToFile();
            saveToFile();
        }
    }

    @Override
    public void returnBook(String bookId, String userId) {
        Book book = Book.getBookById(bookId);
        if (book != null && !book.isAvailable() && borrowRecords.get(bookId).equals(userId)) {
            book.setAvailable(true);
            removeRecord(bookId);
            Book.saveToFile();
            saveToFile();
        }
    }

    public static int getTotalBorrows() {
        return borrowRecords.size();
    }

    public static void saveToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("borrows.txt"))) {
            for (Map.Entry<String, String> entry : borrowRecords.entrySet()) {
                writer.println(entry.getKey() + "," + entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadFromFile() {
        borrowRecords.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader("borrows.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    borrowRecords.put(parts[0], parts[1]);
                    Book.getBookById(parts[0]).setAvailable(false);
                }
            }
        } catch (IOException e) {
            // File không tồn tại hoặc lỗi, khởi tạo mới
        }
    }
}