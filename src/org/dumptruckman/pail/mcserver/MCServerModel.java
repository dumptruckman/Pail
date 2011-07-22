/*
 * MCServerModel.java
 */

package org.dumptruckman.pail.mcserver;

import java.io.*;
import javax.swing.SwingUtilities;
import java.util.Observable;
import java.util.Observer;
import java.util.List;
import java.util.Timer;

import org.dumptruckman.pail.Pail;
import org.hyperic.sigar.ptql.ProcessFinder;
import org.hyperic.sigar.Sigar;
import org.dumptruckman.pail.proxyserver.ProxyServer;
import org.dumptruckman.pail.config.ServerProperties;
import org.dumptruckman.pail.proxyserver.Player;

/**
 *
 * @author dumptruckman
 */
public class MCServerModel implements java.beans.PropertyChangeListener {
    
    public MCServerModel(Pail pail)
    {
        this.pail = pail;
        this.serverRunning = false;
    }

    public void setPail(Pail pail) {
        this.pail = pail;
    }

    public void setServerProps(ServerProperties sp) {
        serverProps = sp;
    }

    // Method for building the cmdLine
    public void setCmdLine(List<String> args) {
        cmdLine = args;
    }

    public void banKick(String name, String msg) {
        if (name != null) {
            pail.sendInput("ban " + name);
            Player p = proxyServer.playerList.findPlayer(name);
            if (p != null) {
                p.kick(msg);
            }
        }
    }

    public void banKick(String name) {
        banKick(name, "Banned!");
    }

    public void banKickIP(String ipAddress, String reason) {
        pail.sendInput("banip " + ipAddress);
        for (Player player : proxyServer.playerList.getArray()) {
            if (player.getIPAddress().equals(ipAddress)) {
                player.kick(reason);
            }
        }
    }

    public void banKickIP(String ipAddress) {
        banKickIP(ipAddress, "Banned!");
    }

    // Method for starting the server
    public String start() {
        timer = null;
        File jar = new File(pail.config.cmdLine.getServerJar());
        if (pail.config.getProxy()) {
            proxyServer = new ProxyServer(pail, serverProps);
            if (proxyServer.getStartCode() == -1) {
                pail.guiLog("Proxy Server failed to starts correctly.  Aborting"
                        + " server fstart.", Pail.LogLevel.SEVERE);
                return "ERROR";
            } else {
                // continue
            }
        }
        try {
            // Run the server
            ProcessFinder pf = new ProcessFinder(new Sigar());
            ProcessBuilder pb = new ProcessBuilder(cmdLine);
            pb.redirectErrorStream(true);
            ps = null;
            try {
                long[] pidlistbefore = pf.find("State.Name.sw=java");
                ps = pb.start();
                long[] pidlistafter = pf.find("State.Name.sw=java");
                if (pidlistafter.length - pidlistbefore.length == 1) {
                    pid = pidlistafter[pidlistafter.length-1];
                    pail.pailWorker.setServerPid(pid);
                } else {
                    pid = 0;
                    pail.pailWorker.setServerPid(-1);
                }
            } catch (UnsatisfiedLinkError ule) { 
                if (ps == null) {
                    ps = pb.start();
                }
            }

            receivedFromServer = "";

            // Collect necessary streams
            br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
            osw = new OutputStreamWriter(ps.getOutputStream());

            serverReceiver = new MCServerReceiver(pail);
            timer = new java.util.Timer();
            timer.scheduleAtFixedRate(serverReceiver, 0, 50);

            // Flag this as started
            serverRunning = true;
            pail.serverStarted();
           
            return "SUCCESS";
        } catch (Exception e) {
            pail.guiLog("Unknown error occured while launching server.",
                    Pail.LogLevel.SEVERE);
            if (ps != null) {
                ps.destroy();
            }
            return "ERROR";
        }
    }

    /*
    public void update(Observable o, Object arg) {
        receivedFromServer = serverReceiver.get();
        this.setChanged();
        notifyObservers("newOutput");
    }*/

    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (evt.getNewValue().equals(false)) {
            serverRunning = false;
            pid = 0;
            //guiServer.stop();
            if (pail.config.getProxy()) {
                proxyServer.stop();
            }
            serverReceiver.setServerRunning(false);
            pail.serverStopped();
            pail.pailWorker.setServerPid(pid);
        }
    }

    public long getPid() {
        return pid;
    }

    public boolean isRunning() {
        return serverRunning;
    }

    // Method for sending commands to the server
    public void send(final String string) {
        if (osw != null) {
            try {
                osw.write(string + "\n");
                osw.flush();
            } catch (IOException e) {
                pail.guiLog("Error sending server input.  Server is likely not"
                        + " running!", Pail.LogLevel.WARNING);
            }
        }
    }

    // Method for stopping server
    public void stop() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (isRunning()) {
                    send("stop");
                    MCServerStopper serverStopper = new MCServerStopper(ps, br, osw);
                    serverStopper.addPropertyChangeListener(MCServerModel.this);
                    serverStopper.execute();
                }
            }
        });
    }

    private Process ps;
    private long pid;
    private List<String> cmdLine;
    private String receivedFromServer;
    public BufferedReader br;
    private OutputStreamWriter osw;
    private MCServerReceiver serverReceiver;
    //private Config config;
    private ProxyServer proxyServer;
    private boolean serverRunning;
    private Pail pail;
    private ServerProperties serverProps;
    private Timer timer;
}
