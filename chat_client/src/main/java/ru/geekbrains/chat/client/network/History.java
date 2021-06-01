package ru.geekbrains.chat.client.network;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class History {
    public static final int SIZE_OF_RETRIEVED_HISTORY = 100;
    String login;
    File history;
    String historyPath;

    public History(String login) {
        this.historyPath = "history/";
        this.login = login;
        this.history = new File(historyPath + "history_" + login + ".txt");
        if (!history.exists()) {
            File path = new File(historyPath);
            path.mkdirs();
        }
    }

    public List<String> readHistory() {
        List<String> result = new ArrayList<>();
        if (!history.exists()) {
            result.add("No previous history");
            return result;
        }
        if (history.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(history))) {
                String historyString;
                List<String> historyStrings = new ArrayList<>();
                while ((historyString = reader.readLine()) != null) {
                    historyStrings.add(historyString);
                }
                if (historyStrings.size() <= SIZE_OF_RETRIEVED_HISTORY) {
                    result = historyStrings;
                }
                if (historyStrings.size() > SIZE_OF_RETRIEVED_HISTORY) {
                    int firstIndex = historyStrings.size() - SIZE_OF_RETRIEVED_HISTORY;
                    result = new ArrayList<>(100);

                    for (int counter = firstIndex - 1; counter < historyStrings.size(); counter++) {
                        result.add(historyStrings.get(counter));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("History for " + result.size());
        return result;
    }


public void writeHistory(String nick, String text){
//    String finalMessage = String.format("[ME] %s\n", text);
//    File file = new File(String.format("%s.log", nick));

    try {
        BufferedWriter writer = new BufferedWriter(new FileWriter(history, true));
        writer.write(text);
        writer.flush();

    } catch (IOException e) {
        e.printStackTrace();
    }
}
}
