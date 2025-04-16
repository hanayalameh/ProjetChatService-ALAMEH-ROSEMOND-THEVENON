package fr.uga.miashs.dciss.chatservice.client;

import java.io.File;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
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
				System.out.println("Base de données existante à : " + dbFile.getAbsolutePath());
			} else {
				System.out.println("Base de données non trouvée ou non créée");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public List<String> getMessagesBetween(int personne1, int personne2) throws SQLException {
        List<String> messages = new ArrayList<>();

        String query = """
            SELECT ID, senderID, recipient_id, contenu
            FROM Messages
            WHERE (senderID = ? AND recipient_id = ?)
               OR (senderID = ? AND recipient_id = ?)
            ORDER BY ID ASC
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, personne1);
            pstmt.setInt(2, personne2);
            pstmt.setInt(3, personne2);
            pstmt.setInt(4, personne1);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int sender = rs.getInt("senderID");
                    int recipient = rs.getInt("recipient_id");
                    String content = rs.getString("contenu");
                    messages.add("Vous " + sender + " à Lui/Elle " + recipient + " : " + content);
                }
            }
        }
        return messages;
    }
	
	public String[] getMessagesArrayBetween(int personne1, int personne2) throws SQLException {
	    // Récupération de la liste existante
	    List<String> messages = getMessagesBetween(personne1, personne2);
	    // Conversion de la liste en tableau
	    return messages.toArray(new String[0]);
	}

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		System.out.println("Entrez votre pseudo : ");
		String username = sc.nextLine();
		initialiserClient(username);

	}
}