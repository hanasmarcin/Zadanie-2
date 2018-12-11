package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import model.Ball;
import model.Messaging;
import model.Move;
import model.Orientation;
import model.State;
import model.Ball.Color;
import model.BoardModel;
import model.ListOfBalls;
import view.Board;

import static model.Ball.Color.*;
import static model.Move.*;
import static model.Orientation.*;
import static model.State.*;
import static model.BoardModel.MY_COLOR;
import static model.BoardModel.tableOfMoves;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.jms.JMSException;
import javax.jms.TextMessage;

public class Controller {

	@FXML
	private GridPane board;
	@FXML
	private Text myCounterText;
	@FXML
	private Text opponentCounterText;

	private Board myBoard;
	private BoardModel myBoardModel = new BoardModel();

	/*
	 * public static final Color MY_COLOR = BLACK; // second player: WHITE public
	 * static final Move[] tableOfMoves = { E, SE, SW, W, NW, NE };
	 * 
	 * private int myCounter; private int opponentCounter; private Board myBoard;
	 * private State state;
	 * 
	 * private List<Ball> chosenBalls; private List<Ball> opponentToMoveBalls;
	 */


	/**
	 * Method initializing the controller. The representation of the board is being
	 * made. Message listener is being set.
	 */
	@FXML
	public void initialize() {
		myBoard = new Board(board);
		for (Ball tmpBall : myBoard.setBoard(MY_COLOR))
			tmpBall.setOnMouseClicked(e -> clickBall(tmpBall));

		setConsumerListener();
	}

	/**
	 * Method is called, when user clicks on a ball
	 * 
	 * @param ball - clicked Ball
	 */
	void clickBall(Ball ball) {

		if (myBoardModel.getState() == CHOOSE_FIRST_BALL) {

			ball.setImage(Ball.getChosenBallImg(MY_COLOR));
			myBoardModel.chooseFirstBall(ball);
		}

		else if (myBoardModel.getState() == CHOOSE_SECOND_BALL) {
			int x1 = myBoardModel.firstBallX();
			int x2 = ball.getPosX();
			int y1 = myBoardModel.firstBallY();
			int y2 = ball.getPosY();
			int dx = x2 - x1;
			int dy = y2 - y1;
			
			Orientation orientation = myBoardModel.chooseSecondBall(ball);
			if (orientation == null) orientation = chooseBalls(dx, dy, x1, y1, myBoard.getMyBallsList());
			final Orientation or = orientation;
			int k[] = myBoardModel.makeMovesTable(myBoard.getMyBallsList(), myBoard.getOpponentBallsList());
			int numberOfBalls = myBoardModel.numberOfChosenBalls();

			for (Move move : tableOfMoves) {
				if (k[move.id()] == numberOfBalls) {
					move.setPossible(true);
					ImageView tmpMoveButton = myBoard.setOneMoveButton(move, x2, y2);
					tmpMoveButton.setOnMouseClicked(e -> moveChoiceClicked(move, or));
				}

			}
			Move[] possibleMoves = orientation.possibleMoves();
			for (Move tmpMove : possibleMoves) {
				if (addMoveAlongLine(x2, y2, tmpMove))
					tableOfMoves[tmpMove.id()].setPossible(true);
			}
		}
	}

	/**
	 * Method responsible for choosing a line of balls
	 * 
	 * @param dx - difference between x positions of second and first chosen balls
	 * @param dy - difference between y positions of second and first chosen balls
	 * @param x1 - x position of the first ball in the line
	 * @param y1 - y position of the first ball in the line
	 * @return orientation of chosen line of balls, null if balls are not in one
	 *         line
	 */

	private Orientation chooseBalls(int dx, int dy, int x1, int y1, ListOfBalls myBallsList) {

		Orientation orientation = myBoardModel.chooseBalls(dx, dy, x1, y1, myBallsList);
		if (orientation != null) {

			myBoard.chooseBalls(myBoardModel.getChosenBalls());
		}
		return orientation;
	}

