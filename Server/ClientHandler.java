import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final Board board;
    private BufferedReader in;
    private PrintWriter out;    

    public ClientHandler(Socket clientSocket, Board board){
        this.clientSocket = clientSocket;
        this.board = board;
    }

    @Override
    public void run(){
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            sendWelcomeMessage();

            String line;
            while((line = in.readLine()) != null){
                System.out.println("Received from client: " + line);
                String response = processCommand(line.trim());
                out.println(response);
                System.out.println("Sent to client: " + response);
                
                if (line.trim().equalsIgnoreCase("DISCONNECT")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
           cleanupServer();
        }
    }
    
    private void sendWelcomeMessage(){
        out.println("BOARD " + board.getBoardWidth() + " " + board.getBoardHeight());
        out.println("NOTE " + board.getNoteWidth() + " " + board.getNoteHeight());

        String colours = String.join(" ", board.getValidColours());
        out.println("COLOURS " + colours);

        System.out.println("Sent welcome message to client.");
    }

    private String processCommand(String line){
        if(line.isEmpty()){
            return "ERROR Empty command";
        }
        String[] parts = line.split(" ", 2);
        String command = parts[0].toUpperCase();
        String args = parts.length > 1 ? parts[1] : "";

        switch(command){
            case "POST":
                return handlePost(args);
            case "GET":
                return handleGet(args);
            case "PIN":
                return handlePin(args);
            case "UNPIN":
                return handleUnpin(args);
            case "SHAKE":
                return handleShake();
            case "CLEAR":
                return handleClear();
            case "DISCONNECT":
                return "DISCONNECT";
            default:
                return "ERROR Unknown command " + command;
        }
    }

    private String handlePost(String args) {
        // Split into 4 parts: x, y, colour, message (message can contain spaces)
        String[] parts = args.trim().split("\\s+", 4);
        if (parts.length < 4) {
            return "ERROR Invalid Format: <x> <y> <colour> <message>";
        }
        try {
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            String colour = parts[2];
            String message = parts[3];
            board.post(x, y, colour, message);
            return "OK";
        } catch (NumberFormatException e) {
            return "ERROR Invalid coordinates (MUST BE INT)!";
        } catch (BoardError e) {
            return e.errorResponse();
        }
    }

    private String handleGet(String args){
        args = args.trim();

        if(args.equalsIgnoreCase("PINS")){
            List<Location> pins = board.getAllPins();
            StringBuilder response = new StringBuilder("COMPLETE! " + pins.size());
            for (Location loc:pins){
                response.append("\n").append(loc.toString());
            }
            return response.toString();
        }

        String colour = null;
        Integer containsX = null;
        Integer containsY = null;
        String refersTo = null;

        int refersToIndex = args.indexOf("refersTo=");
        if (refersToIndex != -1){
            refersTo = args.substring(refersToIndex + 9);
            args = args.substring(0, refersToIndex).trim();
        }

        int colourIndex = args.indexOf("colour=");
        if (colourIndex != -1){
            int start = colourIndex + 6;
            int end = args.indexOf(" ", start);
            if(end == -1) end = args.length();
            colour = args.substring(start, end);
        }

        int containsIndex = args.indexOf("contains=");
        if (containsIndex != -1){
            int start = containsIndex+9;
            int xEnd = args.indexOf(",", start);
            if (xEnd == -1) return "ERROR Invalid contains format, expected contains=<x>,<y>";
            try {
                String xStr = args.substring(start, xEnd);
                containsX = Integer.parseInt(xStr);
                
                int yStart = xEnd + 1;
                int yEnd = args.indexOf(" ", yStart);
                int nextEquals = args.indexOf("=", yStart);

                if(yEnd == -1 && nextEquals == -1) yEnd = args.length();
                else if (yEnd == -1) yEnd = nextEquals;
                else if (nextEquals != -1 && nextEquals < yEnd) yEnd = nextEquals;

                if (yStart >= args.length()) return "ERROR Invalid contains format, expected contains=<x>,<y>";
                String yStr = args.substring(yStart, yEnd).trim();
                containsY = Integer.parseInt(yStr);
                
            } catch (NumberFormatException e){
                return "ERROR Invalid contains coordinates (MUST BE INT)!";
            }
        }

        List<Note> results = board.get(colour, containsX, containsY, refersTo);
        StringBuilder response = new StringBuilder("COMPLETE! " + results.size());
        for(Note note: results){
            response.append("\n")
                    .append(note.getX()).append(" ")
                    .append(note.getY()).append(" ")
                    .append(board.getNoteWidth()).append(" ")
                    .append(board.getNoteHeight()).append(" ")
                    .append(note.getColour()).append(" ")
                    .append(note.getMessage());
        }
        return response.toString();
    }

    private String handlePin(String args) {
        String[] parts = args.trim().split("\\s+");
        if (parts.length != 2) {
            return "ERROR Invalid Format: <x> <y>";
        }
        try {
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int pinned = board.pin(x, y);  // your board.pin returns int
            return "OK " + pinned;
        } catch (NumberFormatException e) {
            return "ERROR Invalid coordinates (MUST BE INT)!";
        } catch (BoardError e) {
            return e.errorResponse();
        }
    }

    private String handleUnpin(String args) {
        String[] parts = args.trim().split("\\s+");
        if (parts.length != 2) {
            return "ERROR Invalid Format: <x> <y>";
        }
        try {
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int unpinned = board.unpin(x, y); // your board.unpin returns int
            return "OK " + unpinned;
        } catch (NumberFormatException e) {
            return "ERROR Invalid coordinates (MUST BE INT)!";
        } catch (BoardError e) {
            return e.errorResponse();
        }
    }

    private String handleShake(){
        board.shake();
        return "OK";
    }

    private String handleClear(){
        board.clear();
        return "OK";
    }

    private void cleanupServer(){
        try {
            if(in != null) in.close();
            if(out != null) out.close();
            if(clientSocket != null) clientSocket.close();
            System.out.println("Closed connection with client.");
        } catch (IOException e){
            System.err.println("Error closing client connection: " + e.getMessage());
        }
    }
}
