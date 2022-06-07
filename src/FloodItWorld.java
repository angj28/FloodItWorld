import java.util.ArrayList;
import java.util.Arrays;

import tester.*;
import javalib.impworld.WorldScene;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.Random;
import java.util.Stack;

//a utilities class
class Utils<T> {
  // checks if either null
  int checkNumColors(int num) {
    if (num < 3 || num > 8) {
      throw new IllegalArgumentException("Illegal Amount of Colors");
    }
    else {
      return num;
    }
  }
}

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// Represents a single square of the game area
class Cell {
  // In logical coordinates, with the origin at the top-left corner of the screen
  int x;
  int y;
  Color color;
  boolean flooded;
  // the four adjacent cells to this one
  Cell left;
  Cell top;
  Cell right;
  Cell bottom;
  int offset;

  // the constructor
  Cell(int x, int y, Color color, boolean flooded, Cell left, Cell top, Cell right, Cell bottom) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.flooded = flooded;
    this.left = left;
    this.top = top;
    this.right = right;
    this.bottom = bottom;
    left.right = this;
    right.left = this;
    bottom.top = this;
    top.bottom = this;
    this.offset = 5;
  }

  // convenience constructor
  Cell(int x, int y, Color color, boolean flooded) {
    this.offset = 50;
    this.x = x * this.offset;
    this.y = y * this.offset;
    this.color = color;
    this.flooded = flooded;

    this.top = null;
    this.left = null;
    this.right = null;
    this.bottom = null;
  }

  // connects to left cell
  void left(Cell c) {
    this.left = c;
    c.right = this;
  }

  // connects to right cell
  void right(Cell c) {
    this.right = c;
    c.left = this;
  }

  // connects to top cell
  void top(Cell c) {
    this.top = c;
    c.bottom = this;
  }

  // connects to bottom cell
  void bottom(Cell c) {
    this.bottom = c;
    c.top = this;
  }

  // draws this cell
  WorldImage draw() {
    return new RectangleImage(this.offset, this.offset, OutlineMode.SOLID, this.color);
  }

  // places this cell on a scene
  void place(WorldScene scene) {
    scene.placeImageXY(this.draw(), this.x + (FloodItWorld.DIMENSION / 2) + 25,
        this.y + (FloodItWorld.DIMENSION / 2) + 25);
  }
  
  void floodSingleNeighbor(Cell neighbor, Stack<Cell> stack, Color color) {
    if (neighbor != null &&
        neighbor.color.equals(color) &&
        !stack.contains(neighbor)) {
      neighbor.flooded = true;
      stack.push(neighbor);
    } 
  }

  //floods neighbors who have the same color 
  void floodNeighbors(Stack<Cell> stack) {
    this.floodSingleNeighbor(this.left, stack, this.color);
    this.floodSingleNeighbor(this.right, stack, this.color);
    this.floodSingleNeighbor(this.top, stack, this.color);
    this.floodSingleNeighbor(this.bottom, stack, this.color);
  }

  //changes color if this cell is flooded and floods neighbors
  void setColor(Color color) {
      this.color = color;
  }
}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

//to represent the game world
class FloodItWorld extends World {
  ArrayList<Cell> board;
  static int BOARD_SIZE;
  static int OFFSET;
  static int DIMENSION;
  int numColors;
  int clicks;
  int winningNum;
  int time;
  Random rand;
  Color[] colors = { Color.black, Color.red, Color.yellow, Color.blue, Color.pink, Color.gray,
      Color.orange, Color.green };
 Stack<Cell> worklist = new Stack<Cell>();

  //the constructor
  FloodItWorld(int size, int numColors) {
    // All the cells of the game
    FloodItWorld.BOARD_SIZE = size;
    FloodItWorld.OFFSET = 50;
    FloodItWorld.DIMENSION = FloodItWorld.BOARD_SIZE * FloodItWorld.OFFSET;
    this.board = new ArrayList<Cell>(size * size);
    this.numColors = new Utils<Integer>().checkNumColors(numColors);
    this.generateCells();
    this.clicks = 0;
    this.winningNum = this.determineWinningNum();
  }
  
