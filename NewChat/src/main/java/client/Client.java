package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

public class Client extends JFrame {

    //переменные для связи с сервером
    private final String SERVER_HOST = "localhost";
    private final int SERVER_PORT = 8888;
    private Socket clientSocket;
    private Scanner inMsg;
    private PrintWriter outMsg;

    //переменные java swing - основное окно
    private JTextField textFieldMsg;
    private JTextArea textAreaMsg;
    private JScrollPane scrollPane;
    private JPanel mainPanel;
    private JPanel bottomPanel;
    private JLabel topLabel;
    private JButton sendButton;

    //переменные java swing - всплывающее окно регистрации
    private JOptionPane optionPane1;
    private JOptionPane optionPane2;
    private JPanel dialogPanel;
    private JTextField login;
    private JTextField password;

    Date date;

    public Client() throws HeadlessException {
        try {
            clientSocket = new Socket(SERVER_HOST, SERVER_PORT);
            inMsg = new Scanner(clientSocket.getInputStream());
            outMsg = new PrintWriter(clientSocket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }

        //задаем основное окно
        setBounds(500, 300, 500, 400);
        setTitle("Test Chat");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        mainPanel = new JPanel(new BorderLayout());
        add(mainPanel);

        date = new Date();
        topLabel = new JLabel("Connection time: " + date.toString());
        mainPanel.add(topLabel, BorderLayout.NORTH);

        textAreaMsg = new JTextArea();
        textAreaMsg.setEditable(false);
        textAreaMsg.setLineWrap(true);
        scrollPane = new JScrollPane(textAreaMsg);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        bottomPanel = new JPanel(new BorderLayout());
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        sendButton = new JButton("SEND");
        getRootPane().setDefaultButton(sendButton);
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String msg = textFieldMsg.getText().trim();
                //отправка сообщения
                if (!msg.isEmpty()) {
                    sendMsg();
                    textFieldMsg.grabFocus();
                }
            }
        });
        bottomPanel.add(sendButton, BorderLayout.EAST);

        textFieldMsg = new JTextField("Please input your message");
        textFieldMsg.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                textFieldMsg.setText("");
            }
        });
        bottomPanel.add(textFieldMsg, BorderLayout.CENTER);

        setVisible(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                //рекурсивный метод входа или регистрации
                popUp();

                //получение и вывод сообщений в окно
                while (true)
                    if (inMsg.hasNext())
                        textAreaMsg.append(inMsg.nextLine() + "\n");
            }
        }).start();

        //закрываем сокет при закрытии окна
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                outMsg.close();
                inMsg.close();
                try {
                    clientSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    //метод для отправки сообщений в основное окно
    private void sendMsg() {
        String msg = "(" + new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) + ") " + login.getText() + ":    " + textFieldMsg.getText();
        outMsg.println(msg);
        outMsg.flush();
        textFieldMsg.setText("");
    }

    public String getLogin() {
        return login.getText();
    }

    public String getPassword() {
        return password.getText();
    }

    //всплывающее окно регистрации или входа
    public void popUp() {
        optionPane1 = new JOptionPane();
        optionPane2 = new JOptionPane();

        dialogPanel = new JPanel(new GridLayout(2,1,0,10));

        login = new JTextField("Login");
        login.addMouseListener(new MouseAdapter() {
            boolean m = false;
            @Override
            public void mouseClicked(MouseEvent e) {
                if ((m == false) && (login.getText().equals("Login"))) {
                    login.setText("");
                    m = true;
                }
            }
        });
        login.addFocusListener(new FocusAdapter() {
            int m = 0;
            @Override
            public void focusGained(FocusEvent e) {
                if ((m == 1) && (login.getText().equals("Login"))) {
                    login.setText("");
                    m++;
                }
                if (m == 0)
                    m++;
            }
        });
        login.addKeyListener(new KeyAdapter() {
            boolean m = false;
            @Override
            public void keyTyped(KeyEvent e) {
                if (m == false) {
                    login.setText("");
                    m = true;
                }
            }
        });
        dialogPanel.add(login);

        password = new JTextField("Password");
        password.addFocusListener(new FocusAdapter() {
            boolean m = false;
            @Override
            public void focusGained(FocusEvent e) {
                if (m == false) {
                    password.setText("");
                    m = true;
                }
            }
        });
        dialogPanel.add(password);

        String[] options = { "Sign In", "Sign Up" };
        int a = JOptionPane.showOptionDialog(optionPane1, dialogPanel, "Hello", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, null);
        if (a == -1)
            this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));

        //если выбран вход (sign in)
        if (a == 0) {
            //если логин или пароль не введены
            if ((login.getText().equals("")) || (login.getText().equals("Login")) || (password.getText().equals("")) || (password.getText().equals("Password"))) {
                int b = JOptionPane.showOptionDialog(optionPane2, "Incorrect login or password", "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
                if (b == -1)
                    this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
                if (b == 0) {
                    popUp();
                }
            }
            //если пользователя нет в базе данных (не совпадают логин и пароль)
            else {
                outMsg.println("$OLD$");
                outMsg.flush();
                outMsg.println(login.getText());
                outMsg.flush();
                outMsg.println(password.getText());
                outMsg.flush();
                boolean doesUserExist = inMsg.nextLine().equals("good");
                if (doesUserExist)
                    JOptionPane.showMessageDialog(optionPane2, "Welcome to the chat");
                else {
                    int b = JOptionPane.showOptionDialog(optionPane2, "Incorrect login or password", "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
                    if (b == -1)
                        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
                    if (b == 0) {
                        popUp();
                    }
                }
            }
        }

        //если выбрана регистрация (sign up)
        if (a == 1) {
            //если логин или пароль не введены
            if ((login.getText().equals("")) || (login.getText().equals("Login")) || (password.getText().equals("")) || (password.getText().equals("Password"))) {
                int b = JOptionPane.showOptionDialog(optionPane2, "Incorrect login or password", "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
                if (b == -1)
                    this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
                if (b == 0) {
                    popUp();
                }
            }
            //если логин занят другим пользователем
            else {
                outMsg.println("$NEW$");
                outMsg.flush();
                outMsg.println(login.getText());
                outMsg.flush();
                outMsg.println(password.getText());
                outMsg.flush();
                boolean createNewUser = inMsg.nextLine().equals("good");
                if (createNewUser)
                    JOptionPane.showMessageDialog(optionPane2, "You have been successfully registered. Welcome to the chat");
                else {
                    int b = JOptionPane.showOptionDialog(optionPane2, "The login is used by another user. Choose another login", "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
                    if (b == -1)
                        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
                    if (b == 0) {
                        popUp();
                    }
                }
            }
        }
    }

}
