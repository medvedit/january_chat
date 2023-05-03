package ru.geekbrains.january_chat.chat_server.auth.db;

import ru.geekbrains.january_chat.chat_server.error.ChangeNickException;
import ru.geekbrains.january_chat.chat_server.error.WrongCredentialsException;

import java.sql.*;
//класс отвечающий за работу с БД
public class ClientsDatabaseService {
    private static final String DRIVER =
            "org.sqlite.JDBC";
    private static final String CONNECTION =
            "jdbc:sqlite:db/clients.db";
    private static final String GET_USERNAME =
            "select username from clients where login = ? and password = ?;";
    private static final String CHANGE_USERNAME =
            "update clients set username = ? where login =?;";
    private static final String CREATE_DB =
            "CREATE TABLE IF NOT EXISTS clients (id    INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " login text unique not null, password text not null, username text unique not null);";



    private static final String INIT_DB =
            "insert into clients (login, password, username) values ('log1', 'pass1', 'user1'); ";



    private static ClientsDatabaseService instance;
    private Connection connection;
    PreparedStatement getClientStatement;
    PreparedStatement changeNickStatement;

    private ClientsDatabaseService(){
        try {
            connect();
        }catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
        } createDb();
    }



    public static ClientsDatabaseService getInstance(){
        if (instance != null) return instance;
        instance = new ClientsDatabaseService();
        return instance;
    }

    public String changeNick(String login, String newNick) {
        try {
            changeNickStatement.setString(1, newNick);
            changeNickStatement.setString(2, login);
            if (changeNickStatement.executeUpdate() > 0) return newNick;//если все успешно возвращаем новый ник
        } catch (SQLException e){
            e.printStackTrace();
        }throw  new ChangeNickException("что то пошло не так...");
    }




    //авторизация, используем PreparedStatement- для безопастности
    public String getClientsNameByLoginPass(String login, String pass) {//подключение к БД из таб. клиентов строка юзеров
        try (PreparedStatement ps = connection.prepareStatement(GET_USERNAME))
        {
            getClientStatement.setString(1, login);//отправка логина и пароля
            getClientStatement.setString(2, pass);
            ResultSet rs = getClientStatement.executeQuery();//запрос отправляется методом executeQuery()


            if(rs.next()){//смотрим запись которая нам пришла
                String result = rs.getString("username");//если получили , то присваиваем ее в результат
                rs.close();
                System.out.printf("login is: %s\n", result);
                return result;
            }

        }catch (SQLException e){
            e.printStackTrace();//если что то не так то выводится исключение
        }throw new WrongCredentialsException();//если запрос в БД не дал результатов, то выбрасываем исключение что ни чего не найдено
    }

    private void connect()throws ClassNotFoundException, SQLException{
        Class.forName(DRIVER);
        connection = DriverManager.getConnection(CONNECTION);
        System.out.println("Соединение с БД");
        getClientStatement = connection.prepareStatement(GET_USERNAME);//его мы получаем из connection.prepareStatement(GET_USERNAME)
        changeNickStatement = connection.prepareStatement(CHANGE_USERNAME);//его мы получаем из  connection.prepareStatement(CHANGE_USERNAME);

    }
    //возможность создания БД
    private void createDb(){
        try
                (Statement st = connection.createStatement()){
            st.execute(CREATE_DB);
            st.execute(INIT_DB);

        }catch (SQLException e){
            e.printStackTrace();
        }

    }
    //метод закрывающий соединения
    public void closeConnection() {
        try {
            if (getClientStatement != null)
                getClientStatement.close();//не 0 закрываем
            if (changeNickStatement != null)
                changeNickStatement.close();//закрываем
            System.out.println("Нет соединения с БД");
        }catch (SQLException e)
        {e.printStackTrace();}
    }
}