  //constructor for testing
  FloodItWorld(int size, int numColors, Random rand) {
    // All the cells of the game
    FloodItWorld.BOARD_SIZE = size;
    FloodItWorld.OFFSET = 50;
    FloodItWorld.DIMENSION = (FloodItWorld.BOARD_SIZE * FloodItWorld.OFFSET);
    this.board = new ArrayList<Cell>(size * size);
    this.numColors = new Utils<Integer>().checkNumColors(numColors);
    this.clicks = 0;
    this.rand = rand;
    this.generateCells();
    this.winningNum = this.determineWinningNum();
  }

  //--------------------INITIALIZE--------------------

  // determines the winning number of steps required based on size and number of
  // colors
  public int determineWinningNum() {
    if (FloodItWorld.BOARD_SIZE > 10) {
      return FloodItWorld.BOARD_SIZE + this.numColors + 10;
    }
    else if (FloodItWorld.BOARD_SIZE < 4) {
      return FloodItWorld.BOARD_SIZE + this.numColors - 6;
    }
    else {
      return FloodItWorld.BOARD_SIZE + this.numColors + 3;
    }
  }

  // generates all the cells in a board
  void generateCells() {
    for (int row = 0; row < FloodItWorld.BOARD_SIZE; row++) {
      for (int col = 0; col < FloodItWorld.BOARD_SIZE; col++) {
        Cell cell = new Cell(col, row, colors[new Random().nextInt(numColors + 1)], false);
        if (row == 0 && col == 0) {
          cell.flooded = true;
          this.board.add(cell);
        }
        else if (row == 0) {
          cell.left(this.board.get(this.board.size() - 1));
          this.board.add(cell);
        }
        else if (col == 0) {
          cell.top(this.board.get(this.board.size() - FloodItWorld.BOARD_SIZE));
          this.board.add(cell);
        }
        else {
          cell.left(this.board.get(this.board.size() - 1));
          cell.top(this.board.get(this.board.size() - FloodItWorld.BOARD_SIZE));
          this.board.add(cell);
        }
      }
    }
  }

  // --------------------DISPLAY--------------------

  @Override
  // makes the world scene that is displayed
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(FloodItWorld.DIMENSION * 2 + 1,
        FloodItWorld.DIMENSION * 2 + 1);
    return this.placeInfo(this.placeGrid(scene));
  }

  // places grid on the screen
  public WorldScene placeGrid(WorldScene scene) {
    WorldImage border = new RectangleImage(FloodItWorld.DIMENSION, FloodItWorld.DIMENSION,
        OutlineMode.OUTLINE, Color.black);
    for (Cell c : board) {
      c.place(scene);
    }
    scene.placeImageXY(border, (FloodItWorld.DIMENSION),
        (FloodItWorld.DIMENSION));
    gameEnd(scene);
    return scene;
  }
  
  //gets the time of the gameplay
  public String getTime() {
    return "Time: " + Integer.toString(this.time/60) + "." + Integer.toString(this.time%60);
  }

  // places info on the scene
  public WorldScene placeInfo(WorldScene scene) {
    WorldImage score = new TextImage(
        Integer.toString(this.clicks) + " / " + Integer.toString(this.winningNum),
        FloodItWorld.DIMENSION / 15, FontStyle.BOLD, Color.black);
    WorldImage title = new TextImage("FLOOD-IT", FloodItWorld.DIMENSION / 10, FontStyle.BOLD,
        Color.black);
    WorldImage time = new TextImage(this.getTime(), FloodItWorld.DIMENSION / 15, FontStyle.BOLD, 
        Color.black);
    scene.placeImageXY(score, FloodItWorld.DIMENSION, FloodItWorld.DIMENSION / 3);
    scene.placeImageXY(title, FloodItWorld.DIMENSION, FloodItWorld.DIMENSION / 5);
    scene.placeImageXY(time, FloodItWorld.DIMENSION/4, FloodItWorld.DIMENSION / 3);
    return scene;
  }
  
  //end scene image
  public WorldImage endScene(String message, Color color) {
    return new TextImage(message, 50.0, color); 
  }
  
  //determines if the game is over and displays correct end game message
  public void gameEnd(WorldScene scene) {
    if (this.clicks > this.winningNum && this.allFlooded()) {
      scene.placeImageXY(this.endScene("You Lose", Color.MAGENTA), FloodItWorld.DIMENSION, FloodItWorld.DIMENSION);
    } else if (this.clicks <= this.winningNum && this.allFlooded()) {
      scene.placeImageXY(this.endScene("You Win", Color.MAGENTA), FloodItWorld.DIMENSION, FloodItWorld.DIMENSION);
    }
  }

  //--------------------FUNCTIONALITY--------------------

  // Determines which cell in the board was clicked on
  public Cell whichCell(Posn pos) {
    for (Cell c: board) {
      if (pos.x - (FloodItWorld.DIMENSION / 2) <= c.x + FloodItWorld.OFFSET && 
          pos.x - (FloodItWorld.DIMENSION / 2) >= c.x &&
          pos.y - (FloodItWorld.DIMENSION / 2) <= c.y + FloodItWorld.OFFSET && 
          pos.y - (FloodItWorld.DIMENSION / 2) >= c.y) {
        return c;
      }
    }
    return null;
  }
 

  @Override
  //handles a mouse click 
  //EFFECT: updates each cell based on the color of the cell clicked
  public void onMouseClicked(Posn pos) {
    if ((pos.x < (FloodItWorld.DIMENSION / 2) || pos.x > (FloodItWorld.DIMENSION + (FloodItWorld.DIMENSION / 2)))
        || (pos.y < (FloodItWorld.DIMENSION / 2) || pos.y > (FloodItWorld.DIMENSION + (FloodItWorld.DIMENSION / 2)))) {
    } else {
      Cell clickedCell = this.whichCell(pos);
      if (clickedCell != null && 
          this.board.get(0).color!= clickedCell.color ) {
        this.board.get(0).color = clickedCell.color;
        clicks++;  
      }
    }
  }
  
