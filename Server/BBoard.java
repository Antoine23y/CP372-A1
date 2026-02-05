import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class BBoard {
    public static void main(String[] args){
        if (args.length < 6){
            System.err.println("Usage: java BBoard <port> <boardWidth> <boardHeight> <noteWidth> <noteHeight> <colours>");
            System.exit(1);
        }
        int port, boardWidth, boardHeight, noteWidth, noteHeight;
        try {
            port = Integer.parseInt(args[0]);
            boardWidth = Integer.parseInt(args[1]);
            boardHeight = Integer.parseInt(args[2]);
            noteWidth = Integer.parseInt(args[3]);
            noteHeight = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            System.err.println("Error: Port, board dimensions, and note dimensions must be integers.");
            System.exit(1);
            return;
        }
        String[] colours = new String[args.length - 5];
        for (int x = 5; x <args.length; x++){
            colours[x-5] = args[x].toLowerCase();
        }

        Board board = new Board(boardWidth, boardHeight, noteWidth, noteHeight, colours);
        System.out.println("Board Created: " + boardWidth + "x" + boardHeight + ", Note Size: " + noteWidth + "x" + noteHeight + ", Colours: " + String.join(", ", colours));
        try (ServerSocket serverSocket = new ServerSocket(port)){
            System.out.println("Starting on Port " + port + "...");

            while (true){
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                ClientHandler handler = new ClientHandler(clientSocket, board);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
            System.exit(1);
        }
    }
}