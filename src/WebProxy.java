/* Assignment 1 - CPSC 441
 *  Author: Patrick Withams
 *  Date: 23/1/2017
 *  Description: A simple Java web server
 */

import java.net.*;
import java.util.Scanner;
import java.util.LinkedList;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.BufferedOutputStream;

public class WebProxy
{
    private ServerSocket serverSocket;
    private Socket connectedSocket;
    private ServerClient proxyClient;
    private PrintWriter outputStream;
    private FileCache cache;
    private BufferedOutputStream byteOutput;

    public WebProxy(int port)
    {
        cache = new FileCache();
        // initialize server listening port
        serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (Exception e) {
            System.out.println("Socket initialization on port " + port
            + "failed: " + e.getMessage());
        }
    }

    /*
     * Waits for a client to connect, then waits
     * for their request. When the request is
     * received, or if they quit, their response
     * is served and their connection terminated,
     * and the server loops back waiting for
     * another connection
     */
    public void start()
    {
        connectedSocket = null;
        Scanner inputStream = null;
        outputStream = null;
        boolean quitProgram = false;
        boolean saveToCache = true;
        // run server indefinitely
        while(true)
        {
            /* Get Client Connection */

            try {
                System.out.println("Waiting for new client connection...");
                connectedSocket = serverSocket.accept();
                outputStream = new PrintWriter(new OutputStreamWriter(connectedSocket.getOutputStream(),"UTF-8"));
                inputStream = new Scanner(connectedSocket.getInputStream(),"UTF-8");
                // for bytes
                byteOutput = new BufferedOutputStream(connectedSocket.getOutputStream());
            } catch (Exception e) {
                System.out.println("Socket failed to connect with client");
            }

            // once client is connected, print message
            // to their screen
            System.out.println("Connected to client");
            //printToClient("Enter your HTTP request (double return to submit):");

            /* Get/Serve Client HTTP Requests */

            // wait for requests from the client
            LinkedList<String> httpRequest = new LinkedList<String>();
            System.out.println("Waiting for client request...");

            String userInput = "";
            while(true) {
                // wait for client request
                try {
                    userInput = inputStream.nextLine();
                    // remove any keep-alive requests
                    // and change to close
                    if(userInput.length() > 10) {
                        String end = userInput.substring(userInput.length()-10);
                        if(end.equals("keep-alive"))
                            userInput = userInput.substring(0,12) + "close";
                    }
                    httpRequest.add(userInput);
                    System.out.println(userInput);
                } catch (Exception e) {
                    System.out.println("No request received, closing connection");
                    quitProgram = true;
                    break;
                }
                // if user enters a blank line, this
                // indicates end of message
                // if they enter quit, then
                // exit connection
                if(userInput.equals("")) {
                    break;
                } else if(userInput.equals("quit")) {
                    System.out.println("Terminating");
                    quitProgram = true;
                    break;
                }
            }
            
            /* Quit */ 

            // if user chose to quit
            if(quitProgram) {
                closeConnection("Termination signal received");
                quitProgram = false;
                continue;
            }

            /* Validate request, send to server client */

            // confirm request received 
            System.out.println("Received client request");

            // process request and create proxy client
            boolean createProxy = createProxy(httpRequest);
            boolean requestIsValid = processRequest(httpRequest);
            // if both successfull, get response
            if(requestIsValid && createProxy) {
                // get the request response from eiter
                // origin or cache, and send back
                // to original client
                boolean fileInCache = cache.fileInCache(httpRequest);
                byte[] response = new byte[10];
                if(fileInCache) {
                    response = cache.getResponse(httpRequest);
                    System.out.println("Successfully served file from cache");
                } else {
                    response = proxyClient.getReponse(httpRequest);
                    cache.saveNewResponse(httpRequest, response);
                }
                // if response is not 200 OK, return 400
                if(true){//firstLine.equals("HTTP/1.1 200 OK")) {
                    // print response to client
                    printByteClient(response);
                } else {
                    printToClient("HTTP/1.1 400 Bad Request\r\nConnection: close\r\n\r\n400");
                }
            } else {
                // if invalid request, respond 400
                printToClient("HTTP/1.1 400 Bad Request\r\nConnection: close\r\n\r\n400");
            }
            // terminate client connection and wait for
            // new client
            closeConnection("\nFinished. Thank you!");
        }
    }

    public boolean processRequest(LinkedList<String> request)
    {
        // if request valid, send to proxy client
        System.out.println("Processing client request...");


        // check that first letters are GET
        String firstLine = request.getFirst();
        String getPart = firstLine.substring(0,3);
        if(getPart.equals("GET"))
            return true;
        else
            return false;
    }

    public boolean createProxy(LinkedList<String> request)
    {
        // initialize client socket, by
        // getting host from HTTP request
        try {
            String host = request.get(1);
            host = host.substring(6);
            proxyClient = new ServerClient(host, 80);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void closeConnection(String message)
    {
        //outputStream.println(message);
        //outputStream.flush();

        try {
            connectedSocket.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void printToClient(String message)
    {
        outputStream.println(message);
        outputStream.flush();
    }

    public void printByteClient(byte[] message)
    {
        try {
            byteOutput.write(message);
            byteOutput.flush();
        } catch (Exception e) {
            System.out.println("Byte output error");
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args)
    {
        String server = "localhost";
        int server_port = 0;
        try
        {
            // check for command line arguments
            if (args.length == 1)
            {
                server_port = Integer.parseInt(args[0]);
            }
            else
            {
                System.out.println("Wrong number of arguments, try again");
                System.out.println("Usage: java WebProxy [port]");
                System.exit(0);
            }

            WebProxy proxy = new WebProxy(server_port);

            System.out.println("Proxy server started...");
            proxy.start();
        }
        catch (Exception e)
        {
            System.out.println("Exception in main: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


