package ru.geekbrains.chat_server.auth;

import java.sql.*;
import java.util.Properties;

public class AuthServiceImpl implements AuthService {
    private static Connection connection;
    private static Statement statement;

    private static final String GET_USER_BY_USERNAME = "SELECT * FROM chat_user WHERE login = '%s'";

    @Override
    public void start() {
        System.out.println("Auth service started");
        createDBConnection();
    }

    @Override
    public void stop() {
        System.out.println("Auth service stopped");

    }

    @Override
    public String getUsernameByLoginAndPassword(String login, String password) {
        try {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(String.format(GET_USER_BY_USERNAME, login));
            resultSet.next();

            User user = new User();
            user.setId(resultSet.getInt(1));
            user.setUsername(resultSet.getString(2));
            user.setLogin(resultSet.getString(3));
            user.setPassword(resultSet.getString(4));

            if (user.getPassword().equals(password)) return user.getUsername();
            return null;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String changeUsername(String oldName, String newName) {
        return null;
    }

    @Override
    public String changePassword(String username, String oldPassword, String newPassword) {
        return null;
    }

    private static void createDBConnection() {
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
