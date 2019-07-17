import java.awt.*;
import java.util.*;
import javalib.impworld.*;
import javalib.worldimages.*;
import tester.*;

// FloodItWorld ===============================================================
class FloodItWorld extends World {
  // a row of cells
  ArrayList<Cell> row;
  // rows of rows of cells aka all of the cells in the game
  ArrayList<ArrayList<Cell>> board;

  // defines constant board size
  // board will always be a square with the defined constant as the size
  static final int BOARD_SIZE = 10;
  // how many pixels wide a cell is
  static final int CELL_LENGTH = 50;
  // initial clicks
  int clickCount = 0;
  // max moves allowed in gameplay
  int maxMoves = BOARD_SIZE * 2;
  // the start diagonal for the flood
  int diagonal = 0;
  // is the game currently flooding?
  boolean isFlooding = false;
  // initial start time
  double start_time = System.currentTimeMillis();
  // generates a random color Palette for game play
  int palette_num = 0;
  ChoosePalette colors = this.randPalette();

  // FloodItWorld constructor for testing
  public FloodItWorld(ArrayList<ArrayList<Cell>> testArray) {
    this.board = testArray;
  }

  // creates the initial FloodItWorld according to board size
  public FloodItWorld() {
    this.board = initBoard();
    System.out.println(board.get(0).get(0).color);
  }

  // creates the initial board
  public ArrayList<ArrayList<Cell>> initBoard() {
    ArrayList<ArrayList<Cell>> grid = new ArrayList<>();
    int boardSize = FloodItWorld.BOARD_SIZE;

    // builds grid
    for (int j = 0; j < boardSize; j++) {
      ArrayList<Cell> rowResult = new ArrayList<>();
      for (int i = 0; i < boardSize; i++) {
        // can play with different palettes 
        rowResult.add(new Cell(i, j, false, colors.getColor()));
      }
      grid.add(rowResult);
    }

    // assigns neighbors
    for (int j = 0; j < boardSize; j++) {
      for (int i = 0; i < boardSize; i++) {
        // if the cell is on the top edge
        if (j != 0) {
          grid.get(j).get(i).top = grid.get(j - 1).get(i);
        }
        if (i != 0) {
          grid.get(j).get(i).left = grid.get(j).get(i - 1);
        }
        if (i != grid.size() - 1) {
          grid.get(j).get(i).right = grid.get(j).get(i + 1);
        }
        if (j != grid.size() - 1) {
          grid.get(j).get(i).bottom = grid.get(j + 1).get(i);
        }
      }
    }

    // if any cells bordering the top left cell are also the same color make them
    // flooded
    if (grid.get(0).get(1).color.equals(grid.get(0).get(0).color)) {
      grid.get(0).get(1).flooded = true;
    }
    if (grid.get(1).get(0).color.equals(grid.get(0).get(0).color)) {
      grid.get(1).get(0).flooded = true;
    }

    grid.get(0).get(0).flooded = true;

    return grid;
  }

  // onKey --> reset board
  public void onKeyEvent(String key) {
    if (key.equals("r")) {
      if (palette_num == 2) {
        palette_num = 0;
      }
      else {
        palette_num = palette_num + 1;
      }
      colors = this.randPalette();
      this.board = this.initBoard();
      clickCount = 0;
    }
  }

  // on mouse --> on click
  // - floods screen if
  // - the color clicked is different from what the top left corner already is and
  // - there are adjacent cells of that clicked color
  // - does nothing if the color clicked is the same as what the flooded area
  // already is
  public void onMouseClicked(Posn click) {
    // if we click inside the flood board
    if ((click.x <= FloodItWorld.BOARD_SIZE * FloodItWorld.CELL_LENGTH)
        && (click.y <= FloodItWorld.BOARD_SIZE * FloodItWorld.CELL_LENGTH)) {
      this.isFlooding = true;
      this.diagonal = 0;
      if (clickCount == 0) {
        start_time = System.currentTimeMillis();
      }

      // translate the click posn into a game index
      int gridXIndex = click.x / FloodItWorld.CELL_LENGTH;
      int gridYIndex = click.y / FloodItWorld.CELL_LENGTH;
      // retreive the color of the posn clicked
      String newColor = board.get(gridYIndex).get(gridXIndex).color;

      // at the beginning of the game, if either adjacent is the same color as the top
      // left
      // change flooded to true
      if (clickCount == 0) {
        this.changeAdjTrue();
      }

      this.changeCellOne(newColor);
      this.changeAdjTrue();
      clickCount++;
    }
  }

