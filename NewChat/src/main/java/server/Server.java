package server;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;


public class Server {

    private static final Logger log = Logger.getLogger(Server.class);

    private List<ClientHandler> clientHandlers = new ArrayList<ClientHandler>();

    private FileWriter historyWriter = null;
    private int msgNumber = 0;

    public Server() {

        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        PrintWriter cleaner = null;

        try {
            serverSocket = new ServerSocket(8888);
            System.out.println("Server launched");
            launchLog();

            //очищаем файл истории сообщений при запуске сервера
            cleaner = new PrintWriter("history.txt");
            cleaner.print("");
            historyWriter = new FileWriter("history.txt", true);

            while (true) {
                clientSocket = serverSocket.accept();
                ClientHandler client = new ClientHandler(clientSocket, this);
                clientHandlers.add(client);

                //управление потоками через ExecutorService
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.execute(client);
                executorService.shutdown();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                serverSocket.close();
                clientSocket.close();
                System.out.println("Server finished");

                historyWriter.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //метод для отправки сообщений всем пользователям
    public void notification(String msg) {
        try {
            //записываем в историю
            historyWriter.write(msg + '\n');
            historyWriter.flush();
            msgNumber++;
            newMsgLog(msg);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        //собственно рассылка
        for (ClientHandler clientHandler : clientHandlers)
            clientHandler.sendMessage(msg);
    }

    public void removeClient(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
    }

    public int getMsgNumber() {
        return msgNumber;
    }

    //логирование
    public void launchLog() { log.info("Server launched"); }
    public void newMsgLog(String s) { log.info("Новое сообщение: " + s); }
    public void newClientLog(String s) { log.info("Новый клиент \" + s + \" подключился к чату"); }
    public void regClientLog(String s) { log.info("Новый клиент \" + s + \" зарегистрировался в системе"); }
    public void wrongPassword(String s) { log.error("Неудачная попытка входа в систему под именем" + s); }
    public void wrongReg(String s) { log.error("Неудачная попытка регистрации в системе под именем" + s); }

}