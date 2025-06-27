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
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import java.util.regex.Pattern;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author rafli
 */
public class FormUser extends javax.swing.JFrame {

    private boolean isUpdateMode = false;
    private int currentUserId = -1;
    private String currentUserRole = "User"; // Default role for current session
    
    // Email validation pattern
    private static final String EMAIL_PATTERN = 
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    /**
     * Creates new form FormUser
     */
    public FormUser() {
        this("Admin"); // Default constructor assumes Admin role for testing
    }
    
    public FormUser(String userRole) {
        this.currentUserRole = userRole;
        initComponents();
        setupRoleComboBox();
        checkAdminAccess();
        getUsers();
    }
    
    private void setupRoleComboBox() {
        // Setup role combo box in dialog
        String[] roles = {"User", "Admin"};
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(roles);
        jComboBox1.setModel(model);
    }
    
    private void checkAdminAccess() {
        // Only Admin can perform CRUD operations
        if (!"Admin".equals(currentUserRole)) {
            jButton1.setEnabled(false); // Disable "Buat User" button
            JOptionPane.showMessageDialog(this, 
                "Akses ditolak! Hanya Admin yang dapat mengelola data user.", 
                "Akses Terbatas", 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void clearDialogFields() {
        // Clear semua text field dalam dialog
        jTextField1.setText("");
        jTextField2.setText("");
        jTextField3.setText("");
        jTextField4.setText("");
        jComboBox1.setSelectedIndex(0); // Reset to "User"

        // Reset dialog label untuk create mode
        jLabel6.setText("Tambah User");

        // Set focus ke field pertama
        jTextField1.requestFocus();
    }
    
    private void getUsers() {
        try {
            Connection conn = DatabaseConnection.connect();
            String sql = "SELECT id, full_name, role, email, created_at FROM users ORDER BY created_at DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("ID");
            model.addColumn("Full Name");
            model.addColumn("Role");
            model.addColumn("Email");
            model.addColumn("Created At");
            model.addColumn("Action");
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("id"),
                    rs.getString("full_name"),
                    rs.getString("role"),
                    rs.getString("email"),
                    rs.getTimestamp("created_at"),
                    "Action" // Placeholder for action buttons
                });
            }
            jTable1.setModel(model);
            
            // Set custom renderer and editor for Action column only if Admin
            int actionColumnIndex = 5; // Action column is at index 5
            if ("Admin".equals(currentUserRole)) {
                jTable1.getColumn("Action").setCellRenderer(new ActionRenderer());
                jTable1.getColumn("Action").setCellEditor(new ActionEditor());
            }
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
                    if ("Admin".equals(currentUserRole)) {
                        handleUpdateAction(currentRow);
                    }
                    fireEditingStopped();
                }
            });
            
            deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if ("Admin".equals(currentUserRole)) {
                        handleDeleteAction(currentRow);
                    }
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
        // Get user data from selected row
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        currentUserId = Integer.parseInt(model.getValueAt(row, 0).toString());
        String fullName = model.getValueAt(row, 1).toString();
        String role = model.getValueAt(row, 2).toString();
        String email = model.getValueAt(row, 3).toString();
        
        // Set update mode
        isUpdateMode = true;
        
        // Populate dialog fields
        jTextField1.setText(fullName);
        jTextField2.setText(email);
        jComboBox1.setSelectedItem(role);
        jTextField3.setText(""); // Clear password field for security
        jTextField4.setText(""); // Clear confirm password field
        
        // Update dialog label
        jLabel6.setText("Update User");
        
        // Setup and show dialog
        jDialog1.setModal(true);
        jDialog1.setTitle("Update User");
        jDialog1.setLocationRelativeTo(this);
        jDialog1.setSize(400, 520);
        jDialog1.setVisible(true);
    }
    
    // Handle Delete Action
    private void handleDeleteAction(int row) {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        int userId = Integer.parseInt(model.getValueAt(row, 0).toString());
        String userName = model.getValueAt(row, 1).toString();
        
        // Show confirmation dialog
        int option = JOptionPane.showConfirmDialog(
            this, 
            "Apakah Anda yakin ingin menghapus user '" + userName + "'?\n\nData yang dihapus tidak dapat dipulihkan kembali.", 
            "Konfirmasi Hapus", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        // If user confirmed deletion
        if (option == JOptionPane.YES_OPTION) {
            deleteUser(userId, userName);
        }
    }
    
    // Delete User (Hard delete)
    private void deleteUser(int userId, String userName) {
        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "DELETE FROM users WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, 
                    "User '" + userName + "' berhasil dihapus!", 
                    "Sukses", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Refresh data display
                getUsers();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Gagal menghapus user '" + userName + "'!", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Gagal menghapus data: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Terjadi kesalahan: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private boolean isValidEmail(String email) {
        return pattern.matcher(email).matches();
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
        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JPasswordField();
        jLabel4 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JPasswordField();
        jLabel7 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();

        jLabel6.setFont(new java.awt.Font("Helvetica", 0, 18)); // NOI18N
        jLabel6.setText("Tambah User");

        jLabel1.setText("Full Name");

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

        jLabel2.setText("Email");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "User", "Admin" }));

        jLabel3.setText("Role");

        jTextField3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField3ActionPerformed(evt);
            }
        });

        jLabel4.setText("Password");

        jLabel7.setText("Confirm Password");

        jButton2.setFont(new java.awt.Font("Helvetica Neue", 0, 18)); // NOI18N
        jButton2.setText("Save");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
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
                        .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 366, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel4)
                        .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 366, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel3)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 366, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
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
        jLabel5.setText("Data User");

        jButton1.setFont(new java.awt.Font("Helvetica Neue", 0, 18)); // NOI18N
        jButton1.setText("Buat User");
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

    // Button `Buat User`
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // Check admin access
        if (!"Admin".equals(currentUserRole)) {
            JOptionPane.showMessageDialog(this, 
                "Akses ditolak! Hanya Admin yang dapat membuat user baru.", 
                "Akses Terbatas", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Reset to create mode
        isUpdateMode = false;
        currentUserId = -1;
        
        jDialog1.setModal(true);
    
        // 2. Set title untuk dialog
        jDialog1.setTitle("Create New User");

        // 3. Set posisi dialog di tengah parent window
        jDialog1.setLocationRelativeTo(this);

        // 4. Clear/reset semua field dalam dialog (opsional)
        clearDialogFields();

        // 5. Set ukuran dialog jika diperlukan
        jDialog1.setSize(400, 520); // sesuaikan dengan kebutuhan

        // 6. Tampilkan dialog
        jDialog1.setVisible(true);
    }//GEN-LAST:event_jButton1ActionPerformed

    // Button Save 
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // Check admin access
        if (!"Admin".equals(currentUserRole)) {
            JOptionPane.showMessageDialog(this, 
                "Akses ditolak! Hanya Admin yang dapat menyimpan data user.", 
                "Akses Terbatas", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get Value from All Input
        String fullName = jTextField1.getText().trim();
        String email = jTextField2.getText().trim();
        String role = (String) jComboBox1.getSelectedItem();
        String password = new String(((javax.swing.JPasswordField)jTextField3).getPassword());
        String confirmPassword = new String(((javax.swing.JPasswordField)jTextField4).getPassword());

        // Validation all Input
        // 1. Check if any field is empty
        if (fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full Name tidak boleh kosong!", "Validasi Error", JOptionPane.ERROR_MESSAGE);
            jTextField1.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email tidak boleh kosong!", "Validasi Error", JOptionPane.ERROR_MESSAGE);
            jTextField2.requestFocus();
            return;
        }

        // 2. Validate email format
        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Format email tidak valid!", "Validasi Error", JOptionPane.ERROR_MESSAGE);
            jTextField2.requestFocus();
            return;
        }

        // 3. Password validation (only for create mode or when password is provided in update mode)
        if (!isUpdateMode || !password.isEmpty()) {
            if (password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Password tidak boleh kosong!", "Validasi Error", JOptionPane.ERROR_MESSAGE);
                jTextField3.requestFocus();
                return;
            }

            if (password.length() < 6) {
                JOptionPane.showMessageDialog(this, "Password minimal 6 karakter!", "Validasi Error", JOptionPane.ERROR_MESSAGE);
                jTextField3.requestFocus();
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Password dan konfirmasi password tidak sama!", "Validasi Error", JOptionPane.ERROR_MESSAGE);
                jTextField4.requestFocus();
                return;
            }
        }

        // 4. Validate string length
        if (fullName.length() > 255) {
            JOptionPane.showMessageDialog(this, "Full Name terlalu panjang (maksimal 255 karakter)!", "Validasi Error", JOptionPane.ERROR_MESSAGE);
            jTextField1.requestFocus();
            return;
        }

        if (email.length() > 255) {
            JOptionPane.showMessageDialog(this, "Email terlalu panjang (maksimal 255 karakter)!", "Validasi Error", JOptionPane.ERROR_MESSAGE);
            jTextField2.requestFocus();
            return;
        }

        // Insert/Update to database
        try (Connection conn = DatabaseConnection.connect()) {
            String sql;
            PreparedStatement stmt;
            
            if (isUpdateMode) {
                // Check if email already exists for other users
                String checkEmailSql = "SELECT id FROM users WHERE email = ? AND id != ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkEmailSql);
                checkStmt.setString(1, email);
                checkStmt.setInt(2, currentUserId);
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Email sudah digunakan oleh user lain!", "Validasi Error", JOptionPane.ERROR_MESSAGE);
                    jTextField2.requestFocus();
                    return;
                }
                
                // Update existing user
                if (password.isEmpty()) {
                    // Update without password
                    sql = "UPDATE users SET full_name = ?, email = ?, role = ? WHERE id = ?";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, fullName);
                    stmt.setString(2, email);
                    stmt.setString(3, role);
                    stmt.setInt(4, currentUserId);
                } else {
                    // Update with password
                    sql = "UPDATE users SET full_name = ?, email = ?, role = ?, password = ? WHERE id = ?";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, fullName);
                    stmt.setString(2, email);
                    stmt.setString(3, role);
                    stmt.setString(4, HashUtil.hashPassword(password));
                    stmt.setInt(5, currentUserId);
                }
            } else {
                // Check if email already exists
                String checkEmailSql = "SELECT id FROM users WHERE email = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkEmailSql);
                checkStmt.setString(1, email);
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Email sudah digunakan!", "Validasi Error", JOptionPane.ERROR_MESSAGE);
                    jTextField2.requestFocus();
                    return;
                }
                
                // Insert new user
                sql = "INSERT INTO users (full_name, email, role, password) VALUES (?, ?, ?, ?)";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, fullName);
                stmt.setString(2, email);
                stmt.setString(3, role);
                stmt.setString(4, HashUtil.hashPassword(password));
            }

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                String successMessage = isUpdateMode ? "Data user berhasil diupdate!" : "Data user berhasil disimpan!";
                JOptionPane.showMessageDialog(this, successMessage, "Sukses", JOptionPane.INFORMATION_MESSAGE);

                // Clear all input fields
                jTextField1.setText("");
                jTextField2.setText("");
                jComboBox1.setSelectedIndex(0);
                jTextField3.setText("");
                jTextField4.setText("");

                // Reset mode
                isUpdateMode = false;
                currentUserId = -1;

                // Close dialog
                jDialog1.setVisible(false);

                // Refresh data display
                getUsers();

                // Set focus back to first field
                jTextField1.requestFocus();
            } else {
                String errorMessage = isUpdateMode ? "Gagal mengupdate data user!" : "Gagal menyimpan data user!";
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
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jTextField3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField3ActionPerformed

    // Input `Email` Dialog
    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_jTextField2ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

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
            java.util.logging.Logger.getLogger(FormUser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FormUser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FormUser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FormUser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FormUser("Admin").setVisible(true); // Pass Admin role for testing
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JPasswordField jTextField3;
    private javax.swing.JPasswordField jTextField4;
    // End of variables declaration//GEN-END:variables
}
