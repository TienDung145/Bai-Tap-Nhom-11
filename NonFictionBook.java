package quanlythuvien;

public class NonFictionBook extends Book {
    public NonFictionBook(String id, String title, String author) {
        super(id, title, author);
    }

    @Override
    public void displayType() {
        System.out.println("Type: NonFiction");
    }
}