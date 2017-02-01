/*  Assignment 1 - CPSC 441
 *  Author: Patrick Withams
 *  Date: 23/1/2017
 *
 *  Class: ProxyClient
 *  Description: Forwards a HTTP request
 *  to the origin server if requested
 *  object is not in local cache.
 */

import java.net.*;
import java.util.Scanner;
import java.util.LinkedList;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.ByteArrayOutputStream;

public class ProxyClient
{
    private Socket socket;
    private String server;

    public ProxyClient(String server, int port)
    {
        // try to create a server socket
        // from the given host name and port
        // number
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
        // check if socket initialized
        // successfully
        if(socket == null)
            return true;
        else
            return false;
    }

    public byte[] getReponse(LinkedList<String> requestList)
    {
        // sends the HTTP request to the desired server
        // and returns the response in a byte array

        // create a single string with \r\n line breaks
        // out of request string list
        String request = "";
        for(String str : requestList)
        {
            System.out.println(str);
            request = request + str + "\r\n";
        }
        request = request + "\r\n";

        // create binary storage buffer and array
        // to store response from origin server
        byte[] data = new byte[1024];
        ByteArrayOutputStream store =  new ByteArrayOutputStream();
        int endInput = 0;

        // try to send request and receive response
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
