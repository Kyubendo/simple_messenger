package krya;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ServerWorker extends Thread
{

    private final Socket clientSocket;
    private final Server server;
    private String login = null;
    private OutputStream outputStream;


    public ServerWorker (Server server, Socket clientSocket)
    {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    public String getLogin()
    {
        return login;
    }

    private void send (String msg) throws IOException
    {
        if (login != null)
        outputStream.write(msg.getBytes());

    }

    private void handleLogoff() throws IOException
    {
        server.removeWorker(this);
        ArrayList<ServerWorker> workerList = server.getWorkerList();
        String offlineMsg = "offline " + login+ "\r\n";
        for (ServerWorker worker : workerList)
        {
            if(!login.equalsIgnoreCase(worker.getLogin())) {
                try {
                    worker.send(offlineMsg);
                } catch (Exception e) {
                }
            }
        }

        clientSocket.close();
    }

    private void handleMessage(String[] tokens) throws IOException
    {
        String sendTo = tokens[1];
        String msgBody = tokens[2];

        ArrayList<ServerWorker> workerList = server.getWorkerList();
        for (ServerWorker worker : workerList)
        {
            if(worker.getLogin().equalsIgnoreCase(sendTo))
            {
                String outMsg = "msg " + login + " " + msgBody + "\r\n";
                worker.send(outMsg);
            }//telnet localhost 8818
        }
    }



    @Override
    public void run()
    {
        try {
            handleClientSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void handleClientSocket() throws IOException
    {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line=reader.readLine())!= null)
        {
            String[] tokens = line.split(" ");

            if (tokens !=null && tokens.length>0)
            {
                String cmd = tokens[0];

                if("logoff".equalsIgnoreCase(cmd) || "quit".equalsIgnoreCase(cmd))
                {
                    handleLogoff();
                    break;
                }
                else if ("login".equalsIgnoreCase(cmd))
                {
                    handleLogin(outputStream, tokens);

                }
                else if ("msg".equalsIgnoreCase(cmd))
                {
                    String[] msgTokens = line.split(" ", 3);
                    handleMessage(msgTokens);
                }
                else
                {
                    String msg = "unknown " +cmd + "\r\n";
                    outputStream.write(msg.getBytes());
                }
            }
        }
        clientSocket.close();
    }

    private void handleLogin(OutputStream outputStream, String[] tokens)
    {
        if (tokens.length == 3)
        {
            String login = tokens[1];
            String password = tokens[2];

            if (login.equals("Bob") && password.equals("qwerty") || (login.equals("Alice") && password.equals("123"))|| (login.equals("Test") && password.equals("test")))
            {
                String msg = "right login\r\n";
                try {
                    outputStream.write(msg.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.login = login;
                System.out.println("User logged in: " + login);

                ArrayList<ServerWorker> workerList = server.getWorkerList();

                String onlineMsg = "online " + login + "\r\n";
                for (ServerWorker worker : workerList)
                {
                    if(!login.equalsIgnoreCase(worker.getLogin())&& worker.getLogin()!=null) {//telnet localhost 8818
                        try {
                            String msg1 = "online " + worker.getLogin() + "\r\n"; //worker.getLogin()!=null
                            send(msg1);
                        } catch (Exception e) {
                        }
                    }
                }

                for (ServerWorker worker : workerList)
                {
                    if(!login.equalsIgnoreCase(worker.getLogin())) {
                        try {
                            worker.send(onlineMsg);
                        } catch (Exception e) {
                        }
                    }
                }

            }else
            {
                String msg = "error login\r\n";
                try {
                    outputStream.write(msg.getBytes());
                    System.err.println("Login failed for " + login + "!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
