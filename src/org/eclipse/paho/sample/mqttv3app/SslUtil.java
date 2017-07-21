package org.eclipse.paho.sample.mqttv3app;

/**
 * Utility class to read encrypted PEM files and generate a
 * SSL Socket Factory based on the provided certificates.
 * The original code is by Sharon Asher (link below). I have modified
 * it to use a newer version of the BouncyCastle Library (v1.52)
 * 
 * Reference - https://gist.github.com/sharonbn/4104301"
 * https://gist.github.com/rohanag12/07ab7eb22556244e9698
 */

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.PEMException;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.File;
import java.io.Reader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

public class SslUtil {
	
	//https://stackoverflow.com/questions/12997559/ssl-connection-from-java-client-eclipse-paho-to-mosquitto-broker-unknown-ca
	public static SSLSocketFactory getSocketFactoryJks (final String caCrtFile, final String crtFile, final String keyFile, final String password) throws Exception
	{ 
	    Security.addProvider(new BouncyCastleProvider());
	
		JcaX509CertificateConverter certificateConverter = new JcaX509CertificateConverter().setProvider("BC");

        /**
         * Load Certificate Authority (CA) certificate
         */
        PEMParser reader = new PEMParser(new FileReader(caCrtFile));
        X509CertificateHolder caCertHolder = (X509CertificateHolder) reader.readObject();
        reader.close();

        X509Certificate caCert = certificateConverter.getCertificate(caCertHolder);
		
		/**
         * Load client certificate
         */
        reader = new PEMParser(new FileReader(crtFile));
        X509CertificateHolder certHolder = (X509CertificateHolder) reader.readObject();
        reader.close();

        X509Certificate cert = certificateConverter.getCertificate(certHolder);
		
		/**
         * Load client private key
         */
        reader = new PEMParser(new FileReader(keyFile));
        Object keyObject = reader.readObject();
        reader.close();

        PEMDecryptorProvider provider = new JcePEMDecryptorProviderBuilder().build(password.toCharArray());
        JcaPEMKeyConverter keyConverter = new JcaPEMKeyConverter().setProvider("BC");

        KeyPair key;

        if (keyObject instanceof PEMEncryptedKeyPair) {
            key = keyConverter.getKeyPair(((PEMEncryptedKeyPair) keyObject).decryptKeyPair(provider));
        } else {
            key = keyConverter.getKeyPair((PEMKeyPair) keyObject);
        }
		
	    // CA certificate is used to authenticate server
	    KeyStore caKs = KeyStore.getInstance("JKS");
	    caKs.load(null, null);
	    caKs.setCertificateEntry("ca-certificate", caCert);
	    TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
	    tmf.init(caKs);
	
	    // client key and certificates are sent to server so it can authenticate us
	    KeyStore ks = KeyStore.getInstance("JKS");
	    ks.load(null, null);
	    ks.setCertificateEntry("certificate", cert);
	    ks.setKeyEntry("private-key", key.getPrivate(), password.toCharArray(), new java.security.cert.Certificate[]{cert});
	    KeyManagerFactory kmf = KeyManagerFactory.getInstance("PKIX");
	    kmf.init(ks, password.toCharArray());
	
	    // finally, create SSL socket factory
	    SSLContext context = SSLContext.getInstance("TLSv1");
	    context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
	
	    return context.getSocketFactory();
	}
	
	public static SSLSocketFactory getSocketFactory(
		final File caCrtFile, final File crtFile, 
		final File keyFile, final String password
	) throws Exception 
	{
        
        return getSocketFactory(
        	new FileReader(caCrtFile), 
        	new FileReader(crtFile), 
        	new FileReader(keyFile),
        	password
        );
    }
	
	public static SSLSocketFactory getSocketFactory(
		final String caCrtFile, final String crtFile, 
		final String keyFile, final String password
	) throws Exception 
	{
        
        return getSocketFactory(
        	new FileReader(caCrtFile), 
        	new FileReader(crtFile), 
        	new FileReader(keyFile),
        	password
        );
    }
	
