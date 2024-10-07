package com.company.Threads;

import com.company.ServceData.BlockchainData;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class MiningThread extends Thread {
    @Override
    public void run() {
        while (true) {

            long lastMinedBlock = LocalDateTime.parse(BlockchainData.getInstance()
                    .getCurrentBlockchain().getLast().getTimeStamp()).toEpochSecond(ZoneOffset.UTC);

            if (lastMinedBlock + BlockchainData.getTimeoutInterval() >
                    LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)) {

                System.out.println("Blockchain is too old for mining. Update it from peers");
            } else if (lastMinedBlock + BlockchainData.getTimeoutInterval() -
                    LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) > 0) {

                System.out.println("Blockchain is current. Will commence in : " +
                        (lastMinedBlock + BlockchainData.getTimeoutInterval() -
                                LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)) + " seconds");
            } else {

                System.out.println("MINING NEW BLOCK");
                BlockchainData.getInstance().mineBlock();
                System.out.println(BlockchainData.getInstance().getWalletBalanceFX());
            }

            System.out.println(LocalDateTime.parse(BlockchainData.getInstance()
                    .getCurrentBlockchain().getLast().getTimeStamp()).toEpochSecond(ZoneOffset.UTC));

            try {
                Thread.sleep(2000);
                if (BlockchainData.getInstance().isExit()) {
                    break;
                }

                BlockchainData.getInstance().setMiningPoints(BlockchainData.getInstance().getMiningPoints() + 2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
