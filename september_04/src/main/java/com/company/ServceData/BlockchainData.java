package com.company.ServceData;

import com.company.Model.Block;
import com.company.Model.Transaction;
import com.company.Model.Wallet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import sun.security.provider.DSAPublicKeyImpl;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.sql.*;
import java.util.*;

public class BlockchainData {

    private ObservableList<Transaction> newBlockTransactionsFX;
    private ObservableList<Transaction> newBlockTransactions;
    private LinkedList<Block> currentBlockchain = new LinkedList<>();
    private Block latestBlock;
    private boolean exit = false;
    private int miningPoints;
    private static final int TIMEOUT_INTERVAL = 65;
    private static final int MINING_INTERVAL = 60;

    private Signature signing = Signature.getInstance("SHA256withDSA");

    private static BlockchainData instance;

    static {
        try {
            instance = new BlockchainData();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public BlockchainData() throws NoSuchAlgorithmException {
        newBlockTransactions = FXCollections.observableArrayList();
        newBlockTransactionsFX = FXCollections.observableArrayList();
    }

    public static BlockchainData getInstance() {
        return instance;
    }

    private Comparator<Transaction> transactionComparator = Comparator.comparing(Transaction::getTimeStamp);

    public ObservableList<Transaction> getTransactionLedgerFX() {
        newBlockTransactionsFX.clear();
        newBlockTransactions.sort(transactionComparator);
        newBlockTransactionsFX.addAll(newBlockTransactions);

        return FXCollections.observableArrayList(newBlockTransactionsFX);
    }

    public String getWalletBalanceFX() {
        return getBalance(currentBlockchain, newBlockTransactions,
                WalletData.getInstance().getWallet().getPublicKey()).toString();
    }

    private Integer getBalance(LinkedList<Block> blockchain,
                               ObservableList<Transaction> currentLedger, PublicKey walletAddress) {

        Integer balance = 0;
        for (Block block : blockchain) {
            for (Transaction transaction : block.getTransactionLedger()) {

                if (Arrays.equals(transaction.getFrom(), walletAddress.getEncoded())) {
                    balance -= transaction.getValue();
                }

                if (Arrays.equals(transaction.getTo(), walletAddress.getEncoded())) {
                    balance += transaction.getValue();
                }
            }
        }

        for (Transaction transaction : currentLedger) {

            if (Arrays.equals(transaction.getFrom(), walletAddress.getEncoded())) {
                balance -= transaction.getValue();
            }
        }

        return balance;
    }

    private void verifyBlockChain(LinkedList<Block> currentTransaction)
            throws GeneralSecurityException {

        for (Block block : currentBlockchain) {
            if (!block.isVerified(signing)) {

                throw new GeneralSecurityException("Block validation failed");
            }

            List<Transaction> transactions = block.getTransactionLedger();
            for (Transaction transaction : transactions) {
                if (Boolean.FALSE.equals(transaction.isVerified(signing))) {

                    throw new GeneralSecurityException("Transaction  validation failed");
                }
            }
        }

    }

    public void addTransactionState(Transaction transaction) {
        newBlockTransactions.add(transaction);
        newBlockTransactions.sort(transactionComparator);
    }

    public void addTransaction(Transaction transaction, boolean blockReward)
            throws GeneralSecurityException {

        if (getBalance(currentBlockchain, newBlockTransactions,
                new DSAPublicKeyImpl(transaction.getFrom())) < transaction.getValue() && !blockReward) {

            throw new GeneralSecurityException("Not enough funds by sender to record transaction");
        } else {
            try {
                Connection connection = DriverManager.getConnection(
                        "jdbc:sqlite:C:\\source\\Blockchain-with-java\\db\\blockchain.db");
                PreparedStatement pstmt;
                pstmt = connection.prepareStatement(
                        "INSERT INTO TRANSACTIONS" +
                                "(\"FROM\",\"TO\", LEDGER_ID, VALUE, SIGNATURE, CREATED_ON)" +
                                "VALUES(?,?,?,?,?,?)");
                pstmt.setBytes(1, transaction.getFrom());
                pstmt.setBytes(2, transaction.getTo());
                pstmt.setInt(3, transaction.getLedgerId());
                pstmt.setInt(4, transaction.getValue());
                pstmt.setBytes(5, transaction.getSignature());
                pstmt.setString(6, transaction.getTimeStamp());
                pstmt.executeUpdate();

                pstmt.close();
                connection.close();
            } catch (SQLException e) {

                System.out.println("Problem with DB");
                e.getMessage();
                e.printStackTrace();
            }
        }
    }

    private ArrayList<Transaction> loadTransactionLedger(Integer ledgerId) throws SQLException {
        ArrayList<Transaction> transactions = new ArrayList<>();
        try {
            Connection connection = DriverManager.getConnection(
                    "jdbc:sqlite:C:\\source\\Blockchain-with-java\\db\\blockchain.db");
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM TRANSACTIONS " +
                    "WHERE LEDGER_ID = ?");
            stmt.setInt(1, ledgerId);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                transactions.add(new Transaction(
                        resultSet.getBytes("FROM"),
                        resultSet.getBytes("TO"),
                        resultSet.getInt("VALUE"),
                        resultSet.getInt("LEDGER_ID"),
                        resultSet.getString("CREATED_ON"),
                        resultSet.getBytes("SIGNATURE")
                ));
            }

            stmt.close();
            resultSet.close();
            connection.close();
        } catch (SQLException e) {

            e.printStackTrace();
        }
        return transactions;
    }

    public void loadBlockChain() {
        try {
            Connection connection = DriverManager.getConnection(
                    "jdbc:sqlite:C:\\source\\Blockchain-with-java\\db\\blockchain.db");
            Statement stmt = connection.createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM BLOCKCHAIN");

            while (resultSet.next()) {
                this.currentBlockchain.add(new Block(
                        resultSet.getBytes("PREVIOUS_HASH"),
                        resultSet.getBytes("CURRENT_HASH"),
                        resultSet.getString("CREATED_ON"),
                        resultSet.getBytes("CREATED_BY"),
                        resultSet.getInt("LEDGER_ID"),
                        resultSet.getInt("MINING_POINTS"),
                        resultSet.getDouble("LUCK"),
                        loadTransactionLedger(resultSet.getInt("LEDGER_ID"))
                ));
            }
            latestBlock = currentBlockchain.getLast();
            Transaction transaction = new Transaction(new Wallet(),
                    WalletData.getInstance().getWallet().getPublicKey().getEncoded(),
                    100, latestBlock.getLedgerId() + 1, signing);

            newBlockTransactions.clear();
            newBlockTransactions.add(transaction);
            verifyBlockChain(currentBlockchain);

            stmt.close();
            resultSet.close();
            connection.close();
        } catch (SQLException | NoSuchAlgorithmException e) {

            System.out.println("Problems with DB " + e.getMessage());
            e.printStackTrace();
        } catch (GeneralSecurityException e) {

            e.printStackTrace();
        }
    }

    public static long getTimeoutInterval() {
        return TIMEOUT_INTERVAL;
    }

    public void setExit(boolean b) {
        exit = b;
    }

    public LinkedList<Block> getCurrentBlockchain() {
        return currentBlockchain;
    }

    public void mineBlock() {

    }

    public boolean isExit() {
        return exit;
    }

    public long getMiningPoints() {
        return miningPoints;
    }

    public void setMiningPoints(long l) {
        miningPoints = l;
    }

    public Object getBlockchainConsensus(LinkedList<Block> returnedBlockchain) {

        return null;
    }
}
