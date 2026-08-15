package com.example.securitytest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Base64;
import java.util.Random;

public class VulnerablePaymentService {

    // 1. Hardcoded credentials
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "password123";

    // 2. Hardcoded database URL
    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/paymentdb";

    // 3. Hardcoded secret/token
    private static final String JWT_SECRET =
            "my-super-secret-jwt-key";

    // 4. SQL Injection
    public void findPayment(String paymentId) {

        try {

            Connection connection =
                    DriverManager.getConnection(
                            DB_URL,
                            USERNAME,
                            PASSWORD
                    );

            Statement statement =
                    connection.createStatement();

            String query =
                    "SELECT * FROM payments WHERE id = '"
                            + paymentId + "'";

            statement.executeQuery(query);

        } catch (Exception e) {

            // 5. Information disclosure
            e.printStackTrace();
        }
    }


    // 6. Command injection
    public void pingServer(String host)
            throws IOException {

        Runtime.getRuntime().exec(
                "ping " + host
        );
    }


    // 7. Path traversal
    public String getReceipt(String fileName) {

        File file =
                new File("/tmp/receipts/" + fileName);

        return file.getAbsolutePath();
    }


    // 8. Sensitive information logging
    public void processPayment(
            String cardNumber,
            String cvv) {

        System.out.println(
                "Processing payment: " +
                cardNumber +
                " CVV: " +
                cvv
        );
    }


    // 9. Weak random number generator
    public String generateOtp() {

        Random random = new Random();

        return String.valueOf(
                random.nextInt(999999)
        );
    }


    // 10. Base64 instead of encryption
    public String storeSecret(String secret) {

        return Base64.getEncoder()
                .encodeToString(
                        secret.getBytes()
                );
    }


    // 11. SSRF-style URL access
    public String fetchUrl(String userUrl)
            throws Exception {

        URL url = new URL(userUrl);

        return url.getContent().toString();
    }


    // 12. Insecure temporary file
    public void writePaymentData(String data)
            throws IOException {

        File file =
                new File("/tmp/payment.txt");

        FileWriter writer =
                new FileWriter(file);

        writer.write(data);

        writer.close();
    }


    // 13. Open redirect
    public String paymentRedirect(String redirectUrl) {

        return "redirect:" + redirectUrl;
    }


    // 14. Reflected XSS-style output
    public String paymentPage(String customerName) {

        return "<html>" +
                "<h1>Welcome " +
                customerName +
                "</h1>" +
                "</html>";
    }


    // 15. Insecure authorization
    public boolean authorize(String role) {

        return role != null &&
                !role.isEmpty();
    }


    // 16. Empty exception handling
    public void ignoreError() {

        try {

            String value = null;

            value.length();

        } catch (Exception e) {

            // Ignored
        }
    }


    // 17. Weak comparison logic for secrets
    public boolean validateToken(
            String token,
            String expectedToken) {

        return token.equals(expectedToken);
    }


    // 18. Sensitive data returned directly
    public String getDatabasePassword() {

        return PASSWORD;
    }


    // 19. Dangerous file deletion
    public boolean deleteFile(String fileName) {

        File file =
                new File("/tmp/" + fileName);

        return file.delete();
    }


    // 20. User-controlled URL redirect
    public String redirectToBank(String bankUrl) {

        return bankUrl;
    }


    // 21. Potential resource leak
    public void resourceLeak(String fileName)
            throws IOException {

        java.io.FileInputStream input =
                new java.io.FileInputStream(fileName);

        System.out.println(input.read());

        // Stream intentionally not closed
    }


    // 22. Sensitive data in exception
    public void throwSensitiveException() {

        throw new RuntimeException(
                "Database password is: " + PASSWORD
        );
    }
}
