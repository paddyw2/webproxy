import java.net.*;
import java.util.Scanner;
import java.util.LinkedList;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;

public class ServerClient
{
    private Socket socket;
    private String server;
    public ServerClient(String server, int port)
    {
        this.server = server;
        socket = null;
        try {
            socket = new Socket(server, port);
        } catch (Exception e) {
            System.out.println("Socket initialization error");
        }
    }

    public boolean failure()
    {
        if(socket == null)
            return true;
        else
            return false;
    }

    public LinkedList<String> getReponse(LinkedList<String> requestList)
    {
        // sends the HTTP request to the desired server
        // and returns the response
        String request = "";
        for(String str : requestList)
        {
            System.out.println(str);
            request = request + str + "\r\n";
        }
        request = request + "\r\n";
        LinkedList<String> responseList = new LinkedList<String>();
        String response = "";
        try {
            PrintWriter outputStream = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            Scanner inputStream = new Scanner(socket.getInputStream(), "UTF-8");
            outputStream.println(request);
            outputStream.flush();
            while(true) {
                response = inputStream.nextLine();
                responseList.add(response);
            }

        } catch (Exception e) {
            System.out.println("Finished serving file to client");
        }

        return responseList;
    }
}

