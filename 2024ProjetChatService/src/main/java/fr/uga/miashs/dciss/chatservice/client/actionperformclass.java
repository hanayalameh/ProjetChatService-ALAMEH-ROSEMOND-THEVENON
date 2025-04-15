// Java Program that creates the file with the method
// actionPerformed(ActionEvent o) of ActionListener
 
// Importing awt module and Swing class
package fr;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
 
// Class
// An action listener that prints a message
public class actionperformclass implements ActionListener {
 
    // Method
    public void actionPerformed(ActionEvent event)
    {
        // settext of textfield object of Jtextfield
    	lblInfoMsg.setText("button is clicked");
    }
}
