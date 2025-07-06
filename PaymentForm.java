import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;


public class PaymentForm extends JFrame {
    private JTextField paymentIdField, discountIdField, paymentMethodField, paymentDateField;
    private JButton saveButton, exitButton;

    public PaymentForm() {
        setTitle("Add New Payment");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Color navy = new Color(0, 0, 50);
        Color textWhite = Color.WHITE;
        Color buttonBlue = new Color(25, 25, 112);
        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);

        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        inputPanel.setBackground(navy);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        paymentIdField = new JTextField(); paymentIdField.setFont(fieldFont);
        discountIdField = new JTextField(); discountIdField.setFont(fieldFont);
        paymentMethodField = new JTextField(); paymentMethodField.setFont(fieldFont);
        paymentDateField = new JTextField(); paymentDateField.setFont(fieldFont);
        saveButton = createStyledButton("Save Payment", buttonBlue, textWhite);
        exitButton = createStyledButton("Exit", buttonBlue, textWhite);

        inputPanel.add(createLabel("Payment ID:", labelFont, textWhite));
        inputPanel.add(paymentIdField);
        inputPanel.add(createLabel("Discount ID:", labelFont, textWhite));
        inputPanel.add(discountIdField);
        inputPanel.add(createLabel("Payment Method:", labelFont, textWhite));
        inputPanel.add(paymentMethodField);
        inputPanel.add(createLabel("Payment Date (YYYY-MM-DD):", labelFont, textWhite));
        inputPanel.add(paymentDateField);
        inputPanel.add(saveButton);
        inputPanel.add(exitButton);

        add(inputPanel);

        saveButton.addActionListener(e -> {
            String paymentIdText = paymentIdField.getText().trim();
            String discountIdText = discountIdField.getText().trim();
            String paymentMethod = paymentMethodField.getText().trim();
            String paymentDateText = paymentDateField.getText().trim();

            if (paymentIdText.isEmpty() || discountIdText.isEmpty() ||
                    paymentMethod.isEmpty() || paymentDateText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int paymentId = Integer.parseInt(paymentIdText);
                int discountId = Integer.parseInt(discountIdText);
                LocalDate paymentDate = LocalDate.parse(paymentDateText);

                Payment payment = new Payment(paymentId, discountId, paymentMethod, paymentDate);
                boolean success = payment.saveToDatabase();

                if (success) {
                    JOptionPane.showMessageDialog(this, "Payment saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Error saving payment to database.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Payment ID and Discount ID must be numbers.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Payment Date must be in YYYY-MM-DD format.", "Error", JOptionPane.ERROR_MESSAGE);
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
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        return button;
    }
}

class Payment {
    private int paymentId;
    private int discountId;
    private String paymentMethod;
    private LocalDate paymentDate;

    public Payment(int paymentId, int discountId, String paymentMethod, LocalDate paymentDate) {
        this.paymentId = paymentId;
        this.discountId = discountId;
        this.paymentMethod = paymentMethod;
        this.paymentDate = paymentDate;
    }

    public boolean saveToDatabase() {
        String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=Carproject;encrypt=true;trustServerCertificate=true;";
        String user = "SANDII";
        String password = "sandy321";

        String insertQuery = "INSERT INTO PAYMENT (PAYMENT_ID, DISCOUNT_ID, PAYMENT_METHOD, PAYMENT_DATE) VALUES (?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(connectionUrl, user, password);
             PreparedStatement ps = connection.prepareStatement(insertQuery)) {

            ps.setInt(1, paymentId);
            ps.setInt(2, discountId);
            ps.setString(3, paymentMethod);
            ps.setDate(4, Date.valueOf(paymentDate));

            int rowsInserted = ps.executeUpdate();
            return rowsInserted > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
