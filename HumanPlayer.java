import javafx.animation.FadeTransition;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.Random;

import static java.time.zone.ZoneOffsetTransitionRule.TimeDefinition.WALL;

public class HumanPlayer {
    public int playerRow;
    public int playerScore = 0;
    public Random random = new Random();
    public int playerCol;
    public void movePlayer(String direction) {
        int newRow = playerRow;
        int newCol = playerCol;
        String message="Player got a penalty. Moving away from the treasure.";

        switch (direction.toLowerCase()) {
            case "up" -> newRow--;
            case "down" -> newRow++;
            case "left" -> newCol--;
            case "right" -> newCol++;
            default -> {
                System.out.println("Invalid direction. Please enter 'up', 'down', 'left', or 'right'.");
                return;
            }
        }

        if (MazeGame.isValidMove(newRow, newCol)) {
            if (MazeGame.maze[newRow][newCol] == MazeGame.PENALTY) {
                System.out.println("Player received a penalty! Moving away from the closest treasures.");
                movePlayerAwayFromClosestTreasure(playerRow, playerCol);

                MazeGame.displayPenaltyMessage(message);
                MazeGame.updatePenaltyLocation(newRow,newCol);
                return;
            }

            performMove(newRow, newCol);
        } else {
            System.out.println("Invalid move. You cannot go outside the maze or through walls.");
        }
    }
    public void movePlayerAwayFromClosestTreasure(int playerRow, int playerCol) {
        MazeGame.Cell closestTreasure = MazeGame.findNearestTreasure(playerRow, playerCol);

        if (closestTreasure != null) {
            int newRow = playerRow;
            int newCol = playerCol;

            int rowDifference = closestTreasure.row - playerRow;
            int colDifference = closestTreasure.col - playerCol;


            newRow = playerRow + Integer.compare(playerRow, closestTreasure.row) * 3;
            newCol = playerCol + Integer.compare(playerCol, closestTreasure.col) * 3;


            newRow = Math.max(1, Math.min(newRow, MazeGame.maze.length - 2));
            newCol = Math.max(1, Math.min(newCol, MazeGame.maze[0].length - 2));

            while(MazeGame.maze[newRow][newCol]==MazeGame.WALL){
                int number = random.nextInt(2);
                int addNumber = random.nextInt(5) - 2;

                int tempNewRow = newRow;
                int tempNewCol = newCol;

                if (number == 0) {
                    tempNewRow += addNumber;
                } else {
                    tempNewCol += addNumber;
                }

                tempNewRow = Math.max(1, Math.min(tempNewRow, MazeGame.maze.length - 2));
                tempNewCol = Math.max(1, Math.min(tempNewCol, MazeGame.maze[0].length - 2));

                if (MazeGame.maze[tempNewRow][tempNewCol] != MazeGame.WALL) {
                    newRow = tempNewRow;
                    newCol = tempNewCol;
                }
            }

            if(this.playerCol== MazeGame.aiPlayer.aiAgentCol&&this.playerRow== MazeGame.aiPlayer.aiAgentRow){
                MazeGame.maze[this.playerRow][this.playerCol] = MazeGame.AI_AGENT;
            }
            else{
                MazeGame.maze[this.playerRow][this.playerCol] = MazeGame.EMPTY_CELL;

            }
            this.playerRow = newRow;
            this.playerCol = newCol;
            MazeGame.maze[this.playerRow][this.playerCol] = MazeGame.PLAYER;

        }
    }
    private void performMove(int newRow, int newCol) {
        if (MazeGame.maze[newRow][newCol] == MazeGame.TREASURE) {
            System.out.println("Congratulations! You found a treasure.");
            playerScore++;
        }

        if(playerRow== MazeGame.aiPlayer.aiAgentRow&&playerCol== MazeGame.aiPlayer.aiAgentCol){
            MazeGame.maze[playerRow][playerCol] = MazeGame.AI_AGENT;
        }else{
            MazeGame.maze[playerRow][playerCol] = MazeGame.EMPTY_CELL;
        }

        playerRow = newRow;
        playerCol = newCol;
        if(MazeGame.maze[playerRow][playerCol]== MazeGame.AI_AGENT){
            MazeGame.maze[playerRow][playerCol]= MazeGame.PLAYER;
            MazeGame.maze[playerRow][playerCol]= MazeGame.AI_AGENT;
        }else{
            MazeGame.maze[playerRow][playerCol] = MazeGame.PLAYER;
        }

    }



}
