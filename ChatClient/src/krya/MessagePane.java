package krya;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class MessagePane extends JPanel implements MessageListener
{


    private final String login;
    private final ChatClient client;

    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> messageList = new JList<>(listModel);
    private JTextField inputField= new JTextField();



    public MessagePane(ChatClient client, String login)
    {
        this.client = client;
        this.login = login;

        client.addMsgListener(this);

        setLayout(new BorderLayout());
        add(new JScrollPane(messageList), BorderLayout.CENTER);
        add(inputField, BorderLayout.SOUTH);

        inputField.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    String text = inputField.getText();
                    listModel.addElement("You:   " + text);
                    client.msg(login, text);
                    inputField.setText("");

                } catch (IOException ex)
                {
                    ex.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onMsg(String fromLogin, String msgBody)
    {
        if(login.equalsIgnoreCase(fromLogin))
        {
            String line = fromLogin + ":   " + msgBody;
            listModel.addElement(line);
        }

    }
}
