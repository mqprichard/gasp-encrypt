package com.cloudbees.gasp;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.AlgorithmParameters;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

/**
 * Hello world!
 *
 */
public class EncryptCerts
{
    // APN Cert/Key PEM filenames: read via ClassLoader
    private static final String apnsCertFilename = "gasp-cert.pem";
    private static final String apnsKeyFilename = "gasp-key.pem";
    private static final String apnsCertBase64Filename = "gasp-cert.b64";
    private static final String apnsKeyBase64Filename = "gasp-key.b64";

    // Apple Development iOS Push Services Certificate and Private Key
    private static String apnsCertificate;
    private static String apnsKey;

    // Google Cloud Messaging API Key
    private static String gcmApiKey;

    // Utility class to read PEM file into a String
    private static String getPemFromStream(String fileName) {
        String pemData = new String();

        try {
            FileInputStream fis = new FileInputStream(new File(fileName));

            // Read PEM cert/key file from InputStream
            InputStreamReader isr = new InputStreamReader ( fis ) ;
            BufferedReader reader = new BufferedReader ( isr ) ;

            String readString = reader.readLine ( ) ;
            while ( readString != null ) {
                // Add newline to each row of PEM cert/key
                pemData = pemData + readString + '\n';
                readString = reader.readLine ( ) ;
            }
            isr.close ( ) ;
        } catch ( IOException ioe ) {
            ioe.printStackTrace ( ) ;
        }
        // Remove trailing newline
        return StringUtils.chomp(pemData);
    }

    private static byte[] mSalt = null;
    private static byte[] mIv = null;

    // Generate salt value for AES encryption
    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[8];
        random.nextBytes(bytes);
        mSalt = bytes;
        return bytes;
    }

    public static void encryptToFile(char[] key, byte[] input, String fileName) {
        try {
            /* Derive the key, given password and salt. */
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            if (mSalt ==  null) {
                mSalt = generateSalt();
                System.out.println("Salt (base64): " + new String(Base64.encodeBase64(mSalt)));
            }
            KeySpec spec = new PBEKeySpec(key, mSalt, 65536, 128);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

            /* Encrypt the message. */
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            if (mIv == null) {
                cipher.init(Cipher.ENCRYPT_MODE, secret);
                AlgorithmParameters params = cipher.getParameters();
                mIv = params.getParameterSpec(IvParameterSpec.class).getIV();
                System.out.println("Init Vector (base64): " + new String(Base64.encodeBase64(mIv)));
            }
            else {
                cipher.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(mIv));
            }

            byte[] ciphertext = cipher.doFinal(input);

            FileOutputStream fos = new FileOutputStream(new File(fileName));
            fos.write(Base64.encodeBase64(ciphertext));
            fos.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main( String[] args )
    {
        // 1. Check system properties for GCM_API_KEY
        if ((gcmApiKey = System.getProperty("GCM_API_KEY")) != null) {
            System.out.println("GCM_API_KEY (from system property): " + gcmApiKey);
        }

        // 2. Check system environment for GCM_API_KEY
        else if ((gcmApiKey = System.getenv("GCM_API_KEY")) != null){
            System.out.println("GCM_API_KEY (from system environment): " + gcmApiKey);
        }

        // Error: GCM_API_KEY not set
        else {
            System.out.println("GCM_API_KEY not set");
        }

        // Read APN iOS Push Service Certificate from ClassLoader
        apnsCertificate = getPemFromStream(apnsCertFilename);
        encryptToFile(gcmApiKey.toCharArray(), apnsCertificate.getBytes(), apnsCertBase64Filename);

        // Read APN iOS Push Service Private Key from ClassLoader
        apnsKey = getPemFromStream(apnsKeyFilename);
        encryptToFile(gcmApiKey.toCharArray(), apnsKey.getBytes(), apnsKeyBase64Filename);
    }
}
