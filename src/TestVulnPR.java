public class TestVulnPR {
    private static final String SECRET = "admin_password_123";
    public void query(String id) {
		
        String sql = "SELECT * FROM users WHERE id='" + id + "'";
    }
}