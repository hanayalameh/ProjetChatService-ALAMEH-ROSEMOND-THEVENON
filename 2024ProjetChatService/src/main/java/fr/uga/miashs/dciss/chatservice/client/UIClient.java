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
import javax.swing.DefaultListModel;

import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.Timer;
import java.awt.event.ActionEvent;
import java.awt.FlowLayout;
import javax.swing.JList;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.MenuEvent;

public class UIClient {

	private JFrame frame;
	private ClientMsg client;
	private JTextArea txtAOutbox;
	private JTextArea txtAInbox;
	private JLabel lblInfoMsg;
	private int selectedUser;
	private JLabel lblSelectedUser;
	private JLabel lblInbox;
	private JMenu mnNewMenu;
	private DefaultListModel<Integer> model;
	private Integer[] connectedUsers;
	private int[] myGroups;
	private int[] myGroupsOwn;

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
		int port = 1666;
		String ipServer = "localhost";
		try {
			this.client = new ClientMsg(ipServer, port);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		client.addMessageListener(p -> {client.formatagePacket(p);});
		client.addConnectionListener(active ->  {if (!active) System.exit(0);});
		
		try {
			client.startSession();
		} catch (UnknownHostException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		//Add requete list users
		System.out.println("Vous êtes : " + this.client.getIdentifier());
		
		initialize();
		Integer[] var = client.getConnectedUsers();
		//System.out.println(var);
	}
	
	public void refresh() {
		//client.getConnectedUsers();
		//client.getGroups();
		//client.getGroupsOwn():
		model.clear();
		Integer[] pouet = {1,2,3,4,5,6,7,8};
		connectedUsers = pouet;
		//connectedUsers = client.getConnectedUsers();
	    for (Integer s : connectedUsers) {
	    	if (s != (Integer)client.getIdentifier()) {
	    		model.addElement(s);
	    	}
	    }
//		for(int i = 0 ; i< myGroups.length ; i += 1) {
//			model.addElement(myGroups[i]);
//		}
//		for(int i = 0 ; i< myGroupsOwn.length ; i += 1) {
//			model.addElement(myGroupsOwn[i]);
//		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JLabel lbltitle = new JLabel("Bienvenue sur le chat " +client.getIdentifier() +" !");
		lbltitle.setHorizontalAlignment(SwingConstants.CENTER);
		frame.getContentPane().add(lbltitle, BorderLayout.NORTH);
		
		JPanel panRight = new JPanel();
		frame.getContentPane().add(panRight, BorderLayout.EAST);
		panRight.setLayout(new GridLayout(0, 1, 0, 0));
		
		JLabel lblUsers = new JLabel("Vos Conversations");
		panRight.add(lblUsers);
		
		        
		// -------------------  REFRESH ---------------------------//

		JButton btnRefresh = new JButton("Rafraîchir");
		btnRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refresh();
				
			}
		});
		panRight.add(btnRefresh);
		
		
		// -------------------  CREATE GROUP ---------------------------//

		JButton btnCreateGroup = new JButton("Créer un groupe");
		panRight.add(btnCreateGroup);
		
		
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
                
                
                //System.out.println(nbMembers);
                if (nbMembers < 1) {
                	lblInfoMsg.setForeground(Color.RED);
                	lblInfoMsg.setText("Vous ne pouvez pas être seul.e dans un groupe");
                } else {
                	
//                String reponse = client.createGroupData(client.getIdentifier(), membersTab);
                	String reponse = "Groupe créé !";	
                	lblInfoMsg.setText(reponse);
                	lblInfoMsg.setForeground(Color.BLUE);

                }
            }
        });
		
		
		
		// -------------------  ADD TO GROUP ---------------------------//

		JButton btnAddToGroup = new JButton("Ajouter à un groupe");
		btnAddToGroup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String myGroupsStr = "";

				int[] myGroups2 = {1,2,3,4,5};
				for (int i = 0; i < myGroups2.length ; i += 1) {
					myGroupsStr += myGroups2[i] + ", ";
				}
				
				String addGroup = JOptionPane.showInputDialog(frame,
                        "Vous êtes admin des groupes : " + myGroupsStr + ". Pour ajouter une personne faire 'idGroup:idPersonne'", null);
				int[] groupAndMember = new int[2];
				int j = 0;
				String tmpStr = "";
				//System.out.println(addGroup);
				for(int i = 0; i < addGroup.length() && j < 2; i += 1 ) {
					char currChar = addGroup.charAt(i);
					//System.out.println(addGroup.charAt(i));
					if (currChar >= '0' && currChar <= '9') {
						tmpStr = tmpStr + addGroup.charAt(i);

					} else if (currChar == '-' && tmpStr.length() == 0 && j ==0) {
						tmpStr = tmpStr + addGroup.charAt(i);
					} else {


						//System.out.println(tmpStr);
						groupAndMember[j] = Integer.parseInt(tmpStr);
						tmpStr = "";
						j += 1;
					}
				}
				client.addUserToGroup(groupAndMember[0], groupAndMember[1]);
				
				//System.out.println(addGroup);
				//client.leaveGroup(int);
            	lblInfoMsg.setText(groupAndMember[1] + " a bien été ajouté.e au groupe" + groupAndMember[0]);
            	lblInfoMsg.setForeground(Color.BLUE);
				refresh();
			}
		});
		panRight.add(btnAddToGroup);
		
		// -------------------  MENU ---------------------------//

		
		JMenuBar menuBar = new JMenuBar();


		panRight.add(menuBar);
		
		mnNewMenu = new JMenu("A  qui écrire ?");
		mnNewMenu.addMenuListener(new MenuListener() {
			public void menuCanceled(MenuEvent e) {
			}
			public void menuDeselected(MenuEvent e) {
			}
			public void menuSelected(MenuEvent e) {
				refresh();
			}
		});
		
		menuBar.add(mnNewMenu);

		
		// -------------------  JLIST ---------------------------//

		
	
	    model = new DefaultListModel<Integer>();
	    
	    //-------------REMAKE-----------//
	    Integer[] pouet = {1,2,3,4,5,6,7,8,9};
	    connectedUsers = pouet;
	    //System.out.println(connectedUsers);
	    for (Integer s : connectedUsers) {
	    	if (s != (Integer)client.getIdentifier()) {
	    		model.addElement(s);
	    	}
	    }
		JList<Integer> list = new JList<>(model);
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				selectedUser = list.getSelectedValue();
				//System.out.println(selectedUser);
				lblSelectedUser.setText("Vous écrivez à "+selectedUser);
				lblInbox.setText("Conversation avec " + selectedUser);
				
				txtAInbox.setText("Debut de la conversation avec " + selectedUser+ "\n");
				//String[] conversation = client.getConversation(int);
				String[] conversation = client.afficherHistoriqueTableau(selectedUser);
				for(int i = 0; i < conversation.length; i += 1) {
					txtAInbox.setText(txtAInbox.getText() + conversation[i] + "\n");
				}
				refresh();
			}
		});
		mnNewMenu.add(list);
		
		lblSelectedUser = new JLabel("Vous écrivez à ");
		panRight.add(lblSelectedUser);
		
		
		
		// -------------------  LEAVE GROUP ---------------------------//

		JButton btnLeaveGroup = new JButton("Quitter un groupe");
		btnLeaveGroup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String myGroupsStr = "";
				int[] myGroups2 = {1,2,3,4,5};
				for (int i = 0; i < myGroups2.length ; i += 1) {
					myGroupsStr += myGroups2[i] + ", ";
				}
				
				String leaveGroup = JOptionPane.showInputDialog(frame,
                        "Vous appartenez aux groupes : " + myGroupsStr + ". Quel groupe voulez-vous quitter ?", null);
				int groupToLeave = Integer.parseInt(leaveGroup);
				//System.out.println(leaveGroup);
				//client.leaveGroup(grouptToLeave);
            	lblInfoMsg.setText("Vous avez bien quitté le groupe" + leaveGroup);
            	lblInfoMsg.setForeground(Color.BLUE);
				refresh();
				//////END////
			}
		});
		panRight.add(btnLeaveGroup);
		
		
		// -------------------  Delete GROUP ---------------------------//

		JButton btnDeleteGroup = new JButton("Supprimer un groupe");
		btnDeleteGroup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String myGroupsOwnStr = "";
				int[] myGroups2 = {1,2,3,4,5};
				for (int i = 0; i < myGroups2.length ; i += 1) {
					myGroupsOwnStr += myGroups2[i] + ", ";
				}
				
				String deleteGroup = JOptionPane.showInputDialog(frame,
                        "Vous administrez les groupes : " + myGroupsOwnStr + ". Quel groupe voulez-vous supprimer ?", null);
				//System.out.println(deleteGroup);
				int groupToDelete = Integer.parseInt(deleteGroup);
				
            	lblInfoMsg.setText("Le groupe " + deleteGroup + " a bien été supprimé");
            	lblInfoMsg.setForeground(Color.BLUE);
				//client.deleteGroup(int);
				
				refresh();
			}
		});
		panRight.add(btnDeleteGroup);
		

		
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
		
				
				lblInbox = new JLabel("Conversation avec  ?");
				panInbox.add(lblInbox, BorderLayout.NORTH);
				
				txtAInbox = new JTextArea();
				JScrollPane scrolltxtAInbox = new JScrollPane(txtAInbox);
				scrolltxtAInbox.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);


				panInbox.add(scrolltxtAInbox);
				txtAInbox.setRows(1);
				
				JTextArea textArea = new JTextArea();
				panInbox.add(textArea, BorderLayout.WEST);
				
				JPanel panOutbox = new JPanel();
				panChatBox.add(panOutbox, BorderLayout.SOUTH);
				panOutbox.setLayout(new BorderLayout(0, 0));
				
				txtAOutbox = new JTextArea();
				panOutbox.add(txtAOutbox);
				
				
				//------------------SEND MSG ------------------- //
				JButton btnSend = new JButton("Envoyer");
				btnSend.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						
						int dest = selectedUser;
						//System.out.println("user : " +selectedUser);
						String message = txtAOutbox.getText();
						//System.out.println("Dest : " + dest + ", Msg : " + message);
						
						client.sendMessage(dest, message);
						//System.out.println(txtAInbox.getText().length());
						if (txtAInbox.getText().length() == 0) {
							txtAInbox.setText("Début de la conversation avec "+dest);
						}
						txtAInbox.setText(txtAInbox.getText() + "\n" + "Vous  : " + message );
						
						txtAOutbox.setText("");
						
						refresh();
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
