import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;
import bagel.*;
import bagel.util.Vector2;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.Random;

/**
 * ShadowDance class represents the main game class for the Shadow Dance game.
 * It extends the AbstractGame class and implements game logic and rendering.
 * @author yellowcode
 * @version 1.0
 * @since 2023-10-12
 */
public class ShadowDance extends AbstractGame 
{
    /**
     * The width,height and title of the game window.
     */
    private final static Integer WINDOW_WIDTH = 1024;
    private final static Integer WINDOW_HEIGHT = 768;
    private final static String GAME_TITLE = "SHADOW DANCE";   

    /**
     * Enumeration representing different game states.
     */
    private enum GameState {
        START, PLAY, END, WIN, LOSE
    }
    /**
     * Enumeration representing different types of musical notes in the game.
     */
    private enum NoteType{
        NORMAL , HOLD , SPECIAL , BOMB
    }
    private GameState gameState = GameState.START;
    private Integer gameLevel = 1 ; 
    private Integer frameCount = 0;

    private Screen screen = null ; 
    private Score score = null ;
    private ObjectsManager obm = null ;

    /**
     * Constructs a new ShadowDance game.
     * Initializes the game window size and title, and starts a new game.
     */
    private ShadowDance()
    {
        super(WINDOW_WIDTH, WINDOW_HEIGHT, GAME_TITLE);
        newGame();
    }
    /**
     * Starts a new game by initializing game state, level, frame count, and game objects.
     */
    private void newGame() 
    {
        gameState = GameState.START;
        gameLevel = 1 ; 
        frameCount = 0;

        screen = new Screen(); 
        score = new Score() ; 
        obm = new ObjectsManager() ; 
        obm.readCSV(); 
    }
    /**
     * Entry point of the game. Creates a new instance of ShadowDance and runs the game.
     * @param args Command line arguments (not used in this game).
     */
    public static void main(String[] args) {
        ShadowDance game = new ShadowDance();
        game.run();
    }

    /**
     * Re-draw screen every frame
     */
    @Override
    protected void update(Input input) 
    {
        screen.Draw(input);
        ++frameCount ; 
        obm.Draw(input);  
    }

    /**
     * The Screen class manages the game screen and its components, including background, fonts, and game state visuals.
     * It handles drawing different game screens, processing user input, and updating game states.
     */
    private class Screen
    {
        /**
         * Constructs a new Screen object.
         * Initializes the screen's refresh rate based on the system's display mode.
         */
        private Screen()
        {
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            speed = gd.getDisplayMode().getRefreshRate();
            if ( speed <= 60) speed = 4;
            else speed = 2 ; 
        }
        private final Image BACKGROUND_IMAGE = new Image("res/background.png");
        private final Font  FONT_TITLE       = new Font("res/FSO8BITR.TTF", 64);
        private final Font  FONT_ORTHER      = new Font("res/FSO8BITR.TTF", 24);
        private final Font FONT_MSG = new Font("res/FSO8BITR.TTF", 64);
        private Integer speed = 0; 

