/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TaskDialog.java
 *
 * Created on May 26, 2011, 10:07:12 PM
 */

package com.dumptruckman.pail.task;

import com.dumptruckman.pail.Pail;
import com.dumptruckman.pail.listmodel.GUIListModel;
import com.dumptruckman.pail.task.event.EventModel;
import net.miginfocom.swing.MigLayout;
import org.quartz.JobKey;
import org.quartz.SchedulerException;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

import static com.dumptruckman.pail.task.event.EventScheduler.scheduleEvent;
import static com.dumptruckman.pail.tools.TimeTools.hmsFromSeconds;

//import org.jdesktop.application.Action;

/**
 *
 * @author dumptruckman
 */
public class TaskDialog extends javax.swing.JDialog {

    /** Creates new form TaskDialog */
    public TaskDialog(Pail pail) {
        super(pail);
        
        this.pail = pail;
        borderTitle = "newTaskBorderTitle.text";
        warningListModel = new GUIListModel<ServerWarning>();
        warningListModel.clear();

        boldFont = new java.awt.Font("Tahoma", java.awt.Font.BOLD, 11);
        normalFont = new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11);
        initComponents();
        fixComponents();

        initDaysOfWeekArray();
        initMonthArray();
        updateTimeSummary();
    }

    public TaskDialog(Pail pail, EventModel editEvent) {
        super(pail);

        this.pail = pail;
        this.editEvent = editEvent;
        borderTitle = "editTaskBorderTitle.text";
        warningListModel = new GUIListModel<ServerWarning>();
        warningListModel.clear();

        boldFont = new java.awt.Font("Tahoma", java.awt.Font.BOLD, 11);
        normalFont = new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11);
        initComponents();
        fixComponents();

        initDaysOfWeekArray();
        initMonthArray();
        parseEditEvent();
        updateTimeSummary();
    }

    public void closeTaskDialog() {
        dispose();
    }

    private void initComponents() {
        //@TODO Fix components sizes
        serverTaskGroup = new javax.swing.ButtonGroup();
        taskEntryPanel = new javax.swing.JPanel();
        taskNameLabel = new javax.swing.JLabel();
        taskNameField = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        secondsLabel = new javax.swing.JLabel();
        secondsField = new javax.swing.JTextField();
        scheduleLabel = new javax.swing.JLabel();
        secondsAgainCheckBox = new javax.swing.JCheckBox();
        secondsAgainField = new javax.swing.JTextField();
        minutesLabel = new javax.swing.JLabel();
        minutesAgainCheckBox = new javax.swing.JCheckBox();
        minutesAgainField = new javax.swing.JTextField();
        minutesField = new javax.swing.JTextField();
        minutesAllCheckBox = new javax.swing.JCheckBox();
        hoursLabel = new javax.swing.JLabel();
        hoursField = new javax.swing.JTextField();
        hoursAgainCheckBox = new javax.swing.JCheckBox();
        hoursAgainField = new javax.swing.JTextField();
        hoursAllCheckBox = new javax.swing.JCheckBox();
        dowLabel = new javax.swing.JLabel();
        dowAllButton = new javax.swing.JToggleButton();
        monthLabel = new javax.swing.JLabel();
        domButton = new javax.swing.JToggleButton();
        domField = new javax.swing.JTextField();
        domAllCheckBox = new javax.swing.JCheckBox();
        sunButton = new javax.swing.JToggleButton();
        monButton = new javax.swing.JToggleButton();
        tueButton = new javax.swing.JToggleButton();
        wedButton = new javax.swing.JToggleButton();
        thuButton = new javax.swing.JToggleButton();
        friButton = new javax.swing.JToggleButton();
        satButton = new javax.swing.JToggleButton();
        janButton = new javax.swing.JToggleButton();
        febButton = new javax.swing.JToggleButton();
        marButton = new javax.swing.JToggleButton();
        aprButton = new javax.swing.JToggleButton();
        mayButton = new javax.swing.JToggleButton();
        junButton = new javax.swing.JToggleButton();
        julButton = new javax.swing.JToggleButton();
        augButton = new javax.swing.JToggleButton();
        sepButton = new javax.swing.JToggleButton();
        octButton = new javax.swing.JToggleButton();
        novButton = new javax.swing.JToggleButton();
        decButton = new javax.swing.JToggleButton();
        monthAllButton = new javax.swing.JToggleButton();
        startServerRadio = new javax.swing.JRadioButton();
        sendCommandRadio = new javax.swing.JRadioButton();
        sendCommandField = new javax.swing.JTextField();
        stopServerRadio = new javax.swing.JRadioButton();
        restartServerRadio = new javax.swing.JRadioButton();
        backupRadio = new javax.swing.JRadioButton();
        remainDownLabel = new javax.swing.JLabel();
        remainDownField = new javax.swing.JTextField();
        createButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        serverWarningLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        warningList = new javax.swing.JList();
        warningAddButton = new javax.swing.JButton();
        warningRemoveButton = new javax.swing.JButton();
        saveWorldsRadio = new javax.swing.JRadioButton();
        warningEditButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        timeSummaryField = new javax.swing.JTextArea();
        taskIsCustomButtonCheckBox = new javax.swing.JCheckBox();

        ResourceBundle lang = ResourceBundle.getBundle("TaskDialog");

        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        this.setName("taskDialog");
        this.setResizable(false);
        //this.setLayout(new MigLayout("fill", "0[]0", "0[]0"));
        {
            taskEntryPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(lang.getString(borderTitle)));
            taskEntryPanel.setName("taskEntryPanel");
            //taskEntryPanel.setLayout(new MigLayout("fill", "0[]0", "0[]0"));
            //this.add(taskEntryPanel, "gr");
            {
                taskNameLabel.setText(lang.getString("taskNameLabel.text"));
                taskNameLabel.setName("taskNameLabel");
                //taskEntryPanel.add(taskNameLabel, "growx 0");

                taskNameField.setText(lang.getString("taskNameField.text"));
                taskNameField.setToolTipText(lang.getString("taskNameField.toolTipText"));
                taskNameField.setName("taskNameField");
                //taskEntryPanel.add(taskNameField, "growx");
            }
        }

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator1.setName("jSeparator1");

        secondsLabel.setText(lang.getString("secondsLabel.text"));
        secondsLabel.setName("secondsLabel");

        secondsField.setText(lang.getString("secondsField.text"));
        secondsField.setToolTipText(lang.getString("secondsField.toolTipText"));
        secondsField.setInputVerifier(new com.dumptruckman.pail.tools.RegexVerifier("^([0-5]?\\d(,\\s?[0-5]?\\d){0,59}|[0-5]?\\d-[0-5]?\\d)$"));
        secondsField.setName("secondsField");
        secondsField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                secondsFieldFocusLost(evt);
            }
        });
        secondsField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                secondsFieldKeyTyped(evt);
            }
        });

        scheduleLabel.setText(lang.getString("scheduleLabel.text")); // NOI18N
        scheduleLabel.setName("scheduleLabel"); // NOI18N

        secondsAgainCheckBox.setText(lang.getString("secondsAgainCheckBox.text")); // NOI18N
        secondsAgainCheckBox.setToolTipText(lang.getString("secondsAgainCheckBox.toolTipText")); // NOI18N
        secondsAgainCheckBox.setName("secondsAgainCheckBox"); // NOI18N
        secondsAgainCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                secondsAgainCheckBoxActionPerformed(evt);
            }
        });

        secondsAgainField.setText(lang.getString("secondsAgainField.text")); // NOI18N
        secondsAgainField.setToolTipText(lang.getString("secondsAgainField.toolTipText")); // NOI18N
        secondsAgainField.setEnabled(false);
        secondsAgainField.setInputVerifier(new com.dumptruckman.pail.tools.RegexVerifier("^[0-5]?\\d$"));
        secondsAgainField.setName("secondsAgainField"); // NOI18N
        secondsAgainField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                secondsAgainFieldFocusLost(evt);
            }
        });

        minutesLabel.setText(lang.getString("minutesLabel.text")); // NOI18N
        minutesLabel.setName("minutesLabel"); // NOI18N

        minutesAgainCheckBox.setText(lang.getString("minutesAgainCheckBox.text")); // NOI18N
        minutesAgainCheckBox.setToolTipText(lang.getString("minutesAgainCheckBox.toolTipText")); // NOI18N
        minutesAgainCheckBox.setName("minutesAgainCheckBox"); // NOI18N
        minutesAgainCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minutesAgainCheckBoxActionPerformed(evt);
            }
        });

        minutesAgainField.setText(lang.getString("minutesAgainField.text")); // NOI18N
        minutesAgainField.setToolTipText(lang.getString("minutesAgainField.toolTipText")); // NOI18N
        minutesAgainField.setEnabled(false);
        minutesAgainField.setInputVerifier(new com.dumptruckman.pail.tools.RegexVerifier("^[0-5]?\\d$"));
        minutesAgainField.setName("minutesAgainField"); // NOI18N
        minutesAgainField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                minutesAgainFieldFocusLost(evt);
            }
        });

        minutesField.setText(lang.getString("minutesField.text")); // NOI18N
        minutesField.setToolTipText(lang.getString("minutesField.toolTipText")); // NOI18N
        minutesField.setInputVerifier(new com.dumptruckman.pail.tools.RegexVerifier("^([0-5]?\\d(,\\s?[0-5]?\\d){0,59}|[0-5]?\\d-[0-5]?\\d)$"));
        minutesField.setName("minutesField"); // NOI18N
        minutesField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                minutesFieldFocusLost(evt);
            }
        });
        minutesField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                minutesFieldKeyTyped(evt);
            }
        });

        minutesAllCheckBox.setText(lang.getString("minutesAllCheckBox.text")); // NOI18N
        minutesAllCheckBox.setToolTipText(lang.getString("minutesAllCheckBox.toolTipText")); // NOI18N
        minutesAllCheckBox.setName("minutesAllCheckBox"); // NOI18N
        minutesAllCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minutesAllCheckBoxActionPerformed(evt);
            }
        });

        hoursLabel.setText(lang.getString("hoursLabel.text")); // NOI18N
        hoursLabel.setName("hoursLabel"); // NOI18N

        hoursField.setText(lang.getString("hoursField.text")); // NOI18N
        hoursField.setToolTipText(lang.getString("hoursField.toolTipText")); // NOI18N
        hoursField.setInputVerifier(new com.dumptruckman.pail.tools.RegexVerifier("^([0-2]?\\d(,\\s?[0-2]?\\d){0,23}|[0-2]?\\d-[0-2]?\\d)$"));
        hoursField.setName("hoursField"); // NOI18N
        hoursField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                hoursFieldFocusLost(evt);
            }
        });
        hoursField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                hoursFieldKeyTyped(evt);
            }
        });

        hoursAgainCheckBox.setText(lang.getString("hoursAgainCheckBox.text")); // NOI18N
        hoursAgainCheckBox.setToolTipText(lang.getString("hoursAgainCheckBox.toolTipText")); // NOI18N
        hoursAgainCheckBox.setName("hoursAgainCheckBox"); // NOI18N
        hoursAgainCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hoursAgainCheckBoxActionPerformed(evt);
            }
        });

        hoursAgainField.setText(lang.getString("hoursAgainField.text")); // NOI18N
        hoursAgainField.setToolTipText(lang.getString("hoursAgainField.toolTipText")); // NOI18N
        hoursAgainField.setEnabled(false);
        hoursAgainField.setInputVerifier(new com.dumptruckman.pail.tools.RegexVerifier("^[0-2]?\\d$"));
        hoursAgainField.setName("hoursAgainField"); // NOI18N
        hoursAgainField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                hoursAgainFieldFocusLost(evt);
            }
        });

        hoursAllCheckBox.setText(lang.getString("hoursAllCheckBox.text")); // NOI18N
        hoursAllCheckBox.setToolTipText(lang.getString("hoursAllCheckBox.toolTipText")); // NOI18N
        hoursAllCheckBox.setName("hoursAllCheckBox"); // NOI18N
        hoursAllCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hoursAllCheckBoxActionPerformed(evt);
            }
        });

        dowLabel.setText(lang.getString("dowLabel.text")); // NOI18N
        dowLabel.setName("dowLabel"); // NOI18N

        dowAllButton.setText(lang.getString("dowAllButton.text"));
        dowAllButton.setToolTipText(lang.getString("dowAllButton.toolTipText"));
        dowAllButton.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(dowAllButton);
        dowAllButton.setName("dowAllButton");
        dowAllButton.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                dowAllButtonStateChanged(evt);
            }
        });
        dowAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dowAllButtonActionPerformed(evt);
            }
        });

        monthLabel.setText(lang.getString("monthLabel.text"));
        monthLabel.setName("monthLabel");

        domButton.setFont(boldFont);
        domButton.setSelected(true);
        domButton.setText(lang.getString("domButton.text"));
        domButton.setToolTipText(lang.getString("domButton.toolTipText"));
        domButton.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(domButton);
        domButton.setName("domButton");
        domButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                domButtonActionPerformed(evt);
            }
        });

        domField.setText(lang.getString("domField.text")); // NOI18N
        domField.setToolTipText(lang.getString("domField.toolTipText")); // NOI18N
        domField.setEnabled(false);
        domField.setInputVerifier(new com.dumptruckman.pail.tools.RegexVerifier("^([0-3]?\\d(,\\s?[0-3]?\\d){0,30}|[0-3]?\\d-[0-3]?\\d)$"));
        domField.setName("domField"); // NOI18N

        domAllCheckBox.setSelected(true);
        domAllCheckBox.setText(lang.getString("domAllCheckBox.text"));
        domAllCheckBox.setToolTipText(lang.getString("domAllCheckBox.toolTipText"));
        domAllCheckBox.setName("domAllCheckBox");
        //domAllCheckBox.putClientProperty("JComponent.sizeVariant", "mini");
        //SwingUtilities.updateComponentTreeUI(domAllCheckBox);
        domAllCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                domAllCheckBoxActionPerformed(evt);
            }
        });

        sunButton.setText(lang.getString("sunButton.text"));
        sunButton.setName("sunButton");
        sunButton.putClientProperty("JComponent.sizeVariant", "mini");
        SwingUtilities.updateComponentTreeUI(sunButton);
        sunButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sunButtonActionPerformed(evt);
            }
        });

        monButton.setText(lang.getString("monButton.text"));
        monButton.setName("monButton"); // NOI18N
        monButton.putClientProperty("JComponent.sizeVariant", "mini");
        SwingUtilities.updateComponentTreeUI(monButton);
        monButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                monButtonActionPerformed(evt);
            }
        });

        tueButton.setText(lang.getString("tueButton.text"));
        tueButton.setName("tueButton");
        tueButton.putClientProperty("JComponent.sizeVariant", "mini");
        SwingUtilities.updateComponentTreeUI(tueButton);
        tueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tueButtonActionPerformed(evt);
            }
        });

        wedButton.setText(lang.getString("wedButton.text"));
        wedButton.setName("wedButton");
        wedButton.putClientProperty("JComponent.sizeVariant", "mini");
        SwingUtilities.updateComponentTreeUI(wedButton);
        wedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wedButtonActionPerformed(evt);
            }
        });

        thuButton.setText(lang.getString("thuButton.text"));
        thuButton.setName("thuButton");
        thuButton.putClientProperty("JComponent.sizeVariant", "mini");
        SwingUtilities.updateComponentTreeUI(thuButton);
        thuButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                thuButtonActionPerformed(evt);
            }
        });

        friButton.setText(lang.getString("friButton.text"));
        friButton.setName("friButton");
        friButton.putClientProperty("JComponent.sizeVariant", "mini");
        SwingUtilities.updateComponentTreeUI(friButton);
        friButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                friButtonActionPerformed(evt);
            }
        });

        satButton.setText(lang.getString("satButton.text"));
        satButton.setName("satButton");
        satButton.putClientProperty("JComponent.sizeVariant", "mini");
        SwingUtilities.updateComponentTreeUI(satButton);
        satButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                satButtonActionPerformed(evt);
            }
        });

        janButton.setFont(boldFont); // NOI18N
        janButton.setSelected(true);
        janButton.setText(lang.getString("janButton.text")); // NOI18N
        janButton.putClientProperty("JComponent.sizeVariant", "mini");
        SwingUtilities.updateComponentTreeUI(janButton);
        janButton.setName("janButton"); // NOI18N
        janButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                janButtonActionPerformed(evt);
            }
        });

        febButton.setFont(boldFont); // NOI18N
        febButton.setSelected(true);
        febButton.setText(lang.getString("febButton.text")); // NOI18N
        febButton.putClientProperty("JComponent.sizeVariant", "mini");
        SwingUtilities.updateComponentTreeUI(febButton);
        febButton.setName("febButton"); // NOI18N
        febButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                febButtonActionPerformed(evt);
            }
        });

        marButton.setFont(boldFont); // NOI18N
        marButton.setSelected(true);
        marButton.setText(lang.getString("marButton.text")); // NOI18N
        marButton.putClientProperty("JComponent.sizeVariant", "mini");
        SwingUtilities.updateComponentTreeUI(marButton);
        marButton.setName("marButton"); // NOI18N
        marButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                marButtonActionPerformed(evt);
            }
        });

        aprButton.setFont(boldFont); // NOI18N
        aprButton.setSelected(true);
        aprButton.setText(lang.getString("aprButton.text")); // NOI18N
        aprButton.putClientProperty("JComponent.sizeVariant", "mini");
        SwingUtilities.updateComponentTreeUI(aprButton);
        aprButton.setName("aprButton"); // NOI18N
        aprButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aprButtonActionPerformed(evt);
            }
        });

        mayButton.setFont(boldFont); // NOI18N
        mayButton.setSelected(true);
        mayButton.setText(lang.getString("mayButton.text")); // NOI18N
        mayButton.putClientProperty("JComponent.sizeVariant", "mini");
        SwingUtilities.updateComponentTreeUI(mayButton);
        mayButton.setName("mayButton"); // NOI18N
        mayButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mayButtonActionPerformed(evt);
            }
        });

        junButton.setFont(boldFont); // NOI18N
        junButton.setSelected(true);
        junButton.setText(lang.getString("junButton.text")); // NOI18N
        junButton.putClientProperty("JComponent.sizeVariant", "mini");
        SwingUtilities.updateComponentTreeUI(junButton);
        junButton.setName("junButton"); // NOI18N
        junButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                junButtonActionPerformed(evt);
            }
        });

        julButton.setFont(boldFont); // NOI18N
        julButton.setSelected(true);
        julButton.setText(lang.getString("julButton.text")); // NOI18N
        julButton.putClientProperty("JComponent.sizeVariant", "mini");
        SwingUtilities.updateComponentTreeUI(julButton);
        julButton.setName("julButton"); // NOI18N
        julButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                julButtonActionPerformed(evt);
            }
        });

        augButton.setFont(boldFont); // NOI18N
        augButton.setSelected(true);
        augButton.setText(lang.getString("augButton.text")); // NOI18N
        augButton.putClientProperty("JComponent.sizeVariant", "mini");
        SwingUtilities.updateComponentTreeUI(augButton);
        augButton.setName("augButton"); // NOI18N
        augButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                augButtonActionPerformed(evt);
            }
        });

        sepButton.setFont(boldFont); // NOI18N
        sepButton.setSelected(true);
        sepButton.setText(lang.getString("sepButton.text")); // NOI18N
        sepButton.putClientProperty("JComponent.sizeVariant", "mini");
        SwingUtilities.updateComponentTreeUI(sepButton);
        sepButton.setName("sepButton"); // NOI18N
        sepButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sepButtonActionPerformed(evt);
            }
        });

        octButton.setFont(boldFont); // NOI18N
        octButton.setSelected(true);
        octButton.setText(lang.getString("octButton.text")); // NOI18N
        octButton.putClientProperty("JComponent.sizeVariant", "mini");
        SwingUtilities.updateComponentTreeUI(octButton);
        octButton.setName("octButton"); // NOI18N
        octButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                octButtonActionPerformed(evt);
            }
        });

        novButton.setFont(boldFont); // NOI18N
        novButton.setSelected(true);
        novButton.setText(lang.getString("novButton.text")); // NOI18N
        novButton.putClientProperty("JComponent.sizeVariant", "mini");
        SwingUtilities.updateComponentTreeUI(novButton);
        novButton.setName("novButton"); // NOI18N
        novButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                novButtonActionPerformed(evt);
            }
        });

        decButton.setFont(boldFont); // NOI18N
        decButton.setSelected(true);
        decButton.setText(lang.getString("decButton.text")); // NOI18N
        decButton.putClientProperty("JComponent.sizeVariant", "mini");
        SwingUtilities.updateComponentTreeUI(decButton);
        decButton.setName("decButton"); // NOI18N
        decButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                decButtonActionPerformed(evt);
            }
        });

        monthAllButton.setFont(boldFont); // NOI18N
        monthAllButton.setSelected(true);
        monthAllButton.setText(lang.getString("monthAllButton.text")); // NOI18N
        monthAllButton.setToolTipText(lang.getString("monthAllButton.toolTipText")); // NOI18N
        monthAllButton.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(monthAllButton);
        monthAllButton.setName("monthAllButton"); // NOI18N
        monthAllButton.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                monthAllButtonStateChanged(evt);
            }
        });
        monthAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                monthAllButtonActionPerformed(evt);
            }
        });

        serverTaskGroup.add(startServerRadio);
        startServerRadio.setText(lang.getString("startServerRadio.text")); // NOI18N
        startServerRadio.setToolTipText(lang.getString("startServerRadio.toolTipText")); // NOI18N
        startServerRadio.setName("startServerRadio"); // NOI18N
        startServerRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startServerRadioActionPerformed(evt);
            }
        });

        serverTaskGroup.add(sendCommandRadio);
        sendCommandRadio.setText(lang.getString("sendCommandRadio.text")); // NOI18N
        sendCommandRadio.setToolTipText(lang.getString("sendCommandRadio.toolTipText")); // NOI18N
        sendCommandRadio.setName("sendCommandRadio"); // NOI18N
        sendCommandRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendCommandRadioActionPerformed(evt);
            }
        });

        sendCommandField.setText(lang.getString("sendCommandField.text")); // NOI18N
        sendCommandField.setEnabled(false);
        sendCommandField.setName("sendCommandField"); // NOI18N

        serverTaskGroup.add(stopServerRadio);
        stopServerRadio.setText(lang.getString("stopServerRadio.text")); // NOI18N
        stopServerRadio.setToolTipText(lang.getString("stopServerRadio.toolTipText")); // NOI18N
        stopServerRadio.setName("stopServerRadio"); // NOI18N
        stopServerRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopServerRadioActionPerformed(evt);
            }
        });

        serverTaskGroup.add(restartServerRadio);
        restartServerRadio.setText(lang.getString("restartServerRadio.text")); // NOI18N
        restartServerRadio.setToolTipText(lang.getString("restartServerRadio.toolTipText")); // NOI18N
        restartServerRadio.setName("restartServerRadio"); // NOI18N
        restartServerRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                restartServerRadioActionPerformed(evt);
            }
        });

        serverTaskGroup.add(backupRadio);
        backupRadio.setText(lang.getString("backupRadio.text")); // NOI18N
        backupRadio.setToolTipText(lang.getString("backupRadio.toolTipText")); // NOI18N
        backupRadio.setName("backupRadio"); // NOI18N
        backupRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backupRadioActionPerformed(evt);
            }
        });

        remainDownLabel.setText(lang.getString("remainDownLabel.text")); // NOI18N
        remainDownLabel.setEnabled(false);
        remainDownLabel.setName("remainDownLabel"); // NOI18N

        remainDownField.setText(lang.getString("remainDownField.text")); // NOI18N
        remainDownField.setToolTipText(lang.getString("remainDownField.toolTipText")); // NOI18N
        remainDownField.setEnabled(false);
        remainDownField.setInputVerifier(new com.dumptruckman.pail.tools.RegexVerifier("^(\\d{1,2}\\s?h)?\\s?(\\d{1,2}\\s?m)?\\s?(\\d{1,2}\\s?s)?$"));
        remainDownField.setName("remainDownField"); // NOI18N

        createButton.setText(lang.getString("createButton.text")); // NOI18N
        createButton.setToolTipText(lang.getString("createButton.toolTipText")); // NOI18N
        createButton.setEnabled(false);
        createButton.setMargin(new java.awt.Insets(2, 5, 2, 5));
        createButton.setName("createButton"); // NOI18N
        createButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createButtonActionPerformed(evt);
            }
        });

        cancelButton.setText(lang.getString("cancelButton.text")); // NOI18N
        cancelButton.setToolTipText(lang.getString("cancelButton.toolTipText")); // NOI18N
        cancelButton.setMargin(new java.awt.Insets(2, 5, 2, 5));
        cancelButton.setName("cancelButton"); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        serverWarningLabel.setText(lang.getString("serverWarningLabel.text"));
        serverWarningLabel.setName("serverWarningLabel");

        jScrollPane1.setName("consoleOutScrollPane");

        warningList.setModel(warningListModel);
        warningList.setToolTipText(lang.getString("warningList.toolTipText"));
        warningList.setEnabled(false);
        warningList.setName("warningList"); // NOI18N
        warningList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                warningListKeyTyped(evt);
            }
        });
        jScrollPane1.setViewportView(warningList);

        warningAddButton.setText(lang.getString("warningAddButton.text")); // NOI18N
        warningAddButton.setToolTipText(lang.getString("warningAddButton.toolTipText")); // NOI18N
        warningAddButton.setEnabled(false);
        warningAddButton.setMargin(new java.awt.Insets(2, 5, 2, 5));
        warningAddButton.setName("warningAddButton"); // NOI18N
        warningAddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                warningAddButtonActionPerformed(evt);
            }
        });

        warningRemoveButton.setText(lang.getString("warningRemoveButton.text")); // NOI18N
        warningRemoveButton.setToolTipText(lang.getString("warningRemoveButton.toolTipText")); // NOI18N
        warningRemoveButton.setEnabled(false);
        warningRemoveButton.setMargin(new java.awt.Insets(2, 5, 2, 5));
        warningRemoveButton.setName("warningRemoveButton"); // NOI18N
        warningRemoveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                warningRemoveButtonActionPerformed(evt);
            }
        });

        serverTaskGroup.add(saveWorldsRadio);
        saveWorldsRadio.setText(lang.getString("saveWorldsRadio.text")); // NOI18N
        saveWorldsRadio.setToolTipText(lang.getString("saveWorldsRadio.toolTipText")); // NOI18N
        saveWorldsRadio.setName("saveWorldsRadio"); // NOI18N
        saveWorldsRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveWorldsRadioActionPerformed(evt);
            }
        });

        warningEditButton.setText(lang.getString("warningEditButton.text")); // NOI18N
        warningEditButton.setEnabled(false);
        warningEditButton.setMargin(new java.awt.Insets(2, 5, 2, 5));
        warningEditButton.setName("warningEditButton"); // NOI18N
        warningEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                warningEditButtonActionPerformed(evt);
            }
        });

        jScrollPane2.setName("playerListScrollPane"); // NOI18N

        timeSummaryField.setColumns(20);
        timeSummaryField.setEditable(false);
        timeSummaryField.setLineWrap(true);
        timeSummaryField.setRows(5);
        timeSummaryField.setToolTipText(lang.getString("timeSummaryField.toolTipText")); // NOI18N
        timeSummaryField.setName("timeSummaryField"); // NOI18N
        jScrollPane2.setViewportView(timeSummaryField);

        taskIsCustomButtonCheckBox.setText(lang.getString("taskIsCustomButtonCheckBox.text")); // NOI18N
        taskIsCustomButtonCheckBox.setToolTipText(lang.getString("taskIsCustomButtonCheckBox.toolTipText")); // NOI18N
        taskIsCustomButtonCheckBox.setName("taskIsCustomButtonCheckBox"); // NOI18N
        taskIsCustomButtonCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                taskIsCustomButtonCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout taskEntryPanelLayout = new javax.swing.GroupLayout(taskEntryPanel);
        taskEntryPanel.setLayout(taskEntryPanelLayout);
        taskEntryPanelLayout.setHorizontalGroup(
            taskEntryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(taskEntryPanelLayout.createSequentialGroup()
                    .addGroup(taskEntryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(taskEntryPanelLayout.createSequentialGroup()
                                    .addComponent(scheduleLabel)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 77, Short.MAX_VALUE)
                                    .addComponent(taskIsCustomButtonCheckBox))
                            .addComponent(hoursLabel)
                            .addGroup(taskEntryPanelLayout.createSequentialGroup()
                                    .addGroup(taskEntryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(secondsLabel)
                                            .addComponent(minutesLabel))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(taskEntryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(taskEntryPanelLayout.createSequentialGroup()
                                                    .addComponent(minutesField, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(minutesAgainCheckBox)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(minutesAgainField, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(minutesAllCheckBox))
                                            .addGroup(taskEntryPanelLayout.createSequentialGroup()
                                                    .addComponent(hoursField, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(hoursAgainCheckBox)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(hoursAgainField, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(hoursAllCheckBox))
                                            .addGroup(taskEntryPanelLayout.createSequentialGroup()
                                                    .addComponent(secondsField, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(secondsAgainCheckBox)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(secondsAgainField, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(taskEntryPanelLayout.createSequentialGroup()
                                    .addComponent(taskNameLabel)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(taskNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(taskEntryPanelLayout.createSequentialGroup()
                                    .addGroup(taskEntryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(taskEntryPanelLayout.createSequentialGroup()
                                                    .addComponent(wedButton, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(thuButton, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(friButton, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addComponent(satButton, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGap(18, 18, 18)
                                    .addGroup(taskEntryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(taskEntryPanelLayout.createSequentialGroup()
                                                    .addComponent(julButton, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(augButton, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(sepButton, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(taskEntryPanelLayout.createSequentialGroup()
                                                    .addComponent(aprButton, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(mayButton, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(junButton, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(taskEntryPanelLayout.createSequentialGroup()
                                                    .addComponent(octButton, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(novButton, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(decButton, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(taskEntryPanelLayout.createSequentialGroup()
                                    .addGroup(taskEntryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(taskEntryPanelLayout.createSequentialGroup()
                                                    .addComponent(sunButton, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(monButton, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(tueButton, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(taskEntryPanelLayout.createSequentialGroup()
                                                    .addComponent(dowLabel)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(dowAllButton, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGap(18, 18, 18)
                                    .addGroup(taskEntryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(taskEntryPanelLayout.createSequentialGroup()
                                                    .addComponent(monthLabel)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(monthAllButton, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(taskEntryPanelLayout.createSequentialGroup()
                                                    .addComponent(janButton, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(febButton, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(marButton, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(taskEntryPanelLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(domButton)
                                    .addGap(6, 6, 6)
                                    .addComponent(domField, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(domAllCheckBox))
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 266, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(3, 3, 3)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(taskEntryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(startServerRadio)
                            .addGroup(taskEntryPanelLayout.createSequentialGroup()
                                    .addComponent(sendCommandRadio)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(sendCommandField, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE))
                            .addComponent(stopServerRadio)
                            .addGroup(taskEntryPanelLayout.createSequentialGroup()
                                    .addComponent(createButton)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 188, Short.MAX_VALUE)
                                    .addComponent(cancelButton))
                            .addGroup(taskEntryPanelLayout.createSequentialGroup()
                                    .addComponent(restartServerRadio)
                                    .addGap(18, 18, 18)
                                    .addComponent(remainDownLabel)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(remainDownField, javax.swing.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
                            .addGroup(taskEntryPanelLayout.createSequentialGroup()
                                    .addComponent(serverWarningLabel)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(warningAddButton)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(warningEditButton)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 69, Short.MAX_VALUE)
                                    .addComponent(warningRemoveButton))
                            .addComponent(saveWorldsRadio)
                            .addComponent(backupRadio))
                    .addContainerGap())
        );
        taskEntryPanelLayout.setVerticalGroup(
            taskEntryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(taskEntryPanelLayout.createSequentialGroup()
                .addGroup(taskEntryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(taskEntryPanelLayout.createSequentialGroup()
                        .addGroup(taskEntryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(taskNameLabel)
                            .addComponent(taskNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(taskEntryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(scheduleLabel)
                            .addComponent(taskIsCustomButtonCheckBox))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(taskEntryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(secondsLabel)
                            .addComponent(secondsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(secondsAgainCheckBox)
                            .addComponent(secondsAgainField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(taskEntryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(minutesLabel)
                            .addComponent(minutesAgainCheckBox)
                            .addComponent(minutesAgainField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(minutesField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(minutesAllCheckBox))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(taskEntryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(hoursLabel)
                            .addComponent(hoursAgainCheckBox)
                            .addComponent(hoursAgainField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(hoursAllCheckBox)
                            .addComponent(hoursField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(taskEntryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(dowLabel)
                            .addComponent(dowAllButton)
                            .addComponent(monthLabel)
                            .addComponent(monthAllButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(taskEntryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(sunButton)
                            .addComponent(monButton)
                            .addComponent(tueButton)
                            .addComponent(janButton)
                            .addComponent(febButton)
                            .addComponent(marButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(taskEntryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(wedButton)
                            .addComponent(thuButton)
                            .addComponent(friButton)
                            .addComponent(aprButton)
                            .addComponent(mayButton)
                            .addComponent(junButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(taskEntryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(satButton)
                            .addComponent(julButton)
                            .addComponent(augButton)
                            .addComponent(sepButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(taskEntryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(octButton)
                            .addComponent(novButton)
                            .addComponent(decButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(taskEntryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(domField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(domButton)
                            .addComponent(domAllCheckBox))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE))
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE)
                    .addGroup(taskEntryPanelLayout.createSequentialGroup()
                        .addComponent(startServerRadio)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(taskEntryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(sendCommandRadio)
                            .addComponent(sendCommandField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(stopServerRadio)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(taskEntryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(remainDownLabel)
                            .addComponent(remainDownField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(restartServerRadio))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(saveWorldsRadio)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(backupRadio)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 87, Short.MAX_VALUE)
                        .addGroup(taskEntryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(serverWarningLabel)
                            .addComponent(warningAddButton)
                            .addComponent(warningRemoveButton)
                            .addComponent(warningEditButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(taskEntryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(createButton)
                            .addComponent(cancelButton))))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(taskEntryPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(taskEntryPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }

    private void fixComponents() {
    }

    private void updateTimeSummary() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                if (taskIsCustomButtonCheckBox.isSelected()) {
                    timeSummaryField.setText("Task is triggered when it is selected and"
                            + " Go button is pressed on the Main Window Tab");
                } else {
                    String summary = "At ";
                    if (hoursAllCheckBox.isSelected()) {
                        summary += "**";
                    } else {
                        if (hoursAgainCheckBox.isSelected()
                                && hoursAgainCheckBox.isEnabled()) {
                            if (hoursField.getText().contains("-")) {
                                hoursField.setText(hoursField.getText().split("-")[0]);
                            }
                            if (hoursField.getText().contains(",")) {
                                hoursField.setText(hoursField.getText().split(",")[0]);
                            }
                            summary += hoursField.getText();
                            int time = Integer.valueOf(hoursField.getText());
                            int inc = Integer.valueOf(hoursAgainField.getText());
                            while(time < 24) {
                                time += inc;
                                if (time < 24) {
                                    summary += "/" + time;
                                }
                            }
                        } else {
                            summary += hoursField.getText();
                        }
                    }
                    summary += ":";
                    if (minutesAllCheckBox.isSelected()) {
                        summary += "**";
                    } else {
                        if (minutesAgainCheckBox.isSelected()
                                && minutesAgainCheckBox.isEnabled()) {
                            if (minutesField.getText().contains("-")) {
                                minutesField.setText(minutesField.getText().split("-")[0]);
                            }
                            if (minutesField.getText().contains(",")) {
                                minutesField.setText(minutesField.getText().split(",")[0]);
                            }
                            summary += minutesField.getText();
                            int time = Integer.valueOf(minutesField.getText());
                            int inc = Integer.valueOf(minutesAgainField.getText());
                            while (time < 60) {
                                time += inc;
                                if (time < 60) {
                                    summary += "/" + time;
                                }
                            }
                        } else {
                            summary += minutesField.getText();
                        }
                    }
                    summary += ":";
                    if (secondsAgainCheckBox.isSelected()
                            && secondsAgainCheckBox.isEnabled()) {
                        if (secondsField.getText().contains("-")) {
                            secondsField.setText(secondsField.getText().split("-")[0]);
                        }
                        if (secondsField.getText().contains(",")) {
                            secondsField.setText(secondsField.getText().split(",")[0]);
                        }
                        summary += secondsField.getText();
                        int time = Integer.valueOf(secondsField.getText());
                        int inc = Integer.valueOf(secondsAgainField.getText());
                        while (time < 60) {
                            time += inc;
                            if (time < 60) {
                                summary += "/" + time;
                            } else {
                                break;
                            }
                        }
                    } else {
                        summary += secondsField.getText();
                    }
                    summary += " on ";
                    if (domButton.isSelected()) {
                        if (domAllCheckBox.isSelected()) {
                            summary += "every day";
                        } else {
                            summary += "select days";
                        }
                    } else {
                        if (dowAllButton.isSelected()) {
                            summary += "every day";
                        } else {
                            summary += "select days";
                        }
                    }
                    summary += " of ";
                    if (monthAllButton.isSelected()) {
                        summary += "every month.";
                    } else {
                        summary += "select months.";
                    }
                    timeSummaryField.setText(summary);
                }
            }
        });
    }

    private class WarningListCellRenderer extends javax.swing.JTextPane implements javax.swing.ListCellRenderer {
        public WarningListCellRenderer() {
            //setOpaque(true);
            this.setEditorKit(new javax.swing.text.html.HTMLEditorKit());
        }

        @Override public java.awt.Component getListCellRendererComponent(
                javax.swing.JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus)
        {
            setText(value.toString());
            setBackground(isSelected ? java.awt.Color.lightGray : java.awt.Color.white);
            setForeground(isSelected ? java.awt.Color.white : java.awt.Color.black);
            return this;
        }
    }

    private void initDaysOfWeekArray() {
        dayOfWeek = new java.util.ArrayList<javax.swing.JToggleButton>();
        dayOfWeek.add(dowAllButton);
        dayOfWeek.add(sunButton);
        dayOfWeek.add(monButton);
        dayOfWeek.add(tueButton);
        dayOfWeek.add(wedButton);
        dayOfWeek.add(thuButton);
        dayOfWeek.add(friButton);
        dayOfWeek.add(satButton);
    }

    private void initMonthArray() {
        month = new java.util.ArrayList<javax.swing.JToggleButton>();
        month.add(monthAllButton);
        month.add(janButton);
        month.add(febButton);
        month.add(marButton);
        month.add(aprButton);
        month.add(mayButton);
        month.add(junButton);
        month.add(julButton);
        month.add(augButton);
        month.add(sepButton);
        month.add(octButton);
        month.add(novButton);
        month.add(decButton);
    }

    private void dowAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dowAllButtonActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                if (dowAllButton.isSelected()) {
                    for (int i = 1; i < 8; i++) {
                        dayOfWeek.get(i).setSelected(true);
                        boldButton(dayOfWeek.get(i));
                    }
                    //dowAllButton.setText("None");
                    domField.setEnabled(false);
                    domAllCheckBox.setEnabled(false);
                    domButton.setSelected(false);
                } else {
                    for (int i = 1; i < 8; i++) {
                        dayOfWeek.get(i).setSelected(false);
                        boldButton(dayOfWeek.get(i));
                    }
                    //dowAllButton.setText("All");
                    domField.setEnabled(true);
                    domAllCheckBox.setEnabled(true);
                    domButton.setSelected(true);
                }
                boldButton(dowAllButton);
                boldButton(domButton);
            }
        });
    }//GEN-LAST:event_dowAllButtonActionPerformed

    private void sunButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sunButtonActionPerformed
        verifyAllSelected(dayOfWeek);
    }//GEN-LAST:event_sunButtonActionPerformed

    private void monButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_monButtonActionPerformed
        verifyAllSelected(dayOfWeek);
    }//GEN-LAST:event_monButtonActionPerformed

    private void tueButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tueButtonActionPerformed
        verifyAllSelected(dayOfWeek);
    }//GEN-LAST:event_tueButtonActionPerformed

    private void wedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wedButtonActionPerformed
        verifyAllSelected(dayOfWeek);
    }//GEN-LAST:event_wedButtonActionPerformed

    private void thuButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_thuButtonActionPerformed
        verifyAllSelected(dayOfWeek);
    }//GEN-LAST:event_thuButtonActionPerformed

    private void friButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_friButtonActionPerformed
        verifyAllSelected(dayOfWeek);
    }//GEN-LAST:event_friButtonActionPerformed

    private void satButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_satButtonActionPerformed
        verifyAllSelected(dayOfWeek);
    }//GEN-LAST:event_satButtonActionPerformed

    private void monthAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_monthAllButtonActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                if (monthAllButton.isSelected()) {
                    for (int i = 1; i < 13; i++) {
                        month.get(i).setSelected(true);
                        boldButton(month.get(i));
                    }
                    //monthAllButton.setText("None");
                } else {
                    for (int i = 1; i < 13; i++) {
                        month.get(i).setSelected(false);
                        boldButton(month.get(i));
                    }
                   //monthAllButton.setText("All");
                }
                boldButton(monthAllButton);
            }
        });
    }//GEN-LAST:event_monthAllButtonActionPerformed

    private void janButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_janButtonActionPerformed
        verifyAllSelected(month);
    }//GEN-LAST:event_janButtonActionPerformed

    private void febButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_febButtonActionPerformed
        verifyAllSelected(month);
    }//GEN-LAST:event_febButtonActionPerformed

    private void marButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_marButtonActionPerformed
        verifyAllSelected(month);
    }//GEN-LAST:event_marButtonActionPerformed

    private void aprButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aprButtonActionPerformed
        verifyAllSelected(month);
    }//GEN-LAST:event_aprButtonActionPerformed

    private void mayButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mayButtonActionPerformed
        verifyAllSelected(month);
    }//GEN-LAST:event_mayButtonActionPerformed

    private void junButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_junButtonActionPerformed
        verifyAllSelected(month);
    }//GEN-LAST:event_junButtonActionPerformed

    private void julButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_julButtonActionPerformed
        verifyAllSelected(month);
    }//GEN-LAST:event_julButtonActionPerformed

    private void augButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_augButtonActionPerformed
        verifyAllSelected(month);
    }//GEN-LAST:event_augButtonActionPerformed

    private void sepButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sepButtonActionPerformed
        verifyAllSelected(month);
    }//GEN-LAST:event_sepButtonActionPerformed

    private void octButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_octButtonActionPerformed
        verifyAllSelected(month);
    }//GEN-LAST:event_octButtonActionPerformed

    private void novButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_novButtonActionPerformed
        verifyAllSelected(month);
    }//GEN-LAST:event_novButtonActionPerformed

    private void decButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_decButtonActionPerformed
        verifyAllSelected(month);
    }//GEN-LAST:event_decButtonActionPerformed

    private void domButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_domButtonActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                if (domButton.isSelected()) {
                    for (int i = 0; i < 8; i++) {
                        dayOfWeek.get(i).setSelected(false);
                        boldButton(dayOfWeek.get(i));
                    }
                    //dayOfWeek.get(0).setText("All");
                    domAllCheckBox.setEnabled(true);
                    domField.setEnabled(!domAllCheckBox.isSelected());
                } else {
                    domButton.setSelected(true);
                }
                boldButton(domButton);
                boldButton(dowAllButton);
                updateTimeSummary();
            }
        });
    }//GEN-LAST:event_domButtonActionPerformed

    private void domAllCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_domAllCheckBoxActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                domField.setEnabled(!domAllCheckBox.isSelected());
                updateTimeSummary();
            }
        });
    }//GEN-LAST:event_domAllCheckBoxActionPerformed

    private void minutesAllCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minutesAllCheckBoxActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                if (minutesAllCheckBox.isSelected()) {
                    if (javax.swing.JOptionPane.showConfirmDialog(TaskDialog.this,
                                "This option is rather dangerous!\n"
                                + "Please be aware that this means the event will occur EVERY minute of the specified hour(s).\n"
                                + "Are you sure you wish to this event to occure EVERY minute of the specified hour(s)?",
                                "Confirm select ALL minutes",
                                javax.swing.JOptionPane.YES_NO_OPTION) ==
                                javax.swing.JOptionPane.NO_OPTION) {
                        minutesAllCheckBox.setSelected(false);
                    }
                }
                minutesField.setEnabled(!minutesAllCheckBox.isSelected());
                minutesAgainCheckBox.setEnabled(!minutesAllCheckBox.isSelected());
                minutesAgainField.setEnabled(!minutesAllCheckBox.isSelected());
                updateTimeSummary();
            }
        });  
    }//GEN-LAST:event_minutesAllCheckBoxActionPerformed

    private void hoursAllCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hoursAllCheckBoxActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                hoursField.setEnabled(!hoursAllCheckBox.isSelected());
                hoursAgainCheckBox.setEnabled(!hoursAllCheckBox.isSelected());
                hoursAgainField.setEnabled(!hoursAllCheckBox.isSelected());
                updateTimeSummary();
            }
        });
    }//GEN-LAST:event_hoursAllCheckBoxActionPerformed

    private void startServerRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startServerRadioActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                createButton.setEnabled(true);
                sendCommandField.setEnabled(false);
                remainDownLabel.setEnabled(false);
                remainDownField.setEnabled(false);
                warningList.setEnabled(false);
                warningAddButton.setEnabled(false);
                warningEditButton.setEnabled(false);
                warningRemoveButton.setEnabled(false);
                task = "Start Server";
            }
        });
    }//GEN-LAST:event_startServerRadioActionPerformed

    private void sendCommandRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendCommandRadioActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                createButton.setEnabled(true);
                sendCommandField.setEnabled(true);
                remainDownLabel.setEnabled(false);
                remainDownField.setEnabled(false);
                warningList.setEnabled(true);
                warningAddButton.setEnabled(true);
                warningEditButton.setEnabled(true);
                warningRemoveButton.setEnabled(true);
                task = "Send Command";
            }
        });
    }//GEN-LAST:event_sendCommandRadioActionPerformed

    private void stopServerRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopServerRadioActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                createButton.setEnabled(true);
                sendCommandField.setEnabled(false);
                remainDownLabel.setEnabled(false);
                remainDownField.setEnabled(false);
                warningList.setEnabled(true);
                warningAddButton.setEnabled(true);
                warningEditButton.setEnabled(true);
                warningRemoveButton.setEnabled(true);
                task = "Stop Server";
            }
        });
    }//GEN-LAST:event_stopServerRadioActionPerformed

    private void restartServerRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_restartServerRadioActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                createButton.setEnabled(true);
                sendCommandField.setEnabled(false);
                remainDownLabel.setEnabled(true);
                remainDownField.setEnabled(true);
                warningList.setEnabled(true);
                warningAddButton.setEnabled(true);
                warningEditButton.setEnabled(true);
                warningRemoveButton.setEnabled(true);
                task = "Restart Server";
            }
        });
    }//GEN-LAST:event_restartServerRadioActionPerformed

    private void backupRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backupRadioActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                createButton.setEnabled(true);
                sendCommandField.setEnabled(false);
                remainDownLabel.setEnabled(false);
                remainDownField.setEnabled(false);
                warningList.setEnabled(true);
                warningAddButton.setEnabled(true);
                warningEditButton.setEnabled(true);
                warningRemoveButton.setEnabled(true);
                task = "Backup";
            }
        });
    }//GEN-LAST:event_backupRadioActionPerformed

    private void warningAddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_warningAddButtonActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                warningDialog = new ServerWarningDialog(
                        pail, warningListModel/*, serverWarningList*/);
                warningDialog.setLocationRelativeTo(pail);
                warningDialog.setVisible(true);
            }
        });
    }//GEN-LAST:event_warningAddButtonActionPerformed

    private void warningRemoveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_warningRemoveButtonActionPerformed
        removeWarningListEntry();
    }//GEN-LAST:event_warningRemoveButtonActionPerformed

    public void removeWarningListEntry() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                ServerWarning warning = (ServerWarning)warningList.getSelectedValue();
                if (warning != null) {
                    if (javax.swing.JOptionPane.showConfirmDialog(TaskDialog.this,
                            "Are you sure you wish to remove this warning?\n",
                            "Remove warning message",
                            javax.swing.JOptionPane.YES_NO_OPTION) ==
                            javax.swing.JOptionPane.YES_OPTION) {
                        warningListModel.removeElement(warning);
                    }
                }
            }
        });
    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                if (javax.swing.JOptionPane.showConfirmDialog(TaskDialog.this,
                        "Are you sure you wish to stop editing this event?\n"
                        + "Any saved changes will be lost!",
                        "Cancel task entry",
                        javax.swing.JOptionPane.YES_NO_OPTION) ==
                        javax.swing.JOptionPane.YES_OPTION) {
                    closeTaskDialog();
                }
            }
        });
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void createButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createButtonActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                if (taskNameField.getText().isEmpty()) {
                    taskNameField.requestFocus();
                    return;
                }

                // Check and build the cron expression
                String second = "", minute = "", hour = "", dow = "", mon = "",
                        dom = "";

                second = secondsField.getText();
                if (secondsAgainCheckBox.isSelected() && secondsAgainCheckBox.isEnabled()) {
                    if (secondsAgainField.getText().isEmpty()) {
                        secondsAgainField.requestFocus();
                        return;
                    }
                    second = secondsField.getText() + "/" + secondsAgainField.getText();
                }

                if (minutesAllCheckBox.isSelected()) {  // Cron minutes
                    minute = "*";
                } else {
                    minute = minutesField.getText();
                    if (minutesAgainCheckBox.isSelected() && minutesAgainCheckBox.isEnabled()) {
                        if (minutesAgainField.getText().isEmpty()) {
                            minutesAgainField.requestFocus();
                            return;
                        }
                        minute = minutesField.getText() + "/" + minutesAgainField.getText();
                    }
                }
                if (hoursAllCheckBox.isSelected()) {  // Cron hours
                    hour = "*";
                } else {
                    hour = hoursField.getText();
                    if (hoursAgainCheckBox.isSelected() && hoursAgainCheckBox.isEnabled()) {
                        if (hoursAgainField.getText().isEmpty()) {
                            hoursAgainField.requestFocus();
                            return;
                        }
                        hour = hoursField.getText() + "/" + hoursAgainField.getText();
                    }
                }
                if (domButton.isSelected()) {  // Cron day of month
                    if (domAllCheckBox.isSelected()) {
                        dom = "*";
                    } else {
                        dom = domField.getText();
                    }
                } else {
                    dom = "?";
                }
                if (monthAllButton.getText().equals("None")) {  // Cron month
                    mon = "*";
                } else {
                    for (int i = 1; i < month.size(); i++) {
                        if (month.get(i).isSelected()) {
                            if (!mon.isEmpty()) {
                                mon += ",";
                            }
                            mon += Integer.toString(i);
                        }
                    }
                }
                if (domButton.isSelected()) {  // Cron day of week
                    dow = "?";
                } else {
                    if (dowAllButton.getText().equals("None")) {
                        dow = "*";
                    } else {
                        for (int i = 1; i < dayOfWeek.size(); i++) {
                            if (dayOfWeek.get(i).isSelected()) {
                                if (!dow.isEmpty()) {
                                    dow += ",";
                                }
                                dow += Integer.toString(i);
                            }
                        }
                    }
                }
                String cronex = second + " " + minute + " " + hour + " " + dom + " "
                        + mon + " " + dow;

                EventModel event = new EventModel();
                event.setCronEx(cronex);
                event.setName(taskNameField.getText());
                event.setTask(task);
                event.setCustomButton(taskIsCustomButtonCheckBox.isSelected());
                java.util.List<String> params = new java.util.ArrayList<String>();
                if (task.equals("Send Command")) {
                    params.add(sendCommandField.getText());
                } else if (task.equals("Restart Server")) {
                    if (!remainDownField.getInputVerifier().verify(remainDownField)) {
                        remainDownField.requestFocus();
                        return;
                    }
                    int seconds = 0, minutes = 0, hours = 0;
                    String time = remainDownField.getText();
                    if (time.contains("h")) {
                        hours = Integer.parseInt(time.split("h")[0].replaceAll(" ", ""));
                        if (time.contains("m") || time.contains("s")) {
                            time = time.split("h")[1];
                        }
                    }
                    if (time.contains("m")) {
                        minutes = Integer.parseInt(time.split("m")[0].replaceAll(" ", ""));
                        if (time.contains("s")) {
                            time = time.split("m")[1];
                        }
                    }
                    if (time.contains("s")) {
                        seconds = Integer.parseInt(time.split("s")[0].replaceAll(" ", ""));
                    }
                    params.add(Integer.toString((hours * 3600) + (minutes * 60) + seconds));
                }
                event.setParams(params);
                event.setWarningList(warningListModel);
                if (editEvent != null) {
                    pail.config.schedule.getEvents().removeElement(editEvent);
                    pail.customButtonBoxModel1.removeElement(editEvent.getName());
                    pail.customButtonBoxModel2.removeElement(editEvent.getName());
                    try {
                        pail.getScheduler().deleteJob(JobKey.jobKey(editEvent.getName()));
                    } catch (SchedulerException se) {
                        System.out.println("Error removing old task");
                    }
                }
                pail.config.schedule.getEvents().add(event);
                if (taskIsCustomButtonCheckBox.isSelected()) {
                    pail.customButtonBoxModel1.addElement(event.getName());
                    pail.customButtonBoxModel2.addElement(event.getName());
                } else {
                    scheduleEvent(event, pail);
                }
                //config.save();
                //configLoader.save();
                closeTaskDialog();
            }
        });
    }//GEN-LAST:event_createButtonActionPerformed

    private void saveWorldsRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveWorldsRadioActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                createButton.setEnabled(true);
                sendCommandField.setEnabled(false);
                remainDownLabel.setEnabled(false);
                remainDownField.setEnabled(false);
                warningList.setEnabled(true);
                warningAddButton.setEnabled(true);
                warningEditButton.setEnabled(true);
                warningRemoveButton.setEnabled(true);
                task = "Save Worlds";
            }
        });
    }//GEN-LAST:event_saveWorldsRadioActionPerformed

    private void warningEditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_warningEditButtonActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                try {
                    ServerWarning warning = (ServerWarning)warningList.getSelectedValue();
                    if (warning != null) {
                        warningDialog = new ServerWarningDialog(
                                pail, warningListModel, /*serverWarningList,*/
                                warning);
                        warningDialog.setLocationRelativeTo(pail);
                        warningDialog.setVisible(true);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {}
            }
        });
    }//GEN-LAST:event_warningEditButtonActionPerformed

    private void secondsAgainCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_secondsAgainCheckBoxActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                if (secondsAgainField.getText().isEmpty()) {
                    secondsAgainField.setText("3");
                }
                secondsAgainField.setEnabled(secondsAgainCheckBox.isSelected());
                updateTimeSummary();
            }
        });
    }//GEN-LAST:event_secondsAgainCheckBoxActionPerformed

    private void minutesAgainCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minutesAgainCheckBoxActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                if (minutesAgainField.getText().isEmpty()) {
                    minutesAgainField.setText("3");
                }
                minutesAgainField.setEnabled(minutesAgainCheckBox.isSelected());
                updateTimeSummary();
            }
        });
    }//GEN-LAST:event_minutesAgainCheckBoxActionPerformed

    private void hoursAgainCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hoursAgainCheckBoxActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                if (hoursAgainField.getText().isEmpty()) {
                    hoursAgainField.setText("3");
                }
                hoursAgainField.setEnabled(hoursAgainCheckBox.isSelected());
                updateTimeSummary();
            }
        });
    }//GEN-LAST:event_hoursAgainCheckBoxActionPerformed

    private void secondsFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_secondsFieldKeyTyped
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
               if (secondsField.getText().contains(",") || secondsField.getText().contains("-")) {
                    secondsAgainCheckBox.setEnabled(false);
                } else {
                    secondsAgainCheckBox.setEnabled(true);
                } 
            }
        });
    }//GEN-LAST:event_secondsFieldKeyTyped

    private void minutesFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_minutesFieldKeyTyped
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                if (minutesField.getText().contains(",") || minutesField.getText().contains("-")) {
                    minutesAgainCheckBox.setEnabled(false);
                } else {
                    minutesAgainCheckBox.setEnabled(true);
                }
            }
        });
    }//GEN-LAST:event_minutesFieldKeyTyped

    private void hoursFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_hoursFieldKeyTyped
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                if (hoursField.getText().contains(",") || hoursField.getText().contains("-")) {
                    hoursAgainCheckBox.setEnabled(false);
                } else {
                    hoursAgainCheckBox.setEnabled(true);
                }
            }
        });
    }//GEN-LAST:event_hoursFieldKeyTyped

    private void secondsFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_secondsFieldFocusLost
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                if (secondsField.getText().isEmpty()) {
                    secondsField.setText("0");
                }
                updateTimeSummary();
            }
        });
    }//GEN-LAST:event_secondsFieldFocusLost

    private void minutesFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_minutesFieldFocusLost
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                if (minutesField.getText().isEmpty()) {
                    minutesField.setText("0");
                }
                updateTimeSummary();
            }
        });
    }//GEN-LAST:event_minutesFieldFocusLost

    private void hoursFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_hoursFieldFocusLost
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                if (hoursField.getText().isEmpty()) {
                    hoursField.setText("0");
                }
                updateTimeSummary();
            }
        });
    }//GEN-LAST:event_hoursFieldFocusLost

    private void secondsAgainFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_secondsAgainFieldFocusLost
        if (secondsAgainField.getText().isEmpty()) {
            secondsAgainField.setText("3");
        }
        updateTimeSummary();
    }//GEN-LAST:event_secondsAgainFieldFocusLost

    private void minutesAgainFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_minutesAgainFieldFocusLost
        if (minutesAgainField.getText().isEmpty()) {
            minutesAgainField.setText("3");
        }
        updateTimeSummary();
    }//GEN-LAST:event_minutesAgainFieldFocusLost

    private void hoursAgainFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_hoursAgainFieldFocusLost
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                if (hoursAgainField.getText().isEmpty()) {
                    hoursAgainField.setText("3");
                }
                updateTimeSummary();
            }
        });
    }//GEN-LAST:event_hoursAgainFieldFocusLost

    private void monthAllButtonStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_monthAllButtonStateChanged
        updateTimeSummary();
    }//GEN-LAST:event_monthAllButtonStateChanged

    private void dowAllButtonStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_dowAllButtonStateChanged
        updateTimeSummary();
    }//GEN-LAST:event_dowAllButtonStateChanged

    private void taskIsCustomButtonCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_taskIsCustomButtonCheckBoxActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                enableTimingSettings(!taskIsCustomButtonCheckBox.isSelected());
                updateTimeSummary();
            }
        });
    }//GEN-LAST:event_taskIsCustomButtonCheckBoxActionPerformed

    private void warningListKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_warningListKeyTyped
        if (evt.getKeyChar() == java.awt.event.KeyEvent.VK_DELETE) {
            removeWarningListEntry();
        }
    }//GEN-LAST:event_warningListKeyTyped

    private void enableTimingSettings(boolean bool) {
        final boolean b = bool;
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                secondsField.setEnabled(b);
                secondsAgainCheckBox.setEnabled(b);
                secondsAgainField.setEnabled(b);
                minutesField.setEnabled(b);
                minutesAgainCheckBox.setEnabled(b);
                minutesAgainField.setEnabled(b);
                minutesAllCheckBox.setEnabled(b);
                hoursField.setEnabled(b);
                hoursAgainCheckBox.setEnabled(b);
                hoursAgainField.setEnabled(b);
                hoursAllCheckBox.setEnabled(b);
                for (int i = 0; i < dayOfWeek.size(); i++) {
                    dayOfWeek.get(i).setEnabled(b);
                }
                for (int i = 0; i < month.size(); i++) {
                    month.get(i).setEnabled(b);
                }
                domButton.setEnabled(b);
                domField.setEnabled(b);
                domAllCheckBox.setEnabled(b);
            }
        });
    }

    private void boldButton(javax.swing.JToggleButton but) {
        final javax.swing.JToggleButton button = but;
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                if (button.isSelected()) {
                    button.setFont(boldFont);
                } else {
                    button.setFont(normalFont);
                }
            }
        });
    }

    private void parseEditEvent() {
        //try {
            //SwingUtilities.invokeAndWait(new Runnable() {
                //@Override public void run() {
                    if (editEvent.isCustomButton()) {
                        taskIsCustomButtonCheckBox.setSelected(true);
                        enableTimingSettings(false);
                    }
                    taskNameField.setText(editEvent.getName());
                    if (editEvent.getTask().equals("Start Server")) {
                        startServerRadio.setSelected(true);
                        task = "Start Server";
                    } else if (editEvent.getTask().equals("Send Command")) {
                        sendCommandField.setEnabled(true);
                        warningList.setEnabled(true);
                        warningAddButton.setEnabled(true);
                        warningEditButton.setEnabled(true);
                        warningRemoveButton.setEnabled(true);
                        task = "Send Command";
                        sendCommandRadio.setSelected(true);
                        sendCommandField.setText(editEvent.getParams().get(0));
                    } else if (editEvent.getTask().equals("Stop Server")) {
                        stopServerRadio.setSelected(true);
                        warningList.setEnabled(true);
                        warningAddButton.setEnabled(true);
                        warningEditButton.setEnabled(true);
                        warningRemoveButton.setEnabled(true);
                        task = "Stop Server";
                    } else if (editEvent.getTask().equals("Restart Server")) {
                        restartServerRadio.setSelected(true);
                        warningList.setEnabled(true);
                        warningAddButton.setEnabled(true);
                        warningEditButton.setEnabled(true);
                        warningRemoveButton.setEnabled(true);
                        remainDownField.setEnabled(true);
                        remainDownLabel.setEnabled(true);
                        task = "Restart Server";
                        remainDownField.setText(hmsFromSeconds(Integer.valueOf(
                                editEvent.getParams().get(0))));
                    } else if (editEvent.getTask().equals("Backup")) {
                        backupRadio.setSelected(true);
                        warningList.setEnabled(true);
                        warningAddButton.setEnabled(true);
                        warningEditButton.setEnabled(true);
                        warningRemoveButton.setEnabled(true);
                        task = "Backup";
                    } else if (editEvent.getTask().equals("Save Worlds")) {
                        saveWorldsRadio.setSelected(true);
                        warningList.setEnabled(true);
                        warningAddButton.setEnabled(true);
                        warningEditButton.setEnabled(true);
                        warningRemoveButton.setEnabled(true);
                        task = "Save Worlds";
                    }
                    String cronex = editEvent.getCronEx();
                    String seconds = cronex.split("\\s")[0];
                    String minutes = cronex.split("\\s")[1];
                    String hours = cronex.split("\\s")[2];
                    String dom = cronex.split("\\s")[3];
                    String mon = cronex.split("\\s")[4];
                    String dow = cronex.split("\\s")[5];

                    if (seconds.contains("/")) {
                        secondsField.setText(seconds.split("/")[0]);
                        secondsAgainCheckBox.setSelected(true);
                        secondsAgainField.setText(seconds.split("/")[1]);
                    } else {
                        secondsField.setText(seconds);
                    }
                    if (minutes.equals("*")) {
                        minutesAllCheckBox.setSelected(true);
                    } else {
                        if (minutes.contains("/")) {
                            minutesField.setText(minutes.split("/")[0]);
                            minutesAgainCheckBox.setSelected(true);
                            minutesAgainField.setText(minutes.split("/")[1]);
                        } else {
                            minutesField.setText(minutes);
                        }
                    }
                    if (hours.equals("*")) {
                        hoursAllCheckBox.setSelected(true);
                    } else {
                        if (hours.contains("/")) {
                            hoursField.setText(hours.split("/")[0]);
                            hoursAgainCheckBox.setSelected(true);
                            hoursAgainField.setText(hours.split("/")[1]);
                        } else {
                            hoursField.setText(hours);
                        }
                    }
                    domAllCheckBox.setSelected(false);
                    if (dom.equals("*")) {
                        domButton.setSelected(true);
                        domAllCheckBox.setSelected(true);
                    } else if (!dom.equals("?")) {
                        domButton.setSelected(true);
                        domField.setText(dom);
                    }
                    if (!mon.equals("*")) {
                        String[] montharray = mon.split(",");
                        int j = 0;
                        for (int i = 1; i < month.size(); i++) {
                            if (montharray[j].equals(Integer.toString(i))) {
                                month.get(i).setSelected(true);
                                j++;
                            } else {
                                month.get(i).setSelected(false);
                            }
                        }
                        verifyAllSelected(month);
                    }
                    if (!dow.equals("?")) {
                        for (int i = 1; i < dayOfWeek.size(); i++) {
                            if (dow.contains(Integer.toString(i)) || dow.contains("*")) {
                                dayOfWeek.get(i).setSelected(true);
                            }
                        }
                        verifyAllSelected(dayOfWeek);
                    }

                    warningListModel = editEvent.getWarningList();
                    warningList.setModel(warningListModel);

                    createButton.setEnabled(true);
                    createButton.setText("Update");

                    secondsAgainField.setEnabled(secondsAgainCheckBox.isSelected());
                    minutesAgainField.setEnabled(minutesAgainCheckBox.isSelected());
                    hoursAgainField.setEnabled(hoursAgainCheckBox.isSelected());
                //}
            //});
        //} catch (InterruptedException e) {
            //taskNameField.setText("Error!");
        //} catch (java.lang.reflect.InvocationTargetException ite) {
            //taskNameField.setText("Error!");
        //}
    }

    private void verifyAllSelected(
            java.util.List<javax.swing.JToggleButton> buttons) {
        final java.util.List<javax.swing.JToggleButton> buttonList = buttons;
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                int numSelected = 0;
                for(int i = 1; i < buttonList.size(); i++) {
                    if (buttonList.get(i).isSelected()) {
                        numSelected++;
                    }
                    boldButton(buttonList.get(i));
                }
                if (numSelected == buttonList.size() - 1) {
                    buttonList.get(0).setSelected(true);
                    //buttonList.get(0).setText("None");
                } else {
                    buttonList.get(0).setSelected(false);
                    //buttonList.get(0).setText("All");
                }
                if (buttonList.get(1).getText().equals("Sun") && numSelected > 0) {
                    domField.setEnabled(false);
                    domAllCheckBox.setEnabled(false);
                    domButton.setSelected(false);
                } else if (buttonList.get(1).getText().equals("Sun") && numSelected == 0) {
                    domField.setEnabled(true);
                    domAllCheckBox.setEnabled(true);
                    domButton.setSelected(true);
                }
                boldButton(buttonList.get(0));
                boldButton(domButton);
            }
        });
    }


    private GUIListModel<ServerWarning> warningListModel;
    private EventModel editEvent;
    private Pail pail;

    private ServerWarningDialog warningDialog;
    private java.util.List<javax.swing.JToggleButton> dayOfWeek;
    private java.util.List<javax.swing.JToggleButton> month;

    private String task;
    private java.awt.Font boldFont;
    private java.awt.Font normalFont;
    private String borderTitle;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton aprButton;
    private javax.swing.JToggleButton augButton;
    private javax.swing.JRadioButton backupRadio;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton createButton;
    private javax.swing.JToggleButton decButton;
    private javax.swing.JCheckBox domAllCheckBox;
    private javax.swing.JToggleButton domButton;
    private javax.swing.JTextField domField;
    private javax.swing.JToggleButton dowAllButton;
    private javax.swing.JLabel dowLabel;
    private javax.swing.JToggleButton febButton;
    private javax.swing.JToggleButton friButton;
    private javax.swing.JCheckBox hoursAgainCheckBox;
    private javax.swing.JTextField hoursAgainField;
    private javax.swing.JCheckBox hoursAllCheckBox;
    private javax.swing.JTextField hoursField;
    private javax.swing.JLabel hoursLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JToggleButton janButton;
    private javax.swing.JToggleButton julButton;
    private javax.swing.JToggleButton junButton;
    private javax.swing.JToggleButton marButton;
    private javax.swing.JToggleButton mayButton;
    private javax.swing.JCheckBox minutesAgainCheckBox;
    private javax.swing.JTextField minutesAgainField;
    private javax.swing.JCheckBox minutesAllCheckBox;
    private javax.swing.JTextField minutesField;
    private javax.swing.JLabel minutesLabel;
    private javax.swing.JToggleButton monButton;
    private javax.swing.JToggleButton monthAllButton;
    private javax.swing.JLabel monthLabel;
    private javax.swing.JToggleButton novButton;
    private javax.swing.JToggleButton octButton;
    private javax.swing.JTextField remainDownField;
    private javax.swing.JLabel remainDownLabel;
    private javax.swing.JRadioButton restartServerRadio;
    private javax.swing.JToggleButton satButton;
    private javax.swing.JRadioButton saveWorldsRadio;
    private javax.swing.JLabel scheduleLabel;
    private javax.swing.JCheckBox secondsAgainCheckBox;
    private javax.swing.JTextField secondsAgainField;
    private javax.swing.JTextField secondsField;
    private javax.swing.JLabel secondsLabel;
    private javax.swing.JTextField sendCommandField;
    private javax.swing.JRadioButton sendCommandRadio;
    private javax.swing.JToggleButton sepButton;
    private javax.swing.ButtonGroup serverTaskGroup;
    private javax.swing.JLabel serverWarningLabel;
    private javax.swing.JRadioButton startServerRadio;
    private javax.swing.JRadioButton stopServerRadio;
    private javax.swing.JToggleButton sunButton;
    private javax.swing.JPanel taskEntryPanel;
    private javax.swing.JCheckBox taskIsCustomButtonCheckBox;
    private javax.swing.JTextField taskNameField;
    private javax.swing.JLabel taskNameLabel;
    private javax.swing.JToggleButton thuButton;
    private javax.swing.JTextArea timeSummaryField;
    private javax.swing.JToggleButton tueButton;
    private javax.swing.JButton warningAddButton;
    private javax.swing.JButton warningEditButton;
    private javax.swing.JList warningList;
    private javax.swing.JButton warningRemoveButton;
    private javax.swing.JToggleButton wedButton;
    // End of variables declaration//GEN-END:variables

}
