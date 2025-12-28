package dbLogic.systemLogin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import MainControllers.DBController;

public class DBSubscriberDetails {

    /**
     * Updates subscriber personal details.
     * Only non-null and non-empty fields will be updated.
     *
     * @param userId   the user ID
     * @param username new username (optional)
     * @param phone    new phone number (optional)
     * @param email    new email (optional)
     * @return true if update was executed, false otherwise
     */
    public boolean updateSubscriberDetails(
            int userId,
            String username,
            String phone,
            String email
    ) {

        List<String> fields = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        if (username != null && !username.isBlank()) {
            fields.add("username = ?");
            values.add(username);
        }

        if (phone != null && !phone.isBlank()) {
            fields.add("phone = ?");
            values.add(phone);
        }

        if (email != null && !email.isBlank()) {
            fields.add("email = ?");
            values.add(email);
        }

        // אם לא הוזן שום שדה – אין מה לעדכן
        if (fields.isEmpty()) {
            return false;
        }

        String sql = "UPDATE user SET " +
                     String.join(", ", fields) +
                     " WHERE id = ?";

        try {
            Connection conn = DBController.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);

            int index = 1;
            for (Object value : values) {
                pstmt.setObject(index++, value);
            }

            pstmt.setInt(index, userId);

            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
