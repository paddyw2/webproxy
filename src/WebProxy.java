/*  Assignment 1 - CPSC 441
 *  Author: Patrick Withams
 *  Date: 23/1/2017
 *
 *  Program Description: A simple Java proxy web server
 *  which takes an HTTP request as input and if
 *  the file is in the local cache, serve from
 *  there. If not, get response from original
 *  server.
 *
 *  Limitations: Does not work with HTTPS, and
 *  any request that does not generate a 200
 *  OK response is responded with 400 Bad
 *  Request. Proxy assumes that request has
 *  a host line.
 *
 *  Class: WebProxy
 *  Description: Contains main logic for web proxy
 *  server. This includes running server, waiting
 *  for cient connections, parsing connections,
 *  sending requests to cache/proxy client etc.
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
    private ProxyClient proxyClient;
    private Scanner inputStream;
    private FileCache cache;
    private BufferedOutputStream byteOutput;

    public WebProxy(int port)
    {
        connectedSocket = null;
        inputStream = null;
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
     * received their response is served
     * and their connection terminated,
     * and the server loops back waiting for
     * another connection
     */
    public void start()
    {
        // run server indefinitely
        while(true)
        {
            /* Get Client Connection */

            getClientConnection();
            System.out.println("Connected to client");

            /* Get/Serve Client HTTP Requests */

            // wait for requests from the client
            System.out.println("Waiting for client request...");
            LinkedList<String> httpRequest = getClientRequest();
            
            // if get request failed (timed out),
            // terminate client connection
            if(httpRequest.getFirst().equals("terminate")) {
                closeConnection();
                continue;
            } else {
                // otherwise, confirm request received 
                System.out.println("Received client request");
            }

            /* Validate request, send to server client */

            // process request and create proxy client
            boolean requestIsValid = processRequest(httpRequest);
            boolean createProxyClient = createProxyClient(httpRequest);

            // if both successfull, get response
            if(requestIsValid && createProxyClient) {

                // check if file in cache
                boolean fileInCache = cache.fileInCache(httpRequest);
                byte[] response = new byte[1024];
                
                /* Get Response */

                // if in cache, get binary response from cache
                if(fileInCache) {
                    response = cache.getResponse(httpRequest);
                    System.out.println("Successfully served file from cache");
                } else {
                    // otherwise, send request to proxy client
                    // to obtain binary response from origin server
                    response = proxyClient.getReponse(httpRequest);
                    // save in cache for future requests
                    cache.saveNewResponse(httpRequest, response);
                }
                
                /* Parse Response and Send to Client */

                // parse binary response to check for 200 message
                String stringResponse = new String(response);
                String firstLine = "";

                // get first line of 200 header
                if(stringResponse.length() > 15)
                    firstLine = stringResponse.substring(0,15);

                if(firstLine.equals("HTTP/1.1 200 OK")) {
                    // if 200, send response to original client
                    sendByteClient(response);
                } else {
                    // if response is not 200 OK, return 400
                    respondBadRequest();
                }
            } else {
                // if invalid request, respond 400
                respondBadRequest();
            }
            // terminate client connection and wait for
            // new client
            closeConnection();
        }
    }

    public void getClientConnection()
    {
        // waits for client connection and creates
        // input/output stream objects to communicate
        // with client
        try {
            System.out.println("Waiting for new client connection...");
            // wait for client connection
            connectedSocket = serverSocket.accept();
            // inputStream to receive string from client
            inputStream = new Scanner(connectedSocket.getInputStream(),"UTF-8");
            // byteOutput to send bytes to client
            byteOutput = new BufferedOutputStream(connectedSocket.getOutputStream());
        } catch (Exception e) {
            System.out.println("Socket failed to connect with client");
        }
    }

    public LinkedList<String> getClientRequest()
    {
        // waits for client request, printing each
        // line for reference
        // the request is ended when a double return
        // (i.e. blank line) is received
        LinkedList<String> httpRequest = new LinkedList<String>();
        while(true) {
            String userInput = "";
            try {
                // get user input line
                userInput = inputStream.nextLine();
                if(userInput.length() > 10) {
                    String end = userInput.substring(userInput.length()-10);
                    if(end.equals("keep-alive"))
                        userInput = userInput.substring(0,12) + "close";
                }
                // add to request list
                httpRequest.add(userInput);
                // print line for reference
                System.out.println(userInput);
            } catch (Exception e) {
                System.out.println("No request received, closing connection");
                // add terminate keyword to trigger
                // connection close
                httpRequest.addFirst("terminate");
                break;
            }
            // if user enters a blank line, this
            // indicates end of request
            if(userInput.equals(""))
                break;
        }
        return httpRequest;
    }

    public boolean processRequest(LinkedList<String> request)
    {
        // checks if first letters of response are "GET"
        // this proxy does not accept other requests
        System.out.println("Processing client request...");

        // get first three letters of request
        String firstLine = request.getFirst();
        String getPart = firstLine.substring(0,3);
        // check that first letters are GET
        // if not, then invalid
        if(getPart.equals("GET"))
            return true;
        else
            return false;
    }

    public boolean createProxyClient(LinkedList<String> request)
    {
        // initialize client socket, by
        // getting host from HTTP request

        // search for host line
        int hostIndex = 0;
        boolean hostNotFound = true;
        for(String line : request) {
            if(line.length() > 4) {
                if(line.substring(0,4).equals("Host")) {
                    hostNotFound = false;
                    break;
                }
            }
            hostIndex++;
        }

        if(hostNotFound)
            return false;

        try {
            String host = request.get(hostIndex);
            host = host.substring(6);
            proxyClient = new ProxyClient(host, 80);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void closeConnection()
    {
        // closes the clients connection to proxy
        try {
            connectedSocket.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void respondBadRequest()
    {
        // converts string message to byte array
        // then sends to client
        String response = "HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain; charset=UTF-8;\r\nConnection: close\r\n\r\n400: Bad Request\r\n";
        byte[] byteString = response.getBytes();
        sendByteClient(byteString);
    }

    public void sendByteClient(byte[] message)
    {
        // sends binary data to client
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


