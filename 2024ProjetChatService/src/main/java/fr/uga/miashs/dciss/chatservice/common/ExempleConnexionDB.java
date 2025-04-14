package fr.uga.miashs.dciss.chatservice.common;

import java.sql.*;

public class ExempleConnexionDB {

    public static void main(String[] args) {

        try {
            // Connexion à la base AppChat.db (fichier placé à la racine du projet)
            String url = "jdbc:sqlite:AppChat.db";
            Connection cnx = DriverManager.getConnection(url);

            // Insertion d'un message dans la table Messages
            String insertSQL = "INSERT INTO Messages (ID, senderID, contenu, group_id, recipient_id) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = cnx.prepareStatement(insertSQL);

            pstmt.setInt(2, 1);  // ID du message
            pstmt.setInt(2, 101);  // senderID
            pstmt.setString(3, "Salut à tous, c-est Hanay !");  // contenu
            pstmt.setInt(4, 1);  // group_id
            pstmt.setInt(5, 202);  // recipient_id
            
            // A faire : voir s-il y a probleme avec l-ID du message

			boolean inserted = pstmt.executeUpdate()==1;
			
 
            // Lecture des messages
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


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
