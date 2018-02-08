package Snek;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.ArrayList;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class GamePanel extends JPanel implements Runnable, KeyListener {
	
	public static final int WIDTH = 640;
	public static final int HEIGHT = 640;
	
	//Render
	private Graphics2D g2d;
	private BufferedImage image;
	
	//Game
	private Thread thread;
	private boolean alive;
	private long tTime;
	int fps_mod = 10000000;
	boolean assume_direct_control;
	boolean rend = true;
	boolean feedback = false;
	
	//Game Objects
	private int SIZE = 20;
	private ArrayList<Entity> snek;
	private Entity head;
	private Entity rabbit;
	private boolean dead;
	
	//Movement
	private int dx, dy;
	
	//AI Control
	int pop_size = 32;
	int mut_chance = 350;
	int elitism = 6;
	int generation = 0;
	int gen10 = 0;
	int idd = 0;
	boolean brand = true;
	
	//AI Snake
	private float s_fitness = 0;
	private int s_dir = 0;
	int s_idd = 0;
	int s_length = 3;
	int s_lifeforce = 400;
	double s_distance = 0;
	
	//AI Stats
	int tot_food = 0;
	int tot_mut = 0;
	float best_fitness = 0;
	int best_length = 0;
	float last_fitness = 0;
	int last_length = 0;
	
	//Analysis Stats
	int iteration_amount = 5;
	int generation_cap = 500;
	int iteration = 0;
	int average_average_fitness = 0;
	int average_fitness = 0;
	int average_average_length = 0;
	int average_length = 0;
	int average_best_fitness = 0;
	int average_best_length = 0;
	
	//AI NN
	int max_weight = 40; //10
	int min_weight = -20; //-5
	double[] inputs = new double[6];
	double[] hidden = new double[4];
	//double[] hidden2 = new double[4];
	//double[] hidden3 = new double[4];
	double[] output = new double[3];
	double[] weights = new double[inputs.length*hidden.length+hidden.length*output.length]; //[6*5+5*3]
	double[][] DNA = new double[pop_size][weights.length];
	double[][] RNA = new double[pop_size][weights.length];
	float[] fit = new float[pop_size];
	int[] best  = new int[pop_size];
	

	//Key Input Player
	private boolean Up, Down, Left, Right;
	
	public GamePanel() {
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setFocusable(true);
		requestFocus();
		addKeyListener(this);
		
	}
	
	public void addNotify() {
		super.addNotify();
		thread = new Thread(this);
		thread.start();
	}
	
	private void setFPS(int FPS){
		tTime = 1000 / FPS;
	}
	
	public void keyPressed(KeyEvent press) {
		int key = press.getKeyCode();
		
		if (key == KeyEvent.VK_UP) Up = true;
		if (key == KeyEvent.VK_DOWN) Down = true;
		if (key == KeyEvent.VK_LEFT) Left = true;
		if (key == KeyEvent.VK_RIGHT) Right = true;
		
	}

	public void keyReleased(KeyEvent press) {
		int key = press.getKeyCode();
		
		if (key == KeyEvent.VK_UP) Up = false;
		if (key == KeyEvent.VK_DOWN) Down = false;
		if (key == KeyEvent.VK_LEFT) Left = false;
		if (key == KeyEvent.VK_RIGHT) Right = false;
		if (key == KeyEvent.VK_SPACE) {
			if (assume_direct_control)
				assume_direct_control = false;
			else if (!assume_direct_control)
				assume_direct_control = true;
		}
		if (key == KeyEvent.VK_ALT) { 
			if (rend)
				rend = false;
			else if (!rend)
				rend = true;
		}
		if (key == KeyEvent.VK_COMMA) { 
			if (feedback)
				feedback = false;
			else if (!feedback)
				feedback = true;
		}
		if (key == KeyEvent.VK_BACK_SPACE) { //ACTIVATE UBERSNAKE SEPHIRAN, DESTROYER OF RABBITS
			weights[0] = 4.824405246979989; weights[1] = 14.732326715450839; weights[2] = 12.641500401363203; 
			weights[3] = 14.92573898838544; weights[4] = -18.954572530688665; weights[5] = -2.553021894394135;
			weights[6] = -19.746902331951368; weights[7] = 19.487502017037606; weights[8] = -8.387772283615899;
			weights[9] = -5.008915388138018; weights[10] = 17.15269748128025; weights[11] = 13.56943119959432;
			weights[12] = -2.6996756519280325; weights[13] = 14.250648647685168; weights[14] = -15.907900279583371;
			weights[15] = -14.925324306510074; weights[16] = -9.830981915304381; weights[17] = -8.774576014101338;
			weights[18] = 16.92688868762177; weights[19] = 16.451243201384322; weights[20] = -18.85461771709131;
			weights[21] = 8.24916428974479; weights[22] = -10.444667530586788; weights[23] = -5.7657162136836035;
			weights[24] = -14.525599799215257; weights[25] = 6.025831026718038; weights[26] = 5.897739283478135;
            weights[27] = 6.463329708307917; weights[28] = -2.4013342174560357; weights[29] = 11.165889595170636;
			weights[30] = 5.010545042995844; weights[31] = -18.417174119050614; weights[32] = -11.696460065682004;
			weights[33] = 12.711746154192042; weights[34] = 3.9767078366606903; weights[35] = -1.0638951750280654;
		}
		if (key == KeyEvent.VK_8) { //Print all nodes and weights
			System.out.println("======================================================================");
			for (int j = 0; j < inputs.length; j++){
				System.out.println("i["+j+"]="+inputs[j]);
			}
			for (int j = 0; j < output.length; j++){
				System.out.println("o["+j+"]="+output[j]);
			}
			for (int j = 0; j < weights.length; j++){
				System.out.println("w["+j+"]="+weights[j]);
			}
			System.out.println("Generation="+generation);
			System.out.println("Best Length="+best_length);
			System.out.println("======================================================================");
		}
		
	}

	public void keyTyped(KeyEvent arg0) {
	}

	public void run() {
		if(alive) 
			return;
		init();
		
		long sTime;
		long pTime;
		long delay;
		
		while(alive){
			sTime = System.nanoTime();
			
			setFPS(fps_mod);
			update();
			if (rend)
				requestRender();
			
			pTime = System.nanoTime() - sTime;
			delay = tTime - pTime / 1000000;
			if(delay > 0){
				try{
					Thread.sleep(delay);
				}
				catch(Exception u){
					u.printStackTrace();
				}
			}
		}
	}
	
	private void init() {
		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
		g2d = image.createGraphics();
		alive = true;
		levelSetUp();
		dead = false;
		setFPS(fps_mod);
		
		for (int j = 0; j < pop_size; j++) {
			for (int k = 0; k < weights.length; k++) {
				DNA[j][k] = min_weight + (double)(Math.random() * max_weight);
				RNA[j][k] = -999;
			}
		}
		
		for(int j = 0; j<weights.length;j++) {
			weights[j] = DNA[s_idd][j];
		}
		
	}
	
	private void levelSetUp(){
		snek = new ArrayList<Entity>();
		head = new Entity(SIZE);
		snek.add(head);
		head.setPosition(WIDTH/2, HEIGHT/2);
		
		for(int i = 1; i < s_length; i++){
			Entity s = new Entity(SIZE);
			s.setPosition(head.getX() - (i * SIZE), head.getY());
			snek.add(s);
		}
		rabbit = new Entity(SIZE);
		setRabbit();
	}
	
	//Rabbit randomizer
	public void setRabbit(){
		boolean first = true;
		do {
			int x = (int)(Math.random() * (WIDTH));
			int y = (int)(Math.random() * (HEIGHT));
			
			x = x - (x % SIZE);
			y = y - (y % SIZE);
			
			rabbit.setPosition(x, y);
			first = false;
		} while (checkOverlap() == true || first == true);
	}
	
	public boolean checkOverlap() {
		for(Entity s : snek){
			if(s.isCollision(rabbit)){
				return true;
			}
		}
		return false;
	}
	
	private void requestRender() {
		render(g2d);
		Graphics graphics = getGraphics();
		graphics.drawImage(image, 0, 0, null);
		graphics.dispose();
		
	}
	
	public void exportDNA() {
		
		PrintWriter writer;
		try {
			writer = new PrintWriter("snake_dna_"+best_length+"L.txt", "UTF-8");
			writer.println("Weights");
			for (int j = 0; j < weights.length; j++)
				writer.println("w["+j+"] = "+weights[j]);
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
	}
	
	private void update() {
		
		//fitness stuff
		if (Math.sqrt(Math.pow(rabbit.getX()-head.getX(), 2)+Math.pow(rabbit.getY()-head.getY(), 2)) < s_distance) {
			s_fitness += 0.8;
		} else {
			if (s_fitness > 1)
				s_fitness -= 1;
			else
				s_fitness = 1;
		}
		s_distance = Math.sqrt(Math.pow(rabbit.getX()-head.getX(), 2)+Math.pow(rabbit.getY()-head.getY(), 2));
		
		//slowly drain life until starvation.
		s_lifeforce--;
		if (s_lifeforce <= 0)
				dead = true;
		
		//get inputs
		inputs[0] = 1;
		switch(s_dir) { //is FRONT clear?
		case 0: //right
			if(head.getX()+SIZE > WIDTH-SIZE)
				inputs[0] = 0;
			for(Entity s : snek){
				if(s.getX() == head.getX()+SIZE && s.getY() == head.getY()){
					inputs[0] = 0;
					break;
				}
			}
			break;
			
		case 1: //up
			if(head.getY()-SIZE < 0)
				inputs[0] = 0;
			for(Entity s : snek){
				if(s.getX() == head.getX() && s.getY() == head.getY()-SIZE){
					inputs[0] = 0;
					break;
				}
			}
			break;
			
		case 2: //left
			if(head.getX()-SIZE < 0)
				inputs[0] = 0;
			for(Entity s : snek){
				if(s.getX() == head.getX()-SIZE && s.getY() == head.getY()){
					inputs[0] = 0;
					break;
				}
			}
			break;
			
		case 3: //down
			if(head.getY()+SIZE > HEIGHT-SIZE)
				inputs[0] = 0;
			for(Entity s : snek){
				if(s.getX() == head.getX() && s.getY() == head.getY()+SIZE){
					inputs[0] = 0;
					break;
				}
			}
			break;
		
		}
		
		inputs[1] = 1;
		switch(s_dir) { //is LEFT clear?
		case 0: //right
			if(head.getY()-SIZE < 0)
				inputs[1] = 0;
			for(Entity s : snek){
				if(s.getX() == head.getX() && s.getY() == head.getY()-SIZE){
					inputs[1] = 0;
					break;
				}
			}
			break;
			
		case 1: //up
			if(head.getX()-SIZE < 0)
				inputs[1] = 0;
			for(Entity s : snek){
				if(s.getX() == head.getX()-SIZE && s.getY() == head.getY()){
					inputs[1] = 0;
					break;
				}
			}
			break;
			
		case 2: //left
			if(head.getY()+SIZE > HEIGHT-SIZE)
				inputs[1] = 0;
			for(Entity s : snek){
				if(s.getX() == head.getX() && s.getY() == head.getY()+SIZE){
					inputs[1] = 0;
					break;
				}
			}
			break;
			
		case 3: //down
			if(head.getX()+SIZE > WIDTH-SIZE)
				inputs[1] = 0;
			for(Entity s : snek){
				if(s.getX() == head.getX()+SIZE && s.getY() == head.getY()){
					inputs[1] = 0;
					break;
				}
			}
			break;
			
		}
		
		inputs[2] = 1;
		switch(s_dir) { //is RIGHT clear?
		case 0: //right
			if(head.getY()+SIZE > HEIGHT-SIZE)
				inputs[2] = 0;
			for(Entity s : snek){
				if(s.getX() == head.getX() && s.getY() == head.getY()+SIZE){
					inputs[2] = 0;
					break;
				}
			}
			break;
			
		case 1: //up
			if(head.getX()+SIZE > WIDTH-SIZE)
				inputs[2] = 0;
			for(Entity s : snek){
				if(s.getX() == head.getX()+SIZE && s.getY() == head.getY()){
					inputs[2] = 0;
					break;
				}
			}
			break;
			
		case 2: //left
			if(head.getY()-SIZE < 0)
				inputs[2] = 0;
			for(Entity s : snek){
				if(s.getX() == head.getX() && s.getY() == head.getY()-SIZE){
					inputs[2] = 0;
					break;
				}
			}
			break;
			
		case 3: //down
			if(head.getX()-SIZE < 0)
				inputs[2] = 0;
			for(Entity s : snek){
				if(s.getX() == head.getX()-SIZE && s.getY() == head.getY()){
					inputs[2] = 0;
					break;
				}
			}
			break;
			
		}
		
		switch(s_dir) { //is food AHEAD?
		case 0: //right
			if(rabbit.getX() > head.getX())
				inputs[3] = 1;
			else
				inputs[3] = 0;
			break;
		case 1: //up
			if(rabbit.getY() < head.getY())
				inputs[3] = 1;
			else
				inputs[3] = 0;
			break;
		case 2: //left
			if(rabbit.getX() < head.getX())
				inputs[3] = 1;
			else
				inputs[3] = 0;
			break;
		case 3: //down
			if(rabbit.getY() > head.getY())
				inputs[3] = 1;
			else
				inputs[3] = 0;
			break;
		}
		
		switch(s_dir) { //is food to the LEFT?
		case 0: //right
			if(rabbit.getY() < head.getY())
				inputs[4] = 1;
			else
				inputs[4] = 0;
			break;
		case 1: //up
			if(rabbit.getX() < head.getX())
				inputs[4] = 1;
			else
				inputs[4] = 0;
			break;
		case 2: //left
			if(rabbit.getY() > head.getY())
				inputs[4] = 1;
			else
				inputs[4] = 0;
			break;
		case 3: //down
			if(rabbit.getX() > head.getX())
				inputs[4] = 1;
			else
				inputs[4] = 0;
			break;
		}
		
		switch(s_dir) { //is food to the RIGHT?
		case 0: //right
			if(rabbit.getY() > head.getY())
				inputs[5] = 1;
			else
				inputs[5] = 0;
			break;
		case 1: //up
			if(rabbit.getX() > head.getX())
				inputs[5] = 1;
			else
				inputs[5] = 0;
			break;
		case 2: //left
			if(rabbit.getY() < head.getY())
				inputs[5] = 1;
			else
				inputs[5] = 0;
			break;
		case 3: //down
			if(rabbit.getX() < head.getX())
				inputs[5] = 1;
			else
				inputs[5] = 0;
			break;
		}
		
		//process hidden layer
		for(int j = 0; j<hidden.length; j++) {
			hidden[j] = 1;
			for(int k = 0; k<inputs.length; k++)
				hidden[j] += inputs[k]*weights[j*inputs.length+k];
			hidden[j] /= inputs.length+1;
			hidden[j] = 1/(1+Math.exp(-hidden[j]));
			
		}
		
		//process hidden layer 2
		/*
		for(int j = 0; j<hidden2.length; j++) {
			hidden2[j] = 1;
			for(int k = 0; k<hidden.length; k++)
				hidden2[j] += hidden[k]*weights[inputs.length*hidden.length+j*hidden.length+k];
			hidden2[j] /= hidden.length+1;
			hidden2[j] = 1/(1+Math.exp(-hidden2[j]));
			
		}
		
		//process hidden layer 3
		for(int j = 0; j<hidden3.length; j++) {
			hidden3[j] = 1;
			for(int k = 0; k<hidden2.length; k++)
				hidden3[j] += hidden2[k]*weights[inputs.length*hidden.length+16+j*hidden.length+k];
			hidden3[j] /= hidden.length+1;
			hidden3[j] = 1/(1+Math.exp(-hidden3[j]));
			
		}*/

		//process output layer
		for(int j = 0; j<output.length; j++) {
			output[j] = 1;
			for(int k = 0; k<hidden.length; k++)
				output[j] += hidden[k]*weights[inputs.length*hidden.length+j*hidden.length+k]; //[6*5+j*5+k]
			output[j] /= hidden.length+1;
			output[j] = 1/(1+Math.exp(-output[j]));
			
		}
		
		//determine action
		if (output[1] > output[0] && output[1] > output[2]) { //turn left
			if (s_dir > 0)
				s_dir--;
			else 
				s_dir = 3;
		}
		else if (output[2] > output[0] && output[2] > output[1]) { //turn right
			if (s_dir < 3)
				s_dir++;
			else 
				s_dir = 0;
		}
		else if (output[0] > output[1] && output[0] > output[2]) { //keep going forward
			//do nothing
		}
		
		//actually move
		if (!assume_direct_control) {
			switch(s_dir) {
			case 0:
				dx = SIZE;
				dy = 0;
				break;
			case 1:
				dx = 0;
				dy = -SIZE;
				break;
			case 2:
				dx = -SIZE;
				dy = 0;
				break;
			case 3:
				dx = 0;
				dy = SIZE;
				break;
			}
		
			if(Up) {
				if (fps_mod == 1)
					fps_mod=10;
				else if (fps_mod == 10)
					fps_mod=100;
				else if (fps_mod == 100)
					fps_mod=1000;
				else if (fps_mod == 1000)
					fps_mod=10000;
				else if (fps_mod == 10000)
					fps_mod=50000;
				else if (fps_mod == 50000)
					fps_mod=10000000;
			}
			if(Down) {
				if (fps_mod == 10000000)
					fps_mod=50000;
				else if (fps_mod == 50000)
					fps_mod=10000;
				else if (fps_mod == 10000)
					fps_mod=1000;
				else if (fps_mod == 1000)
					fps_mod=100;
				else if (fps_mod == 100)
					fps_mod=10;
				else if (fps_mod == 10)
					fps_mod=1;
			}
			Up = false;
			Down = false;
		}
		
		// move with keyboard
		if(assume_direct_control) {
			if(Up && dy == 0){
				dy = -SIZE;
				dx = 0;
				s_dir = 1;
			}
			if(Down && dy == 0){
				dy = SIZE;
				dx = 0;
				s_dir = 3;
			}
			if(Left && dx == 0){
				dy = 0;
				dx = -SIZE;
				s_dir = 2;
			}
			if(Right && dx == 0 && dy != 0){
				dy = 0;
				dx = SIZE;
				s_dir = 0;
			}
		}
		
		//Keep moving
		if(dx != 0 || dy != 0){
				
			for(int i = snek.size() - 1; i > 0; i--){
				
				snek.get(i).setPosition(snek.get(i - 1).getX(), snek.get(i - 1).getY());
			}
			head.move(dx, dy);
		}
		
		for(Entity s : snek){
			if(s.isCollision(head)){
				dead = true;
				break;
			}
		}
		if(head.getX()>WIDTH-SIZE || head.getX() < 0 || head.getY() < 0 || head.getY() > HEIGHT-SIZE) {
			
			dead = true;
			
		}
		
		//dead boiii
		if(dead){
			
			//Stats
			if (s_fitness > best_fitness)
				best_fitness = s_fitness;
			if (s_length > best_length) {
				exportDNA();
				best_length = s_length;
			}
			last_fitness = s_fitness;
			last_length = s_length;
			if (generation == generation_cap || gen10 == 10) {
				average_fitness += s_fitness;
				average_length += s_length;
			}
			
			fit[idd] = s_fitness;
			if (idd < pop_size-1) {
				idd++;
				s_idd = idd;
			} else { //genetics bitch
				
				//Sorting population
				for(int j = 0; j<pop_size; j++) {
					if (feedback)
						System.out.println("fitness["+j+"] = "+fit[j]);
				}
				best[0] = -1;
				for (int j = 0; j<pop_size; j++) {
					
					int selec = -1;
					for (int k = 0; k < pop_size; k++) {
						if (selec == -1) {
							if (fit[k] > 0 && check_list(k) == false)
								selec = k;
						} else {
							if (fit[k] > fit[selec] && check_list(k) == false)
								selec = k;
						}
					}
					best[j] = selec;
				}
				
				int tot_fit = 0;
				for(int j = 0; j<pop_size-1; j++) {
					fit[best[j]] -= 1; //get fitness to 0
					if (feedback)
						System.out.println("best["+j+"] = "+best[j]+", with fitness = "+fit[best[j]]);
					tot_fit += fit[best[j]];
				}
				for(int j = 0; j<pop_size-1; j++) {
					fit[best[j]] /= tot_fit;
				}
				//--Sorting population
				
				//Selection + Crossover
				//elitism
				if (feedback)
					System.out.println("Moving top "+elitism+" to new generation");
				for(int j = 0; j<pop_size-1; j++) {
					for(int k = 0; k<weights.length; k++) {
						RNA[j][k] = DNA[best[j]][k];
					}
				}
				
				//mating
				if (feedback)
					System.out.println("Sexy time started");
				for(int j = 0; j<pop_size-1-elitism; j++) {
				
					//selection
					double selectnum1 = (double)(Math.random());
					double selectnum2 = (double)(Math.random());
					if (feedback)
						System.out.println("Math.random() = "+(double)(Math.random()));
					if (selectnum1 > selectnum2) {
						double buff = selectnum2;
						selectnum2 = selectnum1;
						selectnum1 = buff;
					}
					int selectboi = -1;
					int selectbitch = -1;
					double fit_tot = 0.0000;
					for (int k = 0; k<pop_size-1;k++) {
						fit_tot += fit[best[k]];
						if (fit_tot > selectnum1 && selectboi == -1) {
							selectboi = k;
						}
						if (fit_tot > selectnum2 && k != selectboi && selectbitch == -1) {
							selectbitch = k;
							break;
						}
					}
					if (selectboi == -1) {
						selectboi = 0;
						if (feedback)
							System.out.println("ERROR: Selectboi was -1. Corrected to 0. Selectnum1 was "+selectnum1);
					}
					if (selectbitch == -1) {
						selectbitch = 1;
						if (feedback)
							System.out.println("ERROR: Selectbitch was -1. Corrected to 1. Selectnum2 was "+selectnum2);
					}
						
					
					int mate1 = selectboi;
					int mate2 = selectbitch;
					
					//crossover
					int child = j+elitism;
					int point1 = 2 + (int)(Math.random()*(weights.length-2));
					int point2 = 2 + (int)(Math.random()*(weights.length-2));
					if (point1 > point2) {
						int buff = point1;
						point1 = point2;
						point2 = buff;
					}
					for (int k = 0; k<weights.length; k++) {
						
						if (k < point1)
							RNA[child][k] = DNA[mate1][k];
						else if (k >= point1 && k < point2)
							RNA[child][k] = DNA[mate2][k];
						else if (k >= point2)
							RNA[child][k] = DNA[mate1][k];
						if ((int)(Math.random()*mut_chance) == 1) {
							tot_mut++;
							RNA[child][k] = min_weight + (double)(Math.random()*max_weight);
						}
						
					}
					if (feedback)
						System.out.println("child number "+child+" made by selectboi "+mate1+" and selectbitch "+mate2);
					child++;
					
				}
				//--Selection + Crossover
				
				//Replace old DNA with new RNA
				if (feedback)
					System.out.println("Replacing old DNA with new RNA");
				for (int j = 0; j<pop_size-1; j++) {
					for (int k = 0; k<weights.length; k++) {
						DNA[j][k] = RNA[j][k];
					}
				}
				//--Replace old DNA with new RNA
				
				//Resetting stuff
				for(int j = 0; j<pop_size; j++) {
					best[j] = -1;
					fit[j] = 0;
				}
				
				if (gen10 == 10) { //export shit
					average_fitness /= pop_size;
					average_length /= pop_size;
					try {
						PrintWriter writer2 = new PrintWriter(new FileOutputStream(new File("Graph data.txt"), true)); 
						writer2.println("Gen"+generation+" fitness = "+average_fitness);
						writer2.println("Gen"+generation+" length = "+average_length);
						writer2.println("");
						writer2.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
					gen10 = 0;
				}
				generation++;
				gen10++;
				idd = 0;
				s_idd = 0;
				
			}

			s_fitness = 0;
			s_dir = 0;
			s_length = 3;
			s_lifeforce = 200;
			s_distance = 0;
			
			for(int j = 0; j<weights.length;j++) {
				weights[j] = DNA[s_idd][j];
			}
			
			//STATS FOR ANALYSIS
			/*
			if (generation == generation_cap+1) {
				for (int j = 0; j < pop_size; j++) {
					for (int k = 0; k < weights.length; k++) {
						DNA[j][k] = min_weight + (double)(Math.random() * max_weight);
						RNA[j][k] = -999;
					}
				}
				
				for(int j = 0; j<weights.length;j++) {
					weights[j] = DNA[s_idd][j];
				}
				average_fitness /= pop_size;
				average_length /= pop_size;
				System.out.println("Iteration "+iteration+" done. Average fitness = "+average_fitness+". Average length = "+average_length+". Best fitness = "+best_fitness+". Best length = "+best_length);
				average_average_fitness += average_fitness;
				average_average_length += average_length;
				average_best_fitness+= best_fitness;
				average_best_length += best_length;
				
				if (iteration == iteration_amount) {
					average_average_fitness /= iteration_amount;
					average_average_length /= iteration_amount;
					average_best_fitness /= iteration_amount;
					average_best_length /= iteration_amount;
					System.out.println("Simulation completed. Ultimate average fitness = "+average_average_fitness+". Ultimate average length = "+average_average_length+". Ultimate best fitness = "+average_best_fitness+". Ultimate best length = "+average_best_length);
					alive = false;
					return;
				}
				
				iteration++;
				average_fitness = 0;
				average_length = 0;
				best_fitness = 0;
				best_length = 0;
				generation = 0;
			}*/
			//--STATS FOR ANALYSIS
			
			levelSetUp();
			
			dead = false;
			//return;
		}
		
		//Nomnom function 
		if (rabbit.isCollision(head)){ 
			tot_food++;
			s_length += 1;
			s_fitness += 10;
			s_lifeforce = 400;
			setRabbit();
			
		//Food is good for ya
			Entity s = new Entity(SIZE);
			s.setPosition(-100, -100);
			snek.add(s);
		}
	}
	
	public boolean check_list(int idboi) {
		
		if (best[0] == -1) return false;
		for(int l = 0; l< best.length; l++) {
			if (best[l] == idboi)
				return true;
		}
		return false;
		
	}
	
	public void render(Graphics2D g2d){
		
		g2d.clearRect(0, 0, WIDTH, HEIGHT);
		g2d.setColor(Color.WHITE);
		for(Entity s : snek){
			s.render(g2d);
		}
		
		g2d.setColor(Color.YELLOW);
		rabbit.render(g2d);
		g2d.setColor(Color.WHITE);
		g2d.drawString("FPS_mod= "+ fps_mod +"  Gen= " + generation + "  Idd= " + s_idd + "  Fitness= " + s_fitness, 4, 14);
		g2d.drawString("Lifeforce= " + s_lifeforce + "  Tot_food= " + tot_food + "  Tot_mut= " + tot_mut, 4, 30);
		g2d.drawString("i[0]= " + inputs[0] + "  i[1]= " + inputs[1] + "  i[2]= " + inputs[2], 4, 46);
		g2d.drawString("i[3]= " + inputs[3] + "  i[4]= " + inputs[4] + "  i[5]= " + inputs[5], 4, 62);
		g2d.setColor(Color.RED);
		if (output[0] > output[1] && output[0] > output[2]) 
			g2d.setColor(Color.GREEN);
		g2d.drawString("o[0]= " + output[0], 4, 80);
		g2d.setColor(Color.RED);
		if (output[1] > output[0] && output[1] > output[2]) 
			g2d.setColor(Color.GREEN);
		g2d.drawString("o[1]= " + output[1], 4, 96);
		g2d.setColor(Color.RED);
		if (output[2] > output[1] && output[2] > output[0]) 
			g2d.setColor(Color.GREEN);
		g2d.drawString("o[2]= " + output[2], 4, 112);
		g2d.setColor(Color.WHITE);
		g2d.drawString("Last snake = " + last_length + ", with fitness = " + last_fitness, 4, 616);
		g2d.drawString("Longest Snake = " + best_length + ", with fitness = " + best_fitness, 4, 632);
		
	}


}
