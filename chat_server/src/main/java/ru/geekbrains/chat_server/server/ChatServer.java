package ru.geekbrains.chat_server.server;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ru.geekbrains.april_chat.common.ChatMessage;
import ru.geekbrains.april_chat.common.MessageType;
import ru.geekbrains.chat_server.auth.AuthService;
import ru.geekbrains.chat_server.auth.AuthServiceImpl;
import ru.geekbrains.chat_server.db.ClientsDatabaseService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private static final int PORT = 12256;
    private List<ClientHandler> listOnlineUsers;
    private AuthService authService;
    private ExecutorService executorService;
    public static final Logger LOGGER = LogManager.getLogger(ChatServer.class);

    public ExecutorService getExecutorService() {
        return executorService;
    }


    public ChatServer() {
        this.listOnlineUsers = new ArrayList<>();
        this.authService = new AuthServiceImpl();
        this.executorService = Executors.newCachedThreadPool();

    }

    public void start() {
        LOGGER.info("Server started");
        authService.start();
        executorService.execute(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                while (true) {
                    LOGGER.info("Waiting for connection");
                    Socket socket = serverSocket.accept();
                    LOGGER.info("Client connected");
                    new ClientHandler(socket, this).handle();
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            } finally {
                authService.stop();
                this.executorService.shutdownNow();
            }
        });
    }

    private synchronized void sendListOnlineUsers() {
        ChatMessage msg = new ChatMessage();
        msg.setMessageType(MessageType.CLIENT_LIST);
        msg.setOnlineUsers(new ArrayList<>());
        for (ClientHandler user : listOnlineUsers) {
            msg.getOnlineUsers().add(user.getCurrentName());
        }
        for (ClientHandler user : listOnlineUsers) {
            user.sendMessage(msg);
        }
    }

    public synchronized void sendBroadcastMessage(ChatMessage message) {
        for (ClientHandler user : listOnlineUsers) {
            user.sendMessage(message);
        }
    }

    public void sendPrivateMessage(ChatMessage message) {
        for (ClientHandler user : listOnlineUsers) {
            if (user.getCurrentName().equals(message.getTo())) user.sendMessage(message);
        }
    }


    public synchronized boolean isUserOnline(String username) {
        for (ClientHandler user : listOnlineUsers) {
            if (user.getCurrentName().equals(username)) return true;
        }
        return false;
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        listOnlineUsers.add(clientHandler);
        sendListOnlineUsers();
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        listOnlineUsers.remove(clientHandler);
        sendListOnlineUsers();
    }

    public AuthService getAuthService() {
        return authService;
    }

}
