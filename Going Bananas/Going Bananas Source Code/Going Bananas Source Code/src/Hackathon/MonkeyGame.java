package Hackathon;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import javax.swing.JOptionPane;


public class MonkeyGame extends JPanel implements ActionListener, KeyListener {
	public static final long serialVersionUID = 1L;
	public static final int WIDTH = 640;
	public static final int HEIGHT = 480;
	public static final int DELAY = 10;
	public boolean paused = false;
	public PauseMenu pauseMenu;

	public boolean showDeathMenu = false;

	private String difficultyLevel = "Low";

	private int secondsAlive = 0;
	private boolean jetpack;
	private boolean left;
	private boolean right;
	private boolean up;
	private boolean down;

	public boolean hasJetpack = false;

	public List<Banana> bananas = new ArrayList<>();
	public List<Rock> rocks = new ArrayList<>();
	public List<JetPack> jetpacks = new ArrayList<>();

	public Monkey monkey = new Monkey(WIDTH / 2, HEIGHT - 150);
	public MonkeyGame() {
		Timer timer = new Timer(DELAY, this);
		timer.start();
		addKeyListener(this);
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		pauseMenu = new PauseMenu(this);
		pauseMenu.setBounds(0, 0, WIDTH, HEIGHT);
		pauseMenu.setVisible(false);
		add(pauseMenu);
	}

