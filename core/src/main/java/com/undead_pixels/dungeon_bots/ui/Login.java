package com.undead_pixels.dungeon_bots.ui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.undead_pixels.dungeon_bots.User;

/**
 * A login component that returns a user by calling the Login.challenge()
 * method. If login is invalid, the Login.challenge() method returns null.
 * NOTE:  Using the professional JXLoginPane instead
 */
@Deprecated
public class Login extends JDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/*
	 * This class drawn from tutorial at
	 * http://www.zentut.com/java-swing/simple-login-dialog/
	 */

	private User _User = null;
	private int _Attempts;
	private final int MAX_ATTEMPTS = 3;

	private Login(Frame frame, int attempts) {

		super(frame, "Welcome to DungeonBots", true);

		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.ipadx = constraints.ipady = 10;

		constraints.gridx = 1;
		constraints.gridy = 0;
		constraints.gridwidth = 3;
		mainPanel.add(new JLabel("Please login."), constraints);

		JLabel lblUserName = new JLabel("Username:");
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = constraints.gridheight = 1;
		mainPanel.add(lblUserName, constraints);

		JTextField txtUserName = new JTextField(20);
		constraints.gridx = 1;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		mainPanel.add(txtUserName, constraints);

		JLabel lblPassword = new JLabel("Password:");
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.gridwidth = constraints.gridheight = 1;
		mainPanel.add(lblPassword, constraints);

		JPasswordField pswdField = new JPasswordField(20);
		constraints.gridx = 1;
		constraints.gridy = 2;
		constraints.gridwidth = 2;
		mainPanel.add(pswdField, constraints);

		JPanel bttnPanel = new JPanel();
		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.gridwidth = 3;
		bttnPanel.setLayout(new BoxLayout(bttnPanel, BoxLayout.LINE_AXIS));
		JButton bttnLogin = new JButton("Login");
		bttnPanel.add(bttnLogin);
		JButton bttnCancel = new JButton("Cancel");
		bttnPanel.add(bttnCancel);
		JButton bttnNewUser = new JButton("New User");
		bttnPanel.add(bttnNewUser);
		bttnPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		constraints.ipadx = constraints.ipady = 25;
		mainPanel.add(bttnPanel, constraints);

		JButton bttnDumb = makeHyperTextButton("Forgot password?");
		constraints.gridx = 0;
		constraints.gridy = 4;
		constraints.gridwidth = 3;
		mainPanel.add(bttnDumb, constraints);

		mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		add(mainPanel);
		setResizable(false);
		setAutoRequestFocus(true);

		pack();
		setLocation((Toolkit.getDefaultToolkit().getScreenSize().width) / 2 - getWidth() / 2,
				(Toolkit.getDefaultToolkit().getScreenSize().height) / 2 - getHeight() / 2);

		_Attempts = attempts;

		bttnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				char[] pswd = pswdField.getPassword();
				pswdField.setText("");
				_Attempts--;
				String userName = txtUserName.getText();
				_User = fetchUser(userName, pswd);
				if (_User != null)
					// Valid login.
					dispose();
				else if (_Attempts > 0) {
					JOptionPane.showMessageDialog(Login.this, "Invalid user name or password.  You have "
							+ (MAX_ATTEMPTS - _Attempts) + " attempts remaining.", "Login.", JOptionPane.ERROR_MESSAGE);
					txtUserName.setText("");

				} else { // _Attempts <= 0
					JOptionPane.showMessageDialog(Login.this, "Invalid user name or password.  Max attempts reached.",
							"Login.", JOptionPane.ERROR_MESSAGE);
					dispose();
				}
			}
		});
		bttnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pswdField.setText("");
				dispose();
			}
		});
		bttnNewUser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(Login.this, "Have not implemented new-user functionality.", "Login.",
						JOptionPane.ERROR_MESSAGE);
				pswdField.setText("");
				txtUserName.setText("");
			}
		});
		bttnDumb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(Login.this, "Have not implemented forgotten-password functionality.",
						"Login.", JOptionPane.ERROR_MESSAGE);
				pswdField.setText("");
				txtUserName.setText("");
			}
		});

		Action return_action = new AbstractAction() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				bttnLogin.doClick();
			}
		};

		txtUserName.addActionListener(return_action);
		pswdField.addActionListener(return_action);
	}

	public static JButton makeHyperTextButton(String message) {
		// Suggested by "McDowell", Feb '09, available at:
		// https://stackoverflow.com/questions/527719/how-to-add-hyperlink-in-jlabel

		JButton bttn = new JButton();
		bttn.setText("<HTML><FONT color=\"#000099\"><U>" + message + "</U></FONT></HTML>");
		bttn.setHorizontalAlignment(SwingConstants.CENTER);
		bttn.setBorderPainted(false);
		bttn.setOpaque(false);
		bttn.setBackground(Color.WHITE);

		return bttn;
	}

	private User fetchUser(String username, char[] password) {
		//User fetched = null;
		try {
			return User.fromJSON("");
			// return null;
			// TODO: get the User from remote source as JSON, and then build a
			// User object from it thru User.FromJSON(String).
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Error fetching user:\n" + ex.toString());
			return null;
		} finally {
			// This procedure so passwords attempts don't exist as garbage
			// somewhere in
			// memory:
			for (int i = 0; i < password.length; i++)
				password[i] = 0;
		}

	}

	public static User challenge(String message, int attempts) {
		JFrame loginFrame = new JFrame(message);
		loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		loginFrame.setLayout(new FlowLayout());
		loginFrame.setSize(300, 250);
		Login login = new Login(loginFrame, attempts);
		login.setVisible(true);
		User result = login._User;
		loginFrame.dispose();
		return result;
	}

}
