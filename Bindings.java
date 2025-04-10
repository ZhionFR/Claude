import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class Bindings extends Application {

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        
        Pane circlePane = new Pane();
        circlePane.setMinSize(300, 300);
        circlePane.setMaxSize(300, 300);
        circlePane.setStyle("-fx-background-color: white; -fx-background-radius: 150; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");
        
        Circle blackCircle = new Circle();
        blackCircle.setCenterX(150);
        blackCircle.setCenterY(150);
        blackCircle.setRadius(100);
        blackCircle.setFill(Color.BLACK);
        
        Circle blueCircle = new Circle();
        blueCircle.setCenterX(150);
        blueCircle.setCenterY(150);
        blueCircle.setRadius(50);
        blueCircle.setFill(Color.DODGERBLUE);
        
        circlePane.getChildren().addAll(blackCircle, blueCircle);
        
        Slider radiusSlider = new Slider(0, 150, 50);
        radiusSlider.setShowTickMarks(true);
        radiusSlider.setShowTickLabels(true);
        radiusSlider.setMajorTickUnit(50);
        
        blueCircle.radiusProperty().bind(radiusSlider.valueProperty());
        
        Label radiusLabel = new Label();
        radiusLabel.textProperty().bind(
            Bindings.format("Rayon du cercle: %.0f px", radiusSlider.valueProperty())
        );
        
        VBox controls = new VBox(10);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(20, 0, 0, 0));
        controls.getChildren().addAll(radiusLabel, radiusSlider);
        
        root.setCenter(circlePane);
        root.setBottom(controls);
        
        Scene scene = new Scene(root, 400, 450);
        scene.getStylesheets().add("https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&display=swap");
        scene.setFill(Color.LIGHTGRAY);
        
        primaryStage.setTitle("Redimensionnement de Cercle");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}