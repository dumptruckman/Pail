/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.dumptruckman.pail.backup;

import com.dumptruckman.pail.Pail;

import javax.swing.*;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.io.*;
import java.util.List;
import java.util.Observable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author Roton
 */
public class Backup extends Observable {

    public Backup(Pail pail) {
        this.pail = pail;
        task = new BackupTask();
        nl = System.getProperty("line.separator");
        fs = System.getProperty("file.separator");
        try {
            workingDir = new File(".").getCanonicalPath();
        } catch (IOException ioe) {
            System.out.println("Error retrieving working dir");
        }
    }

    public void startBackup() {
        new File(pail.config.backups.getPath()).mkdir(); // Creates backup directory if it doesn't exist
        pail.backupStatusLog.setText("");
        task.execute();
    }

    public void addTextToBackupLog(String textToAdd) {
        final String text = textToAdd;
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                try
                {
                    ((HTMLEditorKit)pail.backupStatusLog.getEditorKit())
                            .insertHTML((HTMLDocument)pail.backupStatusLog.getDocument(),
                            pail.backupStatusLog.getDocument().getEndPosition().getOffset()-1,
                            text,
                            1, 0, null);
                } catch ( Exception e ) {
                    System.out.println("Error appending text to console output");
                }
                pail.backupStatusLog.setCaretPosition(pail.backupStatusLog.getDocument().getLength());
            }
        });
    }

    public class BackupTask extends SwingWorker<Boolean, Integer> {

        public Double totalSize;
        public Double currentTotal;

        public BackupTask() {
            totalSize = (double)0;
            currentTotal = (double)0;
        }

        public Boolean doInBackground() {
            List<File> files = pail.config.backups.getPathsModel().toList();
            if (!files.isEmpty()) {

                String now = java.util.Calendar.getInstance().getTime().toString().replaceAll(":", ".");
                File backupDir = new File(pail.config.backups.getPath());

                addTextToBackupLog("Calculating total size of data..." + nl);
                pail.statusBarJob.setText("Calculating...");
                pail.progressBar.setIndeterminate(true);
                // Calculate size
                for (File file : files) {
                    totalSize += getFileSize(file, backupDir);
                }
                pail.progressBar.setMaximum((int)Math.ceil(totalSize / 1024));

                pail.statusBarJob.setText("Backing up:");
                pail.progressBar.setIndeterminate(false);
                ZipOutputStream zipout = null;

                File currentBackupDir = new File(pail.config.backups.getPath() + fs + now);
                if (!pail.config.backups.getZip()) {
                    if (currentBackupDir.mkdir()) {
                        addTextToBackupLog("Created backup folder " + currentBackupDir.toString() + nl);
                    } else {
                        addTextToBackupLog("<font color=red>Failed to create backup folder " + currentBackupDir.toString() + nl);
                        return false;
                    }
                }

                if (pail.config.backups.getZip()) {
                    addTextToBackupLog(nl + "<br>Zipping files to " + currentBackupDir.getName() + ".7z...");
                    try {
                        zipout = new ZipOutputStream(
                                new BufferedOutputStream(
                                new FileOutputStream(
                                pail.config.backups.getPath() + fs + currentBackupDir.getName() + ".7z")));

                    } catch (FileNotFoundException e) {
                        addTextToBackupLog("<font color=red>failure! Could not find files to compress!" + nl);
                        return true;
                    }
                }
                
                // Perform the backup without compression
                for (File file : files) {
                    backup(new File(file.toString().replace(workingDir, ".")), currentBackupDir, zipout);
                }

                // Close the zip stream if opened
                if (zipout != null) {
                    try {
                        zipout.close();
                        addTextToBackupLog(nl + "<font color=green>Successfully created " + currentBackupDir.getName() + ".7z!" + nl);
                    } catch (IOException e) {
                        addTextToBackupLog(nl + "<font color=red>Error creating " + currentBackupDir.getName() + ".7z!" + nl);
                    }
                }

                // Delete the server log if set to do so
                if (pail.config.backups.getClearLog()) {
                    addTextToBackupLog("Deleting server.log...");
                    if (new File("./server.log").delete()) {
                        addTextToBackupLog("<font color=green>Success!");
                    } else {
                        addTextToBackupLog("<font color=red>Failed!");
                    }
                }

                return true;
            } else {
                addTextToBackupLog("Nothing selected to backup!" + nl);
                return true;
            }
        }

        public void done() {
            pail.progressBar.setValue(0);
            try {
                if (get()) {
                    addTextToBackupLog(nl + "<font color=green>Backup operation completed succesfully!");
                } else {
                    addTextToBackupLog(nl + "<font color=red>Backup operation encountered an error.  Aborting.");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (java.util.concurrent.ExecutionException e) {
                e.printStackTrace();
            } catch (java.util.concurrent.CancellationException e) {
                addTextToBackupLog("<font color=red>Backup operation was cancelled!");
                this.cancel(true);
            }
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (pail.server.isRunning()) {
                        pail.sendInput("say Server backup complete!");
                        pail.sendInput("save-on");
                    }
                    pail.controlSwitcher("!BACKUP");
                }
            });
        }
    }

    public long getFileSize(File folder, File backupfolder) {
        long foldersize = 0;
        try {
            if (folder.getCanonicalPath().equals(backupfolder.getCanonicalPath())) {
                return 0;
            }
        } catch (IOException ignore) { }

        File[] filelist = folder.listFiles();
        if (filelist != null) {
            for (int i = 0; i < filelist.length; i++) {
                if (filelist[i].isDirectory()) {
                    foldersize += getFileSize(filelist[i], backupfolder);
                } else {
                    foldersize += filelist[i].length();
                }
            }
        } else {
            foldersize += folder.length();
        }
        return foldersize;
    }

    private void backup(File backupfrom, File backupfolder, ZipOutputStream zipout) {
        double progress;
        task.currentTotal += backupfrom.length();
        progress = task.currentTotal / 1024;
        byte[] tmpBuf = new byte[1024];
        try {
            // Get any showstoppers out of the way first
            if (!backupfrom.getAbsolutePath().contains(workingDir)) {
                addTextToBackupLog("<font color=red>You may not backup items in directories above Pail's. Skipping..." + nl);
                pail.progressBar.setValue((int)progress);
                return;
            }
            if (backupfrom.getCanonicalPath().equals(backupfolder.getParentFile().getCanonicalPath())) {
                addTextToBackupLog("<font color=red>You may not backup your backup folder! Skipping..." + nl);
                task.currentTotal -= backupfrom.length();
                progress = task.currentTotal / 1024;
                pail.progressBar.setValue((int)progress);
                return;
            }
            if (!backupfrom.exists()) {
                addTextToBackupLog("<font color=red>Error copying \"" + backupfrom.getPath() + "\". File does not exist! Skipping..." + nl);
                pail.progressBar.setValue((int)progress);
                return;
            }
            if (!backupfrom.canRead()) {
                addTextToBackupLog("<font color=red>Error copying \"" + backupfrom.getPath() + "\". File is unreadable! Skipping..." + nl);
                pail.progressBar.setValue((int)progress);
                return;
            }

            File[] files = backupfrom.listFiles();
            if (files == null || files.length == 0) {
                // Is not a directory or is an empty directory
                java.util.List<File> backupto = new java.util.ArrayList<File>();
                // Specify path for the new file created in backup dir
                backupto.add(new File(backupfolder.getPath() + backupfrom.getPath().replaceFirst(".", "")));
                addTextToBackupLog(nl + "Backing up \"" + backupfrom.getPath() + "\"...");

                // The following segment ensures that all the proper parent files exist (if not zipping)
                if (zipout == null) {
                    int j = 0;
                    while (!backupto.get(j).getPath().equals(backupfolder.getPath())) {
                        // Continue getting parenting dir until reaching the backup dir
                        backupto.add(backupto.get(j).getParentFile());
                        j++;
                    }
                    // Cycle backwards through these dirs and create them into the backup folder
                    j = backupto.size() - 1;
                    while (j != 0) {
                        backupto.get(j).mkdir();
                        j--;
                    }
                }

                if (backupfrom.isDirectory()) {
                    if (zipout == null) {
                        // file to backup is a directory (and not zipping)
                        if (backupto.get(0).mkdir()) {
                            addTextToBackupLog("<font color=green>success!" + nl);
                        } else {
                            addTextToBackupLog("<font color=red>failure! Can not create directory! Skipping..." + nl);
                        }
                    }
                } else {
                    // file to backup is NOT a directory
                    if (!backupto.get(0).getParentFile().canWrite() && zipout == null) { // Ensure can write to the folder new file is going
                        addTextToBackupLog("<font color=red>failure! Can not write to parent directory!" + nl);
                        pail.progressBar.setValue((int)progress);
                        return;
                    }

                    // Perform file copy
                    FileInputStream from = null;
                    FileOutputStream to = null;
                    try {
                        from = new FileInputStream(backupfrom);
                        if (zipout == null) {
                            to = new FileOutputStream(backupto.get(0));
                            byte[] buffer = new byte[4096];
                            int bytesRead;

                            while ((bytesRead = from.read(buffer)) != -1) {
                                to.write(buffer, 0, bytesRead); // write
                            }
                        } else {
                            String formattedname = backupfrom.toString().replace(workingDir + fs, "");
                            formattedname = formattedname.replace("." + fs, "");
                            zipout.putNextEntry(new ZipEntry(formattedname));
                            int len;
                            while ((len = from.read(tmpBuf)) > 0) {
                                zipout.write(tmpBuf, 0, len);
                            }
                            zipout.closeEntry();
                        }
                        addTextToBackupLog("<font color=green>success!" + nl);
                    } catch (FileNotFoundException e) {
                        addTextToBackupLog("<font color=red>failure! Could not find file! Skipping..." + nl);
                    } catch (IOException e) {
                        addTextToBackupLog("<font color=red>failure! Error copying file! Skipping..." + nl);
                    } finally {
                        if (from != null) {
                            try {
                                from.close();
                            } catch (IOException e) {
                                addTextToBackupLog("<font color=red>Error closing file stream! Continuing..." + nl);
                            }
                        }
                        if (to != null) {
                            try {
                                to.close();
                            } catch (IOException e) {
                                addTextToBackupLog("<font color=red>Error closing file stream! Continuing..." + nl);
                            }
                        }
                    }
                }
            } else { 
                // Path is directory (AND contains files), cycle through it to backup all files within.
                for (int i = 0; i < files.length; i++) {
                    backup(files[i], backupfolder, zipout);
                }
            }
        }catch (IOException ioe) {
            addTextToBackupLog("<font color=red>Error attempting backup!");
            System.err.println("Cannot retrieve canonical backup path");
        }
        pail.progressBar.setValue((int)progress);
    }

    private String nl;
    private String fs;
    private String workingDir;
    private int depth;
    private Pail pail;
    public BackupTask task;
}
