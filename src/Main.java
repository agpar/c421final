import java.sql.*;
import java.util.Scanner;


public class Main {

    public static void main(String[] args) throws SQLException
    {
        DBConnect dbc = new DBConnect();
        Connection con = dbc.getConnection();
        drawMenu();

    }

    public static void drawMenu()
    {
        Scanner scanner = new Scanner(System.in);
        int choice = 0;
        do {
            System.out.println("Choose an option:");
            System.out.println("=================\n");
            System.out.println("1. ");
            System.out.println("2. ");
            System.out.println("3. ");
            System.out.println("4. ");
            System.out.println("5. ");
            System.out.println("6. Quit");
            choice = scanner.nextInt();
            System.out.println("\n");
        } while (choice != 6);

    }

}
