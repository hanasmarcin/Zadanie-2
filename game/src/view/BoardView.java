package view;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import view.Color;

import static view.Color.*;

import java.util.ArrayList;
import java.util.List;

public class BoardView {

	private final static Image blackBallImg;
	private final static Image whiteBallImg;
	private final static Image blackChosenBallImg;
	private final static Image whiteChosenBallImg;
	private final static Image moveButtonImg;
	private final static ImageView BORDER;

	static {
		blackBallImg = new Image(BoardView.class.getResourceAsStream("black_ball.png"));
		whiteBallImg = new Image(BoardView.class.getResourceAsStream("white_ball.png"));
		blackChosenBallImg = new Image(BoardView.class.getResourceAsStream("black_chosen_ball.png"));
		whiteChosenBallImg = new Image(BoardView.class.getResourceAsStream("white_chosen_ball.png"));
		moveButtonImg = new Image(BoardView.class.getResourceAsStream("move.png"));
		BORDER = new ImageView();
	}

	private ImageView[][] board;
	private List<ImageView> moveButtons;
	private GridPane boardPane;

	public BoardView(GridPane boardPane) {
		this.board = new ImageView[22][11];
		this.moveButtons = new ArrayList<>();
		this.boardPane = boardPane;
	}

	/*
	 * private Image getBallImage(Color color, boolean isChosen) { if (isChosen)
	 * return color == BLACK ? blackChosenBallImg : whiteChosenBallImg; else return
	 * color == BLACK ? blackBallImg : whiteBallImg; }
	 */

	private Image getBallImage(ImageView ball, boolean isChosen) {
		Image img = ball.getImage();
		if (img.equals(blackChosenBallImg) || img.equals(blackBallImg)) {
			if (isChosen)
				return blackChosenBallImg;
			else
				return blackBallImg;
		} else if (img.equals(whiteChosenBallImg) || img.equals(whiteBallImg)) {
			if (isChosen)
				return whiteChosenBallImg;
			else
				return whiteBallImg;
		} else
			return null;
	}

	public ImageView setChosen(int x, int y) {
		board[x][y].setImage(getBallImage(board[x][y], true));
		return board[x][y];
	}

	public List<ImageView> setChosenLine(int x1, int y1, int dx, int dy, int numberOfBalls) {
		List<ImageView> chosenLine = new ArrayList<>();
		for (int i = 1; i < numberOfBalls; i++) {
			int x = x1 + dx * i;
			int y = y1 + dy * i;
			chosenLine.add(setChosen(x, y));
		}
		return chosenLine;
	}

	/*
	 * private ImageView addBall(int x, int y, Color color) { ImageView ball = new
	 * ImageView(getBallImage(color, false)); boardPane.add(ball, x, y); return
	 * ball; }
	 */

	public int[] getBallPos(ImageView ball) {
		for (int x = 0; x < 22; x++) {
			for (int y = 0; y < 11; y++) {
				if (board[x][y] != null && board[x][y].equals(ball)) {
					int[] pos = { x, y };
					return pos;
				}
			}
		}
		return null;
	}

	private ImageView addBall(int x, int y, ImageView ball) throws ArrayIndexOutOfBoundsException {
		if (board[x][y] != null && board[x][y].equals(BORDER))
			throw new ArrayIndexOutOfBoundsException();
		ball.setImage(getBallImage(ball, false));
		board[x][y] = ball;
		Platform.runLater(() -> {
			boardPane.add(ball, x, y);
		});
		return ball;
	}

	private ImageView deleteBall(int x, int y) {
		ImageView ball = board[x][y];
		Platform.runLater(() -> {
			boardPane.getChildren().remove(ball);
		});
		board[x][y] = null;
		return ball;
	}

	/*
	 * private ImageView moveBall(int x, int y, int dx, int dy) throws
	 * ArrayIndexOutOfBoundsException { ImageView ball = deleteBall(x, y); addBall(x
	 * + dx, y + dy, ball); return ball; }
	 */

	public ImageView addMoveButton(int x, int y, ImageMove move) {
		int dx = move.getDx();
		int dy = move.getDy();
		int rotation = move.getRotation();

		ImageView moveButton = new ImageView(moveButtonImg);
		moveButton.setRotate(rotation);

		moveButtons.add(moveButton);
		boardPane.add(moveButton, x + dx, y + dy);
		return moveButton;
	}

