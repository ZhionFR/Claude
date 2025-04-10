package org.codes_maze.view;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer;
import javafx.beans.binding.Bindings;

import org.codes_maze.controller.MazeController;
import org.codes_maze.model.*;

public class MazeView extends Application {
    private static final int CELL_SIZE = 40;
    private static final long TORCH_DURATION = 3000;
    
    private MazeController controller;
    
    private Canvas mazeCanvas;
    private GraphicsContext gc;
    private Label statusLabel;
    private Label timerLabel;
    private Label torchesLabel;
    private Slider rowsSlider;
    private Slider colsSlider;
    private Slider wallRatioSlider;
    private Slider torchRatioSlider;
    
    private boolean torchActive = false;
    private long torchStartTime = 0;
    private AnimationTimer gameTimer;
    private long gameStartTime;
    
    @Override
    public void start(Stage primaryStage) {
        controller = new MazeController();
        
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        
        VBox configPanel = createConfigPanel();
        root.setLeft(configPanel);
        
        statusLabel = new Label("Utilisez les flèches pour vous déplacer et T pour utiliser une torche");
        statusLabel.setFont(Font.font(14));
        torchesLabel = new Label("Torches: 1");
        timerLabel = new Label("Temps: 0s");
        
        HBox infoPanel = new HBox(20, statusLabel, torchesLabel, timerLabel);
        infoPanel.setPadding(new Insets(10));
        infoPanel.setAlignment(Pos.CENTER);
        root.setTop(infoPanel);
        
        mazeCanvas = new Canvas();
        gc = mazeCanvas.getGraphicsContext2D();
        root.setCenter(mazeCanvas);
        
        HBox controlPanel = createControlPanel();
        root.setBottom(controlPanel);
        
        Scene scene = new Scene(root, 800, 600);
        scene.setOnKeyPressed(e -> handleKeyPress(e.getCode()));
        
        primaryStage.setTitle("Maze Game");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        startNewGame();
    }
    
    private VBox createConfigPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setAlignment(Pos.TOP_CENTER);
        
        Label titleLabel = new Label("Configuration");
        titleLabel.setFont(Font.font(16));
        
        Label rowsLabel = new Label("Lignes: 10");
        rowsSlider = new Slider(5, 20, 10);
        rowsSlider.setShowTickMarks(true);
        rowsSlider.setShowTickLabels(true);
        rowsSlider.setMajorTickUnit(5);
        rowsSlider.setMinorTickCount(4);
        rowsSlider.setSnapToTicks(true);
        rowsSlider.valueProperty().addListener((obs, oldVal, newVal) -> 
            rowsLabel.setText("Lignes: " + newVal.intValue()));
        
        Label colsLabel = new Label("Colonnes: 10");
        colsSlider = new Slider(5, 20, 10);
        colsSlider.setShowTickMarks(true);
        colsSlider.setShowTickLabels(true);
        colsSlider.setMajorTickUnit(5);
        colsSlider.setMinorTickCount(4);
        colsSlider.setSnapToTicks(true);
        colsSlider.valueProperty().addListener((obs, oldVal, newVal) -> 
            colsLabel.setText("Colonnes: " + newVal.intValue()));
        
        Label wallRatioLabel = new Label("Murs: 30%");
        wallRatioSlider = new Slider(0, 60, 30);
        wallRatioSlider.setShowTickMarks(true);
        wallRatioSlider.setShowTickLabels(true);
        wallRatioSlider.setMajorTickUnit(20);
        wallRatioSlider.setMinorTickCount(3);
        wallRatioSlider.valueProperty().addListener((obs, oldVal, newVal) -> 
            wallRatioLabel.setText("Murs: " + newVal.intValue() + "%"));
        
        Label torchRatioLabel = new Label("Torches: 10%");
        torchRatioSlider = new Slider(0, 30, 10);
        torchRatioSlider.setShowTickMarks(true);
        torchRatioSlider.setShowTickLabels(true);
        torchRatioSlider.setMajorTickUnit(10);
        torchRatioSlider.setMinorTickCount(1);
        torchRatioSlider.valueProperty().addListener((obs, oldVal, newVal) -> 
            torchRatioLabel.setText("Torches: " + newVal.intValue() + "%"));
        
        panel.getChildren().addAll(
            titleLabel,
            rowsLabel, rowsSlider,
            colsLabel, colsSlider,
            wallRatioLabel, wallRatioSlider,
            torchRatioLabel, torchRatioSlider
        );
        
