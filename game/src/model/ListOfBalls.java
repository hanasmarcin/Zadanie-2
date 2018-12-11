package model;

import java.util.ArrayList;

public class ListOfBalls extends ArrayList<Ball>{
	
	private static final long serialVersionUID = 1L;

	public enum Orientation {
		DIAGONAL_NE_WS, DIAGONAL_NW_SE, HORIZONTAL
	};
	
	public Ball getBall (int posX, int posY) {
		for (int i=0; i<this.size(); i++) {
			Ball ball = this.get(i);
			if (ball.getPosX() == posX && ball.getPosY() == posY ) return ball;			
		}
		return null;
	}
}
