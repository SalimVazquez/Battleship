package model;

import java.io.*;
import java.net.*;
import java.util.Observable;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class WriteThread extends Observable implements Runnable{

    private PrintWriter writer;
    private Socket socket;
    private ChatClient client;
    private Semaphore turno;
    private Semaphore auxTurno = new Semaphore(0);
    private String playerName;
    private boolean isGameEnded = false;

    public WriteThread(Socket socket, ChatClient client, String playername) {
        this.socket = socket;
        this.client = client;
        this.playerName = playername;
        try {
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
        } catch (IOException ex) {
            System.out.println("Error getting output stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void run() {
        Console console = System.console();
        Scanner sc = new Scanner(System.in);
        String userName = playerName;
        client.setUserName(userName);
        writer.println(userName);
        do {
            try {
                turno.acquire();
                System.out.println("\n-->[" + userName + "]: can shoot ");
                auxTurno.acquire();
            } catch (Exception e) {  }
        } while (!isGameEnded);
    }

    public void setSemaphoreTurn(Semaphore turno){
        this.turno = turno;
    }

    public void sendCoordAtack(String coord){
        writer.println(coord);
        try { auxTurno.release(); } catch (Exception e) {}
        this.setChanged();
        this.notifyObservers("ya no es tu turno "+playerName);
    }

    public void sendMessageToEnemy(String msg){
        writer.println(msg);
    }

    public void closeConnection(){
        try {
            socket.close();
        } catch (IOException ex) {
            System.out.println("Error writing to server: " + ex.getMessage());
        }
    }
}