        /**
         * Updates the screen's refresh rate by the specified amount.
         * @param change The change in speed to be applied.
         */
        private void updateSpeed(Integer change)
        {
            speed += change ; 
        }
        /**
         * Draws the game screen components based on the current game state and user input.
         * {@code if (input.wasPressed(Keys.ESCAPE))} this code is used to check press ESC button to close game
         * @param input The Input object to handle user input.
         */
        private void Draw(Input input) 
        {
            BACKGROUND_IMAGE.draw(Window.getWidth() / 2.0, Window.getHeight() / 2.0);
            if (input.wasPressed(Keys.ESCAPE)) 
            {
                Window.close();
            }
            switch (gameState) {
            case START:
            {
                if ( input.wasPressed(Keys.NUM_1)) 
                {
                    gameLevel = 1 ; 
                    gameState = GameState.PLAY;
                }
                else if (input.wasPressed(Keys.NUM_2))
                {
                    gameLevel = 2 ;  
                    gameState = GameState.PLAY;
                } 
                else if (input.wasPressed(Keys.NUM_3))
                {
                    gameLevel = 3 ; 
                    gameState = GameState.PLAY;
                } 
                FONT_TITLE.drawString("SHADOW DANCE", 220, 250-64);
                FONT_ORTHER.drawString("SELECT LEVELS WITH", 340, 250-64+190-24);
                FONT_ORTHER.drawString("NUMBER KEYS", 405, 250-64+190-24+40);
                FONT_ORTHER.drawString("1 2 3", 465, 250-64+190-24+40+80);

                break ; 
            }
            case PLAY:
            {
                score.Draw();
                break;
            }
            case WIN:
            {
                FONT_MSG.drawString("CLEAR!", Window.getWidth() / 2 - FONT_MSG.getWidth("CLEAR!")/2, Window.getHeight() / 2 - 20);
                break;
            }
            case LOSE:
            {
                FONT_MSG.drawString("TRY AGAIN", Window.getWidth() / 2 - FONT_MSG.getWidth("TRY AGAIN")/2, Window.getHeight() / 2 - 20);
                FONT_ORTHER.drawString("PRESS SPACE TO RETURN TO LEVEL SELECTION", 
                                    Window.getWidth() / 2 - FONT_ORTHER.getWidth("PRESS SPACE TO RETURN TO LEVEL SELECTION")/2, 500);
                
                if (input.wasPressed(Keys.SPACE)) 
                {
                    newGame() ; 
                }
                break;
            }
            default:
                break;
            }
        }
    }
    /**
     * The Score class manages the game score, messages for scoring, and related visual elements.
     * It handles updating and displaying the player's score, game's messages, and game state based on player performance.
     */
    private class Score
    {
        private Score(){}
        /**
         * The font used for displaying the current score.
         * The font used for displaying score-related messages.
         */
        private final Font  FONT_SCORE       = new Font("res/FSO8BITR.TTF", 30);
        private final Font  FONT_MSG_SCORE   = new Font("res/FSO8BITR.TTF", 40);
        
        /**
         * The current score of the player.
         * The current message to be displayed (e.g., "PERFECT", "GOOD", "BAD", "MISS").
         * The frame count for displaying the current message.
         * The frame count for handling 2x score change.
         * The multiplier for score change.
         */
        private Integer currentScore = 0;
        private String currentMsg = "" ; 
        private Integer frameMsgCount = 0;
        private Integer frame2xCount = 0 ; 
        private Integer XscoreChange = 1 ; 

