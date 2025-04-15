package fr.uga.miashs.dciss.chatservice.client;

import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.JPanel;
import java.awt.GridLayout;
import javax.swing.JTextArea;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

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
import java.awt.FlowLayout;

public class UIClient {

	private JFrame frame;
	private ClientMsg client;
	private JTextArea txtAOutbox;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JTextArea txtAInbox;
	private JLabel lblInfoMsg;

	/**
	 * Launch the application.
	 */
	
	public void insertText(String txt) {
		
		
	}
	
	
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
		
		JPanel panel_1 = new JPanel();
		frame.getContentPane().add(panel_1, BorderLayout.EAST);
		panel_1.setLayout(new GridLayout(0, 1, 0, 0));
		
		JLabel lblUsers = new JLabel("Vos Conversations");
		panel_1.add(lblUsers);
		
		
		// ------------------------------------------------------------------//
        ActionListener listener = new actionperformclass();
		
		JButton btnNewButton = new JButton("New button");
		panel_1.add(btnNewButton);
		
		btnNewButton.addActionListener(listener);
		// ------------------------------------------------------------------//
		JButton btnCreateGroup = new JButton("Créer un groupe");
		panel_1.add(btnCreateGroup);
		
		btnCreateGroup.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                String members = JOptionPane.showInputDialog(frame,
                        "Entrez les ID des membres séparées par des virgules ", null);
                int[] membersTab = new int[50];
                String member = "";
                int nbMembers = 0;
                for (int i = 0; i < members.length(); i += 1) {
                	
                	if (members.charAt(i) >= '0' && members.charAt(i) <= '9') {
                		member += members.charAt(i);
                	} else {
                		if (member != "") {
                			membersTab[nbMembers] = Integer.parseInt(member);
                			nbMembers += 1;
                			member = "";
                		}
                	}
                }
                if (member != "") {
                	membersTab[nbMembers] = Integer.parseInt(member);
        			nbMembers += 1;
        			member = "";
                }
                
                
                System.out.println(nbMembers);
                if (nbMembers < 1) {
                	lblInfoMsg.setForeground(Color.RED);
                	lblInfoMsg.setText("Vous ne pouvez pas être seul.e dans un groupe");
                } else {
                	
//                client.createGroup(c.getIdentifier(), membersTab);
                	lblInfoMsg.setText("Groupe Créé !");
                	lblInfoMsg.setForeground(Color.BLUE);

                }
            }
        });
		
		JRadioButton rdbtn1 = new JRadioButton("1");
		buttonGroup.add(rdbtn1);
		panel_1.add(rdbtn1);
		rdbtn1.setActionCommand("1");
		
		JRadioButton rdbtn2 = new JRadioButton("2");
		buttonGroup.add(rdbtn2);
		panel_1.add(rdbtn2);
		rdbtn2.setActionCommand("2");
		
		JRadioButton rdbtn3 = new JRadioButton("3");
		buttonGroup.add(rdbtn3);
		panel_1.add(rdbtn3);
		rdbtn3.setActionCommand("3");
		
		JRadioButton rdbtn4 = new JRadioButton("4");
		buttonGroup.add(rdbtn4);
		panel_1.add(rdbtn4);
		rdbtn4.setActionCommand("4");
		
		JRadioButton rdbtn5 = new JRadioButton("5");
		buttonGroup.add(rdbtn5);
		panel_1.add(rdbtn5);
		rdbtn5.setActionCommand("5");
		

		
		JPanel panAllWithOutChatBox = new JPanel();
		frame.getContentPane().add(panAllWithOutChatBox, BorderLayout.CENTER);
		panAllWithOutChatBox.setLayout(new BorderLayout(0, 0));
		
		lblInfoMsg = new JLabel("Jusqu'ici tout se passe bien !");
		lblInfoMsg.setHorizontalAlignment(SwingConstants.CENTER);
		panAllWithOutChatBox.add(lblInfoMsg, BorderLayout.SOUTH);
		
		JPanel panChatBox = new JPanel();
		panAllWithOutChatBox.add(panChatBox, BorderLayout.CENTER);
		panChatBox.setLayout(new BorderLayout(0, 0));
		
		JPanel panInbox = new JPanel();
		panChatBox.add(panInbox);
		panInbox.setLayout(new BorderLayout(0, 0));

		
		JLabel lblInbox = new JLabel("Conversation");
		panInbox.add(lblInbox, BorderLayout.NORTH);
		
		txtAInbox = new JTextArea();
		panInbox.add(txtAInbox);
		txtAInbox.setRows(1);
		
		JTextArea textArea = new JTextArea();
		panInbox.add(textArea, BorderLayout.WEST);
		
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
				System.out.println("Dest : " + dest + ", Msg : " + message);
				
				client.sendMessage(dest, message);
				System.out.println(txtAInbox.getText().length());
				if (txtAInbox.getText().length() == 0) {
					txtAInbox.setText("Début de la conversation avec "+dest);
				}
				txtAInbox.setText(txtAInbox.getText() + "\n" + "Vous -> " + dest +" : " + message );
				
				txtAOutbox.setText("");
				//END OF ACTION//
			}
		});
		panOutbox.add(btnSend, BorderLayout.SOUTH);
		
		JPanel panel = new JPanel();
		panOutbox.add(panel, BorderLayout.NORTH);
		
		JLabel lblOutbox = new JLabel("Votre message : ");
		panel.add(lblOutbox);
	}

}