	private void restartGame(boolean won) {
		Monkey.spriteName = "monkey.png";
		hasJetpack = false;
		handleDeathMenu(won);
		bananas.clear();
		rocks.clear();
		jetpacks.clear();
		monkey = new Monkey(WIDTH / 2, HEIGHT - 150);
		count = 0; // Reset the count variable to its initial value
		difficultyLevel = "Low";
		left = false;
		right = false;
		up = false;
		down = false;
	}
	int count = 0;
	public void actionPerformed(ActionEvent e) {
		if (!paused) {
			if (left) {
				monkey.moveLeft();
			}
			if (right) {
				monkey.moveRight();
			}
			if (down) {
				monkey.moveDown();
			}
			if (up) {
				monkey.moveUp();
			}
			if (monkey.jumping) {
				monkey.y += monkey.dy;
				monkey.dy += 0.5; // gravity
				if (monkey.y >= MonkeyGame.HEIGHT - 150) { // ground level
					monkey.y = MonkeyGame.HEIGHT - 150;
					monkey.jumping = false;
				}
			}
			if (Math.random() < 0.05) {
				bananas.add(new Banana((int) (Math.random() * WIDTH), 0));
			}
			for (Banana banana : bananas) {
				banana.move();
				if (monkey.intersects(banana)) {
					bananas.remove(banana);
					monkey.collectBanana(banana);
					break;
				}

			}
			count++;
			if(count == 1) {
				secondsAlive = 0;
			}
			if(count % 100 == 0) {
				secondsAlive = count/100;
			}
			double x = Math.random();
			if (count > 6000) {
				// Display win screen with stats
				String message = "Congratulations! You survived for 60 seconds and won!\n\n" +
						"Stats:\n" +
						"Bananas collected in round: " + monkey.getBananasCollected().size() + "\n" +
						"Total deaths: " + Monkey.getTotalDeaths();
				JOptionPane.showMessageDialog(this, message, "Win Screen", JOptionPane.INFORMATION_MESSAGE);
				// Restart the game or perform any necessary actions
				restartGame(true);
				return; // Skip the remaining code in actionPerformed
			}
			//difficulty scaling
			if (x < 0.032 && count > 4000) {
				rocks.add(new Rock((int) (Math.random() * WIDTH), 0));
				difficultyLevel = "High";
			} else if (count > 2000 && x < 0.016) {
				rocks.add(new Rock((int) (Math.random() * WIDTH), 0));
				difficultyLevel = "Medium";
			} else if (x < 0.008) {
				rocks.add(new Rock((int) (Math.random() * WIDTH), 0));
			}

			for (Rock rock : rocks) {
				rock.move();
				if (monkey.intersects(rock)) {
					rocks.remove(rock);
					monkey.lives(rock); // Update the deaths counter
					restartGame(false); // Restart the game after collision
					break;
				}
			}

			if (Math.random() < 0.00005) {
				jetpacks.add(new JetPack((int) (Math.random() * WIDTH), 0));
			}
			for (JetPack jetpack : jetpacks) {
				jetpack.move();
				if (monkey.intersects(jetpack)) {
					jetpacks.remove(jetpack);
					hasJetpack = true;
					Monkey.spriteName = "jetpack_monkey.png";
					break;
				}

			}
			repaint();
		}
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;

		// Load the image
		ImageIcon imageIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("background.png")));
		Image image = imageIcon.getImage();

		// Draw the image
		g2d.drawImage(image, 0, 0, getWidth(), getHeight(), this);

		g2d.setColor(Color.BLACK);
		monkey.draw(g2d);
		for (Banana banana : bananas) {
			banana.draw(g2d);
		}
		for (Rock rock : rocks) {
			rock.draw(g2d);
		}
		for (JetPack jetpack : jetpacks) {
			jetpack.draw(g2d);
		}
		// Draw difficulty level
		g.setColor(Color.WHITE);
		g.fillRect(5, 20, 150, 15);
		g.setColor(Color.BLACK);
		g.drawRect(5, 20, 150, 15);
		g2d.drawString("Bananas collected: " + monkey.getBananasCollected().size(), 10, 32);

		g.setColor(Color.WHITE);
		g.fillRect(5, 5, 150, 15);
		g.setColor(Color.BLACK);
		g.drawRect(5, 5, 150, 15);
		g2d.drawString("Deaths: " + Monkey.getTotalDeaths(), 10, 17);

		g.setColor(Color.WHITE);
		g.fillRect(490, 5, 145, 15);
		g.setColor(Color.BLACK);
		g.drawRect(490, 5, 145, 15);
		g2d.drawString("Difficulty: " + difficultyLevel, 495, 17);

		g.setColor(Color.WHITE);
		g.fillRect(490, 20, 145, 15);
		g.setColor(Color.BLACK);
		g.drawRect(490, 20, 145, 15);
		g2d.drawString("Seconds Alive: " + secondsAlive, 495, 32);

	}

	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {
			left = true;
		}
		if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
			right = true;
		}
		if (keyCode == KeyEvent.VK_SPACE  || keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP && !monkey.jumping && hasJetpack) {
			up = true;
		}
		if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
			down = true;
		}
		if (keyCode == KeyEvent.VK_SPACE  || keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP && !monkey.jumping && !hasJetpack) {
			monkey.jumping = true;
			monkey.dy = -10; // initial vertical velocity
		}
		if (keyCode == KeyEvent.VK_P) {
			paused = !paused;
			pauseMenu.setVisible(paused);
		}
	}


	public void keyTyped(KeyEvent e) {}

	public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {
			left = false;
		}
		if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
			right = false;
		}
		if (keyCode == KeyEvent.VK_SPACE  || keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP && !monkey.jumping && hasJetpack) {
			up = false;
		}
		if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
			down = false;
		}
	}

	private void handleDeathMenu(boolean won) {
		// Display the death menu options
		int option = 0;
		if(!won) {
			option = JOptionPane.showOptionDialog(
					this,                           // Parent component
					"You died!\n\nSelect an option:",  // Message
					"Death Menu",                   // Title
					JOptionPane.YES_NO_OPTION,      // Option type
					JOptionPane.PLAIN_MESSAGE,      // Message type
					null,                           // Icon
					new Object[]{"Quit", "Continue Playing"}, // Options
					"Continue Playing"                          // Default option
			);
		}
		else {
			option = JOptionPane.showOptionDialog(
					this,                           // Parent component
					"You won!\n\nSelect an option:",  // Message
					"Win Menu",                   // Title
					JOptionPane.YES_NO_OPTION,      // Option type
					JOptionPane.PLAIN_MESSAGE,      // Message type
					null,                           // Icon
					new Object[]{"Quit", "Continue Playing"}, // Options
					"Continue Playing"                          // Default option
			);
		}

		if (option == JOptionPane.YES_OPTION) {
			System.exit(0);  // Quit the game
		} else if (option == JOptionPane.NO_OPTION) {
			// Continue playing
			showDeathMenu = false;  // Hide the death menu
		}
	}

	public static void main(String[] args) {
		String message = """
				Welcome!\s
				Collect bananas and avoid rocks; the longer you are alive the more rocks will fall...\s
				Once you last for 60 seconds you win!
				""";
		JOptionPane.showMessageDialog(null, message, "Game Welcome", JOptionPane.INFORMATION_MESSAGE);
		JFrame frame = new JFrame("Going Bananas");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(WIDTH, HEIGHT);
		frame.setResizable(false);
		frame.add(new MonkeyGame());
		frame.setVisible(true);
		MusicPlayer musicPlayer = new MusicPlayer();
		musicPlayer.playMusicFile(0);
	}
}

class Monkey {
	public static String spriteName = "monkey.png";
	private static int totalDeaths = 0; // Static variable to track total deaths
	private int x;
	public int y;
	private List<Banana> bananasCollected = new ArrayList<>();
	private List<Rock> lives = new ArrayList<Rock>();
	public double dy; // vertical velocity
	public boolean jumping;

