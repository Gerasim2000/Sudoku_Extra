package sample;
public class Cell {

    private int row;
    private int column;
    int value;
    private Cage cage;
    private Grid grid;


    public Cell(int row, int column, int value) {
        this.row = row;
        this.column = column;
        this.value = value;
    }

    /*
        Setter methods
        */
    public void setValue(int value) {
      if(value <= 8 && value >= 0) {
          int oldValue = this.value;
          this.value = value;
          this.getGrid().action(this, oldValue);

      }
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
    }

    public Grid getGrid() {
        return grid;
    }

    public void setCage(Cage cage) {
        this.cage = cage;
    }

    public Cage getCage() {
        return cage;
    }

    /*
         Getter methods
          */
    public int getValue() {
        return this.value;
    }

    public int getRow() {
        return this.row;
    }

    public int getColumn() {
        return this.column;
    }
}
