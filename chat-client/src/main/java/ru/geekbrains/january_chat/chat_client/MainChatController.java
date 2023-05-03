package ru.geekbrains.january_chat.chat_client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import ru.geekbrains.january_chat.chat_client.network.MessageProcessor;
import ru.geekbrains.january_chat.chat_client.network.NetworkService;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.List;
public class MainChatController implements Initializable, MessageProcessor {
    public static final String REGEX = "%!%";

    private String nick;
    private NetworkService networkService;
    //добавляем поле
    private HistoryMaker historyMaker;

    @FXML
    private VBox changeNickPanel;

    @FXML
    private TextField newNickField;

    @FXML
    private VBox changePasswordPanel;

    @FXML
    private PasswordField oldPassField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private VBox loginPanel;

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private VBox mainChatPanel;

    @FXML
    private TextArea mainChatArea;

    @FXML
    private ListView contactList;

    @FXML
    private TextField inputField;

    @FXML
    private Button btnSend;

    public void connectToServer(ActionEvent actionEvent) {
    }

    public void disconnectFromServer(ActionEvent actionEvent) {
    }

    public void mockAction(ActionEvent actionEvent) {
    }

    public void exit(ActionEvent actionEvent) {
        System.exit(1);
    }

    public void showHelp(ActionEvent actionEvent) {
    }

    public void showAbout(ActionEvent actionEvent) {
    }

    public void sendMessage(ActionEvent actionEvent) {
        var message = inputField.getText();
        if (message.isBlank()) {
            return;
        }
        var recipient = contactList.getSelectionModel().getSelectedItem();
        if (!recipient.equals("ALL")) {
            networkService.sendMessage("/w" + REGEX + recipient + REGEX + message);
        } else {
            networkService.sendMessage("/broadcast" + REGEX + message);
        }
        inputField.clear();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.networkService = new NetworkService(this);
    }

    @Override
    public void processMessage(String message) {
        Platform.runLater(() -> parseIncomingMessage(message));
    }

    private void parseIncomingMessage(String message) {
        var splitMessage = message.split(REGEX);
        switch (splitMessage[0]) {
            case "/auth_ok":
                this.nick = splitMessage[1];
                loginPanel.setVisible(false);
                mainChatPanel.setVisible(true);
                //инициализация historyMaker после того как клиент успешно авторизовался
                //так как имя файла истории = логину клиента
                this.historyMaker = new HistoryMaker(nick);
                //получаем прошлую историю
                var history = historyMaker.readHistory();
                for (String s : history) {
                    //добавляем истории для просмотра клиенту
                    mainChatArea.appendText(s + System.lineSeparator());
                }
                break;
            case "/error":
                showError(splitMessage[1]);
                System.out.println("got error " + splitMessage[1]);
                break;
            case "/list":
                var contacts = new ArrayList<String>();
                contacts.add("ALL");
                for (int i = 1; i < splitMessage.length; i++) {
                    contacts.add(splitMessage[i]);
                }
                contactList.setItems(FXCollections.observableList(contacts));
                contactList.getSelectionModel().selectFirst();
                break;
            case "/change_pass_ok":
                changePasswordPanel.setVisible(false);
                mainChatPanel.setVisible(true);
                break;
            default:
                mainChatArea.appendText(splitMessage[0] + System.lineSeparator());
                //записываем сообщение
                historyMaker.writeHistory(splitMessage[0] + System.lineSeparator());

                break;
        }
    }

    public void sendChangeNick(ActionEvent actionEvent) {
        if (newNickField.getText().isBlank()) return;
        networkService.sendMessage("/change_nick" + REGEX + newNickField.getText());
    }

    public void sendChangePass(ActionEvent actionEvent) {
        if (newPasswordField.getText().isBlank() || oldPassField.getText().isBlank()) return;
        networkService.sendMessage("/change_pass" + REGEX + oldPassField.getText() + REGEX + newPasswordField.getText());
    }

    public void sendEternalLogout(ActionEvent actionEvent) {
        networkService.sendMessage("/remove");
    }

    private void showError(String message) {
        var alert = new Alert(Alert.AlertType.ERROR,
                "An error occurred: " + message,
                ButtonType.OK);
        alert.showAndWait();
    }

    public void sendAuth(ActionEvent actionEvent) {
        var login = loginField.getText();
        var password = passwordField.getText();
        if (login.isBlank() || password.isBlank()) {
            return;
        }

        var message = "/auth" + REGEX + login + REGEX + password;

        if (!networkService.isConnected()) {
            try {
                networkService.connect();
            } catch (IOException e) {
                e.printStackTrace();
                showError(e.getMessage());

            }
        }

        networkService.sendMessage(message);
    }

    public void returnToChat(ActionEvent actionEvent) {
        changeNickPanel.setVisible(false);
        changePasswordPanel.setVisible(false);
        mainChatPanel.setVisible(true);
    }

    public void showChangeNick(ActionEvent actionEvent) {
        mainChatPanel.setVisible(false);
        changeNickPanel.setVisible(true);
    }

    public void showChangePass(ActionEvent actionEvent) {
        mainChatPanel.setVisible(false);
        changePasswordPanel.setVisible(true);
    }
}

