package src;

import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

public class MyRunnable implements Runnable {
    public Socket client_;

    public MyRunnable(Socket client) {
        client_ = client;
    }

    @Override
    public void run() {
        try {
            HTTPRequest hRequest = new HTTPRequest(client_.getInputStream());
//            hRequest.getTheFileName();
            HTTPResponse hResponse = new HTTPResponse(client_, client_.getInputStream(), client_.getOutputStream(), hRequest);
            hResponse.sendResponse();
            client_.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}