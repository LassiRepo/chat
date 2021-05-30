package ru.geekbrains.chat_server.db;

import ru.geekbrains.chat_server.auth.User;

import java.sql.*;
import java.util.Properties;

public class ClientsDatabaseService {
    private static Connection connection;
    private static Statement statement;
    private static final String GET_USER_BY_USERNAME = "SELECT * FROM chat_user WHERE login = '%s'";


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
            e.printStackTrace();
        } finally {
            DBDisconnect();
        }
    }

    private static void DBDisconnect() {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
