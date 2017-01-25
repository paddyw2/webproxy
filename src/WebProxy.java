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
            outputStream.println("Enter your HTTP request:");
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
                proxyClient = new ServerClient("pages.cpsc.ucalgary.ca");

                
                /*
                 * If request, valid send it to server client
                 */

                // process request
                // boolean requestIsValid = processRequest(httpRequest);
                boolean requestIsValid = true;
                if(requestIsValid) {
                    outputStream.println("Proxy reponse: Valid request!");
                    outputStream.flush();
                    // get the request response, and send to
                    // original client
                    LinkedList<String> response = proxyClient.getReponse(httpRequest);
                    for(String str : response) {
                        outputStream.println(str);
                        outputStream.flush();
                    }
                } else {
                    outputStream.println("Proxy reponse: Invalid request!");
                    outputStream.flush();
                }
                outputStream.println("Finished. Thank you!");
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

    public boolean processRequest(String request)
    {
        // if request valid, send to proxy client
        System.out.println("Processing client request...");

        // presume valid request
        return true;
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


