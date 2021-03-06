import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;

import javax.media.opengl.GL;

import com.sun.opengl.util.GLUT;
import com.sun.opengl.util.texture.Texture;


public class Enemy extends GameObject implements VisibleObject {
	private Maze maze; 										// The maze.
	private double newX, newZ;
	private double speed = 0.0015;
	private Model m ;
	private int displayList;
	private double sx, sy,sz, px, py,pz;
	private boolean alert;
	public boolean dood = false;
	private boolean texture;
	private IntBuffer vboHandle = IntBuffer.allocate(10);
	private int attackTimeout = 0;
	private double angle;
	private int deathAngle = 0;
	private Pheromone highestPher;
	//Shaders
	private int shaderProgram = 0;
	
	//Attack
	private int attackPower = 10;
	
	private int health = 100;
	
	public Enemy(double x, double y, double z, double angle,boolean tex){
		super(x, y, z);
		sx=x;
		sy=y;
		sz=z;
		this.angle = angle;
		alert = false;
		texture = tex;
		try {
			if(texture){
				m = OBJLoader.loadTexturedModel((new File("3d_object/Predator_Youngblood/Predator_Youngblood.obj")));
			}
			else{
				m = OBJLoader.loadModel((new File("3d_object/Predator_Youngblood/Predator_Youngblood.obj")));
			}

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
	
	public void setShaderProgram(int program){
		shaderProgram = program;
	}
	
	public boolean checkWall(double x, double z, double dT){
		double d = 2.0d; 		//distance from the wall
		boolean res = false;
		
		for(int i = 0; i < 360; i = i + 15)
			if(maze.isWall( x+d*Math.sin(i*Math.PI/180) , locationY , z-0.8f+d*Math.cos(i*Math.PI/180) ))
				res = true;
		
		return res;
	}
	
//	public void genDisplayList(GL gl){
//        displayList = OBJLoader.createDisplayList(m, gl);
//	}
	
	public void genVBO(GL gl){
		vboHandle = OBJLoader.createVBO(m, gl);
	}
		
	public void update(int deltaTime, Player player){
		px = player.locationX;
		py = player.locationY;
		pz = player.locationZ;
		if(!dood){
			if(alerted(player)){
				alert = alerted(player);
			}
			if(alert){
				highestPher = MainClass.mazePheromones.Search(locationX, locationY, locationZ, 15);
				
//				System.out.println("enemy: " + highestPher.x + " , " + highestPher.z);
				
				double dX = highestPher.x - locationX;				
				double dZ = highestPher.z - locationZ;				
				double distance = Math.sqrt(dZ*dZ + dX*dX);
				
				newX = locationX;		
				newZ = locationZ;
				
				if(distance > 0.01){
					dX = dX/distance * speed * deltaTime;
					dZ = dZ/distance * speed * deltaTime;
					
					newX += dX;
					newZ += dZ;
					
					if(!checkWall(newX, newZ, deltaTime)){
						locationX = newX;
						locationZ = newZ;
					}
					
					else{
						newX = locationX;
						if(dX > 0.0){
							newX += speed * deltaTime;
						}
						else if(dX < 0.0){
							newX -= speed * deltaTime;
						}
							
						newZ = locationZ;
						if(dZ > 0.0){
							newZ += speed * deltaTime;
						}
						
						else if(dZ < 0.0){
							newZ -= speed * deltaTime;
						}
				
						if(!checkWall(newX, locationZ, deltaTime)){
							locationX = newX;
						}else if(!checkWall(locationX, newZ, deltaTime)){
							locationZ = newZ;
						}
					}
				}
			}
			
			this.caught(player);
		}
	}

	public void display(GL gl) {
		gl.glPushMatrix();
		//gl.glEnable(GL.GL_TEXTURE_NORMAL_EXT);
		//Enable the shaderprogram
		if(shaderProgram >0){
			gl.glUseProgram(shaderProgram);
		}
		
		//Draw nothing if the vboHandle are not loaded
		if(vboHandle.get(0)<=0||vboHandle.get(1)<=0){
			
		}
		
		else{
			//Translate the model to the right location
			gl.glTranslated(locationX, locationY, locationZ);
			
			if(alert && !dood){
				//berekening hoek
					double inP = highestPher.z-locationZ;
					double lengteV = 1;
					double lengteW = Math.sqrt(Math.pow(highestPher.x-locationX, 2)+Math.pow(highestPher.z-locationZ, 2));
					double test = inP/Math.max(lengteV*lengteW, 00001);
					angle = Math.acos(test)*180/Math.PI;
					if(highestPher.x<locationX){
						angle = -angle;
					}
					
				}
			else if(dood){
				if(deathAngle>-90){
					deathAngle -= 2.5;
				}
			}
			gl.glRotated(angle,0, 1, 0);
			gl.glRotated(deathAngle, 1, 0, 0);
			//Reset the color to white
			gl.glClearColor(1.0f, 1.0f, 1.0f, 1);
			gl.glColor3f(1.0f,1.0f,1.0f);
			//Initialize counters
			int vertexSize = 0;
			int vertexCount = 0;
			
			//Initialize the texture
			Texture tempTexture = null;
			for(int i=0;i<m.getModelParts().size();i++){
				gl.glColor3f(1.0f,1.0f,1.0f);
				ModelPart p = m.getModelParts().get(i);
				ModelPart.Face face = p.getFaces().get(0);
				
				//Enable the Array draw Mode for vertex,normals and textures
				gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
				gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
				gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
				
				//the modelPart has textureCoordinates so the material should be used
				if(face.hasMaterial() && !face.hasTexture()){
	                gl.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, new float[] {1f,face.getMaterial()
                            .diffuseColour[0], face.getMaterial().diffuseColour[1],
                            face.getMaterial().diffuseColour[2]},1);
	                gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT, new float[] {1f,face.getMaterial()
                            .ambientColour[0], face.getMaterial().ambientColour[1],
                            face.getMaterial().ambientColour[2]}, 1);
	                gl.glMaterialf(GL.GL_FRONT, GL.GL_SHININESS, face.getMaterial().specularCoefficient);
				}
				
				//Use default material
				else if (!face.hasTexture()){
	                gl.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, new float[] {1.0f,1.0f, 1.0f, 1.0f}, 1);
					gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT, new float[] {1.0f,1.0f, 1.0f, 1.0f}, 1);
	                gl.glMaterialf(GL.GL_FRONT, GL.GL_SHININESS, 120f);
				}
				
				//Bind the vbo buffers for the normal and vertex arrays
                vertexSize = p.getFaces().size()*3;
				gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboHandle.get(2));
				gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, 0L);
				gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboHandle.get(0));
				gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0L);
				gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboHandle.get(1));
				gl.glNormalPointer(GL.GL_FLOAT, 0, 0L);
				
				//Enable the texture if the modelPart has a texture
				if(face.hasTexture()){
					gl.glActiveTexture(GL.GL_TEXTURE0);
					gl.glEnable(GL.GL_TEXTURE_2D);
					tempTexture = face.getTexture();
					tempTexture.bind();
				}
				
				//Draw the arrays
				gl.glDrawArrays(GL.GL_TRIANGLES, vertexCount, vertexSize);
				
				//Enable the Array draw Mode for vertex,normals and textures
				gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
				gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
				gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
				gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
				
				//Keep track of the size of the modelParts. This 
				vertexCount += vertexSize;
				
				//Disable the texture i
				if(face.hasTexture()){
					tempTexture.disable();
					gl.glDisable(GL.GL_TEXTURE_2D);
				}
			}
		}
		
		//Disable shaderprograms
		gl.glUseProgram(0);	
		
		gl.glPopMatrix();
		
		
		//healthbar enemy
		if(!dood){
			gl.glPushMatrix();
			
			gl.glTranslated(locationX, locationY+5,locationZ);
			
			//berekening hoek
				double inP = px-locationX;
				double lengteV = 1;
				double lengteW = Math.sqrt(Math.pow(px-locationX, 2)+Math.pow(pz-locationZ, 2));
				double test = inP/Math.max(lengteV*lengteW, 00001);
				angle = Math.acos(test)*180/Math.PI;
				if(pz>locationZ){
					angle = -angle;
				}
			gl.glRotated(angle,0, 1, 0);
			
			gl.glEnable(GL.GL_COLOR_MATERIAL);
			
			gl.glColor3f(0.2f, 0.2f, 0.2f);
			
			gl.glBegin(GL.GL_QUADS);
				gl.glVertex3d(-0.001, -0.05, 1.05);
				gl.glVertex3d(-0.001, -0.05, -1.05);
				gl.glVertex3d(-0.001, 0.55, -1.05);
				gl.glVertex3d(-0.001, 0.55, 1.05);
			gl.glEnd();
			
			gl.glColor3f(1.0f, 0.0f, 0.0f);
			gl.glBegin(GL.GL_QUADS);
				gl.glVertex3d(0.0, 0.0, 1.0);
				gl.glVertex3d(0.0, 0.0, 1.0-(2.0*health/100.0));
				gl.glVertex3d(0.0, 0.5, 1.0-(2.0*health/100.0));
				gl.glVertex3d(0.0, 0.5, 1.0);
			gl.glEnd();
			gl.glColor3f(1.0f, 1.0f, 1.0f);
			gl.glDisable(GL.GL_COLOR_MATERIAL);
			gl.glPopMatrix();
			
		}
	}
	
	public void getMaze(Maze maze){
		this.maze = maze;
	}

	public void caught(Player player){
		if( Math.abs(locationX - player.locationX) < 1
				&& Math.abs(locationZ - player.locationZ) < 1
				&& Math.abs(locationY - player.locationY) < 0.8*maze.SQUARE_SIZE ){
			if(attackTimeout == 0){
				player.setDeltaHealth(Math.min(0, -attackPower+player.getDefensePower()));
				attackTimeout = 10;
			} else{
				attackTimeout--;
			}
			if(player.getHealth() <= 0){
				MainClass.state.GameStateUpdate(GameState.GAMEOVER_STATE);
				MainClass.state.setStopMainGame(true);
				MainClass.state.setStopGameOver(false);
			}
		}
	}
	
	public boolean alerted (Player player){
		 boolean res = Math.sqrt(Math.pow(sx-player.locationX,2 )+Math.pow(sz-player.locationZ,2)) <15 ;
		 return res;
	}
	
	public void damage(double x, double y, double z, double h, double d){
		if(locationX+3>x && x>locationX-3){
			double r=Math.sqrt(Math.pow(3,2)+Math.pow(x,2));
			if(locationZ+r>z && z>locationZ-r){
				health -=d;
				if(health<=0){
					dood = true;
				}
			}
		}
	}
}
