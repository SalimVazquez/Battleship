package model;

import java.net.*;
import java.util.concurrent.Semaphore;
import java.io.*;
import java.util.Observable;
import java.util.Observer;

public class ChatClient extends Observable implements Observer{

    private String hostname;
    private int port;
    private String userName;
    ReadThread readThread;
    WriteThread writeThread;
    Thread hiloReadThread;
    Thread hiloWriteThread;

    public ChatClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void execute() {
        try {
            Semaphore mutex = new Semaphore(0);
            Semaphore turno;
            Socket socket = new Socket(hostname, port);
            System.out.println("Connected to the chat server");
            readThread = new ReadThread(socket, mutex); // instance creation
            readThread.addObserver(this); // setting observer
            hiloReadThread = new Thread(readThread); // preparing thread
            hiloReadThread.start(); // starting thread
            try { mutex.acquire(); } catch (Exception e) { }
            String playerName = readThread.getPlayerName(); // getting name player
            turno = readThread.getSemaphoreTurn();
            writeThread = new WriteThread(socket, this, playerName); // instance creation
            writeThread.addObserver(this); // setting observer
            writeThread.setSemaphoreTurn(turno);
            hiloWriteThread = new Thread(writeThread); // preparing thread
            hiloWriteThread.start(); // starting thread
        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O Error: " + ex.getMessage());
            this.setChanged();
            this.notifyObservers("No se establecion conexion con el servidor");
        }
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return this.userName;
    }

    public void sendCoordAtack(String coord) {
        writeThread.sendCoordAtack(coord);
    }

    public void sendMessageToEnemy(String msg) {
        writeThread.sendMessageToEnemy(msg);
    }

    public void closeConnection() {
        writeThread.closeConnection();
    }

    public void update (Observable o, Object arg) {
        this.setChanged();
        this.notifyObservers(String.valueOf(arg));
    }
}