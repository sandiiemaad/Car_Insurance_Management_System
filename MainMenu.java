import javax.swing.*;
import java.awt.*;

public class MainMenu extends JFrame {

    public MainMenu() {
        setTitle("Main Menu");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(0, 0, 50)); // navy blue
        setLocationRelativeTo(null); // center window

        // Header label
        JLabel header = new JLabel("<html><div style='text-align: center;'>"
                + "<h2 style='color: white;'>Welcome to the Car Insurance Management System</h2>"
                + "<p style='color: white;'>Please select an option to proceed</p></div></html>");
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setHorizontalAlignment(SwingConstants.CENTER);
        add(header, BorderLayout.NORTH);

        // Panel for buttons
        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        buttonPanel.setBackground(new Color(0, 0, 50));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));

        // Buttons with icons
        JButton carButton = createStyledButton("Car registration", "car.png");
        JButton customerButton = createStyledButton("Customer registration", "customer.png");
        JButton accidentButton = createStyledButton("Accident registration", "accident.png");
        JButton paymentButton = createStyledButton("Payment registration", "payment.png");
        JButton reportButton = createStyledButton("Report registration", "report.png");
        JButton discountButton = createStyledButton("Discount registration", "discount.png");

        // Add buttons to panel
        buttonPanel.add(carButton);
        buttonPanel.add(customerButton);
        buttonPanel.add(accidentButton);
        buttonPanel.add(paymentButton);
        buttonPanel.add(reportButton);
        buttonPanel.add(discountButton);

        // Add button panel to frame
        add(buttonPanel, BorderLayout.CENTER);

        // Action listeners
        carButton.addActionListener(e -> new CarForm());
        customerButton.addActionListener(e -> new CustomerForm());
        accidentButton.addActionListener(e -> new AccidentForm());
        paymentButton.addActionListener(e -> new PaymentForm());
        reportButton.addActionListener(e -> new ReportForm());
        discountButton.addActionListener(e -> new DiscountForm());

        setVisible(true);
    }

    // Method to create styled buttons with icons
    private JButton createStyledButton(String text, String iconFileName) {
        JButton button = new JButton(text);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(25, 25, 112)); // dark navy
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setFocusPainted(false);
        button.setIcon(loadIcon(iconFileName));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(20);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                BorderFactory.createEmptyBorder(10, 20, 10, 10)
        ));
        return button;
    }

    // Safe loading from resources folder
    private ImageIcon loadIcon(String fileName) {
        java.net.URL iconURL = getClass().getResource("/" + fileName);
        if (iconURL == null) {
            System.err.println("Icon not found: " + fileName);
            return new ImageIcon();
        }
        ImageIcon icon = new ImageIcon(iconURL);
        Image scaled = icon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AuthenticationForm::new); // Keep login screen
    }
}
