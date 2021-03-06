import java.awt.Color;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.GLJPanel;

import com.sun.opengl.util.Animator;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;
import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;

public class LevelEditorFrame extends Frame implements GLEventListener, MouseListener,MouseMotionListener, MouseWheelListener {
	static final long serialVersionUID = 7526471155622776147L;
	
	// Screen size.
	private int screenWidth = 800, screenHeight = 600;
	private float buttonSize = screenHeight / 10.0f;
	
	//Grid distances
	//private int worldSizeX = 10, worldSizeY = 10; 
	private float gridDistance = 50.0f;
	private float gridOffsetX = 10, gridOffsetY = 10;
	private float gridDragX = 0, gridDragY = 0;
	
	//Sizes
	private float pointSize = 5.0f;
	private float lineWidth = 3.0f;

	// A GLCanvas is a component that can be added to a frame. The drawing
	// happens on this component.
	private GLJPanel canvas;

	private static final byte DM_OBJECT = 0;
	private static final byte DM_WALL = 1;
	private static final byte DM_FLOOR = 2;
	private static final byte DM_ROOF = 3;
	private static final byte DM_ERASE = 4;
	private byte drawMode = DM_OBJECT;
	
	// 1 = Draw, 2 = Erase
	private int mode = 1;

	private ArrayList<Point2D.Float> points;
	
	//Grid
	private ArrayList<Point2D.Float> gridpoints;
	private ArrayList<Point2D.Float> grid = new ArrayList<Point2D.Float>();
	private Point2D.Float gridHighlight = new Point2D.Float();
	
	//Walls
	private Wall wall = new Wall();
	private WallList wallList = new WallList();
	
	//Floors
	private Floor floor = new Floor();
	private FloorList floorList = new FloorList();
	
	//Roofs
	private Roof roof = new Roof();
	private RoofList roofList = new RoofList();
	
	//Roofs
	private Object object = new Object();
	private ObjectList objectList = new ObjectList();
	
	//Storey
	private Storey storey = new Storey();
	private ArrayList<Storey> storeys;
	private int storeyNumber = 1;
	
	
	//Texture
	private ArrayList<Texture> textures;
	private ArrayList<String> textureNames;

	private Texture tempTexture;
	
	private String textureFileName = "";
	private String textureFileType = "png";
	private float textureTop, textureBottom, textureLeft, textureRight;
	//private int textureID;

	private int objectToDraw;
	private int wallToHighlight = -1;
	private int floorToHighlight = -1;
	private int roofToHighlight = -1;
	private int objectToHighlight = -1;
	
	private CursorHandler c;
	
	/**
	* When instantiating, a GLCanvas is added to draw the level editor. 
	* An animator is created to continuously render the canvas.
	*/
	public LevelEditorFrame(GLJPanel panel) {
		//super("Knight vs Aliens: Level Editor");
		
		//Screen points
		points = new ArrayList<Point2D.Float>();
		
		//Size Map
		//world.setSizeX(xMap);
		//world.setSizeY(yMap);
		
		//Grid points
		gridpoints = new ArrayList<Point2D.Float>();
		
		//Textures
		textures = new ArrayList<Texture>();
		textureNames = new ArrayList<String>();
		

		//Set the desired size and background color of the frame
		//setSize(screenWidth, screenHeight);
		//setBackground(Color.white);
		//setBackground(new Color(0.95f, 0.95f, 0.95f));

		// When the "X" close button is called, the application should exit.
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		
		// The OpenGL capabilities should be set before initializing the
		// GLCanvas. We use double buffering and hardware acceleration.
		//GLCapabilities caps = new GLCapabilities();
		//caps.setDoubleBuffered(true);
		//caps.setHardwareAccelerated(true);

		// Create a GLCanvas with the specified capabilities and add it to this
		// frame. Now, we have a canvas to draw on using JOGL.
		canvas = panel;
		add(canvas);

		// Set the canvas' GL event listener to be this class. Doing so gives
		// this class control over what is rendered on the GL canvas.
		canvas.addGLEventListener(this);

		// Also add this class as mouse listener, allowing this class to react
		// to mouse events that happen inside the GLCanvas.
		canvas.addMouseListener(this);
		
		canvas.addMouseMotionListener(this);
		
		canvas.addMouseWheelListener(this);
		
		// Nieuwe cursordinges maken
		c = new CursorHandler(canvas);
		

		// An Animator is a JOGL help class that can be used to make sure our
		// GLCanvas is continuously being re-rendered. The animator is run on a
		// separate thread from the main thread.
		Animator anim = new Animator(canvas);
		anim.start();

		// With everything set up, the frame can now be displayed to the user.
		//setVisible(true);
		}

