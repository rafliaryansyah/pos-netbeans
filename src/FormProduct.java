import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.AbstractCellEditor;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import javax.swing.JTable;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author rafli
 */
public class FormProduct extends javax.swing.JFrame {

    private boolean isUpdateMode = false;
    private int currentProductId = -1;
    
    // Image handling variables
    private String currentImagePath = null;
    private final String UPLOAD_DIRECTORY = "uploads/products/";
    
    // Callback interface for Dashboard refresh
    private Runnable dashboardRefreshCallback;

    /**
     * Creates new form FormPegawai
     */
    public FormProduct() {
        initComponents();
        initImageComponents();
        getProducts();
    }
    
    /**
     * Set callback to refresh Dashboard when products are modified
     */
    public void setDashboardRefreshCallback(Runnable callback) {
        this.dashboardRefreshCallback = callback;
    }
    
    private void clearDialogFields() {
        // Clear semua text field dalam dialog
        jTextField1.setText("");
        jTextField2.setText("");
        jTextField3.setText("");
        jTextField4.setText("");
        jTextField5.setText("");
        
        // Clear image
        currentImagePath = null;
        updateImagePreview(null);

        // Reset dialog label untuk create mode
        jLabel6.setText("Tambah Produk");

        // Set focus ke field pertama
        jTextField1.requestFocus();
    }
    
