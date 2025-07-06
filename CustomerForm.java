import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

class Customer {
    private int customerId;
    private int paymentId;
    private int reportId;
    private String firstName;
    private String lastName;
    private Date dob;
    private int age;
    private String street;
    private String city;
    private int building;
    private ArrayList<String> phones;

    public Customer() {
    }

    public Customer(int customerId, int paymentId, int reportId, String firstName, String lastName, Date dob, int age, String street, String city, int building, ArrayList<String> phones) {
        this.customerId = customerId;
        this.paymentId = paymentId;
        this.reportId = reportId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.age = age;
        this.street = street;
        this.city = city;
        this.building = building;
        this.phones = phones;
    }

    public boolean saveToDatabase() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=Carproject;encrypt=true;trustServerCertificate=true;", "SANDII", "sandy321");
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO Customer (Customer_ID, Payment_ID, Report_ID, First_Name, Last_Name, Date_Of_Birth, Age, Street_Name, City_Name, Build_No) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

            stmt.setInt(1, customerId);
            stmt.setInt(2, paymentId);
            stmt.setInt(3, reportId);
            stmt.setString(4, firstName);
            stmt.setString(5, lastName);
            stmt.setDate(6, dob);
            stmt.setInt(7, age);
            stmt.setString(8, street);
            stmt.setString(9, city);
            stmt.setInt(10, building);

            int rowsAffected = stmt.executeUpdate();

