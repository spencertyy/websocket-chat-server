package src;
//

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

//
public class Room  {
    //Socket client_;
    public static HashMap<String, Room> roomList = new HashMap<>();
    String RoomName;
    ArrayList<Socket> clients_;
    //static ArrayList<Room> rooms = new ArrayList();
    static ArrayList<String> messages = new ArrayList<>();
    private Room(String RoomName){
          this.RoomName = RoomName;
           clients_ = new ArrayList<>();
          System.out.println("Adding client");
      }

          public synchronized static Room getRoom(String roomName){
          //looking in hashmap for room, if its there don't create a new room
          Room room = roomList.get(roomName);
          //if there is no room already
          if(room == null){
              room = new Room(roomName);
              roomList.put(roomName, room);
          }
          return room;
          }

    public synchronized void addClient(Socket client){
        clients_.add(client);
        System.out.println("@addClient/Adding client: " + client);
    }

    public  synchronized void removeClient(Socket client){
        clients_.remove(client);
    }
      public synchronized void addMessage(String Massage){
          messages.add(Massage);
      }
//    public synchronized void removeMessages(){
//        for(String message : messages)
//        {
//            messages.remove(message) ;
//        }
//    }
      public synchronized void sendMessage(String Message) throws IOException {
          System.out.println("Message being sent: " + Message);
          for (Socket client : clients_) {
              DataOutputStream dataOutputStrem = new DataOutputStream(client.getOutputStream());
              // Convert the message to bytes
              byte[] responseBytes = Message.getBytes(StandardCharsets.UTF_8);
              // Send opcode
              dataOutputStrem.writeByte(0x81);// FIN bit set, opcode for text frame
              // Send payload length
              if (responseBytes.length < 126) {
                  dataOutputStrem.writeByte(responseBytes.length);
              } else if (responseBytes.length < Math.pow(2, 16)) {
                  dataOutputStrem.write(126);
                  dataOutputStrem.writeShort(responseBytes.length);
              } else {
                  dataOutputStrem.write(127);
                  dataOutputStrem.writeLong(responseBytes.length);
              }
              // Write the message bytes (Send Payload)
              dataOutputStrem.write(responseBytes);
              // Flush the output stream to ensure the message is sent
              dataOutputStrem.flush();


          }

      }
}