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

package fr.uga.miashs.dciss.chatservice.server;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import fr.uga.miashs.dciss.chatservice.common.Packet;

public class ServerPacketProcessor implements PacketProcessor {
	private final static Logger LOG = Logger.getLogger(ServerPacketProcessor.class.getName());
	private ServerMsg server;

	public ServerPacketProcessor(ServerMsg s) {
		this.server = s;
	}

	@Override
	public void process(Packet p) {
		// ByteBufferVersion. On aurait pu utiliser un ByteArrayInputStream + DataInputStream à la place
		ByteBuffer buf = ByteBuffer.wrap(p.data);
		byte type = buf.get();
		
//		if (type == 1) { // cas creation de groupe
//			createGroup(p.srcId,buf);
//		} else {
//			LOG.warning("Server message of type=" + type + " not handled by procesor");
//		}
		
		switch (type) {
		case 1:
			createGroup(p.srcId,buf);
			break;
		
		case 3:
			int clientRequete = p.srcId;
			int idUser = buf.getInt();
			int idGroupe = buf.getInt();
			ajoutMembreGroupe(clientRequete,idUser, idGroupe);
			break;
		case 4:
			clientRequete = p.srcId;
			int idU = buf.getInt();
			int idG =  buf.getInt();
			deleteMembreGroupe(clientRequete,idU,idG);
			break;
		case 20:
			int requester = buf.getInt();
			System.out.println("request" + requester);
			requestConnectedUsers(requester);
			break;
		case 30:
			requester = buf.getInt();
			ListGroupNonOwner(requester);
			break;
		case 2:
		    int gid = buf.getInt();
		    boolean ok = server.removeGroup(gid);
		    break;
		case 33: {
	        requester = buf.getInt();
	        requestGroupsOwned(requester);
	        break;
	      }
	      case 35: {
	        requester = buf.getInt();
	        int groupId   = buf.getInt();
	        requestUsersNotInGroup(requester, groupId);
	        break;
	      }
		default:
			erreurs(type);
		}
	}
	
	public void createGroup(int ownerId, ByteBuffer data) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		UserMsg requester = server.getUser(ownerId);
		