	@Override
	/**
	 * A function defined in GLEventListener. It is called once, when the frame containing the GLCanvas 
	 * becomes visible. In this assignment, there is no moving �camera�, so the view and projection can 
	 * be set at initialization. 
	 */
	public void init(GLAutoDrawable drawable) {
		// Retrieve the OpenGL handle, this allows us to use OpenGL calls.
		GL gl = drawable.getGL();

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
		
		//Set the width
		gl.glLineWidth(lineWidth);
		gl.glPointSize(pointSize);
		
		loadTextures();
			
	}

	
	/**
	 * Initialize the grid
	 */
	public void initGrid(){
		grid.clear();
		if(storeys.size()>0){
			for(int x = 1; x < storeys.get(storeyNumber-1).getSizeX(); x++){
				for(int y = 1; y < storeys.get(storeyNumber-1).getSizeY(); y++){
					grid.add(new Point2D.Float((float)(x),(float)(y)));
				}
			}
		}
	}
	
	public void loadTextures(){
		ArrayList<HashMap> tempTextureHashMapArray = TextureLoader.loadTextureArray("textures/");
		int numberOfTextures = tempTextureHashMapArray.size();
		textureNames = new ArrayList<String>(numberOfTextures);
		textures = new ArrayList<Texture>(numberOfTextures);
		for(int i = 0; i<numberOfTextures;i++){
			HashMap<String, Texture> tempTextureHashMap = tempTextureHashMapArray .get(i);
			String tempTextureName = tempTextureHashMap.entrySet().iterator().next().getKey();
			Texture tempTexture = tempTextureHashMap.entrySet().iterator().next().getValue();
			textureNames.add(tempTextureName);
			textures.add(tempTexture);		
		}
		textureFileName = textureNames.get(0);	
	}
	
	@Override
	/**
	 * A function defined in GLEventListener. This function is called many times per second and should 
	 * contain the rendering code.
	 */
	public void display(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();

		// Set the clear color and clear the screen.
		gl.glClearColor(0.95f, 0.95f, 0.95f, 1);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		
		//Set the size of the line and points
		gl.glLineWidth(lineWidth);
		gl.glPointSize(pointSize);
		
		// Draw a figure based on the current draw mode and user input
		drawFigure(gl);
		
		// Draw the Floor or Roof according to the drawMode
		switch (drawMode) {
		case DM_OBJECT:
			drawFloors(gl);
			break;
		case DM_WALL:
			drawFloors(gl);
			break;
		case DM_FLOOR:
			drawFloors(gl); 
			break;
		case DM_ROOF:
			drawRoofs(gl);
			break;
		}
		
		// Draw the Walls
		drawWalls(gl);
		
		drawObjects(gl);
		
		//Draw the grid
		
		drawGrid(gl);
		
		//Delete the old points if neccesary
		deletePoints();

		// Flush the OpenGL buffer, outputting the result to the screen.
		gl.glFlush();
		
	
	}
	
	public WallList getWallList(){
		
		return wallList;
		
	}
	
	public FloorList getFloorList(){
			
		return floorList;
		
	}
	
	public RoofList getRoofList(){
		
		return roofList;
		
	}
	
	public ArrayList<Storey> getStoreys(){
		return storeys;
	}
	
	public void setStoreys(ArrayList<Storey> s){
		storeys = s;
	}
	
	public boolean loadFromFolder(String loadfolder) throws FileNotFoundException{
		grid.clear();
		storeys = new ArrayList<Storey>();
		try {
		    File folder = new File(loadfolder);
		    File[] tList = folder.listFiles();
		    int numberOfStoreys = tList.length;
		    for(int i = 0; i<tList.length;i++){
			    if(tList[i].getName().equals("Thumbs.db")){
			    	numberOfStoreys -= 1;
			    }  
		    }
			for(int i =1;i<numberOfStoreys+1;i++){
				File f = new File(loadfolder + "/Floor " + i);
				if(f.exists()){
					storey = Storey.Read(loadfolder + "/Floor " + i);
					storeys.add(storey);
				}
				else{
					storeys = new ArrayList<Storey>();
					numberOfStoreys = 0;
					return false;
				}

			}			
		} catch (FileNotFoundException e) {
			storeys = new ArrayList<Storey>();
			return false;
		}
		initGrid();
		return true;
	}
	
