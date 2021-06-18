/*
Stores all cells in the cage,
Stores the arithmetic operator,
Stores the value of needed outcome,
Has a boolean value completed when
the cells inside complete
 */
package sample;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class Cage {
    int solution;
    ArrayList<Cell> cageCells;
    enum operator{
        DIVISION,
        MULTIPLICATION,
        ADDITION,
        SUBTRACTION
    }
    operator sign;
    Grid grid;

    public Cage(int solution, operator sign, ArrayList<Integer> coordinates, int n) {
        this.cageCells = new ArrayList<>();
        this.solution = solution;
        for (Integer coordinate : coordinates) {
            this.cageCells.add(new Cell((coordinate - 1) / n, (coordinate - 1) % n, 0));
        }
        this.sign = sign;
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
        this.updateValues();
    }

    // takes the references of the same cells from the grid in order to keep values the same
    public void updateValues() {
        for(int i = 0; i < cageCells.size(); i++) {
            cageCells.set(i, grid.getCell(cageCells.get(i).getRow(), cageCells.get(i).getColumn()));
            grid.getCell(cageCells.get(i).getRow(), cageCells.get(i).getColumn()).setCage(this);
        }
    }

    //checks if the values of the cells in the cage come up to the solution
    public boolean isSolved() {
        // get an array list full of values of cages
        ArrayList<Integer> cageValues = new ArrayList<>();

        // fill it up with values and then sort it
        for (Cell cell: this.cageCells) {
            cageValues.add(cell.getValue());
            if(cell.getValue() == 0) return false;
        }

        cageValues.sort(Collections.reverseOrder());
        int value = 0;

        // check for the sign and do the math
        if(this.sign == operator.MULTIPLICATION) {
            value = 1;
            for(Integer cellValue : cageValues) {
                if(cellValue != 0) value = cellValue * value;
            }
        }

        if(this.sign == operator.ADDITION) {
            for(Integer cellValue : cageValues) {
                value = cellValue + value;
            }
        }

        if(this.sign == operator.DIVISION) {
            value = 1;
            for(int i = 1; i < cageValues.size(); i++) {
                if(cageValues.get(i) != 0) value = cageValues.get(i) * value;
            }
            if(cageValues.get(0) % value == 0) value = cageValues.get(0) / value;
            else value = 0;
        }

        if(this.sign == operator.SUBTRACTION) {
            value = 0;
            for(int i = 1; i < cageValues.size(); i++) {
                value = cageValues.get(i) + value;
            }
            if(cageValues.get(0) - value >= 0) value = cageValues.get(0) - value;
        }
        return value == this.solution;
    }
}