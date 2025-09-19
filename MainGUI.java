package quanlythuvien;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class MainGUI extends JFrame {
    private User currentUser;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private Librarian librarian;

    public MainGUI() {
        setTitle("Quản Lý Thư Viện");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Đọc dữ liệu từ file
        User.loadFromFile();
        Book.loadFromFile();
        BorrowRecord.loadFromFile();

        // Khởi tạo librarian
        librarian = (Librarian) User.getUserByUsername("admin");
        if (librarian == null) {
            librarian = new Librarian("L1", "Admin", "admin", "pass");
            User.registerUser(librarian);
        }

        // Dữ liệu mẫu (nếu file rỗng)
        if (User.getTotalUsers() == 0) {
            Reader reader = new Reader("R1", "User", "user", "pass");
            User.registerUser(reader);
        }
        if (Book.getTotalBooks() == 0) {
            FictionBook book1 = new FictionBook("B1", "Harry Potter", "JK Rowling");
            Book.addBook(book1);
            NonFictionBook book2 = new NonFictionBook("B2", "Java Programming", "Herbert Schildt");
            Book.addBook(book2);
        }

        // Panel chính với CardLayout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Thêm các panel chức năng
        mainPanel.add(createLoginPanel(), "Login");
        mainPanel.add(createHomePanel(), "Home");
        mainPanel.add(createManageBookPanel(), "ManageBook");
        mainPanel.add(createBorrowPanel(), "Borrow");
        mainPanel.add(createReportPanel(), "Report");
        mainPanel.add(createSearchPanel(), "Search");

        add(mainPanel);
        cardLayout.show(mainPanel, "Login");
        setVisible(true);

        // Lưu dữ liệu khi thoát
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                User.saveToFile();
                Book.saveToFile();
                BorrowRecord.saveToFile();
                System.exit(0);
            }
        });
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel usernameLabel = new JLabel("Tên đăng nhập:");
        JTextField usernameField = new JTextField(20);
        JLabel passwordLabel = new JLabel("Mật khẩu:");
        JPasswordField passwordField = new JPasswordField(20);

        JButton loginButton = new JButton("Đăng nhập");
        JButton registerButton = new JButton("Đăng ký");

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(usernameLabel, gbc);
        gbc.gridy = 1;
        panel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(usernameField, gbc);
        gbc.gridy = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(loginButton, gbc);
        gbc.gridy = 3;
        panel.add(registerButton, gbc);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.");
                return;
            }
            User user = User.getUserByUsername(username);
            if (user != null && user.login(username, password)) {
                currentUser = user;
                JOptionPane.showMessageDialog(this, "Đăng nhập thành công.");
                setupMenu();
                cardLayout.show(mainPanel, "Home");
            } else {
                JOptionPane.showMessageDialog(this, "Thông tin đăng nhập không đúng.");
            }
        });

        registerButton.addActionListener(e -> registerUser());

        return panel;
    }

    private void registerUser() {
        String id = JOptionPane.showInputDialog(this, "ID người dùng:");
        if (id == null || id.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID không được để trống.");
            return;
        }
        String name = JOptionPane.showInputDialog(this, "Tên:");
        if (name == null || name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên không được để trống.");
            return;
        }
        String username = JOptionPane.showInputDialog(this, "Tên đăng nhập:");
        if (username == null || username.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập không được để trống.");
            return;
        }
        String password = JOptionPane.showInputDialog(this, "Mật khẩu:");
        if (password == null || password.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mật khẩu không được để trống.");
            return;
        }

        if (User.getUserByUsername(username) != null) {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập đã tồn tại.");
            return;
        }

        Reader newReader = new Reader(id, name, username, password);
        User.registerUser(newReader);
        JOptionPane.showMessageDialog(this, "Đăng ký thành công. Bạn có thể đăng nhập.");
    }

    private void setupMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu accountMenu = new JMenu("Tài khoản");
        JMenuItem logoutItem = new JMenuItem("Đăng xuất");
        logoutItem.addActionListener(e -> {
            currentUser.logout();
            setJMenuBar(null);
            cardLayout.show(mainPanel, "Login");
        });
        JMenuItem exitItem = new JMenuItem("Thoát");
        exitItem.addActionListener(e -> {
            User.saveToFile();
            Book.saveToFile();
            BorrowRecord.saveToFile();
            System.exit(0);
        });
        accountMenu.add(logoutItem);
        accountMenu.add(exitItem);

        menuBar.add(accountMenu);

        if (currentUser instanceof Librarian) {
            JMenu manageMenu = new JMenu("Quản lý sách");
            JMenuItem addItem = new JMenuItem("Thêm sách");
            addItem.addActionListener(e -> addBookDialog());
            JMenuItem viewItem = new JMenuItem("Xem sách");
            viewItem.addActionListener(e -> cardLayout.show(mainPanel, "ManageBook"));
            manageMenu.add(addItem);
            manageMenu.add(viewItem);

            JMenu borrowMenu = new JMenu("Mượn/Trả sách");
            borrowMenu.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    cardLayout.show(mainPanel, "Borrow");
                }
            });

            JMenu reportMenu = new JMenu("Thống kê");
            reportMenu.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    cardLayout.show(mainPanel, "Report");
                }
            });

            JMenu searchMenu = new JMenu("Tìm kiếm");
            searchMenu.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    cardLayout.show(mainPanel, "Search");
                }
            });

            menuBar.add(manageMenu);
            menuBar.add(borrowMenu);
            menuBar.add(reportMenu);
            menuBar.add(searchMenu);
        } else if (currentUser instanceof Reader) {
            JMenu viewMenu = new JMenu("Xem sách");
            viewMenu.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    cardLayout.show(mainPanel, "ManageBook");
                }
            });

            JMenu borrowMenu = new JMenu("Mượn/Trả sách");
            borrowMenu.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    cardLayout.show(mainPanel, "Borrow");
                }
            });

            menuBar.add(viewMenu);
            menuBar.add(borrowMenu);
        }

        setJMenuBar(menuBar);
    }

    private JPanel createHomePanel() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Chào mừng đến với Quản Lý Thư Viện!"));
        return panel;
    }

    private JPanel createManageBookPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Tiêu đề", "Tác giả", "Có sẵn"}, 0);
        JTable table = new JTable(model);
        loadBooksToTable(model);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        if (currentUser instanceof Librarian) {
            JButton addButton = new JButton("Thêm");
            addButton.addActionListener(e -> addBookDialog());
            JButton updateButton = new JButton("Cập nhật");
            updateButton.addActionListener(e -> updateBook(table));
            JButton deleteButton = new JButton("Xóa");
            deleteButton.addActionListener(e -> deleteBook(table, model));
            buttonPanel.add(addButton);
            buttonPanel.add(updateButton);
            buttonPanel.add(deleteButton);
        }
        JButton refreshButton = new JButton("Làm mới");
        refreshButton.addActionListener(e -> loadBooksToTable(model));
        buttonPanel.add(refreshButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void loadBooksToTable(DefaultTableModel model) {
        model.setRowCount(0);
        for (Book b : Book.books.values()) {
            model.addRow(new Object[]{b.getId(), b.getTitle(), b.getAuthor(), b.isAvailable()});
        }
    }

    private void addBookDialog() {
        if (librarian == null) {
            JOptionPane.showMessageDialog(this, "Librarian không tồn tại. Vui lòng khởi động lại chương trình.");
            return;
        }
        String id = JOptionPane.showInputDialog(this, "ID sách:");
        if (id == null || id.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID không được để trống.");
            return;
        }
        if (Book.getBookById(id) != null) {
            JOptionPane.showMessageDialog(this, "ID sách đã tồn tại.");
            return;
        }
        String title = JOptionPane.showInputDialog(this, "Tiêu đề:");
        if (title == null || title.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tiêu đề không được để trống.");
            return;
        }
        String author = JOptionPane.showInputDialog(this, "Tác giả:");
        if (author == null || author.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tác giả không được để trống.");
            return;
        }
        String type = JOptionPane.showInputDialog(this, "Loại (Fiction/NonFiction):");
        if (type == null || (!type.equalsIgnoreCase("Fiction") && !type.equalsIgnoreCase("NonFiction"))) {
            JOptionPane.showMessageDialog(this, "Loại sách phải là Fiction hoặc NonFiction.");
            return;
        }
        try {
            Book book = type.equalsIgnoreCase("Fiction") ? new FictionBook(id, title, author) : new NonFictionBook(id, title, author);
            librarian.add(book);
            JOptionPane.showMessageDialog(this, "Thêm sách thành công: " + title);
            cardLayout.show(mainPanel, "ManageBook");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi thêm sách: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateBook(JTable table) {
        int row = table.getSelectedRow();
        if (row >= 0) {
            String id = (String) table.getValueAt(row, 0);
            Book book = Book.getBookById(id);
            if (book != null && librarian != null) {
                librarian.update(book);
                JOptionPane.showMessageDialog(this, "Cập nhật sách: " + book.getTitle());
            } else {
                JOptionPane.showMessageDialog(this, "Sách không tồn tại hoặc không có quyền.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một sách để cập nhật.");
        }
    }

    private void deleteBook(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row >= 0) {
            String id = (String) table.getValueAt(row, 0);
            Book book = Book.getBookById(id);
            if (book != null && librarian != null) {
                librarian.delete(book);
                loadBooksToTable(model);
                JOptionPane.showMessageDialog(this, "Xóa sách thành công: " + book.getTitle());
            } else {
                JOptionPane.showMessageDialog(this, "Sách không tồn tại hoặc không có quyền.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một sách để xóa.");
        }
    }

    private JPanel createBorrowPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        DefaultListModel<String> availableModel = new DefaultListModel<>();
        JList<String> availableList = new JList<>(availableModel);
        loadAvailableBooks(availableModel);
        availableList.setDragEnabled(true);
        availableList.setTransferHandler(new TransferHandler() {
            @Override
            public int getSourceActions(JComponent c) { return TransferHandler.COPY_OR_MOVE; }
            @Override
            protected Transferable createTransferable(JComponent c) {
                JList<String> list = (JList<String>) c;
                return new StringSelection(list.getSelectedValue());
            }
        });

        DefaultListModel<String> borrowedModel = new DefaultListModel<>();
        JList<String> borrowedList = new JList<>(borrowedModel);
        borrowedList.setDragEnabled(true);
        borrowedList.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) { return support.isDataFlavorSupported(DataFlavor.stringFlavor); }
            @Override
            public boolean importData(TransferSupport support) {
                try {
                    String title = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                    String bookId = getIdByTitle(title);
                    if (bookId != null && Book.isAvailable(bookId)) {
                        BorrowRecord record = new BorrowRecord();
                        record.borrowBook(bookId, currentUser.getId());
                        loadAvailableBooks(availableModel);
                        borrowedModel.addElement(title);
                        JOptionPane.showMessageDialog(MainGUI.this, "Mượn sách thành công: " + title);
                        return true;
                    } else {
                        JOptionPane.showMessageDialog(MainGUI.this, "Sách không có sẵn hoặc không tồn tại.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                JOptionPane.showMessageDialog(MainGUI.this, "Mượn sách thất bại.");
                return false;
            }
        });

        availableList.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) { return support.isDataFlavorSupported(DataFlavor.stringFlavor); }
            @Override
            public boolean importData(TransferSupport support) {
                try {
                    String title = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                    String bookId = getIdByTitle(title);
                    if (bookId != null && !Book.isAvailable(bookId)) {
                        BorrowRecord record = new BorrowRecord();
                        record.returnBook(bookId, currentUser.getId());
                        loadAvailableBooks(availableModel);
                        borrowedModel.removeElement(title);
                        JOptionPane.showMessageDialog(MainGUI.this, "Trả sách thành công: " + title);
                        return true;
                    } else {
                        JOptionPane.showMessageDialog(MainGUI.this, "Sách không đang mượn hoặc không tồn tại.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                JOptionPane.showMessageDialog(MainGUI.this, "Trả sách thất bại.");
                return false;
            }
        });

        JPanel listPanel = new JPanel(new GridLayout(1, 2));
        listPanel.add(new JScrollPane(availableList));
        listPanel.add(new JScrollPane(borrowedList));
        panel.add(listPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton borrowButton = new JButton("Mượn thủ công");
        borrowButton.addActionListener(e -> {
            String bookId = JOptionPane.showInputDialog(this, "ID sách mượn:");
            if (bookId != null && !bookId.trim().isEmpty()) {
                Book book = Book.getBookById(bookId);
                if (book != null && book.isAvailable()) {
                    BorrowRecord record = new BorrowRecord();
                    record.borrowBook(bookId, currentUser.getId());
                    loadAvailableBooks(availableModel);
                    borrowedModel.addElement(book.getTitle());
                    JOptionPane.showMessageDialog(this, "Mượn sách thành công.");
                } else {
                    JOptionPane.showMessageDialog(this, "Sách không có sẵn hoặc không tồn tại.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "ID sách không được để trống.");
            }
        });
        JButton returnButton = new JButton("Trả thủ công");
        returnButton.addActionListener(e -> {
            String bookId = JOptionPane.showInputDialog(this, "ID sách trả:");
            if (bookId != null && !bookId.trim().isEmpty()) {
                Book book = Book.getBookById(bookId);
                if (book != null && !book.isAvailable()) {
                    BorrowRecord record = new BorrowRecord();
                    record.returnBook(bookId, currentUser.getId());
                    loadAvailableBooks(availableModel);
                    borrowedModel.removeElement(book.getTitle());
                    JOptionPane.showMessageDialog(this, "Trả sách thành công.");
                } else {
                    JOptionPane.showMessageDialog(this, "Sách không đang mượn hoặc không tồn tại.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "ID sách không được để trống.");
            }
        });
        buttonPanel.add(borrowButton);
        buttonPanel.add(returnButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadAvailableBooks(DefaultListModel<String> model) {
        model.clear();
        for (Book b : Book.books.values()) {
            if (b.isAvailable()) {
                model.addElement(b.getTitle());
            }
        }
    }

    private String getIdByTitle(String title) {
        for (Book b : Book.books.values()) {
            if (b.getTitle().equals(title)) {
                return b.getId();
            }
        }
        return null;
    }

    private JPanel createReportPanel() {
        JPanel panel = new JPanel();
        JTextArea reportArea = new JTextArea(10, 50);
        reportArea.setEditable(false);
        reportArea.append("Tổng sách: " + Book.getTotalBooks() + "\n");
        reportArea.append("Tổng người dùng: " + User.getTotalUsers() + "\n");
        reportArea.append("Tổng lượt mượn: " + BorrowRecord.getTotalBorrows() + "\n");
        panel.add(new JScrollPane(reportArea));
        return panel;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Tìm");
        JPanel topPanel = new JPanel();
        topPanel.add(searchField);
        topPanel.add(searchButton);
        panel.add(topPanel, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Tiêu đề", "Tác giả", "Có sẵn"}, 0);
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        searchButton.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            if (keyword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập từ khóa tìm kiếm.");
                return;
            }
            model.setRowCount(0);
            List<Book> results = librarian.searchByName(keyword);
            for (Book b : results) {
                model.addRow(new Object[]{b.getId(), b.getTitle(), b.getAuthor(), b.isAvailable()});
            }
            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy sách nào.");
            }
        });

        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new MainGUI();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Lỗi khởi động giao diện: " + e.getMessage());
            }
        });
    }
}