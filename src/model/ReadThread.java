package model;

import java.io.*;
import java.net.*;
import java.util.Observable;
import java.util.concurrent.Semaphore;

public class ReadThread extends Observable implements Runnable{

    private BufferedReader reader;
    private String playerName = "";
    private Semaphore turno;
    private Semaphore mutex;

    public ReadThread(Socket socket,  Semaphore mutex) {
        this.mutex = mutex;
        try {
            InputStream input = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));
        } catch (IOException ex) {
            System.out.println("Error getting input stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void run() {
        try {
            String response = reader.readLine();
            System.out.println("\n" + response);
            if(response.equals("No other users connected"))
                playerName = "player 1";
            else
                playerName = "player 2";
            turno = new Semaphore(0);
            mutex.release();
        } catch (Exception e) { }

        while (true) {
            try {
                String serverMsg = reader.readLine(); // read message from server
                System.out.println("\nServer msg >>" + serverMsg); // print message from server
                if (serverMsg.contains("Atacando") || serverMsg.contains("New user connected")) { // enemy turn done, so u can attack
                    turno.release();
                    this.setChanged();
                    this.notifyObservers("ya es tu turno "+playerName);
                    String data[] = serverMsg.strip().split(":");
                    if (data.length > 2) {
                        if (data[1].contains("Atacando")) { // msg when the enemy attacks
                            this.setChanged();
                            this.notifyObservers("el enemigo te ataco:"+data[2]);
                        }
                    }
                }else if (serverMsg.contains("destruiste mi barco")) {
                    String data[] = serverMsg.strip().split(":");
                    // message when the enemy say´s that u destroyed his ship
                    this.setChanged();
                    this.notifyObservers("destruiste un barco enemigo:"+data[2]); // data[2] contains missile coords
                }else if (serverMsg.contains("fallaste el tiro")) {
                    String data[] = serverMsg.strip().split(":");
                    // message when the enemy say´s that u not destroyed his ship
                    this.setChanged();
                    this.notifyObservers("falle mi tiro:"+data[2]); // data[2] contains missile coords
                }else if (serverMsg.contains("Has perdido")) {
                    this.setChanged();
                    this.notifyObservers("He perdido la partida");
                }
            } catch (IOException ex) {
                System.out.println("Error reading from server: " + ex.getMessage());
                ex.printStackTrace();
                break;
            }
        }
    }

    public String getPlayerName(){
        return playerName;
    }

    public Semaphore getSemaphoreTurn(){
        return turno;
    }
}