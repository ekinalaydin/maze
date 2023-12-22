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

    private static final int MAZE_SIZE = 10;
    private static final int NUM_OBSTACLES = 15;
    private static final int NUM_PENALTIES = 3;
    private static final int NUM_TREASURES = 5;

    private static final char EMPTY_CELL = '.';
    private static final char WALL = '#';
    private static final char PLAYER = 'P';
    private static final char TREASURE = 'T';
    private static final char AI_AGENT = 'A';
    private static final char PENALTY = 'X';

    private Label penaltyLabel;
    private Timeline penaltyTimeline;


    private Random random = new Random();

    private char[][] maze;
    private int playerRow;
    private int playerCol;
    private int aiAgentRow;
    private int aiAgentCol;

    AIPlayer aiPlayer=new AIPlayer();
    HumanPlayer humanPlayer=new HumanPlayer();

    public static void main(String[] args) {
        launch(StartMenu.class);
    }

    private int playerScore = 0;
    private int aiScore = 0;

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
        System.out.println("Number of icons added: " + icons.size());

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
            String direction = null;

            switch (code) {
                case UP:
                    direction = "up";
                    break;
                case DOWN:
                    direction = "down";
                    break;
                case LEFT:
                    direction = "left";
                    break;
                case RIGHT:
                    direction = "right";
                    break;
            }

            if (direction != null) {
                movePlayer(direction);
                moveAiAgent();
                updateMazeGrid(mazeGrid);

                if (maze[playerRow][playerCol] == TREASURE) {
                    System.out.println("Congratulations! You found a treasure.");
                    playerScore++;
                }

                if (maze[aiAgentRow][aiAgentCol] == TREASURE) {
                    System.out.println("AI agent found a treasure.");
                    aiScore++;
                }

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
    private void displayPenaltyMessage(String message) {
        penaltyLabel.setText(message);
        penaltyLabel.setVisible(true);


        penaltyLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        penaltyLabel.setTextFill(Color.RED);


        penaltyLabel.setOpacity(1.0);


        FadeTransition fadeOut = new FadeTransition(Duration.seconds(2), penaltyLabel);
        fadeOut.setToValue(0);

        fadeOut.play();
    }
    private Button createRestartButton(Stage primaryStage) {
        Button restartButton = new Button("Restart Game");
        restartButton.setStyle("-fx-focus-traversable: false;");
        restartButton.setOnAction(e -> {
            aiScore = 0;
            playerScore = 0;
            primaryStage.close();
            start(new Stage());
        });

        return restartButton;
    }



    private void printGameResult(Stage primaryStage) {
        System.out.println("Game Over!");

        System.out.println("Player Score: " + playerScore);
        System.out.println("AI Score: " + aiScore);

        if (playerScore > aiScore) {
            System.out.println("You won!");
        } else if (aiScore > playerScore) {
            System.out.println("AI won!");
        } else {
            System.out.println("It's a tie!");
        }

        Stage scoreChartStage = new Stage();
        scoreChartStage.setTitle("Score Chart");


        Button restartButton = new Button("Restart Game");
        restartButton.setStyle("-fx-focus-traversable: false; -fx-font-family: 'Bookman Old Style'; -fx-font-size: 14;");
        restartButton.setOnAction(e -> {
            aiScore = 0;
            playerScore = 0;
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


        Label playerScoreLabel = new Label("Player Score: " + playerScore);
        playerScoreLabel.setStyle("-fx-font-size: 16;");


        Label aiScoreLabel = new Label("AI Score: " + aiScore);
        aiScoreLabel.setStyle("-fx-font-size: 16; -fx-font-family: 'Bookman Old Style'");


        Label winnerLabel = new Label();
        winnerLabel.setStyle("-fx-font-size: 30; -fx-font-weight: bold; -fx-font-family: 'Bookman Old Style'");


        if (playerScore > aiScore) {
            winnerLabel.setText("You won!");
        } else if (aiScore > playerScore) {
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


    private GridPane createMazeGrid() {
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


    private void updateMazeGrid(GridPane mazeGrid) {
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

                mazeGrid.add(cellNode, j, i);
            }
        }
    }

    private Rectangle createColoredRectangle(Color color) {
        Rectangle rectangle = new Rectangle(60, 60, color);
        rectangle.setStroke(Color.BLACK);
        return rectangle;
    }

    private ImageView createImageView(String imagePath) {
        ImageView imageView = new ImageView(new Image(imagePath));
        imageView.setFitWidth(60);
        imageView.setFitHeight(60);
        return imageView;
    }



    private char[][] generateMaze(int rows, int cols, int numObstacles, int numPenalties) {
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


    private void initializePlayers() {
        Random random = new Random();

        playerRow = random.nextInt(maze.length - 2) + 1;
        playerCol = random.nextInt(maze[0].length - 2) + 1;

        do {
            aiAgentRow = random.nextInt(maze.length - 2) + 1;
            aiAgentCol = random.nextInt(maze[0].length - 2) + 1;
        } while (aiAgentRow == playerRow && aiAgentCol == playerCol);

        maze[playerRow][playerCol] = PLAYER;
        maze[aiAgentRow][aiAgentCol] = AI_AGENT;
    }


    private void placeTreasures(int numTreasures) {
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

    private int countEmptyCells() {
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




    private boolean isValidMove(int row, int col) {
        return row >= 0 && row < maze.length && col >= 0 && col < maze[0].length && maze[row][col] != WALL;
    }


    private void movePlayer(String direction) {
        int newRow = playerRow;
        int newCol = playerCol;
        String message="Player got a penalty. Moving away from the treasure.";

        switch (direction.toLowerCase()) {
            case "up":
                newRow--;
                break;
            case "down":
                newRow++;
                break;
            case "left":
                newCol--;
                break;
            case "right":
                newCol++;
                break;
            default:
                System.out.println("Invalid direction. Please enter 'up', 'down', 'left', or 'right'.");
                return;
        }

        if (isValidMove(newRow, newCol)) {
            if (maze[newRow][newCol] == PENALTY) {
                System.out.println("Player received a penalty! Moving away from the closest treasures.");
                moveAwayFromTreasures(playerRow, playerCol);

                displayPenaltyMessage(message);
                updatePenaltyLocation(newRow,newCol);
                return;
            }

            performMove(newRow, newCol);
        } else {
            System.out.println("Invalid move. You cannot go outside the maze or through walls.");
        }
    }

    private void moveAwayFromTreasures(int row, int col) {
        Cell closestTreasure = findNearestTreasure(row, col);

        if (closestTreasure != null) {
            int newRow;
            int newCol;

            // Calculate the initial position
            newRow = row + Integer.compare(row, closestTreasure.row) * 3;
            newCol = col + Integer.compare(col, closestTreasure.col) * 3;

            // Adjust the position to be within maze boundaries
            newRow = Math.max(1, Math.min(newRow, maze.length - 2));
            newCol = Math.max(1, Math.min(newCol, maze[0].length - 2));

            // Check if the initial position is a wall
            if (maze[newRow][newCol] == WALL) {
                // Calculate a new position further away from the treasure
                int rowDifference = closestTreasure.row - row;
                int colDifference = closestTreasure.col - col;

                newRow = row - Integer.compare(row, closestTreasure.row) * 3;
                newCol = col - Integer.compare(col, closestTreasure.col) * 3;

                // Adjust the new position to be within maze boundaries
                newRow = Math.max(1, Math.min(newRow, maze.length - 2));
                newCol = Math.max(1, Math.min(newCol, maze[0].length - 2));

                // Check if the new position is a wall; if it is, adjust further
                while (maze[newRow][newCol] == WALL) {
                    newRow += Integer.compare(rowDifference, 0);
                    newCol += Integer.compare(colDifference, 0);

                    newRow = Math.max(1, Math.min(newRow, maze.length - 2));
                    newCol = Math.max(1, Math.min(newCol, maze[0].length - 2));
                }
            }

            // Update the player or AI agent position in the maze
            if (row == playerRow && col == playerCol) {
                maze[playerRow][playerCol] = EMPTY_CELL;
                playerRow = newRow;
                playerCol = newCol;
                maze[playerRow][playerCol] = PLAYER;
            } else if (row == aiAgentRow && col == aiAgentCol) {
                maze[aiAgentRow][aiAgentCol] = EMPTY_CELL;
                aiAgentRow = newRow;
                aiAgentCol = newCol;
                maze[aiAgentRow][aiAgentCol] = AI_AGENT;
            }
        }
    }



    private void performMove(int newRow, int newCol) {
        if (maze[newRow][newCol] == TREASURE) {
            System.out.println("Congratulations! You found a treasure.");
            playerScore++;
        }

        maze[playerRow][playerCol] = EMPTY_CELL;
        playerRow = newRow;
        playerCol = newCol;
        maze[playerRow][playerCol] = PLAYER;
    }

    private void moveAiAgent() {
        List<Cell> path = findPathAStar();
        String message="Ai got a penalty.Moving away from the treasure.";

        if (path != null && !path.isEmpty()) {

            Cell nextCell = path.get(0);

            if (nextCell.row == playerRow && nextCell.col == playerCol) {
                System.out.println("AI agent is waiting for its next turn.");
            } else {

                if (maze[nextCell.row][nextCell.col] == PENALTY) {
                    System.out.println("AI agent received a penalty! Moving away from the closest treasures.");
                    moveAwayFromTreasures(aiAgentRow, aiAgentCol);
                    displayPenaltyMessage(message);
                    updatePenaltyLocation(nextCell.row,nextCell.col);
                    return;
                }
                if (maze[nextCell.row][nextCell.col] == TREASURE) {
                    System.out.println("AI agent found a treasure.");
                    aiScore++;
                }

                maze[aiAgentRow][aiAgentCol] = EMPTY_CELL;
                aiAgentRow = nextCell.row;
                aiAgentCol = nextCell.col;
                maze[aiAgentRow][aiAgentCol] = AI_AGENT;

            }
        }
    }

    private void updatePenaltyLocation(int penaltyRow, int penaltyCol) {
        int newRow, newCol;
        do {
            newRow = random.nextInt(maze.length - 2) + 1;
            newCol = random.nextInt(maze[0].length - 2) + 1;
        } while (maze[newRow][newCol] != EMPTY_CELL);

        maze[penaltyRow][penaltyCol] = EMPTY_CELL;
        maze[newRow][newCol] = PENALTY;
    }


    private List<Cell> findPathAStar() {
        PriorityQueue<Cell> openSet = new PriorityQueue<>(Comparator.comparingInt(cell -> cell.fScore));
        Map<Cell, Cell> cameFrom = new HashMap<>();
        Map<Cell, Integer> gScore = new HashMap<>();

        Cell start = new Cell(aiAgentRow, aiAgentCol);
        Cell goal = findNearestTreasure(aiAgentRow, aiAgentCol);

        if (goal == null) {
            return Collections.emptyList();
        }

        gScore.put(start, 0);
        start.fScore = heuristicCostEstimate(start, goal);
        openSet.add(start);

        while (!openSet.isEmpty()) {
            Cell current = openSet.poll();

            if (current.equals(goal)) {
                return reconstructPath(cameFrom, current);
            }

            for (Cell neighbor : getNeighbors(current)) {
                int tentativeGScore = gScore.getOrDefault(current, Integer.MAX_VALUE) + 1;

                if (tentativeGScore < gScore.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentativeGScore);
                    neighbor.fScore = tentativeGScore + heuristicCostEstimate(neighbor, goal);

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }

        return Collections.emptyList();
    }


    private int heuristicCostEstimate(Cell current, Cell goal) {
        int dx = Math.abs(current.row - goal.row);
        int dy = Math.abs(current.col - goal.col);
        return dx + dy + Math.min(dx, dy);
    }


    private List<Cell> reconstructPath(Map<Cell, Cell> cameFrom, Cell current) {
        List<Cell> path = new ArrayList<>();
        while (cameFrom.containsKey(current)) {
            path.add(current);
            current = cameFrom.get(current);
        }
        Collections.reverse(path);
        return path;
    }

    private List<Cell> getNeighbors(Cell cell) {
        List<Cell> neighbors = new ArrayList<>();

        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};

        for (int i = 0; i < 4; i++) {
            int newRow = cell.row + dr[i];
            int newCol = cell.col + dc[i];

            if (isValidMove(newRow, newCol)) {
                neighbors.add(new Cell(newRow, newCol));
            }
        }

        return neighbors;
    }


    private Cell findNearestTreasure(int row, int col) {
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

    private static class Cell {
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
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[0].length; j++) {
                if (maze[i][j] == TREASURE) {
                    return false;
                }
            }
        }
        return true;
    }


}