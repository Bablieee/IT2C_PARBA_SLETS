
package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.dao.DatabaseConnection;
import com.scc.model.User;

import java.sql.*;
import java.util.Scanner;

public class AdminMenu {

    private final UserDao userDao;
    private final Scanner sc;

    public AdminMenu(UserDao userDao, Scanner sc) {
        this.userDao = userDao;
        this.sc = sc;
    }

    public void display(User adminUser) {

        if (!adminUser.roles().equalsIgnoreCase("ADMINISTRATOR")) {
            System.out.println("Access denied!");
            return;
        }

        boolean administering = true;

        while (administering) {

            System.out.println("\n==============================================");
            System.out.println(" SCHOOL LABORATORY EQUIPMENT TRACKING SYSTEM");
            System.out.println("             ADMINISTRATOR DASHBOARD");
            System.out.println("==============================================");

            System.out.println("1. View All Users");
            System.out.println("2. View Pending User Approvals");
            System.out.println("3. Approve User Registration");
            System.out.println("4. Add New User");
            System.out.println("5. Update User Role / Status");
            System.out.println("6. Delete User Account");
            System.out.println("7. Logout");

            System.out.print("Choose an option (1-7): ");

            String choice = sc.nextLine();

            switch (choice) {
                case "1" -> viewAllUsers();
                case "2" -> viewPendingUsers();
                case "3" -> approveUser();
                case "4" -> addNewUser();
                case "5" -> updateUserRoleOrStatus();
                case "6" -> deleteUser();
                case "7" -> {
                    administering = false;
                    System.out.println("Logging out...");
                }
                default -> System.out.println("Invalid option!");
            }
        }
    }

    private void viewAllUsers() {

        String sql =
                "SELECT id, username, roles, status FROM users";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n========== ALL USERS ==========");

            while (rs.next()) {
                System.out.printf(
                        "[%d] Username: %s | Role: %s | Status: %s%n",
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("roles"),
                        rs.getString("status")
                );
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private void viewPendingUsers() {

        String sql =
                "SELECT id, username, roles, status " +
                        "FROM users WHERE status = 'PENDING'";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n====== PENDING APPROVALS ======");

            boolean found = false;

            while (rs.next()) {
                found = true;

                System.out.printf(
                        "[%d] Username: %s | Role: %s | Status: %s%n",
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("roles"),
                        rs.getString("status")
                );
            }

            if (!found) {
                System.out.println("No pending users.");
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private void approveUser() {

        viewPendingUsers();

        System.out.print("Enter User ID to approve: ");

        try {
            int id = Integer.parseInt(sc.nextLine());

            String sql =
                    "UPDATE users SET status = 'APPROVED' " +
                            "WHERE id = ? AND status = 'PENDING'";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, id);

                if (pstmt.executeUpdate() > 0) {
                    System.out.println("User successfully approved.");
                } else {
                    System.out.println("User not found or already approved.");
                }
            }

        } catch (NumberFormatException e) {
            System.out.println("Enter a valid numeric ID.");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private void addNewUser() {

        System.out.print("Enter username: ");
        String username = sc.nextLine();

        System.out.print("Enter password: ");
        String password = sc.nextLine();

        String role = selectRole();

        if (role == null) {
            return;
        }

        String sql =
                "INSERT INTO users(username, password, roles, status) " +
                        "VALUES(?, ?, ?, 'APPROVED')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);

            pstmt.executeUpdate();

            System.out.println("New user successfully added!");
            System.out.println("Assigned Role: " + role);

        } catch (SQLException e) {
            System.err.println("Error adding user: " + e.getMessage());
        }
    }

    private String selectRole() {

        System.out.println("\n========== SELECT USER ROLE ==========");
        System.out.println("[1] ADMINISTRATOR");
        System.out.println("[2] LABORATORY STAFF");
        System.out.println("[3] EQUIPMENT VIEWER");
        System.out.print("Choose role (1-3): ");

        return switch (sc.nextLine()) {
            case "1" -> "ADMINISTRATOR";
            case "2" -> "LABORATORY STAFF";
            case "3" -> "EQUIPMENT VIEWER";
            default -> {
                System.out.println("Invalid role selection!");
                yield null;
            }
        };
    }

    private void updateUserRoleOrStatus() {

        viewAllUsers();

        System.out.print("Enter User ID to update: ");

        try {
            int id = Integer.parseInt(sc.nextLine());

            String role = selectRole();

            if (role == null) {
                return;
            }

            System.out.print("Enter new status (PENDING / APPROVED): ");
            String status = sc.nextLine().toUpperCase();

            if (!status.equals("PENDING") &&
                    !status.equals("APPROVED")) {
                System.out.println("Invalid status!");
                return;
            }

            String sql =
                    "UPDATE users SET roles = ?, status = ? WHERE id = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, role);
                pstmt.setString(2, status);
                pstmt.setInt(3, id);

                if (pstmt.executeUpdate() > 0) {
                    System.out.println("User successfully updated!");
                } else {
                    System.out.println("User ID not found.");
                }
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid numeric ID.");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private void deleteUser() {

        viewAllUsers();

        System.out.print("Enter User ID to delete: ");

        try {
            int id = Integer.parseInt(sc.nextLine());

            String sql =
                    "DELETE FROM users WHERE id = ? " +
                            "AND roles != 'ADMINISTRATOR'";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, id);

                if (pstmt.executeUpdate() > 0) {
                    System.out.println("User account deleted!");
                } else {
                    System.out.println(
                            "User not found or administrator cannot be deleted."
                    );
                }
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid numeric ID.");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}