    private void getProducts() {
        try {
            Connection conn = DatabaseConnection.connect();
            String sql = "SELECT id, name, price, category, unit, stock, image_path FROM products WHERE deleted_at IS NULL";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("ID");
            model.addColumn("Nama");
            model.addColumn("Harga");
            model.addColumn("Kategori");
            model.addColumn("Unit");
            model.addColumn("Stok");
            model.addColumn("Action");
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("price"),
                    rs.getString("category"),
                    rs.getString("unit"),
                    rs.getString("stock"),
                    "Action" // Placeholder for action buttons
                });
            }
            jTable1.setModel(model);
            
            // Set custom renderer and editor for Action column
            int actionColumnIndex = 6; // Action column is at index 6
            jTable1.getColumn("Action").setCellRenderer(new ActionRenderer());
            jTable1.getColumn("Action").setCellEditor(new ActionEditor());
            jTable1.getColumn("Action").setPreferredWidth(150);
            
            // Set row height to accommodate buttons
            jTable1.setRowHeight(35);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    // Custom Cell Renderer untuk Action Buttons
    class ActionRenderer extends JPanel implements TableCellRenderer {
        private JButton updateButton;
        private JButton deleteButton;
        
        public ActionRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
            updateButton = new JButton("Update");
            deleteButton = new JButton("Delete");
            
            updateButton.setPreferredSize(new java.awt.Dimension(70, 25));
            deleteButton.setPreferredSize(new java.awt.Dimension(70, 25));
            
            add(updateButton);
            add(deleteButton);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }
    
    // Custom Cell Editor untuk Action Buttons
    class ActionEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel panel;
        private JButton updateButton;
        private JButton deleteButton;
        private int currentRow;
        
        public ActionEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            updateButton = new JButton("Update");
            deleteButton = new JButton("Delete");
            
            updateButton.setPreferredSize(new java.awt.Dimension(70, 25));
            deleteButton.setPreferredSize(new java.awt.Dimension(70, 25));
            
            updateButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleUpdateAction(currentRow);
                    fireEditingStopped();
                }
            });
            
            deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleDeleteAction(currentRow);
                    fireEditingStopped();
                }
            });
            
            panel.add(updateButton);
            panel.add(deleteButton);
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, 
                boolean isSelected, int row, int column) {
            currentRow = row;
            return panel;
        }
        
        @Override
        public Object getCellEditorValue() {
            return null;
        }
    }
    
    // Handle Update Action
    private void handleUpdateAction(int row) {
        // Get product data from selected row
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        currentProductId = Integer.parseInt(model.getValueAt(row, 0).toString());
        String name = model.getValueAt(row, 1).toString();
        String price = model.getValueAt(row, 2).toString();
        String category = model.getValueAt(row, 3).toString();
        String unit = model.getValueAt(row, 4).toString();
        String stock = model.getValueAt(row, 5).toString();
        
        // Set update mode
        isUpdateMode = true;
        
        // Populate dialog fields
        jTextField1.setText(name);
        jTextField2.setText(price);
        jTextField3.setText(category);
        jTextField4.setText(unit);
        jTextField5.setText(stock);
        
        // Load product image
        loadProductImage(currentProductId);
        
        // Update dialog label
        jLabel6.setText("Update Produk");
        
        // Setup and show dialog
        jDialog1.setModal(true);
        jDialog1.setTitle("Update Product");
        jDialog1.setLocationRelativeTo(this);
        jDialog1.setSize(450, 750); // Increased size for image components
        jDialog1.setVisible(true);
    }
    
    private void loadProductImage(int productId) {
        try {
            Connection conn = DatabaseConnection.connect();
            String sql = "SELECT image_path FROM products WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String imagePath = rs.getString("image_path");
                if (imagePath != null && !imagePath.isEmpty()) {
                    File imageFile = new File(imagePath);
                    if (imageFile.exists()) {
                        currentImagePath = imagePath;
                        updateImagePreview(imagePath);
                    } else {
                        // Image file not found, clear preview
                        currentImagePath = null;
                        updateImagePreview(null);
                    }
                } else {
                    currentImagePath = null;
                    updateImagePreview(null);
                }
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Gagal memuat gambar produk: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Handle Delete Action
    private void handleDeleteAction(int row) {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        int productId = Integer.parseInt(model.getValueAt(row, 0).toString());
        String productName = model.getValueAt(row, 1).toString();
        
        // Show confirmation dialog
        int option = JOptionPane.showConfirmDialog(
            this, 
            "Apakah Anda yakin ingin menghapus produk '" + productName + "'?\n\nData yang dihapus dapat dipulihkan kembali.", 
            "Konfirmasi Hapus", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        // If user confirmed deletion
        if (option == JOptionPane.YES_OPTION) {
            softDeleteProduct(productId, productName);
        }
    }
    
    // Soft Delete Product
    private void softDeleteProduct(int productId, String productName) {
        try {
            Connection conn = DatabaseConnection.connect();
            
            // Soft delete - set deleted_at timestamp instead of actually deleting
            String sql = "UPDATE products SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, productId);
            
            int rowsAffected = stmt.executeUpdate();
            conn.close();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, 
                    "Produk '" + productName + "' berhasil dihapus!\n\nCatatan: Data masih dapat dipulihkan dari database.", 
                    "Hapus Berhasil", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Refresh the product table to show changes
                getProducts();
                
                // Call dashboard refresh callback
                if (dashboardRefreshCallback != null) {
                    dashboardRefreshCallback.run();
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Gagal menghapus produk '" + productName + "'!", 
                    "Hapus Gagal", 
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error menghapus produk: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
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

        jDialog1 = new javax.swing.JDialog();
        jPanel2 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        
        // Initialize image components
        imagePreviewLabel = new javax.swing.JLabel();
        browseImageButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();

        jLabel6.setFont(new java.awt.Font("Helvetica", 0, 18)); // NOI18N
        jLabel6.setText("Tambah Produk");

        jLabel1.setText("Nama Produk");

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jTextField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField2ActionPerformed(evt);
            }
        });
        jTextField2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextField2KeyTyped(evt);
            }
        });

        jLabel2.setText("Harga Produk");

        jTextField3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField3ActionPerformed(evt);
            }
        });

        jLabel3.setText("Kategori");

        jTextField4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField4ActionPerformed(evt);
            }
        });

        jLabel4.setText("Satuan");

        jTextField5.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextField5KeyTyped(evt);
            }
        });

        jLabel7.setText("Stok");

        jLabel8.setText("Gambar Produk");

        jButton2.setFont(new java.awt.Font("Helvetica Neue", 0, 18)); // NOI18N
        jButton2.setText("Save");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        browseImageButton.setText("Browse Image");
        browseImageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseImageButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton2)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel7)
                        .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 366, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel8)
                        .addComponent(imagePreviewLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(browseImageButton, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel4)
                        .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 366, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel3)
                        .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 366, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel2)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 366, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel1)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 366, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(37, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel6)
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(imagePreviewLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(browseImageButton, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton2)
                .addContainerGap(22, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(jDialog1.getContentPane());
        jDialog1.getContentPane().setLayout(jDialog1Layout);
        jDialog1Layout.setHorizontalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jDialog1Layout.setVerticalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jLabel5.setFont(new java.awt.Font("Helvetica", 0, 24)); // NOI18N
        jLabel5.setText("Data Produk");

        jButton1.setFont(new java.awt.Font("Helvetica Neue", 0, 18)); // NOI18N
        jButton1.setText("Buat Produk");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 928, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 593, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 1, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Button `Buat Produk`
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        
        // Reset to create mode
        isUpdateMode = false;
        currentProductId = -1;
        
        jDialog1.setModal(true);
    
        // 2. Set title untuk dialog
        jDialog1.setTitle("Create New Product");

        // 3. Set posisi dialog di tengah parent window
        jDialog1.setLocationRelativeTo(this);

        // 4. Clear/reset semua field dalam dialog (opsional)
        clearDialogFields();

        // 5. Set ukuran dialog jika diperlukan
        jDialog1.setSize(450, 750); // Updated size for image components

        // 6. Tampilkan dialog
        jDialog1.setVisible(true);
    }//GEN-LAST:event_jButton1ActionPerformed

    // Button Save 
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        // Get Value from All Input
        // Get Value from All Input
        String name = jTextField1.getText().trim();
        String price = jTextField2.getText().trim();
        String category = jTextField3.getText().trim();
        String unit = jTextField4.getText().trim();
        String stock = jTextField5.getText().trim();

        // Validation all Input
        // 1. Check if any field is empty
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama produk tidak boleh kosong!", "Validasi Error", JOptionPane.ERROR_MESSAGE);
            jTextField1.requestFocus();
            return;
        }

        if (price.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Harga tidak boleh kosong!", "Validasi Error", JOptionPane.ERROR_MESSAGE);
            jTextField2.requestFocus();
            return;
        }

        if (category.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Kategori tidak boleh kosong!", "Validasi Error", JOptionPane.ERROR_MESSAGE);
            jTextField3.requestFocus();
            return;
        }

        if (unit.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Unit tidak boleh kosong!", "Validasi Error", JOptionPane.ERROR_MESSAGE);
            jTextField4.requestFocus();
            return;
        }

        if (stock.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Stok tidak boleh kosong!", "Validasi Error", JOptionPane.ERROR_MESSAGE);
            jTextField5.requestFocus();
            return;
        }

        // 2. Validate data types
        int priceValue;
        int stockValue;

        try {
            priceValue = Integer.parseInt(price);
            if (priceValue <= 0) {
                JOptionPane.showMessageDialog(this, "Harga harus berupa angka positif!", "Validasi Error", JOptionPane.ERROR_MESSAGE);
                jTextField2.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Harga harus berupa angka yang valid!", "Validasi Error", JOptionPane.ERROR_MESSAGE);
            jTextField2.requestFocus();
            return;
        }

        try {
            stockValue = Integer.parseInt(stock);
            if (stockValue < 0) {
                JOptionPane.showMessageDialog(this, "Stok tidak boleh negatif!", "Validasi Error", JOptionPane.ERROR_MESSAGE);
                jTextField5.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Stok harus berupa angka yang valid!", "Validasi Error", JOptionPane.ERROR_MESSAGE);
            jTextField5.requestFocus();
            return;
        }

        // 3. Validate string length (optional)
        if (name.length() > 255) {
            JOptionPane.showMessageDialog(this, "Nama produk terlalu panjang (maksimal 255 karakter)!", "Validasi Error", JOptionPane.ERROR_MESSAGE);
            jTextField1.requestFocus();
            return;
        }

        if (category.length() > 255) {
            JOptionPane.showMessageDialog(this, "Kategori terlalu panjang (maksimal 255 karakter)!", "Validasi Error", JOptionPane.ERROR_MESSAGE);
            jTextField3.requestFocus();
            return;
        }

        if (unit.length() > 255) {
            JOptionPane.showMessageDialog(this, "Unit terlalu panjang (maksimal 255 karakter)!", "Validasi Error", JOptionPane.ERROR_MESSAGE);
            jTextField4.requestFocus();
            return;
        }

        // Insert to database
        try (Connection conn = DatabaseConnection.connect()) {
            String sql;
            PreparedStatement stmt;
            String savedImagePath = null;
            
            // Save image file if there's a new image selected
            if (currentImagePath != null && !currentImagePath.isEmpty()) {
                // If it's a new file (not already in uploads directory), save it
                if (!currentImagePath.startsWith(UPLOAD_DIRECTORY)) {
                    savedImagePath = saveImageFile(currentImagePath);
                } else {
                    // It's already a saved file (edit mode), keep the existing path
                    savedImagePath = currentImagePath;
                }
            }
            
            if (isUpdateMode) {
                // Update existing product
                sql = "UPDATE products SET name = ?, price = ?, category = ?, unit = ?, stock = ?, image_path = ? WHERE id = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, name);
                stmt.setInt(2, priceValue);
                stmt.setString(3, category);
                stmt.setString(4, unit);
                stmt.setInt(5, stockValue);
                stmt.setString(6, savedImagePath);
                stmt.setInt(7, currentProductId);
            } else {
                // Insert new product
                sql = "INSERT INTO products (name, price, category, unit, stock, image_path) VALUES (?, ?, ?, ?, ?, ?)";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, name);
                stmt.setInt(2, priceValue);
                stmt.setString(3, category);
                stmt.setString(4, unit);
                stmt.setInt(5, stockValue);
                stmt.setString(6, savedImagePath);
            }

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                String successMessage = isUpdateMode ? "Data produk berhasil diupdate!" : "Data produk berhasil disimpan!";
                JOptionPane.showMessageDialog(this, successMessage, "Sukses", JOptionPane.INFORMATION_MESSAGE);

                // Clear all input fields
                jTextField1.setText("");
                jTextField2.setText("");
                jTextField3.setText("");
                jTextField4.setText("");
                jTextField5.setText("");
                
                // Clear image
                currentImagePath = null;
                updateImagePreview(null);

                // Reset mode
                isUpdateMode = false;
                currentProductId = -1;

                // Close dialog
                jDialog1.setVisible(false);

                // Optional: Refresh data display if you have a method to show products
                getProducts();

                // Optional: Set focus back to first field
                jTextField1.requestFocus();

                // Call dashboard refresh callback
                if (dashboardRefreshCallback != null) {
                    dashboardRefreshCallback.run();
                }
            } else {
                String errorMessage = isUpdateMode ? "Gagal mengupdate data produk!" : "Gagal menyimpan data produk!";
                JOptionPane.showMessageDialog(this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            String errorMessage = isUpdateMode ? "Gagal mengupdate data: " + e.getMessage() : "Gagal menyimpan data: " + e.getMessage();
            JOptionPane.showMessageDialog(this, errorMessage, "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        
        // Validation all Input, is empty? is valid with string? number? max min
        // Input to database with response toast message, and return to list products
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jTextField3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField3ActionPerformed

    private void jTextField4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField4ActionPerformed

    // Input `Harga Produk` Dialog
    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_jTextField2ActionPerformed

    // Input `Harga Produk` Dialog KeyTyped
    private void jTextField2KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField2KeyTyped
        // TODO add your handling code here:
        char c = evt.getKeyChar();
        if (!Character.isDigit(c)) {
            evt.consume();
        }
    }//GEN-LAST:event_jTextField2KeyTyped

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jTextField5KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField5KeyTyped
        // TODO add your handling code here:
        char c = evt.getKeyChar();
        if (!Character.isDigit(c)) {
            evt.consume();
        }
    }//GEN-LAST:event_jTextField5KeyTyped

    private void browseImageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseImageButtonActionPerformed
        // TODO add your handling code here:
        browseImage();
    }//GEN-LAST:event_browseImageButtonActionPerformed

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
            java.util.logging.Logger.getLogger(FormProduct.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FormProduct.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FormProduct.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FormProduct.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FormProduct().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JLabel imagePreviewLabel;
    private javax.swing.JButton browseImageButton;
    // End of variables declaration//GEN-END:variables

    // Image handling methods
    private void initImageComponents() {
        // Configure image preview (components are already initialized in initComponents)
        imagePreviewLabel.setHorizontalAlignment(JLabel.CENTER);
        imagePreviewLabel.setText("No Image");
        imagePreviewLabel.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.GRAY));
        imagePreviewLabel.setPreferredSize(new java.awt.Dimension(120, 120));
        
        // Set initial state
        updateImagePreview(null);
    }
    
    private void browseImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Pilih Gambar Produk");
        
        // Filter untuk hanya JPG dan PNG
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Image Files (*.jpg, *.jpeg, *.png)", "jpg", "jpeg", "png");
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // Validate file size (max 5MB)
            if (selectedFile.length() > 5 * 1024 * 1024) {
                JOptionPane.showMessageDialog(this, 
                    "Ukuran file terlalu besar! Maksimal 5MB.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Update preview
            updateImagePreview(selectedFile.getAbsolutePath());
            currentImagePath = selectedFile.getAbsolutePath();
        }
    }
    
    private void updateImagePreview(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            imagePreviewLabel.setIcon(null);
            imagePreviewLabel.setText("No Image");
        } else {
            try {
                ImageIcon originalIcon = new ImageIcon(imagePath);
                Image originalImage = originalIcon.getImage();
                
                // Resize untuk preview (120x120)
                Image resizedImage = originalImage.getScaledInstance(120, 120, Image.SCALE_SMOOTH);
                ImageIcon resizedIcon = new ImageIcon(resizedImage);
                
                imagePreviewLabel.setIcon(resizedIcon);
                imagePreviewLabel.setText("");
            } catch (Exception e) {
                imagePreviewLabel.setIcon(null);
                imagePreviewLabel.setText("Invalid Image");
                JOptionPane.showMessageDialog(this, 
                    "Gagal memuat gambar: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private String saveImageFile(String sourceImagePath) {
        if (sourceImagePath == null || sourceImagePath.isEmpty()) {
            return null;
        }
        
        try {
            // Create upload directory if not exists
            Path uploadDir = Paths.get(UPLOAD_DIRECTORY);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            // Generate unique filename
            String originalFileName = Paths.get(sourceImagePath).getFileName().toString();
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.'));
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            
            // Copy file to upload directory
            Path sourcePath = Paths.get(sourceImagePath);
            Path targetPath = uploadDir.resolve(uniqueFileName);
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            return UPLOAD_DIRECTORY + uniqueFileName;
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Gagal menyimpan file gambar: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}
