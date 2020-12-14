package model;

import java.util.Observable;

public class Board extends Observable{

    String [][] my_board = new String [5][5];
    int cont = 0;

    public Board() {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                my_board[i][j] = ""+cont;
                cont ++;
            }
        }
    }

    public void setPositionShip(int coordX, int coordY) {
        my_board[coordX][coordY] = "ship";
    }

    public boolean markMyBoard(int coordX, int coordY) { // mark when enemy attack
        if (my_board[coordY][coordX].equals("ship")) { // the coordenades are inverted
            my_board[coordY][coordX] = "ship_destroyed";
            return true;
        } else {
            my_board[coordY][coordX] = "missile_failed";
            this.notifyObservers("misil fallido:("+coordX+","+coordY+")");
            return false;
        }
    }

    public boolean isCoordUsed(int coordX, int coordY) {
        if (my_board[coordX][coordY].equals("ship")) {
            System.out.println("cordenada usada");
            return true;
        } else {
            System.out.println("cordenada no usada");
            return false;
        }
    }
}