	public void changeStorey(int newStoreyNumber){
		storeyNumber = newStoreyNumber;
		initGrid();
	}

		
	public void drawGrid(GL gl){
		Point2D.Float p1 = new Point2D.Float();
		for(int i = 0; i < grid.size();i++){
			if(gridHighlight.equals(grid.get(i))){
				gl.glColor3f(0, 0.5f, 0f);
			}
			else{
				gl.glColor3f(0.0f, 0.0f, 0.0f);
			}
			p1.x = gridOffsetX + (grid.get(i).x-1)*gridDistance;
			p1.y = screenHeight - gridOffsetY - (grid.get(i).y-1)*gridDistance;
			pointOnScreen(gl,p1.x,p1.y);
		}
	}
	
	public void setDrawMode(int i){
		
		resetHighlighting();
		
		points.clear();
		gridpoints.clear();
		
		c.setCursor(-1);
		
		if(i == 1)
			drawMode = DM_WALL;
		else if(i == 2)
			drawMode = DM_ROOF;
		else if(i == 3)
			drawMode = DM_FLOOR;
		else if(i == 4){
			// Gummen
			drawMode = DM_ERASE;
			c.setCursor(0);
		}
		else if(i == 5){
			// Object tekenen
			drawMode = DM_OBJECT;
		}
	}
	
	public void setMode(int i){
		
		resetHighlighting();
		mode = i; // 1 = Draw, 2 = Erase
				
	}
	
	public void setWhatObject(int i){
		
		objectToDraw = i; // 1 = Ramp
		
	}
	
	public void setTexture(String textureName){
		textureFileName = "textures/" + textureName;
	}

	/**
	 * A method that draws a figure, when the user has inputted enough points
	 * for the current draw mode.
	 * 
	 * @param gl
	 */
	private void drawFigure(GL gl) {
		// Set line and point size, and set color to black.
		gl.glColor3f(0.0f, 0.0f, 0.0f);

		//Screen points
		Point2D.Float p1, p2, p3, p4;
		
		//Grid points
		Point2D.Float g1, g2, g3, g4;
		switch (drawMode) {
		case DM_OBJECT:
			if (points.size() >= 1) {

				p1 = gridpoints.get(0);
				
								
				p2 = new Point2D.Float(p1.x + 1, p1.y);
				p3 = new Point2D.Float(p1.x + 1, p1.y + 1);
				p4 = new Point2D.Float(p1.x, p1.y + 1);
				
				ArrayList<Point2D.Float> temp = new ArrayList<Point2D.Float>();
				temp.add(p1);
				temp.add(p2);
				temp.add(p3);
				temp.add(p4);
				
				Object obj = new ObjectRamp(temp, "brick.png");
				storeys.get(storeyNumber - 1).getObjectList().addObject(obj);
				
			}
			break;
		case DM_WALL:
			if (points.size() >= 2) {
				// If the draw mode is "line" and the user has supplied at least
				// two points, draw a line between those points
				g1 = gridpoints.get(0);
				g2 = gridpoints.get(1);
				wall = new Wall((int)g1.x,(int)g1.y,(int)g2.x,(int)g2.y,textureFileName);
				storeys.get(storeyNumber - 1).getWallList().addWall(wall);
			}
			break;
		case DM_FLOOR:
			if (points.size() >= 4) {
				// If the draw mode is "floor" and the user has supplied at least
				// three points, draw a line between those points
				g1 = gridpoints.get(0);
				g2 = gridpoints.get(1);
				g3 = gridpoints.get(2);
				g4 = gridpoints.get(3);
				ArrayList<Point2D.Float> p = new ArrayList<Point2D.Float>();
				p.add(g1);
				p.add(g2);
				p.add(g3);
				p.add(g4);
				floor = new Floor(p,textureFileName);
				storeys.get(storeyNumber - 1).getFloorList().addFloor(floor);
				}
			break;
		case DM_ROOF:
			if (points.size() >= 4) {
				// If the draw mode is "roof" and the user has supplied at least
				// three points, draw a line between those points
				g1 = gridpoints.get(0);
				g2 = gridpoints.get(1);
				g3 = gridpoints.get(2);
				g4 = gridpoints.get(3);
				ArrayList<Point2D.Float> p = new ArrayList<Point2D.Float>();
				p.add(g1);
				p.add(g2);
				p.add(g3);
				p.add(g4);
				roof = new Roof(p,textureFileName);
				storeys.get(storeyNumber - 1).getRoofList().addRoof(roof);
				}
			break;
		case DM_ERASE:
			
			// Erase dingen?
			
			break;
			}
		}
	
	
	