//  //updates the world
//  //EFFECT: Changes the color of flooded cells to the color of the clicked cell
//  public void updateWorld() {
//    
//  }
  
  // increments time on each tick 
  public void onTick() {
    if(!this.allFlooded()) {
      time++;      
    }
    Stack<Cell> temp = new Stack<Cell>();
    Color color = this.board.get(0).color;
    while (this.worklist.size() > 0) {
      Cell c = this.worklist.pop();
      c.setColor(color);
      c.floodNeighbors(temp);
    }
    while (temp.size() > 0) {
      this.worklist.push(temp.pop());
    }
  }
  
  // Checks if all the cells in the board are flooded
  boolean allFlooded() {
    boolean allFlooded = true;
    for (Cell c: board) {
      allFlooded = allFlooded && c.flooded;
    }
    return allFlooded;
  }
  
  //resets the game when the player presses "r" key
  public void onKeyEvent(String key) {
    if (key.equals("r")) {
      this.board = new ArrayList<Cell>();
      clicks = 0;
      time = 0;
      generateCells();
    }
  }
}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

//represents examples and test for cells
class ExamplesCell {
  ExamplesCell() {}

  // World scene
  WorldScene background;
  WorldScene background2;

  //examples of cells
  Cell c1;
  Cell c2;
  Cell c3;
  Cell c4;

  //floodItWorlds
  FloodItWorld world1;
  FloodItWorld world2;
  FloodItWorld world3;
  FloodItWorld world4;

  // initial data to use for tests
  void initData() {

    //WorldScene
    this.background = new WorldScene(500, 500);
    this.background2 = new WorldScene(500, 500);

    // cells
    this.c1 = new Cell(0, 0, Color.yellow, false);
    this.c2 = new Cell(0, 1, Color.pink, false);
    this.c3 = new Cell(1, 0, Color.cyan, false);
    this.c4 = new Cell(1, 1, Color.orange, false);

    // floodItWorlds
    this.world1 = new FloodItWorld(22, 6);
    this.world2 = new FloodItWorld(2, 3);
    this.world3 = new FloodItWorld(14, 7);
    this.world4 = new FloodItWorld(1, 3, new Random(5));
  }

  //to test the testLeft method in Cell class
  void testLeft(Tester t) {
    this.initData();
    this.c2.left(this.c1);
    t.checkExpect(this.c2.left, this.c1);
    t.checkExpect(this.c1.right, this.c2);
    this.c4.left(this.c3);
    t.checkExpect(this.c4.left, this.c3);
    t.checkExpect(this.c3.right, this.c4);
  }