            savePhonesToDatabase(conn);

            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void savePhonesToDatabase(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO Customer_Phone (Customer_ID, Phone) VALUES (?, ?)")) {
            for (String phone : phones) {
                stmt.setInt(1, customerId);
                stmt.setString(2, phone);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public boolean editCustomerInDatabase(int customerId, String newFirstName, String newLastName,
                                          Integer newAge, String newDateOfBirth,
                                          String newStreetName, String newCityName, String newBuildNo) {
        String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=Carproject;encrypt=true;trustServerCertificate=true;";
        String user = "SANDII";
        String password = "sandy321";

        try (Connection conn = DriverManager.getConnection(connectionUrl, user, password)) {

            // Fetch existing values
            String selectQuery = "SELECT First_Name, Last_Name, AGE, DATE_OF_BIRTH, STREET_NAME, CITY_NAME, BUILD_NO FROM Customer WHERE Customer_ID = ?";
            String existingFirstName = null, existingLastName = null;
            Integer existingAge = null;
            String existingDob = null, existingStreet = null, existingCity = null, existingBuildNo = null;

            try (PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {
                selectStmt.setInt(1, customerId);
                ResultSet rs = selectStmt.executeQuery();
                if (rs.next()) {
                    existingFirstName = rs.getString("First_Name");
                    existingLastName = rs.getString("Last_Name");
                    existingAge = rs.getInt("AGE");
                    existingDob = rs.getString("DATE_OF_BIRTH");
                    existingStreet = rs.getString("STREET_NAME");
                    existingCity = rs.getString("CITY_NAME");
                    existingBuildNo = rs.getString("BUILD_NO");
                } else {
                    return false; // Customer not found
                }
            }

            // Use existing values if inputs are null/empty
            if (newFirstName == null || newFirstName.trim().isEmpty()) newFirstName = existingFirstName;
            if (newLastName == null || newLastName.trim().isEmpty()) newLastName = existingLastName;
            if (newAge == null) newAge = existingAge;
            if (newDateOfBirth == null || newDateOfBirth.trim().isEmpty()) newDateOfBirth = existingDob;
            if (newStreetName == null || newStreetName.trim().isEmpty()) newStreetName = existingStreet;
            if (newCityName == null || newCityName.trim().isEmpty()) newCityName = existingCity;
            if (newBuildNo == null || newBuildNo.trim().isEmpty()) newBuildNo = existingBuildNo;

            // Perform the update
            String updateQuery = "UPDATE Customer SET First_Name = ?, Last_Name = ?, AGE = ?, DATE_OF_BIRTH = ?, STREET_NAME = ?, CITY_NAME = ?, BUILD_NO = ? WHERE Customer_ID = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                updateStmt.setString(1, newFirstName);
                updateStmt.setString(2, newLastName);
                updateStmt.setInt(3, newAge);
                updateStmt.setString(4, newDateOfBirth);
                updateStmt.setString(5, newStreetName);
                updateStmt.setString(6, newCityName);
                updateStmt.setString(7, newBuildNo);
                updateStmt.setInt(8, customerId);

                int rowsAffected = updateStmt.executeUpdate();
                return rowsAffected > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean deleteCustomerFromDatabase(int customerId) {
        String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=Carproject;encrypt=true;trustServerCertificate=true;";
        String user = "SANDII";
        String password = "sandy321";

        try (Connection conn = DriverManager.getConnection(connectionUrl, user, password)) {
            conn.setAutoCommit(false);

            try (
                    PreparedStatement stmt0 = conn.prepareStatement("DELETE FROM OWNS WHERE Customer_ID = ?");
                    PreparedStatement stmt1 = conn.prepareStatement("DELETE FROM Customer_Phone WHERE Customer_ID = ?");
                    PreparedStatement stmt2 = conn.prepareStatement("DELETE FROM Customer WHERE Customer_ID = ?");
            ) {

                stmt0.setInt(1, customerId);
                stmt0.executeUpdate();

                stmt1.setInt(1, customerId);
                stmt1.executeUpdate();

                stmt2.setInt(1, customerId);
                int rowsAffected = stmt2.executeUpdate();

                if (rowsAffected > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }

            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}


public class CustomerForm extends JFrame {
    private JTextField customerIdField, paymentIdField, reportIdField, firstNameField, lastNameField, dobField, ageField, streetField, cityField, buildField, phoneField;
    private JButton saveButton, addPhoneButton, exitButton, editButton, deleteButton;
    private DefaultListModel<String> phoneListModel;
    private JTable customerTable;
    private DefaultTableModel tableModel;

    public CustomerForm() {
        setTitle("Customer Management");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main panel colors
        Color navy = new Color(0, 0, 50);
        Color darkButton = new Color(25, 25, 112);
        Color textWhite = Color.WHITE;

        // Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(15, 2, 5, 5));
        inputPanel.setBackground(navy);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        customerIdField = new JTextField();
        paymentIdField = new JTextField();
        reportIdField = new JTextField();
        firstNameField = new JTextField();
        lastNameField = new JTextField();
        dobField = new JTextField();
        ageField = new JTextField();
        streetField = new JTextField();
        cityField = new JTextField();
        buildField = new JTextField();
        phoneField = new JTextField();
        phoneListModel = new DefaultListModel<>();
        JList<String> phoneList = new JList<>(phoneListModel);

        // Styled buttons
        addPhoneButton = createStyledButton("Add Phone", darkButton, textWhite);
        saveButton = createStyledButton("Save Customer", darkButton, textWhite);
        editButton = createStyledButton("Edit Customer", darkButton, textWhite);
        deleteButton = createStyledButton("Delete Customer", darkButton, textWhite);
        exitButton = createStyledButton("Exit", darkButton, textWhite);

        // Add labeled fields
        inputPanel.add(createLabel("Customer ID:", textWhite));
        inputPanel.add(customerIdField);
        inputPanel.add(createLabel("Payment ID:", textWhite));
        inputPanel.add(paymentIdField);
        inputPanel.add(createLabel("Report ID:", textWhite));
        inputPanel.add(reportIdField);
        inputPanel.add(createLabel("First Name:", textWhite));
        inputPanel.add(firstNameField);
        inputPanel.add(createLabel("Last Name:", textWhite));
        inputPanel.add(lastNameField);
        inputPanel.add(createLabel("Date of Birth (yyyy-mm-dd):", textWhite));
        inputPanel.add(dobField);
        inputPanel.add(createLabel("Age:", textWhite));
        inputPanel.add(ageField);
        inputPanel.add(createLabel("Street Name:", textWhite));
        inputPanel.add(streetField);
        inputPanel.add(createLabel("City Name:", textWhite));
        inputPanel.add(cityField);
        inputPanel.add(createLabel("Building Number:", textWhite));
        inputPanel.add(buildField);
        inputPanel.add(createLabel("Phone:", textWhite));
        inputPanel.add(phoneField);
        inputPanel.add(addPhoneButton);
        inputPanel.add(new JScrollPane(phoneList));
        inputPanel.add(saveButton);
        inputPanel.add(editButton);
        inputPanel.add(deleteButton);
        inputPanel.add(exitButton);

        // Table Panel
        String[] columns = {"Customer ID", "Payment ID", "Report ID", "First Name", "Last Name", "DOB", "Age", "Street", "City", "Building"};
        tableModel = new DefaultTableModel(columns, 0);
        customerTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(customerTable);

        // Layout
        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.WEST);
        add(tableScrollPane, BorderLayout.CENTER);

        // Actions
        addPhoneButton.addActionListener(e -> {
            String phone = phoneField.getText().trim();
            if (!phone.isEmpty()) {
                phoneListModel.addElement(phone);
                phoneField.setText("");
            }
        });

        saveButton.addActionListener(e -> saveCustomer());
        editButton.addActionListener(e -> editCustomer());
        deleteButton.addActionListener(e -> deleteCustomer());
        exitButton.addActionListener(e -> {
            dispose();
            new MainMenu();
        });

        // Load initial data
        loadCustomerData();

        setVisible(true);
    }

    private JLabel createLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
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

    private void saveCustomer() {
        try {
            int customerId = Integer.parseInt(customerIdField.getText().trim());
            int paymentId = Integer.parseInt(paymentIdField.getText().trim());
            int reportId = Integer.parseInt(reportIdField.getText().trim());
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            Date dob = Date.valueOf(dobField.getText().trim());
            int age = Integer.parseInt(ageField.getText().trim());
            String street = streetField.getText().trim();
            String city = cityField.getText().trim();
            int building = Integer.parseInt(buildField.getText().trim());

            ArrayList<String> phones = new ArrayList<>();
            for (int i = 0; i < phoneListModel.size(); i++) {
                phones.add(phoneListModel.getElementAt(i));
            }

            Customer customer = new Customer(customerId, paymentId, reportId, firstName, lastName, dob, age, street, city, building, phones);
            if (customer.saveToDatabase()) {
                JOptionPane.showMessageDialog(this, "Customer saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCustomerData();
            } else {
                JOptionPane.showMessageDialog(this, "Error saving customer.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editCustomer() {
        try {
            int selectedRow = customerTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "Please select a customer to edit.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int customerId = (int) tableModel.getValueAt(selectedRow, 0);
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String ageText = ageField.getText().trim();
            Integer age = (ageText.isEmpty()) ? null : Integer.parseInt(ageText);

            String dateOfBirth = dobField.getText().trim();
            String streetName = streetField.getText().trim();
            String cityName = cityField.getText().trim();
            String buildNo = buildField.getText().trim();


            Customer customer = new Customer();
            if (customer.editCustomerInDatabase(customerId, firstName, lastName, age, dateOfBirth, streetName, cityName, buildNo)) {
                JOptionPane.showMessageDialog(this, "Customer updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCustomerData();
            } else {
                JOptionPane.showMessageDialog(this, "Error updating customer.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCustomer() {
        try {
            int selectedRow = customerTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "Please select a customer to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int customerId = (int) tableModel.getValueAt(selectedRow, 0);

            Customer customer = new Customer();
            if (customer.deleteCustomerFromDatabase(customerId)) {
                JOptionPane.showMessageDialog(this, "Customer deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCustomerData();
            } else {
                JOptionPane.showMessageDialog(this, "Error deleting customer.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCustomerData() {
        tableModel.setRowCount(0);

        try (Connection conn = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=Carproject;encrypt=true;trustServerCertificate=true;", "SANDII", "sandy321");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Customer")) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("Customer_ID"),
                        rs.getInt("Payment_ID"),
                        rs.getInt("Report_ID"),
                        rs.getString("First_Name"),
                        rs.getString("Last_Name"),
                        rs.getDate("Date_Of_Birth"),
                        rs.getInt("Age"),
                        rs.getString("Street_Name"),
                        rs.getString("City_Name"),
                        rs.getInt("Build_No")
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load customers.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