	public List<ImageView> moveLineOfBalls(int x1, int y1, int dx, int dy, int lineLength, ImageMove move) {

		List<ImageView> movedBalls = deleteLineOfBalls(x1, y1, dx, dy, lineLength);
		int newX = x1 + move.getDx();
		int newY = y1 + move.getDy();
		addLineOfBalls(newX, newY, dx, dy, lineLength, movedBalls);
		return movedBalls;
	}

	public List<ImageView> deleteLineOfBalls(int x1, int y1, int dx, int dy, int lineLength) {
		List<ImageView> deletedBalls = new ArrayList<>();

		for (int i = 0; i < lineLength; i++) {
			int x = x1 + dx * i;
			int y = y1 + dy * i;

			ImageView newBall = deleteBall(x, y);
			deletedBalls.add(newBall);
		}
		return deletedBalls;
	}

	public List<ImageView> addLineOfBalls(int x1, int y1, int dx, int dy, int lineLength, List<ImageView> ballsToAdd) {

		for (int i = 0; i < lineLength; i++) {
			ImageView ball = ballsToAdd.get(i);
			int x = x1 + dx * i;
			int y = y1 + dy * i;

			try {
				addBall(x, y, ball);
			} catch (ArrayIndexOutOfBoundsException e) {
				continue;
			}

		}
		return ballsToAdd;
	}

	public List<ImageView> addAllMoveButtons(int x, int y, List<ImageMove> possibleMovesList) {
		for (ImageMove move : possibleMovesList)
			addMoveButton(x, y, move);

		return moveButtons;
	}

	public void deleteAllMoveButtons() {
		boardPane.getChildren().removeAll(moveButtons);
		moveButtons.clear();
	}

	private void setBorder() {
		for (int x = 0; x < 21; x++) {
			board[x][0] = BORDER;
			board[x][10] = BORDER;
		}
		for (int y = 1; y < 6; y++) {
			for (int x = 0; x < 7 - y; x++)
				board[x][y] = BORDER;
			for (int x = 15 + y; x < 22; x++)
				board[x][y] = BORDER;
		}
		for (int y = 6; y < 10; y++) {
			for (int x = 0; x < y - 3; x++)
				board[x][y] = BORDER;
			for (int x = 25 - y; x < 22; x++)
				board[x][y] = BORDER;
		}
	}

	public List<ImageView> setBoard(Color myColor) {

		List<ImageView> blackBallList = new ArrayList<>();
		List<ImageView> whiteBallList = new ArrayList<>();
		setBorder();

		for (int i = 6; i <= 14; i += 2) {
			ImageView ball = new ImageView(blackBallImg);
			blackBallList.add(ball);
			board[i][1] = ball;
			boardPane.add(ball, i, 1);
		}
		for (int i = 5; i <= 15; i += 2) {
			ImageView ball = new ImageView(blackBallImg);
			blackBallList.add(ball);
			board[i][2] = ball;
			boardPane.add(ball, i, 2);
		}
		for (int i = 8; i <= 12; i += 2) {
			ImageView ball = new ImageView(blackBallImg);
			blackBallList.add(ball);
			board[i][3] = ball;
			boardPane.add(ball, i, 3);
		}

		// adding opponent's balls
		for (int i = 6; i <= 14; i += 2) {
			ImageView ball = new ImageView(whiteBallImg);
			whiteBallList.add(ball);
			board[i][9] = ball;
			boardPane.add(ball, i, 9);
		}
		for (int i = 5; i <= 15; i += 2) {
			ImageView ball = new ImageView(whiteBallImg);
			whiteBallList.add(ball);
			board[i][8] = ball;
			boardPane.add(ball, i, 8);
		}
		for (int i = 8; i <= 12; i += 2) {
			ImageView ball = new ImageView(whiteBallImg);
			whiteBallList.add(ball);
			board[i][7] = ball;
			boardPane.add(ball, i, 7);
		}

		if (myColor == BLACK)
			return blackBallList;
		else
			return whiteBallList;
	}

}
