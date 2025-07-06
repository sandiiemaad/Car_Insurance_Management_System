import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

// === Discount Class ===
class Discount {
    private int discountId;
    private float amount;
    private java.sql.Date startDate;
    private java.sql.Date endDate;

    public Discount(int discountId, float amount, java.sql.Date startDate, java.sql.Date endDate) {
        this.discountId = discountId;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public boolean saveToDatabase() {
        String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=Carproject;encrypt=true;trustServerCertificate=true;";
        String user = "SANDII";
        String password = "sandy321";

        String insertQuery = "INSERT INTO DISCOUNT (DISCOUNT_ID, AMOUNT, START_DATE, END_DATE) VALUES (?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(connectionUrl, user, password);
             PreparedStatement ps = connection.prepareStatement(insertQuery)) {

            ps.setInt(1, discountId);
            ps.setFloat(2, amount);
            ps.setDate(3, startDate);
            ps.setDate(4, endDate);

            int rowsInserted = ps.executeUpdate();
            return rowsInserted > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}


public class DiscountForm extends JFrame {
    private JTextField discountIdField, amountField, startDateField, endDateField;
    private JButton saveButton, exitButton;
    private JTable discountTable;
    private DefaultTableModel tableModel;

    public DiscountForm() {
        setTitle("Discount Records");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Color navy = new Color(0, 0, 50);
        Color textWhite = Color.WHITE;
        Color buttonBlue = new Color(25, 25, 112);
        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);

        // === Input Panel ===
        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        inputPanel.setBackground(navy);

        discountIdField = new JTextField(); discountIdField.setFont(fieldFont);
        amountField = new JTextField(); amountField.setFont(fieldFont);
        startDateField = new JTextField(); startDateField.setFont(fieldFont);
        endDateField = new JTextField(); endDateField.setFont(fieldFont);
        saveButton = new JButton("Save Discount");
        exitButton = new JButton("Exit");

        saveButton.setBackground(buttonBlue);
        saveButton.setForeground(textWhite);
        saveButton.setFocusPainted(false);
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 13));

        exitButton.setBackground(buttonBlue);
        exitButton.setForeground(textWhite);
        exitButton.setFocusPainted(false);
        exitButton.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JLabel l1 = new JLabel("Discount ID:"); l1.setForeground(textWhite); l1.setFont(labelFont);
        JLabel l2 = new JLabel("Amount:"); l2.setForeground(textWhite); l2.setFont(labelFont);
        JLabel l3 = new JLabel("Start Date (yyyy-mm-dd):"); l3.setForeground(textWhite); l3.setFont(labelFont);
        JLabel l4 = new JLabel("End Date (yyyy-mm-dd):"); l4.setForeground(textWhite); l4.setFont(labelFont);

        inputPanel.add(l1);
        inputPanel.add(discountIdField);
        inputPanel.add(l2);
        inputPanel.add(amountField);
        inputPanel.add(l3);
        inputPanel.add(startDateField);
        inputPanel.add(l4);
        inputPanel.add(endDateField);
        inputPanel.add(saveButton);
        inputPanel.add(exitButton);

        // === Table Panel ===
        String[] columns = {"Discount ID", "Amount", "Start Date", "End Date"};
        tableModel = new DefaultTableModel(columns, 0);
        discountTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(discountTable);

        // === Layout ===
        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.WEST);
        add(tableScrollPane, BorderLayout.CENTER);

        // === Actions ===
        saveButton.addActionListener(e -> {
            try {
                int discountId = Integer.parseInt(discountIdField.getText().trim());
                float amount = Float.parseFloat(amountField.getText().trim());
                Date startDate = Date.valueOf(startDateField.getText().trim());
                Date endDate = Date.valueOf(endDateField.getText().trim());

                Discount discount = new Discount(discountId, amount, startDate, endDate);
                boolean success = discount.saveToDatabase();

                if (success) {
                    JOptionPane.showMessageDialog(this, "Discount record saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadDiscountData(); // Refresh the table
                } else {
                    JOptionPane.showMessageDialog(this, "Error saving discount record.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        exitButton.addActionListener(e -> System.exit(0));

        // === Load initial data ===
        loadDiscountData();

        setVisible(true);
    }

    private void loadDiscountData() {
        tableModel.setRowCount(0); // Clear table first

        String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=Carproject;encrypt=true;trustServerCertificate=true;";
        String user = "SANDII";
        String password = "sandy321";

        try (Connection conn = DriverManager.getConnection(connectionUrl, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM DISCOUNT")) {

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("DISCOUNT_ID"),
                        rs.getFloat("AMOUNT"),
                        rs.getDate("START_DATE"),
                        rs.getDate("END_DATE")
                };
                tableModel.addRow(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load discount records.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
