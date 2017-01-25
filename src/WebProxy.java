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

public class WebProxy
{
    private ServerSocket serverSocket;
    private Socket connectedSocket;
    private ServerClient proxyClient;

    public WebProxy(int port)
    {
        // initialize server listening port
        serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (Exception e) {
            System.out.println("Socket initialization on port " + port
                    + "failed: " + e.getMessage());
        }
    }

    public void start()
    {
        connectedSocket = null;
        Scanner inputStream = null;
        PrintWriter outputStream = null;
        boolean runLoop = true;
        // web proxy logic
        while(runLoop)
        {
            /*
             * Get Client Connection
             *
             */
            try {
            // waits until a client connects
            System.out.println("Waiting for new client connection...");
            connectedSocket = serverSocket.accept();
            outputStream = new PrintWriter(new OutputStreamWriter(connectedSocket.getOutputStream(), "UTF-8"));
            inputStream = new Scanner(connectedSocket.getInputStream(), "UTF-8");
            } catch (Exception e) {
                System.out.println("Socket failed to connect with client: " + e.getMessage());
            }

            System.out.println("Connected to client");
            outputStream.println("Enter your HTTP request (double return to submit):");
            outputStream.flush();

            /*
             * Get/Serve Client HTTP Requests
             *
             */

            // now we are connected, we will loop and
            // wait for requests from the client
            while(runLoop)
            {
                System.out.println("Waiting for client request...");
                LinkedList<String> httpRequest = new LinkedList<String>();
                while(true) {
                    // wait for client request
                    String userInput = inputStream.nextLine();
                    httpRequest.add(userInput);
                    if(userInput.equals("")) {
                        break;
                    } else if(userInput.equals("quit")) {
                        runLoop = false;
                        System.out.println("Terminating");
                        return;
                    }
                }

                // print request
                System.out.println("Received client request (list)");
                // initialize client socket
                String host = httpRequest.get(1);
                host = host.substring(6);
                proxyClient = new ServerClient(host, 80);
                if(proxyClient.failure()) {
                    outputStream.println("HTTP/1.1 400 Bad Request");
                    outputStream.flush();
                    try { 
                        connectedSocket.close();
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;
                }
                
                /*
                 * If request, valid send it to server client
                 */

                // process request
                boolean requestIsValid = processRequest(httpRequest);
                if(requestIsValid) {
                    // get the request response, and send to
                    // original client
                    LinkedList<String> response = proxyClient.getReponse(httpRequest);
                    String firstLine = response.getFirst();
                    if(firstLine.equals("HTTP/1.1 200 OK")) {
                        for(String str : response) {
                            outputStream.println(str);
                            outputStream.flush();
                        }
                    } else {
                        outputStream.println("HTTP/1.1 400 Bad Request");
                        outputStream.flush();
                    }

                } else {
                    outputStream.println("HTTP/1.1 400 Bad Request");
                    outputStream.flush();
                }
                outputStream.println("\nFinished. Thank you!");
                outputStream.flush();
                try { 
                    connectedSocket.close();
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
                break;
            }
        }

        try {
            serverSocket.close();
        } catch (Exception e) {
            System.out.println("Socket close error: " + e.getMessage());
        }
        System.out.println("Server received termination signal - exiting");
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


