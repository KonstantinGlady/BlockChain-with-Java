package com.company.Model;

import sun.security.provider.DSAPublicKeyImpl;

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Block implements Serializable {

    private byte[] prevHash;
    private byte[] currHash;
    private String timeStamp;
    private byte[] minedBy;
    private Integer ledgerId = 1;
    private Integer miningPoints = 0;
    private Double luck = 0.0;
    private List<Transaction> transactionLedger = new ArrayList<>();

    public Block(byte[] prevHash, byte[] currHash, String timeStamp,
                 byte[] minedBy, Integer ledgerId, Integer miningPoints,
                 Double luck, List<Transaction> transactionLedger) {

        this.prevHash = prevHash;
        this.currHash = currHash;
        this.timeStamp = timeStamp;
        this.minedBy = minedBy;
        this.ledgerId = ledgerId;
        this.miningPoints = miningPoints;
        this.luck = luck;
        this.transactionLedger = transactionLedger;
    }

    public Block(LinkedList<Block> currentBlockChain) {
        Block lastBlock = currentBlockChain.getLast();
        this.prevHash = lastBlock.getCurrHash();
        this.ledgerId = lastBlock.getLedgerId() + 1;
        this.luck = Math.random() * 1_000_000;
    }

    public Block() {
        this.prevHash = new byte[]{0};
    }

    public Boolean isVerified(Signature signing) throws InvalidKeyException, SignatureException {
        signing.initVerify(new DSAPublicKeyImpl(this.minedBy));
        signing.update(this.toString().getBytes());
        return signing.verify(this.currHash);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Block block)) return false;

        return Arrays.equals(block.getPrevHash(), getPrevHash());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getPrevHash());
    }

    @Override
    public String toString() {
        return "Block{" +
                "prevHash=" + Arrays.toString(prevHash) +
                ", currHash=" + Arrays.toString(currHash) +
                ", timeStamp='" + timeStamp + '\'' +
                ", minedBy=" + Arrays.toString(minedBy) +
                ", ledgerId=" + ledgerId +
                ", miningPoints=" + miningPoints +
                ", luck=" + luck +
                ", transactionLedger=" + transactionLedger +
                '}';
    }

    public byte[] getPrevHash() {
        return prevHash;
    }

    public byte[] getCurrHash() {
        return currHash;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public byte[] getMinedBy() {
        return minedBy;
    }

    public Integer getLedgerId() {
        return ledgerId;
    }

    public Integer getMiningPoints() {
        return miningPoints;
    }

    public Double getLuck() {
        return luck;
    }

    public List<Transaction> getTransactionLedger() {
        return transactionLedger;
    }
}
