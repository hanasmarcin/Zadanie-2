package view;
import javafx.application.Application;
import static model.BoardModel.MY_COLOR;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main  extends Application {

	
	public static void main(String[] args) {
		
			launch(args);
	}

	
	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("window.fxml"));
			Scene scene = new Scene(root, 600, 600);
			primaryStage.setScene(scene);
			primaryStage.setTitle("Gracz "+MY_COLOR);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