  // EFFECT : changes the color of the top left cell to be the color of the
  // clicked one
  // this just changes the first square of the square
  public void changeCellOne(String clickColor) {
    board.get(0).get(0).color = clickColor;
  }

  // EFFECT : change flooded to true if the color matches the clicked color and
  // any of its adjacents are true
  public void changeAdjTrue() {
    Cell topLeft = board.get(0).get(0);

    for (int j = 0; j < board.size(); j++) {
      for (int i = 0; i < board.size(); i++) {
        if (this.board.get(j).get(i).color.equals(topLeft.color)
            && this.board.get(j).get(i).anyAdjTrue()) {
          this.board.get(j).get(i).flooded = true;
        }
      }
    }
  }

  // draws FloodItWorld
  public WorldScene makeScene() {
    // draws out the game board
    WorldScene scene = new WorldScene(BOARD_SIZE * 10, BOARD_SIZE * 10);
    for (ArrayList<Cell> row : this.board) {
      for (Cell cell : row) {
        scene.placeImageXY(cell.drawCell(),
            // each cell is CELL_LENGTH long, /2 for middle of the cell
            cell.x * CELL_LENGTH + CELL_LENGTH / 2, cell.y * CELL_LENGTH + CELL_LENGTH / 2);
      }
    }

    // places moves text, moves counter, time text, and timer
    scene.placeImageXY(movesText(), (BOARD_SIZE * CELL_LENGTH + 100), 50);
    scene.placeImageXY(drawMoveCounter(), (BOARD_SIZE * CELL_LENGTH + 100), 100);
    scene.placeImageXY(timeText(), 50, (BOARD_SIZE * CELL_LENGTH + 50));
    scene.placeImageXY(timeNums(), 125, (BOARD_SIZE * CELL_LENGTH + 50));
    scene.placeImageXY(resetText(), 115, (BOARD_SIZE * CELL_LENGTH + 100));

    // if you win on the last move
    if (this.allFlooded() && clickCount == maxMoves) {
      scene.placeImageXY(win(), (BOARD_SIZE * CELL_LENGTH + 200) / 2,
          (BOARD_SIZE * CELL_LENGTH + 200) / 2);
    }
    // if you flood the entire board aka win
    else if (this.allFlooded() && clickCount < maxMoves) {
      scene.placeImageXY(win(), (BOARD_SIZE * CELL_LENGTH + 200) / 2,
          (BOARD_SIZE * CELL_LENGTH + 200) / 2);
    }
    // if you max out moves aka lose
    else if (!this.allFlooded() && clickCount >= maxMoves) {
      scene.placeImageXY(lose(), (BOARD_SIZE * CELL_LENGTH + 200) / 2,
          (BOARD_SIZE * CELL_LENGTH + 200) / 2);
    }
    return scene;
  }

  // updates and draws how many moves have been used and how many there are
  public WorldImage drawMoveCounter() {
    return new TextImage(Integer.toString(clickCount) + "/" + Integer.toString(maxMoves), 24,
        Color.BLACK);
  }

  // "moves" text
  public WorldImage movesText() {
    return new TextImage("moves", 24, Color.BLACK);
  }

  // "time" text
  public WorldImage timeText() {
    return new TextImage("time: ", 24, Color.BLACK);
  }

  // "reset" text
  public WorldImage resetText() {
    return new TextImage("press 'r' to reset", 24, Color.BLACK);
  }

  // draw the counting numbers
  public WorldImage timeNums() {

    // start timer at the player's first click
    if (clickCount > 0) {
      long current = System.currentTimeMillis();
      int int_seconds = (int) (current - start_time) / 1000;
      int int_minutes = (int) int_seconds / 60;
      int_seconds = int_seconds - int_minutes * 60;
      String str_minutes = "";
      String str_seconds = "";

      // formats the timer
      if (int_minutes < 10) {
        str_minutes = "0" + Integer.toString(int_minutes);
      }
      else {
        str_minutes = Integer.toString(int_minutes);
      }
      if (int_seconds < 10) {
        str_seconds = "0" + Integer.toString(int_seconds);
      }
      else {
        str_seconds = Integer.toString(int_seconds);
      }

      // strings timer together
      return new TextImage(str_minutes + ":" + str_seconds + "", 24, Color.black);
    }
    else {
      return new TextImage("00:00", 24, Color.black);
    }
  }