	private void drawWalls(GL gl){
		//Screen points
		if(storeys.size()>0){
			wallList = storeys.get(storeyNumber - 1).getWallList();
			Point2D.Float p1 = new Point2D.Float(), p2 = new Point2D.Float();
			for(int i = 0;i<wallList.getWalls().size();i++){
				p1.x = gridOffsetX + (wallList.getWalls().get(i).getStartx()-1)*gridDistance;
				p1.y = screenHeight -gridOffsetY - (wallList.getWalls().get(i).getStarty()-1)*gridDistance;
				p2.x = gridOffsetX + (wallList.getWalls().get(i).getEndx()-1)*gridDistance;
				p2.y = screenHeight - gridOffsetY - (wallList.getWalls().get(i).getEndy()-1)*gridDistance;
				if(wallToHighlight == i)
					wallOnScreen(gl, p1.x, p1.y, p2.x, p2.y, true);
				else
					wallOnScreen(gl, p1.x, p1.y, p2.x, p2.y, false);
			}
		}
	}
	
	private void drawObjects(GL gl){
		
		if(storeys.size()>0){
			objectList = storeys.get(storeyNumber - 1).getObjectList();
			ArrayList<Point2D.Float> p = new ArrayList<Point2D.Float>();
			
			for(int i = 0; i < objectList.getObjects().size();i++){
				
				if(objectList.getObjects().get(i) instanceof ObjectRamp){
					
					ObjectRamp or = (ObjectRamp) objectList.getObjects().get(i);
					
					for(int j = 0; j < or.getPoints().size(); j++){
						
						Point2D.Float p1 = new Point2D.Float();
						p1.x = gridOffsetX + (or.getPoints().get(j).x-1)*gridDistance;
						p1.y = screenHeight - gridOffsetY - (or.getPoints().get(j).y-1)*gridDistance;
						p.add(p1);
						
					}
					
				}
				
				/*
				for(int j = 0; j < objectList.getObjects().get(i).getPoints().size(); j++){
					
					Point2D.Float p1 = new Point2D.Float();
					p1.x = gridOffsetX + (floorList.getFloors().get(i).getPoints().get(j).x-1)*gridDistance;
					p1.y = screenHeight - gridOffsetY - (floorList.getFloors().get(i).getPoints().get(j).y-1)*gridDistance;
					p.add(p1);
					
				}
				*/
				
				int textureID = textureNames.lastIndexOf("textures/arrow.png");
				if(objectToHighlight == i)
					polygonOnScreen(gl,p,textureID, true, true);
				else
					polygonOnScreen(gl,p,textureID, true, false);
				
				p.clear();
				
			}
		}
	}
	
	private void drawFloors(GL gl){
		//Screen points
		if(storeys.size()>0){
			floorList = storeys.get(storeyNumber - 1).getFloorList();
			ArrayList<Point2D.Float> p = new ArrayList<Point2D.Float>();
			for(int i = 0; i < floorList.getFloors().size();i++){
				for(int j =0; j < floorList.getFloors().get(i).getPoints().size();j++){
					Point2D.Float p1 = new Point2D.Float();
					p1.x = gridOffsetX + (floorList.getFloors().get(i).getPoints().get(j).x-1)*gridDistance;
					p1.y = screenHeight - gridOffsetY - (floorList.getFloors().get(i).getPoints().get(j).y-1)*gridDistance;
					p.add(p1);
				}
				int textureID = textureNames.lastIndexOf(floorList.getFloors().get(i).getTexture());
				
				//System.out.println(wallToHighlight + " " + i);
				
				if(floorToHighlight == i)
					polygonOnScreen(gl,p,textureID, false, true);
				else
					polygonOnScreen(gl,p,textureID, false, false);	
				
				p.clear();
			}
		}
	}
	
	private void drawRoofs(GL gl){
		//Screen points
		if(storeys.size()>0){
			roofList = storeys.get(storeyNumber - 1).getRoofList();
			ArrayList<Point2D.Float> p = new ArrayList<Point2D.Float>();
			for(int i = 0; i < roofList.getRoofs().size();i++){
				for(int j =0; j < roofList.getRoofs().get(i).getPoints().size();j++){
					Point2D.Float p1 = new Point2D.Float();
					p1.x = gridOffsetX + (roofList.getRoofs().get(i).getPoints().get(j).x-1)*gridDistance;
					p1.y = screenHeight - gridOffsetY - (roofList.getRoofs().get(i).getPoints().get(j).y-1)*gridDistance;
					p.add(p1);
				}
				int textureID = textureNames.lastIndexOf(roofList.getRoofs().get(i).getTexture());
				if(roofToHighlight == i)
					polygonOnScreen(gl,p,textureID, false, true);
				else
					polygonOnScreen(gl,p,textureID, false, false);	
				p.clear();
			}
		}
	}
	
	
	
	
	/**
	 * Help method that uses GL calls to draw a point.
	 */
	private void pointOnScreen(GL gl, float x, float y) {
		//Aanpassen van de grootte van de punten hier
		//gl.glPointSize(5.0f);
		gl.glBegin(GL.GL_POINTS);
		gl.glVertex2f(x, y);
		gl.glEnd();
	}

