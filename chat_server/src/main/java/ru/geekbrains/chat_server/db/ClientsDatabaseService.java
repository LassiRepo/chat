package ru.geekbrains.chat_server.db;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ru.geekbrains.chat_server.auth.AuthServiceImpl;
import ru.geekbrains.chat_server.auth.User;

import java.sql.*;
import java.util.Properties;

public class ClientsDatabaseService {
    private static ClientsDatabaseService instance;
    private static Connection connection;
    private static Statement statement;
    private static final String GET_USER_BY_USERNAME = "SELECT * FROM chat_user WHERE login = '%s'";
    private final String CHANGE_USERNAME = "update chat_user set username = ? where username = ?;";
    public static final Logger LOGGER = LogManager.getLogger(ClientsDatabaseService.class);


    private ClientsDatabaseService() {
        createDBConnection();
    }

    public static ClientsDatabaseService getInstance() {
        if (instance != null) return instance;
        instance = new ClientsDatabaseService();
        return instance;
    }
    public String changeUsername(String oldName, String newName) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(CHANGE_USERNAME)) {
            ps.setString(1, newName);
            ps.setString(2, oldName);
            if (ps.executeUpdate() > 0) return newName;
        }
        return oldName;
    }


    public User getUserByLogin(String login) {
        try {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(String.format(GET_USER_BY_USERNAME, login));
            resultSet.next();

            User user = new User();
            user.setId(resultSet.getInt(1));
            user.setUsername(resultSet.getString(2));
            user.setLogin(resultSet.getString(3));
            user.setPassword(resultSet.getString(4));

            return user;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            e.printStackTrace();
        }

        return null;
    }

    public void createDBConnection() {
        try {
            Properties props = new Properties();
            props.setProperty("user", "postgres");
            props.setProperty("password", "pa55word");
            connection = DriverManager.getConnection("jdbc:postgresql://localhost/chatDB", props);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            DBDisconnect();
        }
    }

    private static void DBDisconnect() {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                LOGGER.error(e.getMessage(), e);
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }
}
