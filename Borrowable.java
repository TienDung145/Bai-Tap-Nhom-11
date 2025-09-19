package quanlythuvien;

public interface Borrowable {
    void borrowBook(String bookId, String userId);
    void returnBook(String bookId, String userId);
}