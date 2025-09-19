package quanlythuvien;

public interface Authenticator {
    boolean login(String username, String password);
    void logout();
}