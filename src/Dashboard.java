import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.awt.print.Printable;
import java.awt.print.PageFormat;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author rafli
 */
public class Dashboard extends javax.swing.JFrame {
    
    // Models dan Data
    private DefaultTableModel cartTableModel;
    private List<Product> productList;
    private List<Product> filteredProductList;
    private DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    
    // Variabel transaksi
    private double subtotal = 0.0;
    private double taxRate = 0.1; // 10% tax
    private double taxAmount = 0.0;
    private double total = 0.0;
    private double amountPaid = 0.0;
    private double changeAmount = 0.0;
    
    // Components (akan diinisialisasi di initComponents)
    private JTextField cashInputField;
    private JTextField searchField;
    private JLabel subtotalLabel, taxLabel, totalLabel, changeLabel;
    private JComboBox<String> paymentMethodCombo;
    private JButton[] numpadButtons;
    private JButton payButton, printButton, resetButton, removeButton;
    
    // Menu components
    private JMenuBar menuBar;
    private JMenu adminMenu;
    private JMenuItem productMenuItem, userMenuItem, transactionMenuItem;
    
    // Product inner class
    static class Product {
        int id;
        String name;
        double price;
        String category;
        String unit;
        int stock;
        String imagePath;
        
        Product(int id, String name, double price, String category, String unit, int stock, String imagePath) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.category = category;
            this.unit = unit;
            this.stock = stock;
            this.imagePath = imagePath;
        }
    }

    /**
     * Creates new form Dashboard
     */
    public Dashboard() {
        initComponents();
        initMenuBar();
        initPOSComponents();
        loadProducts();
        updateTransactionDisplay();
    }
    
    private void initMenuBar() {
        // Create menu bar
        menuBar = new JMenuBar();
        
        // Create admin menu
        adminMenu = new JMenu("Menu Item");
        adminMenu.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Create menu items
        productMenuItem = new JMenuItem("Manage Products");
        productMenuItem.setFont(new Font("Arial", Font.PLAIN, 12));
        productMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFormProduct();
            }
        });
        
        userMenuItem = new JMenuItem("Manage Users");
        userMenuItem.setFont(new Font("Arial", Font.PLAIN, 12));
        userMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFormUser();
            }
        });
        
        transactionMenuItem = new JMenuItem("View Transactions");
        transactionMenuItem.setFont(new Font("Arial", Font.PLAIN, 12));
        transactionMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFormTransaction();
            }
        });
        
        // Add menu items to admin menu
        adminMenu.add(productMenuItem);
        adminMenu.add(userMenuItem);
        adminMenu.add(transactionMenuItem);
        
        // Add admin menu to menu bar
        menuBar.add(adminMenu);
        
        // Set menu bar to frame
        setJMenuBar(menuBar);
    }
    
    private void openFormProduct() {
        try {
            FormProduct formProduct = new FormProduct();
            formProduct.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error opening Product form: " + e.getMessage());
        }
    }
    
    private void openFormUser() {
        try {
            FormUser formUser = new FormUser();
            formUser.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error opening User form: " + e.getMessage());
        }
    }
    
    private void openFormTransaction() {
        try {
            FormTransaksi formTransaksi = new FormTransaksi();
            formTransaksi.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error opening Transaction form: " + e.getMessage());
        }
    }
    
    private void initPOSComponents() {
        // Initialize UI components first
        searchField = new JTextField(20);
        cashInputField = new JTextField(10);
        subtotalLabel = new JLabel("Rp 0.00");
        taxLabel = new JLabel("Rp 0.00");
        totalLabel = new JLabel("Rp 0.00");
        changeLabel = new JLabel("Rp 0.00");
        paymentMethodCombo = new JComboBox<>();
        
        // Initialize numpad buttons
        numpadButtons = new JButton[12];
        String[] buttonLabels = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "C", "0", "."};
        for (int i = 0; i < numpadButtons.length; i++) {
            numpadButtons[i] = new JButton(buttonLabels[i]);
        }
        
        // Initialize action buttons
        payButton = new JButton("Pay");
        printButton = new JButton("Print");
        resetButton = new JButton("Reset");
        removeButton = new JButton("Remove");
        printButton.setEnabled(false);
        
        // Initialize cart table model
        cartTableModel = new DefaultTableModel();
        cartTableModel.addColumn("ID");
        cartTableModel.addColumn("Nama Produk");
        cartTableModel.addColumn("Harga");
        cartTableModel.addColumn("Qty");
        cartTableModel.addColumn("Subtotal");
        cartTable.setModel(cartTableModel);
        
        // Hide ID column
        cartTable.getColumnModel().getColumn(0).setMinWidth(0);
        cartTable.getColumnModel().getColumn(0).setMaxWidth(0);
        cartTable.getColumnModel().getColumn(0).setWidth(0);
        
        // Set column widths
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(60);
        cartTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        
        // Initialize search field listener
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterProducts();
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                filterProducts();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                filterProducts();
            }
        });
        
        // Initialize payment method combo
        paymentMethodCombo.addItem("Cash");
        paymentMethodCombo.addItem("Gopay");
        paymentMethodCombo.addItem("Shopeepay");
        paymentMethodCombo.addItem("Kartu Debit");
        paymentMethodCombo.addItem("Kartu Kredit");
        
        // Add payment method change listener
        paymentMethodCombo.addActionListener(e -> {
            boolean isCash = "Cash".equals(paymentMethodCombo.getSelectedItem());
            cashInputField.setEnabled(isCash);
            enableNumpad(isCash);
            if (!isCash) {
                cashInputField.setText(currencyFormat.format(total));
                amountPaid = total;
                updateTransactionDisplay();
            }
        });
        
        // Initialize numpad buttons actions
        for (int i = 0; i < numpadButtons.length; i++) {
            final String buttonText = numpadButtons[i].getText();
            numpadButtons[i].addActionListener(e -> handleNumpadClick(buttonText));
        }
        
        // Initialize action buttons
        payButton.addActionListener(e -> processPayment());
        printButton.addActionListener(e -> printReceipt());
        resetButton.addActionListener(e -> resetCart());
        removeButton.addActionListener(e -> removeSelectedItem());
        
        // Initialize cash input field
        cashInputField.setText("0.00");
        
        // Add search panel to products panel
        setupSearchPanel();
        
        // Add components to transaction panel
        setupTransactionPanel();
    }
    
    private void setupSearchPanel() {
        // Add search panel at the top of products
        JPanel productSearchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        productSearchPanel.add(new JLabel("Search Products:"));
        productSearchPanel.add(searchField);
        leftPanel.add(productSearchPanel, BorderLayout.NORTH);
    }
    
    private void setupTransactionPanel() {
        transactionPanel.setLayout(new BorderLayout());
        
        // Create main transaction content panel
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Create larger fonts for better visibility
        Font labelFont = new Font("Arial", Font.BOLD, 16);
        Font valueFont = new Font("Arial", Font.BOLD, 18);
        Font buttonFont = new Font("Arial", Font.BOLD, 14);
        
        // Transaction totals - make labels and values larger
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel subtotalTextLabel = new JLabel("Subtotal:");
        subtotalTextLabel.setFont(labelFont);
        contentPanel.add(subtotalTextLabel, gbc);
        gbc.gridx = 1;
        subtotalLabel.setFont(valueFont);
        subtotalLabel.setForeground(new Color(0, 0, 0));
        contentPanel.add(subtotalLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel taxTextLabel = new JLabel("Tax (10%):");
        taxTextLabel.setFont(labelFont);
        contentPanel.add(taxTextLabel, gbc);
        gbc.gridx = 1;
        taxLabel.setFont(valueFont);
        taxLabel.setForeground(new Color(0, 0, 0));
        contentPanel.add(taxLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel totalTextLabel = new JLabel("Total:");
        totalTextLabel.setFont(labelFont);
        contentPanel.add(totalTextLabel, gbc);
        gbc.gridx = 1;
        totalLabel.setFont(new Font("Arial", Font.BOLD, 20));
        totalLabel.setForeground(new Color(220, 20, 60));
        contentPanel.add(totalLabel, gbc);
        
        // Payment method - make combo box larger
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel paymentTextLabel = new JLabel("Payment:");
        paymentTextLabel.setFont(labelFont);
        contentPanel.add(paymentTextLabel, gbc);
        gbc.gridx = 1;
        paymentMethodCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        paymentMethodCombo.setPreferredSize(new Dimension(200, 35));
        contentPanel.add(paymentMethodCombo, gbc);
        
        // Cash input - make input field larger
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel cashTextLabel = new JLabel("Cash:");
        cashTextLabel.setFont(labelFont);
        contentPanel.add(cashTextLabel, gbc);
        gbc.gridx = 1;
        cashInputField.setFont(new Font("Arial", Font.BOLD, 16));
        cashInputField.setPreferredSize(new Dimension(200, 35));
        contentPanel.add(cashInputField, gbc);
        
        // Change - make change label larger
        gbc.gridx = 0; gbc.gridy = 5;
        JLabel changeTextLabel = new JLabel("Change:");
        changeTextLabel.setFont(labelFont);
        contentPanel.add(changeTextLabel, gbc);
        gbc.gridx = 1;
        changeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        changeLabel.setForeground(new Color(0, 150, 0));
        contentPanel.add(changeLabel, gbc);
        
        // Numpad - make buttons much larger
        JPanel numpadPanel = new JPanel(new GridLayout(4, 3, 8, 8));
        numpadPanel.setPreferredSize(new Dimension(250, 200));
        for (JButton button : numpadButtons) {
            button.setFont(new Font("Arial", Font.BOLD, 18));
            button.setPreferredSize(new Dimension(75, 45));
            numpadPanel.add(button);
        }
        
        gbc.gridx = 2; gbc.gridy = 0; gbc.gridheight = 6; gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        contentPanel.add(numpadPanel, gbc);
        
        // Add some vertical spacing before buttons
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 3; gbc.gridheight = 1; gbc.weightx = 0; gbc.weighty = 0;
        contentPanel.add(Box.createVerticalStrut(20), gbc);
        
        // Buttons panel - make buttons larger
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Style all action buttons
        removeButton.setFont(buttonFont);
        removeButton.setPreferredSize(new Dimension(120, 45));
        removeButton.setBackground(new Color(255, 99, 71));
        removeButton.setForeground(Color.WHITE);
        removeButton.setFocusPainted(false);
        
        resetButton.setFont(buttonFont);
        resetButton.setPreferredSize(new Dimension(120, 45));
        resetButton.setBackground(new Color(255, 165, 0));
        resetButton.setForeground(Color.WHITE);
        resetButton.setFocusPainted(false);
        
        payButton.setFont(buttonFont);
        payButton.setPreferredSize(new Dimension(120, 45));
        payButton.setBackground(new Color(34, 139, 34));
        payButton.setForeground(Color.WHITE);
        payButton.setFocusPainted(false);
        
        printButton.setFont(buttonFont);
        printButton.setPreferredSize(new Dimension(120, 45));
        printButton.setBackground(new Color(70, 130, 180));
        printButton.setForeground(Color.WHITE);
        printButton.setFocusPainted(false);
        
        buttonPanel.add(removeButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(payButton);
        buttonPanel.add(printButton);
        
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 3; gbc.gridheight = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(buttonPanel, gbc);
        
        transactionPanel.add(contentPanel, BorderLayout.CENTER);
    }
    
    private void loadProducts() {
        productList = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.connect();
            String sql = "SELECT id, name, price, category, unit, stock, image_path FROM products WHERE deleted_at IS NULL AND stock > 0";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Product product = new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getString("category"),
                    rs.getString("unit"),
                    rs.getInt("stock"),
                    rs.getString("image_path")
                );
                productList.add(product);
            }
            
            conn.close();
            filteredProductList = new ArrayList<>(productList);
            displayProducts();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading products: " + e.getMessage());
        }
    }
    
    private void filterProducts() {
        String searchText = searchField.getText().toLowerCase().trim();
        filteredProductList = new ArrayList<>();
        
        for (Product product : productList) {
            if (searchText.isEmpty() || product.name.toLowerCase().contains(searchText)) {
                filteredProductList.add(product);
            }
        }
        
        displayProducts();
    }
    
    private void displayProducts() {
        productPanel.removeAll();
        
        // Use BoxLayout vertikal untuk membuat baris-baris
        productPanel.setLayout(new BoxLayout(productPanel, BoxLayout.Y_AXIS));
        
        // Group products into rows of 3
        for (int i = 0; i < filteredProductList.size(); i += 3) {
            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
            rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            // Add up to 3 products per row
            for (int j = i; j < Math.min(i + 3, filteredProductList.size()); j++) {
                Product product = filteredProductList.get(j);
                JPanel card = createProductCard(product);
                rowPanel.add(card);
            }
            
            productPanel.add(rowPanel);
        }
        
        // Add vertical glue to push all rows to the top
        productPanel.add(Box.createVerticalGlue());
        
        productPanel.revalidate();
        productPanel.repaint();
    }
    
    private JPanel createProductCard(Product product) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEtchedBorder());
        // Membuat card berbentuk kotak persegi
        card.setPreferredSize(new Dimension(170, 170));
        card.setMinimumSize(new Dimension(170, 170));
        card.setMaximumSize(new Dimension(170, 170));
        
        // Image panel
        JPanel imagePanel = new JPanel();
        imagePanel.setLayout(new BorderLayout());
        imagePanel.setPreferredSize(new Dimension(170, 110));
        imagePanel.setMinimumSize(new Dimension(170, 110));
        imagePanel.setMaximumSize(new Dimension(170, 110));
        
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        if (product.imagePath != null && !product.imagePath.isEmpty()) {
            try {
                ImageIcon icon = new ImageIcon(product.imagePath);
                Image img = icon.getImage().getScaledInstance(150, 90, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(img));
            } catch (Exception e) {
                imageLabel.setText("No Image");
                imageLabel.setFont(new Font("Arial", Font.PLAIN, 10));
                imageLabel.setForeground(Color.GRAY);
            }
        } else {
            imageLabel.setText("No Image");
            imageLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            imageLabel.setForeground(Color.GRAY);
        }
        
        imagePanel.add(imageLabel, BorderLayout.CENTER);
        
        // Info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        infoPanel.setPreferredSize(new Dimension(170, 60));
        infoPanel.setMinimumSize(new Dimension(170, 60));
        infoPanel.setMaximumSize(new Dimension(170, 60));
        
        // Truncate long product names
        String displayName = product.name;
        if (displayName.length() > 15) {
            displayName = displayName.substring(0, 12) + "...";
        }
        
        JLabel nameLabel = new JLabel("<html><center>" + displayName + "</center></html>");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 10));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setToolTipText(product.name); // Show full name on hover
        
        JLabel priceLabel = new JLabel("Rp " + currencyFormat.format(product.price));
        priceLabel.setFont(new Font("Arial", Font.PLAIN, 9));
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        priceLabel.setForeground(new Color(0, 150, 0));
        
        JLabel stockLabel = new JLabel("Stock: " + product.stock);
        stockLabel.setFont(new Font("Arial", Font.PLAIN, 8));
        stockLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        stockLabel.setForeground(Color.GRAY);
        
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(1));
        infoPanel.add(priceLabel);
        infoPanel.add(Box.createVerticalStrut(1));
        infoPanel.add(stockLabel);
        
        card.add(imagePanel, BorderLayout.CENTER);
        card.add(infoPanel, BorderLayout.SOUTH);
        
        // Add click listener
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addToCart(product);
            }
            
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBackground(new Color(240, 240, 240));
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBackground(UIManager.getColor("Panel.background"));
                card.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
        
        return card;
    }
    
    private void addToCart(Product product) {
        // Check if product already in cart
        for (int i = 0; i < cartTableModel.getRowCount(); i++) {
            int productId = (Integer) cartTableModel.getValueAt(i, 0);
            if (productId == product.id) {
                // Increase quantity
                int currentQty = (Integer) cartTableModel.getValueAt(i, 3);
                if (currentQty < product.stock) {
                    int newQty = currentQty + 1;
                    double itemSubtotal = product.price * newQty;
                    cartTableModel.setValueAt(newQty, i, 3);
                    cartTableModel.setValueAt(currencyFormat.format(itemSubtotal), i, 4);
                    calculateTotal();
                } else {
                    JOptionPane.showMessageDialog(this, "Stock tidak mencukupi!");
                }
                return;
            }
        }
        
        // Add new item to cart
        if (product.stock > 0) {
            cartTableModel.addRow(new Object[]{
                product.id,
                product.name,
                currencyFormat.format(product.price),
                1,
                currencyFormat.format(product.price)
            });
            calculateTotal();
        } else {
            JOptionPane.showMessageDialog(this, "Product out of stock!");
        }
    }
    
    private void calculateTotal() {
        subtotal = 0.0;
        for (int i = 0; i < cartTableModel.getRowCount(); i++) {
            int productId = (Integer) cartTableModel.getValueAt(i, 0);
            int qty = (Integer) cartTableModel.getValueAt(i, 3);
            
            // Find product price
            for (Product product : productList) {
                if (product.id == productId) {
                    subtotal += product.price * qty;
                    break;
                }
            }
        }
        
        taxAmount = subtotal * taxRate;
        total = subtotal + taxAmount;
        
        updateTransactionDisplay();
    }
    
    private void updateTransactionDisplay() {
        subtotalLabel.setText("Rp " + currencyFormat.format(subtotal));
        taxLabel.setText("Rp " + currencyFormat.format(taxAmount));
        totalLabel.setText("Rp " + currencyFormat.format(total));
        
        // Calculate change
        try {
            amountPaid = Double.parseDouble(cashInputField.getText().replace(",", ""));
        } catch (NumberFormatException e) {
            amountPaid = 0.0;
        }
        
        changeAmount = amountPaid - total;
        if (changeAmount < 0) changeAmount = 0;
        
        changeLabel.setText("Rp " + currencyFormat.format(changeAmount));
        changeLabel.setForeground(changeAmount >= 0 ? new Color(0, 150, 0) : Color.RED);
    }
    
    private void handleNumpadClick(String buttonText) {
        if (!cashInputField.isEnabled()) return;
        
        String currentText = cashInputField.getText().replace(",", "");
        
        switch (buttonText) {
            case "C":
                cashInputField.setText("0.00");
                break;
            case ".":
                if (!currentText.contains(".")) {
                    cashInputField.setText(currentText + ".");
                }
                break;
            default:
                if (currentText.equals("0") || currentText.equals("0.00")) {
                    cashInputField.setText(buttonText);
                } else {
                    cashInputField.setText(currentText + buttonText);
                }
                break;
        }
        
        updateTransactionDisplay();
    }
    
    private void enableNumpad(boolean enabled) {
        for (JButton button : numpadButtons) {
            button.setEnabled(enabled);
        }
    }
    
    private void processPayment() {
        if (cartTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Cart is empty!");
            return;
        }
        
        if ("Cash".equals(paymentMethodCombo.getSelectedItem()) && amountPaid < total) {
            JOptionPane.showMessageDialog(this, "Insufficient payment amount!");
            return;
        }
        
        // Ask for customer name (optional)
        String customerName = JOptionPane.showInputDialog(this, 
            "Enter customer name (optional):", "Customer Information", 
            JOptionPane.QUESTION_MESSAGE);
        
        if (customerName == null) customerName = "-"; // User canceled
        if (customerName.trim().isEmpty()) customerName = "-";
        
        // Process transaction
        try {
            Connection conn = DatabaseConnection.connect();
            conn.setAutoCommit(false);
            
            // Generate invoice number
            String invoice = UUID.randomUUID().toString();
            
            // Insert transaction
            String insertTransactionSQL = "INSERT INTO transactions (invoice, amount_paid, subtotal, tax_amount, total_amount, change_amount, customer_name, payment_method) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement transactionStmt = conn.prepareStatement(insertTransactionSQL, Statement.RETURN_GENERATED_KEYS);
            transactionStmt.setString(1, invoice);
            transactionStmt.setDouble(2, amountPaid);
            transactionStmt.setDouble(3, subtotal);
            transactionStmt.setDouble(4, taxAmount);
            transactionStmt.setDouble(5, total);
            transactionStmt.setDouble(6, changeAmount);
            transactionStmt.setString(7, customerName);
            transactionStmt.setString(8, paymentMethodCombo.getSelectedItem().toString());
            
            transactionStmt.executeUpdate();
            ResultSet rs = transactionStmt.getGeneratedKeys();
            int transactionId = 0;
            if (rs.next()) {
                transactionId = rs.getInt(1);
            }
            
            // Insert transaction details and update stock
            for (int i = 0; i < cartTableModel.getRowCount(); i++) {
                int productId = (Integer) cartTableModel.getValueAt(i, 0);
                String productName = cartTableModel.getValueAt(i, 1).toString();
                int qty = (Integer) cartTableModel.getValueAt(i, 3);
                
                // Find product price
                double price = 0;
                for (Product product : productList) {
                    if (product.id == productId) {
                        price = product.price;
                        break;
                    }
                }
                
                double itemSubtotal = price * qty;
                
                // Insert transaction detail
                String insertDetailSQL = "INSERT INTO transaction_details (transaction_id, product_id, product_name, price, quantity, subtotal) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement detailStmt = conn.prepareStatement(insertDetailSQL);
                detailStmt.setInt(1, transactionId);
                detailStmt.setInt(2, productId);
                detailStmt.setString(3, productName);
                detailStmt.setDouble(4, price);
                detailStmt.setInt(5, qty);
                detailStmt.setDouble(6, itemSubtotal);
                detailStmt.executeUpdate();
                
                // Update product stock
                String updateStockSQL = "UPDATE products SET stock = stock - ? WHERE id = ?";
                PreparedStatement stockStmt = conn.prepareStatement(updateStockSQL);
                stockStmt.setInt(1, qty);
                stockStmt.setInt(2, productId);
                stockStmt.executeUpdate();
            }
            
            conn.commit();
            conn.close();
            
            JOptionPane.showMessageDialog(this, 
                "Transaction completed successfully!\nInvoice: " + invoice.substring(0, 8) + "...", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
            // Hapus Current Cart After Payment
            cartTableModel.setRowCount(0);
            calculateTotal();
            cashInputField.setText("0.00");
            
            // Enable print button
            printButton.setEnabled(true);
            
            // Reload products to update stock
            loadProducts();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Transaction failed: " + e.getMessage());
        }
    }
    
    private void printReceipt() {
        if (cartTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No transaction to print!");
            return;
        }
        
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(new ReceiptPrintable());
        
        if (job.printDialog()) {
            try {
                job.print();
                JOptionPane.showMessageDialog(this, "Receipt printed successfully!");
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(this, "Print failed: " + e.getMessage());
            }
        }
    }
    
    private void resetCart() {
        int option = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to clear the cart?", 
            "Confirm Reset", JOptionPane.YES_NO_OPTION);
        
        if (option == JOptionPane.YES_OPTION) {
            cartTableModel.setRowCount(0);
            calculateTotal();
            cashInputField.setText("0.00");
            printButton.setEnabled(false);
        }
    }
    
    private void removeSelectedItem() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow >= 0) {
            cartTableModel.removeRow(selectedRow);
            calculateTotal();
        } else {
            JOptionPane.showMessageDialog(this, "Please select an item to remove!");
        }
    }
    
    // Printable class for receipts
    private class ReceiptPrintable implements Printable {
        @Override
        public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
            if (page > 0) return NO_SUCH_PAGE;
            
            Graphics2D g2d = (Graphics2D) g;
            g2d.translate(pf.getImageableX(), pf.getImageableY());
            
            Font titleFont = new Font("Arial", Font.BOLD, 16);
            Font normalFont = new Font("Arial", Font.PLAIN, 12);
            Font smallFont = new Font("Arial", Font.PLAIN, 10);
            
            int y = 20;
            
            // Title
            g2d.setFont(titleFont);
            g2d.drawString("RECEIPT", 100, y);
            y += 30;
            
            // Date
            g2d.setFont(normalFont);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            g2d.drawString("Date: " + dateFormat.format(new Date()), 20, y);
            y += 20;
            
            // Line separator
            g2d.drawString("--------------------------------", 20, y);
            y += 20;
            
            // Items
            g2d.setFont(smallFont);
            for (int i = 0; i < cartTableModel.getRowCount(); i++) {
                String name = cartTableModel.getValueAt(i, 1).toString();
                String price = cartTableModel.getValueAt(i, 2).toString();
                String qty = cartTableModel.getValueAt(i, 3).toString();
                String subtotal = cartTableModel.getValueAt(i, 4).toString();
                
                g2d.drawString(name, 20, y);
                y += 15;
                g2d.drawString(qty + " x " + price + " = " + subtotal, 30, y);
                y += 20;
            }
            
            // Line separator
            g2d.drawString("--------------------------------", 20, y);
            y += 20;
            
            // Totals
            g2d.setFont(normalFont);
            g2d.drawString("Subtotal: Rp " + currencyFormat.format(subtotal), 20, y);
            y += 15;
            g2d.drawString("Tax: Rp " + currencyFormat.format(taxAmount), 20, y);
            y += 15;
            g2d.drawString("Total: Rp " + currencyFormat.format(total), 20, y);
            y += 15;
            g2d.drawString("Paid: Rp " + currencyFormat.format(amountPaid), 20, y);
            y += 15;
            g2d.drawString("Change: Rp " + currencyFormat.format(changeAmount), 20, y);
            y += 30;
            
            g2d.setFont(smallFont);
            g2d.drawString("Thank you for your purchase!", 50, y);
            
            return PAGE_EXISTS;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        leftPanel = new javax.swing.JPanel();
        productScrollPane = new javax.swing.JScrollPane();
        productPanel = new javax.swing.JPanel();
        rightPanel = new javax.swing.JPanel();
        cartPanel = new javax.swing.JPanel();
        cartScrollPane = new javax.swing.JScrollPane();
        cartTable = new javax.swing.JTable();
        transactionPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("POS Dashboard");
        setExtendedState(6);

        mainPanel.setLayout(new java.awt.BorderLayout());

        leftPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Products"));
        leftPanel.setPreferredSize(new java.awt.Dimension(600, 600));
        leftPanel.setLayout(new java.awt.BorderLayout());

        productScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        productPanel.setLayout(new java.awt.GridLayout(0, 4, 10, 10));
        productScrollPane.setViewportView(productPanel);

        leftPanel.add(productScrollPane, BorderLayout.CENTER);

        mainPanel.add(leftPanel, java.awt.BorderLayout.WEST);

        rightPanel.setPreferredSize(new java.awt.Dimension(600, 600));
        rightPanel.setLayout(new java.awt.BorderLayout());

        cartPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Shopping Cart"));
        cartPanel.setPreferredSize(new java.awt.Dimension(580, 300));
        cartPanel.setLayout(new java.awt.BorderLayout());

        cartTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        cartScrollPane.setViewportView(cartTable);

        cartPanel.add(cartScrollPane);

        rightPanel.add(cartPanel, java.awt.BorderLayout.NORTH);

        transactionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Transaction"));
        transactionPanel.setLayout(new java.awt.BorderLayout());
        rightPanel.add(transactionPanel, java.awt.BorderLayout.CENTER);

        mainPanel.add(rightPanel, java.awt.BorderLayout.CENTER);

        getContentPane().add(mainPanel);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Dashboard().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel cartPanel;
    private javax.swing.JScrollPane cartScrollPane;
    private javax.swing.JTable cartTable;
    private javax.swing.JPanel leftPanel;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel productPanel;
    private javax.swing.JScrollPane productScrollPane;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JPanel transactionPanel;
    // End of variables declaration//GEN-END:variables
}
