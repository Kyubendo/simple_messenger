package krya;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server extends Thread
{

    private ArrayList<ServerWorker> workerList = new ArrayList<>();
    private  int serverPort;
    public Server (int serverPort)
    {
        this.serverPort =serverPort;
    }

    public ArrayList<ServerWorker> getWorkerList()
    {
        return workerList;
    }


    @Override
    public void run()
    {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            while(true)
            {
                System.out.println("About to accept client connection...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connnection from " + clientSocket);

                ServerWorker worker = new ServerWorker(this, clientSocket);
                workerList.add(worker);

                worker.start();
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void removeWorker(ServerWorker serverWorker)
    {
        workerList.remove(serverWorker);
    }
}
