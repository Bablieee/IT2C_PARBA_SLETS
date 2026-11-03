
package com.scc.dao;

import com.scc.model.User;
import java.sql.*;

public class UserDao {

    public UserDao() {
        try (Connection conn = DatabaseConnection.getConnection()) {

            // Create users table if it does not exist
            String createTable = """
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT NOT NULL UNIQUE,
                        password TEXT NOT NULL,
                        roles TEXT NOT NULL,
                        status TEXT NOT NULL
                    )
                    """;

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createTable);
            }

            defaultAdminGenerator(conn);

        } catch (SQLException e) {
            System.err.println("Database initialization error: "
                    + e.getMessage());
        }
    }

    public void defaultAdminGenerator(Connection conn) {

        String checkSql =
                "SELECT COUNT(*) FROM users WHERE roles = 'ADMINISTRATOR'";

        String insertSql =
                "INSERT INTO users(username, password, roles, status) " +
                        "VALUES('admin', 'admin123', 'ADMINISTRATOR', 'APPROVED')";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                stmt.executeUpdate(insertSql);
                System.out.println("Default Administrator created.");
            }

        } catch (SQLException e) {
            System.err.println("Error creating administrator: "
                    + e.getMessage());
        }
    }

    public boolean registerUser(
            String username,
            String password,
            String role) {

        String sql =
                "INSERT INTO users(username, password, roles, status) " +
                        "VALUES(?, ?, ?, 'PENDING')";

        if (!isValidRole(role)) {
            System.out.println("Invalid user role.");
            return false;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role.toUpperCase());

            pstmt.executeUpdate();

            System.out.println(
                    "Registration successful. Waiting for approval."
            );

            return true;

        } catch (SQLException e) {
            System.err.println("Registration failed: " + e.getMessage());
            return false;
        }
    }

    public User login(String username, String password) {

        String sql =
                "SELECT id, username, password, roles, status " +
                        "FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {

                    String status = rs.getString("status");

                    if (!"APPROVED".equalsIgnoreCase(status)) {
                        System.out.println(
                                "Account is not approved yet."
                        );
                        return null;
                    }

                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("roles"),
                            status
                    );
                }
            }

            System.out.println("Invalid username or password.");

        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
        }

        return null;
    }

    private boolean isValidRole(String role) {
        return role != null &&
                (
                        role.equalsIgnoreCase("ADMINISTRATOR") ||
                                role.equalsIgnoreCase("LABORATORY STAFF") ||
                                role.equalsIgnoreCase("EQUIPMENT VIEWER")
                );
    }
}
