package ru.geekbrains.chat_server.auth;

import ru.geekbrains.chat_server.db.ClientsDatabaseService;

public class AuthServiceImpl implements AuthService {
    ClientsDatabaseService clientsDatabaseService = new ClientsDatabaseService();


    @Override
    public void start() {
        System.out.println("Auth service started");
        clientsDatabaseService.createDBConnection();
    }

    @Override
    public void stop() {
        System.out.println("Auth service stopped");

    }

    @Override
    public String changeUsername(String oldName, String newName) {
        return null;
    }

    @Override
    public String changePassword(String username, String oldPassword, String newPassword) {
        return null;
    }

    @Override
    public String getUsernameByLoginAndPassword(String login, String password) {
        User user = clientsDatabaseService.getUserByLogin(login);

        if (user.getPassword().equals(password)) {
            return user.getUsername();
        } else {
            return null;
        }
    }


}


