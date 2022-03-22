package de.christianbernstein.bernie.modules.net;

import de.christianbernstein.bernie.ses.annotations.UseTon;
import de.christianbernstein.bernie.ses.bin.ITon;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * @author Christian Bernstein
 */
public class LetsencryptSSLContextProvider implements ISSLContextProvider {

    @UseTon
    private static ITon ton;


    @Override
    public SSLContext load(@NotNull INetModule module) {
        final NetModuleConfigShard config = module.configResource().use(false);

        SSLContext context;
        // todo change it c.c rofl
        String password = "";

        String pathname = ton.interpolate(config.getSslCertificateDir());
        try {
            context = SSLContext.getInstance(config.getSslContext());

            // todo allow absolute paths
            byte[] certBytes = parseDERFromPEM(getBytes(new File(String.format(
                    "%s%s%s", pathname, File.separator, config.getSslRelativeCertificatePath())
            )), "-----BEGIN CERTIFICATE-----", "-----END CERTIFICATE-----");

            // todo allow absolute paths
            byte[] keyBytes = parseDERFromPEM(getBytes(new File(String.format(
                    "%s%s%s", pathname, File.separator, config.getSslRelativePrivateKeyPath())
            )), "-----BEGIN PRIVATE KEY-----", "-----END PRIVATE KEY-----");

            X509Certificate cert = generateCertificateFromDER(certBytes, config);
            RSAPrivateKey key = generatePrivateKeyFromDER(keyBytes, config);

            KeyStore keystore = KeyStore.getInstance(config.getSslKeyStore());
            keystore.load(null);
            keystore.setCertificateEntry("cert-alias", cert);
            keystore.setKeyEntry("key-alias", key, password.toCharArray(), new Certificate[]{cert});

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(config.getSslKeyManagerFactory());
            kmf.init(keystore, password.toCharArray());

            KeyManager[] km = kmf.getKeyManagers();

            context.init(km, null, null);
        } catch (Exception e) {
            context = null;
            e.printStackTrace();
        }
        return context;
    }

    private static byte[] parseDERFromPEM(byte[] pem, String beginDelimiter, String endDelimiter) {
        String data = new String(pem);
        String[] tokens = data.split(beginDelimiter);
        tokens = tokens[1].split(endDelimiter);
        return DatatypeConverter.parseBase64Binary(tokens[0]);
    }

    private static RSAPrivateKey generatePrivateKeyFromDER(byte[] keyBytes, NetModuleConfigShard config) throws InvalidKeySpecException, NoSuchAlgorithmException {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) factory.generatePrivate(spec);
    }

    private static X509Certificate generateCertificateFromDER(byte[] certBytes, NetModuleConfigShard config) throws CertificateException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certBytes));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static byte @NotNull [] getBytes(@NotNull File file) {
        byte[] bytesArray = new byte[(int) file.length()];

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            fis.read(bytesArray); //read file into bytes[]
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytesArray;
    }
}
