import java.net.*;
import java.util.Scanner;
import java.util.LinkedList;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.ByteArrayOutputStream;

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

    public byte[] getReponse(LinkedList<String> requestList)
    {
        // sends the HTTP request to the desired server
        // and returns the response in a byte array
        String request = "";
        for(String str : requestList)
        {
            System.out.println(str);
            request = request + str + "\r\n";
        }
        request = request + "\r\n";

        // create binary storage buffer and array
        byte[] data = new byte[1024];
        ByteArrayOutputStream store =  new ByteArrayOutputStream();
        int endInput = 0;
        try {
            // send request to client as a string
            PrintWriter outputStream = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            InputStream inputStream = socket.getInputStream();
            outputStream.println(request);
            outputStream.flush();
            // receive request from client as binary
            while(endInput > -1) {
                endInput = inputStream.read(data);
                if(endInput > -1)
                    store.write(data, 0, endInput);
            }
        } catch (Exception e) {
            System.out.println("Finished serving file to client");
        }
        
        // return array with binary response data
        data = store.toByteArray();
        return data;
    }
}

