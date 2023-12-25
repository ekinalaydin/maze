import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.util.Duration;

import java.util.*;

public class MazeGame extends Application {

    public MazeGame() {
    }

    public static final int MAZE_SIZE = 10;
    public static final int NUM_OBSTACLES = 15;
    public static final int NUM_PENALTIES = 3;
    public static final int NUM_TREASURES = 5;

    public static final char EMPTY_CELL = '.';
    public static final char WALL = '#';
    public static final char PLAYER = 'P';
    public static final char TREASURE = 'T';
    public static final char AI_AGENT = 'A';
    public static final char PENALTY = 'X';

    public static Label penaltyLabel;
    public Timeline penaltyTimeline;


    public static Random random = new Random();

    public static char[][] maze;


    public static AIPlayer aiPlayer=new AIPlayer();
    public static HumanPlayer humanPlayer=new HumanPlayer();

    public static void main(String[] args) {
        launch(StartMenu.class);
    }



    @Override
    public void start(Stage primaryStage) {

        Image icon = new Image("file:coin2.png");
        primaryStage.getIcons().add(icon);

        maze = generateMaze(MAZE_SIZE, MAZE_SIZE, NUM_OBSTACLES, NUM_PENALTIES);
        initializePlayers();
        placeTreasures(NUM_TREASURES);

        GridPane mazeGrid = createMazeGrid();
        mazeGrid.setFocusTraversable(true);
        updateMazeGrid(mazeGrid);

        List<Image> icons = primaryStage.getIcons();

        Label headerLabel = new Label("Treasure Hunt");
        headerLabel.setStyle("-fx-font-size: 30; -fx-font-family: 'Bookman Old Style'; -fx-font-weight: bold;");


        Button restartButton = createRestartButton(primaryStage);

        Button backToStartButton = new Button("Go Back to Start Menu");
        backToStartButton.setStyle("-fx-focus-traversable: false;");

        backToStartButton.setOnAction(e -> {
            primaryStage.close();
            StartMenu startMenu = new StartMenu();
            startMenu.start(new Stage());
        });


        Button exitButton = new Button("Exit");
        exitButton.setStyle("-fx-focus-traversable: false;");

        exitButton.setOnAction(e -> primaryStage.close());


        HBox buttonsHBox = new HBox(5);
        buttonsHBox.setAlignment(Pos.CENTER);
        buttonsHBox.getChildren().addAll(restartButton, backToStartButton, exitButton);


        VBox vBox = new VBox(10);
        vBox.getChildren().addAll(headerLabel, buttonsHBox, mazeGrid);
        vBox.setAlignment(Pos.CENTER);
        vBox.setBackground(Background.fill(Color.LIGHTGREEN));

        VBox overlay = new VBox();
        overlay.setAlignment(Pos.CENTER);

        overlay.setMouseTransparent(true);


        penaltyLabel = new Label();

        overlay.getChildren().add(penaltyLabel);

        StackPane overlayPane = new StackPane();
        overlayPane.getChildren().addAll(vBox, overlay);

        Scene scene = new Scene(overlayPane, 610, 700);


        penaltyTimeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> {
            penaltyLabel.setText("");
            penaltyLabel.setVisible(false);
        }));
        penaltyTimeline.setCycleCount(1);


        primaryStage.setResizable(false);

        mazeGrid.setFocusTraversable(true);

        scene.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            String direction = switch (code) {
                case UP -> "up";
                case DOWN -> "down";
                case LEFT -> "left";
                case RIGHT -> "right";
                default -> null;
            };

            if (direction != null) {
                humanPlayer.movePlayer(direction);
                aiPlayer.moveAiAgent();
                updateMazeGrid(mazeGrid);

                if (allTreasuresFound()) {
                    Stage currentStage = (Stage) primaryStage.getScene().getWindow();
                    currentStage.close();

                    printGameResult(primaryStage);
                }
            }
        });

        primaryStage.setTitle("Maze Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public static void displayPenaltyMessage(String message) {
        penaltyLabel.setText(message);
        penaltyLabel.setVisible(true);


        penaltyLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        penaltyLabel.setTextFill(Color.RED);


        penaltyLabel.setOpacity(1.0);


        FadeTransition fadeOut = new FadeTransition(Duration.seconds(2), penaltyLabel);
        fadeOut.setToValue(0);

        fadeOut.play();
    }
    public Button createRestartButton(Stage primaryStage) {
        Button restartButton = new Button("Restart Game");
        restartButton.setStyle("-fx-focus-traversable: false;");
        restartButton.setOnAction(e -> {
            aiPlayer.aiScore = 0;
            humanPlayer.playerScore = 0;
            primaryStage.close();
            start(new Stage());
        });

        return restartButton;
    }



    public void printGameResult(Stage primaryStage) {
        System.out.println("Game Over!");

        System.out.println("Player Score: " + humanPlayer.playerScore);
        System.out.println("AI Score: " + aiPlayer.aiScore);

        if (humanPlayer.playerScore > aiPlayer.aiScore) {
            System.out.println("You won!");
        } else if (aiPlayer.aiScore > humanPlayer.playerScore) {
            System.out.println("AI won!");
        } else {
            System.out.println("It's a tie!");
        }

        Stage scoreChartStage = new Stage();
        scoreChartStage.setTitle("Score Chart");


        Button restartButton = new Button("Restart Game");
        restartButton.setStyle("-fx-focus-traversable: false; -fx-font-family: 'Bookman Old Style'; -fx-font-size: 14;");
        restartButton.setOnAction(e -> {
            aiPlayer.aiScore = 0;
            humanPlayer.playerScore = 0;
            scoreChartStage.close();
            primaryStage.close();
            start(new Stage());
        });


        Button exitButton = new Button("Exit Game");
        exitButton.setStyle("-fx-focus-traversable: false; -fx-font-family: 'Bookman Old Style'; -fx-font-size: 14;");
        exitButton.setOnAction(e -> {
            primaryStage.close();
            scoreChartStage.close();
            Platform.exit();
        });


        Button backToStartButton = new Button("Go Back to Start Menu");
        backToStartButton.setStyle("-fx-focus-traversable: false;");
        backToStartButton.setOnAction(e -> {
            primaryStage.close();
            scoreChartStage.close();
            StartMenu startMenu = new StartMenu();
            startMenu.start(new Stage());
        });



        Image scoreTableBackground = new Image("file:assets/pngwing.com (12).png");
        BackgroundImage background = new BackgroundImage(scoreTableBackground, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);


        Label headerLabel = new Label("Score Chart");
        headerLabel.setStyle("-fx-font-size: 60; -fx-font-weight: bold; -fx-font-family: 'Bookman Old Style'");


        Label playerScoreLabel = new Label("Player Score: " + humanPlayer.playerScore);
        playerScoreLabel.setStyle("-fx-font-size: 16;");


        Label aiScoreLabel = new Label("AI Score: " + aiPlayer.aiScore);
        aiScoreLabel.setStyle("-fx-font-size: 16; -fx-font-family: 'Bookman Old Style'");


        Label winnerLabel = new Label();
        winnerLabel.setStyle("-fx-font-size: 30; -fx-font-weight: bold; -fx-font-family: 'Bookman Old Style'");


        if (humanPlayer.playerScore > aiPlayer.aiScore) {
            winnerLabel.setText("You won!");
        } else if (aiPlayer.aiScore > humanPlayer.playerScore) {
            winnerLabel.setText("AI won!");
        } else {
            winnerLabel.setText("It's a tie!");
        }


        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setBackground(new Background(background));
        layout.getChildren().addAll(headerLabel, playerScoreLabel, aiScoreLabel, winnerLabel, restartButton, exitButton,backToStartButton);

        Scene scoreChartScene = new Scene(layout, 700, 550);
        scoreChartStage.setResizable(false);

        scoreChartStage.setScene(scoreChartScene);


        scoreChartStage.show();
    }


    public GridPane createMazeGrid() {
        GridPane mazeGrid = new GridPane();
        mazeGrid.setHgap(1);
        mazeGrid.setVgap(1);

        int cellSize = 60;

        for (int i = 0; i < MAZE_SIZE; i++) {
            for (int j = 0; j < MAZE_SIZE; j++) {
                Rectangle cell = new Rectangle(cellSize, cellSize);
                cell.setStroke(Color.BLACK);

                mazeGrid.add(cell, j, i);
            }
        }

        return mazeGrid;
    }


    public static void updateMazeGrid(GridPane mazeGrid) {
        mazeGrid.getChildren().clear();

        for (int i = 0; i < MAZE_SIZE; i++) {
            for (int j = 0; j < MAZE_SIZE; j++) {
                char cellType = maze[i][j];

                Node cellNode;

                switch (cellType) {
                    case WALL:
                        cellNode = createColoredRectangle(Color.BLACK);
                        break;
                    case PLAYER:
                        cellNode = createImageView("file:assets/player3.png");
                        break;
                    case AI_AGENT:
                        cellNode = createImageView("file:assets/ai3.png");
                        break;
                    case TREASURE:
                        cellNode = createImageView("file:assets/treasure3.png");
                        break;
                    case PENALTY:
                        cellNode = createImageView("file:assets/blank.png");
                        break;
                    default:
                        cellNode = createImageView("file:assets/blank.png");
                        break;
                }

                if (maze[i][j] == PLAYER && maze[i][j] == AI_AGENT) {
                    ImageView playerImageView = createImageView("file:assets/player3.png");
                    ImageView aiAgentImageView = createImageView("file:assets/ai3.png");

                    StackPane stackPane = new StackPane();
                    stackPane.getChildren().addAll(playerImageView, aiAgentImageView);

                    cellNode = stackPane;
                }

                mazeGrid.add(cellNode, j, i);
            }
        }
    }

    public static Rectangle createColoredRectangle(Color color) {
        Rectangle rectangle = new Rectangle(60, 60, color);
        rectangle.setStroke(Color.BLACK);
        return rectangle;
    }

    public static ImageView createImageView(String imagePath) {
        ImageView imageView = new ImageView(new Image(imagePath));
        imageView.setFitWidth(60);
        imageView.setFitHeight(60);
        return imageView;
    }



    public char[][] generateMaze(int rows, int cols, int numObstacles, int numPenalties) {
        char[][] newMaze = new char[rows][cols];
        Random random = new Random();


        for (int i = 0; i < rows; i++) {
            Arrays.fill(newMaze[i], EMPTY_CELL);
        }

        for (int i = 0; i < rows; i++) {
            newMaze[i][0] = WALL;
            newMaze[i][cols - 1] = WALL;
        }
        for (int j = 0; j < cols; j++) {
            newMaze[0][j] = WALL;
            newMaze[rows - 1][j] = WALL;
        }
        for (int i = 0; i < numPenalties; i++) {
            int penaltyRow, penaltyCol;
            do {
                penaltyRow = random.nextInt(rows - 2) + 1;
                penaltyCol = random.nextInt(cols - 2) + 1;
            } while (newMaze[penaltyRow][penaltyCol] != EMPTY_CELL);

            newMaze[penaltyRow][penaltyCol] = PENALTY;
        }


        for (int i = 0; i < numObstacles; i++) {
            int obstacleRow, obstacleCol;
            do {
                obstacleRow = random.nextInt(rows - 2) + 1;
                obstacleCol = random.nextInt(cols - 2) + 1;
            } while (newMaze[obstacleRow][obstacleCol] != EMPTY_CELL);

            newMaze[obstacleRow][obstacleCol] = WALL;
        }

        return newMaze;
    }


    public void initializePlayers() {


        humanPlayer.playerRow = random.nextInt(maze.length - 2) + 1;
        humanPlayer.playerCol = random.nextInt(maze[0].length - 2) + 1;

        do {
            aiPlayer.aiAgentRow = random.nextInt(maze.length - 2) + 1;
            aiPlayer.aiAgentCol = random.nextInt(maze[0].length - 2) + 1;
        } while (aiPlayer.aiAgentRow == humanPlayer.playerRow && aiPlayer.aiAgentCol == MazeGame.humanPlayer.playerCol);

        maze[humanPlayer.playerRow][humanPlayer.playerCol] = PLAYER;
        maze[aiPlayer.aiAgentRow][aiPlayer.aiAgentCol] = AI_AGENT;
    }


    public void placeTreasures(int numTreasures) {
        int emptyCellCount = countEmptyCells();

        if (emptyCellCount == 0) {
            System.out.println("No empty cells to place treasures.");
            return;
        }

        if (emptyCellCount < numTreasures) {
            System.out.println("Not enough empty cells to place the specified number of treasures.");
            numTreasures = emptyCellCount;
        }

        for (int i = 0; i < numTreasures; i++) {
            int row, col;
            do {
                row = random.nextInt(maze.length - 2) + 1;
                col = random.nextInt(maze[0].length - 2) + 1;
            } while (maze[row][col] != EMPTY_CELL);

            maze[row][col] = TREASURE;
            emptyCellCount--;
        }
    }

    public int countEmptyCells() {
        int count = 0;
        for (int i = 1; i < maze.length - 1; i++) {
            for (int j = 1; j < maze[0].length - 1; j++) {
                if (maze[i][j] == EMPTY_CELL) {
                    count++;
                }
            }
        }
        return count;
    }



    public static boolean isValidMove(int row, int col) {
        return row >= 0 && row < maze.length && col >= 0 && col < maze[0].length && maze[row][col] != WALL;
    }




    public static void updatePenaltyLocation(int penaltyRow, int penaltyCol) {
        int newRow, newCol;
        do {
            newRow = random.nextInt(maze.length - 2) + 1;
            newCol = random.nextInt(maze[0].length - 2) + 1;
        } while (maze[newRow][newCol] != EMPTY_CELL);

        maze[penaltyRow][penaltyCol] = EMPTY_CELL;
        maze[newRow][newCol] = PENALTY;
    }



    public static Cell findNearestTreasure(int row, int col) {
        List<Cell> treasures = new ArrayList<>();

        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[0].length; j++) {
                if (maze[i][j] == TREASURE) {
                    treasures.add(new Cell(i, j));
                }
            }
        }

        if (treasures.isEmpty()) {
            return null;
        }

        treasures.sort(Comparator.comparingInt(cell -> Math.abs(row - cell.row) + Math.abs(col - cell.col)));
        return treasures.get(0);
    }

    static class Cell {
        int row;
        int col;
        int fScore;

        Cell(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Cell cell = (Cell) obj;
            return row == cell.row && col == cell.col;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, col);
        }
    }


    public boolean allTreasuresFound() {
        for (char[] chars : maze) {
            for (int j = 0; j < maze[0].length; j++) {
                if (chars[j] == TREASURE) {
                    return false;
                }
            }
        }
        return true;
    }


}