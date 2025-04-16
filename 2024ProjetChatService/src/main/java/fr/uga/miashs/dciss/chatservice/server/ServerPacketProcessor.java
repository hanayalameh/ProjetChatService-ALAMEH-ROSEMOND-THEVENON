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
import java.util.Collection;
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
			requestConnectedUsers(requester);
			break;
		case 30:
			requester = buf.getInt();
			ListGroupNonOwner(requester);
			break;
		default:
			erreurs(type);
		}
	}
	
	public void createGroup(int ownerId, ByteBuffer data) {
		int nb = data.getInt();
		GroupMsg g = server.createGroup(ownerId);
		for (int i = 0; i < nb; i++) {
			g.addMember(server.getUser(data.getInt()));
		}
	}
	
	public void ajoutMembreGroupe(int ownerId,int idUser,int idGroupe) {
		// peux-etre que l on doit faire boolean, ce sera plus facile
		GroupMsg g = server.getGroup(idGroupe);
		if (ownerId!= g.getOwner().getId())
			{LOG.info("Permission Denied ");
			return;}
		System.out.println("ICI 2");
		g.addMember(server.getUser(idUser));
		LOG.info("User "+ idUser+ "a ete ajoute au groupe" + g.getId());
		
	}
	
	public void deleteMembreGroupe(int ownerId,int idUser,int idGroupe) {
		// peux-etre que l on doit faire boolean, ce sera plus facile
		GroupMsg g = server.getGroup(idGroupe);
		if (ownerId == g.getOwner().getId())
			{LOG.info("Vous ne pouvez pas quitter un groupe que vous avez cree ");
			return;}
		g.removeMember(server.getUser(idUser));
		LOG.info("User "+ idUser+ "a ete retire au groupe" + g.getId());
		
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
