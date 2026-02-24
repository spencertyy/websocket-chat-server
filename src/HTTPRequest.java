package src;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Scanner;

public class HTTPRequest {
    InputStream clientRequest;
    HashMap<String,String> header = new HashMap<>();
    String filePath;

    public String key = "";
    public boolean isWS = false;

    public HTTPRequest(InputStream clientRequest_)  {
       this.clientRequest = clientRequest_;
        getTheFileName();
    }

    public void getTheFileName(){
        Scanner sc = new Scanner(clientRequest);
        String line = sc.nextLine();
        String[] lines = line.split(" ");
        System.out.println(Thread.currentThread() + "first line: " + line);
        String fileName = lines[1];
        if(fileName.equals("/")){// if client input "/" just return to the index.html
            fileName = "/index.html";
        }
        line = sc.nextLine();
        while (!line.equals("")){
            String[] pieces = line.split(": ");
            header.put(pieces[0], pieces[1]);
            line = sc.nextLine();
            //System.out.println(line);

        }
        if(header.containsKey("Sec-WebSocket-Key")){
            System.out.println(Thread.currentThread() + " This is a websocket request!");
            isWS = true;
            key = header.get("Sec-WebSocket-Key");
        }

        filePath = "resources" + fileName;
        System.out.println(Thread.currentThread() + "receive filename: " + filePath); //cout the file path we can see how many web page shows,Not important though
    }

}
