/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.dumptruckman.pail.task;

import com.dumptruckman.pail.Pail;
import com.dumptruckman.pail.task.event.EventModel;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;

/**
 *
 * @author dumptruckman
 */
public class Task implements Job {

    //Possibly not thread safe!

    @Override public void execute(JobExecutionContext context)
        throws JobExecutionException {
        EventModel event = (EventModel)context.getMergedJobDataMap().get("Event");
        Pail pail = (Pail)context.getMergedJobDataMap().get("Pail");

        List<ServerWarning> warninglist = event.getWarningList().toList();
        if (!warninglist.isEmpty()) {
            java.util.Collections.sort(warninglist);
            for (int i = 0; i < warninglist.size(); i++) {
                pail.sendInput(warninglist.get(i).getMessage());
                int sleeptime;
                if (i+1 < warninglist.size()) {
                    sleeptime = warninglist.get(i).getTime() -
                            warninglist.get(i+1).getTime();
                } else {
                    sleeptime = warninglist.get(i).getTime();
                }
                try {
                    Thread.sleep(sleeptime * 1000);
                } catch (InterruptedException ie) {
                    System.out.println("Warning sleep interrupted");
                }
            }
        }

        if (event.getTask().equals("Start Server")) {
            waitForBackupFinish(pail);
            pail.startServer();
        } else if (event.getTask().equals("Send Command")) {
            pail.sendInput(event.getParams().get(0));
        } else if (event.getTask().equals("Stop Server")) {
            System.out.println("Stop Server Task");
            waitForBackupFinish(pail);
            pail.stopServer();
        } else if (event.getTask().equals("Restart Server")) {
            waitForBackupFinish(pail);
            System.out.println("Restart task");
            if (!event.getParams().isEmpty()) {
                pail.restartServer(Integer.valueOf(event.getParams().get(0)));
            } else {
                pail.restartServer();
            }
        } else if (event.getTask().equals("Backup")) {
            waitWhileRestarting(pail);
            waitForCheckingFinish(pail);
            pail.backup();
        } else if (event.getTask().equals("Save Worlds")) {
            waitWhileRestarting(pail);
            waitForBackupFinish(pail);
            pail.sendInput("save-all");
        }
    }

    private void waitForBackupFinish(Pail pail) {
        while (pail.getControlState().equals("BACKUP") || pail.getControlState().equals("!BACKUP")) {
            try {
                System.out.println("Waiting for server to finish backing up.");
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                System.out.println("Interrupted while waiting for server to finish backing up.");
            }
        }
    }

    private void waitForCheckingFinish(Pail pail) {
        while (pail.isPropagatingChecks()) {
            try {
                System.out.println("Waiting for server to finish propagating checks.");
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                System.out.println("Interrupted while waiting for server to finish propagating checks.");
            }
        }
    }

    private void waitWhileRestarting(Pail pail) {
        while (pail.isRestarting()) {
            try {
                System.out.println("Waiting while server is restarting.");
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                System.out.println("Interrupted while waiting for server to restart.");
            }
        }
    }
}