/*//управляет содержимым
public class MainChatController implements Initializable, MessageProcessor {
    public static final String REGEX = "%!%";

    private String nick;
    private NetworkService networkService;

    @FXML
    public VBox loginPanel;

    @FXML
    public TextField loginField;

    @FXML
    public PasswordField passwordField;

    @FXML
    public VBox mainChatPanel;

    @FXML
    public TextArea mainChatArea;
    @FXML
    public ListView contactList;
    @FXML
    public TextField inputField;
    @FXML
    public Button btnSend;

    public void connectToServer(ActionEvent actionEvent) {
    }
    public void disconnectFromServer(ActionEvent actionEvent) {
    }

    public void mockAction(ActionEvent actionEvent) {
    }
    public void exit(ActionEvent actionEvent) {
        System.exit(1);
    }
    public void showHelp(ActionEvent actionEvent) {
    }
    public void showAbout(ActionEvent actionEvent) {
    }
/*
    public void sendMessage(ActionEvent actionEvent) {
        var message = inputField.getText();
        if (message.isBlank()) {
            return;
        }
        //получаем выбранный элемент -getSelectedItem()  из contactList
        var recipient = contactList.getSelectionModel().getSelectedItem();
    //mainChatArea.appendText(recipient + ": " + message + System.lineSeparator());
        //если выбранные не все, то можно послать приватное сообщение
        if (!recipient.equals("ALL")){ // отправляем с приставкой "/w" +  разделитель + получатель + разделитель + сообщение
            networkService.sendMessage("/w" + REGEX + recipient + REGEX + message);
            //в ином случае : отправляем с приставкой "/broadcast" и тд
               }else {
                networkService.sendMessage("/broadcast" + REGEX  + message);}
                inputField.clear();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {


     this.networkService = new NetworkService(this);
}

    @Override
    public void processMessage(String message) {
        Platform.runLater(() -> parseIncomingMessage(message));
    }

    private void parseIncomingMessage(String message) {
        var splitMessage = message.split(REGEX);
        switch (splitMessage[0]) {
            //вариант кейсов/константы
            case "/auth_ok" :
                this.nick = splitMessage[1];
                loginPanel.setVisible(false);
                mainChatPanel.setVisible(true);
                break;
            case "/broadcast" :
                mainChatArea.appendText(splitMessage[1] + ": " + splitMessage[2] + System.lineSeparator());
                break;
            case "/error" :
                showError(splitMessage[1]);
                System.out.println("got error " + splitMessage[1]);
                break;
                //список контактов
            case "/list" :
                //создаем contacts /добавляем
                var contacts = new ArrayList<String>();
                contacts.add("ALL");
                for (int i = 1; i < splitMessage.length; i++) {
                    contacts.add(splitMessage[i]);
                }
                contactList.setItems(FXCollections.observableList(contacts));
                break;
        }
    }

    private void showError(String message) {
        var alert = new Alert(Alert.AlertType.ERROR,
                "An error occurred: " + message,
                ButtonType.OK);
        alert.showAndWait();
    }

    //отправка авторизации
    public void sendAuth(ActionEvent actionEvent) {
        //полученный логин из getText();
        var login = loginField.getText();
        var password = passwordField.getText();
        //если логин или пароль из бланка то
        if (login.isBlank() || password.isBlank()) {
            return;
        }

        var message = "/auth" + REGEX + login + REGEX + password;

        //
        if (!networkService.isConnected()) {
            try {
                networkService.connect();
            } catch (IOException e) {
                e.printStackTrace();
                showError(e.getMessage());

            }
        }
        networkService.sendMessage(message);//подключаемся и шлем авторизацию
    }

    public void mockAktion(ActionEvent actionEvent) {
    }
}*/

/*public class MainChatController implements Initializable {

    @FXML
    public VBox mainChatPanel;
    @FXML
    public TextArea mainChatArea;
    @FXML
    public ListView contactList;
    @FXML
    public TextField inputField;
    @FXML
    public Button btnSend;

    public void connectToServer(ActionEvent actionEvent) {
    }

    public void disconnectFromServer(ActionEvent actionEvent) {
    }

    public void mockAktion(ActionEvent actionEvent) {
    }

    public void exit(ActionEvent actionEvent) {
        System.exit(1);//код выхода
    }

    public void showHelp(ActionEvent actionEvent) {
    }

    public void showAbout(ActionEvent actionEvent) {
    }

    public void sendMessage(ActionEvent actionEvent) {
        var message  = inputField.getText();
        //если пустое сообщение то
        if (message.isBlank()){
            return;//ни чего не происходит
        }

        //но если есть что то, он не пустой то дастаем из контакт листа ListView, через
        //getSelectionModel() достаем выбранный объект
        //далее добавляем этот объект recipient + а так же сообщение
               var recipient = contactList.getSelectionModel().getSelectedItem();
        mainChatArea.appendText(recipient + ":\n" + message  + System.lineSeparator());
        //и очистим поле ввода после ввода
        inputField.clear();


    }

    @Override     //выполняется на старте контроллера, заполняется..
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //на старте приложения вложем фейковый список контактов
        var contacts = new ArrayList<String>();
        contacts.add("ALL:");    //выбор всех контактов
        for (int i = 0; i < 10; i ++) {
            contacts.add("Контакт#" + (i + 1) );
        }
        //добавим список в
        contactList.setItems(FXCollections.observableList(contacts));
        //выбор из списка
        contactList.getSelectionModel().selectFirst();
        //contactList.getSelectionModel().
    }
}
*/