package model;

public enum Move {
	E(0, 2, 0, 0, false), SE(1, 1, 1, 60, false), SW(2, -1, 1, 120, false), W(3, -2, 0, 180, false), NW(4, -1, -1, 240, false), NE(5, 1, -1, 300, false);
	private int id, dx, dy, angle;
	private boolean isPossible;

	Move(int id, int dx, int dy, int angle, boolean isPossible) {
		this.id = id;
		this.dx = dx;
		this.dy = dy;
		this.angle = angle;
		this.isPossible = isPossible;
	}

	public void setPossible(boolean isPossible) {
		this.isPossible = isPossible;
	}

	public boolean isPossible() {
		return isPossible;
	}

	public int id() {
		return id;
	}

	public int dx() {
		return dx;
	}

	public int dy() {
		return dy;
	}

	public int angle() {
		return angle;
	}

	public static Move getFromID(int id) {
		switch (id) {
		case 0:
			return E;
		case 1:
			return SE;
		case 2:
			return SW;
		case 3:
			return W;
		case 4:
			return NW;
		case 5:
			return NE;
		default:
			return null;
		}
	}
};