        /**
         * Displays the current score on the game screen.
         */
        private void showScore()
        {
            FONT_SCORE.drawString("SCORE " + currentScore, 35, 35);
        }
        /**
         * Displays the current score-related message on the game screen.
         */
        private void showMsg()
        {
            FONT_MSG_SCORE.drawString(currentMsg, Window.getWidth() / 2 - FONT_MSG_SCORE.getWidth(currentMsg)/2, Window.getHeight() / 2 - 20);
        }
        /**
         * Updates the score change multiplier by the specified amount.
         * @param change The change in the score change multiplier.
         */
        private void updateScoreChange(Integer change)
        {
            XscoreChange = change ; 
            frame2xCount = 0 ; 
        }
        /**
         * Updates the current message to be displayed.
         * @param msg The new message to be displayed.
         */
        private void updateCurrentMsg(String msg)
        {
            currentMsg = msg ; 
            frameMsgCount = 0 ; 
        }
        /**
         * increase the score.
         * @param change The change in the score change multiplier.
         */
        private void updateScore(Integer score)
        {
            currentScore += score ; 
        }
        /**
         * Updates the player's score based on the given distance and returns the corresponding message.
         * @param distance The distance of the player's performance.
         * @return The message indicating the player's performance ("PERFECT", "GOOD", "BAD", "MISS").
         */
        private String callScore(double distance) 
        {
            String message = "";
            Integer scoreChange = 0 ;
            if (distance <= 0) 
            {
                scoreChange = 0;
                message = "";
            } 
            else if (distance <= 15) 
            {
                scoreChange = 10;
                message = "PERFECT";
            }
            else if (distance <= 50) 
            {
                scoreChange = 5;
                message = "GOOD";
            } 
            else if (distance <= 100) 
            {
                scoreChange = -1;
                message = "BAD";
            } 
            else if (distance <= 200) 
            {
                scoreChange = -5;
                message = "MISS";
            } 
            else 
            {
                scoreChange = -5;
                message = "MISS";
            }
            currentScore = currentScore + scoreChange * XscoreChange ;
            if ( (currentScore >= 150 && gameLevel == 1 )|| 
                (currentScore >= 400 && gameLevel == 2 )|| 
                (currentScore >= 350 && gameLevel == 3 ) ) 
                    gameState = GameState.WIN;

            currentMsg = message ; 
            frameMsgCount = 0 ; 

            return message ; 
        }
        /**
         * Updates the frame counts for handling score change multipliers and displaying messages.
         */
        private void updateFrame()
        {
            ++frameMsgCount ; 
            ++frame2xCount;
            if ( frameMsgCount > 30 )
            {
                updateCurrentMsg(""); 
            }
            if ( frame2xCount == 480 ) 
            {
                updateScoreChange(1) ; 
            }
        }
        /**
         * Draws the current score and score-related messages on the game screen.
         */
        private void Draw()
        {
            showScore() ; 
            showMsg() ; 
        }
    }
    /**
     * The ObjectsManager class manages the game objects such as notes, enemies, arrows, and the game lane.
     * It handles reading level data from a CSV file, updating and drawing notes, enemies, and arrows on the game screen.
     */
    private class ObjectsManager
    {
        private ObjectsManager()
        {
        }
        /**
         * The game lane where the notes appear.
         * The list of notes in the game.
         * The guardian object controlled by the player.
         * The list of enemies in the game (applicable for level 3).
         * The list of arrows in the game (applicable for level 3).
         */
        private Lane lane = new Lane() ;
        private List<Note> notes = new ArrayList<>();
        private Guardian guardian = new Guardian();
        private List<Enemy> enemies = new ArrayList<>();
        private List<Arrow> arrows = new ArrayList<>(); 

