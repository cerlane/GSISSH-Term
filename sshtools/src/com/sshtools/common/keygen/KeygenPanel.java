/*
 *  SSHTools - Java SSH2 API
 *
 *  Copyright (C) 2002 Lee David Painter.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  You may also distribute it and/or modify it under the terms of the
 *  Apache style J2SSH Software License. A copy of which should have
 *  been provided with the distribution.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  License document supplied with your distribution for more details.
 *
 */

package com.sshtools.common.keygen;

import java.io.File;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.sshtools.common.ui.NumericTextField;
import com.sshtools.common.ui.UIUtil;
import com.sshtools.common.ui.XTextField;
import com.sshtools.j2ssh.authentication.UserGridCredential;

/**
 *
 *
 * @author $author$
 * @version $Revision: 1.1.1.1 $
 */
public class KeygenPanel
    extends JPanel
    implements DocumentListener,
    ActionListener {
  /**  */
  public final static int GENERATE_KEY_PAIR = 0;

  /**  */
  public final static int CONVERT_IETF_SECSH_TO_OPENSSH = 1;

  /**  */
  public final static int CONVERT_OPENSSH_TO_IETF_SECSH = 2;

  /**  */
  public final static int CHANGE_PASSPHRASE = 3;
  
  /** */
  public final static int CONVERT_PEM_TO_PKCS12 = 4;

  //  Private instance variables
  private JButton browseInput;

  //  Private instance variables
  private JButton browseOutput;
  private JComboBox action;
  private JComboBox type;
  private JLabel bitsLabel;
  private JLabel inputFileLabel;
  private JLabel newPassphraseLabel;
  private JLabel oldPassphraseLabel;
  private JLabel outputFileLabel;
  private JLabel typeLabel;
  private JPasswordField newPassphrase;
  private JPasswordField oldPassphrase;
  private JProgressBar strength;
  private XTextField inputFile;
  private XTextField outputFile;
  private NumericTextField bits;
  
  private JPanel keyPanel;
  private JPanel strengthPanel;
  
  private JPanel convertPanel; 
  private JLabel pem2pkcsLabel;
  private JButton pem2pkcsButton;

  /**
   * Creates a new KeygenPanel object.
   */
  public KeygenPanel() {
    super();
    
    JPanel selectActionPanel = new JPanel(new GridBagLayout());    
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.WEST;

    Insets normalInsets = new Insets(0, 2, 4, 2);
    Insets indentedInsets = new Insets(0, 26, 4, 2);
    gbc.insets = normalInsets;
    gbc.weightx = 0.0;
    
    UIUtil.jGridBagAdd(selectActionPanel, new JLabel("Action"), gbc, 1);
    action = new JComboBox(new String[] {
            "Generate key pair", "Convert IETF SECSH to OpenSSH",
            "Convert OpenSSH to IETF SECSH", "Change passphrase", "Convert PEM to PKCS12"
	});
	action.addActionListener(this);
	UIUtil.jGridBagAdd(selectActionPanel, action, gbc, GridBagConstraints.RELATIVE);
	gbc.weightx = 1.0;
	UIUtil.jGridBagAdd(selectActionPanel, new JLabel(), gbc,
			GridBagConstraints.REMAINDER);
    

    keyPanel = new JPanel(new GridBagLayout());
    keyPanel.setBorder(BorderFactory.createTitledBorder("Key"));

   
    /*//  Action
    UIUtil.jGridBagAdd(keyPanel, new JLabel("Action"), gbc, 1);
    gbc.weightx = 1.0;
    action = new JComboBox(new String[] {
                           "Generate key pair", "Convert IETF SECSH to OpenSSH",
                           "Convert OpenSSH to IETF SECSH", "Change passphrase", "Convert PEM to PKCS12"
    });
    action.addActionListener(this);
    gbc.weightx = 2.0;
    UIUtil.jGridBagAdd(keyPanel, action, gbc, GridBagConstraints.RELATIVE);
    gbc.weightx = 0.0;
    UIUtil.jGridBagAdd(keyPanel, new JLabel(), gbc,
                       GridBagConstraints.REMAINDER);
    gbc.insets = indentedInsets;*/

    //  File
    inputFileLabel = new JLabel("Input File");
    UIUtil.jGridBagAdd(keyPanel, inputFileLabel, gbc, 1);
    gbc.insets = normalInsets;
    gbc.weightx = 1.0;
    inputFile = new XTextField(20);
    UIUtil.jGridBagAdd(keyPanel, inputFile, gbc, GridBagConstraints.RELATIVE);
    inputFileLabel.setLabelFor(inputFile);
    gbc.weightx = 0.0;
    browseInput = new JButton("Browse");
    browseInput.setMnemonic('b');
    browseInput.addActionListener(this);
    UIUtil.jGridBagAdd(keyPanel, browseInput, gbc,
                       GridBagConstraints.REMAINDER);

    //  File
    gbc.insets = indentedInsets;
    outputFileLabel = new JLabel("Output File");
    UIUtil.jGridBagAdd(keyPanel, outputFileLabel, gbc, 1);
    gbc.insets = normalInsets;
    gbc.weightx = 1.0;
    outputFile = new XTextField(20);
    UIUtil.jGridBagAdd(keyPanel, outputFile, gbc,
                       GridBagConstraints.RELATIVE);
    gbc.weightx = 0.0;
    outputFileLabel.setLabelFor(outputFile);
    browseOutput = new JButton("Browse");
    browseOutput.setMnemonic('r');
    browseOutput.addActionListener(this);
    UIUtil.jGridBagAdd(keyPanel, browseOutput, gbc,
                       GridBagConstraints.REMAINDER);

    //  Old Passphrase
    gbc.insets = indentedInsets;
    oldPassphraseLabel = new JLabel("Old Passphrase");
    UIUtil.jGridBagAdd(keyPanel, oldPassphraseLabel, gbc, 1);
    gbc.insets = normalInsets;
    gbc.weightx = 2.0;
    oldPassphrase = new JPasswordField(20);
    oldPassphrase.setBackground(Color.white);
    oldPassphrase.getDocument().addDocumentListener(this);
    oldPassphraseLabel.setLabelFor(oldPassphrase);
    UIUtil.jGridBagAdd(keyPanel, oldPassphrase, gbc,
                       GridBagConstraints.RELATIVE);
    UIUtil.jGridBagAdd(keyPanel, new JLabel(), gbc,
                       GridBagConstraints.REMAINDER);

    //  Passphrase
    gbc.insets = indentedInsets;
    newPassphraseLabel = new JLabel("New Passphrase");
    UIUtil.jGridBagAdd(keyPanel, newPassphraseLabel, gbc, 1);
    gbc.insets = normalInsets;
    gbc.weightx = 2.0;
    newPassphrase = new JPasswordField(20);
    newPassphrase.setBackground(Color.white);
    newPassphrase.getDocument().addDocumentListener(this);
    newPassphraseLabel.setLabelFor(newPassphrase);
    UIUtil.jGridBagAdd(keyPanel, newPassphrase, gbc,
                       GridBagConstraints.RELATIVE);
    UIUtil.jGridBagAdd(keyPanel, new JLabel(), gbc,
                       GridBagConstraints.REMAINDER);

    //  Bits
    gbc.insets = indentedInsets;
    bitsLabel = new JLabel("Bits");
    UIUtil.jGridBagAdd(keyPanel, bitsLabel, gbc, 1);
    gbc.weightx = 2.0;
    gbc.insets = normalInsets;
    bits = new NumericTextField(new Integer(512), new Integer(1024),
                                new Integer(1024));
    bitsLabel.setLabelFor(bits);
    UIUtil.jGridBagAdd(keyPanel, bits, gbc, GridBagConstraints.RELATIVE);
    UIUtil.jGridBagAdd(keyPanel, new JLabel(), gbc,
                       GridBagConstraints.REMAINDER);

    //  Type
    gbc.insets = indentedInsets;
    typeLabel = new JLabel("Type");
    UIUtil.jGridBagAdd(keyPanel, typeLabel, gbc, 1);
    gbc.insets = normalInsets;
    gbc.weightx = 2.0;
    type = new JComboBox(new String[] {"DSA", "RSA"});
    type.setFont(inputFile.getFont());

    //  Combo boxes look crap in metal
    UIUtil.jGridBagAdd(keyPanel, type, gbc, GridBagConstraints.RELATIVE);
    UIUtil.jGridBagAdd(keyPanel, new JLabel(), gbc,
                       GridBagConstraints.REMAINDER);
    strength = new JProgressBar(0, 40);
    strength.setStringPainted(true);

    strengthPanel = new JPanel(new GridLayout(1, 1));
    strengthPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder("Passphrase strength"),
        BorderFactory.createEmptyBorder(4, 4, 4, 4)));
    strengthPanel.add(strength);
    
    /** Conversion Panel **/
    convertPanel = new JPanel(new GridBagLayout());
    convertPanel.setBorder(BorderFactory.createTitledBorder("Convert Certificate Format"));
   
    //PEM to PKCS12 button
    gbc.insets = indentedInsets;
    pem2pkcsLabel = new JLabel("PEM to PKCS12");
    UIUtil.jGridBagAdd(convertPanel, pem2pkcsLabel, gbc, 1);
    gbc.insets = normalInsets;
    gbc.weightx = 1.0;
    pem2pkcsButton = new JButton("Convert");
    pem2pkcsButton.addActionListener(this);
    UIUtil.jGridBagAdd(convertPanel, pem2pkcsButton, gbc, GridBagConstraints.RELATIVE);
    UIUtil.jGridBagAdd(convertPanel, new JLabel(), gbc, GridBagConstraints.REMAINDER);
    
    gbc.insets = indentedInsets;
    gbc.weightx = 0.0;
    JLabel certInfoLabel = new JLabel("<html>Assumes the PEM certficates 'usercert.pem' and 'userkey.pem' are in <br/>{home.directory}/.globus directory.</html>");
    								//"Newly created PKCS12 file will be saved in {home.directory}/.globus/usercred.{p12 or pfx}</html>");
    certInfoLabel.setFont(new Font("Serif", Font.ITALIC, 14));
    UIUtil.jGridBagAdd(convertPanel, certInfoLabel, gbc, GridBagConstraints.REMAINDER);
       
    convertPanel.setSize(50, 50);
    convertPanel.setVisible(false);
    
    
    inputFileLabel = new JLabel("Input File");
    UIUtil.jGridBagAdd(keyPanel, inputFileLabel, gbc, 1);
    
    inputFile = new XTextField(20);
    UIUtil.jGridBagAdd(keyPanel, inputFile, gbc, GridBagConstraints.RELATIVE);

    //  Build this panel
    setLayout(new BorderLayout());
    add(selectActionPanel, BorderLayout.NORTH);
    add(keyPanel, BorderLayout.CENTER);
    add(strengthPanel, BorderLayout.SOUTH);
    
    calculateStrength();
    setAvailableActions();
  }

  /**
   *
   *
   * @return
   */
  public int getAction() {
    return action.getSelectedIndex();
  }

  /**
   *
   *
   * @return
   */
  public int getBits() {
    return ( (Integer) bits.getValue()).intValue();
  }

  /**
   *
   *
   * @return
   */
  public String getInputFilename() {
    return inputFile.getText();
  }

  /**
   *
   *
   * @return
   */
  public char[] getNewPassphrase() {
    return newPassphrase.getPassword();
  }

  /**
   *
   *
   * @return
   */
  public char[] getOldPassphrase() {
    return oldPassphrase.getPassword();
  }

  /**
   *
   *
   * @return
   */
  public String getOutputFilename() {
    return outputFile.getText();
  }

  /**
   *
   *
   * @return
   */
  public String getType() {
    return (String) type.getSelectedItem();
  }

  /**
   *
   *
   * @param evt
   */
  public void actionPerformed(ActionEvent evt) {
    if (evt.getSource() == browseOutput) {
      File f = new File(outputFile.getText());
      JFileChooser chooser = new JFileChooser(f);
      chooser.setSelectedFile(f);
      chooser.setDialogTitle("Choose output file ..");
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

      if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
        outputFile.setText(chooser.getSelectedFile().getPath());
      }
    }
    else if (evt.getSource() == browseInput) {
      File f = new File(inputFile.getText());
      JFileChooser chooser = new JFileChooser(f);
      chooser.setSelectedFile(f);
      chooser.setDialogTitle("Choose input file ..");
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

      if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        inputFile.setText(chooser.getSelectedFile().getPath());
      }
    }
    else if(evt.getSource() == pem2pkcsButton){
    	JLabel lmyPassphrase = new JLabel("PEM Passphrase:");		        
		JPasswordField myPassphrase =  new JPasswordField(20);
		Object[] obj = {lmyPassphrase, myPassphrase};
    	    	
		String usercredEXT = "p12";
        if (System.getProperty("os.name").toLowerCase().indexOf("win")>0){
        	usercredEXT = "pfx";
        }
        String usercredP12Loc = System.getProperty("user.home")+System.getProperty("file.separator")+".globus"+System.getProperty("file.separator")+"usercred."+ usercredEXT;
       
    	int counter = 0;
    	while(counter<3){
    		int result = JOptionPane.showConfirmDialog(this, obj, "Please input passphrase of your PEM key {userkey.pem}", JOptionPane.OK_CANCEL_OPTION);

			if (result == JOptionPane.OK_OPTION) {
				String passphrase = new String(myPassphrase.getPassword()).trim();
				if (passphrase.equals("")){				
					JOptionPane.showMessageDialog(this, "Empty passphrase is not allowed. Please try again.", "Empty Passphrase Warning", JOptionPane.WARNING_MESSAGE);
					
				}
				else{
			    	try {
						UserGridCredential.convertPEMTOPKCS12(passphrase, usercredP12Loc);
						JOptionPane.showMessageDialog(this, "YOur PKCS12 certificate is generated successfully. You can find it at\n"+ usercredP12Loc + "\nIt has the same passphrase as your PEM key.", "Success", JOptionPane.INFORMATION_MESSAGE);
						break;
					} catch (Exception e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(this, "Passphrase is incorrect. Cannot generate PKCS12 file", "Wrong Passphrase Warning", JOptionPane.WARNING_MESSAGE);
					}
					
				}
			}
			else{
				break;
			}
    	}
    	counter ++;
    }
    else {
      setAvailableActions();
    }
  }

  /**
   *
   *
   * @param e
   */
  public void changedUpdate(DocumentEvent e) {
    calculateStrength();
  }

  /**
   *
   *
   * @param e
   */
  public void insertUpdate(DocumentEvent e) {
    calculateStrength();
  }

  /**
   *
   *
   * @param e
   */
  public void removeUpdate(DocumentEvent e) {
    calculateStrength();
  }

  private void setAvailableActions() {
	  
	if (getAction() == CONVERT_PEM_TO_PKCS12){
		keyPanel.setVisible(false);
		this.add(convertPanel, BorderLayout.CENTER);
		convertPanel.setVisible(true);
		strengthPanel.setVisible(false);
		
	}
	else{
		convertPanel.setVisible(false);
		this.add(keyPanel, BorderLayout.CENTER);
		keyPanel.setVisible(true);
		strengthPanel.setVisible(true);
	}
    inputFile.setEnabled( (getAction() == CONVERT_IETF_SECSH_TO_OPENSSH)
                         || (getAction() == CONVERT_OPENSSH_TO_IETF_SECSH)
                         || (getAction() == CHANGE_PASSPHRASE));
    inputFileLabel.setEnabled(inputFile.isEnabled());
    browseInput.setEnabled(inputFile.isEnabled());
    bits.setEnabled(getAction() == GENERATE_KEY_PAIR);
    bitsLabel.setEnabled(bits.isEnabled());
    outputFile.setEnabled( (getAction() == CONVERT_IETF_SECSH_TO_OPENSSH)
                          || (getAction() == CONVERT_OPENSSH_TO_IETF_SECSH)
                          || (getAction() == GENERATE_KEY_PAIR));
    outputFileLabel.setEnabled(outputFile.isEnabled());
    browseOutput.setEnabled(outputFile.isEnabled());
    newPassphrase.setEnabled( (getAction() == GENERATE_KEY_PAIR)
                             || (getAction() == CHANGE_PASSPHRASE));
    newPassphraseLabel.setEnabled(newPassphrase.isEnabled());
    oldPassphrase.setEnabled(getAction() == CHANGE_PASSPHRASE);
    oldPassphraseLabel.setEnabled(oldPassphrase.isEnabled());
    type.setEnabled(getAction() == GENERATE_KEY_PAIR);
    typeLabel.setEnabled(type.isEnabled());

    if (inputFile.isEnabled()) {
      inputFile.requestFocus();
    }
    else {
      outputFile.requestFocus();
    }
    
    //To make entire JPanel visible or invisible
    
  }

  private void calculateStrength() {
    char[] pw = newPassphrase.getPassword();
    strength.setValue( (pw.length < 40) ? pw.length : 40);

    Color f;
    String t;

    if (pw.length == 0) {
      f = Color.red;
      t = "Empty!!";
    }
    else {
      StringBuffer buf = new StringBuffer();
      buf.append(pw.length);
      buf.append(" characters - ");

      if (pw.length < 10) {
        f = Color.red;
        buf.append("Weak!");
      }
      else if (pw.length < 20) {
        f = Color.orange;
        buf.append("Ok");
      }
      else if (pw.length < 30) {
        f = Color.green.darker();
        buf.append("Strong");
      }
      else {
        f = Color.green;
        buf.append("Very strong!");
      }

      t = buf.toString();
    }

    strength.setString(t);
    strength.setForeground(f);
  }
}
