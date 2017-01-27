import java.io.*;
import java.util.LinkedList;

public class FileCache
{
    public FileCache()
    {

    }

    public boolean fileInCache(LinkedList<String> request)
    {
        // search directory for file matching the
        // format "hostname, filepath""
        // i.e. pages.cpsc.ucalgary.ca/~cryiac.james/sample.txt
        String path = request.get(0);
        int len = path.length() - 9;
        path = path.substring(11, len); 
        String filepath = System.getProperty("user.dir") + "/cache/" + path;
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
        System.out.println("Responding from cache");
        String path = request.get(0);
        int len = path.length() - 9;
        path = path.substring(11, len); 
        String filepath = System.getProperty("user.dir") + "/cache/" + path;
        System.out.println("Serving file from cache");
        return readFile(filepath);
    }

    public void saveNewResponse(LinkedList<String> request, byte[] response)
    {
        String path = request.get(0);
        int len = path.length() - 9;
        path = path.substring(11, len); 
        String filepath = System.getProperty("user.dir") + "/cache/" + path;
        writeFile(filepath, response);
    }

    public void writeFile(String path, byte[] data)
    {
        File file = null;
        // check path is correct
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

        // try writing file
        try {
            FileOutputStream fileStream = new FileOutputStream(file);
            DataOutputStream dataStream = new DataOutputStream(fileStream);
            dataStream.write(data);
            dataStream.close();
            System.out.println("HTTP response successfully cached at:\n" + path);
        } catch (Exception e) {
            // handle any exceptions
            System.out.println("Exception triggered");
            System.out.println("Message: "+e.getMessage());
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
        // try reading file
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
        // return saved HTTP response
        return data;
    }
}
