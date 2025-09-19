package quanlythuvien;

public class Reader extends User {
    public Reader(String id, String name, String username, String password) {
        super(id, name, username, password);
    }

    @Override
    public void displayRole() {
        System.out.println("Role: Reader");
    }
}