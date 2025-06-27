import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class FormTransaksi extends JFrame {
    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton, detailButton;
    private DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    
    public FormTransaksi() {
        initComponents();
        loadTransactions();
    }
    
    private void initComponents() {
        setTitle("Transaction History");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        
        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Create title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(59, 89, 182));
        JLabel titleLabel = new JLabel("Transaction History");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        
        // Create table
        tableModel = new DefaultTableModel();
        tableModel.addColumn("ID");
        tableModel.addColumn("Invoice");
        tableModel.addColumn("Customer");
        tableModel.addColumn("Payment Method");
        tableModel.addColumn("Subtotal");
        tableModel.addColumn("Tax");
        tableModel.addColumn("Total");
        tableModel.addColumn("Amount Paid");
        tableModel.addColumn("Change");
        tableModel.addColumn("Date");
        
        transactionTable = new JTable(tableModel);
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionTable.setRowHeight(25);
        
        // Set column widths
        transactionTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        transactionTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        transactionTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        transactionTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        transactionTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        transactionTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        transactionTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        transactionTable.getColumnModel().getColumn(7).setPreferredWidth(100);
        transactionTable.getColumnModel().getColumn(8).setPreferredWidth(80);
        transactionTable.getColumnModel().getColumn(9).setPreferredWidth(150);
        
        JScrollPane tableScrollPane = new JScrollPane(transactionTable);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        refreshButton = new JButton("Refresh");
        detailButton = new JButton("View Details");
        
        refreshButton.setPreferredSize(new Dimension(120, 35));
        detailButton.setPreferredSize(new Dimension(120, 35));
        
        refreshButton.setFont(new Font("Arial", Font.BOLD, 12));
        detailButton.setFont(new Font("Arial", Font.BOLD, 12));
        
        refreshButton.setBackground(new Color(70, 130, 180));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        
        detailButton.setBackground(new Color(34, 139, 34));
        detailButton.setForeground(Color.WHITE);
        detailButton.setFocusPainted(false);
        
        refreshButton.addActionListener(e -> loadTransactions());
        detailButton.addActionListener(e -> showTransactionDetails());
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(detailButton);
        
        // Add components to main panel
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void loadTransactions() {
        tableModel.setRowCount(0);
        
        try {
            Connection conn = DatabaseConnection.connect();
            String sql = "SELECT id, invoice, customer_name, payment_method, subtotal, tax_amount, total_amount, amount_paid, change_amount, created_at FROM transactions ORDER BY created_at DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("invoice").substring(0, 8) + "...",
                    rs.getString("customer_name"),
                    rs.getString("payment_method"),
                    "Rp " + currencyFormat.format(rs.getDouble("subtotal")),
                    "Rp " + currencyFormat.format(rs.getDouble("tax_amount")),
                    "Rp " + currencyFormat.format(rs.getDouble("total_amount")),
                    "Rp " + currencyFormat.format(rs.getDouble("amount_paid")),
                    "Rp " + currencyFormat.format(rs.getDouble("change_amount")),
                    dateFormat.format(rs.getTimestamp("created_at"))
                };
                tableModel.addRow(row);
            }
            
            conn.close();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading transactions: " + e.getMessage());
        }
    }
    
    private void showTransactionDetails() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a transaction to view details!");
            return;
        }
        
        int transactionId = (Integer) tableModel.getValueAt(selectedRow, 0);
        showDetailDialog(transactionId);
    }
    
    private void showDetailDialog(int transactionId) {
        JDialog detailDialog = new JDialog(this, "Transaction Details", true);
        detailDialog.setSize(600, 400);
        detailDialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Transaction info panel
        JPanel infoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Details table
        DefaultTableModel detailModel = new DefaultTableModel();
        detailModel.addColumn("Product Name");
        detailModel.addColumn("Price");
        detailModel.addColumn("Quantity");
        detailModel.addColumn("Subtotal");
        
        JTable detailTable = new JTable(detailModel);
        detailTable.setRowHeight(25);
        
        try {
            Connection conn = DatabaseConnection.connect();
            
            // Get transaction info
            String transactionSQL = "SELECT * FROM transactions WHERE id = ?";
            PreparedStatement transactionStmt = conn.prepareStatement(transactionSQL);
            transactionStmt.setInt(1, transactionId);
            ResultSet transactionRs = transactionStmt.executeQuery();
            
            if (transactionRs.next()) {
                gbc.gridx = 0; gbc.gridy = 0;
                infoPanel.add(new JLabel("Invoice:"), gbc);
                gbc.gridx = 1;
                infoPanel.add(new JLabel(transactionRs.getString("invoice")), gbc);
                
                gbc.gridx = 0; gbc.gridy = 1;
                infoPanel.add(new JLabel("Customer:"), gbc);
                gbc.gridx = 1;
                infoPanel.add(new JLabel(transactionRs.getString("customer_name")), gbc);
                
                gbc.gridx = 0; gbc.gridy = 2;
                infoPanel.add(new JLabel("Payment Method:"), gbc);
                gbc.gridx = 1;
                infoPanel.add(new JLabel(transactionRs.getString("payment_method")), gbc);
                
                gbc.gridx = 0; gbc.gridy = 3;
                infoPanel.add(new JLabel("Date:"), gbc);
                gbc.gridx = 1;
                infoPanel.add(new JLabel(dateFormat.format(transactionRs.getTimestamp("created_at"))), gbc);
                
                gbc.gridx = 0; gbc.gridy = 4;
                infoPanel.add(new JLabel("Total Amount:"), gbc);
                gbc.gridx = 1;
                JLabel totalLabel = new JLabel("Rp " + currencyFormat.format(transactionRs.getDouble("total_amount")));
                totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
                totalLabel.setForeground(new Color(220, 20, 60));
                infoPanel.add(totalLabel, gbc);
            }
            
            // Get transaction details
            String detailSQL = "SELECT * FROM transaction_details WHERE transaction_id = ?";
            PreparedStatement detailStmt = conn.prepareStatement(detailSQL);
            detailStmt.setInt(1, transactionId);
            ResultSet detailRs = detailStmt.executeQuery();
            
            while (detailRs.next()) {
                Object[] row = {
                    detailRs.getString("product_name"),
                    "Rp " + currencyFormat.format(detailRs.getDouble("price")),
                    detailRs.getInt("quantity"),
                    "Rp " + currencyFormat.format(detailRs.getDouble("subtotal"))
                };
                detailModel.addRow(row);
            }
            
            conn.close();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading transaction details: " + e.getMessage());
        }
        
        mainPanel.add(infoPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(detailTable), BorderLayout.CENTER);
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> detailDialog.dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        detailDialog.add(mainPanel);
        detailDialog.setVisible(true);
    }
} 