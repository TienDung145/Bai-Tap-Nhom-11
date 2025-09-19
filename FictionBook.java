package quanlythuvien;

public class FictionBook extends Book {
    public FictionBook(String id, String title, String author) {
        super(id, title, author);
    }

    @Override
    public void displayType() {
        System.out.println("Type: Fiction");
    }
}