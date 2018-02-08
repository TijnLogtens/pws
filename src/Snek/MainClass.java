package Snek;

import java.awt.Dimension;

import javax.swing.JFrame;

public class MainClass {

	public static void main(String[] args) {
		
		//Program window
		JFrame window = new JFrame("Snek");
		
		window.setContentPane(new GamePanel());
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setResizable(false);
		window.pack();
		window.setPreferredSize(new Dimension(GamePanel.WIDTH, GamePanel.HEIGHT));
		window.setLocationRelativeTo(null);
		window.setVisible(true);
		
	}

}
