import org.joda.time.DateTime;

import java.sql.*;
import java.util.ArrayList;

/**
 * Created by akira on 2016/04/10.
 */
public class Bot {

    public enum State {
        INVITED,
        LISTENING_NAMES,
        LISTENING_NAMES_COMPLETE,
        ASKING_TEXTS,
        ASKING_PICTURES,
        ADVERTISING,
        GIFT,
        OTHER
    }

    Connection connection;
    State state;

    private int groupId;

    public Bot(Connection c) {
        this.connection = c;
        this.state = State.INVITED;
    }

    private java.sql.Date now() {
        return new java.sql.Date(new DateTime().getMillis());
    }

    public void invited() throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement("INSERT OR IGNORE INTO groups values(?, ?, ?)");
        final int groupId = 12345;
        this.groupId = groupId;
        pstmt.setInt(1, groupId);
        pstmt.setInt(2, state.ordinal());
        pstmt.setDate(3, now());

        int inserted = pstmt.executeUpdate();
        System.out.println("groups updated: " + inserted);
        sendGreeting();
    }

    private void sendGreeting() throws SQLException {
        String message = "名前を教えてね，終わったらスタンプで教えてね";
        state = State.LISTENING_NAMES;
        sendMessage(message);
    }

    private void sendMessage(String message) throws SQLException {
        final PreparedStatement pstmt = connection.prepareStatement("INSERT OR IGNORE INTO bot_messages (message_type, state, content, group_id, created_at) values(?, ?, ?, ?, ?)");
        pstmt.setInt(1, state.ordinal());
        pstmt.setInt(2, state.ordinal());
        pstmt.setString(3, message);
        pstmt.setInt(4, groupId);
        pstmt.setDate(5, now());
        System.out.println(message);
    }

    public void receiveMessage(String message) throws  SQLException {
        switch (state) {
            case INVITED:
                break;
            case LISTENING_NAMES:
                listenName(message);
                break;
            case LISTENING_NAMES_COMPLETE:
                break;
            case ASKING_TEXTS:
                break;
            case ASKING_PICTURES:
                break;
            case ADVERTISING:
                break;
            case GIFT:
                break;
            case OTHER:
                break;
        }
    }

    private void listenName(String message) throws SQLException {
        if (message.equals("END")) {
            state = State.LISTENING_NAMES_COMPLETE;
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * from users WHERE group_id = " + groupId);

            ArrayList<String> nameList = new ArrayList<>();
            String names = "";
            while(rs.next()) {
                String name = rs.getString("name");
                nameList.add(name);
                names += name + " ";
            }
            sendMessage(names + "だね！これからよろしくね！");
            sendMessage("突然だけど" + nameList.get(1) + "，" + nameList.get(0) + "のいいところはどこ？");
            state = State.ASKING_TEXTS;
            return;
        }
        final String name = message;
        final PreparedStatement pstmt = connection.prepareStatement("INSERT OR IGNORE INTO users (name, group_id) values (?, ?)");
        pstmt.setString(1, name);
        pstmt.setInt(2, groupId);
        pstmt.executeUpdate();
    }
}
