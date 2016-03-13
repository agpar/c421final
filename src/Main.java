import java.sql.*;
import java.util.Scanner;


public class Main {
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws SQLException
    {
        DBConnect dbc = new DBConnect();
        Connection con = dbc.getConnection();
        drawMenu(con);
        con.close();
    }

    public static void drawMenu(Connection con) throws SQLException
    {
        int choice = 0;
        do {
            System.out.println("Choose an option:");
            System.out.println("================");
            System.out.println("1. Add a new Book.");
            System.out.println("2. Find a Book.");
            System.out.println("3. Add a new Discount.");
            System.out.println("4. Submit Restock Order");
            System.out.println("5. ");
            System.out.println("6. Quit\n");
            System.out.print("> ");
            choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 1)
            {
                try{
                    addBook(con);
                }
                catch (SQLException e)
                {
                    System.out.println("Failed with error: ");
                    System.out.println(e);
                }
            }
            else if (choice == 2)
            {
                try
                {
                    findBookMenuItem(con);
                }
                catch (SQLException e)
                {
                    System.out.println("Failed with error: ");
                    System.out.println(e);
                }
            }
            else if (choice == 3)
            {
                addDisc(con);
            }
            else if (choice == 4)
            {
                restock(con);
            }
        } while (choice != 6);
        con.close();
    }

    public static void addBook(Connection con) throws SQLException
    {
        String isbn, publisher, author, title, category;
        long id, qty;
        float price;

        System.out.print("Title: ");
        title = scanner.nextLine();
        System.out.print("Author: ");
        author = scanner.nextLine();
        System.out.print("Publisher: ");
        publisher = scanner.nextLine();
        System.out.print("Category: ");
        category = scanner.nextLine();
        System.out.print("ISBN: ");
        isbn = scanner.nextLine();
        System.out.print("Price: ");
        price = scanner.nextFloat();
        System.out.print("Quantity: ");
        qty = scanner.nextInt();
        scanner.nextLine();

        //Remove any non numbers from isbn (dashes)
        id = normalizeISBN(isbn);

        Statement stmt = con.createStatement();

        //Create the Product
        String cmd = String.format(
                "INSERT INTO product (pid,qty,price,title,author,publisher)" +
                "VALUES (%d, %d, %f, '%s', '%s', '%s');",
                id, qty, price, title, author, publisher);
        stmt.executeUpdate(cmd);

        //Create the Book
        cmd = String.format("INSERT INTO book (pid) VALUES (%d);", id);
        stmt.executeUpdate(cmd);

        //Deal with the category linking field.
        category = category.toUpperCase();
        String genre_search = String.format(
                "SELECT * FROM category " +
                        "WHERE name='%s'", category);
        ResultSet rs = stmt.executeQuery(genre_search);

        if (!rs.next())
        {
            System.out.println(String.format("Category '%s' does not exist. Create it?", category));
            System.out.print("[y]/n");
            String ans = scanner.nextLine();
            if(ans.contains("n") || ans.contains("N")){}
            else
            {
                stmt.executeUpdate(String.format(
                        "INSERT INTO category (name) VALUES ('%s');",
                        category));
                stmt.executeUpdate(String.format(
                        "INSERT INTO incategory (category,pid) VALUES ('%s', %d);",
                        category, id));
            }
        }
        else
        {
            stmt.executeUpdate(String.format(
                    "INSERT INTO incategory (category,pid) VALUES ('%s', %d);",
                    category, id));
        }

        //If no exceptions by now, it worked!
        System.out.println("Created Book successfully.");
        stmt.close();
    }

    public static void findBookMenuItem(Connection con) throws SQLException
    {
        int choice = 0;
        System.out.println("How would you like to search for the book? ");
        do
        {
            System.out.println("1. Title.");
            System.out.println("2. Author.");
            System.out.println("3. Publisher.");
            System.out.println("4. ISBN");
            System.out.println("5. Go back.\n");
            System.out.print("> ");
            choice = scanner.nextInt();
            scanner.nextLine();
        } while (choice > 5 || choice < 1);

        String cmd;
        String search_term;
        if (choice == 1)
        {
            System.out.print("Title: ");
            search_term = scanner.nextLine();
            cmd = String.format("SELECT * FROM product " +
                    "INNER JOIN book ON product.pid = book.pid " +
                    "WHERE LOWER(title)=LOWER('%s')", search_term);
        }
        else if (choice == 2)
        {
            System.out.print("Author: ");
            search_term = scanner.nextLine();
            cmd = String.format("SELECT * FROM product " +
                    "INNER JOIN book ON product.pid = book.pid " +
                    "WHERE LOWER(author)=LOWER('%s')", search_term);
        }
        else if (choice == 3){
            System.out.print("Publisher: ");
            search_term = scanner.nextLine();
            cmd = String.format("SELECT * FROM product " +
                    "INNER JOIN book ON product.pid = book.pid " +
                    "WHERE LOWER(publisher)=LOWER('%s')", search_term);
        }
        else if (choice == 4){
            System.out.print("ISBN: ");
            String inpt = scanner.nextLine();
            search_term = Long.toString(normalizeISBN(inpt));
            cmd = String.format("SELECT * FROM product " +
                    "INNER JOIN book ON product.pid = book.pid " +
                    "WHERE product.pid=%s", search_term);
        }
        else {
            return;
        }

        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(cmd);

        //Print results
        int count = 0;
        System.out.println(String.format("%2s %10s %20s %15s %15s %5s %10s",
                "Numb.", "PID", "TITLE", "PUBLISHER", "AUTHOR", "QTY", "PRICE"));
        System.out.println("====================================" +
                "==================================================");

        while(rs.next())
        {
            long pid = rs.getLong("pid");
            String title = rs.getString("title");
            String publisher = rs.getString("publisher");
            String author = rs.getString("author");
            float price = rs.getFloat("price");
            int qty = rs.getInt("qty");

            System.out.println(String.format("%2d:: %10d %20s %15s %15s %5d %10.2f",
                        count, pid, title, publisher, author, qty, price));
            count++;
        }
        System.out.println();
        System.out.print("Press Enter to continue.");
        scanner.nextLine();
        System.out.println();
    }

    public static void addDisc(Connection con)
    {

    }

    public static void restock(Connection con)
    {

    }
    private static long normalizeISBN(String isbn){
        isbn = isbn.replaceAll("[^\\d.]", "");
        return Long.parseLong(isbn);
    }

}
