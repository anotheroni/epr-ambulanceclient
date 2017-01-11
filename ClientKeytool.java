import java.security.*;
import java.io.*;
import java.util.*;
import java.security.cert.*;
import java.text.*;
import java.lang.*;
import java.util.prefs.*;

/**
 * This class checks the validity of the client certificate
 *
 * @author Kane Neman
 * @version 030307
 *
 * Copyright(c) 2003 xxx Software AB
 * Permission is hereby denied to copy or use this software 
 * without a permission from xxx Software AB. Any illegal use of
 * this software will be prosecuted in a court of law
 *
 */ 
public class ClientKeytool {

    private String message = "";
    private String clientName;
    private String certDir;
    private String pubKey;

    /**
     * Constructor
     */ 
    public ClientKeytool() throws GeneralSecurityException, 
    IOException, KeyStoreException, NullPointerException, 
    IllegalStateException, BackingStoreException {

        String cert;

        RegisterKey rk = new RegisterKey();
        clientName = rk.getClientName();
        certDir = rk.getCertDirectory();
        pubKey = rk.getPubKey();

        //Check client certificate
        cert = certDir + clientName + ".private";
        NotValidAfter(getKeyStoreCertificate(loadKeyStore(cert, clientName)),
                clientName + ".private");
        //check server certificate
        cert = certDir + "server.public";
        NotValidAfter(getKeyStoreCertificate(loadKeyStore(cert, pubKey)),
                "server.public");
    }

    /**
     * Method used to load an existed keystore 
     * @param keyStoreName is the path to keystore
     * @param password is the password to keystore
     * @return KeyStore object containing the certificate
     */
    private KeyStore loadKeyStore(String keyStoreName, String password) 
        throws GeneralSecurityException, IOException {

            FileInputStream fis = new FileInputStream(keyStoreName);
            KeyStore keyStore = KeyStore.getInstance("JKS", "SUN");
            keyStore.load(fis, password.toCharArray());
            fis.close();

            return keyStore;
        }

    /**
     * Method used to retrieve the certificates in this keystore
     * @param keystore The keystore containing the certificate
     * @return Certificate object
     */
    private X509Certificate getKeyStoreCertificate(KeyStore keyStore) 
        throws KeyStoreException {

            X509Certificate cert = null; 
            Enumeration e = keyStore.aliases();

            while(e.hasMoreElements()) {
                String aliasName = (String)e.nextElement();
                cert = 
                    (X509Certificate)keyStore.getCertificate(aliasName);
            }
            return cert;
        }


    /**
     * Method used to retrieve the Not valid After date for a certificate
     * @param The certificate that will be checked
     * @param keyStoreName The keystoreName
     */ 
    private void NotValidAfter(X509Certificate cert, String keyStoreName) {

        if (cert == null) {
            message = message + "Certifikatet " + keyStoreName +
                " kunde inte hittas";
            return;
        }

        Locale locale = new Locale("sv", "SW");
        DateFormat formatter = 
            DateFormat.getDateInstance(DateFormat.SHORT, locale);

        Date date = cert.getNotAfter();
        Date tenDays = new Date(new Date().getTime() + (10*24*3600*1000));

        //If there are less than 10 days left 
        if(date.compareTo(tenDays) <= 0) {
            message = message + "Certifikatet " + keyStoreName +
                " går ut " + formatter.format(date) + ". Kontakta admin!";
        }
    }

    /**
     * Method used to get the Certificate validity date if there
     * are less than 10 days left
     * @return The validity date
     */
    public String getMessage() {
        return message;
    }
}
