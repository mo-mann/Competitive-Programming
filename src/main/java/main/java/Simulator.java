package main.java;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;

public class Simulator {
    private static Random random = new Random();
    private static double[][] champions = new double[8][42];

    private static LongAdder[] scoreBoard = new LongAdder[champions.length];

    private static LongAdder[] plays = new LongAdder[champions.length];

    public static void main(String[] args) throws InterruptedException {
        System.out.println("HEURISTICS: ");
        for (int i = 0; i < scoreBoard.length; i++) {
            for (int j = 0; j < champions[0].length; j++) {
                champions[i][j] = Math.random();
            }
            System.out.println(Arrays.toString(champions[i]));
            scoreBoard[i] = new LongAdder();
            plays[i] = new LongAdder();
        }
        Arrays.fill(champions[scoreBoard.length - 1], 0, champions[0].length - 7, 0);
        Arrays.fill(champions[scoreBoard.length - 1], champions[0].length - 7, champions[0].length - 3, 1);
        Arrays.fill(champions[scoreBoard.length - 1], champions[0].length - 3, champions[0].length, 0);
        System.out.println(Arrays.toString(champions[scoreBoard.length - 1]));
        final int processors = Runtime.getRuntime().availableProcessors();
        System.out.println(processors);
        MinMax.TIME_OUT = 500;
        final ExecutorService executorService = Executors.newFixedThreadPool(processors);
        for (int i = 0; i < champions.length; i++) {
            for (int j = 0; j < i; j++) {
                int finalJ = j;
                int finalI = i;
                executorService.submit(() -> {
                    try {
                        playGame(new Board(new int[5][5][2]), getHeuristicFunction(finalI),
                                 getHeuristicFunction(finalJ), finalI, finalJ);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                executorService.submit(() -> {
                    try {
                        playGame(new Board(new int[5][5][2]), getHeuristicFunction(finalJ),
                                 getHeuristicFunction(finalI), finalJ, finalI);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
        System.out.println(executorService.awaitTermination(1, TimeUnit.HOURS));
    }

    private static Function<int[], Integer> getHeuristicFunction(int player) {
        final double randoms[] = champions[player];
        return (vals) -> {
            double result = 0;
            final double[] features = new double[vals.length * vals.length + vals.length];
            int length = 0;
            for (int val : vals) {
                for (int content : vals) {
                    features[length++] = val * content;
                }
            }
            for (int val : vals) {
                features[length++] = val;
            }
            for (int i = 0; i < features.length; i++) {
                result += features[i] * randoms[i];
            }
            return (int) result;
        };
    }

    private static void playGame(final Board board,
                                 final Function<int[], Integer> first,
                                 final Function<int[], Integer> second, int player_1, int player_2) {
        int count = 0;
        try {
            while (true) {
                board.heuristicEval = first;
                playMove(board, new MinMax(), 1, count);
                count++;
                board.heuristicEval = second;
                playMove(board, new MinMax(), 2, count);
                count++;
            }
        } catch (Exception ignore) {
            if (count % 2 == 0) {
                scoreBoard[player_2].increment();
                System.out.println(player_2 + " BEAT " + player_1);
            } else {
                scoreBoard[player_1].increment();
                System.out.println(player_1 + " BEAT " + player_2);
            }
            plays[player_1].increment();
            plays[player_2].increment();
            System.out.println("\n" + Arrays.toString(scoreBoard) + Arrays.toString(plays));
        }
    }

    private static void playMove(final Board board, final MinMax minMax, final int player, int count) {
        final String[] split = minMax.iterativeSearchForBestMove(board.board, player).split(" ");
        board.play(Board.ALL_MOVES[player][Integer.parseInt(split[0])][Integer.parseInt(split[1])]);
        //System.out.println("MOVE: " + count + " " + player);
        System.out.println(Arrays.toString(split) + " " + count);
//        if (Math.abs(minMax.eval - MinMax.MAX_VALUE) < 100) {
//            System.out.println(board + (player == 1 ? "true);" : "false);"));
//        }
//        System.out.println(minMax.eval + " " + minMax.depth + " " + minMax.moves);
    }
}