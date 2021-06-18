# Sudoku_Extra
This project is a Sudoku type game (1x1 - 9x9 grid where one number can only appear once in every row and column) with a GUI made with javaFX.

In addition to the base rules, the grid has cages (with randomized shapes, a mathematical operator (/,*,+,-) and a value)
and every cage has an additional constraint -the numbers in every cage evaluate to the value of the cage 
when the mathematical operator is applied to them.
For example, a cage with the + operator and a value of 9 and a size of 3 cells
means that the numbers have to add up to 9 (2,3,4).
There are 3 ways of starting a game:
- start with a random field
- start by loading a field from a file (there are example files included)
- start by loading a field by hand

There is an additional file with instructions on how to run this project!
