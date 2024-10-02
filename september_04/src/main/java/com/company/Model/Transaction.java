package com.company.Model;

import sun.security.provider.DSAPublicKey;

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.Signature;
import java.security.SignatureException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;

public class Transaction implements Serializable {

    private byte[] from;
    private String fromFX;
    private byte[] to;
    private String toFX;
    private Integer value;
    private String timeStamp;
    private byte[] signature;
    private String signatureFX;
    private Integer ledgerId;

    public Transaction(byte[] from, byte[] to, Integer value, Integer ledgerId, String timeStamp, byte[] signature) {
        Base64.Encoder encoder = Base64.getEncoder();
        this.from = from;
        this.fromFX = encoder.encodeToString(from);
        this.to = to;
        this.toFX = encoder.encodeToString(to);
        this.value = value;
        this.signature = signature;
        this.signatureFX = encoder.encodeToString(signature);
        this.timeStamp = timeStamp;
        this.ledgerId = ledgerId;
    }

    public Transaction(Wallet fromWallet, byte[] toAddress, Integer value, Integer ledgerId, Signature signing)
            throws InvalidKeyException, SignatureException {

        Base64.Encoder encoder = Base64.getEncoder();
        this.from = fromWallet.getPublicKey().getEncoded();
        this.fromFX = encoder.encodeToString(fromWallet.getPublicKey().getEncoded());
        this.to = toAddress;
        this.toFX = encoder.encodeToString(toAddress);
        this.value = value;
        this.ledgerId = ledgerId;
        signing.initSign(fromWallet.getPrivateKey());
        signing.update(this.toString().getBytes());
        this.signature = signing.sign();
        this.signatureFX = encoder.encodeToString(signature);
        this.timeStamp = LocalDateTime.now().toString();
    }

    public Boolean isVerified(Signature signing) throws InvalidKeyException, SignatureException {
        signing.initVerify(new DSAPublicKey(this.from));
        signing.update(this.toString().getBytes());
        return signing.verify(signature);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Transaction)) return false;
        Transaction t = (Transaction) o;

        return Arrays.equals(t.signature, signature);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(signature);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "from=" + Arrays.toString(from) +
                ", fromFX='" + fromFX + '\'' +
                ", to=" + Arrays.toString(to) +
                ", toFX='" + toFX + '\'' +
                ", value=" + value +
                ", timeStamp='" + timeStamp + '\'' +
                ", signature=" + Arrays.toString(signature) +
                ", signatureFX='" + signatureFX + '\'' +
                ", ledgerId=" + ledgerId +
                '}';
    }

    public byte[] getFrom() {
        return from;
    }

    public String getFromFX() {
        return fromFX;
    }

    public byte[] getTo() {
        return to;
    }

    public String getToFX() {
        return toFX;
    }

    public Integer getValue() {
        return value;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public byte[] getSignature() {
        return signature;
    }

    public String getSignatureFX() {
        return signatureFX;
    }

    public Integer getLedgerId() {
        return ledgerId;
    }
}
