/*
 * Copyright (c) 2024.  Jerome David. Univ. Grenoble Alpes.
 * This file is part of DcissChatService.
 *
 * DcissChatService is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * DcissChatService is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.uga.miashs.dciss.chatservice.client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


import fr.uga.miashs.dciss.chatservice.common.Packet;
import fr.uga.miashs.dciss.chatservice.client.MessageListener;

/**
 * Manages the connection to a ServerMsg. Method startSession() is used to
 * establish the connection. Then messages can be send by a call to sendPacket.
 * The reception is done asynchronously (internally by the method receiveLoop())
 * and the reception of a message is notified to MessagesListeners. To register
 * a MessageListener, the method addMessageListener has to be called. Session
 * are closed thanks to the method closeSession().
 */
public class ClientMsg {

	private String serverAddress;
	private int serverPort;
	private Integer[] connectedUsers;
	private Integer[] ListgroupeNonOwner;
	private Socket s;
	private DataOutputStream dos;
	private DataInputStream dis;
	private String lastServerAnswer;
	
	private ClientDB clientDB;
	private String username;

	private int identifier;
	private String pseudo;

	private List<MessageListener> mListeners;
	private List<ConnectionListener> cListeners;

	/**
	 * Create a client with an existing id, that will connect to the server at the
	 * given address and port
	 * 
	 * @param id      The client id
	 * @param address The server address or hostname
	 * @param port    The port number
	 * @throws SQLException 
	 */
	public ClientMsg(int id, String address, int port) throws SQLException {
		if (id < 0)
			throw new IllegalArgumentException("id must not be less than 0");
		if (port <= 0)
			throw new IllegalArgumentException("Server port must be greater than 0");
		serverAddress = address;
		serverPort = port;
		identifier = id;
		mListeners = new ArrayList<>();
		cListeners = new ArrayList<>();
		try {
			startSession();
		} catch (UnknownHostException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		addMessageListener(p -> {
//			System.out.println("suivi bug");
//			byte type = p.data[0];
//			if (type==21 || type ==31) {
//				ByteBuffer buf =ByteBuffer.wrap(p.data);
//				buf.get();
//				int taille = buf.getInt();
//				connectedUsers = new Integer[taille]; 
//				for (int i =0; i<taille; i++) {
//					connectedUsers[i] = buf.getInt();
//					System.out.print(connectedUsers[i]);
//				}
//			} else
//			
//				System.out.println(p.srcId + " says to " + p.destId + ": " + new String(p.data));
//		
//
//	});
//		addConnectionListener(active ->  {if (!active) System.exit(0);});
//
//		try {
//			startSession();
//		} catch (UnknownHostException | SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		System.out.println("Vous êtes : " + getIdentifier());
		
	}
	
	public void setConnectedUsers(int taille) {
		connectedUsers = new Integer[taille]; 
	}
	public void setListgroupeNonOwner(int taille) {
		ListgroupeNonOwner = new Integer[taille];
	}
	
	public void formatagePacket(Packet p) {
		System.out.println("entrée formatage");
		String resultat = null;
		byte type = p.data[0];
		if (type==21) {
			ByteBuffer buf =ByteBuffer.wrap(p.data);
			buf.get();
			int taille = buf.getInt();
			setConnectedUsers(taille);
			resultat = "Vous avez bien recu le nombre d'utilisateurs connectes";
		}
		else if (type ==31) {
			ByteBuffer buf =ByteBuffer.wrap(p.data);
			buf.get();
			int taille = buf.getInt();
			setListgroupeNonOwner(taille);
			resultat = "Vous avez bien recu les groupe de l utilisateurs";
		}
		else if (type ==41) resultat = "groupe cree avec success";
		else if (type ==42) resultat="probleme lors de la creation du groupe";
		else if (type == 43) resultat = "Vous n'avez pas l'autorisation d'ajouter un membre dans le groupe";
		else if (type == 44) resultat = "Membre ajoute dans le groupe avec success";
		else {
			
		}
		
		
		this.lastServerAnswer = resultat;
	}

	/**
	 * Create a client without id, the server will provide an id during the the
	 * session start
	 * 
	 * @param address The server address or hostname
	 * @param port    The port number
	 * @throws SQLException 
	 */
	public ClientMsg(String address, int port) throws SQLException {
		this(0, address, port);
	}

	/**
	 * Register a MessageListener to the client. It will be notified each time a
	 * message is received.
	 * 
	 * @param l
	 */
	public void addMessageListener(MessageListener l) {
		if (l != null)
			mListeners.add(l);
	}
	protected void notifyMessageListeners(Packet p) {
		
		mListeners.forEach(x -> x.messageReceived(p));
	}
	
	/**
	 * Register a ConnectionListener to the client. It will be notified if the connection  start or ends.
	 * 
	 * @param l
	 */
	public void addConnectionListener(ConnectionListener l) {
		if (l != null)
			cListeners.add(l);
	}
	protected void notifyConnectionListeners(boolean active) {
		cListeners.forEach(x -> x.connectionEvent(active));
	}


	public int getIdentifier() {
		return identifier;
	}

	/**
	 * Method to be called to establish the connection.
	 * 
	 * @throws UnknownHostException
	 * @throws SQLException 
	 * @throws IOException
	 */
	public void startSession() throws UnknownHostException, SQLException {
		if (s == null || s.isClosed()) {
			System.out.println(s);
			try {
				s = new Socket(serverAddress, serverPort);
				System.out.println(s);
				dos = new DataOutputStream(s.getOutputStream());
				dis = new DataInputStream(s.getInputStream());
				dos.writeInt(identifier);
				System.out.println(dis);
				System.out.println(dos);
				dos.flush();
				if (identifier == 0) {
					identifier = dis.readInt();
				}
//				 Scanner sc = new Scanner(System.in);
//		            System.out.print("Entrez votre pseudo : ");
//		            String pseudo = sc.nextLine();
		            this.username = "test";

		            // Crée la base avec pseudo et ID. 
		            this.clientDB = new ClientDB(identifier);

				// start the receive loop
				new Thread(() -> {
					try {
						receiveLoop();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}).start();
				notifyConnectionListeners(true);
			} catch (IOException e) {
				e.printStackTrace();
				// error, close session
				closeSession();
			}
		}
	}

	public String createGroup(int identifier, int[] members) {
		//Création du paquet
		//envoie du paquet
		//réception de la réponse
		//stockage de la réponse
		//return réponse.
	}
	/**
	 * Send a packet to the specified destination (etiher a userId or groupId)
	 * 
	 * @param destId the destinatiion id
	 * @param data   the data to be sent
	 * @throws SQLException 
	 */
	public void sendPacket(int destId, byte[] data) throws SQLException {
		try {
			synchronized (dos) {
				dos.writeInt(destId);
				dos.writeInt(data.length);
				dos.write(data);
				dos.flush();
			}
			
		// Enregistrement du message envoyé ?????????????
	        clientDB.insertionMessage(identifier, new String(data), 0, destId);
	        
	        
		} catch (IOException e) {
			// error, connection closed
			closeSession();
		}
		
	}
	
	
	public Integer[] getConnectedUsers() {
		this.createRequestConnectedUsersData(identifier);
		return connectedUsers;
	}
	public byte[] addUserToGroup(int groupId, int userId) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeByte(3);
			dos.writeInt(userId);
			dos.writeInt(groupId);
			dos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bos.toByteArray();
	}
	public byte[] deleteUserToGroup(int groupId, int userId) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeByte(4);
			dos.writeInt(userId);
			dos.writeInt(groupId);
			dos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bos.toByteArray();
	}
	public void createRequestConnectedUsersData(int resquester) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		System.out.println("ICI ");
		try {
			dos.writeByte(20);
			dos.writeInt(identifier);
			dos.flush();
			 sendPacket(0, bos.toByteArray());
			for(int i =0; i< bos.toByteArray().length;i++)
				System.out.println(bos.toByteArray()[i]);
		} catch (IOException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public byte[] listGroupeNonOwner(int resquester) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeByte(30);
			dos.writeInt(resquester);
			dos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return bos.toByteArray();
	}
	public byte[] createGroupData(int OwnerId, int[] members) {
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		int i =0;
		for(; i<members.length;i++) {
			if (!(members[i]>0))
				break;
		}
		try {
			dos.writeByte(1);
			dos.writeInt(OwnerId);
			dos.writeInt(i+1);
			dos.writeInt(OwnerId);
			for (int j =0; j< i;j++) {
				dos.writeInt(members[j]);
			}
			dos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bos.toByteArray();
	}
	

	/**
	 * Start the receive loop. Has to be called only once.
	 * @throws SQLException 
	 */
	private void receiveLoop() throws SQLException {
		try {
			while (s != null && !s.isClosed()) {

				int sender = dis.readInt();
				int dest = dis.readInt();
				int length = dis.readInt();
				byte[] data = new byte[length];
				dis.readFully(data);
				
				clientDB.insertionMessage(sender, new String(data), 0, dest);

				notifyMessageListeners(new Packet(sender, dest, data));

			}
		} catch (IOException e) {
			// error, connection closed
		}
		closeSession();
	}
	



	public void closeSession() {
		try {
			if (s != null)
				s.close();
		} catch (IOException e) {
		}
		s = null;
		notifyConnectionListeners(false);
	}
	
	protected void sendMessage(int destination, String message) {
		String lu = null;
		if (!"\\quit".equals(lu)) {
			try {
				int dest = destination;

				lu = message;
				this.sendPacket(dest, lu.getBytes());
			} catch (InputMismatchException | NumberFormatException e) {
				System.out.println("Mauvais format");
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	// Méthode ajoutée pour afficher l'historique basée sur getMessagesArrayBetween
	public String[] afficherHistoriqueTableau(int autreId) {
	    try {
	        // Appel de la méthode pour récupérer le tableau de messages entre l'utilisateur courant et autreId
	        String[] historique = clientDB.getMessagesArrayBetween(this.identifier, autreId);
	        System.out.println("Historique des messages avec l'utilisateur " + autreId + " :");
	        for (String msg : historique) {
	            System.out.println(msg);
	        }
	        return historique;
	    } catch (SQLException e) {
	        System.out.println("Erreur lors de la récupération de l'historique : ");
	        e.printStackTrace();
	        String[] pouet = {"1","2"};
	        return pouet;
	    }
	}


	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException, SQLException {
		
		ClientMsg c = new ClientMsg("localhost", 1666);
		System.out.println("before");
		 c.createRequestConnectedUsersData(c.getIdentifier());
		System.out.println("after");
		Scanner sc = new Scanner(System.in);
		String lu = null;
		while (!"\\quit".equals(lu)) {
			try {
				System.out.println("A qui voulez vous écrire ? ");
				int dest = Integer.parseInt(sc.nextLine());

				System.out.println("Votre message ? ");
				lu = sc.nextLine();
				c.sendPacket(dest, lu.getBytes());
			} catch (InputMismatchException | NumberFormatException e) {
				System.out.println("Mauvais format");
				
			}
			c.closeSession();
		
		 // add a dummy listener that print the content of message as a string
//		c.addMessageListener(p -> {
//			byte type = p.data[0];
//			if (type==21 ) {
//				ByteBuffer buf =ByteBuffer.wrap(p.data);
//				buf.get();
//				int taille = buf.getInt();
//				for (int i =0; i<taille; i++) {
//					System.out.print(" -ID: "+ buf.getInt());
//				}
//			} else
//			
//				System.out.println(p.srcId + " says to " + p.destId + ": " + new String(p.data));
//		});
		
		// add a connection listener that exit application when connection closed
//		c.addConnectionListener(active ->  {if (!active) System.exit(0);});
//
//		c.startSession();
//		
//		System.out.println("Vous êtes : " + c.getIdentifier());

		// Thread.sleep(5000);

		// l'utilisateur avec id 4 crée un grp avec 1 et 3 dedans (et lui meme)
//		if (c.getIdentifier() == 4) {
//			ByteArrayOutputStream bos = new ByteArrayOutputStream();
//			DataOutputStream dos = new DataOutputStream(bos);
//
//			// byte 1 : create group on server
//			dos.writeByte(1);
//
//			// nb members
//			dos.writeInt(2);
//			// list members
//			dos.writeInt(1);
//			dos.writeInt(3);
//			dos.flush();
//
//			c.sendPacket(0, bos.toByteArray());
//
//		}
//		Scanner scGroupe = new Scanner(System.in);
//		int lg;
//		System.out.println("Quelle est la taille du groupe ? ");
//		lg=Integer.parseInt(scGroupe.nextLine());
//		int[] users = new int[lg];
//		int i = 0;
//		while (i<lg) {
//			try {
//				System.out.println("Qui fait partie du groupe ? ");
//				int user = Integer.parseInt(scGroupe.nextLine());
//				users[i]=user;
//				i++;
//			} catch (InputMismatchException | NumberFormatException e) {
//				System.out.println("Mauvais format");
//			}
//
//		}
//		
//	
//		
//		byte[] data = c.createGroupData(c.getIdentifier(), users);
//		
//		c.sendPacket(0,data);
//		System.out.println("les utilisateurs connectes ");
//		byte[] data2 =  c.createRequestConnectedUsersData(c.getIdentifier());
//
//		c.sendPacket(0,data2);
//		System.out.println("Quel est l ID du client que vous voulez ajouter a un groupe ");
//		int user = Integer.parseInt(scGroupe.nextLine());
//		System.out.println("Quel est l ID du groupe en question ");
//		int groupe = Integer.parseInt(scGroupe.nextLine());
//		byte[] data3 =  c.addUserToGroup(groupe, user);
//		c.sendPacket(0,data3);
//		byte[] data4 =  c.deleteUserToGroup(groupe, user);
//		c.sendPacket(0,data4);

//			
//			// TEST affichage de l'historique avec l'utilisateur d'ID 3
//			System.out.println("\n== Affichage de l'historique en tableau ==");
//			c.afficherHistoriqueTableau(3);  
//
//
//		}

		/*
		 * int id =1+(c.getIdentifier()-1) % 2; System.out.println("send to "+id);
		 * c.sendPacket(id, "bonjour".getBytes());
		 * 
		 * 
		 * Thread.sleep(10000);
		 */


	}
	}
}