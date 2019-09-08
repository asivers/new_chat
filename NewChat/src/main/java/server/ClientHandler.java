package server;

import java.io.FileReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private Server server;
    private PrintWriter outMsg;
    private Scanner inMsg;

    //база данных хранится на стороне сервера
    private String login;
    private String password;

    private FileReader historyReader = null;
    private Scanner historyScanner = null;

    public ClientHandler(Socket clientSocket, Server server) {
        try {
            this.clientSocket = clientSocket;
            this.server = server;
            this.outMsg = new PrintWriter(clientSocket.getOutputStream());
            this.inMsg = new Scanner(clientSocket.getInputStream());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while ((login == null) || (password == null)) {
                if (inMsg.hasNext()) {
                    String type = inMsg.nextLine();
                    login = inMsg.nextLine();
                    password = inMsg.nextLine();
                    //если выбран вход (sign in)
                    if (type.equals("$OLD$")) {
                        //проверяем, есть ли пользователь в базе данных (совпадают ли логин и пароль)
                        if (SQL.doesUserExist(login, password)) {
                            server.newClientLog(login);
                            outMsg.println("good");
                            outMsg.flush();
                        } else {
                            server.wrongPassword(login);
                            outMsg.println("bad");
                            outMsg.flush();
                            login = null;
                            password = null;
                        }
                    }
                    //если выбрана регистрация (sign up)
                    if (type.equals("$NEW$")) {
                        //проверяем, занят ли введенный логин другим пользователем
                        if (!(SQL.createNewUser(login, password))) {
                            server.regClientLog(login);
                            outMsg.println("good");
                            outMsg.flush();
                        } else {
                            server.wrongReg(login);
                            outMsg.println("bad");
                            outMsg.flush();
                            login = null;
                            password = null;
                        }
                    }
                }
            }

            //для получения 100 последних сообщений из истории при входе в чат
            historyReader = new FileReader("history.txt");
            historyScanner = new Scanner(historyReader);
            int msgNumber = server.getMsgNumber();
            if (msgNumber > 0) {
                for (int i = 1; i <= msgNumber; i++) {
                    String line = historyScanner.nextLine();
                    if (i > msgNumber - 100)
                        sendMessage(line);
                }
            }
            historyReader.close();
            historyScanner.close();

            //отправка сообщений в основное окно
            while (true)
                if (inMsg.hasNext()) {
                    String newLine = inMsg.nextLine();
                    server.notification(newLine);
                }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    //метод для отправки сообщений
    public void sendMessage(String msg) {
        try {
            outMsg.println(msg);
            outMsg.flush();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
