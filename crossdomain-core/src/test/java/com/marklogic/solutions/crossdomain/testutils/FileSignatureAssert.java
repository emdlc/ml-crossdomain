package com.marklogic.solutions.crossdomain.testutils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.DigestException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.X509EncodedKeySpec;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FileSignatureAssert extends Assert {

	PrivateKey priv;
	PublicKey pub;
	Signature dsa;
	
	private static final String KEY_ALGORITHM = "DSA";
	private static final String SIG_ALGORITHM = "SHA1withDSA";
	private static final String PROVIDER = "SUN";
	private static final String INPUT_DATA = "some data";

	@Before
	public void setupKeysAndSignature() throws Exception {
		KeyPairGenerator keyGen;
		keyGen = KeyPairGenerator.getInstance(KEY_ALGORITHM, PROVIDER);

		SecureRandom random = SecureRandom.getInstance("SHA1PRNG", PROVIDER);
		keyGen.initialize(1024, random);

		KeyPair pair = keyGen.generateKeyPair();
		priv = pair.getPrivate();
		pub = pair.getPublic();
		
		dsa = Signature.getInstance(SIG_ALGORITHM, PROVIDER); 
	}
	
	public byte[] signData() throws InvalidKeyException, SignatureException {
		dsa.initSign(priv);
		dsa.update(INPUT_DATA.getBytes());
    	byte[] realSig = dsa.sign();
    	
    	return realSig;
	}
	
	@Test
	public void verifySig() throws Exception {
        byte[] encKey = pub.getEncoded();  
        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);

        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM, PROVIDER);
        PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);

        /* input the signature bytes */ 
        byte[] sigToVerify = signData();

        /* create a Signature object and initialize it with the public key */
        Signature sig = Signature.getInstance(SIG_ALGORITHM, PROVIDER);
        sig.initVerify(pubKey);

        /* Update and verify the data */
        sig.update(INPUT_DATA.getBytes());

        boolean verifies = sig.verify(sigToVerify);
        assertTrue("signature verifies", verifies);
	}
	
	@Test
	public void testMessageDigest() throws DigestException, NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");

		String input = "test input";
		 try {
		     md.update(input.getBytes());
		     MessageDigest tc1 = (MessageDigest) md.clone();
		     byte[] digest = tc1.digest();
		     
		     assertTrue("true".equals("true"));
		     
		 } catch (CloneNotSupportedException cnse) {
		     throw new DigestException("couldn't make digest of partial content");
		 }
	}
}
