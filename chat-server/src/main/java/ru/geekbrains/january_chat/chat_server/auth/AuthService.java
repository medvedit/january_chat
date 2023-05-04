package ru.geekbrains.january_chat.chat_server.auth;

import ru.geekbrains.january_chat.chat_server.entity.User;
//интерфейс описывает как нужно работать серверу, авторизация и т.д.
public interface  AuthService {
    void start();
    void stop();
    String authorizeUserByLoginAndPassword(String login, String password);
    String changeNick(String login, String newNick);
    User createNewUser(String login, String password, String nick);
    void deleteUser(String login, String pass);
    void changePassword(String login, String oldPass, String newPass);
    void resetPassword(String login, String newPass, String secret);
}

/*

//сервис - авторизации - хранит список подключенных клиентов,
// предназначенный для управления соединением с клиентом и рассылкой сообщений.
//Обмен сообщений в виде строк
public interface AuthService {

    void start();//методы старт и стоп
    void stop();

    //метод возвращает String, ожидаем логин и пароль
    String authorizeUserByLoginAndPassword(String login, String password);

    //метод меняющий ник, запрашиваем новый ник
    String changeNick(String login, String newNick);

    //новый ожидаем данные
    User createNewUser(String login, String password, String nick);

    //удаление по логину
    void deleteUser(String login, String pass);

    //
    void changePassword(String login, String oldPass, String newPass);

    //смена пароля запрос логина пароля....секретное слово
    void resetPassword(String login, String newPass, String secret);
}*/
