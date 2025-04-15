package fr.uga.miashs.dciss.chatservice.client;

import java.io.File;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Scanner;

public class ClientDB {
	private Connection connection;
	private String pseudo;

	public ClientDB(String pseudo) throws SQLException {
		this.pseudo = pseudo;
		String dbPath = "db/user_" + pseudo + ".db";
		connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
		createTables();
	}

	private void createTables() throws SQLException {
		Statement stmt = connection.createStatement();

		// Création de la table Messages si elle n'existe pas déjà.
		String createMessagesTable = "CREATE TABLE IF NOT EXISTS Messages (" + 
		"ID INTEGER PRIMARY KEY AUTOINCREMENT, "
		+ "senderID INTEGER, " 
		+ "contenu TEXT, " 
		+ "group_id INTEGER, " 
		+ "recipient_id INTEGER "
		+ ");";
		stmt.executeUpdate(createMessagesTable);

	}

	public void insertionMessage(int senderID, String contenu, int group_id, int recipient_id) throws SQLException {
		String insertSQL = "INSERT INTO Messages (senderID, contenu, group_id, recipient_id) VALUES (?, ?, ?, ?)";
		PreparedStatement pstmt = connection.prepareStatement(insertSQL);
		pstmt.setInt(1, senderID);
		pstmt.setString(2, contenu);
		pstmt.setInt(3, group_id);
		pstmt.setInt(4, recipient_id);
		
		pstmt.executeUpdate();
	}

	public static void initialiserClient(String username) {
		try {
			ClientDB clientDB = new ClientDB(username);

			// Vérifier si le fichier existe
			File dbFile = new File("db/user_" + username + ".db");
			if (dbFile.exists()) {
				System.out.println("Base de données déjà existante à : " + dbFile.getAbsolutePath());
			} else {
				System.out.println("Base de données non trouvée ou non créée");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		System.out.println("Entrez votre pseudo : ");
		String username = sc.nextLine();
		initialiserClient(username);

	}
}
