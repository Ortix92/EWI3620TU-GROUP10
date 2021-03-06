
public class GameStateManager {
	
	protected int gamestate = 0;
	protected boolean sTitle=false,
					sMainGame=false,
					sPause=false,
					sGameOver=false;
	
	private CursorHandler c = new CursorHandler(MainClass.canvas);
	
	void GameStateUpdate(GameState m_curSTATE) {
	     // handle update
		
	    switch(m_curSTATE) {
	       case TITLE_STATE:
	          //
	    	   //MainMenu.setTeller(0);
	    	   gamestate = 0;
	          break;
	       case MAINGAME_STATE:
	          //
	    	   //MainMenu.setTeller(1);
	    	   c.setCursor(2);
	    	   gamestate = 1;
	          break;
	       case PAUSE_STATE:
	           //
	    	   gamestate = 2;
	    	   //MainMenu.setTeller(2);
	           break;
	       case STOP_STATE:
	           //
	    	   gamestate = 3;
	    	   //MainMenu.setTeller(3);
	           break;
	       case GAMEOVER_STATE:
	    	   //
	    	   gamestate = 4;
	    	   //MainMenu.setTeller(4);
	    	   break;
	    }
	}
	
	public int getState(){
		return gamestate;
	}
	
	
	public boolean getStopTitle(){
		return sTitle;
	}
	
	public boolean getStopMainGame(){
		return sMainGame;
	}
	
	public boolean getStopPause(){
		return sPause;
	}
	
	public boolean getStopGameOver(){
		return sGameOver;
	}
	
	public void setStopTitle(boolean s){
		sTitle=s;
	}
	
	public void setStopMainGame(boolean s){
		sMainGame=s;
	}
	
	public void setStopPause(boolean s){
		sPause=s;
	}
	
	public void setStopGameOver(boolean s){
		sGameOver=s;
	}
}

enum GameState {
    TITLE_STATE,
    MAINGAME_STATE,
    PAUSE_STATE,
    STOP_STATE,
    GAMEOVER_STATE
}


