package com.company;

import java.io.Serializable;
import java.security.*;

public class Wallet implements Serializable {
    private KeyPair keyPair;

    public Wallet() throws NoSuchAlgorithmException {
        this(2048, KeyPairGenerator.getInstance("DSA"));
    }

    public Wallet(int keySize, KeyPairGenerator keyPairGen) {
        keyPairGen.initialize(keySize);
        this.keyPair = keyPairGen.generateKeyPair();
    }

    public Wallet(PublicKey publicKey, PrivateKey privateKey) {
        this.keyPair = new KeyPair(publicKey, privateKey);
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }
}