  // the lose screen
  public WorldImage lose() {
    return new OverlayImage(
        new AboveImage(new TextImage("you lose :(", 24, Color.RED), resetText()),
        new RectangleImage(BOARD_SIZE * CELL_LENGTH + 200, BOARD_SIZE * CELL_LENGTH + 200,
            OutlineMode.SOLID, Color.WHITE));
  }

  // the win screen
  public WorldImage win() {
    return new OverlayImage(new AboveImage(new TextImage("you win!", 24, Color.GREEN), resetText()),
        new RectangleImage(BOARD_SIZE * CELL_LENGTH + 200, BOARD_SIZE * CELL_LENGTH + 200,
            OutlineMode.SOLID, Color.WHITE));
  }

  // on tick
  public void onTick() {
    String clickColor = board.get(0).get(0).color;

    // if the board flood is true, start flood in a diagonal
    if (this.isFlooding) {
      this.flood(diagonal, clickColor);
      diagonal++;
    }
  }

  // floods one diagonal per click
  public void flood(int diagonal, String clickColor) {
    for (int j = 0; j < FloodItWorld.BOARD_SIZE; j++) {
      for (int i = 0; i < FloodItWorld.BOARD_SIZE; i++) {
        if (diagonal == (j + i) && board.get(j).get(i).flooded) {
          board.get(j).get(i).color = clickColor;
        }
      }
    }
  }

  // are all of the cells in the game flooded?
  public boolean allFlooded() {
    for (int j = 0; j < FloodItWorld.BOARD_SIZE - 1; j++) {
      for (int i = 0; i < FloodItWorld.BOARD_SIZE - 1; i++) {
        if (board.get(j).get(i).color != board.get(j + 1).get(i + 1).color) {
          return false;
        }
      }
    }
    return true;
  }

  // random palette generator
  public ChoosePalette randPalette() {
    int c = new Random().nextInt(3);
    if (c == 0) {
      return new RainbowColor();
    }
    else if (c == 1) {
      return new CamoColors();
    }
    else {
      return new Blues();
    }
  }
}

//Represents a single square of the game area ===========================================
class Cell {
  // In logical coordinates, with the origin at the top-left corner of the screen
  int x;
  int y;
  String color;
  boolean flooded;

  // the four adjacent cells to this one
  Cell left;
  Cell top;
  Cell right;
  Cell bottom;

  Cell(int x, int y, String color, boolean flooded) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.flooded = flooded;
  }

  // random generator
  Random randColor = new Random();

  // cell constructor with no parameters
  public Cell() {
  }

  // cell constructor to run big bang
  public Cell(int x, int y, boolean flooded, String color) {
    this.x = x;
    this.y = y;
    this.flooded = false;
    this.color = color;
  }

  // cell constructor for testing
  public Cell(int x, int y, boolean flooded, Random seed) {
    this.x = x;
    this.y = y;
    // CHANGE THIS
    this.flooded = true;
    this.randColor = seed;
  }

  // string to Color
  public Color string2Color(String s) {
    if (s.equals("red")) {
      return Color.RED;
    }
    else if (s.equals("blue")) {
      return Color.BLUE;
    }
    else if (s.equals("green")) {
      return Color.GREEN;
    }
    else if (s.equals("purple")) {
      return Color.MAGENTA;
    }
    else if (s.equals("orange")) {
      return Color.ORANGE;
    }
    else if (s.equals("yellow")) {
      return Color.YELLOW;
    }
    else if (s.equals("dark green")) {
      return new Color(54, 121, 38);
    }
    else if (s.equals("light green")) {
      return new Color(106, 236, 74);
    }
    else if (s.equals("light brown")) {
      return new Color(197, 158, 101);
    }
    else if (s.equals("black")) {
      return new Color(0, 0, 0);
    }
    else if (s.equals("dark brown")) {
      return new Color(113, 91, 60);
    }
    else if (s.equals("lightest blue")) {
      return new Color(181, 244, 255);
    }
    else if (s.equals("tiffany blue")) {
      return new Color(0, 238, 220);
    }
    else if (s.equals("navy")) {
      return new Color(10, 55, 123);
    }
    else if (s.equals("teal")) {
      return new Color(40, 155, 166);
    }
    else {
      return new Color(101, 137, 227);
    }
  }

  // draws each individual cell
  public WorldImage drawCell() {
    return new RectangleImage(FloodItWorld.CELL_LENGTH, FloodItWorld.CELL_LENGTH, OutlineMode.SOLID,
        this.string2Color(this.color));
  }

  // are any of the adjacents true?
  public boolean anyAdjTrue() {
    return ((this.left != null) && this.left.flooded) || ((this.top != null) && this.top.flooded)
        || ((this.right != null) && this.right.flooded)
        || ((this.bottom != null) && this.bottom.flooded);
  }
}

