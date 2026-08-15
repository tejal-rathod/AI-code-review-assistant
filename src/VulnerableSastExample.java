package com.example.securitytest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Base64;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class VulnerableSastExample {

    // 1. Hardcoded password
    private static final String DB_PASSWORD = "admin123";

    // 2. Hardcoded API key
    private static final String API_KEY =
            "TEST_API_KEY_123456789";

    // 3. Hardcoded encryption key
    private static final String SECRET_KEY =
            "1234567890123456";

    // 4. Weak MD5 hashing
    public String weakHash(String value) throws Exception {

        MessageDigest md =
                MessageDigest.getInstance("MD5");

        byte[] hash =
                md.digest(value.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder()
                .encodeToString(hash);
    }

    // 5. Weak SHA-1 hashing
    public String sha1Hash(String value) throws Exception {

        MessageDigest md =
                MessageDigest.getInstance("SHA-1");

        return Base64.getEncoder()
                .encodeToString(
                        md.digest(value.getBytes())
                );
    }

    // 6. SQL Injection
    public void getUser(String username) {

        try {

            Connection connection =
                    DriverManager.getConnection(
                            "jdbc:mysql://localhost/test",
                            "root",
                            DB_PASSWORD
                    );

            Statement statement =
                    connection.createStatement();

            String query =
                    "SELECT * FROM users WHERE username = '"
                            + username + "'";

            ResultSet result =
                    statement.executeQuery(query);

            while (result.next()) {

                System.out.println(
                        result.getString("username")
                );
            }

        } catch (Exception e) {

            // 7. Information disclosure
            e.printStackTrace();
        }
    }

    // 8. Command Injection
    public void executeCommand(String command)
            throws IOException {

        Runtime.getRuntime().exec(command);
    }

    // 9. Dangerous ProcessBuilder usage
    public void executeProcess(String input)
            throws IOException {

        ProcessBuilder process =
                new ProcessBuilder("sh", "-c", input);

        process.start();
    }

    // 10. Path Traversal
    public String readFile(String filename)
            throws IOException {

        File file =
                new File("/tmp/" + filename);

        BufferedReader reader =
                new BufferedReader(
                        new FileReader(file)
                );

        String line =
                reader.readLine();

        reader.close();

        return line;
    }

    // 11. Unsafe deserialization
    public Object deserialize(String filePath)
            throws Exception {

        ObjectInputStream input =
                new ObjectInputStream(
                        new FileInputStream(filePath)
                );

        Object object =
                input.readObject();

        input.close();

        return object;
    }

    // 12. Weak random number generator
    public int generateToken() {

        Random random = new Random();

        return random.nextInt();
    }

    // 13. ECB encryption mode
    public String encrypt(String input)
            throws Exception {

        SecretKeySpec key =
                new SecretKeySpec(
                        SECRET_KEY.getBytes(),
                        "AES"
                );

        Cipher cipher =
                Cipher.getInstance("AES/ECB/PKCS5Padding");

        cipher.init(
                Cipher.ENCRYPT_MODE,
                key
        );

        return Base64.getEncoder()
                .encodeToString(
                        cipher.doFinal(
                                input.getBytes()
                        )
                );
    }

    // 14. Disabled TLS certificate validation pattern
    public boolean trustAllCertificates() {

        // Intentionally insecure placeholder
        return true;
    }

    // 15. Open redirect
    public String redirect(String url) {

        return "redirect:" + url;
    }

    // 16. Sensitive information in logs
    public void login(String username,
                      String password) {

        System.out.println(
                "Login user=" + username +
                " password=" + password
        );
    }

    // 17. Null/unsafe exception swallowing
    public void ignoreException() {

        try {

            int value = 10 / 0;

        } catch (Exception e) {

            // Exception ignored
        }
    }

    // 18. Use of predictable temporary file name
    public File createTempFile() {

        return new File(
                "/tmp/application-data.txt"
        );
    }

    // 19. XXE-prone XML parser configuration
    public void parseXml(String xml)
            throws Exception {

        javax.xml.parsers.DocumentBuilderFactory factory =
                javax.xml.parsers.DocumentBuilderFactory
                        .newInstance();

        javax.xml.parsers.DocumentBuilder builder =
                factory.newDocumentBuilder();

        builder.parse(
                new java.io.ByteArrayInputStream(
                        xml.getBytes()
                )
        );
    }

    // 20. LDAP Injection style string construction
    public String buildLdapQuery(String username) {

        return "(uid=" + username + ")";
    }

    // 21. Reflected XSS-style HTML generation
    public String generateHtml(String input) {

        return "<html><body>" +
                input +
                "</body></html>";
    }

    // 22. Insecure direct object reference style
    public String getUserFile(String userId)
            throws IOException {

        return new String(
                java.nio.file.Files.readAllBytes(
                        java.nio.file.Paths.get(
                                "/data/users/" +
                                userId +
                                "/private.txt"
                        )
                )
        );
    }

    // 23. Sensitive data encoded instead of encrypted
    public String encodePassword(String password) {

        return Base64.getEncoder()
                .encodeToString(
                        password.getBytes()
                );
    }

    // 24. Weak permissions assumption
    public boolean isAdmin(String role) {

        // Incorrect authorization logic
        return role != null;
    }

    public static void main(String[] args)
            throws Exception {

        VulnerableSastExample app =
                new VulnerableSastExample();

        System.out.println(
                app.weakHash("password")
        );

        app.executeCommand(
                "echo vulnerable-test"
        );

        app.login(
                "admin",
                "super-secret-password"
        );
    }
}
