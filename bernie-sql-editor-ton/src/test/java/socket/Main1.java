package socket;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Paths;
import java.security.KeyStore;

/**
 * @author Christian Bernstein
 */
public class Main1 {

    public static void main(String[] args) throws Exception {
        final WebSocketClient client = new WebSocketClient(new URI("wss://localhost:25574")) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                System.out.println("opening");
            }

            @Override
            public void onMessage(String s) {
                System.out.println("message: " + s);
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                System.out.println("closing");
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();

            }
        };

        // load up the key store
        String STORETYPE = "JKS";
        String KEYSTORE = Paths.get("keystore.jks").toString();
        String STOREPASSWORD = "storepassword";
        String KEYPASSWORD = "keypassword";

        KeyStore ks = KeyStore.getInstance(STORETYPE);
        File kf = new File(KEYSTORE);
        ks.load(new FileInputStream(kf), STOREPASSWORD.toCharArray());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, KEYPASSWORD.toCharArray());
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext sslContext = null;
        sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        // sslContext.init( null, null, null ); // will use java's default key and trust store which is sufficient unless you deal with self-signed certificates

        SSLSocketFactory factory = sslContext
                .getSocketFactory();// (SSLSocketFactory) SSLSocketFactory.getDefault();

        client.setSocketFactory(factory);

        client.connectBlocking();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String line = reader.readLine();
            if (line.equals("close")) {
                client.closeBlocking();
            } else if (line.equals("open")) {
                client.reconnect();
            } else {
                client.send(line);
            }
        }
    }
}
