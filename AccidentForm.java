import java.sql.*;
import java.time.LocalDate;
import javax.swing.*;
import java.awt.*;

class Accident {
    public int accidentId;
    public String licencePlate;
    public String carId;
    public LocalDate date;
    public String location;

    public Accident(int accidentId, String licencePlate, String carId, LocalDate date, String location) {
        this.accidentId = accidentId;
        this.licencePlate = licencePlate;
        this.carId = carId;
        this.date = date;
        this.location = location;
    }

    public boolean saveToDatabase() {
        String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=Carproject;encrypt=true;trustServerCertificate=true;";
        String user = "SANDII";
        String password = "sandy321";

        String insertQuery = "INSERT INTO ACCIDENT (ACCIDENT_ID, LICENCE_PLATE, CAR_ID, DATE, LOCATION) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(connectionUrl, user, password);
             PreparedStatement ps = conn.prepareStatement(insertQuery)) {

            ps.setInt(1, accidentId);
            ps.setString(2, licencePlate.trim());
            ps.setString(3, carId.trim());
            ps.setDate(4, Date.valueOf(date));
            ps.setString(5, location.trim());

            int rowsInserted = ps.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateInDatabase() {
        String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=Carproject;encrypt=true;trustServerCertificate=true;";
        String user = "SANDII";
        String password = "sandy321";

        String updateQuery = "UPDATE ACCIDENT SET LICENCE_PLATE = ?, CAR_ID = ?, DATE = ?, LOCATION = ? WHERE ACCIDENT_ID = ?";

        try (Connection conn = DriverManager.getConnection(connectionUrl, user, password);
             PreparedStatement ps = conn.prepareStatement(updateQuery)) {

            ps.setString(1, licencePlate);
            ps.setString(2, carId);
            ps.setDate(3, Date.valueOf(date));
            ps.setString(4, location);
            ps.setInt(5, accidentId);

            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Accident getAccidentById(int id) {
        String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=Carproject;encrypt=true;trustServerCertificate=true;";
        String user = "SANDII";
        String password = "sandy321";

        String selectQuery = "SELECT * FROM ACCIDENT WHERE ACCIDENT_ID = ?";

        try (Connection conn = DriverManager.getConnection(connectionUrl, user, password);
             PreparedStatement ps = conn.prepareStatement(selectQuery)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Accident(
                        rs.getInt("ACCIDENT_ID"),
                        rs.getString("LICENCE_PLATE").trim(),
                        rs.getString("CAR_ID").trim(),
                        rs.getDate("DATE").toLocalDate(),
                        rs.getString("LOCATION").trim()
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}


public class AccidentForm extends JFrame {
    private JTextField idField, plateField, carIdField, dateField, locationField;
    private JButton addButton, updateButton, viewButton, exitButton;

    public AccidentForm() {
        setTitle("Accident Management");
        setSize(450, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Color navy = new Color(0, 0, 50);
        Color textWhite = Color.WHITE;
        Color buttonColor = new Color(25, 25, 112);
        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBackground(navy);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        idField = new JTextField();
        plateField = new JTextField();
        carIdField = new JTextField();
        dateField = new JTextField();
        locationField = new JTextField();

        formPanel.add(createLabel("Accident ID:", labelFont, textWhite));
        formPanel.add(idField);
        formPanel.add(createLabel("Licence Plate:", labelFont, textWhite));
        formPanel.add(plateField);
        formPanel.add(createLabel("Car ID:", labelFont, textWhite));
        formPanel.add(carIdField);
        formPanel.add(createLabel("Date (YYYY-MM-DD):", labelFont, textWhite));
        formPanel.add(dateField);
        formPanel.add(createLabel("Location:", labelFont, textWhite));
        formPanel.add(locationField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(navy);

        addButton = createStyledButton("Add Accident", buttonColor, textWhite);
        updateButton = createStyledButton("Update Accident", buttonColor, textWhite);
        viewButton = createStyledButton("View Accident", buttonColor, textWhite);
        exitButton = createStyledButton("Exit", buttonColor, textWhite);

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(exitButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String plate = plateField.getText().trim();
                String carId = carIdField.getText().trim();
                LocalDate date = LocalDate.parse(dateField.getText().trim());
                String location = locationField.getText().trim();

                Accident accident = new Accident(id, plate, carId, date, location);
                boolean success = accident.saveToDatabase();

                JOptionPane.showMessageDialog(this,
                        success ? "Accident added successfully!" : "Failed to add accident.",
                        success ? "Success" : "Error",
                        success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        updateButton.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String plate = plateField.getText().trim();
                String carId = carIdField.getText().trim();
                LocalDate date = LocalDate.parse(dateField.getText().trim());
                String location = locationField.getText().trim();

                Accident accident = new Accident(id, plate, carId, date, location);
                boolean success = accident.updateInDatabase();

                JOptionPane.showMessageDialog(this,
                        success ? "Accident updated successfully!" : "Update failed.",
                        success ? "Success" : "Error",
                        success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        viewButton.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                Accident acc = Accident.getAccidentById(id);

                if (acc != null) {
                    plateField.setText(acc.licencePlate);
                    carIdField.setText(acc.carId);
                    dateField.setText(acc.date.toString());
                    locationField.setText(acc.location);
                } else {
                    JOptionPane.showMessageDialog(this, "This accident ID was not found in the database.",
                            "Not Found", JOptionPane.WARNING_MESSAGE);
                    plateField.setText("");
                    carIdField.setText("");
                    dateField.setText("");
                    locationField.setText("");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid ID format. Please enter a valid number.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        exitButton.addActionListener(e -> {
            dispose();
            new MainMenu();
        });

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
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return button;
    }
}