// represents the different color palettes ==============================================
interface ChoosePalette {
  String getColor();
}

class RainbowColor implements ChoosePalette {
  public String getColor() {
    int c = new Random().nextInt(6);
    if (c == 0) {
      return "red";
    }
    else if (c == 1) {
      return "blue";
    }
    else if (c == 2) {
      return "green";
    }
    else if (c == 3) {
      return "purple";
    }
    else if (c == 4) {
      return "orange";
    }
    else {
      return "yellow";
    }
  }
}

// represents the camo color palette
class CamoColors implements ChoosePalette {
  public String getColor() {
    int c = new Random().nextInt(6);
    if (c == 0) {
      return "dark green";
    }
    else if (c == 1) {
      return "green";
    }
    else if (c == 2) {
      return "light green";
    }
    else if (c == 3) {
      return "light brown";
    }
    else if (c == 4) {
      return "black";
    }
    else {
      return "dark brown";
    }
  }
}

// represents the blue color Palette
class Blues implements ChoosePalette {
  public String getColor() {
    int c = new Random().nextInt(6);
    if (c == 0) {
      return "lightest blue";
    }
    else if (c == 1) {
      return "tiffany blue";
    }
    else if (c == 2) {
      return "navy";
    }
    else if (c == 3) {
      return "blue";
    }
    else if (c == 4) {
      return "teal";
    }
    else {
      return "periwinkle";
    }
  }
}

// tests and examples for FloodIt =======================================================
class FloodExamples {

  private static final int BOARD_SIZE = 10;
  private static final int CELL_LENGTH = 50;
  FloodItWorld testFlood = new FloodItWorld();
  ArrayList<ArrayList<Cell>> board;
  ArrayList<Cell> row;

  Cell testCell;
  Cell greenCell;
  Cell blueCell;
  Cell redCell;
  Cell orangeCell;
  Cell purpleCell;
  Cell yellowCell;

  WorldImage drawRed;
  WorldImage drawOrange;
  WorldImage drawYellow;
  WorldImage drawPurple;

  Cell randSeed1;
  Cell randSeed2;
  Cell randSeed3;
  Cell randSeed4;
  Cell randSeed5;
  Cell randSeed6;

  ArrayList<Cell> testRow1;
  ArrayList<Cell> testRow2;
  ArrayList<Cell> testRow3;
  ArrayList<Cell> testRow4;
  ArrayList<Cell> testRow5;
  ArrayList<Cell> testRow6;
  ArrayList<Cell> testRow7;
  ArrayList<Cell> testRow8;
  ArrayList<Cell> testRow9;
  ArrayList<Cell> testRow10;
  ArrayList<Cell> testRow11;
  ArrayList<Cell> testRow12;
  ArrayList<Cell> testRow13;
  ArrayList<Cell> testRow14;

  ArrayList<ArrayList<Cell>> ypor;
  ArrayList<ArrayList<Cell>> testArray;
  ArrayList<ArrayList<Cell>> testArray2;
  ArrayList<ArrayList<Cell>> testArray3;
  ArrayList<ArrayList<Cell>> testArray4;
  ArrayList<ArrayList<Cell>> prrr;
  ArrayList<ArrayList<Cell>> rrrr;
  ArrayList<ArrayList<Cell>> all10Fl;

  FloodItWorld testFlood2;
  FloodItWorld testFlood3;
  FloodItWorld testFlood4;
  FloodItWorld testFlood5;
  FloodItWorld testFlood6;
  FloodItWorld testFlood10;

  ChoosePalette blues;
  ChoosePalette camoColors;
  ChoosePalette rainbowColors;

