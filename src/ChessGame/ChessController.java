package ChessGame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class ChessController implements ChessDelegate, ActionListener {
    private String socketServerAddress = "192.168.90.135";
    private int port = 50000;

    private ChessModel chessModel = new ChessModel();

    private JFrame frame;
    private ChessView chessBoardPanel;
    private JButton resetBtn;
    private JButton newGameBtn;
    private JButton joinGameBtn;

    private String playerName;
    private String opponentName;
    private JLabel playersDetailLabel;
    private JLabel timerLabel;

    private int gameTimeMinutes = 30; // Default game time in minutes
    private int gameTimeSeconds = gameTimeMinutes * 60; // Total game time in seconds
    private boolean isTimerRunning = false;

    private ServerSocket listener;
    private Socket socket;
    private PrintWriter printWriter;
    private boolean isResetBtnPressed = false;

    ChessController() {
        chessModel.reset();

        frame = new JFrame("Chess");
        frame.setSize(500, 550);
        frame.setLayout(new BorderLayout());

        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.CENTER));
        playersDetailLabel = new JLabel();
        playersDetailLabel.setForeground(new Color(0x1E0342));
        playersDetailLabel.setFont(new Font("Courier", Font.BOLD, 16));
        navBar.add(playersDetailLabel);
        timerLabel = new JLabel("Timer: " + formatTime(gameTimeMinutes, 0), SwingConstants.CENTER);
        timerLabel.setFont(new Font("Courier", Font.BOLD, 16));
        timerLabel.setVisible(false);
        navBar.add(timerLabel);

        frame.add(navBar, BorderLayout.NORTH);

        chessBoardPanel = new ChessView(this);
        frame.add(chessBoardPanel, BorderLayout.CENTER);

        var buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        resetBtn = new JButton("Reset");
        resetBtn.addActionListener(this);
        buttonsPanel.add(resetBtn);

        newGameBtn = new JButton("Start New Game");
        buttonsPanel.add(newGameBtn);
        newGameBtn.addActionListener(this);

        joinGameBtn = new JButton("Join Game");
        buttonsPanel.add(joinGameBtn);
        joinGameBtn.addActionListener(this);

        frame.add(buttonsPanel, BorderLayout.PAGE_END);

        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                if (printWriter != null)
                    printWriter.close();
                try {
                    if (listener != null)
                        listener.close();
                    if (socket != null)
                        socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        new ChessController();
    }

    @Override
    public ChessPiece pieceAt(int col, int row) {
        return chessModel.pieceAt(col, row);
    }

    @Override
    public void movePiece(int fromCol, int fromRow, int toCol, int toRow) {
        chessModel.movePiece(fromCol, fromRow, toCol, toRow);
        chessBoardPanel.repaint();
        if (printWriter != null) {
            printWriter.println(fromCol + "," + fromRow + "," + toCol + "," + toRow);
        }
    }

    private void startTimer() {
        Executors.newFixedThreadPool(1).execute(() -> {
            int remainingTime = gameTimeSeconds;
            while (remainingTime > 0) {
                if (isResetBtnPressed) {
                    return;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                remainingTime--;
                final int minutes = remainingTime / 60;
                final int seconds = remainingTime % 60;
                SwingUtilities.invokeLater(() -> {
                    if (!isTimerRunning) {
                        return;
                    }
                    timerLabel.setText("Timer: " + formatTime(minutes, seconds));
                    if (minutes == 3) {
                        timerLabel.setForeground(new Color(0xFF204E));
                    }
                });
            }
            resetGame();
        });
    }

    private String formatTime(int minutes, int seconds) {
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void resetGame() {
        isResetBtnPressed = true;

        // Stop and reset the timer
        timerLabel.setVisible(false);
        isTimerRunning = false;

        // Set the player detail label and Title of the Frame
        frame.setTitle("Chess Game");
        playersDetailLabel.setText("");

        // Close the server socket and client socket
        try {
            if (listener != null) {
                listener.close();
            }
            if (socket != null) {
                if (socket.isConnected()) {
                    JOptionPane.showMessageDialog(frame, "Other opponent has left the game");
                }
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Enable the new game and join game buttons
        newGameBtn.setEnabled(true);
        joinGameBtn.setEnabled(true);

        // Reset the chess board
        chessModel.reset();
        chessBoardPanel.repaint();
    }

    private void receiveMove(Scanner scanner) {
        while (scanner.hasNextLine() || (listener != null && listener.isClosed())) {
            if (isResetBtnPressed) {
                return;
            }

            var moveStr = scanner.nextLine();
            System.out.println("chess move received: " + moveStr);
            var moveStrArr = moveStr.split(",");
            var fromCol = Integer.parseInt(moveStrArr[0]);
            var fromRow = Integer.parseInt(moveStrArr[1]);
            var toCol = Integer.parseInt(moveStrArr[2]);
            var toRow = Integer.parseInt(moveStrArr[3]);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    chessModel.movePiece(7 - fromCol, 7 - fromRow, 7 - toCol,
                            7 - toRow);
                    chessBoardPanel.repaint();
                    System.out.println(chessModel);
                }
            });
        }
        resetGame();

    }

    private void startNewGameServer() {
        isResetBtnPressed = false;
        chessModel.setBlackPlayerBoard(false);
        Executors.newFixedThreadPool(1).execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // start the server
                    listener = new ServerSocket(port);

                    // wait for the client to connect
                    System.out.println("server is listening on port " + port);
                    playersDetailLabel.setText("Waiting for opponent is connnet...");

                    // accept the client connection
                    socket = listener.accept();
                    System.out.println("connected from " + socket.getInetAddress());
                    JOptionPane.showMessageDialog(frame, "new player has joined the game.");

                    // get the client input and output streams
                    printWriter = new PrintWriter(socket.getOutputStream(), true);
                    var scanner = new Scanner(socket.getInputStream());

                    // send player name to the client
                    printWriter.println(playerName);
                    // receive opponent name from the client
                    opponentName = scanner.nextLine();
                    playersDetailLabel.setText(playerName + " vs " + opponentName + "        ");

                    // start the timer countdown
                    timerLabel.setVisible(true);
                    isTimerRunning = true;
                    startTimer();
                    receiveMove(scanner);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    private void joinExsistingGameServer() {
        chessModel.setBlackPlayerBoard(true);
        chessBoardPanel.repaint();
        Executors.newFixedThreadPool(1).execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // connect to the server
                    socket = new Socket(socketServerAddress, port);
                    isResetBtnPressed = false;
                    System.out.println("client connected to port " + port);
                    JOptionPane.showMessageDialog(frame, "you have joined the game.");
                    var scanner = new Scanner(socket.getInputStream());
                    printWriter = new PrintWriter(socket.getOutputStream(), true);

                    Executors.newFixedThreadPool(1).execute(new Runnable() {
                        @Override
                        public void run() {
                            // receive opponent name from the server
                            opponentName = scanner.nextLine();
                            playersDetailLabel.setText(playerName + " vs " + opponentName + "        ");

                            // send player name to the server
                            printWriter.println(playerName);

                            // start the timer countdown
                            timerLabel.setVisible(true);
                            isTimerRunning = true;
                            startTimer();
                            receiveMove(scanner);
                        }
                    });
                } catch (IOException e) {
                    playersDetailLabel.setText("Finding opponent...");
                    System.out.println("waiting for server to start...");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    if (isResetBtnPressed) {
                        return;
                    } else {
                        joinExsistingGameServer();
                    }
                }
            }

        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == resetBtn) {
            // Ask for confirmation before resetting the game
            int option = JOptionPane.showConfirmDialog(frame, "Do you want to reset the game?", "Reset Game",
                    JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                resetGame();
            }

        } else if (e.getSource() == newGameBtn) {

            newGameBtn.setEnabled(false);
            joinGameBtn.setEnabled(false);
            frame.setTitle("Start the New Chess Game");
            promptPlayerNameAndGameType();
            startNewGameServer();
            // JOptionPane.showMessageDialog(frame, "listening on port " + Port);
        } else if (e.getSource() == joinGameBtn) {
            isResetBtnPressed= false;
            newGameBtn.setEnabled(false);
            joinGameBtn.setEnabled(false);
            frame.setTitle("Join the Chess Game");
            promptPlayerNameAndGameType();
            joinExsistingGameServer();
            // JOptionPane.showMessageDialog(frame, "connected to port " + Port);
        }
    }

    private void promptPlayerNameAndGameType() {
        if (playerName != null && !playerName.isEmpty()) {
            return;
        }
        // Get the player's name
        String playerName = JOptionPane.showInputDialog(frame, "Enter your name (5 chars):", "Player Name",
                JOptionPane.PLAIN_MESSAGE);

        // If player name is not empty, prompt for game type
        if (playerName != null && !playerName.isEmpty()) {

            // remove leading and trailing white spaces
            playerName.trim();
            if (playerName.length() >= 5) {
                playerName = playerName.substring(0, 6);
            }
            // Update the player label
            this.playerName = playerName;
        } else {
            // If player name is empty or null, prompt again
            promptPlayerNameAndGameType();
        }
    }
}