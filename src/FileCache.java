/*  Assignment 1 - CPSC 441
 *  Author: Patrick Withams
 *  Date: 23/1/2017
 *
 *  Class: FileCache
 *  Description: Deals with reading/writing HTTP
 *  requests and responses to the local cache.
 *  Also is able to check if a request can be
 *  served from cache.
 */

import java.io.*;
import java.util.LinkedList;

public class FileCache
{
    public boolean fileInCache(LinkedList<String> request)
    {
        // search directory for file matching the
        // format "hostname, filepath""
        // i.e. pages.cpsc.ucalgary.ca/~cryiac.james/sample.txt

        // get filepath
        String path = parseRequest(request);
        String filepath = System.getProperty("user.dir") + "/" + path;
        // check if file exits already, which indicates
        // file in cache
        File checkCache = null;
        try {
            checkCache = new File(filepath);
        } catch (Exception e) {
            System.out.println("File in cache");
        }
        if(checkCache.exists())
            return true;
        else
            return false;
    }

    public byte[] getResponse(LinkedList<String> request)
    {
        // takes http request, and returns a byte array
        // of the requested file in cache
        System.out.println("Responding from cache");
        String path = parseRequest(request);
        String filepath = System.getProperty("user.dir") + "/" + path;
        System.out.println("Serving file from cache");
        return readFile(filepath);
    }

    public void saveNewResponse(LinkedList<String> request, byte[] response)
    {
        // takes a http request, and saves the response
        // to file
        String path = parseRequest(request);
        String filepath = System.getProperty("user.dir") + "/" + path;
        response = forceCloseConnection(response);
        writeFile(filepath, response);
    }

    public void writeFile(String path, byte[] data)
    {
        File file = null;
        // check path is correct and that
        // it does not already exist
        try {
            file = new File(path);
            file.getParentFile().mkdirs();
            if(!file.exists()) {
                file.createNewFile();
            } else {
                System.out.println("File already exits");
            }
        } catch (Exception e) {
            System.out.println("Invalid file path");
        }

        // try writing data byte array to file
        try {
            FileOutputStream fileStream = new FileOutputStream(file);
            DataOutputStream dataStream = new DataOutputStream(fileStream);
            dataStream.write(data);
            dataStream.close();
            System.out.println("HTTP response successfully cached at:\n" + path);
        } catch (Exception e) {
            // handle any exceptions
            System.out.println("Exception triggered");
            System.out.println("Message: " + e.getMessage());
        }
    }

    public byte[] readFile(String filePath)
    {
        byte[] data = null;
        File file = null;
        // check path is correct
        try {
            file = new File(filePath);
        } catch (Exception e) {
            System.out.println("Invalid file path");
        }
        // try reading file into byte array
        String eachLine = null;
        try {
            data = new byte[(int) file.length()];
            FileInputStream fileStream = new FileInputStream(file);
            DataInputStream dataStream = new DataInputStream(fileStream);
            dataStream.read(data);
            dataStream.close();
        } catch (Exception e) {
            // handle any exceptions
            System.out.println("Exception triggered");
            System.out.println("Message: "+e.getMessage());
        }
        // return saved HTTP response as byte array
        return data;
    }

    public String parseRequest(LinkedList<String> request)
    {
        // take HTTP request, and parse to obtain
        // the domain and file path
        // due to different clients formatting
        // the request line in different ways,
        // the http check is required
        String path = request.get(0);
        int len = path.length() - 9;
        if(path.substring(4,9).equals("http:")) {
            // from browser
            path = path.substring(11, len); 
        } else if (path.substring(4,9).equals("https")) {
            // secure from browser (which this
            // proxy cannot handle)
            path = path.substring(12, len); 
        } else {
            // from telnet/manual request
            path = path.substring(4, len);

            // search for host line
            int hostIndex = 0;
            for(String line : request) {
                if(line.substring(0,4).equals("Host"))
                    break;
                hostIndex++;
            }
            // extract host value
            String domain = request.get(hostIndex);
            domain = domain.substring(6);
            path = domain + path;
        }
        return path;
    }

    public byte[] forceCloseConnection(byte[] response)
    {
        System.out.println("Forcing close connection");
        int breakPoint = 0;
        for(int i=0;i<response.length;i++)
        {
            if(response[i] == '\r' && response[i+1] == '\n' &&
                    response[i+2] == '\r' && response[i+3] == '\n') {
                breakPoint = i+4;
                break;
             }
        }
        int bodySize = response.length - breakPoint;
        byte[] header = new byte[breakPoint];
        byte[] body = new byte[bodySize];

        for(int i=0;i<breakPoint;i++) {
            header[i] = response[i];
        }

        int counter = 0;
        for(int i=breakPoint;i<response.length;i++) {
            body[counter] = response[i];
            counter++;
        }

        String headerString = new String(header);
        String newHeader = headerString.replace("Connection: keep-alive", "Connection: close");
        header = newHeader.getBytes();
        byte[] updatedArray = new byte[response.length];

        for(int i=0;i<response.length;i++) {
            if(i<breakPoint)
                updatedArray[i] = header[i];
            else
                updatedArray[i] = body[i-breakPoint];
        }

        return updatedArray;
    }
}
