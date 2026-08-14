public class VulnerableTest {

    private static final String DB_PASS = "admin_secret_123";

    public void findUser(String userId) {
        String query = "SELECT * FROM users WHERE id='" + userId + "'";
        System.out.println(query);
    }
}