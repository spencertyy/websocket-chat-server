package src;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

class HTTPResponseTest {

    @Test
    void sendWsHandshakeHeader() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String test = "dGhlIHNhbXBsZSBub25jZQ==";
        String result = "s3pPLMBiTxaQ9kYGzzhZRbK+xOo=";
        Assertions.assertEquals(result, HTTPResponse.generateAcceptString(test));
    }
}