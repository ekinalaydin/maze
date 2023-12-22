import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.InputStream;

public class StartMenu extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(javafx.stage.Stage primaryStage) {
        primaryStage.setTitle("Treasure Hunt");


        Image headerImage = new Image("file:assets/pngwing.com (37).png");
        ImageView headerImageView = new ImageView(headerImage);


        Button startButton = new Button("Start");
        startButton.setStyle("-fx-focus-traversable: false; -fx-pref-width: 100px; -fx-pref-height: 40px; -fx-font-size: 20; -fx-font-family: 'Bookman Old Style'");

        Button exitButton = new Button("Exit");
        exitButton.setStyle("-fx-focus-traversable: false; -fx-pref-width: 100px; -fx-pref-height: 40px; -fx-font-size: 20; -fx-font-family: 'Bookman Old Style'");


        startButton.setOnAction(event -> {
            MazeGame mazeGame = new MazeGame();
            mazeGame.start(new javafx.stage.Stage());
            primaryStage.close();
        });

        exitButton.setOnAction(event -> Platform.exit());

        Image startTableBackground = new Image("file:assets/pngwing.com (15).png");
        BackgroundImage background = new BackgroundImage(startTableBackground, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);


        VBox root = new VBox(20);
        root.setBackground(new Background(new BackgroundFill(Color.CHOCOLATE, CornerRadii.EMPTY, Insets.EMPTY)));
        root.setAlignment(Pos.CENTER);
        root.setBackground(new Background(background));

        InputStream stream = getClass().getResourceAsStream("/assets/Treasuremap-Ea1vj.ttf");
        String fontName = "Skulls and Crossbones";
        Label titleLabel = new Label();

        if (stream == null) {
            System.err.println("Error loading font file");
        } else {
            try {
                Font loadedFont = Font.loadFont(stream, 50);

                if (loadedFont == null) {
                    System.err.println("Error loading font");
                } else {

                    String fontPath = getClass().getResource("/assets/Treasuremap-Ea1vj.ttf").toExternalForm();  // Note the leading '/' for an absolute path
                    System.out.println("Font loaded successfully from: " + fontPath);


                    titleLabel = new Label("Treasure\n Hunt");
                    String postScriptName = loadedFont.getName();
                    titleLabel.setStyle("-fx-font-size: 40; -fx-font-weight: 800; -fx-text-fill: black; -fx-font-family: '" + postScriptName + "';");


                    System.out.println("Font name: " + loadedFont.getName());
                    System.out.println("Font family: " + loadedFont.getFamily());
                }
            } catch (Exception e) {
                System.err.println("Error loading font: " + e.getMessage());
            }
        }


        HBox titleHeaderBox = new HBox(10);
        titleHeaderBox.setAlignment(Pos.CENTER);
        titleHeaderBox.getChildren().addAll(titleLabel, headerImageView);


        root.getChildren().addAll(titleHeaderBox, startButton, exitButton);

        // Create scene
        Scene scene = new Scene(root, 600, 600);
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
