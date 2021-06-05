package ru.geekbrains.chat_server.server;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ru.geekbrains.april_chat.common.ChatMessage;
import ru.geekbrains.april_chat.common.MessageType;
import ru.geekbrains.chat_server.auth.AuthServiceImpl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;



public class ClientHandler {
    private static final long AUTH_TIMEOUT = 20_000;
    private Socket socket;
    private ChatServer chatServer;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;
    private String currentUsername;
    public static final Logger LOGGER = LogManager.getLogger(ClientHandler.class);

    public ClientHandler(Socket socket, ChatServer chatServer) {
        try {
            this.chatServer = chatServer;
            this.socket = socket;
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());
            LOGGER.info("Client handler created!!!");
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void handle() {
//        new Thread(() -> {
            chatServer.getExecutorService().execute(() -> {
//                System.out.println("Thread started " + Thread.currentThread().getName());
                        try {
                            authenticate();
                            readMessages();
                        } catch (IOException e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                    });
//        }).start();
    };

    private void readMessages() throws IOException {
        try {
            while (!Thread.currentThread().isInterrupted() || socket.isConnected()) {
                String msg = inputStream.readUTF();
                ChatMessage message = ChatMessage.unmarshall(msg);
                message.setFrom(this.currentUsername);
                switch (message.getMessageType()) {
                    case PUBLIC:
                        chatServer.sendBroadcastMessage(message);
                        break;
                    case PRIVATE:
                        chatServer.sendPrivateMessage(message);
                        break;
                    case CHANGE_USERNAME:
                        LOGGER.info(String.format("Got change un f: %s n %s", this.currentUsername, message.getBody()));
                        String newName = chatServer.getAuthService().changeUsername(this.currentUsername, message.getBody());
                        ChatMessage response = new ChatMessage();
                        if (newName == null && newName.isEmpty()) {
                            response.setMessageType(MessageType.ERROR);
                            response.setBody("Something went wrong!");
                        } else {
                            response.setMessageType(MessageType.CHANGE_USERNAME_CONFIRM);
                            response.setBody(newName);
                        }
                        sendMessage(response);
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            closeHandler();
        }
    }

    public void sendMessage(ChatMessage message) {
        try {
            outputStream.writeUTF(message.marshall());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public String getCurrentName() {
        return this.currentUsername;
    }

    private void authenticate() {

        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        if (currentUsername == null) {
                            ChatMessage response = new ChatMessage();
                            response.setMessageType(MessageType.ERROR);
                            response.setBody("Authentication timeout!\nPlease, try again later!");
                            sendMessage(response);
                            Thread.sleep(50);
                            socket.close();
                        }
                    }
                } catch (InterruptedException | IOException e) {
                    e.getStackTrace();
                }
            }
        }, AUTH_TIMEOUT);


        LOGGER.info("Started client  auth...");

        try {
            while (true) {
                String authMessage = inputStream.readUTF();
                LOGGER.info("Auth received");
                ChatMessage msg = ChatMessage.unmarshall(authMessage);
                String username = chatServer.getAuthService().getUsernameByLoginAndPassword(msg.getLogin(), msg.getPassword());
                ChatMessage response = new ChatMessage();

                if (username.isEmpty()) {
                    response.setMessageType(MessageType.ERROR);
                    response.setBody("Wrong username or password!");
                    LOGGER.info("Wrong credentials");
                } else if (chatServer.isUserOnline(username)) {
                    response.setMessageType(MessageType.ERROR);
                    response.setBody("Double auth!");
                    LOGGER.info("Double auth!");
                } else {
                    response.setMessageType(MessageType.AUTH_CONFIRM);
                    response.setBody(username);
                    currentUsername = username;
                    chatServer.subscribe(this);
                    LOGGER.info("Subscribed");
                    sendMessage(response);
                    break;
                }
                sendMessage(response);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void closeHandler() {
        try {
            chatServer.unsubscribe(this);
            socket.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public String getCurrentUsername() {
        return currentUsername;
    }
}
