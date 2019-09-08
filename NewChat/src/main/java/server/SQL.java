package server;

import java.sql.*;

public class SQL {

    //метод для проверки, есть ли пользователь в базе данных (совпадают ли логин и пароль)
    public static boolean doesUserExist(String login, String password) {
        try {
            boolean doesUserExist = false;
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:h2:~/logpass.db");
            Statement statement = connection.createStatement();
            statement.execute("create table if not exists user(" +
                    "id integer primary key auto_increment, " +
                    "login varchar(100), " +
                    "password varchar(100));");
            ResultSet rs = statement.executeQuery("select * from user");
            while (rs.next()) {
                if ((rs.getString("login").equals(login)) && (rs.getString("password").equals(password))) {
                    doesUserExist = true;
                    break;
                }
            }
            connection.commit();
            connection.close();
            statement.close();
            return doesUserExist;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //метод для проверки, занят ли введенный логин другим пользователем
    public static boolean createNewUser(String login, String password) {
        try {
            boolean isLoginUsed = false;
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:h2:~/logpass.db");
            Statement statement = connection.createStatement();
            statement.execute("create table if not exists user(" +
                    "id integer primary key auto_increment, " +
                    "login varchar(100), " +
                    "password varchar(100));");
            ResultSet rs = statement.executeQuery("select * from user");
            while (rs.next()) {
                if (rs.getString("login").equals(login)) {
                    isLoginUsed = true;
                    break;
                }
            }
            if (!isLoginUsed) {
                PreparedStatement pStatement = connection.prepareStatement("insert into user(login,password) values(?,?)");
                pStatement.setString(1, login);
                pStatement.setString(2, password);
                pStatement.addBatch();
                pStatement.executeBatch();
                pStatement.close();
            }
            connection.commit();
            connection.close();
            statement.close();
            return isLoginUsed;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
