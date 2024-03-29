package controller;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import model.BoardModel;
import model.MessagingModel;
import model.Step;
import view.BoardView;
import view.ImageMove;

import static model.State.*;

import java.util.List;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import static model.BoardModel.MY_COLOR;

public class Controller {
	@FXML
	private GridPane board;
	@FXML
	private Text myCounterText;
	@FXML
	private Text opponentCounterText;

	BoardView boardView;
	BoardModel boardModel;
	MessagingModel msgModel;

	@FXML
	public void initialize() {
		boardModel = new BoardModel();
		boardView = new BoardView(board);
		msgModel = new MessagingModel(boardModel);
		for (ImageView ball : boardView.setBoard(MY_COLOR))
			ball.setOnMouseClicked(e -> ballClicked(ball));
		setConsumerListener();
		Alerts.makeGameStartAlert(MY_COLOR);
	}

	private void ballClicked(ImageView ball) {
		if (boardModel.getState() == GAME_ENDED)
			return;
		if (ballPos == null)
			return;
		int[] ballPos = boardView.getBallPos(ball);
		int x = ballPos[0];
		int y = ballPos[1];

		if (boardModel.getState() == CHOOSE_FIRST_BALL) {
			boardView.setIsChosen(x, y, true);
			boardModel.chooseFirstBall(x, y);
		} else if (boardModel.getState() == CHOOSE_SECOND_BALL) {
			Step lineStep = boardModel.chooseBallLine(x, y);
			if (lineStep != null) {

				int myLineLength = boardModel.getLineLength();
				int[] firstBallPos = boardModel.getBallPos(0);
				int[] lastBallPos = boardModel.getBallPos(myLineLength - 1);

				boardView.setChosenLine(firstBallPos[0], firstBallPos[1], lineStep.dx(), lineStep.dy(), myLineLength);

				List<Step> listOfMoves = boardModel.createListOfMoves();
				if (listOfMoves.size() == 0) {
					boardModel.endTurn();
					boardView.setNotChosenLine(firstBallPos[0], firstBallPos[1], lineStep.dx(), lineStep.dy(), myLineLength);
					boardModel.startTurn();
					Alerts.makeTurnAlert(MY_COLOR);
				}
				for (Step move : listOfMoves) {
						ImageView moveButton = boardView.addMoveButton(lastBallPos[0], lastBallPos[1],
								new ImageMove(move.dx(), move.dy()));
						moveButton.setOnMouseClicked(e -> moveButtonClicked(move, lineStep));
					}
			}
		}

	}

	private void moveButtonClicked(Step move, Step lineStep) {

		moveBalls(move, lineStep);

		sendBoard(move);
		boardModel.moveBallLine(move);
		boardView.deleteAllMoveButtons();

		String msg = boardModel.endTurn();
		if (msg != null) {
			Alerts.makeGameEndAlert(msg, MY_COLOR);
			boardModel.endGame();
		}
		setPoints();
	}

	private void moveBalls(Step move, Step lineStep) {
		int[] firstBallPos = boardModel.getBallPos(0);
		if (firstBallPos == null) {
			return;
		}
		int x1 = firstBallPos[0];
		int y1 = firstBallPos[1];
		int lineLength = boardModel.getLineLength();

		boardView.moveLineOfBalls(x1, y1, lineStep.dx(), lineStep.dy(), lineLength,
				new ImageMove(move.dx(), move.dy()));
	}

	private void sendBoard(Step move) {
		String message = msgModel.makeMsgString(move);
		MessagingModel.sendQueueMessage(message);
	}

	private void receiveBoard(String message) {
		Step move = msgModel.readFromMsgString(message);
		Step lineStep = boardModel.getLineStep();
		moveBalls(move, lineStep);
		boardModel.moveBallLine(move);

		String msg = boardModel.endTurn();
		if (msg != null) {
			Alerts.makeGameEndAlert(msg, MY_COLOR);
			boardModel.endGame();
		}
		setPoints();

	}

	private void setConsumerListener() {
		MessagingModel.getJmsConsumer().setMessageListener(message -> {
			try {
				String msg = ((TextMessage) message).getText();
				receiveBoard(msg);
				boardModel.startTurn();
				Alerts.makeTurnAlert(MY_COLOR);
			} catch (JMSException e) {
				e.printStackTrace();
			}
		});
	}

	private void setPoints() {
		myCounterText.setText(Integer.toString(boardModel.getMyCounter()));
		opponentCounterText.setText(Integer.toString(boardModel.getOpponentCounter()));

	}

}