	/**
	 * Method moves balls on the board. Method calls Board class method to make a
	 * move.
	 * 
	 * @param move        - the direction of a move
	 * @param orientation - the orientation of a line of chosen balls
	 */
	void moveBalls(Move move, Orientation orientation) {
		ListOfBalls newBalls = myBoard.moveBalls(move, myBoardModel.getChosenBalls());

		for (Ball tmpBall : newBalls)
			tmpBall.setOnMouseClicked(e -> clickBall(tmpBall));

		myBoardModel.removeOpponentToMoveBallsIfNeeded(move, orientation);

		ListOfBalls newOpponentBalls = myBoard.moveBalls(move, myBoardModel.getOpponentToMoveBalls());
		myBoardModel.setPointCounters(newBalls, newOpponentBalls);

		opponentCounterText.setText(Integer.toString(myBoardModel.getOpponentCounter()));
		myCounterText.setText(Integer.toString(myBoardModel.getMyCounter()));

		if (myBoardModel.getAlertText() != null)
			makeAlert(myBoardModel.getAlertText());

	}

	/**
	 * Method sets a listener for a consumer, which receive a message from the
	 * server and then perform described action
	 */
	private void setConsumerListener() {
		Messaging.getJmsConsumer().setMessageListener(message -> {
			try {
				String msg = ((TextMessage) message).getText();
				Move move = Messaging.readFromMsgString(msg, myBoard.getMyBallsList(), myBoard.getOpponentBallsList());
				//ListOfBalls chosenBalls = myBoardModel.getChosenBalls();
				//ListOfBalls opponentToMoveBalls = myBoardModel.getOpponentToMoveBalls();

				//chosenBalls = Messaging.getChosenBalls();
				moveBalls(move, null);
				
				myBoardModel.removeToMoveBalls();
				myBoardModel.startGame();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * Method creates alert, shows it and set listener for "OK" button.
	 * 
	 * @param text - text shown in the alert
	 */
	private void makeAlert(String text) {
		Platform.runLater(() -> {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Koniec gry!");
			alert.setHeaderText(text);
			alert.setContentText("Zako�cz gr�");
			Optional<ButtonType> result = alert.showAndWait();
			result.ifPresent(res -> {
				myBoardModel.endGame();
			});
		});
	}

	/**
	 * Method is called, when user clicks on a button, which indicates the direction
	 * of a move
	 * 
	 * @param move        - chosen move direction
	 * @param orientation - orientation of a line of chosen balls
	 */
	private void moveChoiceClicked(Move move, Orientation orientation) {

		moveBalls(move, orientation); // moving balls on a board

		Messaging.sendQueueMessage(
				Messaging.makeMsgString(myBoardModel.getChosenBalls(), myBoardModel.getOpponentToMoveBalls(), move));
		myBoardModel.endMove();

	}

	/**
	 * Method adding a possibility of moving along the line of chosen balls. Method
	 * checks whether these moves are possible and creates new buttons for these
	 * moves.
	 * 
	 * @param x2   - x position of the last ball in the line
	 * @param y2   - y position of the last ball in the line
	 * @param move - direction of a chosen move
	 * @return whether these move is possible or not
	 */
	private boolean addMoveAlongLine(int x2, int y2, Move move) {
		int dx = move.dx();
		int dy = move.dy();
		Integer i = myBoardModel.addMoveAlongLine(x2, y2, move, myBoard.getMyBallsList(), myBoard.getOpponentBallsList());

		if (i != null) {
			ImageView tmpMoveButton = myBoard.setOneMoveButton(move, x2 + (i - 1) * dx, y2 + (i - 1) * dy);
			tmpMoveButton.setOnMouseClicked(e -> moveChoiceClicked(move, Orientation.getOrientation(dx, dy)));
			return true;
		} else
			return false;
	}

}
