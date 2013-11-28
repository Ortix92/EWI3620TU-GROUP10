import java.io.File;
import java.io.IOException;

import javax.media.opengl.GL;

import com.sun.opengl.util.GLUT;


public class Enemy extends GameObject implements VisibleObject {
	private Maze maze; 										// The maze.
	private double newX, newZ;
	private double speed = 0.0015;
	private Model m ;
	private int displayList;
	private double sx, sy,sz;
	private boolean alert;
	
	public Enemy(double x, double y, double z){
		super(x, y, z);
		sx=x;
		sy=y;
		sz=z;
		alert = false;
		try {
			m = OBJLoader.loadModel((new File("3d_object/lion.obj")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public double getSpeed() {
		return speed;
	}
	
	public void setSpeed(double speed) {
		this.speed = speed;
	}
	
	public boolean checkWall(double x, double z, double dT){
		double d = 2.0d; 		//distance from the wall
		boolean res = false;
		
		for(int i = 0; i < 360; i = i + 15)
			if(maze.isWall( x+d*Math.sin(i*Math.PI/180) , locationY , z-0.8f+d*Math.cos(i*Math.PI/180) ))
				res = true;
		
		return res;
	}
	
	public void genDisplayList(GL gl){
        displayList = OBJLoader.createDisplayList(m, gl);
	}
		
	public void update(int deltaTime, Player player){
		if(alerted(player)){
			alert = alerted(player);
		}
		if(alert){
			double dX = player.locationX - locationX;
			if(dX > 0)
				dX = speed * deltaTime;
			if(dX < 0)
				dX = -1 * speed * deltaTime;
			
			newX = locationX + dX;
					
			double dZ = player.locationZ - locationZ;
			if(dZ > 0)
				dZ = speed * deltaTime;
			if(dZ < 0)
				dZ = -1 * speed * deltaTime;
			
			newZ = locationZ + dZ;
			
			if(!checkWall(newX, newZ, deltaTime)){
				locationX = newX;
				locationZ = newZ;
			}else if(!checkWall(newX, locationZ, deltaTime)){
				locationX = newX;
			}else if(!checkWall(locationX, newZ, deltaTime)){
				locationZ = newZ;
			}
		}
		
		this.caught(player);
	}

	public void display(GL gl) {
		//GLUT glut = new GLUT();
		
		gl.glColor3f(1, 0, 0);
		
		gl.glPushMatrix();
		gl.glTranslated(locationX, locationY, locationZ);
		if(displayList == 0){
			gl.glCallList(1);	
		}
		else{
			gl.glCallList(displayList);
		}
		//glut.glutSolidSphere(2.0d, 10, 10);
		gl.glPopMatrix();
		
	}
	
	public void getMaze(Maze maze){
		this.maze = maze;
	}

	public void caught(Player player){
		if( Math.abs(locationX - player.locationX) < 1
				&& Math.abs(locationZ - player.locationZ) < 1
				&& Math.abs(locationY - player.locationY) < 0.8*maze.SQUARE_SIZE ){
			MainClass.state.GameStateUpdate(GameState.GAMEOVER_STATE);
			MainClass.state.setStopMainGame(true);
			MainClass.state.setStopGameOver(false);
		}
	}
	
	public boolean alerted (Player player){
		 boolean res = Math.sqrt(Math.pow(sx-player.locationX,2 )+Math.pow(sz-player.locationZ,2)) <15 ;
		 return res;
	}

}
