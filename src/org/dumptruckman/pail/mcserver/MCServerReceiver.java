/*
 * MCServerReceiver.java
 */

package org.dumptruckman.pail.mcserver;

import org.dumptruckman.pail.Pail;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Observable;
import java.util.Observer;
import java.io.*;

/**
 *
 * @author dumptruckman
 */
public class MCServerReceiver extends TimerTask {

    public MCServerReceiver(Pail pail) {
        this.pail = pail;
        serverRunning = true;
    }

    @Override public void run() {
        if (serverRunning) {
            receivedFromServer = "";
            try {
                while (pail.server.br.ready()) {
                    try {
                        receivedFromServer = pail.server.br.readLine();
                    } catch (IOException e) {
                        System.out.println("ServerReceiver reports BufferedReader IOException while trying to readLine().");
                    }
                    //System.out.println(receivedFromServer); // Testing
                    if ((!receivedFromServer.equals("")) && (!receivedFromServer.equals(">")) && (!receivedFromServer.equals(">>")) && (!receivedFromServer.equals(">>>"))) {
                        receivedFromServer += System.getProperty("line.separator");
                        //receivedFromServer += "\\n";
                        // This part helps me find special characters
                        /*for (int i=0; i < receivedFromServer.length(); i++) {
                            System.out.print(receivedFromServer.codePointAt(i) + " ");
                        }
                        System.out.println();*/
                        if ((receivedFromServer != null) && (!receivedFromServer.equals("null\n"))) {
                            pail.addTextToConsoleOutput(receivedFromServer);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("ServerReceiver reports BufferedReader IOException while waiting for ready().  Assuming server ended.");
                serverRunning = false;
            }
        } else {
            this.cancel();
        }
    }

    public void update(Observable o, Object arg) {
        if (arg.equals("serverStopped")) {
            serverRunning = false;
        }
        if (arg.equals("serverStarted")) {
            serverRunning = true;
        }
    }

    public void setServerRunning(boolean b) {
        serverRunning = b;
    }

    private Pail pail;
    private String receivedFromServer;
    private boolean serverRunning;
}