	/**
	 * Help method that uses GL calls to draw a line.
	 */
	private void wallOnScreen(GL gl, float x1, float y1, float x2, float y2, boolean highlight) {
		if(highlight == false)
			gl.glColor3f(1.0f, 0.0f, 0.0f);
		else
			gl.glColor3f(0.0f, 1.0f, 0.0f);
		
		gl.glBegin(GL.GL_LINES);
		gl.glVertex2f(x1, y1);
		gl.glVertex2f(x2, y2);
		gl.glEnd();
	}
	
	private void polygonOnScreen(GL gl, ArrayList<Point2D.Float> p, int textureID, boolean obj, boolean highlight){
		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_TEXTURE);
		
		//Enable textures
		if(highlight == true)
			gl.glColor3f(0.0f, 1.0f, 0.0f);
		else
			gl.glColor3f(1.0f, 1.0f, 1.0f);
		
		gl.glEnable(GL.GL_TEXTURE_2D);
		//gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_DECAL);
		
		//Apply texture
		//gl.glBindTexture(GL.GL_TEXTURE_2D, texNamesArray[0]);
		textures.get(textureID).getTarget();
		//brickTexture.enable();
		textures.get(textureID).bind();		
		gl.glBegin(GL.GL_POLYGON);
//		for(int i =0; i<p.size();i++){
//			if(i ==0){
//				gl.glTexCoord2f(0.0f,1.0f);
//			} else if(i == 1){
//				gl.glTexCoord2f(0.0f,1.0f);
//			} else if(i == 2){
//				gl.glTexCoord2f(1.0f,1.0f);
//			} else if(i == 3){
//				gl.glTexCoord2f(1.0f,0.0f);
//			}
//			gl.glVertex2f(p.get(i).x, p.get(i).y);
//		}
		
		float numTex2 = (float) Math.ceil(disPoints(p.get(0), p.get(1)) / 60);
		float numTex1 = (float) Math.ceil(disPoints(p.get(1), p.get(2)) / 60);
		
		if(obj == true){
			numTex1 = 1;
			numTex2 = 1;
		}
		
		
		gl.glTexCoord2f(numTex2,0);
		
		gl.glVertex2f(p.get(0).x, p.get(0).y);
		gl.glTexCoord2f(0,0);
		gl.glVertex2f(p.get(1).x, p.get(1).y);
		gl.glTexCoord2f(0,numTex1);
		gl.glVertex2f(p.get(2).x, p.get(2).y);
		gl.glTexCoord2f(numTex2,numTex1);
		gl.glVertex2f(p.get(3).x, p.get(3).y);
		
		gl.glEnd();
		
		//Disable texture
		textures.get(textureID).disable();
		
		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
	}
	
	private double disPoints(Point2D.Float p1, Point2D.Float p2){
		
		double r = Math.sqrt(Math.pow((p2.y - p1.y), 2) + Math.pow((p2.x - p1.x), 2));	
		r = Math.round(r);
		return r;		
	}
	
