package com.company;

import com.company.Model.Block;
import com.company.Model.Transaction;
import com.company.Model.Wallet;
import com.company.ServceData.BlockchainData;
import com.company.ServceData.WalletData;
import com.company.Threads.MiningThread;
import com.company.Threads.PeerClient;
import com.company.Threads.PeerServer;
import com.company.Threads.UI;
import javafx.application.Application;
import javafx.stage.Stage;

import java.security.*;
import java.sql.*;
import java.time.LocalDateTime;

public class ECoin extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        new UI().start(primaryStage);
        new PeerClient().start();
        new PeerServer(6000).start();
        new MiningThread().start();
    }

    @Override
    public void init() {
        try {

            Connection walletConnection = DriverManager.getConnection(
                    "jdbc:sqlite:c:\\source\\Blockchain-with-Java\\db\\wallet.db");
            Statement walletStatement = walletConnection.createStatement();
            walletStatement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS  WALLET ( " +
                            "PRIVATE_KEY BLOB NOT NULL UNIQUE, " +
                            "PUBLIC_KEY BLOB NOT NULL UNIQUE, " +
                            "PRIMARY KEY (PRIVATE_KEY, PUBLIC_KEY))"
            );

            ResultSet result = walletStatement.executeQuery("SELECT * FROM WALLET");
            if (!result.next()) {

                Wallet newWallet = new Wallet();
                byte[] prvBlob = newWallet.getPrivateKey().getEncoded();
                byte[] pubBlob = newWallet.getPublicKey().getEncoded();

                PreparedStatement pstmt = walletConnection.prepareStatement(
                        "INSERT INTO WALLET " +
                                "(PRIVATE_KEY, PUBLIC_KEY) " +
                                "VALUES (?,?)"
                );
                pstmt.setBytes(1, prvBlob);
                pstmt.setBytes(2, pubBlob);

                pstmt.execute();
            }

            result.close();
            walletConnection.close();
            walletStatement.close();
            WalletData.getInstance().loadWallet();

            Connection blockchainConnection = DriverManager.getConnection("jdbc:sqlite:c:\\source\\blockchain.db");
            Statement blockchainStmnt = blockchainConnection.createStatement();
            blockchainStmnt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS BLOCKCHAIN " +
                            "ID INTEGER NOT NULL UNIQUE, " +
                            "PREVIOUS_HASH BLOB, " +
                            "CURRENT_HASH BLOB, " +
                            "LEDGER_ID INTEGER NOT NULL UNIQUE, " +
                            "CREATED_ON TEXT, " +
                            "CREATED_BY BLOB, " +
                            "MINING_POINTS TEXT, " +
                            "LUCK NUMERIC, " +
                            "PRIMARY KEY (ID AUTOINCREMENT)"
            );

            ResultSet blockchainResult = blockchainStmnt.executeQuery("SELECT * FROM BLOCKCHAIN");
            Transaction initBlockRewardTransaction = null;

            if (!blockchainResult.next()) {
                Block firstBlock = new Block();
                firstBlock.setMinedBy(WalletData.getInstance().getWallet().getPublicKey().getEncoded());
                firstBlock.setTimeStamp(LocalDateTime.now().toString());

                Signature signature = Signature.getInstance("SHA256withDSA");
                signature.initSign(WalletData.getInstance().getWallet().getPrivateKey());
                signature.update(firstBlock.toString().getBytes());

                firstBlock.setCurrHash(signature.sign());

                PreparedStatement pstmt = blockchainConnection.prepareStatement(
                        "INSERT INTO BLOCKCHAIN (PREVIOUS_HASH, CURRENT_HASH, LEDGER_ID, " +
                                "CREATED_ON, CREATED_BY, MINING_POINTS, LUCK " +
                                "VALUES (?,?,?,?,?,?)"
                );

                pstmt.setBytes(1, firstBlock.getPrevHash());
                pstmt.setBytes(2, firstBlock.getCurrHash());
                pstmt.setInt(3, firstBlock.getLedgerId());
                pstmt.setString(4, firstBlock.getTimeStamp());
                pstmt.setBytes(5, WalletData.getInstance().getWallet().getPublicKey().getEncoded());
                pstmt.setInt(6, firstBlock.getMiningPoints());
                pstmt.setDouble(7, firstBlock.getLuck());

                pstmt.executeUpdate();

                Signature transSignature = Signature.getInstance("SHA256withDSA");
                initBlockRewardTransaction = new Transaction(WalletData.getInstance().getWallet(),
                        WalletData.getInstance().getWallet().getPublicKey().getEncoded(),
                        100, 1, transSignature);
            }
            blockchainResult.close();

            blockchainStmnt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS TRANSACTION " +
                            "ID INTEGER NOT NULL UNIQUE, " +
                            "FROM BLOB " +
                            "TO BLOB " +
                            "LEDGER_ID INTEGER " +
                            "VALUE INTEGER " +
                            "CREATED_ON TEXT " +
                            "SIGNATURE BLOB UNIQUE " +
                            "PRIMARY KEY (ID AUTOINCREMENT)"

            );

            if (initBlockRewardTransaction != null) {
                BlockchainData.getInstance()
                        .addTransaction(initBlockRewardTransaction, true);
                BlockchainData.getInstance()
                        .addTransactionState(initBlockRewardTransaction);
            }

            blockchainStmnt.close();
            blockchainConnection.close();

        } catch (SQLException | NoSuchAlgorithmException |
                 InvalidKeyException | SignatureException e) {

            System.out.println("db fail: " + e.getMessage());
        } catch (GeneralSecurityException e) {

            e.printStackTrace();
        }
        BlockchainData.getInstance().loadBlockchain();
    }
}
