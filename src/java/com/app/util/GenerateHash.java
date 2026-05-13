public class GenerateHash {
    public static void main(String[] args) {
        String plainPassword = "admin123"; // change to your desired password
        String hash = com.app.util.PasswordUtil.hashPassword(plainPassword);
        System.out.println("New hash: " + hash);
    }
}