package krya;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class LoginWindow extends JFrame
{
    private final ChatClient client;
    JTextField loginField = new JTextField();
    JPasswordField passwordField = new JPasswordField();
    JButton loginButton = new JButton("Login");


    public LoginWindow()
    {
        super("Login");

        this.client = new ChatClient("localhost", 8818);
        client.connect();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(loginField);
        p.add(passwordField);
        p.add(loginButton);

        loginButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    doLogin();
                } catch (IOException ex)
                {
                    ex.printStackTrace();
                }
            }
        });

        getContentPane().add(p, BorderLayout.CENTER);
        setSize(200,120);

        setVisible(true);
    }

    private void doLogin() throws IOException
    {
        java.lang.String login = loginField.getText();
        java.lang.String password = passwordField.getText();

        UserListPane userListPane = new UserListPane(client);
        if (client.login(login,password))
        {


            JFrame frame =  new JFrame("User list");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 600);

            frame.getContentPane().add(new JScrollPane());
            frame.getContentPane().add(userListPane, BorderLayout.CENTER);
            frame.setVisible(true);
            setVisible(false);
        }else
        {
            JOptionPane.showMessageDialog(this, "Invalid login or password!");
        }


    }

    public static void main(String[] args)
    {
        LoginWindow loginWindow = new LoginWindow();
        loginWindow.setVisible(true);
    }

}
