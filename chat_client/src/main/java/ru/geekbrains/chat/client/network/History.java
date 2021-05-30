package ru.geekbrains.chat.client.network;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class History {

public static void writeHistory(String nick, String text){
    String finalMessage = String.format("[ME] %s\n", text);
    File file = new File(String.format("%s.log", nick));

    try {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
        writer.write(text);
        writer.flush();

    } catch (IOException e) {
        e.printStackTrace();
    }
}
}
