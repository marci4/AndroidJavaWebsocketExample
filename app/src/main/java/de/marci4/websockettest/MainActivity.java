package de.marci4.websockettest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;


import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebSocketImpl.DEBUG = true;
        setContentView(R.layout.activity_main);
        try {
            startServer();
        } catch (Exception e) {
            Log.e("Main", e.getMessage());
        }

    }

    public void startServer() {
        ChatServer server = new ChatServer();
        // load up the key store
        String KEYPASSWORD = "PASSWORD";
        try {
            KeyStore keystore = KeyStore.getInstance("BKS");
            InputStream in = getResources().openRawResource(R.raw.keystore);
            try {
                keystore.load(in, KEYPASSWORD.toCharArray());
            } finally {
                in.close();
            }
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("X509");
            keyManagerFactory.init(keystore, KEYPASSWORD.toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init(keystore);

            SSLContext sslContext = null;
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), tmf.getTrustManagers(), null);

            server.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(sslContext));
        } catch (Exception e) {
            Log.e("ChatServer", e.getMessage());
            throw new AssertionError(e);
        }
        server.start();
    }

    public void connectToServer() throws URISyntaxException {
        ChatClient client = new ChatClient();

        String KEYPASSWORD = "PASSWORD";
        try {
            KeyStore keystore = KeyStore.getInstance("BKS");
            InputStream in = getResources().openRawResource(R.raw.keystore);
            try {
                keystore.load(in, KEYPASSWORD.toCharArray());
            } finally {
                in.close();
            }
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("X509");
            keyManagerFactory.init(keystore, KEYPASSWORD.toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init(keystore);

            SSLContext sslContext = null;
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), tmf.getTrustManagers(), null);
            //sslContext.init(null, null, null);
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            client.setSocket(sslSocketFactory.createSocket());
        } catch (Exception e) {
            Log.e("ChatClient", e.getMessage());
            throw new AssertionError(e);
        }
        Log.i("ChatClient", "Trying to connect");
        client.connect();
    }

    private class ChatServer extends WebSocketServer {
        public ChatServer() {
            super(new InetSocketAddress(8887));
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            Log.i("ChatServer", "Open");
            conn.send("hi from server");
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            Log.i("ChatServer", "Close");
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            Log.i("ChatServer", "Message: " + message);
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            Log.i("ChatServer", "Error: " + ex.getMessage());
        }

        @Override
        public void onStart() {
            Log.i("ChatServer", "Start");

            try {
                connectToServer();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

        }
    }

    private class ChatClient extends WebSocketClient {
        public ChatClient() throws URISyntaxException {
            super(new URI("wss://127.0.0.1:8887"));
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            Log.i("ChatClient", "Open");
            send("hi");
            Log.i("ChatClient", "Send hi");
        }

        @Override
        public void onMessage(String message) {
            Log.i("ChatClient", "Message: " + message);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            Log.i("ChatClient", "Close");
        }

        @Override
        public void onError(Exception ex) {
            Log.i("ChatClient", "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
