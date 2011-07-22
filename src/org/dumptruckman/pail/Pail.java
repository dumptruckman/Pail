/*
 * GUI.java
 */
package org.dumptruckman.pail;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLDocument;

//import it.cnr.imaa.essi.lablib.gui.checkboxtree.CheckboxTree;
//import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel;
import net.miginfocom.swing.MigLayout;
import org.dumptruckman.pail.listmodel.GUIListModel;
import org.quartz.*;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import org.dumptruckman.pail.backup.Backup;
import org.dumptruckman.pail.config.Config;
import org.dumptruckman.pail.tools.ConsoleParser;
import org.dumptruckman.pail.tools.RegexVerifier;
import org.dumptruckman.pail.mcserver.MCServerModel;
import org.dumptruckman.pail.task.TaskDialog;
import org.dumptruckman.pail.config.ServerProperties;
import org.dumptruckman.pail.proxyserver.PlayerList;
import org.dumptruckman.pail.webinterface.WebInterface;
import org.dumptruckman.pail.task.event.EventModel;
import static org.dumptruckman.pail.task.event.EventScheduler.*;
import static org.dumptruckman.pail.tools.TimeTools.*;

/**
 * The application's main frame.
 */
public class Pail extends javax.swing.JFrame implements ComponentListener {

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Pail().setVisible(true);
            }
        });
    }

    private static final int MIN_WIDTH = 451;
    private static final int MIN_HEIGHT = 521;

    public Pail() {
        config = new Config();
        initConfig();
        wantsToQuit = false;

        initScheduler();

        addComponentListener(this);
        

        UIManager.put("TitledBorder.border", new BorderUIResource(new EtchedBorder()));
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, you can set the Pail to another look and feel.
        }

        // Sets model for backup file check box tree
        backupFileSystem = new org.dumptruckman.pail.fileexplorer.FileSystemModel(".");
        // Sets model for player list
        playerListModel = new PlayerList();
        backupFileListModel = new GUIListModel<File>();
        // Initializes the custom Button Combo Boxes
        customButtonBoxModel1 = new javax.swing.DefaultComboBoxModel();
        customButtonBoxModel1.addElement("Edit Tasks");
        customButtonBoxModel2 = new javax.swing.DefaultComboBoxModel();
        customButtonBoxModel2.addElement("Edit Tasks");
        propagatingChecks = false;



        // Pail starts unhidden, indication of that:
        isHidden = false;

        serverProperties = new ServerProperties();

        server = new MCServerModel(this);
        pailWorker = new PailWorker(this);
        server.setServerProps(serverProperties);
        setTitle(config.getWindowTitle());
        controlSwitcher("OFF");

        inputHistory = new ArrayList<String>();
        inputHistoryIndex = -1;

        enableSystemTrayIcon();

        parser = new ConsoleParser(config.display, this);

        webServer = new WebInterface(this);
        schedulePaused = false;

        initComponents();
        updateGuiWithConfigValues();
        updateGuiWithServerProperties();
        initSchedule();

        saveConfig();

        pailWorker.startMainWorker();
        
        // Sets a control+s hotkey for the say checkbox
        sayCheckBox.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK),"sayOn");
        sayCheckBox.getActionMap().put("sayOn", sayToggle);
        // Sets shift+enter as a hotkey to reverse the say setting.
        consoleInput.getInputMap().put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, java.awt.event.InputEvent.SHIFT_MASK),"sayOn");
        consoleInput.getActionMap().put("sayOn", saySend);


        if (config.web.isEnabled()) {
            startWebServer();
        }

        if (config.getServerStartOnStartup()) {
            this.startServer();
        }

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                performExitSequence();
            }
        });

    }

    public void initScheduler() {
        scheduler = null;
        try {
            // Grab the Scheduler instance from the Factory
            scheduler =  new org.quartz.impl.StdSchedulerFactory().getScheduler();
            // and start it off
            scheduler.start();
        } catch (SchedulerException se) {
            se.printStackTrace();
        }
    }

    public void componentResized(ComponentEvent e) {
        int width = this.getWidth();
        int height = this.getHeight();

        boolean resize = false;
        if (width < MIN_WIDTH) {
            resize = true;
            width = MIN_WIDTH;
        }
        if (height < MIN_HEIGHT) {
            resize = true;
            height = MIN_HEIGHT;
        }
        if (resize) {
            this.setSize(width, height);
        }
    }

    public void componentMoved(ComponentEvent e) {
    }
    public void componentShown(ComponentEvent e) {
    }
    public void componentHidden(ComponentEvent e) {
    }

    public void showAboutBox() {
        if (aboutBox == null) {
            aboutBox = new AboutBox(this);
            aboutBox.setLocationRelativeTo(this);
        }
        aboutBox.setVisible(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {

        mainPanel = new JPanel();
        tabber = new JTabbedPane();
        //configTabber = new JTabbedPane();
        mainWindowTab = new JPanel();
        backupFileList = new JList();
        consoleOutputPanel = new JPanel();
        consoleOutScrollPane = new JScrollPane();
        consoleOutput = new JTextPane();
        playerListPanel = new JPanel();
        playerListScrollPane = new JScrollPane();
        playerList = new JList();
        consoleInputPanel = new JPanel();
        consoleInput = new JTextField();
        submitButton = new JButton();
        sayCheckBox = new JCheckBox();
        serverControlPanel = new JPanel();
        startstopButton = new JButton();
        saveWorldsButton = new JButton();
        customCombo1 = new JComboBox();
        customButton1 = new JButton();
        customCombo2 = new JComboBox();
        customButton2 = new JButton();
        serverInfoPanel = new JPanel();
        serverCpuUsageLabel = new JLabel();
        serverCpuUsage = new JLabel();
        serverMemoryUsageLabel = new JLabel();
        serverMemoryUsage = new JLabel();
        receivingBytesLabel = new JLabel();
        transmittingBytesLabel = new JLabel();
        receivingBytes = new JLabel();
        transmittingBytes = new JLabel();
        guiInfoPanel = new JPanel();
        versionLabel = new JLabel();
        guiCpuUsageLabel = new JLabel();
        guiCpuUsage = new JLabel();
        guiMemoryUsageLabel = new JLabel();
        guiMemoryUsage = new JLabel();
        useNetStat = new JCheckBox();
        configurationTab = new JPanel();
        serverCmdLinePanel = new JPanel();
        javaExecLabel = new JLabel();
        javaExecField = new JTextField();
        serverJarLabel = new JLabel();
        serverJarField = new JTextField();
        bukkitCheckBox = new JCheckBox();
        javaExecBrowseButton = new JButton();
        serverJarBrowseButton = new JButton();
        xmxMemoryLabel = new JLabel();
        xmxMemoryField = new JTextField();
        xincgcCheckBox = new JCheckBox();
        extraArgsLabel = new JLabel();
        extraArgsField = new JTextField();
        customCommandLineLabel = new JLabel();
        cmdLineField = new JTextField();
        customLaunchCheckBox = new JCheckBox();
        saveServerConfigButton = new JButton();
        useProxyCheckBox = new JCheckBox();
        extPortLabel = new JLabel();
        extPortField = new JTextField();
        intPortLabel = new JLabel();
        intPortField = new JTextField();
        serverPropertiesPanel = new JPanel();
        allowFlightCheckBox = new JCheckBox();
        allowNetherCheckBox = new JCheckBox();
        levelNameLabel = new JLabel();
        levelNameField = new JTextField();
        levelSeedLabel = new JLabel();
        levelSeedField = new JTextField();
        maxPlayersSpinner = new JSpinner();
        maxPlayersLabel = new JLabel();
        onlineModeCheckBox = new JCheckBox();
        pvpCheckBox = new JCheckBox();
        serverIpLabel = new JLabel();
        serverIpField = new JTextField();
        serverPortLabel = new JLabel();
        serverPortField = new JTextField();
        spawnAnimalsCheckBox = new JCheckBox();
        spawnMonstersCheckBox = new JCheckBox();
        spawnProtectionLabel = new JLabel();
        spawnProtectionField = new JTextField();
        viewDistanceLabel = new JLabel();
        viewDistanceSpinner = new JSpinner();
        whiteListCheckBox = new JCheckBox();
        themeTab = new JPanel();
        guiConfigPanel = new JPanel();
        windowTitleLabel = new JLabel();
        windowTitleField = new JTextField();
        inputHistoryMaxSizeLabel = new JLabel();
        inputHistoryMaxSizeField = new JTextField();
        startServerOnLaunchCheckBox = new JCheckBox();
        commandPrefixLabel = new JLabel();
        commandPrefixField = new JTextField();
        saveThemeButton = new JButton();
        colorizationPanel = new JPanel();
        textColorLabel = new JLabel();
        bgColorLabel = new JLabel();
        infoColorLabel = new JLabel();
        warningColorLabel = new JLabel();
        severeColorLabel = new JLabel();
        textColorBox = new JTextField();
        bgColorBox = new JTextField();
        infoColorBox = new JTextField();
        warningColorBox = new JTextField();
        severeColorBox = new JTextField();
        textSizeLabel = new JLabel();
        textSizeField = new JSpinner();
        backupTab = new JPanel();
        backupButton = new JButton();
        backupSettingsPanel = new JPanel();
        backupPathLabel = new JLabel();
        backupPathField = new JTextField();
        backupPathBrowseButton = new JButton();
        zipBackupCheckBox = new JCheckBox();
        clearLogCheckBox = new JCheckBox();
        saveBackupControlButton = new JButton();
        backupFileChooserPanel = new JPanel();
        backupFileListScrollPane = new JScrollPane();
        backupAddButton = new JButton();
        backupRemoveButton = new JButton();
        backupStatusPanel = new JPanel();
        backupStatusLogScrollPane = new JScrollPane();
        backupStatusLog = new JTextPane();
        schedulerTab = new JPanel();
        taskSchedulerPanel = new JPanel();
        taskSchedulerScrollPane = new JScrollPane();
        taskSchedulerList = new JList();
        taskListAddButton = new JButton();
        taskListEditButton = new JButton();
        taskListRemoveButton = new JButton();
        pauseSchedulerButton = new JToggleButton();
        webInterfaceTab = new JPanel();
        webInterfaceConfigPanel = new JPanel();
        webPortLabel = new JLabel();
        webPortField = new JTextField();
        useWebInterfaceCheckBox = new JCheckBox();
        webPasswordLabel = new JLabel();
        webPasswordField = new JPasswordField();
        showWebPasswordButton = new JToggleButton();
        disableGetOutputNotificationsCheckBox = new JCheckBox();
        webLogPanel = new JPanel();
        webLogScrollPane = new JScrollPane();
        webLog = new JTextPane();
        menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu();
        JMenuItem exitMenuItem = new JMenuItem();
        JMenu helpMenu = new JMenu();
        JMenuItem aboutMenuItem = new JMenuItem();
        hideMenu = new JMenu();
        versionNotifier = new JMenu();
        launchSupportPage = new JMenuItem();
        viewChangeLog = new JMenuItem();
        downloadLatestVersion = new JMenuItem();
        jMenuItem1 = new JMenuItem();
        statusPanel = new JPanel();
        JSeparator statusPanelSeparator = new JSeparator();
        serverStatusLabel = new JLabel();
        statusAnimationLabel = new JLabel();
        progressBar = new JProgressBar();
        statusBarJob = new JLabel();

        ResourceBundle lang = ResourceBundle.getBundle("Pail");
        versionNumber = lang.getString("Application.version");

        mainPanel.setLayout(new MigLayout("fill", "0[]0", "0[]0"));

        tabber.setName("tabber");
        tabber.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent evt) {
                tabberKeyTyped(evt);
            }
        });
        mainPanel.add(tabber, "grow");
        {
            mainWindowTab.setName("mainWindowTab");
            mainWindowTab.setLayout(new MigLayout("fill", "0[33%]0[33%]0[33%]0", "0[]0[min!]0[min!]0"));
            tabber.addTab(lang.getString("mainWindowTab.TabConstraints.tabTitle"), mainWindowTab);
            {
                consoleOutputPanel.setBorder(BorderFactory.createTitledBorder(lang.getString("consoleOutputPanel.border.title")));
                consoleOutputPanel.setName("consoleOutputPanel");
                consoleOutputPanel.setLayout(new MigLayout("fill", "0[]0", "0[]0"));
                mainWindowTab.add(consoleOutputPanel, "width 80%, grow, span 3, split 2");
                {
                    consoleOutScrollPane.setName("consoleOutScrollPane");
                    consoleOutScrollPane.setViewportView(consoleOutput);
                    consoleOutputPanel.add(consoleOutScrollPane, "grow");
                    {
                        consoleOutput.setContentType(lang.getString("consoleOutput.contentType")); // NOI18N
                        consoleOutput.setEditable(false);
                        consoleOutput.setToolTipText(lang.getString("consoleOutput.toolTipText")); // NOI18N
                        consoleOutput.setCursor(new Cursor(Cursor.TEXT_CURSOR));
                        consoleOutput.setName("consoleOutput"); // NOI18N
                        consoleOutput.addMouseListener(new MouseAdapter() {
                            public void mouseClicked(MouseEvent evt) {
                                consoleOutputMouseClicked(evt);
                            }
                            public void mouseEntered(MouseEvent evt) {
                                consoleOutputMouseEntered(evt);
                            }
                            public void mouseExited(MouseEvent evt) {
                                consoleOutputMouseExited(evt);
                            }
                        });
                        consoleOutput.addFocusListener(new FocusAdapter() {
                            public void focusGained(FocusEvent evt) {
                                consoleOutputFocusGained(evt);
                            }
                            public void focusLost(FocusEvent evt) {
                                consoleOutputFocusLost(evt);
                            }
                        });
                        consoleOutput.addKeyListener(new KeyAdapter() {
                            public void keyTyped(KeyEvent evt) {
                                consoleOutputKeyTyped(evt);
                            }
                        });
                    }
                }

                playerListPanel.setBorder(BorderFactory.createTitledBorder(lang.getString("playerListPanel.border.title")));
                playerListPanel.setName("playerListPanel");
                playerListPanel.setLayout(new MigLayout("fill", "0[]0", "0[]0"));
                mainWindowTab.add(playerListPanel, "gapleft -5, width 20%, grow, wrap");
                {
                    playerListScrollPane.setName("playerListScrollPane");
                    playerListScrollPane.setViewportView(playerList);
                    playerListPanel.add(playerListScrollPane, "grow");
                    {
                        playerList.setModel(playerListModel);
                        playerList.setToolTipText(lang.getString("playerList.toolTipText"));
                        playerList.setFocusable(false);
                        playerList.setName("playerList");
                        playerList.addMouseListener(new MouseAdapter() {
                            public void mouseClicked(MouseEvent evt) {
                                playerListMouseClicked(evt);
                            }
                        });
                        playerList.addKeyListener(new KeyAdapter() {
                            public void keyTyped(KeyEvent evt) {
                                playerListKeyTyped(evt);
                            }
                        });
                    }
                }

                consoleInputPanel.setBorder(BorderFactory.createTitledBorder(lang.getString("consoleInputPanel.border.title")));
                consoleInputPanel.setName("consoleInputPanel");
                consoleInputPanel.setLayout(new MigLayout("fill", "0[]0", "0[]0"));
                mainWindowTab.add(consoleInputPanel, "grow, span 3, wrap");
                {
                    sayCheckBox.setText(lang.getString("sayCheckBox.text")); // NOI18N
                    sayCheckBox.setToolTipText(lang.getString("sayCheckBox.toolTipText")); // NOI18N
                    sayCheckBox.setName("sayCheckBox"); // NOI18N
                    sayCheckBox.addKeyListener(new KeyAdapter() {
                        public void keyTyped(KeyEvent evt) {
                            sayCheckBoxKeyTyped(evt);
                        }
                    });
                    consoleInputPanel.add(sayCheckBox, "growx 0, split 3");

                    consoleInput.setText(lang.getString("consoleInput.text")); // NOI18N
                    consoleInput.setToolTipText(lang.getString("consoleInput.toolTipText")); // NOI18N
                    consoleInput.setName("consoleInput"); // NOI18N
                    consoleInput.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            consoleInputActionPerformed(evt);
                        }
                    });
                    consoleInput.addKeyListener(new KeyAdapter() {
                        public void keyPressed(KeyEvent evt) {
                            consoleInputKeyPressed(evt);
                        }
                    });
                    consoleInputPanel.add(consoleInput, "growx");

                    submitButton.setText(lang.getString("submitButton.text")); // NOI18N
                    submitButton.setName("submitButton"); // NOI18N
                    submitButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            submitButtonActionPerformed(evt);
                        }
                    });
                    submitButton.addKeyListener(new KeyAdapter() {
                        public void keyTyped(KeyEvent evt) {
                            submitButtonKeyTyped(evt);
                        }
            });
                    consoleInputPanel.add(submitButton, "growx 0");
                }

                serverControlPanel.setBorder(BorderFactory.createTitledBorder(lang.getString("serverControlPanel.border.title")));
                serverControlPanel.setName("serverControlPanel");
                serverControlPanel.setLayout(new MigLayout("fill", "0[35%]0[55%]0[10%]0", "0[]0"));
                mainWindowTab.add(serverControlPanel, "grow");
                {
                    startstopButton.setText(lang.getString("startstopButton.text"));
                    startstopButton.setMargin(new Insets(2, 5, 2, 5));
                    startstopButton.setName("startstopButton");
                    startstopButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            startstopButtonActionPerformed(evt);
                        }
                    });
                    serverControlPanel.add(startstopButton, "growx");

                    saveWorldsButton.setText(lang.getString("saveWorldsButton.text"));
                    saveWorldsButton.setMargin(new Insets(2, 5, 2, 5));
                    saveWorldsButton.setName("saveWorldsButton");
                    saveWorldsButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            saveWorldsButtonActionPerformed(evt);
                        }
                    });
                    serverControlPanel.add(saveWorldsButton, "span 2, growx, wrap");

                    customCombo1.setModel(customButtonBoxModel1);
                    customCombo1.setToolTipText(lang.getString("customCombo1.toolTipText"));
                    customCombo1.setName("customCombo1");
                    serverControlPanel.add(customCombo1, "span 2, growx");

                    customButton1.setText(lang.getString("customButton1.text"));
                    customButton1.setToolTipText(lang.getString("customButton1.toolTipText"));
                    customButton1.putClientProperty("JComponent.sizeVariant", "small");
                    SwingUtilities.updateComponentTreeUI(customButton1);
                    customButton1.setMargin(new Insets(2, 2, 2, 2));
                    customButton1.setName("customButton1");
                    customButton1.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            customButton1ActionPerformed(evt);
                        }
                    });
                    serverControlPanel.add(customButton1, "right, growx, wrap");

                    customCombo2.setModel(customButtonBoxModel2);
                    customCombo2.setToolTipText(lang.getString("customCombo2.toolTipText"));
                    customCombo2.setName("customCombo2");
                    serverControlPanel.add(customCombo2, "span 2, growx");

                    customButton2.setText(lang.getString("customButton2.text"));
                    customButton2.setToolTipText(lang.getString("customButton2.toolTipText"));
                    customButton2.putClientProperty("JComponent.sizeVariant", "small");
                    SwingUtilities.updateComponentTreeUI(customButton2);
                    customButton2.setMargin(new Insets(2, 2, 2, 2));
                    customButton2.setName("customButton2");
                    customButton2.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            customButton2ActionPerformed(evt);
                        }
                    });
                    serverControlPanel.add(customButton2, "right, growx");
                }

                serverInfoPanel.setBorder(BorderFactory.createTitledBorder("Server Information"));
                serverInfoPanel.setName("serverInfoPanel");
                serverInfoPanel.setLayout(new MigLayout());
                mainWindowTab.add(serverInfoPanel, "grow");
                {
                    serverCpuUsageLabel.setText(lang.getString("serverCpuUsageLabel.text"));
                    serverCpuUsageLabel.setName("serverCpuUsageLabel");
                    serverInfoPanel.add(serverCpuUsageLabel);

                    serverCpuUsage.setText(lang.getString("serverCpuUsage.text"));
                    serverCpuUsage.setName("serverCpuUsage");
                    serverInfoPanel.add(serverCpuUsage, "wrap");

                    serverMemoryUsageLabel.setText(lang.getString("serverMemoryUsageLabel.text"));
                    serverMemoryUsageLabel.setName("serverMemoryUsageLabel");
                    serverInfoPanel.add(serverMemoryUsageLabel);

                    serverMemoryUsage.setText(lang.getString("serverMemoryUsage.text"));
                    serverMemoryUsage.setName("serverMemoryUsage");
                    serverInfoPanel.add(serverMemoryUsage, "wrap");

                    receivingBytesLabel.setText(lang.getString("receivingBytesLabel.text"));
                    receivingBytesLabel.setName("receivingBytesLabel");
                    serverInfoPanel.add(receivingBytesLabel);

                    receivingBytes.setText(lang.getString("receivingBytes.text"));
                    receivingBytes.setName("receivingBytes");
                    serverInfoPanel.add(receivingBytes, "wrap");

                    transmittingBytesLabel.setText(lang.getString("transmittingBytesLabel.text"));
                    transmittingBytesLabel.setName("transmittingBytesLabel");
                    serverInfoPanel.add(transmittingBytesLabel);

                    transmittingBytes.setText(lang.getString("transmittingBytes.text"));
                    transmittingBytes.setName("transmittingBytes");
                    serverInfoPanel.add(transmittingBytes);
                }

                guiInfoPanel.setBorder(BorderFactory.createTitledBorder("Pail Information"));
                guiInfoPanel.setName("guiInfoPanel");
                guiInfoPanel.setLayout(new MigLayout());
                mainWindowTab.add(guiInfoPanel, "grow");
                {
                    guiCpuUsageLabel.setText(lang.getString("guiCpuUsageLabel.text"));
                    guiCpuUsageLabel.setName("guiCpuUsageLabel");
                    guiInfoPanel.add(guiCpuUsageLabel, "");

                    guiCpuUsage.setText(lang.getString("guiCpuUsage.text"));
                    guiCpuUsage.setName("guiCpuUsage");
                    guiInfoPanel.add(guiCpuUsage, "wrap");

                    guiMemoryUsageLabel.setText(lang.getString("guiMemoryUsageLabel.text"));
                    guiMemoryUsageLabel.setName("guiMemoryUsageLabel");
                    guiInfoPanel.add(guiMemoryUsageLabel, "");

                    guiMemoryUsage.setText(lang.getString("guiMemoryUsage.text"));
                    guiMemoryUsage.setName("guiMemoryUsage");
                    guiInfoPanel.add(guiMemoryUsage, "wrap");

                    versionLabel.setText(lang.getString("versionLabel.text") + " " + lang.getString("Application.version"));
                    versionLabel.setName("versionLabel");
                    guiInfoPanel.add(versionLabel, "span 2, wrap");

                    useNetStat.setText(lang.getString("useNetStat.text"));
                    useNetStat.setToolTipText(lang.getString("useNetStat.toolTipText"));
                    useNetStat.setName("useNetStat");
                    guiInfoPanel.add(useNetStat, "span 2");
                }
            }

            configurationTab.setName("configurationTab");
            configurationTab.setLayout(new MigLayout("fill", "0[]0", "0[]0[]0[]0[min!]0"));
            tabber.addTab(lang.getString("configurationTab.TabConstraints.tabTitle"), configurationTab);
            {
                //@TODO configTabber.setName("configTabber");
                //configurationTab.add(configTabber, "grow");
                {
                    serverCmdLinePanel.setBorder(BorderFactory.createTitledBorder(lang.getString("serverCmdLinePanel.border.title"))); // NOI18N
                    serverCmdLinePanel.setName("serverCmdLinePanel");
                    serverCmdLinePanel.setLayout(new MigLayout("fill", "0[min!]0[]0[min!]0", "0[]0[]0[]0[]0"));
                    configurationTab.add(serverCmdLinePanel, "grow, wrap");
                    //configTabber.addTab("Server Setup", serverCmdLinePanel);
                    {
                        javaExecLabel.setText(lang.getString("javaExecLabel.text"));
                        javaExecLabel.setName("javaExecLabel");
                        serverCmdLinePanel.add(javaExecLabel, "grow 0");

                        javaExecField.setText(lang.getString("javaExecField.text"));
                        javaExecField.putClientProperty("JComponent.sizeVariant", "small");
                        SwingUtilities.updateComponentTreeUI(javaExecField);
                        javaExecField.setToolTipText(lang.getString("javaExecField.toolTipText"));
                        javaExecField.setName("javaExecField");
                        javaExecField.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                javaExecFieldActionPerformed(evt);
                            }
                        });
                        serverCmdLinePanel.add(javaExecField, "growx, growy 0");

                        javaExecBrowseButton.setText(lang.getString("javaExecBrowseButton.text"));
                        javaExecBrowseButton.putClientProperty("JComponent.sizeVariant", "mini");
                        SwingUtilities.updateComponentTreeUI(javaExecBrowseButton);
                        javaExecBrowseButton.setToolTipText(lang.getString("javaExecBrowseButton.toolTipText"));
                        javaExecBrowseButton.setMargin(new Insets(2, 5, 2, 5));
                        javaExecBrowseButton.setName("javaExecBrowseButton");
                        javaExecBrowseButton.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                javaExecBrowseButtonActionPerformed(evt);
                            }
                        });
                        serverCmdLinePanel.add(javaExecBrowseButton, "wrap, growx 0");

                        serverJarLabel.setText(lang.getString("serverJarLabel.text"));
                        serverJarLabel.setName("serverJarLabel");
                        serverCmdLinePanel.add(serverJarLabel, "growx 0");

                        serverJarField.setText(lang.getString("serverJarField.text"));
                        serverJarField.setName("serverJarField");
                        serverJarField.putClientProperty("JComponent.sizeVariant", "small");
                        SwingUtilities.updateComponentTreeUI(serverJarField);
                        serverJarField.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                serverJarFieldActionPerformed(evt);
                            }
                        });
                        serverCmdLinePanel.add(serverJarField, "growx, split 2");

                        bukkitCheckBox.setText(lang.getString("bukkitCheckBox.text"));
                        bukkitCheckBox.setName("bukkitCheckBox");
                        bukkitCheckBox.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                bukkitCheckBoxActionPerformed(evt);
                            }
                        });
                        serverCmdLinePanel.add(bukkitCheckBox, "growx 0");

                        serverJarBrowseButton.setText(lang.getString("serverJarBrowseButton.text"));
                        serverJarBrowseButton.setToolTipText(lang.getString("serverJarBrowseButton.toolTipText"));
                        serverJarBrowseButton.putClientProperty("JComponent.sizeVariant", "mini");
                        SwingUtilities.updateComponentTreeUI(serverJarBrowseButton);
                        serverJarBrowseButton.setMargin(new Insets(2, 5, 2, 5));
                        serverJarBrowseButton.setName("serverJarBrowseButton");
                        serverJarBrowseButton.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                serverJarBrowseButtonActionPerformed(evt);
                            }
                        });
                        serverCmdLinePanel.add(serverJarBrowseButton, "growx 0, wrap");

                        xmxMemoryLabel.setText(lang.getString("xmxMemoryLabel.text"));
                        xmxMemoryLabel.setName("xmxMemoryLabel");
                        serverCmdLinePanel.add(xmxMemoryLabel, "span 3, split 5");

                        xmxMemoryField.setText(lang.getString("xmxMemoryField.text"));
                        xmxMemoryField.setToolTipText(lang.getString("xmxMemoryField.toolTipText"));
                        xmxMemoryField.setName("xmxMemoryField");
                        xmxMemoryField.putClientProperty("JComponent.sizeVariant", "small");
                        SwingUtilities.updateComponentTreeUI(xmxMemoryField);
                        xmxMemoryField.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                xmxMemoryFieldActionPerformed(evt);
                            }
                        });
                        serverCmdLinePanel.add(xmxMemoryField, "growx 10");

                        xincgcCheckBox.setText(lang.getString("xincgcCheckBox.text"));
                        xincgcCheckBox.setToolTipText(lang.getString("xincgcCheckBox.toolTipText"));
                        xincgcCheckBox.setName("xincgcCheckBox");
                        xincgcCheckBox.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                xincgcCheckBoxActionPerformed(evt);
                            }
                        });
                        serverCmdLinePanel.add(xincgcCheckBox, "growx 0");

                        extraArgsLabel.setText(lang.getString("extraArgsLabel.text"));
                        extraArgsLabel.setName("extraArgsLabel");
                        serverCmdLinePanel.add(extraArgsLabel, "gapleft 20, growx 0");

                        extraArgsField.setText(lang.getString("extraArgsField.text"));
                        extraArgsField.setToolTipText(lang.getString("extraArgsField.toolTipText"));
                        extraArgsField.setName("extraArgsField");
                        extraArgsField.putClientProperty("JComponent.sizeVariant", "small");
                        SwingUtilities.updateComponentTreeUI(extraArgsField);
                        extraArgsField.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                extraArgsFieldActionPerformed(evt);
                            }
                        });
                        serverCmdLinePanel.add(extraArgsField, "growx, wrap");

                        customCommandLineLabel.setText(lang.getString("customCommandLineLabel.text"));
                        customCommandLineLabel.setName("customCommandLineLabel");
                        serverCmdLinePanel.add(customCommandLineLabel, "growx 0, span 3, split 3");

                        cmdLineField.setEditable(false);
                        cmdLineField.setText(lang.getString("cmdLineField.text"));
                        cmdLineField.setToolTipText(lang.getString("cmdLineField.toolTipText"));
                        cmdLineField.setName("cmdLineField");
                        cmdLineField.putClientProperty("JComponent.sizeVariant", "small");
                        SwingUtilities.updateComponentTreeUI(cmdLineField);
                        cmdLineField.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                cmdLineFieldActionPerformed(evt);
                            }
                        });
                        serverCmdLinePanel.add(cmdLineField, "growx");

                        customLaunchCheckBox.setText(lang.getString("customLaunchCheckBox.text"));
                        customLaunchCheckBox.setToolTipText(lang.getString("customLaunchCheckBox.toolTipText"));
                        customLaunchCheckBox.setName("customLaunchCheckBox");
                        customLaunchCheckBox.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                customLaunchCheckBoxActionPerformed(evt);
                            }
                        });
                        serverCmdLinePanel.add(customLaunchCheckBox, "growx 0");
                    }

                    serverPropertiesPanel.setBorder(BorderFactory.createTitledBorder(lang.getString("serverPropertiesPanel.border.title"))); // NOI18N
                    serverPropertiesPanel.setName("serverPropertiesPanel");
                    serverPropertiesPanel.setLayout(new MigLayout("fill", "0[]0", "0[]0[]0"));
                    configurationTab.add(serverPropertiesPanel, "grow, wrap");
                    //configTabber.addTab("Server Properties", serverPropertiesPanel);
                    {
                        allowFlightCheckBox.setText(lang.getString("allowFlightCheckBox.text"));
                        allowFlightCheckBox.setName("allowFlightCheckBox");
                        serverPropertiesPanel.add(allowFlightCheckBox, "growx, split 5");

                        allowNetherCheckBox.setSelected(true);
                        allowNetherCheckBox.setText(lang.getString("allowNetherCheckBox.text"));
                        allowNetherCheckBox.setName("allowNetherCheckBox");
                        serverPropertiesPanel.add(allowNetherCheckBox, "growx");

                        onlineModeCheckBox.setSelected(true);
                        onlineModeCheckBox.setText(lang.getString("onlineModeCheckBox.text"));
                        onlineModeCheckBox.setName("onlineModeCheckBox");
                        serverPropertiesPanel.add(onlineModeCheckBox, "growx");

                        pvpCheckBox.setSelected(true);
                        pvpCheckBox.setText(lang.getString("pvpCheckBox.text"));
                        pvpCheckBox.setName("pvpCheckBox");
                        serverPropertiesPanel.add(pvpCheckBox, "growx");

                        whiteListCheckBox.setText(lang.getString("whiteListCheckBox.text"));
                        whiteListCheckBox.setName("whiteListCheckBox");
                        serverPropertiesPanel.add(whiteListCheckBox, "growx, wrap");

                        spawnProtectionLabel.setText(lang.getString("spawnProtectionLabel.text"));
                        spawnProtectionLabel.setName("spawnProtectionLabel");
                        serverPropertiesPanel.add(spawnProtectionLabel, "growx 0, split 6");

                        spawnProtectionField.setText(lang.getString("spawnProtectionField.text"));
                        spawnProtectionField.setName("spawnProtectionField");
                        spawnProtectionField.putClientProperty("JComponent.sizeVariant", "small");
                        SwingUtilities.updateComponentTreeUI(spawnProtectionField);
                        serverPropertiesPanel.add(spawnProtectionField, "growx");

                        viewDistanceLabel.setText(lang.getString("viewDistanceLabel.text"));
                        viewDistanceLabel.setName("viewDistanceLabel");
                        serverPropertiesPanel.add(viewDistanceLabel, "growx 0");

                        viewDistanceSpinner.setModel(new SpinnerNumberModel(10, 3, 15, 1));
                        viewDistanceSpinner.setName("viewDistanceSpinner");
                        viewDistanceSpinner.putClientProperty("JComponent.sizeVariant", "small");
                        SwingUtilities.updateComponentTreeUI(viewDistanceSpinner);
                        serverPropertiesPanel.add(viewDistanceSpinner, "growx");

                        maxPlayersLabel.setText(lang.getString("maxPlayersLabel.text"));
                        maxPlayersLabel.setName("maxPlayersLabel");
                        serverPropertiesPanel.add(maxPlayersLabel, "growx 0");

                        maxPlayersSpinner.setModel(new SpinnerNumberModel(20, 0, 100, 1));
                        maxPlayersSpinner.setName("maxPlayersSpinner");
                        maxPlayersSpinner.putClientProperty("JComponent.sizeVariant", "small");
                        SwingUtilities.updateComponentTreeUI(maxPlayersSpinner);
                        serverPropertiesPanel.add(maxPlayersSpinner, "growx 10, wrap");

                        serverIpLabel.setText(lang.getString("serverIpLabel.text"));
                        serverIpLabel.setName("serverIpLabel");
                        serverPropertiesPanel.add(serverIpLabel, "growx 0, split 6");

                        serverIpField.setText(lang.getString("serverIpField.text"));
                        serverIpField.setName("serverIpField");
                        serverIpField.putClientProperty("JComponent.sizeVariant", "small");
                        SwingUtilities.updateComponentTreeUI(serverIpField);
                        serverPropertiesPanel.add(serverIpField, "growx 100");

                        serverPortLabel.setLabelFor(serverPortField);
                        serverPortLabel.setText(lang.getString("serverPortLabel.text"));
                        serverPortLabel.setName("serverPortLabel");
                        serverPropertiesPanel.add(serverPortLabel, "growx 0");

                        serverPortField.setText(lang.getString("serverPortField.text"));
                        serverPortField.setToolTipText(lang.getString("serverPortField.toolTipText"));
                        serverPortField.putClientProperty("JComponent.sizeVariant", "small");
                        SwingUtilities.updateComponentTreeUI(serverPortField);
                        serverPortField.setInputVerifier(new RegexVerifier("^(6553[0-5]|655[0-2]\\d|65[0-4]\\d\\d|6[0-4]\\d{3}|[1-5]\\d{4}|[1-9]\\d{0,3}|0)$"));
                        serverPortField.setName("serverPortField");
                        serverPropertiesPanel.add(serverPortField, "growx 20");

                        spawnAnimalsCheckBox.setSelected(true);
                        spawnAnimalsCheckBox.setText(lang.getString("spawnAnimalsCheckBox.text"));
                        spawnAnimalsCheckBox.setName("spawnAnimalsCheckBox");
                        serverPropertiesPanel.add(spawnAnimalsCheckBox);

                        spawnMonstersCheckBox.setSelected(true);
                        spawnMonstersCheckBox.setText(lang.getString("spawnMonstersCheckBox.text"));
                        spawnMonstersCheckBox.setName("spawnMonstersCheckBox");
                        serverPropertiesPanel.add(spawnMonstersCheckBox, "wrap");

                        levelNameLabel.setText(lang.getString("levelNameLabel.text"));
                        levelNameLabel.setName("levelNameLabel");
                        serverPropertiesPanel.add(levelNameLabel, "growx 0, split 4");

                        levelNameField.setText(lang.getString("levelNameField.text"));
                        levelNameField.setName("levelNameField");
                        levelNameField.putClientProperty("JComponent.sizeVariant", "small");
                        SwingUtilities.updateComponentTreeUI(levelNameField);
                        serverPropertiesPanel.add(levelNameField, "growx 40");

                        levelSeedLabel.setText(lang.getString("levelSeedLabel.text"));
                        levelSeedLabel.setName("levelSeedLabel");
                        serverPropertiesPanel.add(levelSeedLabel, "growx 0");

                        levelSeedField.setText(lang.getString("levelSeedField.text"));
                        levelSeedField.setName("levelSeedField");
                        levelSeedField.putClientProperty("JComponent.sizeVariant", "small");
                        SwingUtilities.updateComponentTreeUI(levelSeedField);
                        serverPropertiesPanel.add(levelSeedField, "growx 100");
                    }

                    guiConfigPanel.setBorder(BorderFactory.createTitledBorder(lang.getString("guiConfigPanel.border.title")));
                    guiConfigPanel.setName("guiConfigPanel");
                    guiConfigPanel.setLayout(new MigLayout("fill", "0[]0", "0[]0"));
                    configurationTab.add(guiConfigPanel, "grow, wrap");
                    //configTabber.addTab("GUI Settings", guiConfigPanel);
                    {
                        useProxyCheckBox.setText(lang.getString("useProxyCheckBox.text"));
                        useProxyCheckBox.setToolTipText(lang.getString("useProxyCheckBox.toolTipText"));
                        useProxyCheckBox.setName("useProxyCheckBox");
                        useProxyCheckBox.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                useProxyCheckBoxActionPerformed(evt);
                            }
                        });
                        guiConfigPanel.add(useProxyCheckBox, "growx 0, split 5");

                        extPortLabel.setLabelFor(extPortField);
                        extPortLabel.setText(lang.getString("extPortLabel.text"));
                        extPortLabel.setName("extPortLabel");
                        guiConfigPanel.add(extPortLabel, "gapleft 20, growx 0");

                        extPortField.setText(lang.getString("extPortField.text"));
                        extPortField.setToolTipText(lang.getString("extPortField.toolTipText"));
                        extPortField.putClientProperty("JComponent.sizeVariant", "small");
                        SwingUtilities.updateComponentTreeUI(extPortField);
                        extPortField.setInputVerifier(new RegexVerifier("^(6553[0-5]|655[0-2]\\d|65[0-4]\\d\\d|6[0-4]\\d{3}|[1-5]\\d{4}|[1-9]\\d{0,3}|0)$"));
                        extPortField.setName("extPortField");
                        guiConfigPanel.add(extPortField, "growx 100");

                        intPortLabel.setLabelFor(intPortField);
                        intPortLabel.setText(lang.getString("intPortLabel.text"));
                        intPortLabel.setName("intPortLabel");
                        guiConfigPanel.add(intPortLabel, "growx 0");

                        intPortField.setText(lang.getString("intPortField.text"));
                        intPortField.setToolTipText(lang.getString("intPortField.toolTipText"));
                        intPortField.putClientProperty("JComponent.sizeVariant", "small");
                        SwingUtilities.updateComponentTreeUI(intPortField);
                        intPortField.setInputVerifier(new RegexVerifier("^(6553[0-5]|655[0-2]\\d|65[0-4]\\d\\d|6[0-4]\\d{3}|[1-5]\\d{4}|[1-9]\\d{0,3}|0)$"));
                        intPortField.setName("intPortField");
                        guiConfigPanel.add(intPortField, "growx 100, wrap");

                        windowTitleLabel.setText(lang.getString("windowTitleLabel.text"));
                        windowTitleLabel.setName("windowTitleLabel");
                        guiConfigPanel.add(windowTitleLabel, "growx 0, split 4");

                        windowTitleField.setText(lang.getString("windowTitleField.text"));
                        windowTitleField.setName("windowTitleField");
                        windowTitleField.putClientProperty("JComponent.sizeVariant", "small");
                        SwingUtilities.updateComponentTreeUI(windowTitleField);
                        windowTitleField.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                windowTitleFieldActionPerformed(evt);
                            }
                        });
                        guiConfigPanel.add(windowTitleField, "growx");

                        inputHistoryMaxSizeLabel.setText(lang.getString("inputHistoryMaxSizeLabel.text"));
                        inputHistoryMaxSizeLabel.setName("inputHistoryMaxSizeLabel");
                        guiConfigPanel.add(inputHistoryMaxSizeLabel, "growx 0");

                        inputHistoryMaxSizeField.setText(lang.getString("inputHistoryMaxSizeField.text"));
                        inputHistoryMaxSizeField.setToolTipText(lang.getString("inputHistoryMaxSizeField.toolTipText"));
                        inputHistoryMaxSizeField.setInputVerifier(new RegexVerifier("\\d{1,4}"));
                        inputHistoryMaxSizeField.putClientProperty("JComponent.sizeVariant", "small");
                        SwingUtilities.updateComponentTreeUI(inputHistoryMaxSizeField);
                        inputHistoryMaxSizeField.setName("inputHistoryMaxSizeField");
                        guiConfigPanel.add(inputHistoryMaxSizeField, "growx, wrap");

                        startServerOnLaunchCheckBox.setText(lang.getString("startServerOnLaunchCheckBox.text"));
                        startServerOnLaunchCheckBox.setName("startServerOnLaunchCheckBox");
                        startServerOnLaunchCheckBox.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                startServerOnLaunchCheckBoxActionPerformed(evt);
                            }
                        });
                        guiConfigPanel.add(startServerOnLaunchCheckBox, "growx 0, split 3");

                        commandPrefixLabel.setText(lang.getString("commandPrefixLabel.text"));
                        commandPrefixLabel.setName("commandPrefixLabel");
                        guiConfigPanel.add(commandPrefixLabel, "gapleft 20, growx 0");

                        commandPrefixField.setText(lang.getString("commandPrefixField.text"));
                        commandPrefixField.setToolTipText(lang.getString("commandPrefixField.toolTipText"));
                        commandPrefixField.setName("commandPrefixField");
                        commandPrefixField.putClientProperty("JComponent.sizeVariant", "small");
                        SwingUtilities.updateComponentTreeUI(commandPrefixField);
                        commandPrefixField.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                commandPrefixFieldActionPerformed(evt);
                            }
                        });
                        guiConfigPanel.add(commandPrefixField, "growx");
                }
                }

                saveServerConfigButton.setText(lang.getString("saveServerConfigButton.text"));
                saveServerConfigButton.setMargin(new Insets(2, 5, 2, 5));
                saveServerConfigButton.setName("saveServerConfigButton");
                saveServerConfigButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        saveServerConfigButtonActionPerformed(evt);
                    }
                });
                configurationTab.add(saveServerConfigButton, "growx");
            }

            backupTab.setName("backupTab");
            backupTab.setLayout(new MigLayout("fill", "0[70%]0[30%]0", "0[min!]0[]0[min!]0"));
            tabber.addTab(lang.getString("backupTab.TabConstraints.tabTitle"), backupTab);
            {
                backupSettingsPanel.setBorder(BorderFactory.createTitledBorder(lang.getString("backupSettingsPanel.border.title")));
                backupSettingsPanel.setName("backupSettingsPanel");
                backupSettingsPanel.setLayout(new MigLayout("fill", "0[]0", "0[]0"));
                backupTab.add(backupSettingsPanel, "grow");
                {
                    backupPathLabel.setText(lang.getString("backupPathLabel.text"));
                    backupPathLabel.setName("backupPathLabel");
                    backupSettingsPanel.add(backupPathLabel, "growx 0, split 3");

                    backupPathField.setText(lang.getString("backupPathField.text"));
                    backupPathField.setName("backupPathField");
                    backupPathField.putClientProperty("JComponent.sizeVariant", "small");
                    SwingUtilities.updateComponentTreeUI(backupPathField);
                    backupPathField.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            backupPathFieldActionPerformed(evt);
                        }
                    });
                    backupSettingsPanel.add(backupPathField, "growx");

                    backupPathBrowseButton.setText(lang.getString("backupPathBrowseButton.text"));
                    backupPathBrowseButton.setMargin(new Insets(2, 5, 2, 5));
                    backupPathBrowseButton.setName("backupPathBrowseButton");
                    backupPathBrowseButton.putClientProperty("JComponent.sizeVariant", "mini");
                    SwingUtilities.updateComponentTreeUI(backupPathBrowseButton);
                    backupPathBrowseButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            backupPathBrowseButtonActionPerformed(evt);
                        }
                    });
                    backupSettingsPanel.add(backupPathBrowseButton, "growx 0, wrap");

                    zipBackupCheckBox.setText(lang.getString("zipBackupCheckBox.text")); // NOI18N
                    zipBackupCheckBox.setName("zipBackupCheckBox"); // NOI18N
                    zipBackupCheckBox.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            zipBackupCheckBoxActionPerformed(evt);
                        }
                    });
                    backupSettingsPanel.add(zipBackupCheckBox, "growx 0, split 2");

                    clearLogCheckBox.setText(lang.getString("clearLogCheckBox.text")); // NOI18N
                    clearLogCheckBox.setToolTipText(lang.getString("clearLogCheckBox.toolTipText")); // NOI18N
                    clearLogCheckBox.setName("clearLogCheckBox"); // NOI18N
                    clearLogCheckBox.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            clearLogCheckBoxActionPerformed(evt);
                        }
                    });
                    backupSettingsPanel.add(clearLogCheckBox, "gapleft 20, growx 0");
                }

                backupStatusPanel.setBorder(BorderFactory.createTitledBorder(lang.getString("backupStatusPanel.border.title")));
                backupStatusPanel.setName("backupStatusPanel");
                backupStatusPanel.setLayout(new MigLayout("fill", "0[]0", "0[]0"));
                backupTab.add(backupStatusPanel, "grow, span 1 3, wrap");
                {
                    backupStatusLog.setEditable(false);
                    backupStatusLog.setName("backupStatusLog");
                    backupStatusLogScrollPane.setName("backupStatusLogScrollPane");
                    backupStatusLogScrollPane.setViewportView(backupStatusLog);
                    backupStatusPanel.add(backupStatusLogScrollPane, "grow");
                }

                backupFileChooserPanel.setBorder(BorderFactory.createTitledBorder(lang.getString("backupFileChooserPanel.border.title"))); // NOI18N
                backupFileChooserPanel.setName("backupFileChooserPanel");
                backupFileChooserPanel.setLayout(new MigLayout("fill", "0[]0", "0[]0[min!]0[min!]0"));
                backupTab.add(backupFileChooserPanel, "grow, wrap");
                {
                    backupFileList.setName("backupFileList");
                    backupFileList.setModel(backupFileListModel);
                    backupFileListScrollPane.setName("backupFileListScrollPane");
                    backupFileListScrollPane.setViewportView(backupFileList);
                    backupFileChooserPanel.add(backupFileListScrollPane, "grow, wrap");

                    backupAddButton.setText(lang.getString("backupAddButton.text"));
                    backupAddButton.setMargin(new Insets(2, 5, 2, 5));
                    backupAddButton.setName("backupAddButton");
                    backupAddButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            backupAddButtonActionPerformed(evt);
                        }
                    });
                    backupFileChooserPanel.add(backupAddButton, "growx, split 2");

                    backupRemoveButton.setText(lang.getString("backupRemoveButton.text"));
                    backupRemoveButton.setMargin(new Insets(2, 5, 2, 5));
                    backupRemoveButton.setName("backupRemoveButton");
                    backupRemoveButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            backupRemoveButtonActionPerformed(evt);
                        }
                    });
                    backupFileChooserPanel.add(backupRemoveButton, "gapleft 40, growx, wrap");

                    backupButton.setText(lang.getString("backupButton.text"));
                    backupButton.setMargin(new Insets(2, 5, 2, 5));
                    backupButton.setName("backupButton");
                    backupButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            backupButtonActionPerformed(evt);
                        }
                    });
                    backupFileChooserPanel.add(backupButton, "growx");
                }

                saveBackupControlButton.setText(lang.getString("saveBackupControlButton.text"));
                saveBackupControlButton.setMargin(new Insets(2, 5, 2, 5));
                saveBackupControlButton.setName("saveBackupControlButton");
                saveBackupControlButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        saveBackupControlButtonActionPerformed(evt);
                    }
                });
                backupTab.add(saveBackupControlButton, "growx");

            }

            schedulerTab.setName("schedulerTab");
            schedulerTab.setLayout(new MigLayout("fill", "5[]10[]30[]50[]5", "0[]0[min!]0[min!]0[min!]0[min!]0"));
            tabber.addTab(lang.getString("schedulerTab.TabConstraints.tabTitle"), schedulerTab);
            {
                taskSchedulerPanel.setBorder(BorderFactory.createTitledBorder(lang.getString("taskSchedulerPanel.border.title"))); // NOI18N
                taskSchedulerPanel.setName("taskSchedulerPanel");
                taskSchedulerPanel.setLayout(new MigLayout("fill", "0[]0", "0[]0"));
                schedulerTab.add(taskSchedulerPanel, "grow, wrap, span 4");
                {
                    taskSchedulerScrollPane.setName("taskSchedulerScrollPane");
                    taskSchedulerScrollPane.setViewportView(taskSchedulerList);
                    taskSchedulerPanel.add(taskSchedulerScrollPane, "grow");
                    {
                        taskSchedulerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                        taskSchedulerList.setName("taskSchedulerList");
                        taskSchedulerList.addKeyListener(new KeyAdapter() {
                            public void keyTyped(KeyEvent evt) {
                                taskSchedulerListKeyTyped(evt);
                            }
                        });
                    }
                }

                taskListAddButton.setText(lang.getString("taskListAddButton.text")); // NOI18N
                taskListAddButton.setMargin(new Insets(2, 5, 2, 5));
                taskListAddButton.setName("taskListAddButton"); // NOI18N
                taskListAddButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        taskListAddButtonActionPerformed(evt);
                    }
                });
                schedulerTab.add(taskListAddButton, "growx");

                taskListEditButton.setText(lang.getString("taskListEditButton.text")); // NOI18N
                taskListEditButton.setMargin(new Insets(2, 5, 2, 5));
                taskListEditButton.setName("taskListEditButton"); // NOI18N
                taskListEditButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        taskListEditButtonActionPerformed(evt);
                    }
                });
                schedulerTab.add(taskListEditButton, "growx");

                taskListRemoveButton.setText(lang.getString("taskListRemoveButton.text"));
                taskListRemoveButton.setMargin(new Insets(2, 5, 2, 5));
                taskListRemoveButton.setName("taskListRemoveButton");
                taskListRemoveButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        taskListRemoveButtonActionPerformed(evt);
                    }
                });
                schedulerTab.add(taskListRemoveButton, "growx");

                pauseSchedulerButton.setText(lang.getString("pauseSchedulerButton.text"));
                pauseSchedulerButton.setToolTipText(lang.getString("pauseSchedulerButton.toolTipText"));
                pauseSchedulerButton.setMargin(new Insets(2, 5, 2, 5));
                pauseSchedulerButton.setName("pauseSchedulerButton");
                pauseSchedulerButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        pauseSchedulerButtonActionPerformed(evt);
                    }
                });
                schedulerTab.add(pauseSchedulerButton, "growx");
            }

            webInterfaceTab.setName("webInterfaceTab");
            webInterfaceTab.setLayout(new MigLayout("fill", "0[]0", "0[min!]0[]0"));
            tabber.addTab(lang.getString("webInterfaceTab.TabConstraints.tabTitle"), webInterfaceTab);
            {
                webInterfaceConfigPanel.setBorder(BorderFactory.createTitledBorder(lang.getString("webInterfaceConfigPanel.border.title")));
                webInterfaceConfigPanel.setName("webInterfaceConfigPanel");
                webInterfaceConfigPanel.setLayout(new MigLayout("fill", "0[]0", "0[]0[]0"));
                webInterfaceTab.add(webInterfaceConfigPanel, "grow, wrap");
                {
                    useWebInterfaceCheckBox.setText(lang.getString("useWebInterfaceCheckBox.text"));
                    useWebInterfaceCheckBox.setToolTipText(lang.getString("useWebInterfaceCheckBox.toolTipText"));
                    useWebInterfaceCheckBox.setName("useWebInterfaceCheckBox");
                    useWebInterfaceCheckBox.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            useWebInterfaceCheckBoxActionPerformed(evt);
                        }
                    });
                    webInterfaceConfigPanel.add(useWebInterfaceCheckBox, "growx 0, split");

                    webPortLabel.setText(lang.getString("webPortLabel.text"));
                    webPortLabel.setName("webPortLabel");
                    webInterfaceConfigPanel.add(webPortLabel, "gapleft 20, growx 0");

                    webPortField.setText(lang.getString("webPortField.text"));
                    webPortField.setToolTipText(lang.getString("webPortField.toolTipText"));
                    webPortField.putClientProperty("JComponent.sizeVariant", "small");
                    SwingUtilities.updateComponentTreeUI(webPortField);
                    webPortField.setInputVerifier(new RegexVerifier("^(6553[0-5]|655[0-2]\\d|65[0-4]\\d\\d|6[0-4]\\d{3}|[1-5]\\d{4}|[1-9]\\d{0,3}|0)$"));
                    webPortField.setName("webPortField");
                    webPortField.addFocusListener(new FocusAdapter() {
                        public void focusLost(FocusEvent evt) {
                            webPortFieldFocusLost(evt);
                        }
                    });
                    webInterfaceConfigPanel.add(webPortField, "growx 20");

                    webPasswordLabel.setText(lang.getString("webPasswordLabel.text"));
                    webPasswordLabel.setName("webPasswordLabel");
                    webInterfaceConfigPanel.add(webPasswordLabel, "growx 0");

                    webPasswordField.setText(lang.getString("webPasswordField.text"));
                    webPasswordField.setName("webPasswordField");
                    webPasswordField.putClientProperty("JComponent.sizeVariant", "small");
                    SwingUtilities.updateComponentTreeUI(webPasswordField);
                    webPasswordField.addFocusListener(new FocusAdapter() {
                        public void focusLost(FocusEvent evt) {
                            webPasswordFieldFocusLost(evt);
                        }
                    });
                    webInterfaceConfigPanel.add(webPasswordField, "growx");

                    showWebPasswordButton.setText(lang.getString("showWebPasswordButton.text"));
                    showWebPasswordButton.setMargin(new Insets(2, 5, 2, 5));
                    showWebPasswordButton.setName("showWebPasswordButton");
                    showWebPasswordButton.putClientProperty("JComponent.sizeVariant", "mini");
                    SwingUtilities.updateComponentTreeUI(showWebPasswordButton);
                    showWebPasswordButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            showWebPasswordButtonActionPerformed(evt);
                        }
                    });
                    webInterfaceConfigPanel.add(showWebPasswordButton, "growx 0, wrap");

                    disableGetOutputNotificationsCheckBox.setText(lang.getString("disableGetOutputNotificationsCheckBox.text"));
                    disableGetOutputNotificationsCheckBox.setToolTipText(lang.getString("disableGetOutputNotificationsCheckBox.toolTipText"));
                    disableGetOutputNotificationsCheckBox.setName("disableGetOutputNotificationsCheckBox");
                    disableGetOutputNotificationsCheckBox.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            disableGetOutputNotificationsCheckBoxActionPerformed(evt);
                        }
                    });
                    webInterfaceConfigPanel.add(disableGetOutputNotificationsCheckBox);
                }

                webLogPanel.setBorder(BorderFactory.createTitledBorder(lang.getString("webLogPanel.border.title")));
                webLogPanel.setName("webLogPanel");
                webLogPanel.setLayout(new MigLayout("fill", "0[]0", "0[]0"));
                webInterfaceTab.add(webLogPanel, "grow");
                {
                    webLogScrollPane.setName("webLogScrollPane");
                    webLogScrollPane.setViewportView(webLog);
                    webLogPanel.add(webLogScrollPane, "grow");
                    webLog.setName("webLog");
                }
            }

            themeTab.setName("themeTab");
            themeTab.setLayout(new MigLayout("fill", "0[]0", "0[]0[min!]0"));
            tabber.addTab(lang.getString("themeTab.TabConstraints.tabTitle"), themeTab);
            {
                colorizationPanel.setBorder(BorderFactory.createTitledBorder(lang.getString("colorizationPanel.border.title")));
                colorizationPanel.setName("colorizationPanel");
                colorizationPanel.setLayout(new MigLayout("fill", "0[]0", "0[]0"));
                themeTab.add(colorizationPanel, "grow, wrap");
                {
                    textColorLabel.setText(lang.getString("textColorLabel.text"));
                    textColorLabel.setName("textColorLabel");
                    colorizationPanel.add(textColorLabel, "growx 0");

                    textColorBox.setEditable(false);
                    textColorBox.setText(lang.getString("textColorBox.text"));
                    textColorBox.setToolTipText(lang.getString("textColorBox.toolTipText"));
                    textColorBox.setName("textColorBox");
                    textColorBox.addMouseListener(new MouseAdapter() {
                        public void mouseClicked(MouseEvent evt) {
                            textColorBoxMouseClicked(evt);
                        }
                    });
                    textColorBox.addFocusListener(new FocusAdapter() {
                        public void focusLost(FocusEvent evt) {
                            textColorBoxFocusLost(evt);
                        }
                    });
                    colorizationPanel.add(textColorBox, "grow, wrap");

                    bgColorLabel.setText(lang.getString("bgColorLabel.text"));
                    bgColorLabel.setName("bgColorLabel");
                    colorizationPanel.add(bgColorLabel, "growx 0");

                    bgColorBox.setEditable(false);
                    bgColorBox.setToolTipText(lang.getString("bgColorBox.toolTipText")); // NOI18N
                    bgColorBox.setName("bgColorBox"); // NOI18N
                    bgColorBox.addMouseListener(new MouseAdapter() {
                        public void mouseClicked(MouseEvent evt) {
                            bgColorBoxMouseClicked(evt);
                        }
                    });
                    bgColorBox.addFocusListener(new FocusAdapter() {
                        public void focusLost(FocusEvent evt) {
                            bgColorBoxFocusLost(evt);
                        }
                    });
                    colorizationPanel.add(bgColorBox, "grow, wrap");

                    infoColorLabel.setText(lang.getString("infoColorLabel.text"));
                    infoColorLabel.setName("infoColorLabel");
                    colorizationPanel.add(infoColorLabel, "growx 0");

                    infoColorBox.setEditable(false);
                    infoColorBox.setToolTipText(lang.getString("infoColorBox.toolTipText")); // NOI18N
                    infoColorBox.setName("infoColorBox"); // NOI18N
                    infoColorBox.addMouseListener(new MouseAdapter() {
                        public void mouseClicked(MouseEvent evt) {
                            infoColorBoxMouseClicked(evt);
                        }
                    });
                    infoColorBox.addFocusListener(new FocusAdapter() {
                        public void focusLost(FocusEvent evt) {
                            infoColorBoxFocusLost(evt);
                        }
                    });
                    colorizationPanel.add(infoColorBox, "grow, wrap");

                    warningColorLabel.setText(lang.getString("warningColorLabel.text"));
                    warningColorLabel.setName("warningColorLabel");
                    colorizationPanel.add(warningColorLabel, "growx 0");

                    warningColorBox.setEditable(false);
                    warningColorBox.setToolTipText(lang.getString("warningColorBox.toolTipText")); // NOI18N
                    warningColorBox.setName("warningColorBox"); // NOI18N
                    warningColorBox.addMouseListener(new MouseAdapter() {
                        public void mouseClicked(MouseEvent evt) {
                            warningColorBoxMouseClicked(evt);
                        }
                    });
                    warningColorBox.addFocusListener(new FocusAdapter() {
                        public void focusLost(FocusEvent evt) {
                            warningColorBoxFocusLost(evt);
                        }
                    });
                    colorizationPanel.add(warningColorBox, "grow, wrap");

                    severeColorLabel.setText(lang.getString("severeColorLabel.text"));
                    severeColorLabel.setName("severeColorLabel");
                    colorizationPanel.add(severeColorLabel, "growx 0");
    
                    severeColorBox.setEditable(false);
                    severeColorBox.setToolTipText(lang.getString("severeColorBox.toolTipText")); // NOI18N
                    severeColorBox.setName("severeColorBox"); // NOI18N
                    severeColorBox.addMouseListener(new MouseAdapter() {
                        public void mouseClicked(MouseEvent evt) {
                            severeColorBoxMouseClicked(evt);
                        }
                    });
                    severeColorBox.addFocusListener(new FocusAdapter() {
                        public void focusLost(FocusEvent evt) {
                            severeColorBoxFocusLost(evt);
                        }
                    });
                    colorizationPanel.add(severeColorBox, "grow, wrap");

                    textSizeLabel.setText(lang.getString("textSizeLabel.text")); // NOI18N
                    textSizeLabel.setName("textSizeLabel"); // NOI18N
                    colorizationPanel.add(textSizeLabel, "growx 0");

                    textSizeField.setModel(new SpinnerNumberModel(3, 1, 10, 1));
                    textSizeField.setName("textSizeField"); // NOI18N
                    textSizeField.addChangeListener(new ChangeListener() {
                        public void stateChanged(ChangeEvent evt) {
                            textSizeFieldStateChanged(evt);
                        }
                    });
                    textSizeField.addPropertyChangeListener(new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent evt) {
                            textSizeFieldPropertyChange(evt);
                        }
                    });
                    colorizationPanel.add(textSizeField, "grow");
                }

                saveThemeButton.setText(lang.getString("saveThemeButton.text")); // NOI18N
                saveThemeButton.setMargin(new Insets(2, 5, 2, 5));
                saveThemeButton.setName("saveThemeButton"); // NOI18N
                saveThemeButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        saveGuiConfigButtonActionPerformed(evt);
                    }
                });
                themeTab.add(saveThemeButton, "growx");
            }
        }















        menuBar.setName("menuBar");

        fileMenu.setText(lang.getString("fileMenu.text"));
        fileMenu.setName("fileMenu");

        exitMenuItem.setText(lang.getString("exitMenuItem.text"));
        exitMenuItem.setName("exitMenuItem");
        exitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                performExitSequence();
            }
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(lang.getString("helpMenu.text"));
        helpMenu.setName("helpMenu");

        aboutMenuItem.setName("aboutMenuItem");
        aboutMenuItem.setText(lang.getString("aboutMenuItem.text"));
        aboutMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showAboutBox();
            }
        });
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        hideMenu.setText(lang.getString("hideMenu.text"));
        hideMenu.setName("hideMenu");
        hideMenu.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                hideMenuMouseClicked(evt);
            }
        });
        menuBar.add(hideMenu);

        versionNotifier.setText(lang.getString("versionNotifier.text")); // NOI18N
        versionNotifier.setToolTipText(lang.getString("versionNotifier.toolTipText")); // NOI18N
        versionNotifier.setName("versionNotifier"); // NOI18N

        launchSupportPage.setText(lang.getString("launchSupportPage.text")); // NOI18N
        launchSupportPage.setName("launchSupportPage"); // NOI18N
        launchSupportPage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                launchSupportPageActionPerformed(evt);
            }
        });
        versionNotifier.add(launchSupportPage);

        viewChangeLog.setText(lang.getString("viewChangeLog.text")); // NOI18N
        viewChangeLog.setName("viewChangeLog"); // NOI18N
        viewChangeLog.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                viewChangeLogActionPerformed(evt);
            }
        });
        versionNotifier.add(viewChangeLog);

        downloadLatestVersion.setText(lang.getString("downloadLatestVersion.text")); // NOI18N
        downloadLatestVersion.setName("downloadLatestVersion"); // NOI18N
        downloadLatestVersion.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                downloadLatestVersionActionPerformed(evt);
            }
        });
        versionNotifier.add(downloadLatestVersion);

        jMenuItem1.setText(lang.getString("jMenuItem1.text")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        jMenuItem1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        versionNotifier.add(jMenuItem1);

        menuBar.add(versionNotifier);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        serverStatusLabel.setText(lang.getString("serverStatusLabel.text")); // NOI18N
        serverStatusLabel.setName("serverStatusLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        statusBarJob.setText(lang.getString("statusBarJob.text")); // NOI18N
        statusBarJob.setName("statusBarJob"); // NOI18N

        GroupLayout statusPanelLayout = new GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(statusPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(statusAnimationLabel)
                    .addGroup(statusPanelLayout.createSequentialGroup()
                        .addComponent(serverStatusLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 235, Short.MAX_VALUE)
                        .addComponent(statusBarJob, GroupLayout.PREFERRED_SIZE, 77, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addGroup(statusPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(serverStatusLabel)
                        .addComponent(statusAnimationLabel)
                        .addComponent(statusBarJob, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
                    .addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        this.setLayout(new MigLayout("fill", "0[]0", "0[]0[min!]0"));
        this.add(mainPanel, "grow, wrap");
        this.add(statusPanel, "growx");
        this.setJMenuBar(menuBar);

        consoleOutput.setEditorKit(new javax.swing.text.html.HTMLEditorKit());
        consoleOutput.setStyledDocument(new javax.swing.text.html.HTMLDocument());
        backupStatusLog.setEditorKit(new javax.swing.text.html.HTMLEditorKit());
        backupStatusLog.setStyledDocument(new javax.swing.text.html.HTMLDocument());
        webLog.setEditorKit(new javax.swing.text.html.HTMLEditorKit());
        webLog.setStyledDocument(new javax.swing.text.html.HTMLDocument());

        // Sets the application icon
        this.setIconImage(Toolkit.getDefaultToolkit().createImage(Pail.this.getClass().getResource("resources/mcserverguiicon.png")));
    }

    /**
     * Action object for the toggling of the sayCheckBox
     */
    javax.swing.Action sayToggle = new javax.swing.AbstractAction() {
         public void actionPerformed(ActionEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
                 public void run() {
                    if (sayCheckBox.isSelected()) {
                        sayCheckBox.setSelected(false);
                    } else {
                        sayCheckBox.setSelected(true);
                    }
                }
            });
        }
    };

    public void performExitSequence() {
        saveConfigAction();

        if (server.isRunning()) {
            wantsToQuit = true;
            System.out.println("Server is running and GUI would like to exit");
            this.stopServer();
        } else {
            try {
                scheduler.shutdown();
            } catch (SchedulerException se) {
                se.printStackTrace();
            }
            exitGui();
        }
    }

    public void exitGui() {
        this.dispose();
        System.exit(0);
    }

    /**
     * Action object for the sending of input with prepended "Say " (caused by shift+enter)
     */
    javax.swing.Action saySend = new javax.swing.AbstractAction() {
         public void actionPerformed(ActionEvent e) {
            sendInput(true);
        }
    };

    private void startstopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startstopButtonActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                if (startstopButton.getText().equals("Start")) {
                    startServer();
                } else {
                    stopServer();
                }
            }
        });
    }//GEN-LAST:event_startstopButtonActionPerformed

    private void submitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitButtonActionPerformed
        sendInput();
    }//GEN-LAST:event_submitButtonActionPerformed

    private void consoleInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_consoleInputActionPerformed
        sendInput();
    }//GEN-LAST:event_consoleInputActionPerformed

    private void windowTitleFieldActionPerformed(java.awt.event.ActionEvent evt) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                config.setWindowTitle(windowTitleField.getText());
                Pail.this.setTitle(windowTitleField.getText());
                if (trayIcon != null) {
                    trayIcon.setToolTip(windowTitleField.getText());
                }
            }
        });
    }

    private void javaExecBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_javaExecBrowseButtonActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final JFileChooser fc = new JFileChooser();
                int returnVal = fc.showOpenDialog(Pail.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    javaExecField.setText(fc.getSelectedFile().getPath());
                    config.cmdLine.setJavaExec(fc.getSelectedFile().getPath());
                }
                cmdLineField.setText(config.cmdLine.parseCmdLine());
            }
        });
    }//GEN-LAST:event_javaExecBrowseButtonActionPerformed

    private void serverJarBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverJarBrowseButtonActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                try {
                    final JFileChooser fc = new JFileChooser(new File(".").getCanonicalPath());
                    int returnVal = fc.showOpenDialog(Pail.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        config.cmdLine.setServerJar(fc.getSelectedFile().getName());
                        serverJarField.setText(fc.getSelectedFile().getName());
                        cmdLineField.setText(config.cmdLine.parseCmdLine());
                    }
                } catch (IOException e) {
                    System.out.println("Error retrieving path");
                }
            }
        });
    }//GEN-LAST:event_serverJarBrowseButtonActionPerformed

    private void bukkitCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bukkitCheckBoxActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                config.cmdLine.setBukkit(bukkitCheckBox.isSelected());
                cmdLineField.setText(config.cmdLine.parseCmdLine());
            }
        });
    }//GEN-LAST:event_bukkitCheckBoxActionPerformed

    private void xmxMemoryFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xmxMemoryFieldActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                config.cmdLine.setXmx(xmxMemoryField.getText());
                cmdLineField.setText(config.cmdLine.parseCmdLine());
            }
        });
    }//GEN-LAST:event_xmxMemoryFieldActionPerformed

    private void xincgcCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xincgcCheckBoxActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                config.cmdLine.setXincgc(xincgcCheckBox.isSelected());
                cmdLineField.setText(config.cmdLine.parseCmdLine());
            }
        });
        
    }//GEN-LAST:event_xincgcCheckBoxActionPerformed

    private void extraArgsFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_extraArgsFieldActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                config.cmdLine.setExtraArgs(extraArgsField.getText());
                cmdLineField.setText(config.cmdLine.parseCmdLine());
            }
        });
    }//GEN-LAST:event_extraArgsFieldActionPerformed

    private void serverJarFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverJarFieldActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                config.cmdLine.setServerJar(serverJarField.getText());
                cmdLineField.setText(config.cmdLine.parseCmdLine());
            }
        });
    }//GEN-LAST:event_serverJarFieldActionPerformed

    private void javaExecFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_javaExecFieldActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                config.cmdLine.setJavaExec(javaExecField.getText());
                cmdLineField.setText(config.cmdLine.parseCmdLine());
            }
        });
    }//GEN-LAST:event_javaExecFieldActionPerformed

    private void saveGuiConfigButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveGuiConfigButtonActionPerformed
        saveConfig();
    }//GEN-LAST:event_saveGuiConfigButtonActionPerformed

    private void saveServerConfigButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveServerConfigButtonActionPerformed
        saveConfig();
    }//GEN-LAST:event_saveServerConfigButtonActionPerformed

    private void consoleOutputMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_consoleOutputMouseExited
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                int selMin = consoleOutput.getSelectionStart();
                int selMax = consoleOutput.getSelectionEnd();
                if ((server.isRunning()) && (selMax - selMin == 0)) {
                    textScrolling = true;
                }
                mouseInConsoleOutput = false;
            }
        });
    }//GEN-LAST:event_consoleOutputMouseExited

    private void consoleOutputFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_consoleOutputFocusGained
        textScrolling = false;
    }//GEN-LAST:event_consoleOutputFocusGained

    private void consoleOutputFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_consoleOutputFocusLost
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                int selMin = consoleOutput.getSelectionStart();
                int selMax = consoleOutput.getSelectionEnd();
                if ((selMax - selMin == 0) && (server.isRunning()) && (!mouseInConsoleOutput)) {
                    textScrolling = true;
                }
            }
        });
    }//GEN-LAST:event_consoleOutputFocusLost

    private void tabberKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tabberKeyTyped
        final java.awt.event.KeyEvent event = evt;
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                if(tabber.getSelectedIndex() == 0) {
                    giveInputFocus(event);
                }
            }
        });
    }//GEN-LAST:event_tabberKeyTyped

    private void playerListKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_playerListKeyTyped
        giveInputFocus(evt);
    }//GEN-LAST:event_playerListKeyTyped

    private void sayCheckBoxKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_sayCheckBoxKeyTyped
        giveInputFocus(evt);
    }//GEN-LAST:event_sayCheckBoxKeyTyped

    private void submitButtonKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_submitButtonKeyTyped
        giveInputFocus(evt);
    }//GEN-LAST:event_submitButtonKeyTyped

    private void consoleOutputKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_consoleOutputKeyTyped
        giveInputFocus(evt);
    }//GEN-LAST:event_consoleOutputKeyTyped

    private void consoleInputKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_consoleInputKeyPressed
        final java.awt.event.KeyEvent event = evt;
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                if (!inputHistory.isEmpty()) {
                    if (event.getKeyCode() == 38) {
                        // Move back through the input history
                        inputHistoryIndex++;
                        if (inputHistoryIndex > inputHistory.size()) {
                            inputHistoryIndex = 0;
                        }
                        if (inputHistoryIndex == inputHistory.size()) {
                            consoleInput.setText("");
                        } else {
                            consoleInput.setText(inputHistory.get(inputHistoryIndex));
                        }
                    } else if (event.getKeyCode() == 40) {
                        // Move forward through the input history
                        inputHistoryIndex--;
                        if (inputHistoryIndex < 0) {
                            inputHistoryIndex = inputHistory.size();
                        }
                        if (inputHistoryIndex == inputHistory.size()) {
                            consoleInput.setText("");
                        } else {
                            consoleInput.setText(inputHistory.get(inputHistoryIndex));
                        }
                    }
                }
            }
        });
    }//GEN-LAST:event_consoleInputKeyPressed

    private void customLaunchCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customLaunchCheckBoxActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                config.cmdLine.setUseCustomLaunch(customLaunchCheckBox.isSelected());
                cmdLineField.setText(config.cmdLine.parseCmdLine());
                if (customLaunchCheckBox.isSelected()) {
                    if (java.util.regex.Pattern.matches("^\\s*$", config.cmdLine.getCustomLaunch())) {
                        config.cmdLine.setUseCustomLaunch(false);
                        config.cmdLine.setCustomLaunch(config.cmdLine.parseCmdLine());
                        config.cmdLine.setUseCustomLaunch(true);
                        cmdLineField.setText(config.cmdLine.getCustomLaunch());
                    }
                    cmdLineField.setEditable(true);
                    javaExecField.setEditable(false);
                    javaExecBrowseButton.setEnabled(false);
                    serverJarField.setEditable(false);
                    serverJarBrowseButton.setEnabled(false);
                    xmxMemoryField.setEditable(false);
                    xincgcCheckBox.setEnabled(false);
                    extraArgsField.setEditable(false);
                } else {
                    if (java.util.regex.Pattern.matches("^\\s*$", config.cmdLine.getCustomLaunch())) {
                        config.cmdLine.setCustomLaunch(config.cmdLine.parseCmdLine());
                    }
                    cmdLineField.setEditable(false);
                    javaExecField.setEditable(true);
                    javaExecBrowseButton.setEnabled(true);
                    serverJarField.setEditable(true);
                    serverJarBrowseButton.setEnabled(true);
                    xmxMemoryField.setEditable(true);
                    xincgcCheckBox.setEnabled(true);
                    extraArgsField.setEditable(true);
                }
            }
        });  
    }//GEN-LAST:event_customLaunchCheckBoxActionPerformed

    private void cmdLineFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdLineFieldActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                config.cmdLine.setCustomLaunch(cmdLineField.getText());
                if (java.util.regex.Pattern.matches("^\\s*$", config.cmdLine.getCustomLaunch())) {
                    config.cmdLine.setUseCustomLaunch(false);
                    config.cmdLine.setCustomLaunch(config.cmdLine.parseCmdLine());
                    config.cmdLine.setUseCustomLaunch(true);
                    cmdLineField.setText(config.cmdLine.getCustomLaunch());
                }
            }
        });
    }//GEN-LAST:event_cmdLineFieldActionPerformed
    
    private void saveWorldsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveWorldsButtonActionPerformed
        this.sendInput("save-all");
    }//GEN-LAST:event_saveWorldsButtonActionPerformed

    private void saveBackupControlButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveBackupControlButtonActionPerformed
        saveConfig();
    }//GEN-LAST:event_saveBackupControlButtonActionPerformed

    private void backupButtonActionPerformed(java.awt.event.ActionEvent evt) {
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                config.backups.setPath(backupPathField.getText());
                backup();
            }
        });
        
    }

    private void backupPathBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backupPathBrowseButtonActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                try {
                    File backup = new File(backupPathField.getText());
                    final JFileChooser fc;
                    if (backup.exists()) {
                        fc = new JFileChooser(backup.getCanonicalPath());
                    } else {
                        fc = new JFileChooser(new File(".").getCanonicalPath());
                    }
                    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    int returnVal = fc.showOpenDialog(Pail.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        backupPathField.setText(fc.getSelectedFile().getPath());
                        config.backups.setPath(fc.getSelectedFile().getPath());
                    }
                } catch (IOException e) {
                    System.err.println("[MC Server Pail] Error retrieving program path.");
                }
            }
        });
    }//GEN-LAST:event_backupPathBrowseButtonActionPerformed

    private void backupPathFieldActionPerformed(java.awt.event.ActionEvent ignore) {
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                config.backups.setPath(backupPathField.getText());
            }
        });
    }

    private void zipBackupCheckBoxActionPerformed(java.awt.event.ActionEvent ignore) {
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                config.backups.setZip(zipBackupCheckBox.isSelected());
            }
        });
    }

    private void taskListAddButtonActionPerformed(java.awt.event.ActionEvent ignore) {
        addTaskListEntry();
    }

    private void backupAddButtonActionPerformed(java.awt.event.ActionEvent ignore) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final JFileChooser fc = new JFileChooser(new File("."));
                fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                fc.setDialogTitle("Select Files/Folders");
                fc.setMultiSelectionEnabled(true);
                int returnVal = fc.showOpenDialog(Pail.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File[] files = fc.getSelectedFiles();
                    for (File file : files) {
                        backupFileListModel.add(file);
                    }
                }
                cmdLineField.setText(config.cmdLine.parseCmdLine());
            }
        });
    }

    private void backupRemoveButtonActionPerformed(java.awt.event.ActionEvent ignore) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                System.out.println(backupFileList.getSelectedValues().getClass());
                //File[] files = (File[])backupFileList.getSelectedValues();
                //if (files != null) {
                //    removeFilesFromPaths(files);
                //}
            }
        });
    }

    public void removeFilesFromPaths(File[] files) {
        for (File file : files) {
            backupFileListModel.removeElement(file);
        }
    }

    private void taskListEditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_taskListEditButtonActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                 EventModel event = (EventModel)taskSchedulerList.getSelectedValue();
                 if (event != null) {
                    editTaskListEntry(event);
                 }
            }
        });
    }//GEN-LAST:event_taskListEditButtonActionPerformed

    private void taskListRemoveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_taskListRemoveButtonActionPerformed
        removeTaskListEntry();
    }//GEN-LAST:event_taskListRemoveButtonActionPerformed

    public void removeTaskListEntry() {
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                if (javax.swing.JOptionPane.showConfirmDialog(Pail.this,
                        "Are you sure you wish to remove this event?\n"
                        + "If it is running it will be interrupted.\n",
                        "Remove scheduled task",
                        javax.swing.JOptionPane.YES_NO_OPTION) ==
                        javax.swing.JOptionPane.YES_OPTION) {

                    EventModel event = (EventModel)taskSchedulerList
                            .getSelectedValue();
                    try {
                        scheduler.interrupt(JobKey.jobKey(event.getName()));
                        scheduler.deleteJob(JobKey.jobKey(event.getName()));
                    } catch (SchedulerException se) {
                        System.out.println("Error removing old task");
                    }
                    customButtonBoxModel1.removeElement(event.getName());
                    customButtonBoxModel2.removeElement(event.getName());
                    config.schedule.getEvents().removeElement(event);
                    config.save();
                }
            }
        });
    }

    public void removeTaskByName(String name) {
        final String taskname = name;
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                EventModel event = getTaskByName(taskname);
                if (event == null) return;
                try {
                    scheduler.interrupt(JobKey.jobKey(event.getName()));
                    scheduler.deleteJob(JobKey.jobKey(event.getName()));
                } catch (SchedulerException se) {
                    System.out.println("Error removing old task");
                }
                customButtonBoxModel1.removeElement(event.getName());
                customButtonBoxModel2.removeElement(event.getName());
                config.schedule.getEvents().removeElement(event);
                config.save();
            }
        });
    }

    private void textSizeFieldPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_textSizeFieldPropertyChange
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                config.display.setTextSize(Integer.parseInt(textSizeField.getValue().toString()));
            }
        });
    }//GEN-LAST:event_textSizeFieldPropertyChange

    private void textColorBoxMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_textColorBoxMouseClicked
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ColorChooser colorchooser = new ColorChooser(
                        Pail.this, textColorBox);
                colorchooser.setLocationRelativeTo(Pail.this);
               //@TODO Main.getApplication().show(colorchooser);
            }
        });
    }//GEN-LAST:event_textColorBoxMouseClicked

    private void bgColorBoxMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bgColorBoxMouseClicked
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                ColorChooser colorchooser = new ColorChooser(
                        Pail.this, bgColorBox);
                colorchooser.setLocationRelativeTo(Pail.this);
                //@TODO Main.getApplication().show(colorchooser);
            }
        });
    }

    private void infoColorBoxMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_infoColorBoxMouseClicked
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ColorChooser colorchooser = new ColorChooser(
                        Pail.this, infoColorBox);
                colorchooser.setLocationRelativeTo(Pail.this);
                //@TODO Main.getApplication().show(colorchooser);
            }
        });
    }//GEN-LAST:event_infoColorBoxMouseClicked

    private void warningColorBoxMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_warningColorBoxMouseClicked
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ColorChooser colorchooser = new ColorChooser(
                        Pail.this, warningColorBox);
                colorchooser.setLocationRelativeTo(Pail.this);
                //@TODO Main.getApplication().show(colorchooser);
            }
        });
    }//GEN-LAST:event_warningColorBoxMouseClicked

    private void severeColorBoxMouseClicked(java.awt.event.MouseEvent evt) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ColorChooser colorchooser = new ColorChooser(
                        Pail.this, severeColorBox);
                colorchooser.setLocationRelativeTo(Pail.this);
                //@TODO Main.getApplication().show(colorchooser);
            }
        });
    }

    private void textColorBoxFocusLost(java.awt.event.FocusEvent evt) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                String rgb = Integer.toHexString(textColorBox.getBackground().getRGB());
                rgb = rgb.substring(2, rgb.length());
                config.display.setTextColor(rgb);
            }
        });
    }

    private void bgColorBoxFocusLost(java.awt.event.FocusEvent evt) {
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                String rgb = Integer.toHexString(bgColorBox.getBackground().getRGB());
                rgb = rgb.substring(2, rgb.length());
                config.display.setBgColor(rgb);
                updateConsoleOutputBgColor();
            }
        });
    }

    private void infoColorBoxFocusLost(java.awt.event.FocusEvent evt) {
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                String rgb = Integer.toHexString(infoColorBox.getBackground().getRGB());
                rgb = rgb.substring(2, rgb.length());
                config.display.setInfoColor(rgb);
            }
        });
    }

    private void warningColorBoxFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_warningColorBoxFocusLost
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                String rgb = Integer.toHexString(warningColorBox.getBackground().getRGB());
                rgb = rgb.substring(2, rgb.length());
                config.display.setWarningColor(rgb);
            }
        });
    }//GEN-LAST:event_warningColorBoxFocusLost

    private void severeColorBoxFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_severeColorBoxFocusLost
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                String rgb = Integer.toHexString(severeColorBox.getBackground().getRGB());
                rgb = rgb.substring(2, rgb.length());
                config.display.setSevereColor(rgb);
            }
        });
    }//GEN-LAST:event_severeColorBoxFocusLost

    private void playerListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_playerListMouseClicked
        final java.awt.event.MouseEvent event = evt;
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                if (event.getButton() == event.BUTTON3 && (playerList.getSelectedIndex() > -1)) {
                    javax.swing.JPopupMenu playerListContextMenu = new javax.swing.JPopupMenu();
                    javax.swing.JMenuItem kickMenuItem;
                    kickMenuItem = new javax.swing.JMenuItem("Kick");
                    kickMenuItem.addActionListener(
                            new ActionListener() {
                         public void actionPerformed(ActionEvent ev) {
                            String s = (String)javax.swing.JOptionPane.showInputDialog(
                                    Pail.this, "Add a kick message or just "
                                    + "press enter.", "Kick Player", javax.swing
                                    .JOptionPane.PLAIN_MESSAGE, null, null, "");
                            playerListModel.findPlayer(playerListModel.getElementAt(
                                    playerList.getSelectedIndex())).kick(s);
                        }
                    });
                    javax.swing.JMenuItem banMenuItem;
                    banMenuItem = new javax.swing.JMenuItem("Ban");
                    banMenuItem.addActionListener(
                            new ActionListener() {
                         public void actionPerformed(ActionEvent ev) {
                            String s = (String)javax.swing.JOptionPane.showInputDialog(
                                    Pail.this, "Add a ban message or just "
                                    + "press enter.", "Ban Player", javax.swing
                                    .JOptionPane.PLAIN_MESSAGE, null, null, "Banned!");
                            server.banKick(playerListModel.findPlayer(
                                    playerListModel.getElementAt(
                                    playerList.getSelectedIndex())).getName(), s);
                        }
                    });
                    javax.swing.JMenuItem banIpMenuItem;
                    banIpMenuItem = new javax.swing.JMenuItem("Ban IP");
                    banIpMenuItem.addActionListener(
                            new ActionListener() {
                         public void actionPerformed(ActionEvent ev) {
                            String s = (String)javax.swing.JOptionPane.showInputDialog(
                                    Pail.this, "Add a ban message or just "
                                    + "press enter.", "Ban Player IP Address", javax.swing
                                    .JOptionPane.PLAIN_MESSAGE, null, null, "Banned!");
                            server.banKickIP(playerListModel.findPlayer(
                                    playerListModel.getElementAt(
                                    playerList.getSelectedIndex())).getIPAddress(), s);
                        }
                    });
                    playerListContextMenu.add(kickMenuItem);
                    playerListContextMenu.add(banMenuItem);
                    playerListContextMenu.add(banIpMenuItem);
                    playerListContextMenu.show(event.getComponent(), event.getX(), event.getY());
                }
            }
        });
    }//GEN-LAST:event_playerListMouseClicked

    private void clearLogCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearLogCheckBoxActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                config.backups.setClearLog(clearLogCheckBox.isSelected());
            }
        });
    }//GEN-LAST:event_clearLogCheckBoxActionPerformed

    private void startServerOnLaunchCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startServerOnLaunchCheckBoxActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                config.setServerStartOnStartup(startServerOnLaunchCheckBox.isSelected());
            }
        });
    }//GEN-LAST:event_startServerOnLaunchCheckBoxActionPerformed

    private void useWebInterfaceCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useWebInterfaceCheckBoxActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                config.web.setEnabled(useWebInterfaceCheckBox.isSelected());
                if (useWebInterfaceCheckBox.isSelected()) {
                    config.web.setPassword(String.valueOf(webPasswordField.getPassword()));
                    if (webPortField.getInputVerifier().verify(webPortField)) {
                        config.web.setPort(Integer.valueOf(webPortField.getText()));
                        webServer.setPort(config.web.getPort());
                        webServer.start();
                    } else {
                        webPortField.requestFocusInWindow();
                    }
                } else {
                    webServer.stop();
                }
            }
        });
    }//GEN-LAST:event_useWebInterfaceCheckBoxActionPerformed

    private void webPortFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_webPortFieldFocusLost
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                config.web.setPort(Integer.valueOf(webPortField.getText()));
            }
        });
    }//GEN-LAST:event_webPortFieldFocusLost

    private void showWebPasswordButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showWebPasswordButtonActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                if (showWebPasswordButton.isSelected()) {
                    webPasswordField.setEchoChar((char)0);
                } else {
                    webPasswordField.setEchoChar('\u25cf');
                }
            }
        });
    }//GEN-LAST:event_showWebPasswordButtonActionPerformed

    private void webPasswordFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_webPasswordFieldFocusLost
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                config.web.setPassword(String.valueOf(webPasswordField.getPassword()));
            }
        });
    }//GEN-LAST:event_webPasswordFieldFocusLost

    private void disableGetOutputNotificationsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disableGetOutputNotificationsCheckBoxActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                config.web.setDisableGetRequests(disableGetOutputNotificationsCheckBox.isSelected());
            }
        });
    }//GEN-LAST:event_disableGetOutputNotificationsCheckBoxActionPerformed

    private void consoleOutputMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_consoleOutputMouseClicked
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                if (server.isRunning()) {
                    textScrolling = false;
                }
            }
        });
    }

    private void customButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        customButtonAction(customCombo1);
    }

    private void customButton2ActionPerformed(java.awt.event.ActionEvent evt) {
        customButtonAction(customCombo2);
    }

    private void useProxyCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                config.setProxy(useProxyCheckBox.isSelected());
                serverIpField.setEnabled(!useProxyCheckBox.isSelected());
                if (useProxyCheckBox.isSelected()) {
                    playerList.setToolTipText("This shows a list of players connected to the server.  Right click a to pull up the player action menu.");
                } else {
                    playerList.setToolTipText("Player list is currently only supported when using the Pail's Proxy feature.");
                }
            }
        });
    }

    private void hideMenuMouseClicked(java.awt.event.MouseEvent evt) {
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                Pail.this.setVisible(false);
                isHidden = true;
            }
        }); 
    }

    private void textSizeFieldStateChanged(javax.swing.event.ChangeEvent evt) {
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                config.display.setTextSize(Integer.parseInt(textSizeField.getValue().toString()));
            }
        });
    }

    private void commandPrefixFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_commandPrefixFieldActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                config.setCommandPrefix(commandPrefixField.getText());
            }
        });
    }//GEN-LAST:event_commandPrefixFieldActionPerformed

    private void launchSupportPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_launchSupportPageActionPerformed
        String url = "http://forums.bukkit.org/threads/admin-mc-server-pail-8-2-cross-platform-a-pail-wrapper-for-your-server-now-w-remote-ctrl-860.17834/";
        try {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (IOException ioe) {
            System.out.println("Error launching page.");
        }
    }//GEN-LAST:event_launchSupportPageActionPerformed

    private void viewChangeLogActionPerformed(java.awt.event.ActionEvent evt) {
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                ChangeLog changeLog = new ChangeLog(Pail.this, versionNumber);
                changeLog.setLocationRelativeTo(Pail.this);
                //@TODO Main.getApplication().show(changeLog);
            }
        });
    }

    private void downloadLatestVersionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downloadLatestVersionActionPerformed
        String urltext = "https://raw.github.com/dumptruckman/MC-Server-Pail--multi-/master/VERSION";
        String newVersion= "";
        try {
            URL url = new URL(urltext);
            BufferedReader in = new BufferedReader(new InputStreamReader(url
                    .openStream()));
            String line = "";
            if ((line = in.readLine()) != null) {
                newVersion = line;
            }
            in.close();
            String downloadurl = "https://github.com/downloads/dumptruckman/MC-Server-Pail--multi-/org.dumptruckman.pail.pail-" + newVersion + ".zip";
            try {
                java.awt.Desktop.getDesktop().browse(java.net.URI.create(downloadurl));
            } catch (IOException ioe) {
                System.out.println("Error launching page.");
            }
        } catch (java.net.MalformedURLException e) {
        } catch (IOException ioe) {
        }
    }//GEN-LAST:event_downloadLatestVersionActionPerformed

    private void pauseSchedulerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseSchedulerButtonActionPerformed
        pauseSchedule();
    }//GEN-LAST:event_pauseSchedulerButtonActionPerformed

    public boolean pauseSchedule() {
        class SchedulePauser implements Runnable {
            public SchedulePauser() {
                super();
            }

             public void run() {
                schedulePaused = !schedulePaused;
                if (schedulePaused) {
                    pauseSchedulerButton.setSelected(true);
                    try {
                        scheduler.pauseAll();
                        pauseSchedulerButton.setFont(new java.awt.Font("Tahoma",
                                java.awt.Font.BOLD, 11));
                    } catch (SchedulerException se) {
                        schedulePaused = !schedulePaused;
                        pauseSchedulerButton.setSelected(false);
                    }
                } else {
                    pauseSchedulerButton.setSelected(false);
                    try {
                        scheduler.resumeAll();
                        pauseSchedulerButton.setFont(new java.awt.Font("Tahoma",
                                java.awt.Font.PLAIN, 11));
                    } catch (SchedulerException se) {
                        schedulePaused = !schedulePaused;
                        pauseSchedulerButton.setSelected(true);
                    }
                }
            }

            public boolean getPaused() {
                return schedulePaused;
            }
        }

        try {
            SchedulePauser pause = new SchedulePauser();
            SwingUtilities.invokeAndWait(pause);
            return pause.getPaused();
        } catch (InterruptedException ie) {
            return schedulePaused;
        } catch (java.lang.reflect.InvocationTargetException ite) {
            return schedulePaused;
        }
    }

    private void taskSchedulerListKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_taskSchedulerListKeyTyped
        final java.awt.event.KeyEvent event = evt;
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                if (event.getKeyChar() == java.awt.event.KeyEvent.VK_DELETE) {
                    removeTaskListEntry();
                }
            }
        });
    }//GEN-LAST:event_taskSchedulerListKeyTyped

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        String url = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=9XY8KKATD26GL";
        try {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (IOException ioe) {
            System.out.println("Error launching page.");
        }
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void backupFileChooserMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_backupFileChooserMouseClicked

    }

    private void consoleOutputMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_consoleOutputMouseEntered
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                mouseInConsoleOutput = true;
            }
        });
    }//GEN-LAST:event_consoleOutputMouseEntered

    private void customButtonAction(javax.swing.JComboBox box) {
        final javax.swing.JComboBox boxxy = box;
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                if (boxxy.getSelectedItem().toString().equals("Edit Tasks")) {
                    tabber.setSelectedIndex(tabber.indexOfTab("Tasks"));
                } else {
                    startTaskByName(boxxy.getSelectedItem().toString());
                }
            }
        });
    }

    public javax.swing.JCheckBox allowFlightCheckBox;
    public javax.swing.JCheckBox allowNetherCheckBox;
    public javax.swing.JButton backupButton;
    public JList backupFileList;
    public javax.swing.JPanel backupFileChooserPanel;
    public javax.swing.JButton backupPathBrowseButton;
    public javax.swing.JButton backupAddButton;
    public javax.swing.JButton backupRemoveButton;
    public javax.swing.JTextField backupPathField;
    public javax.swing.JLabel backupPathLabel;
    public javax.swing.JPanel backupSettingsPanel;
    public javax.swing.JTextPane backupStatusLog;
    public javax.swing.JPanel backupStatusPanel;
    public javax.swing.JPanel backupTab;
    public javax.swing.JTextField bgColorBox;
    public javax.swing.JLabel bgColorLabel;
    public javax.swing.JCheckBox bukkitCheckBox;
    public javax.swing.JCheckBox clearLogCheckBox;
    public javax.swing.JTextField cmdLineField;
    public javax.swing.JTextField commandPrefixField;
    public javax.swing.JLabel commandPrefixLabel;
    public javax.swing.JTextField consoleInput;
    public javax.swing.JPanel consoleInputPanel;
    public javax.swing.JTextPane consoleOutput;
    public javax.swing.JPanel consoleOutputPanel;
    public javax.swing.JButton customButton1;
    public javax.swing.JButton customButton2;
    public javax.swing.JComboBox customCombo1;
    public javax.swing.JComboBox customCombo2;
    public javax.swing.JCheckBox customLaunchCheckBox;
    public javax.swing.JCheckBox disableGetOutputNotificationsCheckBox;
    public javax.swing.JMenuItem downloadLatestVersion;
    public javax.swing.JTextField extPortField;
    public javax.swing.JLabel extPortLabel;
    public javax.swing.JTextField intPortField;
    public javax.swing.JLabel intPortLabel;
    public javax.swing.JTextField extraArgsField;
    public javax.swing.JLabel extraArgsLabel;
    public javax.swing.JPanel themeTab;
    public javax.swing.JLabel guiCpuUsage;
    public javax.swing.JLabel guiCpuUsageLabel;
    public javax.swing.JPanel guiInfoPanel;
    public javax.swing.JLabel guiMemoryUsage;
    public javax.swing.JLabel guiMemoryUsageLabel;
    public javax.swing.JMenu hideMenu;
    public javax.swing.JTextField infoColorBox;
    public javax.swing.JLabel infoColorLabel;
    public javax.swing.JTextField inputHistoryMaxSizeField;
    public javax.swing.JLabel inputHistoryMaxSizeLabel;
    public javax.swing.JLabel customCommandLineLabel;
    public javax.swing.JMenuItem jMenuItem1;
    public javax.swing.JPanel colorizationPanel;
    public javax.swing.JPanel webInterfaceConfigPanel;
    public javax.swing.JPanel guiConfigPanel;
    public javax.swing.JScrollPane consoleOutScrollPane;
    public javax.swing.JScrollPane playerListScrollPane;
    public javax.swing.JScrollPane backupFileListScrollPane;
    public javax.swing.JScrollPane backupStatusLogScrollPane;
    public javax.swing.JScrollPane taskSchedulerScrollPane;
    public javax.swing.JScrollPane webLogScrollPane;
    public javax.swing.JButton javaExecBrowseButton;
    public javax.swing.JTextField javaExecField;
    public javax.swing.JLabel javaExecLabel;
    public javax.swing.JMenuItem launchSupportPage;
    public javax.swing.JTextField levelNameField;
    public javax.swing.JLabel levelNameLabel;
    public javax.swing.JTextField levelSeedField;
    public javax.swing.JLabel levelSeedLabel;
    public javax.swing.JPanel mainPanel;
    public javax.swing.JPanel mainWindowTab;
    public javax.swing.JLabel maxPlayersLabel;
    public javax.swing.JSpinner maxPlayersSpinner;
    public javax.swing.JMenuBar menuBar;
    public javax.swing.JCheckBox onlineModeCheckBox;
    public javax.swing.JToggleButton pauseSchedulerButton;
    public javax.swing.JList playerList;
    public javax.swing.JPanel playerListPanel;
    private javax.swing.JProgressBar progressBar;
    public javax.swing.JCheckBox pvpCheckBox;
    public javax.swing.JLabel receivingBytes;
    public javax.swing.JLabel receivingBytesLabel;
    public javax.swing.JButton saveBackupControlButton;
    public javax.swing.JButton saveThemeButton;
    public javax.swing.JButton saveServerConfigButton;
    public javax.swing.JButton saveWorldsButton;
    public javax.swing.JCheckBox sayCheckBox;
    public javax.swing.JPanel schedulerTab;
    public javax.swing.JPanel serverCmdLinePanel;
    public javax.swing.JPanel configurationTab;
    public javax.swing.JPanel serverControlPanel;
    public javax.swing.JLabel serverCpuUsage;
    public javax.swing.JLabel serverCpuUsageLabel;
    public javax.swing.JPanel serverInfoPanel;
    public javax.swing.JTextField serverIpField;
    public javax.swing.JLabel serverIpLabel;
    public javax.swing.JButton serverJarBrowseButton;
    public javax.swing.JTextField serverJarField;
    public javax.swing.JLabel serverJarLabel;
    public javax.swing.JLabel serverMemoryUsage;
    public javax.swing.JLabel serverMemoryUsageLabel;
    public javax.swing.JTextField serverPortField;
    public javax.swing.JLabel serverPortLabel;
    public javax.swing.JPanel serverPropertiesPanel;
    private javax.swing.JLabel serverStatusLabel;
    public javax.swing.JTextField severeColorBox;
    public javax.swing.JLabel severeColorLabel;
    public javax.swing.JToggleButton showWebPasswordButton;
    public javax.swing.JCheckBox spawnAnimalsCheckBox;
    public javax.swing.JCheckBox spawnMonstersCheckBox;
    public javax.swing.JTextField spawnProtectionField;
    public javax.swing.JLabel spawnProtectionLabel;
    public javax.swing.JCheckBox startServerOnLaunchCheckBox;
    public javax.swing.JButton startstopButton;
    private javax.swing.JLabel statusAnimationLabel;
    public javax.swing.JLabel statusBarJob;
    public javax.swing.JPanel statusPanel;
    public javax.swing.JButton submitButton;
    public javax.swing.JTabbedPane tabber;
    //public javax.swing.JTabbedPane configTabber;
    public javax.swing.JButton taskListAddButton;
    public javax.swing.JButton taskListEditButton;
    public javax.swing.JButton taskListRemoveButton;
    public javax.swing.JList taskSchedulerList;
    public javax.swing.JPanel taskSchedulerPanel;
    public javax.swing.JTextField textColorBox;
    public javax.swing.JLabel textColorLabel;
    public javax.swing.JSpinner textSizeField;
    public javax.swing.JLabel textSizeLabel;
    public javax.swing.JLabel transmittingBytes;
    public javax.swing.JLabel transmittingBytesLabel;
    public javax.swing.JCheckBox useNetStat;
    public javax.swing.JCheckBox useProxyCheckBox;
    public javax.swing.JCheckBox useWebInterfaceCheckBox;
    public javax.swing.JLabel versionLabel;
    public javax.swing.JMenu versionNotifier;
    public javax.swing.JMenuItem viewChangeLog;
    public javax.swing.JLabel viewDistanceLabel;
    public javax.swing.JSpinner viewDistanceSpinner;
    public javax.swing.JTextField warningColorBox;
    public javax.swing.JLabel warningColorLabel;
    public javax.swing.JPanel webInterfaceTab;
    public javax.swing.JTextPane webLog;
    public javax.swing.JPanel webLogPanel;
    public javax.swing.JPasswordField webPasswordField;
    public javax.swing.JLabel webPasswordLabel;
    public javax.swing.JTextField webPortField;
    public javax.swing.JLabel webPortLabel;
    public javax.swing.JCheckBox whiteListCheckBox;
    public javax.swing.JTextField windowTitleField;
    public javax.swing.JLabel windowTitleLabel;
    public javax.swing.JCheckBox xincgcCheckBox;
    public javax.swing.JTextField xmxMemoryField;
    public javax.swing.JLabel xmxMemoryLabel;
    public javax.swing.JCheckBox zipBackupCheckBox;
    // End of variables declaration//GEN-END:variables

    public void outOfDate(String version) {
        final String ver = version;
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                versionNotifier.setForeground(Color.red);
                versionNotifier.setText("New version " + ver + " is available!");
            }
        });
    }

    public void setPlayerList(PlayerList playerListModel) {
        final PlayerList pl = playerListModel;
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                Pail.this.playerListModel = pl;
                playerList.setModel(pl);
                playerList.updateUI();
            }
        });
    }

    public boolean startTaskByName(String name) {
        EventModel event = getTaskByName(name);
        if (event != null) {
            scheduleImmediateEvent(event, scheduler, this);
            return true;
        }
        System.out.println("Could not find event by that name");
        return false;
    }

    public EventModel getTaskByName(String name) {
        java.util.Iterator it = config.schedule.getEvents().iterator();
        while (it.hasNext()) {
            EventModel event = (EventModel)it.next();
            if (event.getName().equalsIgnoreCase(name)) {
                return event;
            }
        }
        return null;
    }

    private void enableSystemTrayIcon() {
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                trayIcon = null;
                if (java.awt.SystemTray.isSupported()) {
                    hideMenu.setEnabled(true);
                    hideMenu.setToolTipText("Press this to minimize to tray.");
                    // get the SystemTray instance
                    java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
                    // load an image
                    // create a action listener to listen for default action executed on the tray icon
                    ActionListener listener = new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            Pail.this.setVisible(!isHidden);
                            isHidden = !isHidden;
                        }
                    };
                    // create a popup menu
                    java.awt.PopupMenu popup = new java.awt.PopupMenu();
                    // create menu item for the default action
                    java.awt.MenuItem defaultItem = new java.awt.MenuItem("Show/Hide");
                    defaultItem.addActionListener(listener);
                    popup.add(defaultItem);

                    trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().createImage(Pail.this.getClass().getResource("resources/mcserverguiicon.png")), config.getWindowTitle(), popup);

                    trayIcon.setImageAutoSize(true);
                    // set the TrayIcon properties
                    trayIcon.addActionListener(listener);
                    // add the tray image
                    try {
                        tray.add(trayIcon);
                    } catch (java.awt.AWTException e) {
                        System.err.println(e);
                    }
                } else {
                    hideMenu.setEnabled(false);
                    hideMenu.setToolTipText("Your Operating System does not support this action!");
                }
            }
        });
    }

    public void addTaskListEntry() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                taskDialog = new TaskDialog(Pail.this);
                taskDialog.setLocationRelativeTo(Pail.this);
                //@TODO Main.getApplication().show(taskDialog);
            }
        });
    }

    public void editTaskListEntry(EventModel evt) {
        final EventModel event = evt;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                taskDialog = new TaskDialog(Pail.this, event);
                taskDialog.setLocationRelativeTo(Pail.this);
                taskDialog.setVisible(true);
            }
        });
    }

    public void backup() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                stateBeforeBackup = controlState;
                controlSwitcher("BACKUP");
                statusBarJob.setText("Backing up:");
                if (server.isRunning()) {
                    sendInput("save-off");
                    sendInput("say Backing up server...");
                }
                backup = new Backup(Pail.this);
                backup.startBackup();
            }
        });
    }

    /**
     * Sends whatever is in the console box to the server as is.
     */
    public void sendInput() {
        sendInput(false);
    }

    /**
     * Sends whatever is in the console box to the server.
     * @param b determines if "say " should be prepended to the text.
     */
    public void sendInput(boolean b) {
        final boolean shouldSay = b;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                String stringToSend = consoleInput.getText();
                if (inputHistory.size() >= config.getInputHistoryMaxSize()) {
                    inputHistory.remove(inputHistory.size() - 1);
                }
                inputHistory.add(0, stringToSend);
                if ((sayCheckBox.isSelected()) && (!shouldSay)) {
                    sendInput("say " + stringToSend);
                } else if ((!sayCheckBox.isSelected()) && (shouldSay)) {
                    sendInput("say " + stringToSend);
                } else {
                    sendInput(stringToSend);
                }
                consoleInput.setText("");
                inputHistoryIndex = -1;
            }
        });
    }

    /**
     * Sends a String to the server.
     * @param s String to send to the server
     */
    public void sendInput(String s) {
        server.send(s);
    }

    public void setSaving(boolean b) {
        saving = b;
    }

    public boolean isSaving() {
        return saving;
    }

    /**
     * Determines if keyboard focus should be given to the console input field based on the passed KeyEvent.
     * Basically any alpha-numeric keys will cause focus to be granted.
     * @param event KeyEvent to be passed in.
     */
    public void giveInputFocus(java.awt.event.KeyEvent event) {
        final java.awt.event.KeyEvent evt = event;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if ((evt.getKeyChar() != java.awt.event.KeyEvent.CHAR_UNDEFINED) && (consoleInput.isEnabled()) && ((int)evt.getKeyChar() > 32)) {
                    if (consoleInput.requestFocusInWindow()) {
                        consoleInput.setText(consoleInput.getText() + evt.getKeyChar());
                    }
                }
            }
        });
    }

    public void startWebServer() {
        webServer.setPort(config.web.getPort());
        webServer.start();
    }

    public void stopWebServer() {
        webServer.stop();
    }

    public String getConsoleOutput(OutputFormat format) {
        String output = getConsoleOutput();
        output = Jsoup.clean(output, Whitelist.none().addTags("br"));
        switch (format) {
            case LINEBREAK:
                //output = output.replaceAll("(<font.*>|</font>)", "");
                break;
            case PLAINTEXTCRLF:
                //output = output.replaceAll("(<font.*>|</font>)", "");
                output = output.replaceAll("<br />", Character.toString((char)13)
                        + Character.toString((char)10));
                break;
            case PLAINTEXTLFCR:
                //output = output.replaceAll("(<font.*>|</font>)", "");
                output = output.replaceAll("<br />", Character.toString((char)10)
                        + Character.toString((char)13));
                break;
            case PLAINTEXTLF:
                //output = output.replaceAll("(<font.*>|</font>)", "");
                output = output.replaceAll("<br />", Character.toString((char)10));
                break;
            case PLAINTEXTCR:
                //output = output.replaceAll("(<font.*>|</font>)", "");
                output = output.replaceAll("<br />", Character.toString((char)13));
                break;
            default:
        }
        return output;
    }

    public String getConsoleOutput() {
        String output = "";
        
        class ConsoleOutput implements Runnable {
            String output;
            
            public ConsoleOutput() {
                super();
                output = "";
            }
            
             public void run() {
                output = consoleOutput.getText();
            }

            public String getOutput() {
                return output;
            }
        }
        
        try {
            ConsoleOutput conOut = new ConsoleOutput();
            SwingUtilities.invokeAndWait(conOut);
            output = conOut.getOutput();
        } catch (InterruptedException ie) {
            output = "[MC Server Pail] Interrupted while retrieving output from Pail!";
        } catch (java.lang.reflect.InvocationTargetException ite) {
            output = "[MC Server Pail] Error retrieving output from Pail!";
        }
        
        output = output.replaceAll("(<html.*>|<body.*>|<head.*>|</head>|</body>|</html>)", "");
        return output;
    }

    /**
     * Initializes the config file if necessary and sets all the pail elements to their config'd values
     * Usually this is only called once during the constructor.
     */
    public void initConfig() {
        setConsoleOutput("");
        if (config.load()) {
            guiLog("Configuration file loaded succesfully!");
        } else {
            guiLog("Configuration file not found or invalid!", LogLevel.WARNING);
            guiLog("Creating new config file with default values.");
        }
        if (config.cmdLine.getServerJar().isEmpty()) {
            if (detectServerJar()) {
                guiLog("Automatically detected server jar file: "
                        + config.cmdLine.getServerJar());
            } else {
                guiLog("Could not locate a server jar file automatically!",
                        LogLevel.WARNING);
            }
        }
    }

    public boolean detectServerJar() {
        File dir = new File(".");
        File[] files = dir.listFiles();

        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().matches("(\\S*bukkit\\S*.jar|\\S*server\\S*"
                    + ".jar)")) {
                config.cmdLine.setServerJar(files[i].getName());
                return true;
            }
        }
        return false;
    }

    public void initSchedule() {
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                java.util.Iterator it = config.schedule.getEvents().iterator();
                while (it.hasNext()) {
                    EventModel event = (EventModel)it.next();
                    if (!event.isCustomButton()) {
                        scheduleEvent(event, Pail.this);
                    } else {
                        customCombo1.addItem(event.getName());
                        customCombo2.addItem(event.getName());
                    }
                }
                customCombo1.setSelectedItem(config.getCustomButton1());
                customCombo2.setSelectedItem(config.getCustomButton2());
            }
        });
    }

    public void initBackupFileChooser() {
        // @TODO
    }

    public void updateGuiWithServerProperties() {
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                allowFlightCheckBox.setSelected(serverProperties.getAllowFlight());
                allowNetherCheckBox.setSelected(serverProperties.getAllowNether());
                onlineModeCheckBox.setSelected(serverProperties.getOnlineMode());
                pvpCheckBox.setSelected(serverProperties.getPvp());
                spawnAnimalsCheckBox.setSelected(serverProperties.getSpawnAnimals());
                spawnMonstersCheckBox.setSelected(serverProperties.getSpawnMonsters());
                whiteListCheckBox.setSelected(serverProperties.getWhiteList());
                levelNameField.setText(serverProperties.getLevelName());
                levelSeedField.setText(serverProperties.getLevelSeed());
                serverIpField.setText(serverProperties.getServerIp());
                serverPortField.setText(serverProperties.getServerPort());
                spawnProtectionField.setText(serverProperties.getSpawnProtection());
                maxPlayersSpinner.setValue(serverProperties.getMaxPlayers());
                viewDistanceSpinner.setValue(serverProperties.getViewDistance());
            }
        });
    }

    public void updateGuiWithConfigValues() {
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                textColorBox.setBackground(java.awt.Color.decode("0x"
                        + config.display.getTextColor()));
                bgColorBox.setBackground(java.awt.Color.decode("0x"
                        + config.display.getBgColor()));
                infoColorBox.setBackground(java.awt.Color.decode("0x"
                        + config.display.getInfoColor()));
                warningColorBox.setBackground(java.awt.Color.decode("0x"
                        + config.display.getWarningColor()));
                severeColorBox.setBackground(java.awt.Color.decode("0x"
                        + config.display.getSevereColor()));
                textSizeField.setValue(config.display.getTextSize());
                webPortField.setText(Integer.toString(config.web.getPort()));
                useWebInterfaceCheckBox.setSelected(config.web.isEnabled());
                webPasswordField.setText(config.web.getPassword());
                disableGetOutputNotificationsCheckBox.setSelected(config.web.isDisableGetRequests());
                useProxyCheckBox.setSelected(config.getProxy());
                serverIpField.setEnabled(!useProxyCheckBox.isSelected());
                extPortField.setText(Integer.toString(config.getExtPort()));
                startServerOnLaunchCheckBox.setSelected(config.getServerStartOnStartup());
                zipBackupCheckBox.setSelected(config.backups.getZip());
                clearLogCheckBox.setSelected(config.backups.getClearLog());
                //pathsToBackup = config.backups.getPathsToBackup();
                backupPathField.setText(config.backups.getPath());
                //backupFileChooser.setCheckingPaths(createTreePathArray(pathsToBackup));
                initBackupFileChooser();
                windowTitleField.setText(config.getWindowTitle());
                commandPrefixField.setText(config.getCommandPrefix());
                Pail.this.setTitle(windowTitleField.getText());
                javaExecField.setText(config.cmdLine.getJavaExec());
                serverJarField.setText(config.cmdLine.getServerJar());
                bukkitCheckBox.setSelected(config.cmdLine.getBukkit());
                xmxMemoryField.setText(config.cmdLine.getXmx());
                xincgcCheckBox.setSelected(config.cmdLine.getXincgc());
                extraArgsField.setText(config.cmdLine.getExtraArgs());
                customLaunchCheckBox.setSelected(config.cmdLine.getUseCustomLaunch());
                inputHistoryMaxSizeField.setText(Integer.toString(config.getInputHistoryMaxSize()));
                if (!config.cmdLine.getUseCustomLaunch()) {
                    if (java.util.regex.Pattern.matches("^\\s*$", config.cmdLine.getCustomLaunch())) {
                        config.cmdLine.setCustomLaunch(config.cmdLine.parseCmdLine());
                    }
                } else {
                    if (java.util.regex.Pattern.matches("^\\s*$", config.cmdLine.getCustomLaunch())) {
                        config.cmdLine.setUseCustomLaunch(false);
                        config.cmdLine.setCustomLaunch(config.cmdLine.parseCmdLine());
                        config.cmdLine.setUseCustomLaunch(true);
                    }
                    cmdLineField.setEditable(true);
                    javaExecField.setEditable(false);
                    javaExecBrowseButton.setEnabled(false);
                    serverJarField.setEditable(false);
                    serverJarBrowseButton.setEnabled(false);
                    xmxMemoryField.setEditable(false);
                    xincgcCheckBox.setEnabled(false);
                    extraArgsField.setEditable(false);
                }
                cmdLineField.setText(config.cmdLine.parseCmdLine());
                taskSchedulerList.setModel(config.schedule.getEvents());
                if (useProxyCheckBox.isSelected()) {
                    playerList.setToolTipText("This shows a list of players connected to the server.  Right click a to pull up the player action menu.");
                } else {
                    playerList.setToolTipText("Player list is currently only supported when using the Pail's Proxy feature.");
                }
            }
        });
    }

    public void saveBackupPathsToConfig() {
        config.backups.getPathsToBackup().clear();
        //@TODO
    }

    public void saveConfigAction() {
        config.setWindowTitle(windowTitleField.getText());
        this.setTitle(windowTitleField.getText());
        if (trayIcon != null) {
            trayIcon.setToolTip(windowTitleField.getText());
        }
        config.setInputHistoryMaxSize(Integer.parseInt(inputHistoryMaxSizeField.getText()));
        config.setCommandPrefix(commandPrefixField.getText());
        config.setCustomButton1(customCombo1.getSelectedItem().toString());
        config.setCustomButton2(customCombo2.getSelectedItem().toString());
        config.cmdLine.setXmx(xmxMemoryField.getText());
        config.cmdLine.setExtraArgs(extraArgsField.getText());
        config.cmdLine.setServerJar(serverJarField.getText());
        config.cmdLine.setJavaExec(javaExecField.getText());
        config.display.setTextSize(Integer.valueOf(textSizeField.getValue().toString()));
        if (config.cmdLine.getUseCustomLaunch()) {
            config.cmdLine.setCustomLaunch(cmdLineField.getText());
            if (java.util.regex.Pattern.matches("^\\s*$", cmdLineField.getText())) {
                config.cmdLine.setUseCustomLaunch(false);
                config.cmdLine.setCustomLaunch(config.cmdLine.parseCmdLine());
                config.cmdLine.setUseCustomLaunch(true);
            } else {
                config.cmdLine.setCustomLaunch(config.cmdLine.parseCmdLine());
            }
            cmdLineField.setText(config.cmdLine.parseCmdLine());
        } else {
            cmdLineField.setText(config.cmdLine.parseCmdLine());
            if (java.util.regex.Pattern.matches("^\\s*$", config.cmdLine.getCustomLaunch())) {
                config.cmdLine.setCustomLaunch(config.cmdLine.parseCmdLine());
            }
        }
        config.backups.setPath(backupPathField.getText());
        saveBackupPathsToConfig();
        config.setProxy(useProxyCheckBox.isSelected());
        config.setExtPort(Integer.valueOf(extPortField.getText()));

        config.save();
        saveServerProperties();
    }
    /**
     * Saves the config file with any changes made by the user through the pail.
     */
    public void saveConfig() {
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                saveConfigAction();
            }
        });
    }

    public void saveServerProperties() {
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                serverProperties.setAllowFlight(allowFlightCheckBox.isSelected());
                serverProperties.setAllowNether(allowNetherCheckBox.isSelected());
                serverProperties.setOnlineMode(onlineModeCheckBox.isSelected());
                serverProperties.setPvp(pvpCheckBox.isSelected());
                serverProperties.setSpawnAnimals(spawnAnimalsCheckBox.isSelected());
                serverProperties.setSpawnMonsters(spawnMonstersCheckBox.isSelected());
                serverProperties.setWhiteList(whiteListCheckBox.isSelected());
                serverProperties.setLevelName(levelNameField.getText());
                serverProperties.setLevelSeed(levelSeedField.getText());
                serverProperties.setServerIp(serverIpField.getText());
                serverProperties.setServerPort(serverPortField.getText());
                serverProperties.setSpawnProtection(spawnProtectionField.getText());
                serverProperties.setMaxPlayers(Integer.valueOf(maxPlayersSpinner.getValue().toString()));
                serverProperties.setViewDistance(Integer.valueOf(viewDistanceSpinner.getValue().toString()));
                if (!server.isRunning()) {
                    try {
                        serverProperties.writeProps();
                    } catch (IOException ioe) {

                    }
                }
            }
        });
    }

    /**
     * As long as textScrolling is true, this will cause the consoleOutput to be scrolled to the bottom.
     */
    public void scrollText() {
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                if (textScrolling) {
                    consoleOutput.setCaretPosition(consoleOutput.getDocument().getLength());
                }
                webLog.setCaretPosition(webLog.getDocument().getLength());
            }
        });
    }

    public boolean isRestarting() { return restarting; }

    public void restartServer() { restartServer(0); }

    public void restartServer(int delay) {
        restarting = true;
        restartDelay = delay;
        stopServer();
    }

    /**
     * Starts the Minecraft server and verifies that it started properly.
     */
    public void startServer() {
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                if (controlState.equals("OFF")) {
                    setConsoleOutput("");
                    if (backup != null) {
                        if (!backup.task.isDone()) {
                            guiLog("Stopping backup first.");
                            backup.task.cancel(true);
                        }
                    }
                    server.setCmdLine(config.cmdLine.getCmdLine());
                    String start = server.start();
                    if (start.equals("SUCCESS")) {
                    } else if (start.equals("ERROR")) {
                        
                    } else if (start.equals("INVALIDJAR")) {
                        guiLog("The jar file you specified is not a valid file."
                                + "  Please make corrections on the Server "
                                + "Config tab.", LogLevel.WARNING);
                    }
                }
            }
        });
    }

    public void startServer(int delay) {
        final int dly = delay;
        class ServerStartThread extends Thread {
            // This method is called when the thread runs
             public void run() {
                try {
                    Thread.sleep(dly * 1000);
                } catch (InterruptedException ie) { }
                startServer();
                restarting = false;
            }
        }
        new ServerStartThread().start();
    }

    /**
     * Tells the Minecraft server to stop.
     */
    public void stopServer() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                /* @TODO
                TaskMonitor taskMonitor = getApplication().getContext().getTaskMonitor();
                if ((taskMonitor.getForegroundTask() != null) && taskMonitor.getForegroundTask().isStarted()) {
                    guiLog("Stopping backup first.");
                    taskMonitor.getForegroundTask().cancel(true);
                }
                */
                if (server.isRunning()) {
                    server.stop();
                }
            }
        });
    }

    public void guiLog(String message, LogLevel level) {
        // format message
        String text = getTimeStamp() + " ";
        switch (level) {
            case INFO:
                text += "[INFO]";
                break;
            case WARNING:
                text += "[WARNING]";
                break;
            case SEVERE:
                text += "[SEVERE]";
                break;
            default:
                text += "[INFO]";
        }
        message = text + " MC Server Pail: " + message;
        // put message in console output
        addTextToConsoleOutput(message);

        // log message to file
        File logFile = new File("org.dumptruckman.pail.pail.log");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException ignore) { }
        }
        if (!logFile.canWrite()) {
            return;
        }
        FileWriter logWriter = null;
        try {
            logWriter = new FileWriter(logFile, true);
            logWriter.append(message + System.getProperty("line.separator"));
        } catch (IOException ignore) { }
        finally {
            if (logWriter != null) {
                try {
                    logWriter.close();
                } catch (IOException ignore) { }
            }
        }
    }

    public void guiLog(String message) {
        guiLog(message, LogLevel.INFO);
    }

    /**
     * Adds text to the end of the Console Output box.
     * @param text String of text to add.
     */
    public void addTextToConsoleOutput(final String text) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try
                {
                    ((HTMLEditorKit)consoleOutput.getEditorKit())
                            .insertHTML((HTMLDocument)consoleOutput.getDocument(),
                            consoleOutput.getDocument().getEndPosition().getOffset()-1,
                            parser.parseText(text),
                            1, 0, null);
                } catch ( Exception e ) {
                    e.printStackTrace();
                    System.err.println("Error appending text to console output: " + text);
                }
            }
        });
        scrollText();
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void webLogAdd(String textToAdd) {
        final String text = textToAdd;
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                try
                {
                    String textToAdd = parser.parseText(text);

                    ((HTMLEditorKit)webLog.getEditorKit())
                            .insertHTML((HTMLDocument)webLog.getDocument(),
                            webLog.getDocument().getEndPosition().getOffset()-1,
                            textToAdd, 1, 0, null);
                } catch ( Exception e ) {
                    //e.printStackTrace();
                    System.err.println("Error appending text to web interface log: " + text);
                }
                scrollText();
            }
        });
        
    }

    public void setConsoleOutput(String setText) {
        final String text = setText;
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                consoleOutput.setText("<html><body bgcolor = " + config.display.getBgColor()
                        + "><font color = \"" + config.display.getTextColor()
                        + "\" size = " + config.display.getTextSize() + ">" + text + "</html>");
            }
        });
    }

    public void webLogReplace(String replaceText) {
        final String text = replaceText;
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                webLog.setText("<body bgcolor = " + config.display.getBgColor()
                        + "><font color = \"" + config.display.getTextColor()
                        + "\" size = " + config.display.getTextSize() + ">" + text);
            }
        });
    }

    public void updateConsoleOutputBgColor() {
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                consoleOutput.setText(consoleOutput.getText().replaceFirst(
                        "([\\d,a,b,c,d,e,f,A,B,C,D,E,F]){6}",
                        config.display.getBgColor()));
                webLog.setText(webLog.getText().replaceFirst(
                        "([\\d,a,b,c,d,e,f,A,B,C,D,E,F]){6}",
                        config.display.getBgColor()));
            }
        });
    }

    public void serverStopped() {
        controlSwitcher("OFF");
        if (restarting) {
            startServer(restartDelay);
        }
        if (wantsToQuit) {
            try {
                while(!scheduler.getCurrentlyExecutingJobs().isEmpty()) {
                    System.out.println("Interrupting a job");
                    scheduler.interrupt(scheduler.getCurrentlyExecutingJobs().get(0).getJobDetail().getKey());
                }
                scheduler.shutdown();
            } catch (SchedulerException se) {
                se.printStackTrace();
            }
            exitGui();
        }
    }

    public void serverStarted() {
        controlSwitcher("ON");
    }

    /**
     * Switches Pail components into specific states based on param passed.
     * @param newServerState Typically the state of the server. "ON" or "OFF"
     */
    public void controlSwitcher(String newServerState) {
        final String serverState = newServerState;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                controlState = serverState;
                if (serverState.equals("ON")) {
                    // Switch Pail control to "ON" status
                    startstopButton.setText("Stop");
                    consoleInput.setEnabled(true);
                    submitButton.setEnabled(true);
                    serverStatusLabel.setForeground(Color.BLUE);
                    serverStatusLabel.setText("Server UP");
                    textScrolling = true;
                    saveWorldsButton.setEnabled(true);
                    useProxyCheckBox.setEnabled(false);
                } else if (serverState.equals("OFF")) {
                    // Switch Pail controls to "OFF" status
                    startstopButton.setText("Start");
                    consoleInput.setEnabled(false);
                    submitButton.setEnabled(false);
                    serverStatusLabel.setForeground(Color.red);
                    serverStatusLabel.setText("Server DOWN");
                    textScrolling = false;
                    mouseInConsoleOutput = false;
                    saveWorldsButton.setEnabled(false);
                    useProxyCheckBox.setEnabled(true);
                } else if (serverState.equals("BADCONFIG")) {
                    startstopButton.setEnabled(false);
                } else if (serverState.equals("BACKUP")) {
                    //startstopButton.setEnabled(false);
                    saveWorldsButton.setEnabled(false);
                    backupButton.setEnabled(false);
                    saveThemeButton.setEnabled(false);
                    saveServerConfigButton.setEnabled(false);
                    saveBackupControlButton.setEnabled(false);
                    backupPathField.setEnabled(false);
                    backupPathBrowseButton.setEnabled(false);
                    //backupFileChooser.setEnabled(false);
                } else if (serverState.equals("!BACKUP")) {
                    //startstopButton.setEnabled(true);
                    saveWorldsButton.setEnabled(true);
                    backupButton.setEnabled(true);
                    saveThemeButton.setEnabled(true);
                    saveServerConfigButton.setEnabled(true);
                    saveBackupControlButton.setEnabled(true);
                    backupPathField.setEnabled(true);
                    backupPathBrowseButton.setEnabled(true);
                    //backupFileChooser.setEnabled(true);
                    statusBarJob.setText("");
                    controlSwitcher(stateBeforeBackup);
                }
            }
        });
    }

    public void finishBackup() {
        backup = null;
    }

    public boolean isPropagatingChecks() {
        return propagatingChecks;
    }

    public void setPropagatingChecks(boolean b) {
        propagatingChecks = b;
    }

    public String getControlState() {
        return controlState;
    }

    public String getServerStatus() {
        if (server.isRunning()) {
            return "UP";
        } else {
            return "DOWN";
        }
    }

    public MCServerModel server;
    private boolean textScrolling;
    private boolean mouseInConsoleOutput;
    private List<String> inputHistory;
    private int inputHistoryIndex;
    private String controlState;
    private String stateBeforeBackup;
    private org.dumptruckman.pail.fileexplorer.FileSystemModel backupFileSystem;
    public Config config;
    private Scheduler scheduler;
    public javax.swing.DefaultComboBoxModel customButtonBoxModel1;
    public javax.swing.DefaultComboBoxModel customButtonBoxModel2;
    private boolean restarting;
    private ConsoleParser parser;
    private boolean saving;
    private boolean wantsToQuit;
    private ServerProperties serverProperties;
    private PlayerList playerListModel;
    private boolean isHidden;
    private java.awt.TrayIcon trayIcon;
    private WebInterface webServer;
    private int restartDelay;
    private boolean propagatingChecks;
    private boolean schedulePaused;
    public PailWorker pailWorker;
    public GUIListModel backupFileListModel;
    private String versionNumber;
    public Backup backup;

    public static enum LogLevel { INFO, WARNING, SEVERE }
    public static enum OutputFormat { 
        LINEBREAK, PLAINTEXTCRLF, PLAINTEXTLFCR, PLAINTEXTCR, PLAINTEXTLF
    }

    //Auto created
    //private final Timer messageTimer;
    //private final Timer busyIconTimer;
    //private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;

    private TaskDialog taskDialog;
}
