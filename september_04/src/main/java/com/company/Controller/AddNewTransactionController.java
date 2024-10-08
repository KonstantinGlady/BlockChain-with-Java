package com.company.Controller;

import com.company.Model.Transaction;
import com.company.ServceData.BlockchainData;
import com.company.ServceData.WalletData;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.security.GeneralSecurityException;
import java.security.Signature;
import java.util.Base64;

public class AddNewTransactionController {

    @FXML
    private TextField toAddress;
    @FXML
    private TextField value;

    @FXML
    public void createNewTransaction() throws GeneralSecurityException {

        Base64.Decoder decoder = Base64.getDecoder();
        Signature signature = Signature.getInstance("SHA256withDSA");
        byte[] sendB = decoder.decode(toAddress.getText());
        Integer ledgerId = BlockchainData.getInstance().getTransactionLedgerFX().get(0).getLedgerId();

        Transaction transaction = new Transaction(
                WalletData.getInstance().getWallet(), sendB, Integer.parseInt(value.getText()), ledgerId, signature);

        BlockchainData.getInstance().addTransaction(transaction, true);
        BlockchainData.getInstance().addTransactionState(transaction);
    }
}
