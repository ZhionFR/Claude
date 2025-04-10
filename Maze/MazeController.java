package org.codes_maze.controller;

import org.codes_maze.model.*;

public class MazeController {
    private Maze maze;
    private GameState gameState;
    private int availableTorches;
    private long startTime;
    
    public MazeController() {
        this.maze = new Maze();
        this.gameState = GameState.RUNNING;
        this.availableTorches = 1;
        this.startTime = System.currentTimeMillis();
    }
    
    public void initializeGame(int rows, int cols, double wallRatio, double torchRatio) {
        maze.initialize(rows, cols, wallRatio, torchRatio);
        gameState = GameState.RUNNING;
        availableTorches = 1;
        startTime = System.currentTimeMillis();
    }
    
    public boolean movePlayer(Direction direction) {
        if (gameState != GameState.RUNNING) {
            return false;
        }
        
        Position playerPos = maze.getPlayerPosition();
        Position newPos = calculateNewPosition(playerPos, direction);
        
        if (!isValidPosition(newPos)) {
            return false;
        }
        
        Cell targetCell = maze.getCell(newPos.getRow(), newPos.getCol());
        if (targetCell.getType() == CellType.WALL) {
            return false;
        }
        
        if (targetCell.getType() == CellType.EXIT) {
            gameState = GameState.WON;
        }
        
        if (targetCell.getType() == CellType.TORCH) {
            availableTorches++;
        }
        
        maze.movePlayer(newPos);
        
        revealAdjacentCells(newPos);
        
        return true;
    }
    
    public boolean useTorch() {
        if (availableTorches <= 0 || gameState != GameState.RUNNING) {
            return false;
        }
        
        availableTorches--;
        
        Position playerPos = maze.getPlayerPosition();
        int row = playerPos.getRow();
        int col = playerPos.getCol();
        
        for (int r = Math.max(0, row - 2); r <= Math.min(maze.getRows() - 1, row + 2); r++) {
            for (int c = Math.max(0, col - 2); c <= Math.min(maze.getCols() - 1, col + 2); c++) {
                maze.getCell(r, c).setRevealed(true);
            }
        }
        
        return true;
    }
    
    private Position calculateNewPosition(Position currentPos, Direction direction) {
        int newRow = currentPos.getRow();
        int newCol = currentPos.getCol();
        
        switch (direction) {
            case UP:
                newRow--;
                break;
            case DOWN:
                newRow++;
                break;
            case LEFT:
                newCol--;
                break;
            case RIGHT:
                newCol++;
                break;
        }
        
        return new Position(newRow, newCol);
    }
    
    private boolean isValidPosition(Position pos) {
        return pos.getRow() >= 0 && pos.getRow() < maze.getRows() && 
               pos.getCol() >= 0 && pos.getCol() < maze.getCols();
    }
    
    private void revealAdjacentCells(Position pos) {
        int row = pos.getRow();
        int col = pos.getCol();
        
        for (int r = Math.max(0, row - 1); r <= Math.min(maze.getRows() - 1, row + 1); r++) {
            for (int c = Math.max(0, col - 1); c <= Math.min(maze.getCols() - 1, col + 1); c++) {
                maze.getCell(r, c).setRevealed(true);
            }
        }
    }
    
    public Maze getMaze() {
        return maze;
    }
    
    public GameState getGameState() {
        return gameState;
    }
    
    public int getAvailableTorches() {
        return availableTorches;
    }
    
    public Position getPlayerPosition() {
        return maze.getPlayerPosition();
    }
    
    public long getGameTime() {
        return (System.currentTimeMillis() - startTime) / 1000;
    }
}