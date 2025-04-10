import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class code_reading_file_content extends Application {

    private TextArea contentTextArea;
    private Label statusLabel;
    private Label lineCountLabel;
    private ProgressBar progressBar;
    private Button selectFileButton;
    private Button cancelButton;
    
    private Task<List<String>> fileReadingTask;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        contentTextArea = new TextArea();
        contentTextArea.setEditable(false);
        contentTextArea.setWrapText(true);
        contentTextArea.setPrefRowCount(25);
        
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        
        statusLabel = new Label("Sélectionnez un fichier pour commencer");
        lineCountLabel = new Label("Nombre de lignes : 0");
        
        selectFileButton = new Button("Sélectionner un fichier");
        selectFileButton.setOnAction(e -> selectAndReadFile(primaryStage));
        
        cancelButton = new Button("Annuler");
        cancelButton.setDisable(true);
        cancelButton.setOnAction(e -> {
            if (fileReadingTask != null) {
                fileReadingTask.cancel();
            }
        });
        
        HBox buttonBox = new HBox(10, selectFileButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);
        
        VBox bottomBox = new VBox(10, lineCountLabel, progressBar, statusLabel, buttonBox);
        bottomBox.setPadding(new Insets(10, 0, 0, 0));
        bottomBox.setAlignment(Pos.CENTER);
        
        root.setCenter(contentTextArea);
        root.setBottom(bottomBox);
        
        Scene scene = new Scene(root, 700, 500);
        primaryStage.setTitle("Lecteur de Fichier Texte");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        if (getParameters().getRaw().size() > 0) {
            String filename = getParameters().getRaw().get(0);
            readFile(new File(filename));
        }
    }
    
    private void selectAndReadFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionnez un fichier texte");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers texte", "*.txt")
        );
        fileChooser.setInitialDirectory(new File("file_contents"));
        
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            readFile(selectedFile);
        }
    }
    
    private void readFile(File file) {
        contentTextArea.clear();
        lineCountLabel.setText("Nombre de lignes : 0");
        statusLabel.setText("Lecture du fichier...");
        selectFileButton.setDisable(true);
        cancelButton.setDisable(false);
        
        fileReadingTask = new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                List<String> lines = new ArrayList<>();
                long lineCount = 0;
                long totalLines = countLines(file);
                updateProgress(0, totalLines);
                
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null && !isCancelled()) {
                        lines.add(line);
                        lineCount++;
                        updateProgress(lineCount, totalLines);
                        updateMessage("Lecture en cours... Ligne " + lineCount + " sur " + totalLines);
                        Thread.sleep(1);
                    }
                }
                return lines;
            }
            
            private long countLines(File file) throws IOException {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    long count = 0;
                    while (reader.readLine() != null) {
                        count++;
                    }
                    return count;
                }
            }
        };
        
        progressBar.progressProperty().bind(fileReadingTask.progressProperty());
        statusLabel.textProperty().bind(fileReadingTask.messageProperty());
        
        fileReadingTask.setOnSucceeded(event -> {
            List<String> lines = fileReadingTask.getValue();
            
            for (String line : lines) {
                contentTextArea.appendText(line + "\n");
            }
            
            lineCountLabel.setText("Nombre de lignes : " + lines.size());
            statusLabel.textProperty().unbind();
            statusLabel.setText("Lecture terminée");
            selectFileButton.setDisable(false);
            cancelButton.setDisable(true);
        });
        
        fileReadingTask.setOnCancelled(event -> {
            statusLabel.textProperty().unbind();
            statusLabel.setText("Lecture annulée");
            selectFileButton.setDisable(false);
            cancelButton.setDisable(true);
        });
        
        fileReadingTask.setOnFailed(event -> {
            statusLabel.textProperty().unbind();
            statusLabel.setText("Erreur: " + fileReadingTask.getException().getMessage());
            selectFileButton.setDisable(false);
            cancelButton.setDisable(true);
        });
        
        Thread thread = new Thread(fileReadingTask);
        thread.setDaemon(true);
        thread.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}