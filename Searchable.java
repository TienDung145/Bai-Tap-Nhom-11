package quanlythuvien;

import java.util.List;

public interface Searchable {
    List<Book> searchByName(String keyword);
}