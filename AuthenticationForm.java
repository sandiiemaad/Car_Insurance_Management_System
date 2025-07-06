import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import javax.swing.table.DefaultTableModel;
import java.time.format.DateTimeParseException;

class Investigator {
    private int employeeId;
    private LocalDate bdDate;
    private String email;
    private int age;
    private String firstName;
    private String lastName;
    private int activeCases;

    public Investigator(int employeeId, LocalDate bdDate, String email, int age, String firstName, String lastName, int activeCases) {
        this.employeeId = employeeId;
        this.bdDate = bdDate;
        this.email = email;
        this.age = age;
        this.firstName = firstName;
        this.lastName = lastName;
        this.activeCases = activeCases;
    }

    public boolean saveToDatabase() {
        String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=Carproject;encrypt=true;trustServerCertificate=true;";
        String user = "SANDII";
        String password = "sandy321";

        try (Connection connection = DriverManager.getConnection(connectionUrl, user, password)) {
            String checkEmployeeQuery = "SELECT COUNT(*) FROM EMPLOYEE WHERE EMPLOYEE_ID = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkEmployeeQuery)) {
                checkStmt.setInt(1, employeeId);
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                int count = rs.getInt(1);

                if (count == 0) {
                    String insertEmployeeQuery = "INSERT INTO EMPLOYEE (EMPLOYEE_ID, BD_DATE, EMAIL, AGE, FIRST_NAME, LAST_NAME) VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement insertEmpStmt = connection.prepareStatement(insertEmployeeQuery)) {
                        insertEmpStmt.setInt(1, employeeId);
                        insertEmpStmt.setDate(2, Date.valueOf(bdDate));
                        insertEmpStmt.setString(3, email);
                        insertEmpStmt.setInt(4, age);
                        insertEmpStmt.setString(5, firstName);
                        insertEmpStmt.setString(6, lastName);
                        insertEmpStmt.executeUpdate();
                    }
                }
            }

            String insertInvestigatorQuery = "INSERT INTO INVESTIGATOR (EMPLOYEE_ID, BD_DATE, EMAIL, AGE, FIRST_NAME, LAST_NAME, ACTIVE_CASES) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(insertInvestigatorQuery)) {
                ps.setInt(1, employeeId);
                ps.setDate(2, Date.valueOf(bdDate));
                ps.setString(3, email);
                ps.setInt(4, age);
                ps.setString(5, firstName);
                ps.setString(6, lastName);
                ps.setInt(7, activeCases);
                int rowsInserted = ps.executeUpdate();
                return rowsInserted > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}



public class AuthenticationForm extends JFrame {
    private JTextField investigatorIdField;
    private JButton loginButton, signupButton, exitButton;

