import java.util.*;

import static java.time.zone.ZoneOffsetTransitionRule.TimeDefinition.WALL;

public class AIPlayer {
    public static Random random = new Random();

    public int aiAgentRow;
    public int aiScore = 0;
    public int aiAgentCol;
    public void moveAIAwayFromClosestTreasure(int aiAgentRow, int aiAgentCol) {
        MazeGame.Cell closestTreasure = MazeGame.findNearestTreasure(aiAgentRow, aiAgentCol);

        if (closestTreasure != null) {
            int newRow = aiAgentRow;
            int newCol = aiAgentCol;

            int rowDifference = closestTreasure.row - aiAgentRow;
            int colDifference = closestTreasure.col - aiAgentCol;


            newRow = aiAgentRow + Integer.compare(aiAgentRow, closestTreasure.row) * 3;
            newCol = aiAgentCol + Integer.compare(aiAgentCol, closestTreasure.col) * 3;


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
            if(this.aiAgentRow== MazeGame.humanPlayer.playerRow&&this.aiAgentCol==MazeGame.humanPlayer.playerCol){
                MazeGame.maze[this.aiAgentRow][this.aiAgentCol] = MazeGame.PLAYER;
            }
            else{
                MazeGame.maze[this.aiAgentRow][this.aiAgentCol] = MazeGame.EMPTY_CELL;

            }
            this.aiAgentRow = newRow;
            this.aiAgentCol = newCol;
            MazeGame.maze[this.aiAgentRow][this.aiAgentCol] = MazeGame.AI_AGENT;

        }
    }
    public void moveAiAgent() {
        List<MazeGame.Cell> path = findPathAStar();
        String message="Ai got a penalty.Moving away from the treasure.";

        if (path != null && !path.isEmpty()) {

            MazeGame.Cell nextCell = path.get(0);

            if (nextCell.row == MazeGame.humanPlayer.playerRow && nextCell.col == MazeGame.humanPlayer.playerCol) {
                System.out.println("AI agent is waiting for its next turn.");
            } else {

                if (MazeGame.maze[nextCell.row][nextCell.col] == MazeGame.PENALTY) {
                    System.out.println("AI agent received a penalty! Moving away from the closest treasures.");
                    moveAIAwayFromClosestTreasure(aiAgentRow, aiAgentCol);
                    MazeGame.displayPenaltyMessage(message);
                    MazeGame.updatePenaltyLocation(nextCell.row,nextCell.col);
                    return;
                }
                if (MazeGame.maze[nextCell.row][nextCell.col] == MazeGame.TREASURE) {
                    System.out.println("AI agent found a treasure.");
                    aiScore++;
                }
                if(aiAgentCol==MazeGame.humanPlayer.playerCol&&aiAgentRow==MazeGame.humanPlayer.playerRow){
                    MazeGame.maze[aiAgentRow][aiAgentCol] = MazeGame.PLAYER;

                }else{
                    MazeGame.maze[aiAgentRow][aiAgentCol] = MazeGame.EMPTY_CELL;

                }
                aiAgentRow = nextCell.row;
                aiAgentCol = nextCell.col;
                MazeGame.maze[aiAgentRow][aiAgentCol] = MazeGame.AI_AGENT;


            }
        }
    }
    public List<MazeGame.Cell> findPathAStar() {
        PriorityQueue<MazeGame.Cell> openSet = new PriorityQueue<>(Comparator.comparingInt(cell -> cell.fScore));
        Map<MazeGame.Cell, MazeGame.Cell> cameFrom = new HashMap<>();
        Map<MazeGame.Cell, Integer> gScore = new HashMap<>();

        MazeGame.Cell start = new MazeGame.Cell(aiAgentRow, aiAgentCol);
        MazeGame.Cell goal = MazeGame.findNearestTreasure(aiAgentRow, aiAgentCol);

        if (goal == null) {
            return Collections.emptyList();
        }

        gScore.put(start, 0);
        start.fScore = heuristicCostEstimate(start, goal);
        openSet.add(start);

        while (!openSet.isEmpty()) {
            MazeGame.Cell current = openSet.poll();

            if (current.equals(goal)) {
                return reconstructPath(cameFrom, current);
            }

            for (MazeGame.Cell neighbor : getNeighbors(current)) {
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
    public int heuristicCostEstimate(MazeGame.Cell current, MazeGame.Cell goal) {
        int dx = Math.abs(current.row - goal.row);
        int dy = Math.abs(current.col - goal.col);
        return dx + dy + Math.min(dx, dy);
    }


    public List<MazeGame.Cell> reconstructPath(Map<MazeGame.Cell, MazeGame.Cell> cameFrom, MazeGame.Cell current) {
        List<MazeGame.Cell> path = new ArrayList<>();
        while (cameFrom.containsKey(current)) {
            path.add(current);
            current = cameFrom.get(current);
        }
        Collections.reverse(path);
        return path;
    }

    public List<MazeGame.Cell> getNeighbors(MazeGame.Cell cell) {
        List<MazeGame.Cell> neighbors = new ArrayList<>();

        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};

        for (int i = 0; i < 4; i++) {
            int newRow = cell.row + dr[i];
            int newCol = cell.col + dc[i];

            if (MazeGame.isValidMove(newRow, newCol)) {
                neighbors.add(new MazeGame.Cell(newRow, newCol));
            }
        }

        return neighbors;
    }




}
