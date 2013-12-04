import java.util.ArrayList;
import java.util.PriorityQueue;


public class MazePheromones {
	private PriorityQueue<Pheromone> pheromonesOrder;
	private ArrayList<Pheromone> pheromones;
	private Pheromone lastPher;
	
	
	public MazePheromones(){
		pheromonesOrder = new PriorityQueue<Pheromone>();
		pheromones = new ArrayList<Pheromone>();
	}
	
	public void addPher(double x, double y, double z){
		Pheromone newpheromone = new Pheromone(x, y, z);
		
		if(pheromonesOrder.contains(newpheromone)){
			pheromonesOrder.remove(newpheromone);
		}

		if(pheromonesOrder.size() > 1){
			if(pherDistance(lastPher, newpheromone) > 0.5){
				pheromonesOrder.add(newpheromone);
				lastPher = newpheromone;
			}
		}
		else{
			pheromonesOrder.add(newpheromone);
			lastPher = newpheromone;
		}
		
//		System.out.println("player: " + newpheromone.x + " , " + newpheromone.z);
		
		pheromones = new ArrayList<Pheromone>(pheromonesOrder);
	}
	
	public void evapPheromones(){
		for( Pheromone pher : pheromones ){
			pher.evapPher();
		}
		for(int i = pheromones.size()-1; i >= 0; i--){
			if(pheromones.get(i).pheromone < 5){
				pheromones.remove(i);
//				System.out.println("pheromone deleted");
			}
		}
//		System.out.println(pheromones.size());
	}
	
	public double pherDistance(Pheromone pher1, Pheromone pher2){
		double dx = Math.abs(pher1.x - pher2.x);
		double dy = Math.abs(pher1.z - pher2.z);
		
		return Math.sqrt(dx*dx + dy+dy);
	}
	
	public boolean obstructed(double x, double deltaX, double z, double deltaZ, double y){
		for( int i = 0; i < 200; i++ ){
			double tx = x + deltaX*i/200.0;
			double tz = z + deltaZ*i/200.0;
			if( MainClass.maze.isWall(tx, y, tz) )
				return true;
		}
		return false;
//		System.out.println(obstructed);
	}
	
	/**
	 * returns highest pheromone within vision
	 * @param x
	 * @param y
	 * @param z
	 * @param vision
	 * @return
	 */
	public Pheromone Search(double x, double y, double z, double vision){
		Pheromone highestPher = new Pheromone(x,y,z);
		highestPher.setPher(0);									//intial highest pheromone with pheromone = 0
		
		for( Pheromone pher : pheromones){
			double dx = pher.x - x;
			double dy = pher.y+1.25 - y;
			double dz = pher.z - z;
			double distance = Math.sqrt(dx*dx + dz*dz);			//calculate distance to current pheromone
			
			if( distance <= vision /*&& Math.abs(dy) <= 1.25*/ ){	//pheromone within vision?
				if( pher.pheromone > highestPher.pheromone ){		//current pheromone higher than highest?
					if(!obstructed(x, dx, z, dz, y))
						highestPher = pher;
				}
			}
			
			if(highestPher.pheromone > 0)
				break;
		}
		
//		System.out.println(pheromones.get(0).pheromone);
		
		return highestPher;
	}
	
}
