package ru.geekbrains.january_chat.chat_client.network;

import ru.geekbrains.january_chat.props.PropertyReader;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class NetworkService {
    private final String host;
    private final int port;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private MessageProcessor messageProcessor;

    public NetworkService(MessageProcessor messageProcessor) {
        host = PropertyReader.getInstance().getHost();
        port = PropertyReader.getInstance().getPort();
        this.messageProcessor = messageProcessor;
    }

    public void connect() throws IOException {
        this.socket = new Socket(host, port);
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        readMessages();
    }

    public void readMessages() {
        var thread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                    var message = in.readUTF();
                    messageProcessor.processMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        // thread.setDaemon(true);
        thread.start();
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return socket != null && !socket.isClosed();
    }

    public void close() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}




/*
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;




//сервер отвечающий за работу сети/подключение к серверу
//читает первоначальные сообщения/и отдаем контроллеру обработку сообщения
public class NetworkService {
    private  final String HOST = "127.0.0.1";
    private  final int PORT = 8189;
    private Socket socket;//абстракция описывающая связь сервера и клиента в нашем случае
    private DataInputStream in;
    private DataOutputStream out;
    private MessageProcessor messageProcessor;

    public NetworkService(MessageProcessor messageProcessor)  {

        //HOST = PropertyReader.getInstance().getHost();
        //PORT = PropertyReader.getInstance().getPort();
        //создаем
        this.messageProcessor = messageProcessor;
    }

    //создает соединение сокет, он не слушает порт а просто говорит о запросе , соединие в сервер сокет, создается сокет
    public void connect() throws IOException {
        this.socket = new Socket(HOST, PORT);
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        readMessages();
    }

    //читаем сообщение в отдельном потоке
    public void readMessages() {
        var thread = new Thread(() -> {
            try {
                //когда было полученно сообщение мы его отдаем messageProcessor
                while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                    var message = in.readUTF();
                    messageProcessor.processMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);//наш поток - демон
        thread.start();
    }

    //отправка сообщений
    public void sendMessage(String message) {

        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public boolean isConnected() {
        return socket != null && !socket.isClosed();
    }

    //метод закрытие
    public void close() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
*/