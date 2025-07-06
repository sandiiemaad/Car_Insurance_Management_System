import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

class Report {
    private int reportId;
    private LocalDate generationDate;
    private String brief;
    private LocalDate approvalDate;

    public Report(int reportId, LocalDate generationDate, String brief, LocalDate approvalDate) {
        this.reportId = reportId;
        this.generationDate = generationDate;
        this.brief = brief;
        this.approvalDate = approvalDate;
    }

    public boolean saveToDatabase() {
        String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=Carproject;encrypt=true;trustServerCertificate=true;";
        String user = "SANDII";
        String password = "sandy321";

        String insertQuery = "INSERT INTO REPORT (REPORT_ID, GENERATION_DATE, BRIEF, APPROVAL_DATE) VALUES (?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(connectionUrl, user, password);
             PreparedStatement ps = connection.prepareStatement(insertQuery)) {

            ps.setInt(1, reportId);
            ps.setDate(2, Date.valueOf(generationDate));
            ps.setString(3, brief);
            ps.setDate(4, Date.valueOf(approvalDate));

            int rowsInserted = ps.executeUpdate();
            return rowsInserted > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ArrayList<String[]> getMonthlyReports(int month) {
        ArrayList<String[]> reports = new ArrayList<>();
        String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=Carproject;encrypt=true;trustServerCertificate=true;";
        String user = "SANDII";
        String password = "sandy321";

        String query = "SELECT " +
                "R.REPORT_ID, R.GENERATION_DATE, R.BRIEF, R.APPROVAL_DATE, " +
                "CU.CUSTOMER_ID, CU.FIRST_NAME, CU.LAST_NAME, " +
                "C.LICENCE_PLATE, C.CAR_MODEL, " +
                "A.ACCIDENT_ID, A.[DATE] AS ACCIDENT_DATE, A.LOCATION " +
                "FROM REPORT R " +
                "JOIN CUSTOMER CU ON R.REPORT_ID = CU.REPORT_ID " +
                "JOIN OWNS O ON CU.CUSTOMER_ID = O.CUSTOMER_ID " +
                "JOIN CAR C ON O.CAR_ID = C.CAR_ID AND O.LICENCE_PLATE = C.LICENCE_PLATE " +
                "LEFT JOIN ACCIDENT A ON C.CAR_ID = A.CAR_ID AND C.LICENCE_PLATE = A.LICENCE_PLATE " +
                "WHERE MONTH(A.DATE) = ?";

        try (Connection conn = DriverManager.getConnection(connectionUrl, user, password);
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, month);
            System.out.println("Executing query for month: " + month);
            ResultSet rs = ps.executeQuery();

            boolean hasRows = false;
            while (rs.next()) {
                hasRows = true;
                String[] row = {
                        String.valueOf(rs.getInt("REPORT_ID")),
                        rs.getDate("GENERATION_DATE").toString(),
                        rs.getString("BRIEF"),
                        rs.getDate("APPROVAL_DATE").toString(),
                        String.valueOf(rs.getInt("CUSTOMER_ID")),
                        rs.getString("FIRST_NAME"),
                        rs.getString("LAST_NAME"),
                        rs.getString("LICENCE_PLATE"),
                        rs.getString("CAR_MODEL"),
                        rs.getString("ACCIDENT_ID") != null ? rs.getString("ACCIDENT_ID") : "N/A",
                        rs.getDate("ACCIDENT_DATE") != null ? rs.getDate("ACCIDENT_DATE").toString() : "N/A",
                        rs.getString("LOCATION") != null ? rs.getString("LOCATION") : "N/A"
                };
                reports.add(row);
            }

            if (!hasRows) {
                System.out.println("No records found for month " + month);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reports;
    }
}

public class ReportForm extends JFrame {
    private JTextField idField, genDateField, briefField, approvalDateField;
    private JButton saveButton, exitButton, generateReportButton, specificQueriesButton;

    public ReportForm() {
        setTitle("Add New Report");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        // === Styling Colors ===
        Color background = new Color(10, 25, 49);
        Color foreground = Color.WHITE;
        Color buttonColor = new Color(30, 80, 150);

        // === Form Panel ===
        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        formPanel.setBackground(background);

        idField = new JTextField();
        genDateField = new JTextField();
        briefField = new JTextField();
        approvalDateField = new JTextField();

        saveButton = new JButton("Save Report");
        generateReportButton = new JButton("Generate Monthly Report");
        specificQueriesButton = new JButton("Specific Report Queries");
        exitButton = new JButton("Exit");

        JLabel[] labels = {
                new JLabel("Report ID:"),
                new JLabel("Generation Date (YYYY-MM-DD):"),
                new JLabel("Brief:"),
                new JLabel("Approval Date (YYYY-MM-DD):")
        };
        JTextField[] fields = { idField, genDateField, briefField, approvalDateField };

        for (int i = 0; i < labels.length; i++) {
            labels[i].setForeground(foreground);
            formPanel.add(labels[i]);
            formPanel.add(fields[i]);
        }

        JButton[] buttons = { saveButton, generateReportButton, specificQueriesButton, exitButton };
        for (JButton btn : buttons) {
            btn.setBackground(buttonColor);
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setFont(new Font("Arial", Font.BOLD, 13));
            btn.setPreferredSize(new Dimension(180, 35));
        }

        formPanel.add(saveButton);
        formPanel.add(generateReportButton);
        formPanel.add(specificQueriesButton);
        formPanel.add(exitButton);

        add(formPanel, BorderLayout.CENTER);
        getContentPane().setBackground(background);

        // === Actions ===
        saveButton.addActionListener(e -> {
            String idText = idField.getText().trim();
            String genDateText = genDateField.getText().trim();
            String brief = briefField.getText().trim();
            String approvalDateText = approvalDateField.getText().trim();

            if (idText.isEmpty() || genDateText.isEmpty() || brief.isEmpty() || approvalDateText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int reportId = Integer.parseInt(idText);
                LocalDate generationDate = LocalDate.parse(genDateText);
                LocalDate approvalDate = LocalDate.parse(approvalDateText);

                Report report = new Report(reportId, generationDate, brief, approvalDate);
                boolean success = report.saveToDatabase();

                if (success) {
                    JOptionPane.showMessageDialog(this, "Report saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Error saving report to database.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Report ID must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Dates must be in YYYY-MM-DD format.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        generateReportButton.addActionListener(e -> {
            String monthInput = JOptionPane.showInputDialog(this, "Enter month number (1-12):");

            try {
                int month = Integer.parseInt(monthInput);
                if (month < 1 || month > 12) throw new NumberFormatException();

                ArrayList<String[]> reports = Report.getMonthlyReports(month);
                if (reports.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No reports found for this month.", "Info", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JTextArea reportArea = new JTextArea();
                    reportArea.setEditable(false);
                    reportArea.setBackground(background);
                    reportArea.setForeground(foreground);
                    reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

                    StringBuilder sb = new StringBuilder();
                    for (String[] row : reports) {
                        sb.append("Report ID: ").append(row[0]).append("\n");
                        sb.append("Generation Date: ").append(row[1]).append("\n");
                        sb.append("Brief: ").append(row[2]).append("\n");
                        sb.append("Approval Date: ").append(row[3]).append("\n");
                        sb.append("Customer ID: ").append(row[4]).append("\n");
                        sb.append("Customer Name: ").append(row[5]).append(" ").append(row[6]).append("\n");
                        sb.append("Licence Plate: ").append(row[7]).append("\n");
                        sb.append("Car Model: ").append(row[8]).append("\n");
                        sb.append("Accident ID: ").append(row[9]).append("\n");
                        sb.append("Accident Date: ").append(row[10]).append("\n");
                        sb.append("Location: ").append(row[11]).append("\n");
                        sb.append("------------------------------------------------------------\n\n");
                    }

                    reportArea.setText(sb.toString());
                    JScrollPane scrollPane = new JScrollPane(reportArea);
                    scrollPane.setPreferredSize(new Dimension(500, 300));
                    JOptionPane.showMessageDialog(this, scrollPane, "Monthly Reports", JOptionPane.INFORMATION_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid month. Please enter a number between 1 and 12.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        specificQueriesButton.addActionListener(e -> {
            String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=Carproject;encrypt=true;trustServerCertificate=true;";
            String user = "SANDII";
            String password = "sandy321";

            String[] queries = {
                    "SELECT COUNT(DISTINCT CUST.Customer_ID) AS Total_Owners FROM Customer CUST JOIN OWNS O ON CUST.CUSTOMER_ID = O.CUSTOMER_ID JOIN CAR C ON O.CAR_ID = C.CAR_ID AND O.LICENCE_PLATE = C.LICENCE_PLATE JOIN ACCIDENT A ON C.CAR_ID = A.CAR_ID AND C.LICENCE_PLATE = A.LICENCE_PLATE WHERE YEAR(A.DATE) = 2017;",
                    "SELECT COUNT(*) AS Accident_Count FROM ACCIDENT A JOIN CAR C ON A.CAR_ID = C.CAR_ID AND A.LICENCE_PLATE = C.LICENCE_PLATE JOIN OWNS O ON C.CAR_ID = O.CAR_ID AND C.LICENCE_PLATE = O.LICENCE_PLATE JOIN CUSTOMER CUST ON O.CUSTOMER_ID = CUST.CUSTOMER_ID WHERE CUST.FIRST_NAME = 'Ahmed' AND CUST.LAST_NAME = 'Mohamed';",
                    "SELECT DISTINCT C.CAR_MODEL FROM CAR C WHERE C.CAR_MODEL NOT IN (SELECT C2.CAR_MODEL FROM CAR C2 JOIN ACCIDENT A ON C2.CAR_ID = A.CAR_ID AND C2.LICENCE_PLATE = A.LICENCE_PLATE WHERE YEAR(A.DATE) = 2017);",
                    "SELECT DISTINCT CUST.* FROM Customer CUST JOIN OWNS O ON CUST.CUSTOMER_ID = O.CUSTOMER_ID JOIN CAR C ON O.CAR_ID = C.CAR_ID AND O.LICENCE_PLATE = C.LICENCE_PLATE JOIN ACCIDENT A ON C.CAR_ID = A.CAR_ID AND C.LICENCE_PLATE = A.LICENCE_PLATE WHERE YEAR(A.DATE) = 2017;",
                    "SELECT COUNT(*) AS Accident_Count FROM ACCIDENT A JOIN CAR C ON A.CAR_ID = C.CAR_ID AND A.LICENCE_PLATE = C.LICENCE_PLATE WHERE C.CAR_MODEL = 'Toyota Corolla';"
            };

            String[] queryDescriptions = {
                    "1. Total number of people who owned cars involved in accidents in 2017:",
                    "2. Number of accidents involving cars owned by Ahmed Mohamed:",
                    "3. Car models NOT involved in accidents in 2017:",
                    "4. Customers who owned cars involved in accidents in 2017:",
                    "5. Number of accidents involving 'Toyota Corolla':"
            };

            StringBuilder results = new StringBuilder();

            try (Connection conn = DriverManager.getConnection(connectionUrl, user, password)) {
                for (int i = 0; i < queries.length; i++) {
                    results.append(queryDescriptions[i]).append("\n");

                    try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(queries[i])) {
                        ResultSetMetaData meta = rs.getMetaData();
                        int colCount = meta.getColumnCount();

                        while (rs.next()) {
                            for (int j = 1; j <= colCount; j++) {
                                results.append(meta.getColumnLabel(j)).append(": ").append(rs.getString(j)).append("  ");
                            }
                            results.append("\n");
                        }
                    }
                    results.append("\n------------------------------------------------------------\n\n");
                }

                JTextArea output = new JTextArea(results.toString());
                output.setEditable(false);
                output.setBackground(background);
                output.setForeground(foreground);
                JScrollPane scrollPane = new JScrollPane(output);
                scrollPane.setPreferredSize(new Dimension(500, 300));

                JOptionPane.showMessageDialog(this, scrollPane, "Specific Report Query Results", JOptionPane.INFORMATION_MESSAGE);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error running queries: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        exitButton.addActionListener(e -> {
            dispose();
            new MainMenu();
        });

        setVisible(true);
    }
}