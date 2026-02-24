package src;
import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;

public class HTTPResponse {
    InputStream inputStream;
    OutputStream outputStream;
    HTTPRequest request;

    String roomName;

    Room room_;
    public static HashMap<String, String> messageMap = new HashMap<>();
    Socket client_;

    public HTTPResponse(Socket client, InputStream inStream, OutputStream clientStream, HTTPRequest req) {
        inputStream = inStream;
        outputStream = clientStream;
        request = req;
        client_ = client;
    }


    public void sendTheExistingFile() {
        try {//use try and catch if can't get the webpage will come out the 404 error page.
            String filePath = request.filePath;
            System.out.println(Thread.currentThread() + " send filename: " + filePath);
            File file = new File(filePath);
            FileInputStream fileToRead = new FileInputStream(file);
//    System.out.println(fileToRead.readAllBytes());
            outputStream.write("HTTP/1.1 200 OK\r\n".getBytes());
            String fileExtension = filePath.substring(filePath.lastIndexOf(".") + 1);
            outputStream.write(("Content-Type: text/" + fileExtension + "\r\n").getBytes());
            outputStream.write("\r\n".getBytes());
            outputStream.flush();
            fileToRead.transferTo(outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException fe) {
            System.out.println("here"+ fe.getMessage());
            String errorFilePath = "/Users/yuyaotu/Desktop/cs6011-labs/week1/day4/MyHttpServer/resources/error.html";
            sendErrorFile(errorFilePath);
        } catch (IOException io) {
            System.out.println("io:"+io.getMessage());
        }

    }

    public void sendErrorFile(String errorFilePath) {
        try {// this is the error page
            FileInputStream fileToRead = new FileInputStream(errorFilePath);
            outputStream.write("HTTP/1.1 404\n".getBytes());
            outputStream.write("Content-Type: text/html\n".getBytes());
            outputStream.write("\n".getBytes());
            fileToRead.transferTo(outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException fe) {
            System.out.println(fe.getMessage());
        } catch (IOException io) {
            System.out.println(io.getMessage());
        }

    }

    public static String generateAcceptString(String key) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String magicString = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        String combined = key + magicString;

        // 使用SHA-1哈希算法进行散列
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] hashBytes = sha1.digest(combined.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(hashBytes);
    }

    public void sendWsHandshakeHeader(String key) throws NoSuchAlgorithmException, IOException {
        System.out.println("Handshake ... ");
        // 对哈希结果进行Base64编码
        String secWebSocketAccept = HTTPResponse.generateAcceptString(key);
        System.out.println("HTTP/1.1 101 Switching Protocols");
        System.out.println("Upgrade: websocket");
        System.out.println("Connection: Upgrade");
        System.out.println("Sec-WebSocket-Accept: " + secWebSocketAccept);
        PrintWriter pw = new PrintWriter(outputStream);
        pw.println("HTTP/1.1 101 Switching Protocols");
        pw.println("Upgrade: websocket");
        pw.println("Connection: Upgrade");
        pw.println("Sec-WebSocket-Accept: " + secWebSocketAccept);
        pw.print("\n");
        pw.flush();

    }
    public void sendResponse() throws NoSuchAlgorithmException, IOException {
        if(!request.isWS) {
            sendTheExistingFile();
        }
        else{
            System.out.println(Thread.currentThread() + " This is a websocket request!");
            sendWsHandshakeHeader(request.key);
            while (true){////Start reading the WS message in binary
                DataInputStream dataInputStream = new DataInputStream(inputStream);
                byte b0 = dataInputStream.readByte();
                byte b1 = dataInputStream.readByte();
                int payloadLen;
                int opcode = b0 & 0x0F;
                System.out.println("Opcode is: "+opcode);
                if(opcode != 1){
                    break;
                }
                if(opcode == 1){
                    boolean isMasked = (b1 & 0x80 ) != 0;
                    if(isMasked){
                        payloadLen = b1 & 0x7F;
                        System.out.println("payloadLen is: "+payloadLen);
                        if(payloadLen == 126){
                            payloadLen =  dataInputStream.readShort();
                            System.out.println("payloadLen short is: "+payloadLen);
                        }
                        if ( payloadLen == 127) {
                            payloadLen = (int) dataInputStream.readLong();
                            System.out.println("payloadLen long is: " + payloadLen);
                        }
                        byte[] mask = dataInputStream.readNBytes(4);
                        byte[] payload = dataInputStream.readNBytes(payloadLen);
                        byte[] DECODED = new byte[payload.length];
                        for (var i = 0; i < payload.length; i++) {
                            DECODED[i] = (byte) (payload[i] ^ mask[i % 4]);
                        }
                        String DecodeMessage = new String(DECODED);
                        System.out.println("Message is:: "+DecodeMessage);

                        //-----------
                        //ROOM-LOGIC
                        //-----------

                        //CREATE ROOM
                        String type = getType(DecodeMessage);
                        if(type.equals("join")) {
                            roomName = getRoomName(DecodeMessage);
                            sendBackResponse(DecodeMessage,outputStream);
                        }
                        else {
                            for(Socket s : room_.clients_)
                                sendBackResponse(DecodeMessage,s.getOutputStream());

                        }
                        room_=Room.getRoom(roomName);
                        //LISTEN FOR USERS JOINING AND LEAVING ROOM
                        handleClients(DecodeMessage);
                        //ADD MSG TO MSG-LOG
                        room_.addMessage(DecodeMessage);

//                        if(!DecodeMessage.contains("object")){
//                            for(Socket s : room_.clients_)
//                            sendBackResponse(DecodeMessage,s.getOutputStream());

//                        }
                    }
                }
            }
        }
    }
    String getRoomName(String message){
        String roomName="";
        String roomname=message.split("\"room\":\"")[1];
        roomName = roomname.split("\"")[0];
        return roomName;
    }

    String getType(String message){
        String typeName="";
        String type=message.split("\"type\":\"")[1];
        typeName = type.split("\"")[0];
        return typeName;
    }

    public synchronized void handleClients(String message_) throws IOException {
        String message = message_;

        // Step 1: Remove the curly braces from the JSON string
        String jsonContent = message.substring(1, message.length() - 1);

        // Step 2: Split the JSON content into key-value pairs
        String[] keyValuePairs = jsonContent.split(",");

        for (String pair : keyValuePairs) {
            String[] keyValue = pair.split(":");
            String key = keyValue[0].replaceAll("\"", "").trim();
            String value = keyValue[1].replaceAll("\"", "").trim();

            messageMap.put(key, value);

            if(key.equals("type") && value.equals("join")) {
                for(Socket oldClient : room_.clients_){
                    sendBackResponse(message_,oldClient.getOutputStream());
                }
                room_.addClient(client_);
                // Send message history to the new client
                for (String historyMessage : room_.messages) {
                    sendBackResponse(historyMessage,client_.getOutputStream());
                }

            }
            if(key.equals("type") && value.equals("leave")) {
                room_.removeClient(client_);
//                room_.removeMessages();
            }
        }
    }


    public void sendBackResponse(String message,OutputStream out) throws IOException {
        //Echo Server sends the message we just recieved back
        DataOutputStream dataOutputStrem = new DataOutputStream(out);
        byte firstByte = (byte) 0x81;
        dataOutputStrem.write(firstByte);
        int len = message.length();
        if(len < 126){
            firstByte = (byte) len;
           dataOutputStrem.write(firstByte);
        }else if(len<(Math.pow(2,16)) ){
            firstByte = (byte) 126;
            dataOutputStrem.write(firstByte);
            dataOutputStrem.writeShort(len);
        }else {
            firstByte=(byte) 127;
            dataOutputStrem.write(firstByte);
            dataOutputStrem.writeLong(len);
        }
        System.out.println(message);
//        dataOutputStrem.write(message.length());
        dataOutputStrem.write(message.getBytes());
        dataOutputStrem.flush();
    }
}



//        DataOutputStream dataOutputStream = new DataOutputStream(out);
//        byte b0 =(byte) 0b10000001;
//        dataOutputStream.write(b0);
//        int len = message.length();
//        if(len < 126){
//            byte b1 = (byte) len;
//            dataOutputStream.write(b1);
//        }else if(len < (Math.pow(2,16))){
//            byte b1 = (byte) 126;
//            dataOutputStream.write(b1);
//            dataOutputStream.writeShort(len);
//        }else{
//            byte b1 = (byte) 127;
//            dataOutputStream.write(b1);
//            dataOutputStream.writeLong(len);
//        }
//        dataOutputStream.writeBytes(message);
//        dataOutputStream.flush();

//        outputStream.write(DECODED);
//        byte secondByte = mask + payload.length;
                // WS response
                // First
                // Second byte : mask bit + payload length
                // Extended payload length(if needed)
                // Payload
//                outputStream.write(DECODED);
//
//                String outputObj;
//                byte[] outputObjByte = null;
//
//                if (s[0].equals("join")) {
//                    String type = s[0];
//                    String room = s[2];
//                    String user = s[1];
//
//                    //This returns a list of socket that also in the same room.
//                    _socketArrayList = Room.getRoom(room, _client);
//                    System.out.println(_socketArrayList + "outside: ");
//                    outputObj = String.format("{\"type\": \"join\", \"user\": \"%s\", \"room\": \"%s\"}", user, room);
//                    outputObjByte = outputObj.getBytes();
//
//
//                } else if (s[0].equals("leave")) {
//                    _socketArrayList = Room.getRoom(s[1], _client);
//                    System.out.println(_socketArrayList + "leave: ");
//                    outputObj = String.format("{\"type\": \"leave\", \"user\": \"%s\", \"room\": \"%s\"}", s[2], s[1]);
//                    outputObjByte = outputObj.getBytes();
//                } else {
////            System.out.println(_client + "this is single client");
//                    _socketArrayList = Room.getRoom("", _client);
//                    String msg = "";
//                    for (int i = 1; i < s.length; i++) {
//                        msg = msg.concat(s[i]);
//                        msg = msg.concat(" ");
//                    }
//                    System.out.println(msg + "msg");
////            System.out.println(arrayList + "this is many client");
//                    outputObj = String.format("{\"type\": \"message\", \"user\": \"%s\", \"message\": \"%s\"}", s[0], msg);
//                    outputObjByte = outputObj.getBytes();
//
//
//                }
//                for (Socket cl : _socketArrayList) {
//                    OutputStream out = cl.getOutputStream();
//                    out.write(0x81);
//                    assert outputObjByte != null;
//                    out.write(outputObjByte.length & 0x7f);
//                    out.write(outputObjByte);
//                    out.flush();
////                }
//            }
//        }
