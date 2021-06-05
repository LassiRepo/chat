package ru.geekbrains.chat_server.auth;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ru.geekbrains.chat_server.db.ClientsDatabaseService;

import java.sql.SQLException;

public class AuthServiceImpl implements AuthService {
    private ClientsDatabaseService dbService;
    public static final Logger LOGGER = LogManager.getLogger(AuthServiceImpl.class);

    @Override
    public void start() {
        LOGGER.info("Auth service start");
        dbService = ClientsDatabaseService.getInstance();
        dbService.createDBConnection();
    }

    @Override
    public void stop() {
        LOGGER.info("Auth service stopped");


    }

    @Override
    public String changeUsername(String oldName, String newName) {
        try {
            return dbService.changeUsername(oldName, newName);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException("Username change unsuccessful");
        }
    }

    @Override
    public String changePassword(String username, String oldPassword, String newPassword) {
        return null;
    }

    @Override
    public String getUsernameByLoginAndPassword(String login, String password) {
        User user = dbService.getUserByLogin(login);

        if (user.getPassword().equals(password)) {
            return user.getUsername();
        } else {
            return null;
        }
    }


}