        return panel;
    }
    
    private HBox createControlPanel() {
        HBox panel = new HBox(10);
        panel.setPadding(new Insets(10));
        panel.setAlignment(Pos.CENTER);
        
        Button newGameButton = new Button("Nouvelle partie");
        newGameButton.setOnAction(e -> startNewGame());
        
        Button quitButton = new Button("Quitter");
        quitButton.setOnAction(e -> {
            if (gameTimer != null) {
                gameTimer.stop();
            }
            System.exit(0);
        });
        
        panel.getChildren().addAll(newGameButton, quitButton);
        return panel;
    }
    
    private void handleKeyPress(KeyCode code) {
        if (controller.getGameState() != GameState.RUNNING) {
            return;
        }
        
        boolean moved = false;
        
        switch (code) {
            case UP:
                moved = controller.movePlayer(Direction.UP);
                break;
            case DOWN:
                moved = controller.movePlayer(Direction.DOWN);
                break;
            case LEFT:
                moved = controller.movePlayer(Direction.LEFT);
                break;
            case RIGHT:
                moved = controller.movePlayer(Direction.RIGHT);
                break;
            case T:
                if (controller.getAvailableTorches() > 0) {
                    controller.useTorch();
                    torchActive = true;
                    torchStartTime = System.currentTimeMillis();
                    statusLabel.setText("Torche allumée !");
                } else {
                    statusLabel.setText("Pas de torche disponible !");
                }
                break;
            case Q:
                if (gameTimer != null) {
                    gameTimer.stop();
                    statusLabel.setText("Partie terminée. Appuyez sur 'Nouvelle partie' pour recommencer.");
                }
                break;
            default:
                break;
        }
        
        if (moved) {
            updateLabels();
            checkGameState();
        }
        
        drawMaze();
    }
    
    private void startNewGame() {
        int rows = (int) rowsSlider.getValue();
        int cols = (int) colsSlider.getValue();
        double wallRatio = wallRatioSlider.getValue() / 100.0;
        double torchRatio = torchRatioSlider.getValue() / 100.0;
        
        controller.initializeGame(rows, cols, wallRatio, torchRatio);
        
        mazeCanvas.setWidth(cols * CELL_SIZE);
        mazeCanvas.setHeight(rows * CELL_SIZE);
        
        torchActive = false;
        gameStartTime = System.currentTimeMillis();
        
        statusLabel.setText("Nouvelle partie ! Utilisez les flèches pour vous déplacer et T pour utiliser une torche");
        updateLabels();
        
        if (gameTimer != null) {
            gameTimer.stop();
        }
        
        gameTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateGame();
            }
        };
        gameTimer.start();
        
        drawMaze();
    }
    
    private void updateGame() {
        long elapsedTime = (System.currentTimeMillis() - gameStartTime) / 1000;
        timerLabel.setText("Temps: " + elapsedTime + "s");
        
        if (torchActive && (System.currentTimeMillis() - torchStartTime > TORCH_DURATION)) {
            torchActive = false;
            statusLabel.setText("La torche s'est éteinte.");
            drawMaze();
        }
    }
    
    private void checkGameState() {
        if (controller.getGameState() == GameState.WON) {
            gameTimer.stop();
            long gameTime = (System.currentTimeMillis() - gameStartTime) / 1000;
            statusLabel.setText("Félicitations ! Vous avez gagné en " + gameTime + " secondes !");
        }
    }
    
    private void updateLabels() {
        torchesLabel.setText("Torches: " + controller.getAvailableTorches());
    }
    
    private void drawMaze() {
        Maze maze = controller.getMaze();
        int rows = maze.getRows();
        int cols = maze.getCols();
        
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, mazeCanvas.getWidth(), mazeCanvas.getHeight());
        
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Cell cell = maze.getCell(row, col);
                
                int x = col * CELL_SIZE;
                int y = row * CELL_SIZE;
                
                boolean isVisible = cellIsVisible(cell, row, col);
                
                if (isVisible) {
                    drawVisibleCell(cell, x, y);
                } else {
                    gc.setFill(Color.DARKGRAY);
                    gc.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                    gc.setFill(Color.WHITE);
                    gc.setFont(Font.font(CELL_SIZE * 0.6));
                    gc.fillText("~", x + CELL_SIZE/2 - CELL_SIZE*0.15, y + CELL_SIZE/2 + CELL_SIZE*0.2);
                }
                
                gc.setStroke(Color.GRAY);
                gc.strokeRect(x, y, CELL_SIZE, CELL_SIZE);
            }
        }
    }
    
    private boolean cellIsVisible(Cell cell, int row, int col) {
        if (cell.isRevealed()) {
            return true;
        }
        
        if (torchActive) {
            return true;
        }
        
        Position playerPos = controller.getPlayerPosition();
        int distance = Math.abs(row - playerPos.getRow()) + Math.abs(col - playerPos.getCol());
        return distance <= 1;
    }
    
    private void drawVisibleCell(Cell cell, int x, int y) {
        CellType type = cell.getType();
        
        switch (type) {
            case PLAYER:
                gc.setFill(Color.BLUE);
                gc.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font(CELL_SIZE * 0.7));
                gc.fillText("P", x + CELL_SIZE/2 - CELL_SIZE*0.2, y + CELL_SIZE/2 + CELL_SIZE*0.25);
                break;
                
            case EXIT:
                gc.setFill(Color.GREEN);
                gc.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                gc.setFill(Color.BLACK);
                gc.setFont(Font.font(CELL_SIZE * 0.7));
                gc.fillText("E", x + CELL_SIZE/2 - CELL_SIZE*0.2, y + CELL_SIZE/2 + CELL_SIZE*0.25);
                break;
                
            case WALL:
                gc.setFill(Color.BROWN);
                gc.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font(CELL_SIZE * 0.7));
                gc.fillText("W", x + CELL_SIZE/2 - CELL_SIZE*0.25, y + CELL_SIZE/2 + CELL_SIZE*0.25);
                break;
                
            case TORCH:
                gc.setFill(Color.ORANGE);
                gc.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                gc.setFill(Color.BLACK);
                gc.setFont(Font.font(CELL_SIZE * 0.7));
                gc.fillText("T", x + CELL_SIZE/2 - CELL_SIZE*0.2, y + CELL_SIZE/2 + CELL_SIZE*0.25);
                break;
                
            case EMPTY:
                gc.setFill(Color.LIGHTGRAY);
                gc.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                break;
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}