package ru.geekbrains.january_chat.chat_server.error;

public class ChangeNickException extends RuntimeException {

    public ChangeNickException(String message){
        super(message);
    }
}