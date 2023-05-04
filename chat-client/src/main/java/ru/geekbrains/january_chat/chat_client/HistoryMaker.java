package ru.geekbrains.january_chat.chat_client;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
//работа с историей сообщений
public class HistoryMaker {
    private static final int SIZE_OF_RETRIEVED_HISTORY = 100;
    private static final String HISTORY_PATH = "history/";
    private  String login;
    private File history;

    public HistoryMaker(String login) {
        this.login = login;
        //история записывается в файл по имени пользователя - login
        this.history = new File(HISTORY_PATH + "history_" + login + ".txt");
        //если пользователь еще без истории, нет каталога, то мы его создаем
        if (!history.exists()) {//проверка
            //создание файла
            File path = new File(HISTORY_PATH);
            path.mkdirs();//mkdirs
        }
    }

    public List<String> readHistory() { // Метод считывания истории сообщений, зарегистрированного пользователя, из
                                        // текстового файла.
        if (!history.exists()) // проверяем если истории нет, то...
            return Collections.singletonList("У вас еще нет истории сообщений"); // Выводим сообщение: нет истории сообщений.
        List<String> result = null;
        if (history.exists()) { //если история имеется, то...
            try (BufferedReader reader = new BufferedReader(new FileReader(history))) { // Читаем наш файл
                                                                             // методом BufferedReader/экономит ресурсы
                String historyString;
                List<String> historyStringUser = new ArrayList<>();
                //имеется в виду, что сообщение занимает не больше 1 строки
                while ((historyString = reader.readLine()) != null) { //чтение файла происходит построчно,
                    historyStringUser.add(historyString); //add - добавляем результат
                }
                if (historyStringUser.size() <= SIZE_OF_RETRIEVED_HISTORY) { //если в сообщении меньше 100 строк, то...
                    result = historyStringUser; // Все их и выводим.
                }//если в сообщении больше 100 строк, то обрезаем
                if (historyStringUser.size() > SIZE_OF_RETRIEVED_HISTORY) { //если в сообщении больше 100 строк, то...
                    int firstIndex = historyStringUser.size() - SIZE_OF_RETRIEVED_HISTORY; // обрезаем историю
                    result = new ArrayList<>(SIZE_OF_RETRIEVED_HISTORY);

                    for (int counter = firstIndex - 1; counter < historyStringUser.size(); counter++) {
                        result.add(historyStringUser.get(counter));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("История сообщений для: " + login + " - " + result.size() + " строк" );
        return result;
    }
    //метод для записи истории
    public void writeHistory(String message) { // Метод сохранения истории сообщения в текстовый файл.
                                               // Контроллер передает сообщение, и записываем в файл
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(history, true))) { // Добавляем
            // историю пользователя в текстовый файл. Флаг true - не перезатирает записи сообщений при последующих сессиях.
            writer.write(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}