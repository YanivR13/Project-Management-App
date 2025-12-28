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
	    boolean updated = false;

	    try {
	        Connection conn = DBController.getInstance().getConnection();

	        /* ===== update subscriber.username ===== */
	        if (username != null && !username.isBlank()) {
	            String sqlSubscriber =
	                "UPDATE subscriber SET username = ? WHERE user_id = ?";
	            try (PreparedStatement ps = conn.prepareStatement(sqlSubscriber)) {
	                ps.setString(1, username);
	                ps.setInt(2, userId);
	                updated |= ps.executeUpdate() > 0;
	            }
	        }

	        /* ===== update user.phone_number & email ===== */
	        List<String> fields = new ArrayList<>();
	        List<Object> values = new ArrayList<>();

	        if (phone != null && !phone.isBlank()) {
	            fields.add("phone_number = ?");
	            values.add(phone);
	        }

	        if (email != null && !email.isBlank()) {
	            fields.add("email = ?");
	            values.add(email);
	        }

	        if (!fields.isEmpty()) {
	            String sqlUser =
	                "UPDATE user SET " + String.join(", ", fields) +
	                " WHERE user_id = ?";

	            try (PreparedStatement ps = conn.prepareStatement(sqlUser)) {
	                int i = 1;
	                for (Object v : values) {
	                    ps.setObject(i++, v);
	                }
	                ps.setInt(i, userId);
	                updated |= ps.executeUpdate() > 0;
	            }
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }

	    return updated;
	}
}

