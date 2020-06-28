package de.marci4.websocketssl;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            startServer();
        } catch (Exception e) {
            Log.e("Main", e.getMessage());
        }

    }

    public void startServer() {
        ChatServer server = new ChatServer();
        try {
            SSLContext sslContext = getSSLContext(getResources().openRawResource(R.raw.server));
            server.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(sslContext));
        } catch (Exception e) {
            Log.e("ChatServer", e.getMessage());
            throw new AssertionError(e);
        }
        server.start();
    }

    public void connectToServer() throws URISyntaxException, UnknownHostException {
        ChatClient client = new ChatClient();
        try {
            SSLSocketFactory sslSocketFactory = getSSLContext(getResources().openRawResource(R.raw.client)).getSocketFactory();
            client.setSocketFactory(sslSocketFactory);
        } catch (Exception e) {
            Log.e("ChatClient", e.getMessage());
            throw new AssertionError(e);
        }
        Log.i("ChatClient", "Trying to connect");
        client.connect();
    }

    private SSLContext getSSLContext(InputStream in) throws KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException, CertificateException, UnrecoverableKeyException {
        String KEYPASSWORD = "PASSWORD";
        KeyStore keystore = KeyStore.getInstance("BKS");
        try {
            keystore.load(in, KEYPASSWORD.toCharArray());
        } finally {
            in.close();
        }
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("X509");
        keyManagerFactory.init(keystore, KEYPASSWORD.toCharArray());
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
        tmf.init(keystore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), tmf.getTrustManagers(), null);
        return sslContext;
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
            } catch (URISyntaxException | UnknownHostException e) {
                e.printStackTrace();
            }

        }
    }

    private class ChatClient extends WebSocketClient {
        public ChatClient() throws URISyntaxException {
            super(new URI("wss://localhost:8887"));
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