	public static SSLSocketFactory getSocketFactory
	(
		final Reader caCrtFile, final Reader crtFile, 
		final Reader keyFile, final String password
	) throws Exception
	{
    	//System.out.println(">>>>>>getSocketFactory>>>>>>>>Encrypted key - we will use provided password");
		//validateKey(keyFile, password.toCharArray());
		
        /**
         * Add BouncyCastle as a Security Provider
         */
        Security.addProvider(new BouncyCastleProvider());

        JcaX509CertificateConverter certificateConverter = new JcaX509CertificateConverter().setProvider("BC");

        /**
         * Load Certificate Authority (CA) certificate
         */
        PEMParser reader = new PEMParser(caCrtFile);
        X509CertificateHolder caCertHolder = (X509CertificateHolder) reader.readObject();
        reader.close();

        X509Certificate caCert = certificateConverter.getCertificate(caCertHolder);

        /**
         * Load client certificate
         */
        reader = new PEMParser(crtFile);
        X509CertificateHolder certHolder = (X509CertificateHolder) reader.readObject();
        reader.close();

        X509Certificate cert = certificateConverter.getCertificate(certHolder);

        /**
         * Load client private key
         */
        reader = new PEMParser(keyFile);
        Object keyObject = reader.readObject();
        reader.close();

        PEMDecryptorProvider provider = new JcePEMDecryptorProviderBuilder().build(password.toCharArray());
        JcaPEMKeyConverter keyConverter = new JcaPEMKeyConverter().setProvider("BC");

        KeyPair key;

        if (keyObject instanceof PEMEncryptedKeyPair) {
            key = keyConverter.getKeyPair(((PEMEncryptedKeyPair) keyObject).decryptKeyPair(provider));
        } else {
            key = keyConverter.getKeyPair((PEMKeyPair) keyObject);
        }

        /**
         * CA certificate is used to authenticate server
         */
        KeyStore caKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        caKeyStore.load(null, null);
        caKeyStore.setCertificateEntry("ca-certificate", caCert);

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(caKeyStore);

        /**
         * Client key and certificates are sent to server so it can authenticate the client
         */
        KeyStore clientKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        clientKeyStore.load(null, null);
        clientKeyStore.setCertificateEntry("certificate", cert);
        clientKeyStore.setKeyEntry("private-key", key.getPrivate(), password.toCharArray(),
                new Certificate[]{cert});

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(clientKeyStore, password.toCharArray());

        /**
         * Create SSL socket factory
         */
        SSLContext context = SSLContext.getInstance("TLSv1.2");
        context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

        /**
         * Return the newly created socket factory object
         */
        return context.getSocketFactory();
    }
	
	/*
	Original source code
    public static SSLSocketFactory getSocketFactory(final String caCrtFile, final String crtFile, final String keyFile,
                                                    final String password) {
        try {
			System.out.println(">>>>>>getSocketFactory>>>>>>>>Encrypted key - we will use provided password");
			//validateKey(keyFile, password.toCharArray());
			
            // Add BouncyCastle as a Security Provider
            Security.addProvider(new BouncyCastleProvider());

            JcaX509CertificateConverter certificateConverter = new JcaX509CertificateConverter().setProvider("BC");

            //Load Certificate Authority (CA) certificate
            PEMParser reader = new PEMParser(new FileReader(caCrtFile));
            X509CertificateHolder caCertHolder = (X509CertificateHolder) reader.readObject();
            reader.close();

            X509Certificate caCert = certificateConverter.getCertificate(caCertHolder);

            //Load client certificate
            reader = new PEMParser(new FileReader(crtFile));
            X509CertificateHolder certHolder = (X509CertificateHolder) reader.readObject();
            reader.close();

            X509Certificate cert = certificateConverter.getCertificate(certHolder);

            //Load client private key
            reader = new PEMParser(new FileReader(keyFile));
            Object keyObject = reader.readObject();
            reader.close();

            PEMDecryptorProvider provider = new JcePEMDecryptorProviderBuilder().build(password.toCharArray());
            JcaPEMKeyConverter keyConverter = new JcaPEMKeyConverter().setProvider("BC");

            KeyPair key;

            if (keyObject instanceof PEMEncryptedKeyPair) {
                key = keyConverter.getKeyPair(((PEMEncryptedKeyPair) keyObject).decryptKeyPair(provider));
            } else {
                key = keyConverter.getKeyPair((PEMKeyPair) keyObject);
            }

           //CA certificate is used to authenticate server
            KeyStore caKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            caKeyStore.load(null, null);
            caKeyStore.setCertificateEntry("ca-certificate", caCert);

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(caKeyStore);

            //Client key and certificates are sent to server so it can authenticate the client
            KeyStore clientKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            clientKeyStore.load(null, null);
            clientKeyStore.setCertificateEntry("certificate", cert);
            clientKeyStore.setKeyEntry("private-key", key.getPrivate(), password.toCharArray(),
                    new Certificate[]{cert});

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(clientKeyStore, password.toCharArray());

            //Create SSL socket factory
            SSLContext context = SSLContext.getInstance("TLSv1.2");
            context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

            //Return the newly created socket factory object
            return context.getSocketFactory();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    */
}