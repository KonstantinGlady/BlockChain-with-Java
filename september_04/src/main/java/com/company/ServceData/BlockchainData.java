package com.company.ServceData;

import com.company.Model.Block;
import com.company.Model.Transaction;
import javafx.collections.ObservableList;

import java.util.LinkedList;

public class BlockchainData {
    public static BlockchainData getInstance() {
        return null;
    }

    public static long getTimeoutInterval() {
        return 0;
    }

    public void loadBlockchain() {

    }

    public void addTransaction(Transaction initBlockRewardTransaction, boolean b) {

    }

    public void addTransactionState(Transaction initBlockRewardTransaction) {

    }

    public String getWalletBalanceFX() {
        return null;
    }

    public ObservableList<Transaction> getTransactionLedgerFX() {
        return null;
    }

    public void setExit(boolean b) {

    }

    public LinkedList<Block> getCurrentBlockchain() {
        return null;
    }

    public void mineBlock() {

    }

    public boolean isExit() {
        return false;
    }

    public long getMiningPoints() {
        return 0;
    }

    public void setMiningPoints(long l) {

    }

    public Object getBlockchainConsensus(LinkedList<Block> returnedBlockchain) {

        return null;
    }
}