  //to test the testRight method in Cell class
  void testRight(Tester t) {
    this.initData();
    this.c1.right(this.c2);
    t.checkExpect(this.c2.left, this.c1);
    t.checkExpect(this.c1.right, this.c2);
    this.c3.right(this.c4);
    t.checkExpect(this.c4.left, this.c3);
    t.checkExpect(this.c3.right, this.c4);
  }

  //to test the testTop method in Cell class
  void testTop(Tester t) {
    this.initData();
    this.c3.top(this.c1);
    t.checkExpect(this.c3.top, this.c1);
    t.checkExpect(this.c1.bottom, this.c3);
    this.c4.top(this.c2);
    t.checkExpect(this.c4.top, this.c2);
    t.checkExpect(this.c2.bottom, this.c4);
  }

  // to test the testBottom method in Cell class
  void testBottom(Tester t) {
    this.initData();
    this.c1.bottom(this.c3);
    t.checkExpect(this.c3.top, this.c1);
    t.checkExpect(this.c1.bottom, this.c3);
    this.c2.bottom(this.c4);
    t.checkExpect(this.c4.top, this.c2);
    t.checkExpect(this.c2.bottom, this.c4);
  }

  //to test the Draw method in Cell class
  void testDraw(Tester t) {
    this.initData();
    t.checkExpect(this.c1.draw(), new RectangleImage(50, 50, OutlineMode.SOLID, Color.yellow));
    t.checkExpect(this.c2.draw(), new RectangleImage(50, 50, OutlineMode.SOLID, Color.pink));
    t.checkExpect(this.c3.draw(), new RectangleImage(50, 50, OutlineMode.SOLID, Color.cyan));
    t.checkExpect(this.c4.draw(), new RectangleImage(50, 50, OutlineMode.SOLID, Color.orange));
  }

  // to test the Place method in the Cell class *****
  void testPlace(Tester t) {
    this.initData();
    t.checkExpect(this.c1, this.c1);
    t.checkExpect(this.background, new WorldScene(500, 500));
    this.c1.place(this.background);
    this.background2.placeImageXY(this.c1.draw(), 50, 50);
    t.checkExpect(this.background, this.background2);
  }

  // to test the determineWinningNum method in FloodItWorld class
  void testDetermineWinningNum(Tester t) {
    this.initData();
    t.checkExpect(this.world1.determineWinningNum(), 5);
    t.checkExpect(this.world2.determineWinningNum(), 2);
    t.checkExpect(this.world3.determineWinningNum(), 6);
  }

  // to test the makeScene method in the FloodItWorld class
  void testMakeScene(Tester t) {
    this.initData();
    t.checkExpect(this.world4, new FloodItWorld(1, 3, new Random(5)));
    t.checkExpect(this.world4.makeScene(), this.world4.placeInfo(this.world4.placeGrid(
        new WorldScene(101, 101))));
  }

  // to test the placeGrid function in the FloodItWorld class
  void testPlaceGrid(Tester t) {
    this.initData();
    t.checkExpect(this.world2.placeGrid(this.background), this.background);
    t.checkExpect(this.world1.placeGrid(this.background), this.background);
  }

  //to test the placeInfo function in the FloodItWorld class
  void testPlaceInfo(Tester t) {
    this.initData();
    t.checkExpect(this.world2.placeInfo(this.background), this.background);
    t.checkExpect(this.world1.placeInfo(this.background), this.background);
  }

  //to test the generateCells function in the FloodItWorld class
  void testGenerateCells(Tester t) {
    this.initData();
    t.checkExpect(this.world4, this.world4);
    this.world4.generateCells();
    t.checkExpect(this.world4.board, new ArrayList<Cell>(Arrays.asList(this.c1, 
        new Cell(0, 0, Color.magenta, false))));
  }
}

//to represent examples and tests of MyWorldProgram
class ExamplesFloodItWorld {
  void testGame(Tester t) {
    FloodItWorld g = new FloodItWorld(15, 7);
    g.bigBang(FloodItWorld.OFFSET * FloodItWorld.BOARD_SIZE * 2,
        FloodItWorld.OFFSET * FloodItWorld.BOARD_SIZE * 2, 1.0);
  }
}

