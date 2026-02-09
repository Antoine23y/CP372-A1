import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Board {
    private final int boardHeight;
    private final int boardWidth;
    private final int noteWidth;
    private final int noteHeight;
    private final Set<String> validColours;
    private final List<Note> notes;


    public Board(int boardWidth, int boardHeight, int noteWidth, int noteHeight, String[] colours){
        this.boardHeight = boardHeight;
        this.boardWidth = boardWidth;
        this.noteWidth = noteWidth;
        this.noteHeight = noteHeight;
        this.validColours = new HashSet<>(Arrays.asList(colours));
        this.notes = new ArrayList<>();
    }
    // Getters
    public int getBoardHeight(){
        return boardHeight;
    }
    public int getBoardWidth(){
        return boardWidth;
    }
    public int getNoteWidth(){
        return noteWidth;
    }
    public int getNoteHeight(){
        return noteHeight;
    }
    public Set<String> getValidColours(){
        return validColours;
    }

    public synchronized void post(int x, int y, String colour, String message) throws BoardError {
        Note newNote = new Note(x, y, noteWidth, noteHeight, colour.toLowerCase(), message);

        if(!newNote.isInBounds(boardWidth, boardHeight)) {
            throw new BoardError("OUT_OF_RANGE", "Out of Board Range!");
        }
        if(!validColours.contains(colour.toLowerCase())){
            throw new BoardError("COLOUR_NOT_SUPPORTED", "Chose out of: " + validColours);
        }

        for (Note existingNote: notes){
            if (newNote.overlaps(existingNote)){
                throw new BoardError("OVERLAP", "Note Overlaps with Existing Note!");
            }
        }
        notes.add(newNote);
    }
    public synchronized List<Note> get(String colour, Integer containsX, Integer containsY, String refersTo ){
        List<Note> results = new ArrayList<>();

        for (Note note: notes) {

            if(colour != null && !note.getColour().equalsIgnoreCase(colour)){
                continue;
            }
            if(containsX != null && containsY != null && !note.hasPoint(containsX, containsY)){
                continue;
            }
            if(refersTo != null && !note.getMessage().contains(refersTo)){
                continue;
            }
            results.add(note);
        }
        return results;
    }
    public synchronized int pin(int posX, int posY) throws BoardError {
        int pinned = 0;
        for (Note note : notes) {
            int x0 = note.getX();
            int y0 = note.getY();
            int x1 = x0 + noteWidth;
            int y1 = y0 + noteHeight;

            boolean inside = posX >= x0 && posX < x1 && posY >= y0 && posY < y1;

            if (inside) {
                note.addPin(posX, posY);
                pinned++;
            }
        }
        if (pinned == 0) {
            throw new BoardError("NOTE_NOT_FOUND", "No Note at that Position!");
        }
        return pinned;
    }
    public synchronized int unpin(int posX, int posY) throws BoardError {
        int unpinned = 0;

        for(Note note: notes){
            if (note.removePin(posX, posY)){
                unpinned++;
            }
        }
        if(unpinned == 0){
            throw new BoardError("NOTE_NOT_FOUND", "No Note at that Position!");
        }
        return unpinned;
    }

    public synchronized int shake(){
        int removedNotes = 0;
        List<Note> notesToRemove = new ArrayList<>();

        for (Note note:notes){
            if(!note.isPinned()){
                notesToRemove.add(note);
                removedNotes++;
            }
        }
        notes.removeAll(notesToRemove);
        return removedNotes;
    }
    public synchronized int clear(){
        int removedNotes = notes.size();
        notes.clear();
        return removedNotes;
    }
    public synchronized List<Location> getAllPins(){
        List<Location> allPins = new ArrayList<>();
        for (Note note: notes){
            allPins.addAll(note.getPins());
        }
        return allPins;
    }

}

