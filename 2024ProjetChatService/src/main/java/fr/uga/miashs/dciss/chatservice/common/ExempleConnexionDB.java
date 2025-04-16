package fr.uga.miashs.dciss.chatservice.common;

import java.sql.*;

public class ExempleConnexionDB {

    public static void main(String[] args) {

        try {
            // Connexion à la base AppChat.db (fichier à la racine du projet)
            String url = "jdbc:sqlite:AppChat.db";
            Connection cnx = DriverManager.getConnection(url);
            
            String createUsersTable = """
                    CREATE TABLE IF NOT EXISTS Users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        pseudo TEXT UNIQUE NOT NULL,
                        password TEXT NOT NULL
                    );
                """;
            cnx.createStatement().executeUpdate(createUsersTable);

            // Création (si nécessaire) de la table Messages pour cet exemple
            String createMessagesTable = """
                    CREATE TABLE IF NOT EXISTS Messages (
                        ID INTEGER PRIMARY KEY AUTOINCREMENT,
                        senderID INTEGER,
                        contenu TEXT,
                        group_id INTEGER,
                        recipient_id INTEGER
                    );
                """;
            cnx.createStatement().executeUpdate(createMessagesTable);

            // Insertion d'un message sans préciser l'ID (auto-incrémenté)
            String insertSQL = "INSERT INTO Messages (senderID, contenu, group_id, recipient_id) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = cnx.prepareStatement(insertSQL);
            pstmt.setInt(1, 101);  // senderID
            pstmt.setString(2, "Salut à tous, c'est Hanay !");  // contenu
            pstmt.setInt(3, 1);    // group_id
            pstmt.setInt(4, 202);  // recipient_id

            boolean inserted = pstmt.executeUpdate() == 1;
            System.out.println("Insertion réussie : " + inserted);
 
            // Lecture des messages de la table Messages
            String selectSQL = "SELECT * FROM Messages";
            ResultSet res = cnx.createStatement().executeQuery(selectSQL);

            System.out.println("Contenu des messages :");
            while (res.next()) {
                System.out.println(
                    "ID: " + res.getInt("ID") +
                    ", Sender: " + res.getInt("senderID") +
                    ", Contenu: " + res.getString("contenu") +
                    ", Group ID: " + res.getInt("group_id") +
                    ", Recipient ID: " + res.getInt("recipient_id")
                );
            }
            cnx.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
