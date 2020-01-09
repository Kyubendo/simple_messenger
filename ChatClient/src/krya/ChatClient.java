package krya;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ChatClient
{
    private String serverName;
    private int serverPort;
    private Socket socket;
    private InputStream serverIn;
    private OutputStream serverOut;
    private BufferedReader bufferedIn;

    private ArrayList<UserStatusListener> userStLisList = new ArrayList<>();
    private ArrayList<MessageListener> msgLisList = new ArrayList<>();

    public ChatClient(String serverName, int serverPort)
    {
        this.serverName = serverName;
        this.serverPort = serverPort;
    }

    public static void main(String[] args) throws IOException
    {
        ChatClient client = new ChatClient("localhost", 8818);

        client.addUserStLis(new UserStatusListener()
        {
            @Override
            public void online(String login)
            {
                System.out.println("Online: " + login);
            }

            @Override
            public void offline(String login)
            {
                System.out.println("Offline: " + login);
            }
        });

        client.addMsgListener(new MessageListener()
        {
            @Override
            public void onMsg(String fromLogin, String msgBody)
            {
                System.out.println("You have received a message from " + fromLogin + ":  " + msgBody);
            }
        });

        if (!client.connect())
        {
            System.err.println("Connect failed!");
        } else
        {
            System.out.println("Connect successfully.");
            if (client.login("guest", "123"))
            {
                System.out.println("Login successfully.");

                //client.msg("admin", "hello, admin");
            } else
            {
                System.err.println("Login failed!");
            }


        }
    }

    public void msg(String sendTo, String msgBody) throws IOException
    {
        String cmd = "msg " + sendTo + " " + msgBody + "\r\n";
        serverOut.write(cmd.getBytes());
    }

    public void logoff() throws IOException
    {
        String cmd = "logoff\r\n";
        serverOut.write(cmd.getBytes());

    }


    public boolean login(String login, String password) throws IOException
    {
        String cmd = "login " + login + " " + password + "\r\n";
        serverOut.write(cmd.getBytes());

        String response = bufferedIn.readLine();
        System.out.println("Response line: " + response);

        if ("right login".equalsIgnoreCase(response))
        {
            startMsgReader();
            return true;
        } else
        {
            return false;
        }
    }

    public void startMsgReader()
    {
        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                readMsgLoop();
            }
        };
        t.start();
    }

    public void readMsgLoop()
    {
        try
        {
            String line;
            while ((line = bufferedIn.readLine()) != null)
            {
                String[] tokens = line.split(" ");

                if (tokens != null && tokens.length > 0)
                {
                    String cmd = tokens[0];
                    if ("online".equalsIgnoreCase(cmd))
                    {
                        handleOnline(tokens);
                    } else if ("offline".equalsIgnoreCase(cmd))
                    {
                        handleOffline(tokens);
                    } else if ("msg".equalsIgnoreCase(cmd))
                    {
                        String[] msgTokens = line.split(" ", 3);
                        handleMsg(msgTokens);
                    }

                }

            }
        } catch (Exception e)
        {

            e.printStackTrace();
            try
            {
                socket.close();
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }

        }
    }

    public void handleMsg(String[] msgTokens)
    {
        String login = msgTokens[1];
        String msgBody = msgTokens[2];

        for(MessageListener listener: msgLisList)
        {
            listener.onMsg(login, msgBody);
        }
    }

    public void handleOffline(String[] tokens)
    {
        String login = tokens[1];
        for (UserStatusListener listener : userStLisList)
        {
            listener.offline(login);
        }
    }

    public void handleOnline(String[] tokens)
    {
        String login = tokens[1];
        for (UserStatusListener listener : userStLisList)
        {
            listener.online(login);
        }
    }

    public boolean connect()
    {
        try
        {
            this.socket = new Socket(serverName, serverPort);
            System.out.println("Client port is: " + socket.getLocalPort());
            this.serverIn = socket.getInputStream();
            this.serverOut = socket.getOutputStream();
            this.bufferedIn = new BufferedReader(new InputStreamReader(serverIn));

            return true;
        } catch (IOException e)
        {
            e.printStackTrace();

        }
        return false;
    }

    public void addUserStLis(UserStatusListener listener)
    {
        userStLisList.add(listener);
    }

    public void removeUserStLis(UserStatusListener listener)
    {
        userStLisList.remove(listener);
    }


    public  void addMsgListener (MessageListener listener)
    {
        msgLisList.add(listener);
    }
    public  void removeMsgListener (MessageListener listener)
    {
        msgLisList.remove(listener);
    }


}
