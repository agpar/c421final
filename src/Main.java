import java.sql.*;
import java.text.ParseException;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.lang.StringBuilder;

public class Main {
    static Scanner scanner = new Scanner(System.in);
    static SimpleDateFormat datefmt = new SimpleDateFormat("yyy-MM-dd");

    public static void main(String[] args) throws SQLException
    {
        DBConnect dbc = new DBConnect();
        Connection con = dbc.getConnection();
        drawMenu(con);
        con.close();
    }

    //Draws main menu for picking what to do.
    public static void drawMenu(Connection con) throws SQLException
    {
        int choice = 0;
        do {
            System.out.println("Choose an option:");
            System.out.println("================");
            System.out.println("1. Add a new Book.");
            System.out.println("2. Find a Book.");
            System.out.println("3. Add a new Discount.");
            System.out.println("4. Submit Restock Order.");
            System.out.println("5. Daily Sales Manifest.\n");
            System.out.println("6. Quit\n");
            choice = scan_int("> ");

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
                try
                {
                    addDisc(con);
                }
                catch (SQLException e)
                {
                    System.out.println("Failed with error: ");
                    System.out.println(e);
                }
            }
            else if (choice == 4)
            {
                try
                {
                    restock(con);
                }
                catch (SQLException e)
                {
                    System.out.println("Failed with error: ");
                    System.out.println(e);
                }
            }
            else if (choice == 5)
            {
                try
                {
                    dailySales(con);
                }
                catch (SQLException e)
                {
                    System.out.println("Failed with error: ");
                    System.out.println(e);
                }
            }
        } while (choice != 6);
        con.close();
    }

    //Draws menu and interactively collects data for adding a book.
    public static void addBook(Connection con) throws SQLException
    {
        String publisher, author, title, category;
        long id, qty;
        double price;

        System.out.print("Title: ");
        title = scanner.nextLine();
        System.out.print("Author: ");
        author = scanner.nextLine();
        System.out.print("Publisher: ");
        publisher = scanner.nextLine();
        System.out.print("Category: ");
        category = scanner.nextLine();
        id = scan_isbn("ISBN: ");
        price = scan_double("Price: ");
        qty = scan_int("Quantity: ");

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

        //Interactively create a Category for the book if the input does not exist.
        if (!rs.next())
        {
            System.out.println(String.format("Category '%s' does not exist. Create it?", category));
            System.out.print("[y]/n: ");
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

    //Interactive book searching.
    public static void findBookMenuItem(Connection con) throws SQLException
    {
        //Get user preferences for book finding.
        int choice = 0;
        System.out.println("How would you like to search for the book? ");
        do
        {
            System.out.println("1. Title.");
            System.out.println("2. Author.");
            System.out.println("3. Publisher.");
            System.out.println("4. ISBN");
            System.out.println("5. Go back.\n");
            choice = scan_int("> ");
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
            search_term = Long.toString(scan_isbn("ISBN: "));
            cmd = String.format("SELECT * FROM product " +
                    "INNER JOIN book ON product.pid = book.pid " +
                    "WHERE product.pid=%s", search_term);
        }
        else {
            return;
        }

        //Execute select command.
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(cmd);

        //Print results.
        int count = 0;
        System.out.println(String.format("%6s %10s %20s %15s %15s %5s %10s",
                "Numb.", "ISBN", "TITLE", "PUBLISHER", "AUTHOR", "QTY", "PRICE"));
        System.out.println("=======================================" +
                "==================================================");

        while(rs.next())
        {
            long pid = rs.getLong("pid");
            String title = rs.getString("title");
            String publisher = rs.getString("publisher");
            String author = rs.getString("author");
            float price = rs.getFloat("price");
            int qty = rs.getInt("qty");

            System.out.println(String.format("%4d:: %10d %20s %15s %15s %5d %10.2f",
                        count, pid, title, publisher, author, qty, price));
            count++;
        }
        System.out.println();
        System.out.print("Press Enter to continue.");
        scanner.nextLine();
        System.out.println();
        stmt.close();
    }

    //Interactive discount creation.
    public static void addDisc(Connection con) throws SQLException
    {
        //Gather information for discount user wishes to make.
        int choice = 0;
        String searchType, cmd;
        System.out.println("What would you like to apply a discount to? ");
        do
        {
            System.out.println("1. Single Book.");
            System.out.println("2. Author's Work.");
            System.out.println("3. Category.");
            System.out.println("4. Go back.\n");
            choice = scan_int("> ");
        } while (choice > 4 || choice < 1);

        //Get choice (ISBN, author, category) and confirm it exists.
        String search_term;
        if (choice == 1)
        {
            search_term = Long.toString(scan_isbn("ISBN: "));
            cmd = String.format("SELECT * FROM product " +
                    "INNER JOIN book ON product.pid = book.pid " +
                    "WHERE product.pid=%s", search_term);
            searchType = "ISBN";
        }
        else if (choice == 2)
        {
            System.out.print("Author: ");
            search_term = scanner.nextLine();
            cmd = String.format("SELECT * FROM product " +
                    "INNER JOIN book ON product.pid = book.pid " +
                    "WHERE LOWER(author)=LOWER('%s')", search_term);
            searchType = "Author";
        }
        else if (choice == 3)
        {
            System.out.print("Category: ");
            search_term = scanner.nextLine();
            search_term = search_term.toUpperCase();
            cmd = String.format("SELECT * FROM category " +
                    "WHERE category.name='%s'", search_term);
            searchType = "Category";
        }
        else
        {
            return;
        }

        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(cmd);
        if (!rs.next()) {
            System.out.println(String.format("%s does not exist.\n", searchType));
            return;
        }

        //Get information for discount creation.
        choice = 0;
        System.out.println("Discount by percent or dollar amount?");
        do
        {
            System.out.println("1. Percent");
            System.out.println("2. Dollar Amount");
            System.out.println("3. Go back.\n");
            choice = scan_int("> ");
        } while (choice > 3 || choice < 1);

        String prct="", amnt="";
        if (choice == 1)
        {
            prct = String.valueOf(scan_int("Percent: "));
        }
        else if(choice == 2)
        {
            amnt = String.valueOf(scan_int("Amount: "));
        }

        //Get expiry date of Discount (lazily assuming discounts go into effect today.)
        String expDate = "";
        Date today = new Date();
        String today_fmted = datefmt.format(today);
        while(expDate.isEmpty())
        {
            System.out.print("When will discount expire (yyyy-mm-dd)? ");
            expDate = scanner.nextLine();
            try
            {
                Date expirey = datefmt.parse(expDate);
                if (expirey.before(today))
                {
                    System.out.println("Invalid date. Expiry date must be in the future. \n");
                    expDate = "";
                }
            }
            catch (ParseException e)
            {
                System.out.println("Invalid date. Use yyy-mm-dd format.\n");
                expDate = "";
            }
        }

        //Create the discount.
        if(prct.isEmpty()) {
            cmd = String.format("INSERT INTO discount (percent,amount,from_date,to_date) " +
                    "VALUES (null, %s, '%s', '%s')", amnt, today_fmted, expDate);
        }
        else
        {
            cmd = String.format("INSERT INTO discount (percent,amount,from_date,to_date) " +
                    "VALUES (%s, null, '%s', '%s')", prct, today_fmted, expDate);
        }
        stmt.executeUpdate(cmd, Statement.RETURN_GENERATED_KEYS);
        rs = stmt.getGeneratedKeys();
        rs.next();
        int did = rs.getInt("did");


        //Create and apply discount to single book.
        if(searchType.equals("ISBN"))
        {
            cmd = String.format("INSERT INTO discOnProd (did, pid) " +
                    "VALUES (%d, %s)",did, search_term );
            stmt.executeUpdate(cmd);

            cmd = String.format("SELECT * FROM product where pid=%s", search_term);
            rs = stmt.executeQuery(cmd);
            rs.next();
            String title = rs.getString("title");
            float price = rs.getFloat("price");
            float new_price;

            if(prct.isEmpty()){
                new_price = price - Float.valueOf(amnt);
            }
            else
            {
                new_price = price - (price * (Float.valueOf(prct)/100));
            }

            System.out.println("Discount created successfully. ");
            System.out.println(String.format("%s price changed from %.2f to %.2f. \n", title, price, new_price));

        }

        //Create and apply discount to all of an author's books.
        else if(searchType.equals("Author"))
        {
            cmd = String.format("SELECT * FROM product where author='%s'", search_term);
            rs = stmt.executeQuery(cmd);
            cmd = "INSERT INTO disconProd (did, pid) " +
                    "VALUES ";
            StringBuilder sb = new StringBuilder();
            sb.append(cmd);
            int counter = 0;

            while(rs.next())
            {
                counter++;
                int isbn = rs.getInt("pid");
                if (counter == 1)
                    sb.append(String.format("(%d, %d)", did, isbn));
                else
                    sb.append(String.format(", (%d, %d)", did, isbn));

            }

            cmd = sb.toString();
            stmt.executeUpdate(cmd);
            System.out.println(String.format("Applied discount to all %d books by %s.\n", counter, search_term));

        }

        //Create and apply discount to category.
        else if(searchType.equals("Category"))
        {
            cmd = String.format("INSERT INTO discOnCat (did, category) " +
                    "VALUES (%d, '%s')", did, search_term);
            stmt.executeUpdate(cmd);

            System.out.println(String.format("Applied discount to all books in %s category.\n", search_term));
        }

    }

    //Interactive Restocking.
    public static void restock(Connection con) throws SQLException
    {
        Statement stmt = con.createStatement();
        int eid = scan_int("Input employee ID: ");

        //Check if employee ID exists.
        String emp_search = String.format(
                "SELECT * FROM employee " +
                "WHERE eid=%d", eid);
        ResultSet rs = stmt.executeQuery(emp_search);
        if (!rs.next())
        {
            System.out.println("Employee ID does not exist.");
            return;
        }

        //Check that book exists.
        long id = scan_isbn("Input ISBN of book to restock: ");

        String book_search = String.format(
                "SELECT * FROM book " +
                        "WHERE pid=%d", id);
        rs = stmt.executeQuery(book_search);
        if (!rs.next())
        {
            System.out.println("Book with this ISBN does not exist.");
            return;
        }

        //Find out qty to restock.
        int order_qty = scan_int("Order qty: ");

        //Make the restock happen.
        String restocks = String.format(
                "INSERT INTO restocks (eid,pid,qty) " +
                "VALUES (%d, %d, %d)", eid, id, order_qty);
        stmt.executeUpdate(restocks);

        System.out.println("Submitted restock order on book " + id + ".\n");
        stmt.close();
    }

    //Print daily sales.
    public static void dailySales(Connection con) throws SQLException
    {
        String dsq = "SELECT * FROM DAILY_SALES;";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(dsq);

        int total = 0;
        System.out.println(String.format("%5s %35s %25s %20s %5s %8s %10s",
                "ISBN", "TITLE", "PUBLISHER", "AUTHOR", "QTY", "SALES","PRICE"));
        System.out.println("==================================================================" +
                "==================================================");
        while(rs.next())
        {
            long pid = rs.getLong("pid");
            String title = rs.getString("title");
            String publisher = rs.getString("publisher");
            String author = rs.getString("author");
            float price = rs.getFloat("price");
            int qty = rs.getInt("qty");
            int sales = rs.getInt("sales");

            total += (sales*price);

            System.out.println(String.format("%5d %35s %25s %20s %5d %8d %10.2f",
                    pid, title, publisher, author, qty, sales, price));
        }
        System.out.println();
        System.out.println("Total sales today: $" + total + ".\n");
        System.out.println("Press Enter to continue");
        scanner.nextLine();
        stmt.close();
    }

    private static long normalizeISBN(String isbn) throws NumberFormatException
    {
        isbn = isbn.replaceAll("[^\\d.]", "");
        return Long.parseLong(isbn);
    }

    private static int scan_int(String prompt)
    {
        boolean got = false;
        int x = 0;
        while(!got)
        {
            try
            {
                System.out.print(prompt);
                x = scanner.nextInt();
                scanner.nextLine();
                got = true;
            }
            catch (InputMismatchException e)
            {
                scanner.nextLine();
                System.out.println("\nERROR: Input must be an integral number.\n");
                got = false;
            }
        }
        return x;
    }

    private static long scan_isbn(String prompt)
    {
        boolean got = false;
        long isbn = 0;
        while(!got)
        {
            try
            {
                System.out.print(prompt);
                String x = scanner.nextLine();
                System.out.println();
                isbn = normalizeISBN(x);
                got = true;
            }
            catch (NumberFormatException e)
            {
                System.out.println("ERROR: ISBN must contain some numbers(e.g. 143-2334-12)\n");
                got = false;
            }
        }
        return isbn;
    }

    private static double scan_double(String prompt)
    {
        boolean got = false;
        double target = 0.0;
        while(!got)
        {
            try
            {
                System.out.print(prompt);
                target = scanner.nextDouble();
                System.out.println();
                scanner.nextLine();
                got = true;
            }
            catch (InputMismatchException e)
            {
                scanner.nextLine();
                System.out.println("\nERROR: Input must be a floating point number.\n");
                got = false;
            }
        }
        return target;
    }

}
