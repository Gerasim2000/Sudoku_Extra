package sample;

import java.util.ArrayList;
import java.util.Stack;

/*
This class represents the board,
it has N*N square objects in a 2d array
it also has a cage[] with all which stores
all cages
 */
//TODO:Grid (resizable) 34 marks?
public class Grid {

    //
    private int n;
    ArrayList<Cage> cages;
    Cell[][] playerCells;
    private Cell selectedCell;
    private Cell[][] solution;
    Stack<Cell> undoStack;
    Stack<Integer> undoMemory;
    Stack<Cell> redoStack;
    Stack<Integer> redoMemory;
    boolean badInput;

    // initialise 2d array
    public Grid(int n) {
        this.n = n;
        playerCells = new Cell[n][n];
        for (int i = 0; i < n; i++) {
            for(int j = 0; j < n; j++) {
                playerCells[i][j] = new Cell(i, j, 0);
            }
        }
        this.selectedCell = playerCells[0][0];
        this.cages = new ArrayList<>();
        this.solution = new Cell[n][n];
        undoStack = new Stack<>();
        undoMemory = new Stack<>();
        redoStack = new Stack<>();
        redoMemory = new Stack<>();
        this.badInput = false;
    }

    public int getN() {
        return n;
    }

    public void setSolution(Cell[][] solution) {
        this.solution = solution;
    }

    public Cell[][] getSolution() {
        return solution;
    }

    public Cell getCell(int row, int col) {
        return this.playerCells[row][col];
    }

    public void setSelectedCell(int row, int column) {
        this.selectedCell = this.playerCells[row][column];
    }

    public Cell getSelectedCell() {
        return this.selectedCell;
    }

    public void clearStacks() {
        undoStack.clear();
        undoMemory.clear();
        redoStack.clear();
        redoMemory.clear();
    }

    public void undo() {
        this.getCell(undoStack.peek().getRow(), undoStack.peek().getColumn()).value = undoMemory.peek();
        redoStack.push(undoStack.pop());
        redoMemory.push(undoMemory.pop());
    }

    public void redo() {
            this.getCell(redoStack.peek().getRow(), redoStack.peek().getColumn()).value = redoStack.peek().getValue();
            undoStack.push(redoStack.pop());
            undoMemory.push(redoMemory.pop());
    }

    public void action(Cell cell, Integer previousValue) {

        if(cell.getValue() != previousValue) {
            redoStack.clear();
            undoStack.push(new Cell(cell.getRow(), cell.getColumn(), cell.getValue()));
            undoMemory.push(previousValue);
        }
    }

    public void clear() {
        for(int i = 0; i < n; i++) {
            for(int j = 0; j < n; j++) {
                this.getCell(i,j).setValue(0);
            }
        }
        this.clearStacks();
    }
    /*
    1. goes through all cages and calls the method which checks if the value
    obtained by doing the calculations is the same as the value of the solution

    2. goes through all the elements row by row and then column by column
    adds all of the elements of each row and each column to an arraylist
    and then goes through all of the numbers and checks for the numbers from
    1 to N whether first index == last index of, if not returns false
     */
    public boolean checkVictory() {

        for(Cage cage : this.cages) {
            if(!cage.isSolved()) return false;
        }

        ArrayList<Integer> numbersInRow = new ArrayList<>();

        //goes through all rows
        for(int i = 0; i < this.n; i++) {

            // adds the values of cells from the row
            for(int j = 0; j < this.n; j++) {
                numbersInRow.add(this.playerCells[i][j].getValue());
            }

            //checks if there are any numbers that appear twice
            for(int number = 1; number <= this.n; number++) {
                if(numbersInRow.indexOf(number) != numbersInRow.lastIndexOf(number)) {
                    return false;
                }
            }
            numbersInRow.clear();
        }

        // goes through all columns and checks the values
        for(int k = 0; k < this.n; k++) {

            // adds the values of cells from the column
            for(int l = 0; l < this.n; l++) {
                numbersInRow.add(this.playerCells[l][k].getValue());
            }

            //checks if there are any numbers that appear twice
            for(int num = 1; num <= this.n; num++) {
                if(numbersInRow.indexOf(num) != numbersInRow.lastIndexOf(num)) {
                    return false;
                }
            }
            numbersInRow.clear();
        }

        return true;
    }
}
