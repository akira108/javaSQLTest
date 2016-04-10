import org.sqlite.SQLiteConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import static spark.Spark.*;
import org.joda.time.DateTime;

public class Main {

    public static void main(String[] args) {
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            SQLiteConfig config = new SQLiteConfig();
            config.enforceForeignKeys(true);
            connection = DriverManager.getConnection("jdbc:sqlite:bot.db",config.toProperties());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(2);
        }

        createTables(connection);

        try {

            Bot bot = new Bot(connection);
            bot.invited();

            while(true) {
                InputStreamReader isr = new InputStreamReader(System.in);
                BufferedReader br = new BufferedReader(isr);
                try {
                    String message = br.readLine();

                    if (message.equals("bye")) {
                        break;
                    }

                    bot.receiveMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

//        get("/hello", (req, res) -> "Hello World!");
    }


    public static void createTables(Connection connection) {
        String createGroupsTableSQL =  "CREATE TABLE IF NOT EXISTS groups (id integer primary key, state integer, invited_at datetime)";
        String createUsersTableSQL = "CREATE TABLE IF NOT EXISTS users (id integer primary key, group_id integer, name ntext)";
        String createBotMessagesTableSQL = "CREATE TABLE IF NOT EXISTS bot_messages (id integer primary key, message_type integer, content_type integer, content ntext, state integer, group_id integer, created_at datetime)";
        String createMessagesTableSQL = "CREATE TABLE IF NOT EXISTS messages (id integer primary key, content_type integer, content ntext, state integer, parent_message_id integer, created_at datetime)";

        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(createGroupsTableSQL);
            stmt.executeUpdate(createUsersTableSQL);
            stmt.executeUpdate(createBotMessagesTableSQL);
            stmt.executeUpdate(createMessagesTableSQL);
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }
}