  void reset() {
    this.board = new ArrayList<ArrayList<Cell>>();

    this.testCell = new Cell(10, 10, "", true);
    this.greenCell = new Cell(10, 10, "green", true);
    this.blueCell = new Cell(10, 10, "blue", false);
    this.redCell = new Cell(10, 10, "red", true);
    this.orangeCell = new Cell(10, 10, "orange", true);
    this.purpleCell = new Cell(10, 10, "purple", true);
    this.yellowCell = new Cell(10, 10, "yellow", true);

    this.drawRed = new RectangleImage(FloodItWorld.CELL_LENGTH, FloodItWorld.CELL_LENGTH,
        OutlineMode.SOLID, Color.RED);
    this.drawOrange = new RectangleImage(FloodItWorld.CELL_LENGTH, FloodItWorld.CELL_LENGTH,
        OutlineMode.SOLID, Color.ORANGE);
    this.drawYellow = new RectangleImage(FloodItWorld.CELL_LENGTH, FloodItWorld.CELL_LENGTH,
        OutlineMode.SOLID, Color.YELLOW);
    this.drawPurple = new RectangleImage(FloodItWorld.CELL_LENGTH, FloodItWorld.CELL_LENGTH,
        OutlineMode.SOLID, Color.MAGENTA);

    this.testRow1 = new ArrayList<Cell>();
    this.testRow2 = new ArrayList<Cell>();
    testRow1.add(redCell);
    testRow1.add(orangeCell);
    testRow2.add(purpleCell);
    testRow2.add(yellowCell);
    this.testArray = new ArrayList<ArrayList<Cell>>();
    testArray.add(testRow1);
    testArray.add(testRow2);

    this.testRow3 = new ArrayList<Cell>();
    this.testRow4 = new ArrayList<Cell>();
    testRow3.add(redCell);
    testRow3.add(blueCell);
    testRow4.add(greenCell);
    testRow4.add(yellowCell);
    this.testArray2 = new ArrayList<ArrayList<Cell>>();
    testArray2.add(testRow3);
    testArray2.add(testRow4);

    this.testFlood2 = new FloodItWorld(testArray);
    this.testFlood3 = new FloodItWorld(testArray2);
    this.testFlood4 = new FloodItWorld(testArray3);
    this.testFlood5 = new FloodItWorld(testArray4);

    this.testRow5 = new ArrayList<Cell>();
    this.testRow6 = new ArrayList<Cell>();
    testRow5.add(greenCell);
    testRow5.add(greenCell);
    testRow6.add(greenCell);
    testRow6.add(greenCell);

    this.testArray3 = new ArrayList<ArrayList<Cell>>();
    testArray3.add(testRow5);
    testArray3.add(testRow6);

    this.testRow7 = new ArrayList<Cell>();
    this.testRow8 = new ArrayList<Cell>();
    testRow7.add(yellowCell);
    testRow7.add(yellowCell);
    testRow8.add(yellowCell);
    testRow8.add(yellowCell);

    this.testArray4 = new ArrayList<ArrayList<Cell>>();
    testArray4.add(testRow7);
    testArray4.add(testRow8);

    this.testRow9 = new ArrayList<Cell>();
    this.testRow10 = new ArrayList<Cell>();
    testRow9.add(yellowCell);
    testRow9.add(purpleCell);
    testRow10.add(orangeCell);
    testRow10.add(redCell);
    this.ypor = new ArrayList<ArrayList<Cell>>();
    ypor.add(testRow9);
    ypor.add(testRow10);
    this.testFlood5 = new FloodItWorld(ypor);

    this.testRow11 = new ArrayList<Cell>();
    this.testRow12 = new ArrayList<Cell>();
    testRow9.add(purpleCell);
    testRow9.add(redCell);
    testRow10.add(redCell);
    testRow10.add(redCell);
    this.prrr = new ArrayList<ArrayList<Cell>>();
    prrr.add(testRow5);
    prrr.add(testRow6);
    this.testFlood4 = new FloodItWorld(prrr);

    this.rrrr = new ArrayList<ArrayList<Cell>>();
    rrrr.add(testRow6);
    rrrr.add(testRow6);
    this.testFlood6 = new FloodItWorld(rrrr);

    this.all10Fl = new ArrayList<ArrayList<Cell>>();
    all10Fl.add(testRow13);
    all10Fl.add(testRow14);
    this.testFlood10 = new FloodItWorld(all10Fl);

    this.testRow13 = new ArrayList<Cell>();
    this.all10Fl = new ArrayList<ArrayList<Cell>>();
    for (int i = 0; i < 10; i++) {
      testRow13.add(yellowCell);
    }
    for (int i = 0; i < 10; i++) {
      all10Fl.add(testRow13);
    }
    this.testFlood10 = new FloodItWorld(all10Fl);

    this.randSeed1 = new Cell(10, 10, true, new Random(1));
    this.randSeed2 = new Cell(10, 10, true, new Random(2));
    this.randSeed3 = new Cell(10, 10, true, new Random(3));
    this.randSeed4 = new Cell(10, 10, true, new Random(4));
    this.randSeed5 = new Cell(10, 10, true, new Random(5));
    this.randSeed6 = new Cell(10, 10, true, new Random(6));

    this.blues = new Blues();
    this.rainbowColors = new RainbowColor();
    this.camoColors = new CamoColors();
  }

