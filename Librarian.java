package quanlythuvien;

import java.util.List;

public class Librarian extends User implements Manageable, Searchable {

    public Librarian(String id, String name, String username, String password) {
        super(id, name, username, password);
    }

    @Override
    public void displayRole() {
        System.out.println("Role: Librarian");
    }

    @Override
    public void add(Book book) {
        if (book != null && Book.getBookById(book.getId()) == null) {
            Book.addBook(book);
            Book.saveToFile();
            System.out.println("Librarian added book: " + book.getTitle() + " (ID: " + book.getId() + ")");
        }
    }

    @Override
    public void update(Book book) {
        if (book != null) {
            Book.saveToFile();
            System.out.println("Librarian updated book: " + book.getTitle());
        }
    }

    @Override
    public void delete(Book book) {
        if (book != null) {
            Book.removeBook(book.getId());
            Book.saveToFile();
            System.out.println("Librarian deleted book: " + book.getTitle());
        }
    }

    @Override
    public List<Book> searchByName(String keyword) {
        List<Book> results = new java.util.ArrayList<>();
        for (Book b : Book.books.values()) {
            if (b.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
                results.add(b);
            }
        }
        return results;
    }
}