	public Monkey(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public boolean intersects(Banana banana) {
		return Math.abs(x - banana.getX()) < 50 && Math.abs(y - banana.getY()) < 50;
	}

	public boolean intersects(Rock rock) {
		if (Math.abs(x - rock.getX()) < 35 && Math.abs(y - rock.getY()) < 35) {
			return true;
		}
		return false;
	}

	public boolean intersects(JetPack jetpack) {
		return Math.abs(x - jetpack.getX()) < 50 && Math.abs(y - jetpack.getY()) < 50;
	}

	public void draw(Graphics2D g) {
		ImageIcon icon1 = new ImageIcon(Objects.requireNonNull(getClass().getResource(spriteName)));
		Image image = icon1.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
		icon1 = new ImageIcon(image);
		g.drawImage(icon1.getImage(), x - 20, y - 20, null);
	}

	public void collectBanana(Banana banana) {
		bananasCollected.add(banana);
	}

	public void lives(Rock rock) {
		lives.add(rock);
		totalDeaths++; // Increment total deaths
		// Update the deaths counter display or perform any other necessary actions
	}

	public static int getTotalDeaths() {
		return totalDeaths;
	}
	public List<Banana> getBananasCollected() {
		return bananasCollected;
	}
	public List<Rock> getLives(){
		return lives;
	}

	public void moveLeft() {
		x -= 5; // change this value to adjust the speed of movement
		if (x < 0) { // prevent the monkey from moving off the screen
			x = 0;
		}
	}

	public void moveRight() {
		x += 5; // change this value to adjust the speed of movement
		if (x > MonkeyGame.WIDTH - 40) { // prevent the monkey from moving off the screen
			x = MonkeyGame.WIDTH - 40;
		}
	}

	public void moveUp() {
		y -= 5; // change this value to adjust the speed of movement
		if (y < 0) { // prevent the monkey from moving off the screen
			y = 0;
		}
	}

	public void moveDown() {
		y += 5; // change this value to adjust the speed of movement
		if (y > MonkeyGame.HEIGHT - 150) { // prevent the monkey from moving off the screen
			y = MonkeyGame.HEIGHT - 150;
		}
	}
}

class Banana {
	private int x;
	private int y;
	private ImageIcon icon;

	public Banana(int x, int y) {
		this.x = x;
		this.y = y;
		ImageIcon icon1 = new ImageIcon(Objects.requireNonNull(getClass().getResource("banana.png")));
		Image image = icon1.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
		this.icon = new ImageIcon(image);
	}

	public void move() {
		y += 2;
	}

	public void draw(Graphics2D g) {
		g.drawImage(icon.getImage(), x - 10, y - 10, null);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
}

class Rock {
	private int x;
	private int y;
	private ImageIcon icon;

	public Rock(int x, int y) {
		this.x = x;
		this.y = y;
		ImageIcon icon1 = new ImageIcon(Objects.requireNonNull(getClass().getResource("Rock.png")));
		Image image = icon1.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
		this.icon = new ImageIcon(image);
	}

	public void move() {
		y += 1.5;
	}

	public void draw(Graphics2D g) {
		g.drawImage(icon.getImage(), x - 10, y - 10, null);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
}

class JetPack {
	private int x;
	private int y;
	private ImageIcon icon;

	public JetPack(int x, int y) {
		this.x = x;
		this.y = y;
		ImageIcon icon1 = new ImageIcon(Objects.requireNonNull(getClass().getResource("jetpack.png")));
		Image image = icon1.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
		this.icon = new ImageIcon(image);
	}

	public void move() {
		y += 1;
	}

	public void draw(Graphics2D g) {
		g.drawImage(icon.getImage(), x - 10, y - 10, null);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
}

class MusicPlayer {
	private Clip clip;

	public void playMusic(String filePath) {
		try {
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(Objects.requireNonNull(getClass().getResourceAsStream(filePath)));
			clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			clip.start();
		} catch (Exception e) {
			System.out.println("Error playing music: " + e.getMessage());
		}
	}
	public void playMusicFile(int x){
		if (x >= 0){
			if (x == 0){
				playMusic("theme.WAV");
			}
			else if (x == 1){
				playMusic("collect.WAV");
			}
			else if (x == 2){
				playMusic("obstacle.WAV");
			}
			else if (x == 3){
				playMusic("death.WAV");
			}
			else if (x == 4){
				playMusic("power_up.WAV");
			}
		}

	}

	public void stopMusic() {
		if (clip != null) {
			clip.stop();
			clip.close();
		}
	}
}

class PauseMenu extends JPanel {
	public PauseMenu(MonkeyGame game) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(false);

		JLabel label = new JLabel("Game Paused");
		label.setFont(new Font("Arial", Font.BOLD, 24));
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(label);

		JButton resumeButton = new JButton("Resume");
		resumeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		resumeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				game.paused = false;
				setVisible(false);
			}
		});
		add(resumeButton);

		JButton exitButton = new JButton("Exit");
		exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		exitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		add(exitButton);
	}
}