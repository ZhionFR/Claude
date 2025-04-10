package org.codes_maze.model;

import java.util.Random;

public enum CellType {
    EMPTY, WALL, PLAYER, EXIT, TORCH
}

public enum GameState {
    RUNNING, WON, LOST
}

public enum Direction {
    UP, DOWN, LEFT, RIGHT
}

public class Position {
    private int row;
    private int col;
    
    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }
    
    public int getRow() { return row; }
    public int getCol() { return col; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Position other = (Position) obj;
        return row == other.row && col == other.col;
    }
    
    @Override
    public int hashCode() {
        return 31 * row + col;
    }
}

public class Cell {
    private CellType type;
    private boolean revealed;
    
    public Cell(CellType type) {
        this.type = type;
        this.revealed = false;
    }
    
    public CellType getType() { return type; }
    public void setType(CellType type) { this.type = type; }
    
    public boolean isRevealed() { return revealed; }
    public void setRevealed(boolean revealed) { this.revealed = revealed; }
}

public class Maze {
    private int rows;
    private int cols;
    private Cell[][] grid;
    private Position playerPosition;
    private Position exitPosition;
    private Random random;
    
    public Maze() {
        this.random = new Random();
    }
    
    public void initialize(int rows, int cols, double wallRatio, double torchRatio) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new Cell[rows][cols];
        
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = new Cell(CellType.EMPTY);
            }
        }
        
        int totalCells = rows * cols;
        int wallCount = (int) (totalCells * wallRatio);
        
        for (int i = 0; i < wallCount; i++) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);
            
            if (grid[r][c].getType() == CellType.EMPTY) {
                grid[r][c].setType(CellType.WALL);
            }
        }
        
        do {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);
            
            if (grid[r][c].getType() == CellType.EMPTY) {
                grid[r][c].setType(CellType.PLAYER);
                playerPosition = new Position(r, c);
                grid[r][c].setRevealed(true);
                break;
            }
        } while (true);
        
        do {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);
            
            if (grid[r][c].getType() == CellType.EMPTY && 
                (Math.abs(r - playerPosition.getRow()) + Math.abs(c - playerPosition.getCol()) > 3)) {
                grid[r][c].setType(CellType.EXIT);
                exitPosition = new Position(r, c);
                break;
            }
        } while (true);
        
        int torchCount = (int) (totalCells * torchRatio);
        int placedTorches = 0;
        
        while (placedTorches < torchCount) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);
            
            if (grid[r][c].getType() == CellType.EMPTY) {
                grid[r][c].setType(CellType.TORCH);
                placedTorches++;
            }
        }
        
        revealAdjacentCells(playerPosition);
    }
    
    public Cell getCell(int row, int col) {
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            return grid[row][col];
        }
        return null;
    }
    
    public void movePlayer(Position newPos) {
        CellType destType = grid[newPos.getRow()][newPos.getCol()].getType();
        
        grid[playerPosition.getRow()][playerPosition.getCol()].setType(CellType.EMPTY);
        
        if (destType != CellType.EXIT) {
            grid[newPos.getRow()][newPos.getCol()].setType(CellType.PLAYER);
        }
        
        playerPosition = newPos;
    }
    
    private void revealAdjacentCells(Position pos) {
        int row = pos.getRow();
        int col = pos.getCol();
        
        for (int r = Math.max(0, row - 1); r <= Math.min(rows - 1, row + 1); r++) {
            for (int c = Math.max(0, col - 1); c <= Math.min(cols - 1, col + 1); c++) {
                grid[r][c].setRevealed(true);
            }
        }
    }
    
    public int getRows() {
        return rows;
    }
    
    public int getCols() {
        return cols;
    }
    
    public Position getPlayerPosition() {
        return playerPosition;
    }
    
    public Position getExitPosition() {
        return exitPosition;
    }
}