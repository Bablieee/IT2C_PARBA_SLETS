
package com.scc;

import com.scc.dao.UserDao;
import com.scc.model.User;
import com.scc.ui.AdminMenu;

import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final UserDao userDao = new UserDao();

    public static void main(String[] args) {

        boolean running = true;

        while (running) {

            System.out.println("\n==============================================");
            System.out.println(" SCHOOL LABORATORY EQUIPMENT TRACKING SYSTEM");
            System.out.println("==============================================");
            System.out.println("[1] Register Account");
            System.out.println("[2] Login");
            System.out.println("[3] Exit");
            System.out.println("==============================================");

            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    registerFlow();
                    break;

                case "2":
                    loginFlow();
                    break;

                case "3":
                    running = false;
                    System.out.println("Thank you for using the system!");
                    break;

                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        }

        scanner.close();
    }

    // ==============================
    // USER REGISTRATION
    // ==============================

    private static void registerFlow() {

        System.out.println("\n==============================================");
        System.out.println("              USER REGISTRATION");
        System.out.println("==============================================");

        System.out.print("Enter Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Username and password cannot be empty.");
            return;
        }

        System.out.println("\nAVAILABLE USER ROLES:");
        System.out.println("[1] ADMINISTRATOR");
        System.out.println("[2] LABORATORY STAFF");
        System.out.println("[3] EQUIPMENT VIEWER");


        String roleChoice = scanner.nextLine().trim();

        String role = switch (roleChoice) {
            case "1" -> "ADMINISTRATOR";
            case "2" -> "LABORATORY STAFF";
            case "3" -> "EQUIPMENT VIEWER";
            default -> null;
        };

        if (role == null) {
            System.out.println("Invalid role selection!");
            return;
        }

        // UserDao.registerUser requires exactly 3 arguments
        if (userDao.registerUser(username, password, role)) {

            System.out.println("\nRegistration Successful!");
            System.out.println("Assigned Role: " + role);
            System.out.println("Account Status: PENDING");
            System.out.println("Please wait for administrator approval.");

        } else {
            System.out.println("\nRegistration Failed!");
            System.out.println("Username may already exist.");
        }
    }

    // ==============================
    // USER LOGIN
    // ==============================

    private static void loginFlow() {

        System.out.println("\n==============================================");
        System.out.println("                  USER LOGIN");
        System.out.println("==============================================");

        System.out.print("Enter Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();

        // Login using the correct UserDao method
        User user = userDao.login(username, password);

        if (user == null) {
            System.out.println("Login Failed!");
            return;
        }

        System.out.println("\nLogin Successful!");
        System.out.println("Welcome, " + user.username());
        System.out.println("User Role: " + user.roles());

        // Check the correct User record accessor: roles()
        switch (user.roles().toUpperCase()) {

            case "ADMINISTRATOR":

                System.out.println("\nOpening Administrator Dashboard...");

                AdminMenu adminMenu = new AdminMenu(userDao, scanner);
                adminMenu.display(user);

                break;

            case "LABORATORY STAFF":

                System.out.println("\n==============================================");
                System.out.println("          LABORATORY STAFF DASHBOARD");
                System.out.println("==============================================");

                System.out.println("Welcome, " + user.username());
                System.out.println("Laboratory Staff access granted.");
                System.out.println("Equipment management menu is not yet connected.");

                break;

            case "EQUIPMENT VIEWER":

                System.out.println("\n==============================================");
                System.out.println("           EQUIPMENT VIEWER DASHBOARD");
                System.out.println("==============================================");

                System.out.println("Welcome, " + user.username());
                System.out.println("View-only access granted.");
                System.out.println("Equipment viewing menu is not yet connected.");

                break;

            default:

                System.out.println("Unknown user role!");
                break;
        }
    }
}
