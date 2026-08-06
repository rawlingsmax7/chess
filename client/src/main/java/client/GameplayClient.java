package client;

import chess.*;
import jakarta.websocket.DeploymentException;
import model.GameData;
import ui.DrawBoard;
import websocket.ServerMessageObserver;
import websocket.WebSocketFacade;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class GameplayClient extends LoginClient implements ServerMessageObserver {

    private final boolean whitePerspective;
    private ChessGame currentGame;
    private final int gameID;
    private final WebSocketFacade webSocketFacade;

    GameplayClient(ServerFacade facade, boolean whitePerspective, GameData game) throws DeploymentException, URISyntaxException, IOException {
        // login client has a server facade already
        super(facade);
        this.whitePerspective = whitePerspective;
        this.gameID = game.gameID();
        this.webSocketFacade = new WebSocketFacade(facade.getServerUrl(), this);

        webSocketFacade.send(new UserGameCommand(UserGameCommand.CommandType.CONNECT, facade.getAuthToken(), gameID));
    }

    private void printBoard() {
        PrintStream output = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        ChessBoard board = currentGame.getBoard();
        new DrawBoard(board, output).draw(whitePerspective);
    }

    private void printBoard(Collection<ChessPosition> highlights, ChessPosition selectedPosition) {
        PrintStream output = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        ChessBoard board = currentGame.getBoard();
        new DrawBoard(board, output).draw(whitePerspective, highlights, selectedPosition);
    }

    @Override
    public void notify(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case NOTIFICATION -> System.out.println(((NotificationMessage) message).getMessage());
            case ERROR -> System.out.println(((ErrorMessage) message).getMessage());
            case LOAD_GAME -> {
                currentGame = ((LoadGameMessage) message).getGame();
                printBoard();
            }
        }
    }

    @Override
    protected String prompt() {
        return "[IN GAME] >>> ";
    }

    @Override
    protected String evaluate(String input) {
        try {
            var tokens = input.split(" ");
            var command = (tokens.length > 0) ? tokens[0].toLowerCase() : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            // depending on the command do a different thing
            switch (command) {
                case "redraw":
                    printBoard();
                    return "";
                case "leave":
                    webSocketFacade.send(new UserGameCommand(UserGameCommand.CommandType.LEAVE,
                            facade.getAuthToken(), gameID));
                    return "quit";
                case "move":
                    if (params.length < 2) {
                        return "Expected: move <letter><number> <letter><number> ex: move e2 e4";
                    }
                    String startingPositionText = params[0].toLowerCase();
                    String endingPositionText = params[1].toLowerCase();
                    if (!isValidPosition(startingPositionText) || !isValidPosition(endingPositionText)) {
                        return "Squares must be between a1 and h8. Ex: move e2 e4";
                    }

                    ChessPiece.PieceType promotion = null;
                    if (params.length >= 3) {
                        promotion = switch (params[2].toLowerCase()) {
                            case "queen" -> ChessPiece.PieceType.QUEEN;
                            case "rook" -> ChessPiece.PieceType.ROOK;
                            case "bishop" -> ChessPiece.PieceType.BISHOP;
                            case "knight" -> ChessPiece.PieceType.KNIGHT;
                            default -> null;
                        };
                    }
                    // parse the parameters to a CHESS MOVE
                    ChessMove move =
                            new ChessMove(squareToPosition(startingPositionText.toLowerCase()),
                                    squareToPosition(endingPositionText.toLowerCase()), promotion);
                    webSocketFacade.send(new MakeMoveCommand(facade.getAuthToken(), gameID, move));
                    return "";
                case "resign":
                    System.out.println("Are you sure you want to resign? Type <yes/no>");
                    String userResponse = scanner.nextLine().toLowerCase();
                    if (userResponse.equals("yes")) {
                        webSocketFacade.send(new UserGameCommand(UserGameCommand.CommandType.RESIGN, facade.getAuthToken(), gameID));
                        return "";
                    } else {
                        return "Resign cancelled.";
                    }
                case "highlight":
                    if (params.length < 1) {
                        return "Expected: highlight <letter><number>  ex: highlight e4";
                    }
                    String squareToHighlight = params[0].toLowerCase();
                    if (!isValidPosition(squareToHighlight)) {
                        return "Squares must be between a1 and h8. Ex: highlight e4";
                    }

                    // make sure you can highlight that move
                    ChessPosition position = squareToPosition(squareToHighlight);
                    Collection<ChessMove> possibleMoves = currentGame.validMoves(position);

                    if (possibleMoves == null || possibleMoves.isEmpty()) {
                        return "No legal moves for that square.";
                    }
                    Set<ChessPosition> positionsToHighlight = new HashSet<>();
                    for (ChessMove possibleMove : possibleMoves) {
                        positionsToHighlight.add(possibleMove.getEndPosition());
                    }

                    printBoard(positionsToHighlight, position);
                    return "";

                default:
                    return printHelp();
            }
        } catch (Exception e) {
            return "Couldn't process request. Try again.";
        }
    }

    private ChessPosition squareToPosition(String square) {
        char file = square.charAt(0);
        int col = file - 'a' + 1;
        int row = square.charAt(1) - '0';
        return new ChessPosition(row, col);
    }

    private boolean isValidPosition(String square) {
        if (square.length() != 2) {
            return false;
        }
        char file = square.charAt(0);
        char rank = square.charAt(1);
        return (file >= 'a' && file <= 'h') && (rank >= '1' && rank <= '8');
    }

    private String printHelp() {
        return """
                Type one of the following options:\s
                "redraw"  ---> Redraw the board
                "leave"  ---> Leaves the game
                "move <letter><number> <letter><number> [promotion]" ---> Moves a chess piece
                "resign" ---> Resigns the game
                "highlight <letter><number>" ---> shows the possible moves for a piece
                """;
    }
}
