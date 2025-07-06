import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class CarForm extends JFrame {
    private JTextField plateField, idField, yearField, modelField, valueField;
    private JButton saveButton, exitButton, editButton, deleteButton;
    private JTable carTable;
    private DefaultTableModel tableModel;

    public CarForm() {
        setTitle("Add Car");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Color navy = new Color(0, 0, 50);
        Color textWhite = Color.WHITE;
        Color darkButton = new Color(25, 25, 112);
        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);

        JPanel formPanel = new JPanel(new GridLayout(7, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        formPanel.setBackground(navy);

        plateField = new JTextField(); plateField.setFont(fieldFont);
        idField = new JTextField(); idField.setFont(fieldFont);
        yearField = new JTextField(); yearField.setFont(fieldFont);
        modelField = new JTextField(); modelField.setFont(fieldFont);
        valueField = new JTextField(); valueField.setFont(fieldFont);

        saveButton = createStyledButton("Save", darkButton, textWhite);
        exitButton = createStyledButton("Back", darkButton, textWhite);
        editButton = createStyledButton("Edit", darkButton, textWhite);
        deleteButton = createStyledButton("Delete", darkButton, textWhite);

        formPanel.add(createLabel("License Plate:", labelFont, textWhite));
        formPanel.add(plateField);
        formPanel.add(createLabel("Car ID:", labelFont, textWhite));
        formPanel.add(idField);
        formPanel.add(createLabel("Year:", labelFont, textWhite));
        formPanel.add(yearField);
        formPanel.add(createLabel("Model:", labelFont, textWhite));
        formPanel.add(modelField);
        formPanel.add(createLabel("Current Value:", labelFont, textWhite));
        formPanel.add(valueField);
        formPanel.add(saveButton);
        formPanel.add(exitButton);
        formPanel.add(editButton);
        formPanel.add(deleteButton);

        setLayout(new BorderLayout(10, 10));
        add(formPanel, BorderLayout.WEST);

        String[] columns = {"License Plate", "Car ID", "Year", "Model", "Current Value"};
        tableModel = new DefaultTableModel(columns, 0);
        carTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(carTable);
        add(scrollPane, BorderLayout.CENTER);

        saveButton.addActionListener(e -> insertCar());
        editButton.addActionListener(e -> updateCar());
        deleteButton.addActionListener(e -> deleteCar());
        exitButton.addActionListener(e -> {
            dispose();
            new MainMenu();
        });

        carTable.getSelectionModel().addListSelectionListener(event -> {
            int selectedRow = carTable.getSelectedRow();
            if (selectedRow >= 0) {
                plateField.setText(tableModel.getValueAt(selectedRow, 0).toString());
                idField.setText(tableModel.getValueAt(selectedRow, 1).toString());
                yearField.setText(tableModel.getValueAt(selectedRow, 2).toString());
                modelField.setText(tableModel.getValueAt(selectedRow, 3).toString());
                valueField.setText(tableModel.getValueAt(selectedRow, 4).toString());
                plateField.setEditable(false);
                idField.setEditable(false);
            }
        });

        loadCarData();
        setVisible(true);
    }

    private JLabel createLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text);
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        return button;
    }

    private boolean customerExists(int customerId) {
        String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=Carproject;encrypt=true;trustServerCertificate=true;";
        String user = "SANDII";
        String password = "sandy321";

        String query = "SELECT COUNT(*) FROM CUSTOMER WHERE CUSTOMER_ID = ?";
        try (Connection conn = DriverManager.getConnection(connectionUrl, user, password);
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void insertCar() {
        String plate = plateField.getText().trim();
        String id = idField.getText().trim();
        String yearText = yearField.getText().trim();
        String model = modelField.getText().trim();
        String value = valueField.getText().trim();

        if (plate.isEmpty() || id.isEmpty() || yearText.isEmpty() || model.isEmpty() || value.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String customerIdText = JOptionPane.showInputDialog(this, "Enter Customer ID (integer):");
        if (customerIdText == null || customerIdText.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Customer ID is required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int year = Integer.parseInt(yearText);
            int customerId = Integer.parseInt(customerIdText.trim());

            String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=Carproject;encrypt=true;trustServerCertificate=true;";
            String user = "SANDII";
            String password = "sandy321";

            try (Connection conn = DriverManager.getConnection(connectionUrl, user, password)) {
                conn.setAutoCommit(false);

                if (!customerExists(customerId)) {
                    CustomerForm customerForm = new CustomerForm();
                    customerForm.setVisible(true);
                }

                String insertCarSql = "INSERT INTO CAR (LICENCE_PLATE, CAR_ID, YEAR, CAR_MODEL, CURRENT_VALUE) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement psCar = conn.prepareStatement(insertCarSql)) {
                    psCar.setString(1, plate);
                    psCar.setString(2, id);
                    psCar.setInt(3, year);
                    psCar.setString(4, model);
                    psCar.setString(5, value);
                    psCar.executeUpdate();
                }

                String insertOwnsSql = "INSERT INTO OWNS (CUSTOMER_ID, LICENCE_PLATE, CAR_ID) VALUES (?, ?, ?)";
                try (PreparedStatement psOwns = conn.prepareStatement(insertOwnsSql)) {
                    psOwns.setInt(1, customerId);
                    psOwns.setString(2, plate);
                    psOwns.setString(3, id);
                    psOwns.executeUpdate();
                }

                conn.commit();
                JOptionPane.showMessageDialog(this, "Car and Ownership saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
                loadCarData();

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Year and Customer ID must be numbers.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCar() {
        int selectedRow = carTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a car to update.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String plate = plateField.getText().trim();
        String id = idField.getText().trim();
        String yearText = yearField.getText().trim();
        String model = modelField.getText().trim();
        String value = valueField.getText().trim();

        if (plate.isEmpty() || id.isEmpty() || yearText.isEmpty() || model.isEmpty() || value.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int year = Integer.parseInt(yearText);

            String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=Carproject;encrypt=true;trustServerCertificate=true;";
            String user = "SANDII";
            String password = "sandy321";

            String updateQuery = "UPDATE CAR SET YEAR=?, CAR_MODEL=?, CURRENT_VALUE=? WHERE LICENCE_PLATE=? AND CAR_ID=?";

            try (Connection conn = DriverManager.getConnection(connectionUrl, user, password);
                 PreparedStatement ps = conn.prepareStatement(updateQuery)) {

                ps.setInt(1, year);
                ps.setString(2, model);
                ps.setString(3, value);
                ps.setString(4, plate);
                ps.setString(5, id);

                int rowsUpdated = ps.executeUpdate();
                if (rowsUpdated > 0) {
                    JOptionPane.showMessageDialog(this, "Car updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadCarData();
                    clearFields();
                } else {
                    JOptionPane.showMessageDialog(this, "Update failed.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Year must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error occurred.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCar() {
        int selectedRow = carTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a car to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String plate = tableModel.getValueAt(selectedRow, 0).toString();
        String id = tableModel.getValueAt(selectedRow, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this car?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=Carproject;encrypt=true;trustServerCertificate=true;";
        String user = "SANDII";
        String password = "sandy321";

        try (Connection conn = DriverManager.getConnection(connectionUrl, user, password)) {
            conn.setAutoCommit(false);

            try {
                String deleteAccidentSql = "DELETE FROM ACCIDENT WHERE CAR_ID = ?";
                try (PreparedStatement psAccident = conn.prepareStatement(deleteAccidentSql)) {
                    psAccident.setString(1, id);
                    psAccident.executeUpdate();
                }

                String deleteOwnsSql = "DELETE FROM OWNS WHERE LICENCE_PLATE = ? AND CAR_ID = ?";
                try (PreparedStatement psOwns = conn.prepareStatement(deleteOwnsSql)) {
                    psOwns.setString(1, plate);
                    psOwns.setString(2, id);
                    psOwns.executeUpdate();
                }

                String deleteCarSql = "DELETE FROM CAR WHERE LICENCE_PLATE = ? AND CAR_ID = ?";
                try (PreparedStatement psCar = conn.prepareStatement(deleteCarSql)) {
                    psCar.setString(1, plate);
                    psCar.setString(2, id);
                    int rowsDeleted = psCar.executeUpdate();

                    if (rowsDeleted > 0) {
                        conn.commit();
                        JOptionPane.showMessageDialog(this, "Car and related data deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadCarData();
                        clearFields();
                    } else {
                        conn.rollback();
                        JOptionPane.showMessageDialog(this, "Delete failed. Car not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCarData() {
        tableModel.setRowCount(0);
        String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=Carproject;encrypt=true;trustServerCertificate=true;";
        String user = "SANDII";
        String password = "sandy321";

        try (Connection conn = DriverManager.getConnection(connectionUrl, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM CAR")) {

            while (rs.next()) {
                String plate = rs.getString("LICENCE_PLATE");
                String id = rs.getString("CAR_ID");
                int year = rs.getInt("YEAR");
                String model = rs.getString("CAR_MODEL");
                String value = rs.getString("CURRENT_VALUE");

                tableModel.addRow(new Object[]{plate, id, year, model, value});
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load car data.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        plateField.setText("");
        idField.setText("");
        yearField.setText("");
        modelField.setText("");
        valueField.setText("");
        plateField.setEditable(true);
        idField.setEditable(true);
    }
}
