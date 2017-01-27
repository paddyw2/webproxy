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

    public LinkedList<String> getResponse(LinkedList<String> request)
    {
        System.out.println("Responding from cache");
        String path = request.get(0);
        int len = path.length() - 9;
        path = path.substring(11, len); 
        String filepath = System.getProperty("user.dir") + "/cache/" + path;
        System.out.println("Serving file from cache");
        return readFile(filepath);
    }

    public void saveNewResponse(LinkedList<String> request, LinkedList<String> response)
    {
        String path = request.get(0);
        int len = path.length() - 9;
        path = path.substring(11, len); 
        String filepath = System.getProperty("user.dir") + "/cache/" + path;
        writeFile(filepath, response);
    }

    public void writeFile(String path, LinkedList<String> response)
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
            FileWriter fileOb = new FileWriter(file);
            BufferedWriter buff = new BufferedWriter(fileOb);
            for(String str : response) {
                buff.write(str + "\r\n");
            }
            buff.close();
            System.out.println("HTTP response successfully cached at:\n" + path);
        } catch (Exception e) {
            // handle any exceptions
            System.out.println("Exception triggered");
            System.out.println("Message: "+e.getMessage());
        }
    }

    public LinkedList<String> readFile(String filePath)
    {
        LinkedList<String> response = new LinkedList<String>();
        FileReader file = null;
        // check path is correct
        try {
            file = new FileReader(filePath);
        } catch (Exception e) {
            System.out.println("Invalid file path");
        }
        // try reading file
        String eachLine = null;
        try {
            BufferedReader buff = new BufferedReader(file);
            while((eachLine = buff.readLine()) != null) {
                response.add(eachLine); 
            }
        } catch (Exception e) {
            // handle any exceptions
            System.out.println("Exception triggered");
            System.out.println("Message: "+e.getMessage());
        }
        // return saved HTTP response
        return response;
    }
}
