package fr.uga.miashs.dciss.chatservice.client;

import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
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
import java.util.Arrays;
import java.util.Collection;
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

import fr.uga.miashs.dciss.chatservice.server.UserMsg;

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

		client.addMessageListener(p -> {
		    byte type = p.data[0];
		    ByteBuffer buf = ByteBuffer.wrap(p.data);
		    buf.get();  // skip the type byte

		    switch (type) {
		        case 21: {  
		            int count = buf.getInt();
		            Integer[] users = new Integer[count];
		            for (int i = 0; i < count; i++) {
		                users[i] = buf.getInt();
		            }
		            SwingUtilities.invokeLater(() -> {
		                model.clear();
		                for (Integer u : users) {
		                    model.addElement(u);
		                }
		            });
		            break;
		        }

		        case 34: {  
		            int n = buf.getInt();
		            Integer[] groups = new Integer[n];
		            for (int i = 0; i < n; i++) {
	
		                client.setLastOwnedGroups(buf.getInt());
		            }
		            
		            break;
		        }

		        case 36: {  
		            int groupId = buf.getInt();
		            int count = buf.getInt();
		            int[] others = new int[count];
		            for (int i = 0; i < count; i++) {
		                others[i] = buf.getInt();
		            }
		            client.setLastOtherUsers(groupId, others);
		            break;
		        }

		        case 41: case 42: {  
		            boolean success = buf.get() != 0;
		            System.out.println(success);
		           
		            SwingUtilities.invokeLater(() -> {
		                if (type == 41 && success) {
		                	 int createdGroupId = buf.getInt();
		                    lblInfoMsg.setForeground(Color.BLUE);
		                    lblInfoMsg.setText("Groupe " + createdGroupId + " créé avec succès");
		                    client.setLastOwnedGroups(createdGroupId);
		                    
		                    try {
								client.requestUsersNotInGroup(createdGroupId);
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		                } else {
		                    lblInfoMsg.setForeground(Color.RED);
		                    lblInfoMsg.setText("Erreur création groupe");
		                }
		            });
		            break;
		        }

		        case 43: case 44: {  
		            boolean success = buf.get() != 0;
		            int targetGroup = buf.getInt();
		            SwingUtilities.invokeLater(() -> {
		                if (type == 44 && success) {
		                    lblInfoMsg.setForeground(Color.BLUE);
		                    int userId = buf.getInt();
		                    client.setLastOtherUsers(targetGroup, userId);
		                    lblInfoMsg.setText("Utilisateur ajouté au groupe " + targetGroup);
		                } else {
		                    lblInfoMsg.setForeground(Color.RED);
		                    lblInfoMsg.setText("Échec ajout au groupe");
		                }
		            });
		            break;
		        }

		        case 45: {  
		            int joinedGroup = buf.getInt();
		            SwingUtilities.invokeLater(() ->
		                JOptionPane.showMessageDialog(frame,
		                    "Vous avez été ajouté au groupe " + joinedGroup)
		            );
		            break;
		        }

		        case 46: case 47: {  
		            boolean success = buf.get() != 0;
		            int deletedGroup = buf.getInt();
		            SwingUtilities.invokeLater(() -> {
		                if (type == 46 && success) {
		                    lblInfoMsg.setForeground(Color.BLUE);
		                    lblInfoMsg.setText("Groupe " + deletedGroup + " quitté avec succès");
		                } else {
		                    lblInfoMsg.setForeground(Color.RED);
		                    lblInfoMsg.setText("Échec de quitter le groupe");
		                }
		            });
		            break;
		        }

		        case 48: { 
		            int removedGroup = buf.getInt();
		            SwingUtilities.invokeLater(() ->
		                JOptionPane.showMessageDialog(frame,
		                    "Vous avez quitté le groupe " + removedGroup)
		            );
		            break;
		        }
		    }
		});


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
		System.out.println(var);
	}
	
	public void refresh() {
		//client.getConnectedUsers();
		//client.getGroups();
		//client.getGroupsOwn():
		System.out.println("entrée refresh");
		model.clear();
		Integer[] pouet = {1,2,3};
		connectedUsers = pouet;
		//connectedUsers = client.getConnectedUsers();
		for (int i = 0; i < connectedUsers.length ; i += 1) {
			model.addElement(connectedUsers[i]);
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
		
		JLabel lbltitle = new JLabel("Bienvenue sur le chat !");
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
                
                
                System.out.println(nbMembers);
                if (nbMembers < 1) {
                	lblInfoMsg.setForeground(Color.RED);
                	lblInfoMsg.setText("Vous ne pouvez pas être seul.e dans un groupe");
                } else {
                	
                	 try {
                         client.sendCreateGroup(membersTab);
                         
                     } catch (SQLException ex) {
                         lblInfoMsg.setForeground(Color.RED);
                         lblInfoMsg.setText("Erreur d’envoi de la requête");
                     }

                }
            }
        });
		
		
		
		// -------------------  ADD TO GROUP ---------------------------//

		JButton btnAddToGroup = new JButton("Ajouter à un groupe");
		btnAddToGroup.addActionListener(e -> {
		    try {
		        client.requestGroupsOwned();
		    } catch (SQLException ex) {
		        lblInfoMsg.setForeground(Color.RED);
		        lblInfoMsg.setText("Erreur de requête des groupes");
		        return;
		    }

		
		    StringBuilder sbGroups = new StringBuilder();
		    int nombreDeGroupe=0;
		    for (int i=0; i<client.getLastOwnedGroups().length;i++) {
		    	if (client.getLastOwnedGroups()[i]!= null) {
		    		nombreDeGroupe++;
		    	}
		    }
		    if (client.getLastOwnedGroups() != null && client.getLastOwnedGroups().length > 0) {
		        for (int i=0; i<nombreDeGroupe;i++) {
		        	 sbGroups.append(client.getLastOwnedGroups()[i]).append(", ");
		        	 sbGroups.setLength(sbGroups.length() - 2);
		        }
		    	
		    } else {
		        lblInfoMsg.setForeground(Color.RED);
		        lblInfoMsg.setText("Vous n'administrez aucun groupe");
		        return;
		    }

		    String inputGroup = JOptionPane.showInputDialog(
		        frame,
		        "Vous êtes admin des groupes : " + sbGroups + "\nEntrez l'ID du groupe :",
		        null
		    );
		    if (inputGroup == null || inputGroup.isBlank()) return;
		    int groupId = Integer.parseInt(inputGroup.trim());

		    try {
		        client.requestUsersNotInGroup(groupId);
		    } catch (SQLException ex) {
		        lblInfoMsg.setForeground(Color.RED);
		        lblInfoMsg.setText("Erreur de requête des utilisateurs");
		        return;
		    }

		    StringBuilder sbUsers = new StringBuilder();
		    int[] members = client.getLastOtherUsers().get(groupId);
		    
		    if (members != null && members.length > 0) {
		        for (Integer uid : members) {
		            sbUsers.append(uid).append(", ");
		        }
		        sbUsers.setLength(sbUsers.length() - 2);
		    } else {
		        lblInfoMsg.setForeground(Color.RED);
		        lblInfoMsg.setText("Aucun utilisateur à ajouter");
		        return;
		    }

		    String inputUser = JOptionPane.showInputDialog(
		        frame,
		        "Utilisateurs disponibles : " + sbUsers + "\nEntrez l'ID de l'utilisateur :",
		        null
		    );
		    if (inputUser == null || inputUser.isBlank()) return;
		    int userId = Integer.parseInt(inputUser.trim());

		    try {
		        client.sendAddUserToGroup(groupId, userId);
		    } catch (SQLException ex) {
		        lblInfoMsg.setForeground(Color.RED);
		        lblInfoMsg.setText("Erreur d’envoi de la requête d’ajout");
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
	    Integer[] pouet = {1,2,3};
	    connectedUsers = pouet;
	    System.out.println(connectedUsers);
	    for (Integer s : connectedUsers) {
	      model.addElement(s);
	    }
		JList<Integer> list = new JList<>(model);
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				selectedUser = list.getSelectedValue();
				System.out.println(selectedUser);
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
		btnLeaveGroup.addActionListener(e -> {
		    try {
		        client.requestGroupsToLeave();
		    } catch (SQLException ex) {
		        lblInfoMsg.setForeground(Color.RED);
		        lblInfoMsg.setText("Erreur lors de la requête des groupes");
		        return;
		    }

		    Integer[] myGroups = client.getListGroupeNonOwner();
		    if (myGroups == null || myGroups.length == 0) {
		        lblInfoMsg.setForeground(Color.RED);
		        lblInfoMsg.setText("Vous n'appartenez à aucun groupe");
		        return;
		    }

		    StringBuilder sb = new StringBuilder();
		    for (Integer gid : myGroups) {
		        sb.append(gid).append(", ");
		    }
		    sb.setLength(sb.length() - 2);

		    String input = JOptionPane.showInputDialog(
		        frame,
		        "Vous appartenez aux groupes : " + sb + "\nQuel groupe voulez-vous quitter ?",
		        null
		    );
		    if (input == null || input.isBlank()) return;

		    int groupToLeave = Integer.parseInt(input.trim());

		    try {
		        client.sendDeleteUserToGroup(groupToLeave, client.getIdentifier());
		    } catch (SQLException ex) {
		        lblInfoMsg.setForeground(Color.RED);
		        lblInfoMsg.setText("Erreur d’envoi de la requête");
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
				System.out.println(deleteGroup);
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
						System.out.println("user : " +selectedUser);
						String message = txtAOutbox.getText();
						System.out.println("Dest : " + dest + ", Msg : " + message);
						
						client.sendMessage(dest, message);
						System.out.println(txtAInbox.getText().length());
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
