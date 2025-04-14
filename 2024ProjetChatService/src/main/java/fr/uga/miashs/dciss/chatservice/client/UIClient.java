package fr.uga.miashs.dciss.chatservice.client;

import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JPanel;
import java.awt.GridLayout;
import javax.swing.JTextArea;
import javax.swing.JScrollBar;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.UnknownHostException;
import java.awt.event.ActionEvent;

public class UIClient {

	private JFrame frame;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private ClientMsg client;
	private JTextArea txtAOutbox;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIClient window = new UIClient();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * @throws UnknownHostException 
	 */
	public UIClient() throws UnknownHostException {
		int port = 1667;
		String ipServer = "localhost";
		this.client = new ClientMsg(ipServer, port);
		this.client.addMessageListener(p -> System.out.println(p.srcId + " says to " + p.destId + ": " + new String(p.data)));
		this.client.addConnectionListener(active ->  {if (!active) System.exit(0);});
		this.client.startSession();
		System.out.println("Vous êtes : " + this.client.getIdentifier());

		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JLabel lbltitle = new JLabel("Bienvenue sur le chat !");
		lbltitle.setHorizontalAlignment(SwingConstants.CENTER);
		frame.getContentPane().add(lbltitle, BorderLayout.NORTH);
		
		JPanel panAllWithOutChatBox = new JPanel();
		frame.getContentPane().add(panAllWithOutChatBox, BorderLayout.CENTER);
		panAllWithOutChatBox.setLayout(new BorderLayout(0, 0));
		
		JLabel lblInfoMsg = new JLabel("Un message d'information");
		panAllWithOutChatBox.add(lblInfoMsg, BorderLayout.SOUTH);
		
		JPanel panChatBox = new JPanel();
		panAllWithOutChatBox.add(panChatBox, BorderLayout.CENTER);
		panChatBox.setLayout(new BorderLayout(0, 0));
		
		JPanel panInbox = new JPanel();
		panChatBox.add(panInbox);
		panInbox.setLayout(new BorderLayout(0, 0));
		
		JTextArea txtAInbox = new JTextArea();
		panInbox.add(txtAInbox);
		txtAInbox.setRows(1);
		
		JLabel lblInbox = new JLabel("Conversation");
		panInbox.add(lblInbox, BorderLayout.NORTH);
		
		JPanel panOutbox = new JPanel();
		panChatBox.add(panOutbox, BorderLayout.SOUTH);
		panOutbox.setLayout(new BorderLayout(0, 0));
		
		txtAOutbox = new JTextArea();
		panOutbox.add(txtAOutbox);
		
		JButton btnSend = new JButton("Envoyer");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				int dest = Integer.parseInt(buttonGroup.getSelection().getActionCommand());
				String message = txtAOutbox.getText();
				System.out.println("DEst : " + dest + "Msg : " + message);
				
				client.sendMessage(dest, message);
				
				//END OF ACTION//
			}
		});
		panOutbox.add(btnSend, BorderLayout.SOUTH);
		
		JPanel panel = new JPanel();
		panOutbox.add(panel, BorderLayout.NORTH);
		
		JLabel lblOutbox = new JLabel("Votre message : ");
		panel.add(lblOutbox);
		
		JRadioButton rdbtn1 = new JRadioButton("1");
		rdbtn1.setActionCommand("1");
		buttonGroup.add(rdbtn1);
		panel.add(rdbtn1);
		
		JRadioButton rdbtn2 = new JRadioButton("2");
		rdbtn2.setActionCommand("2");
		buttonGroup.add(rdbtn2);
		panel.add(rdbtn2);
		
		JRadioButton rdbtn3 = new JRadioButton("3");
		rdbtn3.setActionCommand("3");
		buttonGroup.add(rdbtn3);
		panel.add(rdbtn3);
		
		JRadioButton rdbtn4 = new JRadioButton("4");
		rdbtn4.setActionCommand("4");
		buttonGroup.add(rdbtn4);
		panel.add(rdbtn4);
		
		JRadioButton rdbtn5 = new JRadioButton("5");
		rdbtn5.setActionCommand("5");
		buttonGroup.add(rdbtn5);
		panel.add(rdbtn5);
	}

}
