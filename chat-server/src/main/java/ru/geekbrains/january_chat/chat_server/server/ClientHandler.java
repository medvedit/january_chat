package ru.geekbrains.january_chat.chat_server.server;

import ru.geekbrains.january_chat.chat_server.error.WrongCredentialsException;
import ru.geekbrains.january_chat.props.PropertyReader;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class ClientHandler {
    private final long authTimeout; // + поле таймаут по авторизации
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private Thread handlerThread;
    private Server server;
    private String user;

    public ClientHandler(Socket socket, Server server) {
        authTimeout = PropertyReader.getInstance().getAuthTimeout();// +  authTimeout = последовательность методов
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            System.out.println("Handler created");
        } catch (IOException e) {
            System.out.println("Connection broken with user " + user);
        }
    }

    public void handle() {
//        handlerThread = new Thread(() -> { //  удалил после добавления ExecutorService в chat_server.server.server
        server.getExecutorService().execute(() -> {
            authorize();
            while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                try {
                    var message = in.readUTF();
                    handleMessage(message);
                } catch (IOException e) {
                    System.out.println("Connection broken with user " + user);
                    server.removeAuthorizedClientFromList(this);
                }
            }
        });
//        handlerThread.start(); // соответственно при использовании newCachedThreadPool -> .start() не нужен
    }

    private void handleMessage(String message) {
        var splitMessage = message.split(Server.REGEX);
        try {
            switch (splitMessage[0]) {
                case "/w":
                    server.privateMessage(this.user, splitMessage[1], splitMessage[2], this);
                    break;
                case "/broadcast":
                    server.broadcastMessage(user, splitMessage[1]);
                    break;
                case "/change_nick":
                    String nick = server.getAuthService().changeNick(this.user, splitMessage[1]);
                    server.removeAuthorizedClientFromList(this);
                    this.user = nick;
                    server.addAuthorizedClientToList(this);
                    send("/change_nick_ok");
                    break;
                case "/change_pass":
                    server.getAuthService().changePassword(this.user, splitMessage[1], splitMessage[2]);
                    send("/change_pass_ok");
                    break;
                case "/remove":
                    server.getAuthService().deleteUser(splitMessage[1], splitMessage[2]);
                    this.socket.close();
                    break;
                case "/register":
                    server.getAuthService().createNewUser(splitMessage[1], splitMessage[2], splitMessage[3]);
                    send("register_ok:");
                    break;
            }
        } catch (IOException e) {
            send("/error" + Server.REGEX + e.getMessage());
        }
    }

    private void authorize() {
        System.out.println("Authorizing");
        var timer = new Timer(true);//присваивается ссылка на Timer, поток не демон
        timer.schedule(new TimerTask() {
            @Override
            public void run() { // timer- вызывает -schedule- выполняется указанная задача в определенное время
                try {
                    if (user == null) {//если user не авторизовался = 0, то отправляем сообщение об ошибке
                        send("/error" + Server.REGEX + "Authentication timeout!\nPlease, try again later!");
                        Thread.sleep(50);//даем немного времени
                        socket.close();//отключаем соединение
                        System.out.println("Connection with client closed");//выдаем сообщение
                    }
                } catch (InterruptedException | IOException e) {
                    e.getStackTrace();//ловим исключение
                }
            }
        }, authTimeout);//возвращаем значение
        try {
            while (!socket.isClosed()) {
                var message = in.readUTF();
                if (message.startsWith("/auth")) {
                    var parsedAuthMessage = message.split(Server.REGEX);
                    var response = "";
                    String nickname = null;
                    try {
                        nickname = server.getAuthService().authorizeUserByLoginAndPassword(parsedAuthMessage[1], parsedAuthMessage[2]);
                    } catch (WrongCredentialsException e) {
                        response = "/error" + Server.REGEX + e.getMessage();
                        System.out.println("Wrong credentials, nick " + parsedAuthMessage[1]);
                    }

                    if (server.isNickBusy(nickname)) {
                        response = "/error" + Server.REGEX + "this client already connected";
                        System.out.println("Nick busy " + nickname);
                    }
                    if (!response.equals("")) {
                        send(response);
                    } else {
                        this.user = nickname;
                        server.addAuthorizedClientToList(this);
                        send("/auth_ok" + Server.REGEX + nickname);
                        break;
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Thread getHandlerThread() {
        return handlerThread;
    }

    public String getUserNick() {
        return this.user;
    }
}
/*import january_chat.chat_server.error.WrongCredentialsException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private Thread handlerThread;
    private Server server;
    private String user;

    //запускает отдельный поток для работы с клиентом
    public ClientHandler(Socket socket, Server server) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());//DataInputStream для удобства отправления джава примитивов и записявает строку в виде байт и считываем
            this.out = new DataOutputStream(socket.getOutputStream());//DataOutputStream-своего рода обертка потоков ввода и вывода
            System.out.println("Handler created / Обработчик создан ");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //обработка/
    public void handle() {
        handlerThread = new Thread(() -> {
            authorize();
            while (!Thread.currentThread().isInterrupted() && socket.isConnected()) {
                try {
                    var message = in.readUTF();//считываем строку в виде байт
                    handleMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                   // System.out.println("Connection broken with user " + user);
                   server.removeAuthorizedClientToList(this);
                }
            }
        });
        handlerThread.start();
    }

    //получаем все сообщения и обрабатываем
    private void handleMessage(String message) {
        var splitMessage = message.split(Server.REGEX);
        switch (splitMessage[0]) {

        //если мы получили "/w" . то у сервера вызываем метод privateMessage
          case "/w":
              server.privateMessage(this.user, splitMessage[1], splitMessage[2], this);
              break;
            case "/broadcast":
                server.broadcastMessage(user, splitMessage[1]);
                break;
      }
            }






    //обработка/когда подключился клиент мы его должны авторизовать
    private void authorize() {
        System.out.println("Authorizing");
        while (true) {
            try {
                var message = in.readUTF();
                if (message.startsWith("/auth")) {
                    var parsedAuthMessage = message.split(Server.REGEX);
                    var response = "";
                    String nickname = null;
                    try {
                        nickname = server.getAuthService().authorizeUserByLoginAndPassword(parsedAuthMessage[1], parsedAuthMessage[2]);
                    } catch (WrongCredentialsException e) {
                        response = "/error" + Server.REGEX + e.getMessage();
                        System.out.println("Wrong credentials, nick / не верные данные " + parsedAuthMessage[1]);
                    }

                    //отправка ошибки если ник занят
                    if (server.isNickBusy(nickname)) {
                        response = "/error" + Server.REGEX + "this client already connected";
                        System.out.println("Nick busy " + nickname);
                    }
                    if (!response.equals("")) {
                        send(response);
                    } else {
                        //если все хорошо то
                        this.user = nickname;
                        server.addAuthorizedClientToList(this);
                        send("/auth_ok" + Server.REGEX + nickname);
                        break;//прерывание авторизации
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();//выводит полную информацию об исключении в консоль
            }
        }
    }

    //отправка сообщения в виде строки
    public void send(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();//выводит полную информацию об исключении в консоль
        }
    }

    public Thread getHandlerThread() {
        return handlerThread;
    }

    public String getUserNick() {
        return this.user;
    }
}

/*import java.io.DataInputStream;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private static int clientCounter = 0;//нумерация клиентов для их идентификации/ счетчик статика, новый клиент с новым номером
    private int clientNumber;//по номеру
    private Socket socket;//поле мы получаем из сервера, который создает объект Handler на каждого подключившегося клиента
    private DataOutputStream out;//потоки ввода вывода
    private DataInputStream in;
    private Thread handlerThread;
    private Server server;//ссылка на сервер

    //когда сервер создает обработчика
    public ClientHandler(Socket socket, Server server) { //ссылка на сервер, для отправки сообщений
        try {
            this.server = server;//передается ссылка на самого себя/что бы были сообщения
            this.socket = socket;//передается серверам
            this.in = new DataInputStream(socket.getInputStream());//из socket - достаем потоки вв и выв
            this.out = new DataOutputStream(socket.getOutputStream());
            System.out.println("Handler created");
            this.clientNumber = ++clientCounter;//обновляем номер клиента
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //метод обработки сообщений
    public void handle() {
        handlerThread = new Thread(() -> { //запуск в отдельном потоке
            while (!Thread.currentThread().isInterrupted() && socket.isConnected()) {
                //слушает входящее сообщение от своего клиента
                try {
                    String message = in.readUTF();
                    message = "client #" + this.clientNumber + ": " + message;//форматирование сообщения
                    server.broadcast(message);// у сервера вызываем метод для отправки сообщения всем
                    System.out.printf("Client #%d: %s\n", this.clientNumber, message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        handlerThread.start();//запуск потока
    }

    //метод, который сервер у каждого Handler- вызывает у обработчика
    public void send(String msg) {
        try {
            out.writeUTF(msg);//запись сообщения и отправка его в сеть
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Thread getHandlerThread() {
        return handlerThread;
    }
}*/