		try {
			 int nb = data.getInt();                      
			    GroupMsg g = server.createGroup(ownerId);   
			    for (int k = 0; k < nb; k++) {
			        int memberId = data.getInt();
			        g.addMember(server.getUser(memberId));
			    }
			
			dos.writeByte(41);
			dos.writeBoolean(true);
			dos.writeInt(g.getId());
			dos.flush();
			
		} catch (Exception e) {
			try {
				dos.writeByte(42);
				dos.writeBoolean(false);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
		requester.process(new Packet(ownerId, ownerId, bos.toByteArray()));
		
		
	}
	
	public void ajoutMembreGroupe(int ownerId, int idUser, int idGroupe) {
	    GroupMsg g = server.getGroup(idGroupe);
	    UserMsg requester = server.getUser(ownerId);
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    DataOutputStream dos = new DataOutputStream(bos);

	    try {
	        if (ownerId != g.getOwner().getId()) {
	            dos.writeByte(43);       
	            dos.writeBoolean(false);
	            dos.flush();
	            requester.process(new Packet(ownerId, ownerId, bos.toByteArray()));
	            return;
	        }

	        g.addMember(server.getUser(idUser));

	        dos.writeByte(44);         
	        dos.writeBoolean(true);
	        dos.writeInt(idGroupe);
	        dos.writeInt(idUser);
	        dos.flush();

	        UserMsg newUser = server.getUser(idUser);
	        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
	        DataOutputStream dos2      = new DataOutputStream(bos2);
	        dos2.writeByte(45);          
	        dos2.writeInt(idGroupe);
	        dos2.flush();
	        newUser.process(new Packet(ownerId, idUser, bos2.toByteArray()));

	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    
	    requester.process(new Packet(ownerId, ownerId, bos.toByteArray()));
	}
	
	public void deleteMembreGroupe(int ownerId, int idUser, int idGroupe) {
	    GroupMsg g = server.getGroup(idGroupe);
	    UserMsg requester = server.getUser(ownerId);
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    DataOutputStream dos = new DataOutputStream(bos);

	    try {
	        if (ownerId == g.getOwner().getId()) {
	            dos.writeByte(47);      
	            dos.writeBoolean(false);
	        } else {
	            boolean removed = g.removeMember(server.getUser(idUser));
	            if (removed) {
	                dos.writeByte(46);  
	                dos.writeBoolean(true);

	                UserMsg removedUser = server.getUser(idUser);
	                ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
	                DataOutputStream dos2 = new DataOutputStream(bos2);
	                dos2.writeByte(48);        
	                dos2.writeInt(idGroupe);
	                removedUser.process(new Packet(ownerId, idUser, bos2.toByteArray()));
	            } else {
	                dos.writeByte(47);
	                dos.writeBoolean(false);
	            }
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    requester.process(new Packet(ownerId, ownerId, bos.toByteArray()));
	}

	public void requestConnectedUsers(int requestID) {
		
		Collection <UserMsg> clients = server.getUsers().values();
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		System.out.print("ici");
		try {
			dos.writeByte(21);
			dos.writeInt(clients.size());
			for (UserMsg u:clients) {
				if (u.isConnected())
					dos.writeInt(u.getId());
			}
			UserMsg requester = server.getUser(requestID);
			requester.process(new Packet(requestID, requestID, bos.toByteArray()));
			System.out.println("sortie server");
			for (int i  = 0; i < bos.toByteArray().length ; i += 1) {
			System.out.println(bos.toByteArray()[i]);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void ListGroupNonOwner(int requester) {
		Collection <GroupMsg> groupes = server.getGroups().values();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		UserMsg client = server.getUser(requester);
		try {
			dos.writeByte(31);
			dos.writeInt(groupes.size());
			
			for(GroupMsg g: groupes) {
				Set<UserMsg> members = g.getMembers();
				if (members.contains(client) && g.getOwner().getId()!=requester)
					dos.writeInt(g.getId());
			}
			
			client.process(new Packet(requester, requester, bos.toByteArray()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void requestGroupsOwned(int requesterId) {
	    UserMsg u = server.getUser(requesterId);
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    DataOutputStream dos = new DataOutputStream(bos);
	    try {
	        dos.writeByte(34);                      
	        List<Integer> owned = new ArrayList<>();
	        for (GroupMsg g : server.getGroups().values()) {
	            if (g.getOwner().getId() == requesterId) {
	                owned.add(g.getId());
	            }
	        }
	        dos.writeInt(owned.size());
	        for (int gid : owned) dos.writeInt(gid);
	    } catch (IOException ex) {}
	    u.process(new Packet(requesterId, requesterId, bos.toByteArray()));
	}

	
	public void requestUsersNotInGroup(int requesterId, int groupId) {
	    UserMsg u = server.getUser(requesterId);
	    GroupMsg g = server.getGroup(groupId);
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    DataOutputStream dos = new DataOutputStream(bos);
	    try {
	        dos.writeByte(36);
	        dos.writeInt(groupId);
	        Set<UserMsg> members = g.getMembers();
	        List<Integer> others = new ArrayList<>();
	        for (UserMsg x : server.getUsers().values()) {
	            if (x.isConnected() && !members.contains(x)) {
	                others.add(x.getId());
	            }
	        }
	        System.out.println("Server: group " + groupId +
		    	    " others=" + others);
	        dos.writeInt(others.size());
	        for (int uid : others) dos.writeInt(uid);
	    } catch (IOException ex) {}
	    
	    u.process(new Packet(requesterId, requesterId, bos.toByteArray()));
	}
	public String erreurs(byte t) {
		String s = null;
		if (t==10) {
			s = "Erreur lors de la creation de l utilisateur";
		}
		switch(t) {
		case 10: 
			s = "Erreur lors de la creation de l utilisateur";
			break;
		case 11:
			s = "Erreur lors d'ajout du client dans un groupe";
			break;
		case 12:
			s = "Erreur lors de la suppresion d 1 utilisateur a un groupe";
			break;
			
		}
		
		return s;
	}
	
	

}