    public AuthenticationForm() {
        setTitle("Investigator Authentication");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Color navy = new Color(0, 0, 50);
        Color darkButton = new Color(25, 25, 112);
        Color textWhite = Color.WHITE;

// Title label
        JLabel titleLabel = new JLabel("Car Insurance Management System");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(textWhite);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

// Header description as JTextArea
        String descriptionText = "The Car Insurance Company System is a application designed to help insurance investigators manage the operations of a car insurance company. " +
                "The system allows investigators to sign in securely and perform various tasks such as adding, updating, or deleting customer and car records, managing accident reports, and tracking customer information. It supports linking multiple cars to customers and recording accident details for each vehicle. Additionally, " +
                "the system provides tools to generate monthly accident reports, helping the company monitor cases and improve service efficiency. ";

        JTextArea header = new JTextArea(descriptionText);
        header.setLineWrap(true);
        header.setWrapStyleWord(true);
        header.setEditable(false);
        header.setOpaque(false);
        header.setForeground(textWhite);
        header.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));

        // Header panel with vertical layout
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(navy);

        // Add components with space between
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 10))); // ← space between title and description
        headerPanel.add(header);

        // Add to frame
        add(headerPanel, BorderLayout.NORTH);


        // Form panel
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBackground(navy);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel label = new JLabel("Investigator ID:");
        label.setForeground(textWhite);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(label);

        investigatorIdField = new JTextField();
        panel.add(investigatorIdField);

        panel.add(new JLabel());
        panel.add(new JLabel());

        // Buttons
        loginButton = createStyledButton("Login", darkButton, textWhite);
        signupButton = createStyledButton("Sign Up", darkButton, textWhite);
        exitButton = createStyledButton("Exit", darkButton, textWhite);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(navy);
        buttonPanel.add(loginButton);
        buttonPanel.add(signupButton);
        buttonPanel.add(exitButton);

        add(panel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Actions
        loginButton.addActionListener(e -> login());
        signupButton.addActionListener(e -> {
            dispose();
            new InvestigatorSignup();
        });
        exitButton.addActionListener(e -> System.exit(0));

        setVisible(true);
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

    private void login() {
        String investigatorId = investigatorIdField.getText().trim();

        if (investigatorId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Investigator ID", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DriverManager.getConnection(
                "jdbc:sqlserver://localhost:1433;databaseName=Carproject;encrypt=true;trustServerCertificate=true;",
                "SANDII", "sandy321");
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Investigator WHERE EMPLOYEE_ID = ?")) {

            stmt.setString(1, investigatorId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                new MainMenu();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Investigator ID", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}


 class InvestigatorSignup extends JFrame {
    private JTextField idField, bdDateField, emailField, ageField, firstNameField, lastNameField, activeCasesField;
    private JButton signupButton, exitButton;
    private JTable investigatorTable;
    private DefaultTableModel tableModel;

    public InvestigatorSignup() {
        setTitle("Signup Investigator");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        Color navy = new Color(0, 0, 50);
        Color textWhite = Color.WHITE;
        Color darkButton = new Color(25, 25, 112);
        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);

        // Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(9, 2, 8, 8));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        inputPanel.setBackground(navy);

        // Fields
        idField = new JTextField(); idField.setFont(fieldFont);
        bdDateField = new JTextField(); bdDateField.setFont(fieldFont);
        emailField = new JTextField(); emailField.setFont(fieldFont);
        ageField = new JTextField(); ageField.setFont(fieldFont);
        firstNameField = new JTextField(); firstNameField.setFont(fieldFont);
        lastNameField = new JTextField(); lastNameField.setFont(fieldFont);
        activeCasesField = new JTextField(); activeCasesField.setFont(fieldFont);

        signupButton = createStyledButton("Signup", darkButton, textWhite);
        exitButton = createStyledButton("Exit", darkButton, textWhite);

        // Labels
        inputPanel.add(createLabel("Employee ID:", labelFont, textWhite));
        inputPanel.add(idField);
        inputPanel.add(createLabel("Birth Date (YYYY-MM-DD):", labelFont, textWhite));
        inputPanel.add(bdDateField);
        inputPanel.add(createLabel("Email:", labelFont, textWhite));
        inputPanel.add(emailField);
        inputPanel.add(createLabel("Age:", labelFont, textWhite));
        inputPanel.add(ageField);
        inputPanel.add(createLabel("First Name:", labelFont, textWhite));
        inputPanel.add(firstNameField);
        inputPanel.add(createLabel("Last Name:", labelFont, textWhite));
        inputPanel.add(lastNameField);
        inputPanel.add(createLabel("Active Cases:", labelFont, textWhite));
        inputPanel.add(activeCasesField);
        inputPanel.add(signupButton);
        inputPanel.add(exitButton);

        add(inputPanel, BorderLayout.WEST);

        // Table
        String[] columns = {"Employee ID", "Birth Date", "Email", "Age", "First Name", "Last Name", "Active Cases"};
        tableModel = new DefaultTableModel(columns, 0);
        investigatorTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(investigatorTable);
        add(tableScrollPane, BorderLayout.CENTER);

        // Actions
        signupButton.addActionListener(e -> signupInvestigator());
        exitButton.addActionListener(e -> {
            dispose();
            new AuthenticationForm();
        });

        loadInvestigatorData();
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

    private void signupInvestigator() {
        try {
            int id = Integer.parseInt(idField.getText().trim());
            LocalDate bdDate = LocalDate.parse(bdDateField.getText().trim());
            String email = emailField.getText().trim();
            int age = Integer.parseInt(ageField.getText().trim());
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            int activeCases = Integer.parseInt(activeCasesField.getText().trim());

            Investigator inv = new Investigator(id, bdDate, email, age, firstName, lastName, activeCases);
            if (inv.saveToDatabase()) {
                JOptionPane.showMessageDialog(this, "Investigator signed up successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadInvestigatorData();
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this, "Error saving to database.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Numeric fields must be numbers.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadInvestigatorData() {
        tableModel.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(
                "jdbc:sqlserver://localhost:1433;databaseName=Carproject;encrypt=true;trustServerCertificate=true;",
                "SANDII", "sandy321");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Investigator")) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("EMPLOYEE_ID"),
                        rs.getDate("BD_DATE"),
                        rs.getString("EMAIL"),
                        rs.getInt("AGE"),
                        rs.getString("FIRST_NAME"),
                        rs.getString("LAST_NAME"),
                        rs.getInt("ACTIVE_CASES")
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load investigators.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        idField.setText("");
        bdDateField.setText("");
        emailField.setText("");
        ageField.setText("");
        firstNameField.setText("");
        lastNameField.setText("");
        activeCasesField.setText("");
    }
}