  // run FloodIt
  void testFlood(Tester t) {
    reset();
    // num cells * cell length for width and height
    testFlood.bigBang(FloodItWorld.BOARD_SIZE * FloodItWorld.CELL_LENGTH + 200,
        FloodItWorld.BOARD_SIZE * FloodItWorld.CELL_LENGTH + 200, 0.03);
  }

  // tests for initBoard
  void testInitBoard(Tester t) {
    reset();
    t.checkExpect(this.testFlood.initBoard().size(), FloodItWorld.BOARD_SIZE);
    t.checkExpect(this.testFlood.initBoard().get(0).size(), (FloodItWorld.BOARD_SIZE));
    t.checkExpect(this.testFlood.initBoard().get(1).get(1).x, 1);
    t.checkExpect(this.testFlood.initBoard().get(5).get(3).y, 5);
  }

  // tests for onKeyEvent
  void testOnKey(Tester t) {
    reset();
    t.checkExpect(this.testFlood2, this.testFlood2);
    this.testFlood2.onKeyEvent("r");
    t.checkExpect(this.testFlood2.board.get(0).get(0).y, 0);
    t.checkExpect(this.testFlood10, this.testFlood10);
    this.testFlood10.onKeyEvent("r");
    t.checkExpect(this.testFlood10.board.get(0).get(0).flooded, true);
    t.checkExpect(this.testFlood4, this.testFlood4);
    this.testFlood4.onKeyEvent("r");
    t.checkExpect(this.testFlood4.board.get(0).get(0).x, 0);
  }

  // tests for onMouseClicked
  void testOnMouseClicked(Tester t) {
    reset();
    t.checkExpect(this.testFlood3, this.testFlood3);
    this.testFlood3.onMouseClicked(new Posn(1, 0));
    t.checkExpect(this.testFlood3.board.get(1).get(0).color, "green");

    t.checkExpect(this.testFlood5, this.testFlood5);
    this.testFlood5.onMouseClicked(new Posn(0, 1));
    t.checkExpect(this.testFlood5.board.get(0).get(1).flooded, true);

    t.checkExpect(this.testFlood4, this.testFlood4);
    this.testFlood4.onMouseClicked(new Posn(0, 0));
    t.checkExpect(this.testFlood4.board.get(0).get(1).color, "green");
  }

  // tests for changeAdjTrue

  // tests for changeCellOne
  void testChangeCellOne(Tester t) {
    reset();
    this.testFlood5.changeCellOne("blue");
    t.checkExpect(testFlood5.board.get(0).get(0).color, "blue");
    this.testFlood2.changeCellOne("red");
    t.checkExpect(testFlood2.board.get(0).get(0).color, "red");
  }

  // test for makeScene
  void testMakeScene(Tester t) {
    reset();
    WorldScene scene = new WorldScene(10 * 10, 10 * 10);
    scene.placeImageXY(drawRed, 525, 525);
    scene.placeImageXY(drawOrange, 525, 525);
    scene.placeImageXY(drawYellow, 525, 525);
    scene.placeImageXY(drawPurple, 525, 525);
    t.checkExpect(this.testFlood2.makeScene(), scene);
  }

  // test the number of moves text(counter)
  boolean testDrawMoveCounter(Tester t) {
    reset();
    return t.checkExpect(this.testFlood.drawMoveCounter(),
        new TextImage(Integer.toString(0) + "/" + Integer.toString(20), 24, Color.BLACK))
        && t.checkExpect(this.testFlood2.drawMoveCounter(),
            new TextImage(Integer.toString(0) + "/" + Integer.toString(20), 24, Color.BLACK));
  }

