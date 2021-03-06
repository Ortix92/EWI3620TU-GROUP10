import java.awt.Color;
import java.awt.Frame;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.swing.JFrame;

import com.sun.opengl.util.Animator;
import com.sun.opengl.util.GLUT;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;
import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;


public class MainClass extends Frame implements GLEventListener, MouseListener {
	
	private static final long serialVersionUID = 1L;
	public static GLCanvas canvas;
	public static int screenWidth = 600, screenHeight = 600;		// Screen size.
//	public static int screenX = 0, screenY = 0;
	public int tel;
	
	public static MazeRunner mazeRunner;
	public static GameStateManager state;
	public static MainMenu mainMenu;
	public static Maze maze;
	public static MazePheromones mazePheromones;
	public static Player player;
	public static ArrayList<Enemy> enemies = new ArrayList<Enemy>();
	public static Camera camera;
	public static UserInput input;
	public static Pause pause;
	public static GameOver gameover;
	public static Sword sword;
	public static Shield shield;
	
	//Load the textures
	protected static ArrayList<Texture> textures;
	protected static ArrayList<String> textureNames;
	private int numberOfTextures;
	private Texture tempTexture;
	private String textureFileName = "";
	private String textureFileType = "png";
	private float textureTop, textureBottom, textureLeft, textureRight;
	
	public MainClass(){
		super("Medieval Invasion");
		
		// Let's change the window to our liking.
		setSize( screenWidth, screenHeight);
		setExtendedState(JFrame.MAXIMIZED_BOTH); 
		setBackground(new Color(0.0f, 0.0f, 0.0f, 1));
		
		//Textures
		textures = new ArrayList<Texture>();
		textureNames = new ArrayList<String>();
		// The window also has to close when we want to.
		this.addWindowListener( new WindowAdapter()
		{
			public void windowClosing( WindowEvent e )
			{
				System.exit(0);
			}
		});
		
		// The OpenGL capabilities should be set before initializing the
		// GLCanvas. We use double buffering and hardware acceleration.
		GLCapabilities caps = new GLCapabilities();
		caps.setDoubleBuffered(true);
		caps.setHardwareAccelerated(true);

		// Create a GLCanvas with the specified capabilities and add it to this
		// frame. Now, we have a canvas to draw on using JOGL.
		canvas = new GLCanvas(caps);
		add(canvas);

		// Set the canvas' GL event listener to be this class. Doing so gives
		// this class control over what is rendered on the GL canvas.
		canvas.addGLEventListener(this);

		// Also add this class as mouse listener, allowing this class to react
		// to mouse events that happen inside the GLCanvas.
		canvas.addMouseListener(this);
		
		mainMenu = new MainMenu(screenHeight, screenWidth);
		state = new GameStateManager();
		
		// Set the frame to visible. This automatically calls upon OpenGL to prevent a blank screen.
		setVisible(true);
		initObjects();
		
	}
	
	public void display(GLAutoDrawable drawable) {
		render(drawable);
	}
	
	public void rendersize(GLAutoDrawable drawable, int tel){
		switch(tel){
		case 0: 
			mainMenu.reshape(drawable, 0, 0, screenWidth, screenHeight);
			break;
		case 1: 
			mazeRunner.reshape(drawable, 0, 0, screenWidth, screenHeight);
			break;
		case 2: 
			pause.reshape(drawable, 0, 0, screenWidth, screenHeight);
			break;
		case 4:
			gameover.reshape(drawable, 0, 0, screenWidth, screenHeight);
			break;
		}
	}
	
	public void render (GLAutoDrawable drawable){
		GL gl = drawable.getGL();

		// Set the clear color and clear the screen.
		gl.glClearColor(1.0f, 0.0f, 0.0f, 1);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		
		if(tel != state.getState()){
			switch(tel){
			case 0: 
				setScreenSize(mainMenu.ScreenHeight, mainMenu.ScreenWidth);
				break;
			case 1: 
				setScreenSize(mazeRunner.screenHeight, mazeRunner.screenWidth); 
				break;
			case 2: 
				setScreenSize(pause.ScreenHeight, pause.ScreenWidth); 
				break;
			case 4:
				setScreenSize(gameover.ScreenHeight, gameover.ScreenWidth);
				break;
			}
			rendersize(drawable, state.getState());
		}
		
		tel = state.getState();
		
		if(tel==0){
			mainMenu.render(drawable);
		}
		else if (tel==1){
			mazeRunner.render(drawable);
		}
		else if (tel==2){
			pause.render(drawable);
			pause.init(drawable);
		}
		else if (tel==3){
			System.exit(0);
		}
		else if (tel==4){
			gameover.render(drawable);
			gameover.init(drawable);
		}
		
		initUpdater(drawable,0,0, screenWidth, screenHeight);

	}
	
/*
 * **********************************************
 * *		OpenGL event handlers				*
 * **********************************************
 */

	//@Override
	//public void display(GLAutoDrawable arg0) {
	//	// TODO Auto-generated method stub
	//	
	//}

