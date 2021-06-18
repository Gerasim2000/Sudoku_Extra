package sample;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.swing.text.html.Option;
import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.net.http.WebSocket;
import java.nio.Buffer;
import java.util.*;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {

        /**
         * MAIN MENU PART
         */
        VBox menu = new VBox(10);
        menu.setPrefSize(300, 300);
        Scene main = new Scene(menu);
        menu.setAlignment(Pos.CENTER);
        Label generate = new Label("Grid size(NxN)");
        Slider size = new Slider(2,8,1);
        size.setShowTickLabels(true);
        size.setMajorTickUnit(1);
        size.setMinorTickCount(0);
        size.setSnapToTicks(true);
        size.setValue(5);
        Button loadFile = new Button("Load game from file");
        Button loadText = new Button("Load game from text input");
        Button start = new Button("Start");


        //TEXT INPUT PART
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Create a game");
        alert.setHeaderText("Enter cages");
        alert.setContentText("Format: 11+ 1,7,8");
        TextArea textArea = new TextArea();
        textArea.setEditable(true);
        textArea.setWrapText(false);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);

        alert.getDialogPane().setExpandableContent(expContent);
        loadText.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent eventMouse) {
                alert.showAndWait();
                String[] allLines = textArea.getText().split("\n");


                /**
                 * READING FROM TEXT
                 */
                String[] line;
                String[] numbers;
                ArrayList<Integer> coordinates = new ArrayList<>();
                ArrayList<Cage> cages = new ArrayList<>();
                Cage cage;
                Cage.operator sign = Cage.operator.DIVISION;
                int solution;
                int n;
                int max = 0;
                int currentNumber;
                boolean badInput = false;
                ArrayList<Integer> allNumbers = new ArrayList<>();
                // finds n
                for(String string : allLines){
                    line = string.split(" ");
                    numbers = line[1].split(",");
                    for (String coord: numbers) {
                        currentNumber = Integer.parseInt(coord);
                        if(allNumbers.contains(currentNumber)) badInput = true;
                        allNumbers.add(currentNumber);
                        if(currentNumber > max) max = currentNumber;

                    }
                }
                n = (int) Math.sqrt(max);
                Grid grid = new Grid(n);

                //Obtains the information and creates the cages
                for(String string : allLines) {
                    line = string.split(" ");
                    numbers = line[1].split(",");
                    coordinates.clear();
                    for (String coord: numbers) {
                        currentNumber = Integer.parseInt(coord);
                        coordinates.add(currentNumber);
                    }
                    if (line[0].endsWith("÷")) sign = Cage.operator.DIVISION;
                    if (line[0].endsWith("x")) sign = Cage.operator.MULTIPLICATION;
                    if (line[0].endsWith("+")) sign = Cage.operator.ADDITION;
                    if (line[0].endsWith("-")) sign = Cage.operator.SUBTRACTION;
                    if(line[0].length() != 1) solution = Integer.parseInt(line[0].substring(0,line[0].length() - 1));
                    else solution = Integer.parseInt(line[0].charAt(0) + "");
                    cage = new Cage(solution, sign, coordinates, n);
                    cages.add(cage);
                    cage.setGrid(grid);
                    for(Cell cell: cage.cageCells) {
                        cell.setGrid(grid);
                    }



                    Collections.sort(coordinates);
                    for(int i = 0; i < coordinates.size(); i++) {
                        boolean itsOk = false;

                        for(Integer coordinate : coordinates) {
                            if (coordinates.get(i) - coordinate == n || coordinates.get(i) - coordinate == 1 ||
                                    coordinate - coordinates.get(i) == n || coordinate - coordinates.get(i) == 1) {
                                itsOk = true;
                                break;
                            }
                            if(coordinates.size() == 1) itsOk = true;
                        }
                        if(!itsOk) badInput = true;
                    }
                }
                grid.cages = cages;

                /**
                 * Game screen
                 */
                Pane root = new Pane();
                BorderPane bp = new BorderPane();
                Scene borderPane = new Scene(bp);
                HBox bottom = new HBox();
                VBox left = new VBox();
                HBox top = new HBox();
                VBox right = new VBox();
                right.setSpacing(10);
                Button undo = new Button("Undo");
                Button redo = new Button("Redo");
                Button clear = new Button("Clear");
                CheckBox mistake = new CheckBox("Show mistakes");
                Button back = new Button("Back");
                mistake.setSelected(false);
                undo.setDisable(true);
                redo.setDisable(true);
                Label fontLabel = new Label("Font size :");
                ComboBox<String> fontSize = new ComboBox<String>();
                fontSize.getItems().addAll("Small", "Medium", "Large");


                right.getChildren().addAll(undo,redo,clear,mistake,back,fontLabel,fontSize);
                bp.setRight(right);
                bp.setLeft(left);
                bp.setBottom(bottom);
                bp.setTop(top);
                //appropriate disabling of the buttons
                back.setOnMousePressed(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        primaryStage.setScene(main);
                    }
                });
                bp.setOnMouseMoved(mouseEvent -> {
                    if(grid.redoStack.isEmpty()) redo.setDisable(true);
                    else redo.setDisable(false);
                    if(grid.undoStack.isEmpty()) undo.setDisable(true);
                    else undo.setDisable(false);
                });
                bottom.setFillHeight(true);

                //grid layer DO NOT TOUCH
                Canvas layer1 = new Canvas(n*75, n*75);
                GraphicsContext gc1 = layer1.getGraphicsContext2D();
                drawGrid(gc1, grid, n);

                //drawing layer
                Canvas layer2 = new Canvas(n*75, n*75);
                GraphicsContext gc2 = layer2.getGraphicsContext2D();
                refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                root.getChildren().addAll(layer1,layer2);

                bp.setOnMousePressed(mouseEvent -> {
                    if(grid.redoStack.isEmpty()) redo.setDisable(true);
                    else redo.setDisable(false);
                    if(grid.undoStack.isEmpty()) undo.setDisable(true);
                    else undo.setDisable(false);
                    refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);

                });
                root.setPadding(new Insets(10));

                layer1.toFront();
                layer1.addEventHandler(MouseEvent.MOUSE_CLICKED,
                        t -> {
                            int row = (int) Math.floor(t.getY() / 75);
                            int col = (int) Math.floor(t.getX() / 75);
                            grid.setSelectedCell(row, col);
                            refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                        });

                // adding buttons to the grid
                Button button1 = new Button("1");
                button1.setOnMousePressed(mouseEvent -> {
                    grid.getSelectedCell().setValue(1);
                    refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                });
                Button button2 = new Button("2");
                button2.setOnMousePressed(mouseEvent -> {
                    grid.getSelectedCell().setValue(2);
                    refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                });
                Button button3 = new Button("3");
                button3.setOnMousePressed(mouseEvent -> {
                    grid.getSelectedCell().setValue(3);
                    refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                });
                Button button4 = new Button("4");
                button4.setOnMousePressed(mouseEvent -> {
                    grid.getSelectedCell().setValue(4);
                    refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                });
                Button button5 = new Button("5");
                button5.setOnMousePressed(mouseEvent -> {
                    grid.getSelectedCell().setValue(5);
                    refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                });
                Button button6 = new Button("6");
                button6.setOnMousePressed(mouseEvent -> {
                    grid.getSelectedCell().setValue(6);
                    refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                });
                Button button7 = new Button("7");
                button7.setOnMousePressed(mouseEvent -> {
                    grid.getSelectedCell().setValue(7);
                    refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                });
                Button button8 = new Button("8");
                button8.setOnMousePressed(mouseEvent -> {
                    grid.getSelectedCell().setValue(8);
                    refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                });
                Button delete = new Button("delete");
                delete.setOnMousePressed(mouseEvent -> {
                    grid.getSelectedCell().setValue(0);
                    refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                });

                ArrayList<Button> buttons = new ArrayList<>();
                buttons.add(button1);
                buttons.add(button2);
                buttons.add(button3);
                buttons.add(button4);
                buttons.add(button5);
                buttons.add(button6);
                buttons.add(button7);
                buttons.add(button8);
                for(int i = 1; i <= n; i++) {
                    bottom.getChildren().add(buttons.get(i-1));
                }
                bottom.getChildren().add(delete);
                mistake.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {

                        refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                    }
                });
                fontSize.valueProperty().setValue("Medium");
                fontSize.valueProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                        refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                    }
                });
                // handle keys 3from the keyboard
                borderPane.setOnKeyPressed(keyEvent -> {
                    if(grid.redoStack.isEmpty()) redo.setDisable(true);
                    else redo.setDisable(false);
                    if(grid.undoStack.isEmpty()) undo.setDisable(true);
                    else undo.setDisable(false);
                    KeyCode code = keyEvent.getCode();
                    String character = code.getChar().charAt(0) + "";
                    if(code.isDigitKey()) {
                        if(Integer.parseInt(character) <= n) grid.getSelectedCell().setValue(Integer.parseInt(character));
                        refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                    }
                    if(code == KeyCode.BACK_SPACE) {
                        grid.getSelectedCell().setValue(0);
                        refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                    }
                });
                undo.setOnMousePressed(mouseEvent -> {
                    grid.undo();
                    if(grid.redoStack.isEmpty()) redo.setDisable(true);
                    else redo.setDisable(false);
                    if(grid.undoStack.isEmpty()) undo.setDisable(true);
                    else undo.setDisable(false);
                    refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                });
                redo.setOnMousePressed(mouseEvent -> {
                    grid.redo();
                    if (grid.redoStack.isEmpty()) redo.setDisable(true);
                    else redo.setDisable(false);
                    if (grid.undoStack.isEmpty()) undo.setDisable(true);
                    else undo.setDisable(false);
                    refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                });
                clear.setOnMousePressed(mouseEvent -> {
                    Alert clearAlert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to clear the board?");
                    Optional<ButtonType> result = clearAlert.showAndWait();
                    if(result.isPresent() && result.get() == ButtonType.OK) {
                        grid.clear();
                        if (grid.redoStack.isEmpty()) redo.setDisable(true);
                        else redo.setDisable(false);
                        if (grid.undoStack.isEmpty()) undo.setDisable(true);
                        else undo.setDisable(false);
                    }
                    refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                });
                bottom.setSpacing(5);
                bottom.setAlignment(Pos.CENTER);
                bp.setPadding(new Insets(10,10,10,10));
                bp.setCenter(root);
                if(!badInput) {
                    primaryStage.setScene(borderPane);
                    primaryStage.show();
                } else {
                    Alert warning = new Alert(Alert.AlertType.ERROR);
                    warning.setTitle("Error");
                    warning.setHeaderText("There was an error in the input!");
                    warning.showAndWait();
                }

            }
        });

        loadFile.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent eventMouse) {

                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Open Resource File");
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                        new FileChooser.ExtensionFilter("All Files", "*.*"));
                File selectedFile = fileChooser.showOpenDialog(primaryStage);
                if (selectedFile != null) {
                    try {
                        Grid grid = readFromFile(selectedFile);
                        int n = grid.getN();
                        Pane root = new Pane();
                        BorderPane bp = new BorderPane();
                        Scene borderPane = new Scene(bp);
                        HBox bottom = new HBox();
                        VBox left = new VBox();
                        HBox top = new HBox();
                        VBox right = new VBox();
                        right.setSpacing(10);
                        Button undo = new Button("Undo");
                        Button redo = new Button("Redo");
                        Button clear = new Button("Clear");
                        CheckBox mistake = new CheckBox("Show mistakes");
                        Button back = new Button("Back");
                        mistake.setSelected(false);
                        undo.setDisable(true);
                        redo.setDisable(true);
                        boolean badInput = false;
                        Label fontLabel = new Label("Font size :");
                        ComboBox<String> fontSize = new ComboBox<String>();
                        fontSize.getItems().addAll("Small", "Medium", "Large");


                        right.getChildren().addAll(undo,redo,clear,mistake,back,fontLabel,fontSize);
                        bp.setRight(right);
                        bp.setLeft(left);
                        bp.setBottom(bottom);
                        bp.setTop(top);
                        //appropriate disabling of the buttons
                        back.setOnMousePressed(new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent mouseEvent) {
                                primaryStage.setScene(main);
                            }
                        });
                        bp.setOnMouseMoved(mouseEvent -> {
                            if(grid.redoStack.isEmpty()) redo.setDisable(true);
                            else redo.setDisable(false);
                            if(grid.undoStack.isEmpty()) undo.setDisable(true);
                            else undo.setDisable(false);
                        });
                        bottom.setFillHeight(true);

                        //grid layer DO NOT TOUCH
                        Canvas layer1 = new Canvas(n*75, n*75);
                        GraphicsContext gc1 = layer1.getGraphicsContext2D();
                        drawGrid(gc1, grid, n);

                        //drawing layer
                        Canvas layer2 = new Canvas(n*75, n*75);
                        GraphicsContext gc2 = layer2.getGraphicsContext2D();
                        refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                        root.getChildren().addAll(layer1,layer2);

                        bp.setOnMousePressed(mouseEvent -> {
                            if(grid.redoStack.isEmpty()) redo.setDisable(true);
                            else redo.setDisable(false);
                            if(grid.undoStack.isEmpty()) undo.setDisable(true);
                            else undo.setDisable(false);
                            refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);

                        });
                        root.setPadding(new Insets(10));

                        layer1.toFront();
                        layer1.addEventHandler(MouseEvent.MOUSE_CLICKED,
                                t -> {
                                    int row = (int) Math.floor(t.getY() / 75);
                                    int col = (int) Math.floor(t.getX() / 75);
                                    grid.setSelectedCell(row, col);
                                    refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                                });

                        // adding buttons to the grid
                        Button button1 = new Button("1");
                        button1.setOnMousePressed(mouseEvent -> {
                            grid.getSelectedCell().setValue(1);
                            refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                        });
                        Button button2 = new Button("2");
                        button2.setOnMousePressed(mouseEvent -> {
                            grid.getSelectedCell().setValue(2);
                            refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                        });
                        Button button3 = new Button("3");
                        button3.setOnMousePressed(mouseEvent -> {
                            grid.getSelectedCell().setValue(3);
                            refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                        });
                        Button button4 = new Button("4");
                        button4.setOnMousePressed(mouseEvent -> {
                            grid.getSelectedCell().setValue(4);
                            refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                        });
                        Button button5 = new Button("5");
                        button5.setOnMousePressed(mouseEvent -> {
                            grid.getSelectedCell().setValue(5);
                            refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                        });
                        Button button6 = new Button("6");
                        button6.setOnMousePressed(mouseEvent -> {
                            grid.getSelectedCell().setValue(6);
                            refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                        });
                        Button button7 = new Button("7");
                        button7.setOnMousePressed(mouseEvent -> {
                            grid.getSelectedCell().setValue(7);
                            refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                        });
                        Button button8 = new Button("8");
                        button8.setOnMousePressed(mouseEvent -> {
                            grid.getSelectedCell().setValue(8);
                            refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                        });
                        Button delete = new Button("delete");
                        delete.setOnMousePressed(mouseEvent -> {
                            grid.getSelectedCell().setValue(0);
                            refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                        });

                        ArrayList<Button> buttons = new ArrayList<>();
                        buttons.add(button1);
                        buttons.add(button2);
                        buttons.add(button3);
                        buttons.add(button4);
                        buttons.add(button5);
                        buttons.add(button6);
                        buttons.add(button7);
                        buttons.add(button8);
                        for(int i = 1; i <= n; i++) {
                            bottom.getChildren().add(buttons.get(i-1));
                        }
                        bottom.getChildren().add(delete);
                        mistake.selectedProperty().addListener(new ChangeListener<Boolean>() {
                            @Override
                            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {

                                refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                            }
                        });
                        fontSize.valueProperty().setValue("Medium");
                        fontSize.valueProperty().addListener(new ChangeListener<String>() {
                            @Override
                            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                                refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                            }
                        });

                        // handle keys 3from the keyboard
                        borderPane.setOnKeyPressed(keyEvent -> {
                            if(grid.redoStack.isEmpty()) redo.setDisable(true);
                            else redo.setDisable(false);
                            if(grid.undoStack.isEmpty()) undo.setDisable(true);
                            else undo.setDisable(false);
                            KeyCode code = keyEvent.getCode();
                            String character = code.getChar().charAt(0) + "";
                            if(code.isDigitKey()) {
                                if(Integer.parseInt(character) <= n) grid.getSelectedCell().setValue(Integer.parseInt(character));
                                refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                            }
                            if(code == KeyCode.BACK_SPACE) {
                                grid.getSelectedCell().setValue(0);
                                refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                            }
                        });
                        undo.setOnMousePressed(mouseEvent -> {
                            grid.undo();
                            if(grid.redoStack.isEmpty()) redo.setDisable(true);
                            else redo.setDisable(false);
                            if(grid.undoStack.isEmpty()) undo.setDisable(true);
                            else undo.setDisable(false);
                            refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                        });
                        redo.setOnMousePressed(mouseEvent -> {
                            grid.redo();
                            if (grid.redoStack.isEmpty()) redo.setDisable(true);
                            else redo.setDisable(false);
                            if (grid.undoStack.isEmpty()) undo.setDisable(true);
                            else undo.setDisable(false);
                            refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                        });
                        clear.setOnMousePressed(mouseEvent -> {
                            Alert clearAlert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to clear the board?");
                            Optional<ButtonType> result = clearAlert.showAndWait();
                            if(result.isPresent() && result.get() == ButtonType.OK) {
                                grid.clear();
                                if (grid.redoStack.isEmpty()) redo.setDisable(true);
                                else redo.setDisable(false);
                                if (grid.undoStack.isEmpty()) undo.setDisable(true);
                                else undo.setDisable(false);
                            }
                            refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                        });
                        bottom.setSpacing(5);
                        bottom.setAlignment(Pos.CENTER);
                        bp.setPadding(new Insets(10,10,10,10));
                        bp.setCenter(root);

                        if(!grid.badInput) {
                            primaryStage.setScene(borderPane);
                            primaryStage.show();
                        } else {
                            Alert warning = new Alert(Alert.AlertType.ERROR);
                            warning.setTitle("Error");
                            warning.setHeaderText("There was an error in the input!");
                            warning.showAndWait();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Error");
                        alert.setHeaderText("Error");
                        alert.setContentText("Wrong input!");
                        alert.showAndWait();
                    }
                }
            }
        });

        // Grid layout part
        start.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent eventMouse) {
                Grid grid = generateGrid((int)size.getValue());
                int n = grid.getN();
                Pane root = new Pane();
                BorderPane bp = new BorderPane();
                Scene borderPane = new Scene(bp);
                HBox bottom = new HBox();
                VBox left = new VBox();
                HBox top = new HBox();
                VBox right = new VBox();
                right.setSpacing(10);
                Button undo = new Button("Undo");
                Button redo = new Button("Redo");
                Button clear = new Button("Clear");
                CheckBox mistake = new CheckBox("Show mistakes");
                Button back = new Button("Back");
                mistake.setSelected(false);
                undo.setDisable(true);
                redo.setDisable(true);
                Label fontLabel = new Label("Font size :");
                ComboBox<String> fontSize = new ComboBox<String>();
                fontSize.getItems().addAll("Small", "Medium", "Large");
                Button hint = new Button("Hint");
                Button solve = new Button("Solve");

                right.getChildren().addAll(undo,redo,clear,mistake,fontLabel,fontSize,hint,solve,back);
                bp.setRight(right);
                bp.setLeft(left);
                bp.setBottom(bottom);
                bp.setTop(top);
                //appropriate disabling of the buttons
                back.setOnMousePressed(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        primaryStage.setScene(main);
                    }
                });
                bp.setOnMouseMoved(mouseEvent -> {
                    if(grid.redoStack.isEmpty()) redo.setDisable(true);
                    else redo.setDisable(false);
                    if(grid.undoStack.isEmpty()) undo.setDisable(true);
                    else undo.setDisable(false);
                });
                bottom.setFillHeight(true);

                //grid layer DO NOT TOUCH
                Canvas layer1 = new Canvas(n*75, n*75);
                GraphicsContext gc1 = layer1.getGraphicsContext2D();
                drawGrid(gc1, grid, n);

                //drawing layer
                Canvas layer2 = new Canvas(n*75, n*75);
                GraphicsContext gc2 = layer2.getGraphicsContext2D();
                refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                root.getChildren().addAll(layer1,layer2);

                bp.setOnMousePressed(mouseEvent -> {
                    if(grid.redoStack.isEmpty()) redo.setDisable(true);
                    else redo.setDisable(false);
                    if(grid.undoStack.isEmpty()) undo.setDisable(true);
                    else undo.setDisable(false);
                    refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);

                });
                root.setPadding(new Insets(10));

                layer1.toFront();
                layer1.addEventHandler(MouseEvent.MOUSE_CLICKED,
                        t -> {
                            int row = (int) Math.floor(t.getY() / 75);
                            int col = (int) Math.floor(t.getX() / 75);
                            grid.setSelectedCell(row, col);
                            refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                        });

                // adding buttons to the grid
                Button button1 = new Button("1");
                button1.setOnMousePressed(mouseEvent -> {
                    grid.getSelectedCell().setValue(1);
                    refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                });
                Button button2 = new Button("2");
                button2.setOnMousePressed(mouseEvent -> {
                    grid.getSelectedCell().setValue(2);
                    refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                });
                Button button3 = new Button("3");
                button3.setOnMousePressed(mouseEvent -> {
                    grid.getSelectedCell().setValue(3);
                    refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                });
                Button button4 = new Button("4");
                button4.setOnMousePressed(mouseEvent -> {
                    grid.getSelectedCell().setValue(4);
                    refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                });
                Button button5 = new Button("5");
                button5.setOnMousePressed(mouseEvent -> {
                    grid.getSelectedCell().setValue(5);
                    refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                });
                Button button6 = new Button("6");
                button6.setOnMousePressed(mouseEvent -> {
                    grid.getSelectedCell().setValue(6);
                    refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                });
                Button button7 = new Button("7");
                button7.setOnMousePressed(mouseEvent -> {
                    grid.getSelectedCell().setValue(7);
                    refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                });
                Button button8 = new Button("8");
                button8.setOnMousePressed(mouseEvent -> {
                    grid.getSelectedCell().setValue(8);
                    refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                });
                Button delete = new Button("delete");
                delete.setOnMousePressed(mouseEvent -> {
                    grid.getSelectedCell().setValue(0);
                    refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                });

                ArrayList<Button> buttons = new ArrayList<>();
                buttons.add(button1);
                buttons.add(button2);
                buttons.add(button3);
                buttons.add(button4);
                buttons.add(button5);
                buttons.add(button6);
                buttons.add(button7);
                buttons.add(button8);
                for(int i = 1; i <= n; i++) {
                    bottom.getChildren().add(buttons.get(i-1));
                }
                bottom.getChildren().add(delete);
                mistake.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {

                        refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                    }
                });
                fontSize.valueProperty().setValue("Medium");
                fontSize.valueProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                        refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                    }
                });

                // handle keys 3from the keyboard
                borderPane.setOnKeyPressed(keyEvent -> {
                    if(grid.redoStack.isEmpty()) redo.setDisable(true);
                    else redo.setDisable(false);
                    if(grid.undoStack.isEmpty()) undo.setDisable(true);
                    else undo.setDisable(false);
                    KeyCode code = keyEvent.getCode();
                    String character = code.getChar().charAt(0) + "";
                    if(code.isDigitKey()) {
                        if(Integer.parseInt(character) <= n) grid.getSelectedCell().setValue(Integer.parseInt(character));
                        refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                    }
                    if(code == KeyCode.BACK_SPACE) {
                        grid.getSelectedCell().setValue(0);
                        refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                    }
                });
                undo.setOnMousePressed(mouseEvent -> {
                    grid.undo();
                    if(grid.redoStack.isEmpty()) redo.setDisable(true);
                    else redo.setDisable(false);
                    if(grid.undoStack.isEmpty()) undo.setDisable(true);
                    else undo.setDisable(false);
                    refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(),fontSize);
                });
                redo.setOnMousePressed(mouseEvent -> {
                    grid.redo();
                    if (grid.redoStack.isEmpty()) redo.setDisable(true);
                    else redo.setDisable(false);
                    if (grid.undoStack.isEmpty()) undo.setDisable(true);
                    else undo.setDisable(false);
                    refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                });
                clear.setOnMousePressed(mouseEvent -> {
                    Alert clearAlert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to clear the board?");
                    Optional<ButtonType> result = clearAlert.showAndWait();
                    if(result.isPresent() && result.get() == ButtonType.OK) {
                        grid.clear();
                        if (grid.redoStack.isEmpty()) redo.setDisable(true);
                        else redo.setDisable(false);
                        if (grid.undoStack.isEmpty()) undo.setDisable(true);
                        else undo.setDisable(false);
                    }
                    refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                });


                solve.setOnMousePressed(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        for (int i = 0; i < n; i++) {
                            for (int j = 0; j < n; j++) {

                                grid.getCell(i, j).setValue(grid.getSolution()[i][j].getValue());
                                refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                            }
                        }
                    }
                });
                hint.setOnMousePressed(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        boolean finished = false;
                        if(grid.getSelectedCell().getValue() != grid.getSolution()[grid.getSelectedCell().getRow()][grid.getSelectedCell().getColumn()].getValue()) {
                            grid.getSelectedCell().setValue(grid.getSolution()[grid.getSelectedCell().getRow()][grid.getSelectedCell().getColumn()].getValue());
                        } else {
                            for (int i = 0; i < n; i++) {
                                if(finished) break;
                                for (int j = 0; j < n; j++) {
                                    if(grid.getCell(i, j).getValue() != grid.getSolution()[i][j].getValue()) {
                                        grid.getCell(i, j).setValue(grid.getSolution()[i][j].getValue());
                                        finished = true;
                                        break;
                                    }
                                }
                            }
                        }

                        refreshNumbers(gc2, grid, n, mistake.selectedProperty().get(), fontSize);
                    }
                });

                bottom.setSpacing(5);
                bottom.setAlignment(Pos.CENTER);
                bp.setPadding(new Insets(10,10,10,10));
                bp.setCenter(root);
                primaryStage.setScene(borderPane);
                primaryStage.show();
            }
        });
        menu.getChildren().addAll(loadFile, loadText, start, generate, size);
        primaryStage.setScene(main);
        primaryStage.show();


        //File file = new File("file.txt");
        //Grid grid = readFromFile(file);
    }


    /**
     * Draws the grid itself and the cages
     * LAYER 1
     */
    public void drawGrid(GraphicsContext gc, Grid grid, int n) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                gc.strokeRect(i * 75, j * 75, 75, 75);
            }
        }
        for (Cage cage : grid.cages) {
            gc.setLineWidth(5);

            // goes through cells and draws the borders of cages
            for (Cell cell : cage.cageCells) {
                int pos_Y = cell.getRow() * 75;
                int pos_X = cell.getColumn() * 75;
                int row = cell.getRow();
                int col = cell.getColumn();

                //checks if cell above exists
                if (row > 0) {
                    if (cell.getCage() != grid.getCell(row - 1, col).getCage()) {
                        gc.strokeLine(pos_X, pos_Y, pos_X + 75, pos_Y);
                    }
                }
                if(row == 0) gc.strokeLine(pos_X, pos_Y, pos_X + 75, pos_Y);
                // checks if below cell exists
                if (row < n - 1) {
                    if (cell.getCage() != grid.getCell(row + 1, col).getCage()) {
                        gc.strokeLine(pos_X, pos_Y + 75, pos_X + 75, pos_Y + 75);
                    }
                }
                if(row == n - 1) gc.strokeLine(pos_X, pos_Y + 75, pos_X + 75, pos_Y + 75);
                // checks if right cell exists
                if (col < n - 1) {
                    if (cell.getCage() != grid.getCell(row, col + 1).getCage()) {
                        gc.strokeLine(pos_X + 75, pos_Y, pos_X + 75, pos_Y + 75);
                    }
                }
                if(col == n - 1) gc.strokeLine(pos_X + 75, pos_Y, pos_X + 75, pos_Y + 75);
                // checks if left cell exists
                if (col > 0) {
                    if (cell.getCage() != grid.getCell(row, col - 1).getCage()) {
                        gc.strokeLine(pos_X, pos_Y, pos_X, pos_Y + 75);
                    }
                }
                if(col == 0) gc.strokeLine(pos_X, pos_Y, pos_X, pos_Y + 75);
                // write the solution and the sign
            }
            String sign = Integer.valueOf(cage.solution).toString();
            if(cage.cageCells.size() > 1) {
                if(cage.sign == Cage.operator.DIVISION) sign += "/";
                if(cage.sign == Cage.operator.MULTIPLICATION) sign += "x";
                if(cage.sign == Cage.operator.ADDITION) sign += "+";
                if(cage.sign == Cage.operator.SUBTRACTION) sign += "-";
            }
            //gc.setFont(new Font(4));
            //gc.setFont(new Font());
            gc.setLineWidth(1);
            gc.strokeText(sign, cage.cageCells.get(0).getColumn() * 75 + 5, cage.cageCells.get(0).getRow() * 75 + 15);
        }
    }
    /**
     * draws the numbers written by the player
     * LAYER 2
     */
    public void refreshNumbers(GraphicsContext gc, Grid grid, int n, boolean checked, ComboBox<String> fontSize) {


        gc.clearRect(0,0,gc.getCanvas().getWidth(),gc.getCanvas().getHeight());

        if(checked) {
            boolean wrong = false;
            //MISTAKE DETECTION
            for (Cage cage : grid.cages) {
                if (!cage.isSolved()) {
                    wrong = true;
                    for (Cell cell : cage.cageCells) {
                        if (cell.getValue() == 0) {
                            wrong = false;
                            break;
                        }
                    }
                }
                if (wrong) {
                    for (Cell cell : cage.cageCells) {
                        gc.setFill(Color.web("0xff4d4d"));
                        gc.fillRect(cell.getColumn() * 75 + 1, cell.getRow() * 75 + 1, 73, 73);
                    }
                }
                wrong = false;
            }

            ArrayList<Integer> numbersInRow = new ArrayList<>();

            //goes through all rows
            for (int i = 0; i < grid.getN(); i++) {

                // adds the values of cells from the row
                for (int j = 0; j < grid.getN(); j++) {
                    numbersInRow.add(grid.playerCells[i][j].getValue());
                }

                //checks if there are any numbers that appear twice
                for (int number = 1; number <= grid.getN(); number++) {
                    if (numbersInRow.indexOf(number) != numbersInRow.lastIndexOf(number)) {
                        wrong = true;
                    }
                }

                //color all cells red
                if (wrong) {
                    for (int rowEntry = 0; rowEntry < grid.getN(); rowEntry++) {
                        Cell currentCell = grid.getCell(i, rowEntry);
                        gc.setFill(Color.web("0xff4d4d"));
                        gc.fillRect(currentCell.getColumn() * 75 + 1, currentCell.getRow() * 75 + 1, 73, 73);
                    }
                }
                wrong = false;
                numbersInRow.clear();
            }

            // goes through all columns and checks the values
            for (int k = 0; k < grid.getN(); k++) {

                // adds the values of cells from the column
                for (int l = 0; l < grid.getN(); l++) {
                    numbersInRow.add(grid.playerCells[l][k].getValue());
                }

                //checks if there are any numbers that appear twice
                for (int num = 1; num <= grid.getN(); num++) {
                    if (numbersInRow.indexOf(num) != numbersInRow.lastIndexOf(num)) {
                        wrong = true;
                    }
                }
                // color all cells red
                if (wrong) {
                    for (int colEntry = 0; colEntry < grid.getN(); colEntry++) {
                        Cell currentCell = grid.getCell(colEntry, k);
                        gc.setFill(Color.web("0xff4d4d"));
                        gc.fillRect(currentCell.getColumn() * 75 + 1, currentCell.getRow() * 75 + 1, 73, 73);
                    }
                }
                wrong = false;
                numbersInRow.clear();
            }
        }
            int selectedRow = grid.getSelectedCell().getRow();
            int selectedCol = grid.getSelectedCell().getColumn();
            gc.setFill(Color.web("0xd1d1e0"));
            gc.setLineWidth(3);
            gc.fillRect(selectedCol * 75 + 1, selectedRow * 75 + 1, 74, 74);
            gc.setLineWidth(1);

        /**
         * FONT SIZE CHANGE
         */

        gc.setFont(new Font(40));
        try {
            if (fontSize.getValue().equals("Small")) gc.setFont(new Font(35));
            if (fontSize.getValue().equals("Large")) gc.setFont(new Font(45));
        } catch (Exception ignored) {
        }
            gc.setStroke(Color.BLACK);
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {

                    int pos_x = j * 75 + 25;
                    int pos_y = i * 75 + 50;
                    if (grid.getCell(i, j).getValue() != 0) {
                        gc.strokeText(Integer.valueOf(grid.getCell(i, j).getValue()).toString(), pos_x, pos_y);
                    }
                }
            }

        if(grid.checkVictory()) {
            Random random = new Random();

            Timeline timeline = new Timeline(new KeyFrame(Duration.millis(51), actionEvent -> {
                for(int k = 0; k < n; k++) {
                    for(int j = 0; j < n; j ++) {
                        float r = random.nextFloat();
                        float g = random.nextFloat();
                        float b = random.nextFloat();
                        Color color = new Color(r, g, b, 0.7);
                        gc.setFill(color);
                        int pos_x = k * 75;
                        int pos_y = j * 75;
                        gc.fillRect(pos_x, pos_y, 75, 75);
                    }
                }
            }));
            timeline.setCycleCount(100);
            timeline.play();
            Alert victory = new Alert(Alert.AlertType.INFORMATION, "Congratulations!!!\nYou WIN!");
            victory.setHeaderText("Puzzle solved!");
            victory.setTitle("");
            Optional<ButtonType> buttonType = victory.showAndWait();
            Timeline endTimes = new Timeline(new KeyFrame(Duration.millis(50), actionEvent -> {
                refreshNumbers(gc, grid, n);
            }));
            endTimes.setCycleCount(103);
            endTimes.play();
        }
    }

    public void refreshNumbers(GraphicsContext gc, Grid grid, int n) {


        gc.clearRect(0,0,gc.getCanvas().getWidth(),gc.getCanvas().getHeight());

        int selectedRow = grid.getSelectedCell().getRow();
        int selectedCol = grid.getSelectedCell().getColumn();
        gc.setFill(Color.web("0xd1d1e0"));
        gc.setLineWidth(3);
        gc.fillRect(selectedCol * 75 + 1, selectedRow * 75 + 1, 74, 74);
        gc.setLineWidth(1);
        gc.setFont(new Font(40));
        gc.setStroke(Color.BLACK);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {

                int pos_x = j * 75 + 25;
                int pos_y = i * 75 + 50;
                if (grid.getCell(i, j).getValue() != 0) {
                    gc.strokeText(Integer.valueOf(grid.getCell(i, j).getValue()).toString(), pos_x, pos_y);
                }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     *for every new element 2 array lists:
     * 1 for the numbers in the column and
     * 1 for the numbers already in the row,
     * 1 for loop to go through all elements in the column and
     * 1 for loop to go through the row and add all elements to the arraylist
     * and a random number from 1 to n
     */
    public static Cell[][] generateSolution(int n) {
        Cell[][] solution = new Cell[n][n];
        ArrayList<Integer> possibleNumbers = new ArrayList<>();
        Random rand = new Random();

        solution[0][0] = new Cell(0,0,0);
        // goes through all rows
        for(int row = 0; row < n; row++) {

            // goes through all the numbers in the row
            for(int col = 0; col < n; col++) {

                //refills the possibleNumbers
                for(int number = 1; number <= n; number++) {
                    possibleNumbers.add(number);
                }

                // goes through all the elements in the above rows
                for(int i = 0; i < row; i++) {
                    if(possibleNumbers.contains(solution[i][col].getValue())) {
                        possibleNumbers.remove(Integer.valueOf(solution[i][col].getValue()));
                    }
                }

                // goes through all the elements in the left columns
                // removes all elements that exist form the possibilities
                for(int j = 0; j < col; j++) {
                    if(possibleNumbers.contains(solution[row][j].getValue())) {
                        possibleNumbers.remove(Integer.valueOf(solution[row][j].getValue()));
                    }
                }

                // the value of the cell is a random value from the possibilities
                if(possibleNumbers.size() == 0) {
                   row--;
                   break;
                }
                int random = rand.nextInt(possibleNumbers.size());

                solution[row][col] = new Cell(row,col,possibleNumbers.get(random));
                possibleNumbers.clear();
            }
        }
        return solution;
    }

    public static ArrayList<ArrayList<Integer>> generateCageCoordinates(int n){

        //all of the coordinates
        ArrayList<Integer> coordinates= new ArrayList<>();
        Random rand = new Random();
        int randomValue;
        ArrayList<ArrayList<Integer>> cageList = new ArrayList<>();
        int currentCoordinate;

        for(int number = 1; number <= n * n; number++) {
            coordinates.add(number);
        }

        int arListPointer = 0;

        // goes through all the coordinates
        while(!coordinates.isEmpty()) {
            currentCoordinate = coordinates.get(0);
            int bottom;
            int right;
            cageList.add(new ArrayList<>());

                // goes to complete a cage
                for(int z = 1; z < 5; z++) {
                    bottom = currentCoordinate + n;
                    right = currentCoordinate + 1;
                    randomValue = rand.nextInt(100 * z);
                    //if there exists a bottom value and a right value choose one at random
                    if(coordinates.contains(bottom) && coordinates.contains(right) && currentCoordinate % n != 0) {
                        if(randomValue <= 10) {

                            // add the values to the first cage
                            cageList.get(arListPointer).add(currentCoordinate);

                            // remove the coordinates from the array list and set current coordinates to right or bottom
                            coordinates.remove(Integer.valueOf(currentCoordinate));
                            if(randomValue <= 5) {

                                cageList.get(arListPointer).add(bottom);
                                coordinates.remove(Integer.valueOf(bottom));
                                currentCoordinate = right;
                            } else {

                                cageList.get(arListPointer).add(right);
                                coordinates.remove(Integer.valueOf(right));
                                currentCoordinate = bottom;
                            }
                            continue;
                        }

                        if(randomValue <= 55) {

                            cageList.get(arListPointer).add(currentCoordinate);
                            coordinates.remove(Integer.valueOf(currentCoordinate));
                            currentCoordinate = bottom;
                            continue;

                        }

                        if(randomValue <= 100) {

                            cageList.get(arListPointer).add(currentCoordinate);
                            coordinates.remove(Integer.valueOf(currentCoordinate));
                            currentCoordinate = right;
                            continue;

                        }
                        cageList.get(arListPointer).add(currentCoordinate);
                        coordinates.remove(Integer.valueOf(currentCoordinate));
                        break;
                    }

                    // case on the bottom of the grid
                    if(!coordinates.contains(bottom) && currentCoordinate % n != 0 && coordinates.contains(right)) {

                        if(randomValue <= 75) {
                            cageList.get(arListPointer).add(currentCoordinate);
                            coordinates.remove(Integer.valueOf(currentCoordinate));
                            currentCoordinate = right;
                            continue;
                        } else {

                            cageList.get(arListPointer).add(currentCoordinate);
                            coordinates.remove(Integer.valueOf(currentCoordinate));
                            break;
                        }
                    }

                    // case on the right side of the grid
                    if(currentCoordinate % n == 0 && coordinates.contains(bottom)) {

                        cageList.get(arListPointer).add(currentCoordinate);
                        coordinates.remove(Integer.valueOf(currentCoordinate));
                        if(randomValue <= 100) {
                            currentCoordinate = bottom;
                            continue;
                        } else {
                            break;
                        }
                    }

                    if(coordinates.contains(bottom) && !coordinates.contains(right)) {
                        cageList.get(arListPointer).add(currentCoordinate);
                        coordinates.remove(Integer.valueOf(currentCoordinate));
                        currentCoordinate = bottom;
                        continue;
                    }
                    if(!coordinates.contains(bottom) && coordinates.contains(right) && currentCoordinate % n != 0) {
                        cageList.get(arListPointer).add(currentCoordinate);
                        coordinates.remove(Integer.valueOf(currentCoordinate));
                        currentCoordinate = right;
                        continue;
                    }
                    // case where it is an only cell
                    if(!coordinates.contains(bottom) && !coordinates.contains(right)) {
                        cageList.get(arListPointer).add(currentCoordinate);
                        coordinates.remove(Integer.valueOf(currentCoordinate));
                        break;
                    }
                }
            arListPointer++;
        }
        return cageList;
    }

    public static ArrayList<Cage> generateCages(Cell[][] cells, ArrayList<ArrayList<Integer>> cagesCoordinates, int n) {

        ArrayList<Cage> cages = new ArrayList<>();
        Random rand = new Random();


        for(ArrayList<Integer> cageCoordinates : cagesCoordinates) {
            int randValue;
            int division = 1;
            int multiplication = 1;
            int addition = 0;
            int subtraction = 0;
            int max = 0;
            randValue = rand.nextInt(3);
            for (Integer number : cageCoordinates) {
                // value of the cell
                int cellValue = cells[(number - 1) / n][(number - 1) % n].getValue();
                if (max < cellValue) max = cellValue;

               division = division * cellValue;
               multiplication = multiplication * cellValue;
               addition = addition + cellValue;
               subtraction = subtraction + cellValue;
            }
            if(randValue % 2 == 0 && (max*max) % division == 0) {
                cages.add(new Cage((max*max) / division, Cage.operator.DIVISION, cageCoordinates, n));
                continue;
            }
            if(randValue == 0 && (max*2)-subtraction >= 0) {
                cages.add(new Cage((max*2)-subtraction, Cage.operator.SUBTRACTION, cageCoordinates, n));
                continue;
            }
            if(multiplication > 400 && randValue == 2) randValue = 1;
            if(randValue == 1 || randValue == 0) {
                cages.add(new Cage(addition, Cage.operator.ADDITION, cageCoordinates, n));
                continue;
            }
            if(randValue == 2) {
                cages.add(new Cage(multiplication, Cage.operator.MULTIPLICATION, cageCoordinates, n));
            }
        }
        return cages;
    }

    public static Grid generateGrid(int n) {
        Grid newGrid = new Grid(n);
        Cell[][] cells = generateSolution(n);
        newGrid.setSolution(cells);
        ArrayList<ArrayList<Integer>> cagesList = generateCageCoordinates(n);
        ArrayList<Cage> cagesForRealThisTime = generateCages(cells, cagesList, n);
        newGrid.cages = cagesForRealThisTime;

        for(Cage cage : cagesForRealThisTime) {
            cage.setGrid(newGrid);
            for(Cell cell : cage.cageCells) {
                cell.setCage(cage);
                cell.setGrid(newGrid);
            }
        }
        for (int row = 0; row < n; row++) {
            for(int col = 0; col < n; col++) {
                newGrid.getCell(row,col).setValue(cells[row][col].getValue());
            }
        }

        // remove the values from the grid
        for(int k = 0; k < n; k++) {

            for(int l = 0; l < n; l++) {
                newGrid.getCell(k,l).setValue(0);
                newGrid.getCell(k,l).setGrid(newGrid);
            }

        }
        newGrid.clearStacks();
        return newGrid;
    }

    public static Grid readFromFile(File file) throws IOException {

        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String[] line;
        String[] numbers;
        ArrayList<Integer> coordinates = new ArrayList<>();
        ArrayList<Cage> cages = new ArrayList<>();
        Cage cage;
        Cage.operator sign = Cage.operator.DIVISION;
        int solution;
        int n;
        int max = 0;
        int currentNumber;
        boolean wrongInput = false;
        ArrayList<Integer> allCoords = new ArrayList<>();
        // finds n
        while(bufferedReader.ready()) {
            line = bufferedReader.readLine().split(" ");
            numbers = line[1].split(",");
            for (String coord: numbers) {
                currentNumber = Integer.parseInt(coord);
                if(allCoords.contains(currentNumber)) wrongInput = true;
                if(currentNumber > max) max = currentNumber;
                allCoords.add(currentNumber);
            }
        }
        bufferedReader.close();
        fileReader.close();
        fileReader = new FileReader(file);
        bufferedReader = new BufferedReader(fileReader);
        n = (int) Math.sqrt(max);
        Grid grid = new Grid(n);
        while(bufferedReader.ready()) {
            line = bufferedReader.readLine().split(" ");
            numbers = line[1].split(",");
            coordinates.clear();
            for (String coord: numbers) {
                currentNumber = Integer.parseInt(coord);
                coordinates.add(currentNumber);
            }
            if (line[0].endsWith("÷")) sign = Cage.operator.DIVISION;
            if (line[0].endsWith("x")) sign = Cage.operator.MULTIPLICATION;
            if (line[0].endsWith("+")) sign = Cage.operator.ADDITION;
            if (line[0].endsWith("-")) sign = Cage.operator.SUBTRACTION;
            if(line[0].length() != 1) solution = Integer.parseInt(line[0].substring(0,line[0].length() - 1));
                else solution = Integer.parseInt(line[0].charAt(0) + "");
            cage = new Cage(solution, sign, coordinates, n);
            cages.add(cage);
            cage.setGrid(grid);
            for(Cell cell: cage.cageCells) {
                cell.setGrid(grid);
            }

            Collections.sort(coordinates);
            for(int i = 0; i < coordinates.size(); i++) {
                boolean itsOk = false;

                for(Integer coordinate : coordinates) {
                    if (coordinates.get(i) - coordinate == n || coordinates.get(i) - coordinate == 1 ||
                            coordinate - coordinates.get(i) == n || coordinate - coordinates.get(i) == 1) {
                        itsOk = true;
                        break;
                    }
                    if(coordinates.size() == 1) itsOk = true;
                }
                if(!itsOk) wrongInput = true;
            }

        }
        bufferedReader.close();
        grid.badInput = wrongInput;
        grid.cages = cages;
        return grid;
    }
}
