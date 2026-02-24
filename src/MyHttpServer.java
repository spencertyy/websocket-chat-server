package src;

import src.MyRunnable;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

//yuyao tu / Thu Sep 28/ Week1/Day4, HW Project Name: MyHttpServer
public class MyHttpServer {
//    static ArrayList<Room> rooms = new ArrayList();

    public static void main(String[] args) throws IOException {
        ServerSocket  server = new ServerSocket(8080);

        while (true) {
            Socket client = server.accept();
            Thread thread = new Thread(new MyRunnable(client));
            thread.start();
        }
    }
}