	@Override
	public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(GLAutoDrawable arg0) {
		// Retrieve the OpenGL handle, this allows us to use OpenGL calls.
		GL gl = arg0.getGL();
		

		// Set the matrix mode to GL_PROJECTION, allowing us to manipulate the
		// projection matrix
		gl.glMatrixMode(GL.GL_PROJECTION);

		// Always reset the matrix before performing transformations, otherwise
		// those transformations will stack with previous transformations!
		gl.glLoadIdentity();

		/*
		 * glOrtho performs an "orthogonal projection" transformation on the
		 * active matrix. In this case, a simple 2D projection is performed,
		 * matching the viewing frustum to the screen size.
		 */
		gl.glOrtho(0, screenWidth, 0, screenHeight, -1, 1);

		// Set the matrix mode to GL_MODELVIEW, allowing us to manipulate the
		// model-view matrix.
		gl.glMatrixMode(GL.GL_MODELVIEW);

		// We leave the model view matrix as the identity matrix. As a result,
		// we view the world 'looking forward' from the origin.
		gl.glLoadIdentity();

		// We have a simple 2D application, so we do not need to check for depth
		// when rendering.
		gl.glDisable(GL.GL_DEPTH_TEST);
		
		loadTextures();
		
		// First, we set up JOGL. We start with the default settings.
		GLCapabilities caps = new GLCapabilities();
		// Then we make sure that JOGL is hardware accelerated and uses double buffering.
		caps.setDoubleBuffered( true );
		caps.setHardwareAccelerated( true );
		
		/* We need to create an internal thread that instructs OpenGL to continuously repaint itself.
		 * The Animator class handles that for JOGL.
		 */
		Animator anim = new Animator( canvas );
		anim.start();
		
				
	}

	@Override
	public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3, int arg4) {
		int tel = state.getState();
		if(tel == 0){
			mainMenu.reshape(arg0, arg1, arg2, arg3, arg4);
//			setScreenSize(mainMenu.ScreenHeight, mainMenu.ScreenWidth);
		}
		else if(tel == 1){
			mazeRunner.reshape(arg0, arg1, arg2, arg3, arg4);
		}
		else if(tel == 2){
			pause.reshape(arg0, arg1, arg2, arg3, arg4);
		}
		else if(tel == 4){
			gameover.reshape(arg0, arg1, arg2, arg3, arg4);
		}
		
		
	}

/*
 * **********************************************
 * *		Mouse event handlers				*
 * **********************************************
 */
	
	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent me) {
		int tel = state.getState();
		if(tel==0){
			mainMenu.mouseReleased(me);
		}
		else if(tel==2){
			pause.mouseReleased(me);
		}
		else if(tel==4){
			gameover.mouseReleased(me);
		}

	}
	
	public void initUpdater(GLAutoDrawable drawable, int x, int y, int screenWidth, int screenHeight){
		int tel=state.getState();
		if(tel==0 && state.getStopTitle()==false){
			mainMenu.init(drawable);
		}
		if(tel==1 && state.getStopMainGame()==false ){
			mazeRunner.mazeInit(drawable,0,0,screenWidth, screenHeight);
		}
		else if(tel==2 && state.getStopPause()==false){
			pause.init(drawable);
		}
		else if(tel==4 && state.getStopGameOver()==false){
			gameover.init(drawable);
		}
	}
	
	public void loadTextures(){
		ArrayList<HashMap> tempTextureHashMapArray = TextureLoader.loadTextureArray("textures/");
		numberOfTextures = tempTextureHashMapArray.size();
		textureNames = new ArrayList<String>(numberOfTextures);
		textures = new ArrayList<Texture>(numberOfTextures);
		for(int i = 0; i<numberOfTextures;i++){
			HashMap<String, Texture> tempTextureHashMap = tempTextureHashMapArray.get(i);
			String tempTextureName = tempTextureHashMap.entrySet().iterator().next().getKey();
			Texture tempTexture = tempTextureHashMap.entrySet().iterator().next().getValue();
			textureNames.add(tempTextureName);
			textures.add(tempTexture);		
		}
		textureFileName = textureNames.get(0);			
	}
	
	public static void initObjects(){
		mainMenu.setDrawButtons();
		
		maze = new Maze();
		mazePheromones = new MazePheromones();
		player = new Player( 6 * maze.SQUARE_SIZE + maze.SQUARE_SIZE / 2, 	// x-position
							 maze.SQUARE_SIZE / 2,							// y-position
							 5 * maze.SQUARE_SIZE + maze.SQUARE_SIZE / 2, 	// z-position
							 90, 0 );										// horizontal and vertical angle
		sword = new Sword( 6 * maze.SQUARE_SIZE + maze.SQUARE_SIZE / 2 -1, 	
							 maze.SQUARE_SIZE / 2 -1,							
							 5 * maze.SQUARE_SIZE + maze.SQUARE_SIZE / 2,true, 1);
		shield = new Shield( 6 * maze.SQUARE_SIZE + maze.SQUARE_SIZE / 2 -1, 	
				 maze.SQUARE_SIZE / 2 -1,							
				 5 * maze.SQUARE_SIZE + maze.SQUARE_SIZE / 2,true, false);


		enemies.add(new Enemy(	2 * maze.SQUARE_SIZE + maze.SQUARE_SIZE / 2, 	// x-position
				0.01 * maze.SQUARE_SIZE,							// y-position
				5 * maze.SQUARE_SIZE + maze.SQUARE_SIZE / 2,0, true));	// z-position, texture boolean
		
		enemies.add(new Enemy(	2 * maze.SQUARE_SIZE + maze.SQUARE_SIZE / 2, 	// x-position
				0.01 * maze.SQUARE_SIZE,							// y-position
				3 * maze.SQUARE_SIZE + maze.SQUARE_SIZE / 2,0, true));
		
		camera = new Camera( player.getLocationX(), player.getLocationY(), player.getLocationZ(), 
				             player.getHorAngle(), player.getVerAngle() );
		
		input = new UserInput(canvas);
		
		mazeRunner = new MazeRunner(screenHeight, screenWidth);
		pause = new Pause(screenHeight, screenWidth);
		gameover = new GameOver(screenHeight, screenWidth);

	}

	public void setScreenSize(int ScreenHeight, int ScreenWidth){//, int y, int x){
		screenHeight = ScreenHeight;
		screenWidth = ScreenWidth;
//		screenY = y;
//		screenX = x;
	}
	
}