        /**
         * The file path to the CSV file containing level data.
         */
        private final static String filePath = "res/level3.csv";
        /**
         * Reads level data from the CSV file and initializes game objects accordingly.
         */
        private void readCSV() 
        {
            try {
                Scanner scanner = new Scanner(new File(filePath));
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] parts = line.split(",");
                    String laneName ="" , type = ""; 
                    Integer frame = 0;
                    if (parts.length == 3) 
                    {
                        laneName = parts[0].trim();
                        type = parts[1].trim();
                        frame = Integer.parseInt(parts[2].trim());
                        if ( type.equals("DoubleScore")) type = "2x";
                    }
                    if ( laneName.equals("Lane")) lane.setX(type, frame);
                    else
                    {
                        if ( type.equals("Hold"))
                            notes.add(new HoldNote(NoteType.HOLD,frame,laneName));
                        else if (type.equals("Normal"))
                            notes.add(new NormalNote(NoteType.NORMAL,frame,laneName));
                        else if (type.equals("Bomb"))
                            notes.add(new BombNote(NoteType.BOMB,frame,laneName));
                        else notes.add(new SpecialNote(NoteType.SPECIAL,frame,type,"Special"));
                    } 
                }
                scanner.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        /**
         * Draws the notes on the game screen and checks for player performance to update the score and game state.
         * @param input The input from the player.
         */
        public void DrawNote(Input input)
        {
            score.updateFrame() ;
            for (Note note : notes) 
                if (frameCount >= note.frame && note.Y <= Window.getHeight() && !note.isCleared ) 
                {
                    note.Draw(input) ; 
                    note.Y += screen.speed ; 
                }
            if (notes.get(notes.size()-1).Y >  Window.getHeight()) 
            {
                gameState = GameState.LOSE;
            }
        }
        /**
         * Draws the enemies on the game screen (applicable for level 3).
         */
        public void DrawEnemy()
        {
            if ( gameLevel == 3 )
            {
                if ( frameCount % 600 == 0) enemies.add(new Enemy());
                for (Enemy enemy : enemies) 
                    {
                        enemy.Draw() ;
                    }
            }
        }
        /**
         * Draws the arrows on the game screen (applicable for level 3).
         */
        public void DrawArrow()
        {
            if ( gameLevel == 3 )
            {
                for (Arrow arrow : arrows) 
                    {
                        arrow.Draw() ;
                    }
            }
        }
        /**
         * Draws game objects based on the current game state.
         * @param input The input from the player.
         */
        public void Draw(Input input) 
        {
            switch (gameState) 
            {
            case PLAY:
            {
                lane.Draw();
                DrawEnemy();
                DrawNote(input);
                guardian.Draw(input) ; 
                DrawArrow() ; 
                break ; 
            }
            default:
                break;
            }
        }
    }
    /**
     * The Objects class represents the base class for game objects.
     * It contains methods for calculating distances between points in the game space.
     */
    public class Objects 
    {
        /**
         * The X-coordinate of the object in the game space.
         * The Y-coordinate of the object in the game space.
         */
        protected Integer X = 0 ; 
        protected Integer Y = 24;
        /**
         * Calculates the distance between two points (x1, y1) and (x2, y2) using the Euclidean distance formula.
         * @param x1 The x-coordinate of the first point.
         * @param y1 The y-coordinate of the first point.
         * @param x2 The x-coordinate of the second point.
         * @param y2 The y-coordinate of the second point.
         * @return The distance between the two points.
         */
        protected double Calculate(Integer x1 , Integer y1, Integer x2, Integer y2)
        {
            double dodaiAB = Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1-y2), 2));
            return dodaiAB ; 
        }
    }
    /**
     * The Lane class represents the game lanes where notes and objects move.
     * It extends the Objects class and manages lane positions and drawing on the game screen.
     */
    private class Lane extends Objects 
    {
        private Lane(){}
        /**
         * A map storing lane names as keys and their corresponding X-coordinates as values.
         */
        private Map<String, Integer> cLanes = new HashMap<>();
        /**
         * Draws the lanes on the game screen based on their positions.
         */
        public void Draw()
        {
            for (Map.Entry<String, Integer> entry : cLanes.entrySet()) 
            {
                String key = entry.getKey();
                Integer value = entry.getValue();
                if ( gameLevel == 1 && key.equals("Special")) continue ;
                String strImage = "res/lane" + key + ".png" ;  
                Image LANE_IMAGE = new Image(strImage) ; 
                LANE_IMAGE.draw(value,384);
            }
        }
        /**
         * Retrieves the X-coordinate of a specific lane.
         * @param laneName The name of the lane.
         * @return The X-coordinate of the specified lane.
         */
        public Integer getX(String laneName)
        {
            return cLanes.get(laneName);
        }
        /**
         * Sets the X-coordinate of a specific lane.
         * @param laneName The name of the lane.
         * @param X The new X-coordinate for the lane.
         */
        public void setX(String laneName, Integer X) 
        {
            cLanes.put(laneName, X);
        }
    }
    /**
     * The Note class represents different types of notes in the game.
     * It is an abstract class that extends the Objects class and defines common properties and methods for all note types.
     */
    abstract class Note extends Objects
    {
        /**
         * The frame at which the note appears in the game.
         * The type of the note (NORMAL, HOLD, SPECIAL, BOMB).
         * The lane name of the note.
         * A flag indicating whether the note is cleared.
         */
        private Integer frame = 0;
        private NoteType NoteType ; 
        public String laneName = "";
        private boolean isCleared = false ; 
        /**
         * Creates a new note with a specified type, frame, and lane name.
         * @param NoteType The type of the note (NORMAL, HOLD, SPECIAL, BOMB).
         * @param frame The frame at which the note appears in the game.
         * @param laneName The lane name of the note.
         */
        public Note(NoteType NoteType, Integer frame, String laneName) 
        {
            this.NoteType = NoteType;
            this.frame = frame;
            this.laneName = laneName ;
        }
        /**
         * Draws the note on the game screen and handles player input.
         * @param input The input object to check for user input.
         */
        abstract void Draw(Input input) ;
        /**
         * Checks if the specified action (e.g., Down, Up, Right, Left, Special) is triggered by player input.
         * @param input The input object to check for user input.
         * @param str The specific action to check (Down, Up, Right, Left, Special).
         * @return True if the action is triggered, false otherwise.
         */
        public boolean actionUp(Input input,String str)
        {
            if (input.wasReleased(Keys.DOWN) && str.equals("Down")) 
            {
                return true ;
            } 
            else if (input.wasReleased(Keys.UP)&& str.equals("Up")) 
            {
                return true ;
            } 
            else if(input.wasReleased(Keys.RIGHT)&& str.equals("Right"))
            {
                return true ;
            }
            else if(input.wasReleased(Keys.LEFT)&& str.equals("Left"))
            {
                return true ;
            }
            else if(input.wasReleased(Keys.SPACE)&& str.equals("Special"))
            {
                return true ; 
            }
            return false ;
        }
        /**
         * Checks if the specified action (e.g., Down, Up, Right, Left, Special) is initiated by player input.
         * @param input The input object to check for user input.
         * @param str The specific action to check (Down, Up, Right, Left, Special).
         * @return True if the action is initiated, false otherwise.
         */
        public boolean actionDown(Input input, String str)
        {
            if (input.wasPressed(Keys.DOWN) && str.equals("Down")) 
            {
                return true ; 
            } 
            else if (input.wasPressed(Keys.UP)&& str.equals("Up")) 
            {
                return true ;
            } 
            else if(input.wasPressed(Keys.RIGHT)&& str.equals("Right"))
            {
                return true ;
            }
            else if(input.wasPressed(Keys.LEFT)&& str.equals("Left"))
            {
                return true ; 
            }
            else if(input.wasPressed(Keys.SPACE)&& str.equals("Special"))
            {
                return true ; 
            }
            return false ;
        }
    }
    /**
     * The NormalNote class represents normal notes in the game.
     * It extends the Note class and handles the drawing and scoring logic for normal notes.
     */
    private class NormalNote extends Note
    {
        /**
         * Creates a new normal note with a specified type, frame, and lane name.
         * @param NoteType The type of the note (NORMAL, HOLD, SPECIAL, BOMB).
         * @param frame The frame at which the note appears in the game.
         * @param laneName The lane name of the note.
         */
        public NormalNote(NoteType NoteType, Integer frame, String laneName)
        {
            super(NoteType,frame,laneName);
            this.Y = 100 ;
            this.X = obm.lane.getX(laneName) ; 
        }
        /**
         * A flag indicating whether the normal note is alive and active.
         * A flag indicating whether the normal note is scored.
         */
        private boolean isAlive = true ; 
        private boolean isScored = false ; 

        /**
         * Draws the normal note on the game screen and handles player input for scoring.
         * @param input The input object to check for user input.
         */
        @Override
        public void Draw(Input input) 
        {
            if ( !this.isAlive) return ;
            String imageName  = "res/note" + this.laneName + ".png" ; 
            Image IMAGE = new Image(imageName);
            IMAGE.draw(this.X, this.Y);
            boolean nhan = actionDown(input,laneName) ; 
            String msg = ""; 
            if ( nhan == true)
            {
                double dis = Calculate(X,Y,obm.lane.getX(laneName),657) ; 
                msg = score.callScore(dis);
                score.updateCurrentMsg(msg);
                isScored = true ; 
            }
            if ( !isScored && this.Y > Window.getHeight()-1) 
            {
                msg = score.callScore(300);
                score.updateCurrentMsg(msg);
                isScored = true ; 
            }     
        }
    }
    /**
     * The HoldNote class represents hold notes in the game.
     * It extends the Note class and handles the drawing and scoring logic for hold notes.
     */
    private class HoldNote extends Note
    {
        /**
         * Creates a new hold note with a specified type, frame, and lane name.
         * @param NoteType The type of the note (NORMAL, HOLD, SPECIAL, BOMB).
         * @param frame The frame at which the note appears in the game.
         * @param laneName The lane name of the note.
         */
        public HoldNote(NoteType NoteType, Integer frame, String laneName)
        {
            super(NoteType,frame,laneName);
            this.X = obm.lane.getX(laneName) ; 
        }
        /**
         * A flag indicating whether the hold note is alive and active.
         * The distance pressed by the player on the hold note.
         * The distance released by the player on the hold note.
         * A flag indicating whether the hold note is scored.
         */
        private boolean isAlive = true ; 
        private double dPressed = 0 ; 
        private double dReleased = 0 ; 
        private boolean isScored = false ;
        /**
         * Draws the hold note on the game screen and handles player input for scoring.
         * @param input The input object to check for user input.
         */
        @Override
        public void Draw(Input input) 
        {
            if ( !this.isAlive) return ;
            String imageName = "res/holdNote" + this.laneName + ".png" ; 
            Image IMAGE = new Image(imageName);
            IMAGE.draw(this.X, this.Y);
            
            boolean nhan = false , tha = false;
            String msg = ""; 

            nhan = actionDown(input,laneName) ; 
            tha = actionUp(input,laneName) ; 
            
            if ( nhan == true )
            {
                dPressed = Calculate(X,Y+82,obm.lane.getX(laneName),657) ; 
            }
            if ( tha == true)
            {
                dReleased = Calculate(X,Y-82,obm.lane.getX(laneName),657) ; 
                double dis = Math.abs(dPressed-dReleased);
                msg = score.callScore(dis);
                score.updateCurrentMsg(msg);
                score.showMsg();
                isScored = true ; 
                dPressed = dReleased = 0 ; 
            }
            
            if ( !isScored && this.Y-82 > Window.getHeight()-1-82) 
            {
                msg = score.callScore(300);
                score.updateCurrentMsg(msg);
                score.showMsg();
            }
        }
    }
    /**
     * The BombNote class represents bomb notes in the game.
     * It extends the Note class and handles the drawing and interaction logic for bomb notes.
     */
    private class BombNote extends Note
    {
        /**
         * Creates a new bomb note with a specified type, frame, and lane name.
         * @param NoteType The type of the note (NORMAL, HOLD, SPECIAL, BOMB).
         * @param frame The frame at which the note appears in the game.
         * @param laneName The lane name of the note.
         */
        public BombNote(NoteType NoteType, Integer frame, String laneName)
        {
            super(NoteType,frame,laneName);
            this.X = obm.lane.getX(laneName) ; 
        }
        /**
         * A flag indicating whether the bomb note is active.
         */
        private boolean isActive = false ; 
        /**
         * Clears all notes in the specified list that share the same lane as the bomb note.
         * @param notes The list of notes to clear.
         */
        private void clearNote( List<Note> notes)
        {
            for(Note note : notes)
            if (frameCount >= note.frame && note.Y <= Window.getHeight() && note.laneName.equals(this.laneName) ) 
            {
                note.isCleared = true ; 
            }
        }
        /**
         * Draws the bomb note on the game screen and handles player input for interaction.
         * @param input The input object to check for user input.
         */
        @Override
        public void Draw(Input input) 
        {
            if ( this.isActive) return ;
            Image IMAGE = new Image("res/noteBomb.png");
            IMAGE.draw(this.X, this.Y);

            boolean nhan = actionDown(input,laneName) ; 
            if ( nhan == true)
            {
                double dis = Calculate(X,Y,obm.lane.getX(laneName),657) ; 
                if ( dis <= 50 )
                {
                    isActive = true ; 
                    clearNote(obm.notes) ; 
                    score.updateCurrentMsg("LANE CLEAR") ; 
                }
            } 
        }
    }
    /**
     * The SpecialNote class represents special notes in the game.
     * It extends the Note class and handles the drawing and interaction logic for special notes.
     */
    private class SpecialNote extends Note
    {
        /**
         * Creates a new special note with a specified type, frame, and lane name.
         * @param NoteType The type of the note (NORMAL, HOLD, SPECIAL, BOMB).
         * @param frame The frame at which the note appears in the game.
         * @param type The type of the special note (SpeedUp, SlowDown, 2x).
         * @param laneName The lane name of the note.
         */
        public SpecialNote(NoteType NoteType, Integer frame, String type, String laneName)
        {
            super(NoteType,frame,laneName);
            this.type = type;
            this.X = obm.lane.getX("Special") ; 
            this.Y = 100 ; 
        }
         /**
         * A flag indicating whether the special note is active.
         * The type of the special note (SpeedUp, SlowDown, 2x).
         */
        private boolean isActive = false ; 
        private String type = "";
        /**
         * Draws the special note on the game screen and handles player input for interaction.
         * @param input The input object to check for user input.
         */
        @Override
        public void Draw(Input input) 
        {
            if ( this.isActive) return ;
            if ( gameLevel == 1 ) return ;
            String imageName = "res/note" + this.type + ".png";
            Image IMAGE = new Image(imageName);
            IMAGE.draw(this.X, this.Y);

            boolean nhan = actionDown(input,"Special") ; 
            if ( nhan == true)
            {
                double dis = Calculate(X,Y,obm.lane.getX("Special"),657) ; 
                if ( dis <= 50 )
                {
                    isActive = true ; 
                    if ( type.equals("SpeedUp"))
                    {
                        score.updateCurrentMsg("SPEED UP") ; 
                        score.updateScore(15) ; 
                        screen.updateSpeed(1);
                    }   
                    else if ( type.equals("SlowDown"))
                    {
                        score.updateCurrentMsg("SLOW DOWN") ; 
                        score.updateScore(15) ; 
                        screen.updateSpeed(-1);
                    }
                    else if ( type.equals("2x"))
                    {
                        score.updateCurrentMsg("DOUBLE SCORE") ; 
                        score.updateScoreChange(2);
                    }
                }
            }     
        }
    }
    /**
     * The Arrow class represents the arrows fired by the guardian to defeat enemies.
     * It extends the Objects class and handles arrow movement, collision detection, and firing logic.
     */
    private class Arrow extends Objects  
    {
        private Arrow()
        {
        }
        /**
         * The rotation angle of the projectile.
         * The position of the projectile in the game space.
         * A flag indicating whether the arrow is fired.
         */
        private double projectileRotation = 0;
        private Vector2 projectilePosition = new Vector2( 800,600); 
        private boolean isFire = false ; 

        /**
         * Sets the direction of the arrow based on the guardian's position and the nearest enemy.
         * @param gu The guardian object.
         * @param e The nearest enemy object.
         */
        private void SetDirect(Guardian gu, Enemy e)
        {
            Vector2 guardianPosition = new Vector2(gu.X, gu.Y); 
            Vector2 enemyPosition = new Vector2(e.X,e.Y); 
            Vector2 direction = enemyPosition.sub(guardianPosition);
            projectileRotation = Math.atan2(direction.y, direction.x);
        }
        /**
         * Checks if the arrow collides with a specific enemy.
         * @param enemy The enemy object to check for collision.
         * @return True if the arrow collides with the enemy, false otherwise.
         */
        private boolean checkIntersertion(Enemy enemy )
        {
            Vector2 enemyPosition = new Vector2(enemy.X,enemy.Y); 
            double dx = enemyPosition.x - projectilePosition.x;
            double dy = enemyPosition.y - projectilePosition.y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance <= 62) return true ; 
            return false ; 
        }
        /**
         * Draws the arrow and handles its movement, collision detection, and enemy interaction.
         */
        public void Draw()
        {
            if ( !this.isFire )
            {
                Image IMAGE = new Image("res/arrow.png") ; 
                
                DrawOptions DrawOption = new DrawOptions();
                DrawOption.setRotation(projectileRotation);
                IMAGE.draw(this.projectilePosition.x,this.projectilePosition.y,DrawOption);
                double _speed = 6.0;
                double dx = _speed * Math.cos(projectileRotation);
                double dy = _speed * Math.sin(projectileRotation);
                projectilePosition = new Vector2(projectilePosition.x + dx, projectilePosition.y + dy);
                for( Enemy another : obm.enemies)
                    if ( !another.isFire && checkIntersertion(another))
                    {
                        this.isFire = true ; 
                        another.isFire = true ; 
                        break ;
                    }
                if (projectilePosition.x < 0 || projectilePosition.x > Window.getWidth()-1 ||
                projectilePosition.y < 0 || projectilePosition.y > Window.getHeight()-1) 
                {
                    this.isFire = true;
                }
            }
        }
    }
    /**
     * The Guardian class represents the guardian character in the game.
     * It extends the Objects class and manages the guardian's position, shooting logic, and interaction with enemies.
     */
    private class Guardian extends Objects
    {
        /**
         * Creates a new guardian object with a specified initial position.
         */
        private Guardian()
        {
            X = 800 ;
            Y = 600;
        }
        /**
         * Finds the nearest enemy from a list of enemies.
         * @param enemies The list of enemies to search for the nearest one.
         * @return The nearest enemy object, or null if no enemies are nearby.
         */
        public Enemy findNearestEnemy(List<Enemy> enemies)
        {
            Enemy nearestEnemy = null; 
            double dis = 10000; 
            for (Enemy enemy : enemies) 
                if ( !enemy.isFire)
                {
                    double tmp_dis = Calculate(enemy.X, enemy.Y,this.X,this.Y) ; 
                    if ( tmp_dis < dis )
                    {
                        dis = tmp_dis ; 
                        nearestEnemy = enemy ; 
                    }
                }
            return nearestEnemy; 
        } 
        /**
         * Draws the guardian on the game screen and handles shooting logic.
         * @param input The input object to check for user input.
         */
        public void Draw(Input input)
        {
            if ( gameLevel == 3)
            {
                if (input.wasPressed(Keys.LEFT_SHIFT)) 
                {
                    Enemy nearestEnemy = findNearestEnemy(obm.enemies) ;   
                    if ( nearestEnemy != null)
                    {
                        Arrow arrow = new Arrow(); 
                        arrow.SetDirect(this,nearestEnemy) ; 
                        obm.arrows.add(arrow);
                    }
                }
                Image IMAGE = new Image("res/guardian.png") ; 
                IMAGE.draw(this.X,this.Y);
            }
        } 
        
    }
    /**
     * The Enemy class represents the enemies in the game.
     * It extends the Objects class and manages enemy movement, collision detection, and interaction with notes.
     */
    private class Enemy extends Objects
    {
        /**
         * The direction of enemy movement.
         * A flag indicating whether the enemy is firing arrows.
         */
        int direct = 1 ; 
        private boolean isFire = false ;
        /**
         * Creates a new enemy object with a random initial position and movement direction.
         */
        private Enemy()
        {
            Random rand = new Random();
            this.X  = rand.nextInt(900) + 100;
            this.Y  = rand.nextInt(500) + 100;
            int randomNumber = rand.nextInt(2);
            direct = (randomNumber == 0) ? -1 : 1;
        }
        /**
         * Checks if the enemy is stealing notes from the specified list of notes.
         * @param notes The list of notes to check for stealing.
         */
        private void stealNote( List<Note> notes)
        {
            for(Note note : notes)
            if ( note.NoteType == NoteType.NORMAL )
                if ( ((NormalNote)note).isAlive)
                {
                    double dis = Calculate(note.X, note.Y, this.X, this.Y);
                    if ( dis <= 104 ) 
                    {
                        ((NormalNote)note).isAlive = false ; 
                    }
                }
        }
        /**
         * Draws the enemy on the game screen and handles enemy movement and note stealing logic.
         */
        public void Draw()
        {
            if ( gameLevel == 3 && !isFire)
            {
                Image IMAGE = new Image("res/enemy.png") ; 
                IMAGE.draw(this.X,this.Y);
                if ( this.X < 100 ) direct = 1 ; 
                else if ( this.X > 900 ) direct = -1 ; 
                this.X += direct ; 
                stealNote(obm.notes) ; 
            }
        } 
        
    }   
}
