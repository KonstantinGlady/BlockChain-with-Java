package com.company.Threads;

import com.company.Model.Block;
import com.company.ServceData.BlockchainData;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PeerClient extends Thread {

    private final Queue<Integer> queue = new ConcurrentLinkedQueue<>();
    public PeerClient() {
        queue.add(6001);
        queue.add(6002);
    }
    @Override
    public void run() {
        while (true) {
            try (Socket socket = new Socket("127.0.0.1", queue.peek())) {

                System.out.println("Sending blockchain object on port: " + queue.peek());
                queue.add(queue.poll());
                socket.setSoTimeout(5000);

                ObjectOutputStream objectOutput = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream objectInput = new ObjectInputStream(socket.getInputStream());

                LinkedList<Block> blockchain = BlockchainData.getInstance().getCurrentBlockchain();
                objectOutput.writeObject(blockchain);

                LinkedList<Block> returnedBlockchain = (LinkedList<Block>) objectInput.readObject();
                System.out.println("Returned BC ledgerId " + returnedBlockchain.getLast().getLedgerId() + " size " +
                        returnedBlockchain.getLast().getTransactionLedger().size());

                BlockchainData.getInstance()
                        .getBlockchainConsensus(returnedBlockchain);
                Thread.sleep(2000);
            } catch (SocketTimeoutException e) {

                System.out.println("The socket is timeout");
                queue.add(queue.poll());
            } catch (IOException e) {

                System.out.println("Client error : " +
                        e.getMessage() + " error on port: " +
                        queue.peek());
                queue.add(queue.poll());
            } catch (InterruptedException | ClassNotFoundException e) {

                e.printStackTrace();
                queue.add(queue.peek());
            }
        }
    }
}
