import java.util.HashSet;
import java.util.Set;

public class Note {
    private final int posX;
    private final int posY;
    private final int noteWidth;
    private final int noteHeight;
    private final String noteColour;
    private final String message;


    private final Set<Location> pins;

    public Note(int posX, int posY, int noteHeight, int noteWidth, String noteColour, String message){
        this.posX = posX;
        this.posY = posY;
        this.noteHeight = noteHeight;
        this.noteWidth = noteWidth;
        this.noteColour = noteColour;
        this.message = message;
        this.pins = new HashSet<>();
    }
    //getters

    public int getX() {
        return posX;
    }
    public int getY(){
        return posY;
    }
    public String getColour(){
        return noteColour;
    }
    public String getMessage(){
        return message;
    }
    public Set<Location> getPins(){
        return pins;
    }



    public boolean hasPoint(int pinX, int pinY){
        return pinX >= posX && pinX < posX + noteWidth && pinY >= posY && pinY < posY + noteHeight;
    }

    public boolean isInBounds(int boardWidth, int boardHeight){
        return posX >= 0 && posY >= 0 && (posX + noteWidth) <= boardWidth && (posY + noteHeight) <= boardHeight;
    }

    public boolean overlaps(Note other){
        return this.posX == other.posX && this.posY == other.posY && this.noteWidth == other.noteWidth && this.noteHeight == other.noteHeight;
    }

    public boolean isPinned() {
        return !pins.isEmpty();
    }

    public void addPin(int posX, int posY){
        pins.add(new Location(posX, posY));
    }

    public boolean removePin(int posX, int posY){
        return pins.remove(new Location(posX, posY));
    }

    public boolean containsPoint(int posX, int posY){
        return pins.contains(new Location(posX, posY));
    }

}