//	private void roofOnScreen(GL gl, ArrayList<Point2D.Float> p){
//		gl.glColor3f(0.0f, 0.0f, 1.0f);
//		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);
//		gl.glBegin(GL.GL_POLYGON);
//		for(int i =0; i<p.size();i++){
//			gl.glVertex2f(p.get(i).x, p.get(i).y);
//		}
//		gl.glEnd();
//		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
//	}
		
	/**
	 * Help method that uses Gl calls to draw a triangle
	 */
	private void triangleOnScreen(GL gl, float x1, float y1, float x2, float y2, float x3, float y3) {
		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex2f(x1, y1);
		gl.glVertex2f(x2, y2);
		gl.glVertex2f(x3, y3);
		gl.glEnd();
	}
		
	/**
	 * Help method that uses GL calls to draw a square
	 */
	private void boxOnScreen(GL gl, float x, float y, float size) {
		gl.glBegin(GL.GL_QUADS);
		gl.glVertex2f(x, y);
		gl.glVertex2f(x + size, y);
		gl.glVertex2f(x + size, y + size);
		gl.glVertex2f(x, y + size);
		gl.glEnd();
	}
	
	private void deletePoints(){
		if (drawMode == DM_OBJECT && points.size() >= 1) {
			points.clear();
			gridpoints.clear();
		} else if (drawMode == DM_WALL && points.size() >= 2) {
			// If we're drawing lines and two points were already stored, reset the points list
			points.clear();
			gridpoints.clear();
		} else if (drawMode == DM_FLOOR && points.size() >= 4) {
			// If we're drawing Floors and four points were already stored, reset the points list
			points.clear();
			gridpoints.clear();
		} else if (drawMode == DM_ROOF && points.size() >= 4) {
			// If we're drawing Roofs and four points were already stored, reset the points list
			points.clear();
			gridpoints.clear();
		}
	}

	@Override
	/**
	 * A function defined in GLEventListener. This function is called when there is a change in certain 
	 * external display settings. 
	 */
	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
			boolean deviceChanged) {
		// Not needed.
	}

	@Override
	/**
	 * A function defined in GLEventListener. This function is called when the GLCanvas is resized or moved. 
	 * Since the canvas fills the frame, this event also triggers whenever the frame is resized or moved.
	 */
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		GL gl = drawable.getGL();

		// Set the new screen size and adjusting the viewport
		screenWidth = width;
		screenHeight = height;
		buttonSize = height / 10.0f;
		gl.glViewport(0, 0, screenWidth, screenHeight);

		// Update the projection to an orthogonal projection using the new screen size
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrtho(0, screenWidth, 0, screenHeight, -1, 1);
	}

	@Override
	/**
	 * A function defined in MouseListener. Is called when the pointer is in the GLCanvas, and a mouse button is released.
	 */
	public void mouseReleased(MouseEvent me) {

		
		// Drawing
		if(mode == 1){
		
			if((gridHighlight.x >= ((me.getX()-gridOffsetX)/gridDistance+1)-0.1) 
					&& (gridHighlight.x <= ((me.getX()-gridOffsetX)/gridDistance+1)+0.1)
					&& (gridHighlight.y >= ((me.getY()-gridOffsetY)/gridDistance+1)-0.1)
					&& (gridHighlight.y <= ((me.getY()-gridOffsetY)/gridDistance+1)+0.1)){	
				if (drawMode == DM_OBJECT && points.size() >= 1) {
					// If we're placing objects and one point was stored, reset the points list
					points.clear();
					gridpoints.clear();
				} else if (drawMode == DM_WALL && points.size() >= 2) {
					// If we're drawing lines and two points were already stored, reset the points list
					points.clear();
					gridpoints.clear();
				} else if (drawMode == DM_FLOOR && points.size() >= 4) {
					// If we're drawing Floors and four points were already stored, reset the points list
					points.clear();
					gridpoints.clear();
				} else if (drawMode == DM_ROOF && points.size() >= 4) {
					// If we're drawing Roofs and four points were already stored, reset the points list
					points.clear();
					gridpoints.clear();
				}
				// Add a new point to the points list.
				points.add(new Point2D.Float((float)(gridHighlight.x*gridDistance),(float)(screenHeight - gridHighlight.y*gridDistance)));
				gridpoints.add(gridHighlight);
				System.out.println(gridHighlight.x*gridDistance + " " + (screenHeight - gridHighlight.y*gridDistance));
				c.setCursor(-1);
				
			}
			// Objecten tekenen
			else if (drawMode == DM_OBJECT && points.size() == 0){
					
				ArrayList<Object> tempList = objectList.getObjects();
				
				for(int i = 0; i < tempList.size(); i++){
		
					ArrayList<Point2D.Float> ps = null;
					
					if(tempList.get(i) instanceof ObjectRamp){
						ObjectRamp temp = (ObjectRamp) tempList.get(i);
						ps = temp.getPoints();
					}
					
					float hiX = Integer.MIN_VALUE;
					float hiZ = Integer.MIN_VALUE;
					
					float loX = Integer.MAX_VALUE;
					float loZ = Integer.MAX_VALUE;
					
					for(int j = 0; j < ps.size(); j++){
					
						if(ps.get(j).x > hiX)
							hiX = ps.get(j).x;
						
						if(ps.get(j).y > hiZ)
							hiZ = ps.get(j).y;
						
						if(ps.get(j).x < loX)
							loX = ps.get(j).x;
					
						if(ps.get(j).y < loZ)
							loZ = ps.get(j).y;
							
					}
					
					float clickedX = (me.getX() - gridOffsetX ) / gridDistance + 1;
					float clickedY = (me.getY() - gridOffsetY ) / gridDistance + 1;
					
					if(clickedX > loX && clickedX < hiX && clickedY > loZ && clickedY < hiZ){
						
						ArrayList<Point2D.Float> newPoints = new ArrayList<Point2D.Float>();
						newPoints.add(ps.get(1));
						newPoints.add(ps.get(2));
						newPoints.add(ps.get(3));
						newPoints.add(ps.get(0));
						
						ObjectRamp newRamp = new ObjectRamp(newPoints, "textures/arrow.png");
						objectList.getObjects().set(i, newRamp);
						
						
					}
					
				}
				
			}
		
		// Erase
		}else{
	
			if(wallToHighlight != -1){
					
				wallList.getWalls().remove(wallToHighlight);
				
				wallToHighlight = -1;
				
			}
					
			if(floorToHighlight != -1){
				
				floorList.getFloors().remove(floorToHighlight);
				
				floorToHighlight = -1;
				
			}
			
			if(roofToHighlight != -1){
				
				roofList.getRoofs().remove(roofToHighlight);
				
				roofToHighlight = -1;
				
			}
			
			if(objectToHighlight != -1){
				
				objectList.getObjects().remove(objectToHighlight);
				
				objectToHighlight = -1;

			}
			
		}
		
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// Not needed.
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// Not needed.
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// Not needed.
	}

	@Override
	public void mousePressed(MouseEvent me) {
		gridDragX = me.getX();
		gridDragY = me.getY();
	}
	
	@Override
	public void mouseMoved(MouseEvent me) {
		
		Point2D.Float p1 = new Point2D.Float();
		p1.x = me.getX();
		p1.y = me.getY();
		gridHighlight = new Point2D.Float();
		boolean highlighting = false;
		
		for(int i = 0; i<grid.size();i++){
			if(((grid.get(i).x) >= (((p1.x-gridOffsetX)/gridDistance+1)-0.1) 
					&& grid.get(i).x <= ((p1.x-gridOffsetX)/gridDistance+1)+0.1)
					&&((grid.get(i).y) >= (((p1.y-gridOffsetY)/gridDistance+1)-0.1)
					&& grid.get(i).y <= ((p1.y-gridOffsetY)/gridDistance+1)+0.1)){
				gridHighlight = grid.get(i);
				highlighting = true;
						
			}
			
		}
		
		if(highlighting == true && mode == 1)
			c.setCursor(-2);
		else if(mode == 2)
			c.setCursor(-3);
		else
			c.setCursor(-1);
		
		// Drawing
		if(mode == 1){
		
			if (drawMode == DM_OBJECT && points.size() == 0){
				
				ArrayList<Object> tempList = objectList.getObjects();
				
				for(int i = 0; i < tempList.size(); i++){
	
					ArrayList<Point2D.Float> ps = null;
					
					if(tempList.get(i) instanceof ObjectRamp){
						ObjectRamp temp = (ObjectRamp) tempList.get(i);
						ps = temp.getPoints();
					}
					
					float hiX = Integer.MIN_VALUE;
					float hiZ = Integer.MIN_VALUE;
					
					float loX = Integer.MAX_VALUE;
					float loZ = Integer.MAX_VALUE;
					
					for(int j = 0; j < ps.size(); j++){
					
						if(ps.get(j).x > hiX)
							hiX = ps.get(j).x;
						
						if(ps.get(j).y > hiZ)
							hiZ = ps.get(j).y;
						
						if(ps.get(j).x < loX)
							loX = ps.get(j).x;
					
						if(ps.get(j).y < loZ)
							loZ = ps.get(j).y;
							
					}
					
					float clickedX = (me.getX() - gridOffsetX ) / gridDistance + 1;
					float clickedY = (me.getY() - gridOffsetY ) / gridDistance + 1;
					
					if(clickedX > loX && clickedX < hiX && clickedY > loZ && clickedY < hiZ){
						c.setCursor(1);			
					}
			
				}
			
			}
			
		// Erase
		}else{
			
			float gridX = (me.getX() - gridOffsetX ) / gridDistance + 1;
			float gridY = (me.getY() - gridOffsetY ) / gridDistance + 1;
			
			Point2D.Float mouseInGrid = new Point2D.Float(gridX, gridY);
			
			// Highlight wall to erase
			if(drawMode == DM_WALL){
			
				ArrayList<Wall> tempList = storeys.get(storeyNumber - 1).getWallList().getWalls();;
				
				wallToHighlight = -1;
				
				for(int i = 0; i < tempList.size(); i++){
				
					if(distToSegment(gridX, gridY, tempList.get(i).getStartx(), tempList.get(i).getStarty(), tempList.get(i).getEndx(), tempList.get(i).getEndy()) < 0.05){
						wallToHighlight = i ;
					}
					
				}
				
			}else if(drawMode == DM_FLOOR){
				
				ArrayList<Floor> tempList = storeys.get(storeyNumber - 1).getFloorList().getFloors();;
				
				floorToHighlight = -1;
				
				for(int i = 0; i < tempList.size(); i++){
					
					Floor tempFloor = tempList.get(i);
					Point2D.Float[] tempPoints = new Point2D.Float[4];
					
					for(int j = 0; j < tempFloor.getPoints().size(); j++){
						
						tempPoints[j] = tempFloor.getPoints().get(j);
						
					}
					
					if(inPolygon(mouseInGrid, tempPoints)){
						
						floorToHighlight = i;
						
					}
					
				}
				
			}else if(drawMode == DM_ROOF){
				
				ArrayList<Roof> tempList = storeys.get(storeyNumber - 1).getRoofList().getRoofs();;
				
				roofToHighlight = -1;
				
				for(int i = 0; i < tempList.size(); i++){
					
					Roof tempRoof = tempList.get(i);
					Point2D.Float[] tempPoints = new Point2D.Float[4];
					
					for(int j = 0; j < tempRoof.getPoints().size(); j++){
						
						tempPoints[j] = tempRoof.getPoints().get(j);
						
					}
					
					if(inPolygon(mouseInGrid, tempPoints)){
						
						roofToHighlight = i;
						
					}
					
				}
				
			}else if(drawMode == DM_OBJECT){
				
				ArrayList<Object> tempList = storeys.get(storeyNumber - 1).getObjectList().getObjects();
				
				objectToHighlight = -1;
				
				for(int i = 0; i < tempList.size(); i++){
					
					ObjectRamp tempObject = null;
					
					if(tempList.get(i) instanceof ObjectRamp){
						tempObject = (ObjectRamp) tempList.get(i);
					}
						
					
					Point2D.Float[] tempPoints = new Point2D.Float[4];
					
					for(int j = 0; j < tempObject.getPoints().size(); j++){
						
						tempPoints[j] = tempObject.getPoints().get(j);
						
					}
					
					if(inPolygon(mouseInGrid, tempPoints)){
						
						objectToHighlight = i;
						
					}
					
				}
				
			}
			
		}
		
		
	}
	
	public void resetHighlighting(){
		
		floorToHighlight = -1;
		roofToHighlight = -1;
		wallToHighlight = -1;
		//floorToHighlight = -1;
		
	}
	
	public boolean inPolygon(Point2D.Float test, Point2D.Float[] points) {
		int i;
		int j;
		boolean result = false;
		for (i = 0, j = points.length - 1; i < points.length; j = i++) {
			if ((points[i].y > test.y) != (points[j].y > test.y) &&
				(test.x < (points[j].x - points[i].x) * (test.y - points[i].y) / (points[j].y-points[i].y) + points[i].x)) {
				result = !result;
			}
		}
		return result;
	}
	
	public double dist2(double vx, double vy, double wx, double wy) { return Math.pow(vx - wx, 2) + Math.pow(vy - wy, 2); }
	
	public double distToSegmentSquared(double px, double py, double vx, double vy, double wx, double wy) {
	  double l2 = dist2(vx, vy, wx, wy);
	  if (l2 == 0) return dist2(px, py, vx, vy);
	  double t = ((px - vx) * (wx - vx) + (py - vy) * (wy - vy)) / l2;
	  if (t < 0) return dist2(px, py, vx, vy);
	  if (t > 1) return dist2(px, py, wx, wy);
	  return dist2(px, py, vx + t * (wx - vx), vy + t * (wy - vy));
	}	
	
	public double distToSegment(double px, double py, double vx, double vy, double wx, double wy) 
	{ 
		return Math.sqrt(distToSegmentSquared(px, py, vx, vy, wx, wy)); 
	}
	
	@Override
	public void mouseDragged(MouseEvent me) {
		gridOffsetX += me.getX()- gridDragX;
		gridOffsetY += me.getY()- gridDragY;
		gridDragX = me.getX();
		gridDragY = me.getY();
		
		c.setCursor(0);
		
		resetHighlighting();
		
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent me) {
		gridDistance -= gridDistance*0.05*me.getWheelRotation();
		lineWidth -= lineWidth*0.05*me.getWheelRotation();
		pointSize -= pointSize*0.05*me.getWheelRotation();
	}
	

}