  // test movesText
  boolean testMovesText(Tester t) {
    reset();
    return t.checkExpect(this.testFlood.movesText(), new TextImage("moves", 24, Color.BLACK))
        && t.checkExpect(this.testFlood2.movesText(), new TextImage("moves", 24, Color.BLACK))
        && t.checkExpect(this.testFlood4.movesText(), new TextImage("moves", 24, Color.BLACK));
  }

  // test Time text
  boolean testTimeText(Tester t) {
    reset();
    return t.checkExpect(this.testFlood.timeText(), new TextImage("time: ", 24, Color.BLACK))
        && t.checkExpect(this.testFlood2.timeText(), new TextImage("time: ", 24, Color.BLACK))
        && t.checkExpect(this.testFlood3.timeText(), new TextImage("time: ", 24, Color.BLACK));
  }

  // test the r for reset text
  boolean testResetText(Tester t) {
    reset();
    return t.checkExpect(this.testFlood3.resetText(),
        new TextImage("press 'r' to reset", 24, Color.BLACK))
        && t.checkExpect(this.testFlood4.resetText(),
            new TextImage("press 'r' to reset", 24, Color.BLACK))
        && t.checkExpect(this.testFlood2.resetText(),
            new TextImage("press 'r' to reset", 24, Color.BLACK));
  }

  // test timeNums
  boolean testTimeNums(Tester t) {
    reset();
    return t.checkExpect(this.testFlood.timeNums(), new TextImage("00:00", 24, Color.BLACK))
        && t.checkExpect(this.testFlood5.timeNums(), new TextImage("00:00", 24, Color.BLACK));

  }

  // test lose screen
  boolean testLose(Tester t) {
    reset();
    return t.checkExpect(this.testFlood.lose(),
        new OverlayImage(
            new AboveImage(new TextImage("you lose :(", 24, Color.RED),
                new TextImage("press 'r' to reset", 24, Color.BLACK)),
            new RectangleImage(BOARD_SIZE * CELL_LENGTH + 200, BOARD_SIZE * CELL_LENGTH + 200,
                OutlineMode.SOLID, Color.WHITE)))

        &&

        t.checkExpect(this.testFlood2.lose(),
            new OverlayImage(
                new AboveImage(new TextImage("you lose :(", 24, Color.RED),
                    new TextImage("press 'r' to reset", 24, Color.BLACK)),
                new RectangleImage(BOARD_SIZE * CELL_LENGTH + 200, BOARD_SIZE * CELL_LENGTH + 200,
                    OutlineMode.SOLID, Color.WHITE)));
  }

  // test win screen
  boolean testWin(Tester t) {
    reset();
    return t.checkExpect(this.testFlood3.win(),
        new OverlayImage(
            new AboveImage(new TextImage("you win!", 24, Color.GREEN),
                new TextImage("press 'r' to reset", 24, Color.BLACK)),
            new RectangleImage(BOARD_SIZE * CELL_LENGTH + 200, BOARD_SIZE * CELL_LENGTH + 200,
                OutlineMode.SOLID, Color.WHITE)))
        && t.checkExpect(this.testFlood4.win(),
            new OverlayImage(
                new AboveImage(new TextImage("you win!", 24, Color.GREEN),
                    new TextImage("press 'r' to reset", 24, Color.BLACK)),
                new RectangleImage(BOARD_SIZE * CELL_LENGTH + 200, BOARD_SIZE * CELL_LENGTH + 200,
                    OutlineMode.SOLID, Color.WHITE)));
  }

  // tests for onTick
  void testOnTick(Tester t) {
    reset();
    t.checkExpect(this.testFlood2, this.testFlood2);
    this.testFlood3.onTick();
    t.checkExpect(this.testFlood2.board.get(0).get(0).color, "red");

    t.checkExpect(this.testFlood10, this.testFlood10);
    this.testFlood10.onTick();
    t.checkExpect(this.testFlood10.board.get(0).get(0).flooded, true);

    t.checkExpect(this.testFlood4, this.testFlood4);
    this.testFlood4.onTick();
    t.checkExpect(this.testFlood4.board.get(1).get(0), this.testFlood4.board.get(1).get(0));
  }

