import java.util.*;

class MazeGame {
    private static final char EMPTY_CELL = '.';
    private static final char WALL = '#';
    private static final char PLAYER = 'P';
    private static final char TREASURE = 'T';
    private static final char AI_AGENT = 'A';
    private static final char PENALTY = 'X';
    Random random = new Random();


    private char[][] maze;
    private int playerRow;
    private int playerCol;
    private int aiAgentRow;
    private int aiAgentCol;

    public MazeGame(int rows, int cols, int numObstacles,int numPenalties) {
        maze = generateMaze(rows, cols, numObstacles, numPenalties);

        initializePlayers();
        placeTreasures(5); // Adjust the number of treasures as needed
    }
    
    
    

    private char[][] generateMaze(int rows, int cols, int numObstacles, int numPenalties) {
        char[][] newMaze = new char[rows][cols];
        Random random = new Random();
    
        // Initialize the maze with empty cells
        for (int i = 0; i < rows; i++) {
            Arrays.fill(newMaze[i], EMPTY_CELL);
        }
    
        // Add walls on borders
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
    
    
        // Add random obstacles
        
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

        aiAgentRow = random.nextInt(maze.length - 2) + 1;
        aiAgentCol = random.nextInt(maze[0].length - 2) + 1;

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
    
    

    private void printMaze() {
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[0].length; j++) {
                char displayChar = maze[i][j];
                if (displayChar == PENALTY) {
                    // Replace the display character for penalties with a space
                    displayChar = EMPTY_CELL;
                }
                System.out.print(displayChar + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
    

    private boolean isValidMove(int row, int col) {
        return row >= 0 && row < maze.length && col >= 0 && col < maze[0].length && maze[row][col] != WALL;
    }
    

    private void movePlayer(String direction) {
        int newRow = playerRow;
        int newCol = playerCol;
    
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
            performMove(newRow, newCol);
        } else {
            System.out.println("Invalid move. You cannot go outside the maze or through walls.");
        }
        if (maze[playerRow][playerCol] == PENALTY) {
            System.out.println("Penalty! Moving back to another empty place.");
            moveBackToEmptyPlace(playerRow, playerCol);
        }
    }
    private void moveBackToEmptyPlace(int currentRow, int currentCol) {
        int newRow, newCol;
        do {
            newRow = random.nextInt(maze.length - 2) + 1;
            newCol = random.nextInt(maze[0].length - 2) + 1;
        } while (maze[newRow][newCol] != EMPTY_CELL);
    
        maze[currentRow][currentCol] = EMPTY_CELL;
        maze[newRow][newCol] = currentRow == playerRow ? PLAYER : AI_AGENT;
    
        if (currentRow == playerRow) {
            playerRow = newRow;
            playerCol = newCol;
        } else {
            aiAgentRow = newRow;
            aiAgentCol = newCol;
        }
    }
    private void performMove(int newRow, int newCol) {
        if (maze[newRow][newCol] == TREASURE) {
            System.out.println("Congratulations! You found a treasure.");
        }

        maze[playerRow][playerCol] = EMPTY_CELL;
        playerRow = newRow;
        playerCol = newCol;
        maze[playerRow][playerCol] = PLAYER;
    }

    private void moveAiAgent() {
        List<Cell> path = findPathAStar();
    
        if (path != null && !path.isEmpty()) {


            
            // Check if the AI agent and player are in the same cell
            if (aiAgentRow == playerRow && aiAgentCol == playerCol) {
                System.out.println("AI agent is waiting for its next turn.");
            } else {
                // Move the AI agent to the next cell in the path
                Cell nextCell = path.get(0);
    
                // Check if the next cell is occupied by the player
                if (nextCell.row == playerRow && nextCell.col == playerCol) {
                    System.out.println("AI agent is waiting for its next turn.");
                } else {
                    maze[aiAgentRow][aiAgentCol] = EMPTY_CELL;
                    aiAgentRow = nextCell.row;
                    aiAgentCol = nextCell.col;
                    maze[aiAgentRow][aiAgentCol] = AI_AGENT;
                }

      // Check if the next cell is a penalty
        if (maze[aiAgentRow][aiAgentCol] == PENALTY) {
            System.out.println("AI agent received a penalty! Moving back to another empty place.");
            moveBackToEmptyPlace(aiAgentRow, aiAgentCol);
        } else {
            maze[aiAgentRow][aiAgentCol] = EMPTY_CELL;
            aiAgentRow = nextCell.row;
            aiAgentCol = nextCell.col;
            maze[aiAgentRow][aiAgentCol] = AI_AGENT;
        }
            }
     
            
        }
    }
    
    
    
    private List<Cell> findPathAStar() {
        PriorityQueue<Cell> openSet = new PriorityQueue<>(Comparator.comparingInt(cell -> cell.fScore));
        Map<Cell, Cell> cameFrom = new HashMap<>();
        Map<Cell, Integer> gScore = new HashMap<>();
    
        Cell start = new Cell(aiAgentRow, aiAgentCol);
        Cell goal = findNearestTreasure();
    
        if (goal == null) {
            return Collections.emptyList(); // No treasures left
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
    
        return Collections.emptyList(); // No path found
    }
    
    

    private int heuristicCostEstimate(Cell current, Cell goal) {
        // A* heuristic: Manhattan distance with tie-breaker for straight movements
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
    

    private Cell findNearestTreasure() {
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

        // Find the nearest treasure (using Manhattan distance)
        treasures.sort(Comparator.comparingInt(cell -> Math.abs(aiAgentRow - cell.row) + Math.abs(aiAgentCol - cell.col)));
        return treasures.get(0);
    }

    private static class Cell {
        int row;
        int col;
        int fScore; // Combined cost of gScore and heuristic (f = g + h)

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

    public void removeTreasure(int row, int col) {
        if (maze[row][col] == TREASURE) {
            maze[row][col] = EMPTY_CELL;
        }
    }
    
    public boolean allTreasuresFound() {
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[0].length; j++) {
                if (maze[i][j] == TREASURE) {
                    return false; // There is at least one treasure remaining
                }
            }
        }
        return true; // All treasures have been found
    }
    

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the number of rows: ");
        int rows = scanner.nextInt();

        System.out.print("Enter the number of columns: ");
        int cols = scanner.nextInt();

        System.out.print("Enter the number of obstacles: ");
        int numObstacles = scanner.nextInt();

        System.out.print("Enter the number of penalties: ");
        int numOfPenalties = scanner.nextInt();



        MazeGame mazeGame = new MazeGame(rows, cols, numObstacles,numOfPenalties); 

        while (true) {
            mazeGame.printMaze();

            System.out.print("Enter direction for player (up/down/left/right): ");
            String playerDirection = scanner.next();
            mazeGame.movePlayer(playerDirection);

            mazeGame.moveAiAgent();

            // Check if the player has reached a treasure
            if (mazeGame.maze[mazeGame.playerRow][mazeGame.playerCol] == TREASURE) {
                System.out.println("Congratulations! You found a treasure.");
                break;
            }

            if (mazeGame.allTreasuresFound()) {
                System.out.println("Congratulations! You found all treasures. Game over!");
                break;
            }
        }

        scanner.close();
    }
}