  // test for flooding
  void testingFlood(Tester t) {
    reset();
    this.testFlood2.flood(0, "orange");
    t.checkExpect(testFlood2.board.get(0).get(0).color, "orange");
    t.checkExpect(testFlood2.board.get(0).get(1).color, "orange");
    t.checkExpect(testFlood2.board.get(1).get(0).color, "purple");
    t.checkExpect(testFlood2.board.get(1).get(1).color, "yellow");
    this.testFlood5.flood(0, "orange");
    t.checkExpect(testFlood5.board.get(0).get(0).color, "orange");
    t.checkExpect(testFlood5.board.get(0).get(1).color, "purple");
    t.checkExpect(testFlood5.board.get(1).get(0).color, "orange");
    t.checkExpect(testFlood5.board.get(1).get(1).color, "orange");
    this.testFlood6.flood(0, "red");
    t.checkExpect(testFlood6.board.get(0).get(0).color, "red");
    t.checkExpect(testFlood6.board.get(0).get(1).color, "red");
    t.checkExpect(testFlood6.board.get(1).get(0).color, "red");
    t.checkExpect(testFlood6.board.get(1).get(1).color, "red");
  }

  // test for all Flooded
  boolean testAllFlooded(Tester t) {
    reset();
    return t.checkExpect(this.testFlood10.allFlooded(), true)
        && t.checkExpect(this.testFlood2.allFlooded(), false)
        && t.checkExpect(this.testFlood3.allFlooded(), false);
  }

  // tests for string2Color
  void testString2Color(Tester t) {
    reset();
    t.checkExpect(this.testCell.string2Color("red"), Color.RED);
    t.checkExpect(this.testCell.string2Color("blue"), Color.BLUE);
    t.checkExpect(this.testCell.string2Color("green"), Color.GREEN);
    t.checkExpect(this.testCell.string2Color("purple"), Color.MAGENTA);
    t.checkExpect(this.testCell.string2Color("yellow"), Color.YELLOW);
    t.checkExpect(this.testCell.string2Color("orange"), Color.ORANGE);
  }

  // tests for drawCell
  void testDrawCell(Tester t) {
    reset();
    t.checkExpect(this.greenCell.drawCell(), new RectangleImage(FloodItWorld.CELL_LENGTH,
        FloodItWorld.CELL_LENGTH, OutlineMode.SOLID, Color.GREEN));
    t.checkExpect(this.blueCell.drawCell(), new RectangleImage(FloodItWorld.CELL_LENGTH,
        FloodItWorld.CELL_LENGTH, OutlineMode.SOLID, Color.BLUE));
    t.checkExpect(this.redCell.drawCell(), new RectangleImage(FloodItWorld.CELL_LENGTH,
        FloodItWorld.CELL_LENGTH, OutlineMode.SOLID, Color.RED));
    t.checkExpect(this.orangeCell.drawCell(), new RectangleImage(FloodItWorld.CELL_LENGTH,
        FloodItWorld.CELL_LENGTH, OutlineMode.SOLID, Color.ORANGE));
    t.checkExpect(this.purpleCell.drawCell(), new RectangleImage(FloodItWorld.CELL_LENGTH,
        FloodItWorld.CELL_LENGTH, OutlineMode.SOLID, Color.MAGENTA));
    t.checkExpect(this.yellowCell.drawCell(), new RectangleImage(FloodItWorld.CELL_LENGTH,
        FloodItWorld.CELL_LENGTH, OutlineMode.SOLID, Color.YELLOW));
  }

  // tests for change Adj true
  void testChangeAdjTrue(Tester t) {
    reset();
    t.checkExpect(this.testFlood2, this.testFlood2);
    this.testFlood2.changeAdjTrue();
    t.checkExpect(this.testFlood3.board.get(0).get(0).flooded, true);
    t.checkExpect(this.testFlood6, this.testFlood6);
    this.testFlood6.changeAdjTrue();
    t.checkExpect(this.testFlood6.board.get(0).get(0).flooded, true);
    t.checkExpect(this.testFlood2, this.testFlood2);
    this.testFlood2.changeAdjTrue();
    t.checkExpect(this.testFlood2.board.get(1).get(1).flooded, true);
  }

  //// test get Colros
  // boolean testGetColor(Tester t) {
  // return t.checkExpect(this.Blues.getColor(), this.randSeed1);
}