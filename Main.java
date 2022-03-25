import java.io.*;
import java.util.*;

public class Main {

//  00 01 02 | 03 04 05 | 06 07	08
//  09 10 11 | 12 13 14 | 15 16	17
//  18 19 20 | 21 22 23 | 24 25	26
//  ---------+----------+---------
//  27 28 29 | 30 31 32 | 33 34	35
//  36 37 38 | 39 40 41 | 42 43	44
//  45 46 47 | 48 49 50 | 51 52	53
//  ---------+----------+---------
//  54 55 56 | 57 58 59 | 60 61	62
//  63 64 65 | 66 67 68 | 69 70	71
//  72 73 74 | 75 76 77 | 78 79	80

//  00 01 02 | 09 10 11 | 18 19	20
//  03 04 05 | 12 13 14 | 21 22	23
//  06 07 08 | 15 16 17 | 24 25	26
//  ---------+----------+---------
//  27 28 29 | 36 37 38 | 45 46 47
//  30 31 32 | 39 40 41 | 48 49 50
//  33 34 35 | 42 43 44 | 51 52	53
//  ---------+----------+---------
//  54 55 56 | 63 64 65 | 72 73 74
//  57 58 59 | 66 67 68 | 75 76 77
//  60 61 62 | 69 70 71 | 78 79	80

	public static void main(String[] args) throws IOException {
		System.out.println("3..2..1...FIGHT!");
		System.out.println("NOTE: p1 wins/p2 wins/ties");
        int[] board = new int[81];
		fight(100);
	}

	// 22/75/3 where p1 is MCTS and p2 is Minimax
	// 100/0/0 where p1 is MCTS and p2 is random
	// 100/0/0 where p1 is Minimax and p2 is random
	public static long[] fight(int n) throws IOException {
		long[] data = new long[5];
		for (int i = 0; i < n; i++) {
			int[] board = new int[81];
			State s = new State(board, 1, 0, 0);
			int t1 = 0, t1max = 0, t2 = 0, t2max = 0;
			Scanner sc = new Scanner(System.in);
			while (s.done() == 0) {
				long startTime = System.nanoTime();

				MonteCarloTreeSearch m = new MonteCarloTreeSearch(s);
				int ret = m.next();
               // System.out.println("monte " + m.ble);
			    //System.out.println(ret);
				s = s.next(ret);

				long endTime = System.nanoTime();
				t1 += (endTime - startTime) / 1000000;
				t1max = (int) Math.max(t1max, (endTime - startTime) / 1000000);
				// s.printBoard("log "+i);
              //  s.printBoard();

                if(s.done() !=0)
                    break;
                    
				long startTime2 = System.nanoTime();

				Minimax mm = new Minimax(s);
				int ret2 = mm.next();
                //System.out.println("mini " + mm.ble);
			    //System.out.println(ret2);
				s = s.next(ret2);
				// s = s.next(s.random());
				//s = s.next(sc.nextInt());

				long endTime2 = System.nanoTime();
				t2 += (endTime2 - startTime2) / 1000000;
				t2max = (int) Math.max(t2max, (endTime2 - startTime2) / 1000000);
				// s.printBoard("log "+i);
				// System.out.print(t2 + "\t");
				// s.printBoard();
			}
			// System.out.println();
			data[s.done() - 1]++;
			data[3] += t1 / (s.numMoves / 2);
			data[4] += t2 / (s.numMoves / 2);
			System.out.println((i + 1) + " rounds: " + data[0] + "/" + data[1] + "/" + data[2]);
			System.out.println("\tp1: " + t1 / (s.numMoves / 2) + " avg ms/move, " + t1max + " max ms/move");
			System.out.println("\tp2: " + t2 / (s.numMoves / 2) + " avg ms/move, " + t2max + " max ms/move");

		}
		data[3] /= n;
		data[4] /= n;
		return data;
	}

	static class State {
		int[] board; // 0 - empty, 1 - player1, 2 - player2, 3 - filler
		int toMove;
		int lastMove;
		int numMoves;

		public State(int[] a, int b, int c, int d) {
			this.board = a;
			this.toMove = b;
			this.lastMove = c;
			this.numMoves = d;
		}

		// gets a list of moves (ignore the -1s)
		public int[] getMoves() {
			int mainCol = lastMove % 3;
			int mainRow = (lastMove / 9) % 3;
			int[] out = new int[] { 0, 1, 2, 9, 10, 11, 18, 19, 20 };
			boolean check = false;
			for (int i = 0; i < 9; i++) {
				out[i] = out[i] + 27 * mainRow + 3 * mainCol;
				if (board[out[i]] != 0) {
					out[i] = -1;
				} else {
					check = true;
				}
			}
			if (check) {
				return out;
			} else {
				out = new int[81];
				for (int i = 0; i < 81; i++) {
					if (board[i] == 0) {
						out[i] = i;
					} else {
						out[i] = -1;
					}
				}
				return out;
			}
		}

		public int random() {
			ArrayList<Integer> tot = new ArrayList<Integer>();
			int[] moves = getMoves();
			for (int i : moves) {
				if (i == -1)
					continue;
				tot.add(i);
			}
			return tot.get((int) (Math.random() * (tot.size() - 1)));
		}

		public State next(int n) {
			int[] tempBoard = new int[81];
			for (int i = 0; i < 81; i++) {
				tempBoard[i] = board[i];
			}
			tempBoard[n] = toMove;
			board[n] = toMove;
			int mainCol = (n % 9) / 3;
			int mainRow = n / 27;
			if (miniWin(mainRow, mainCol) != 0) {
				int[] out = new int[] { 0, 1, 2, 9, 10, 11, 18, 19, 20 };
				for (int i = 0; i < 9; i++) {
					out[i] = out[i] + 27 * mainRow + 3 * mainCol;
					if (tempBoard[out[i]] == 0) {
						tempBoard[out[i]] = 3;
					}
				}
			}
			State out = new State(tempBoard, toMove % 2 + 1, n, numMoves + 1);
			board[n] = 0;
			return out;
		}

		public int done() {
			int[] out = new int[9];
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					out[3 * i + j] = miniWin(i, j);
				}
			}
			// check each col
			for (int i = 0; i < 3; i++) {
				if (out[i] != 0 && out[i] == out[i + 3] && out[i + 3] == out[i + 6]) {
					return out[i];
				}
			}
			// check each row
			for (int i = 0; i < 9; i += 3) {
				if (out[i] != 0 && out[i] == out[i + 1] && out[i + 1] == out[i + 2]) {
					return out[i];
				}
			}
			// check each diag
			if (out[0] != 0 && out[0] == out[4] && out[4] == out[8]) {
				return out[0];
			}
			if (out[2] != 0 && out[2] == out[4] && out[4] == out[6]) {
				return out[2];
			}
			for (int i = 0; i < 81; i++) {
				if (board[i] == 0) {
					return 0;
				}
			}
			return 3;
		}

		public int miniWin(int row, int col) {
			int[] out = new int[] { 0, 1, 2, 9, 10, 11, 18, 19, 20 };
			for (int i = 0; i < 9; i++) {
				out[i] = out[i] + 27 * row + 3 * col;
			}
			// check each col
			for (int i = 0; i < 3; i++) {
				if (board[out[i]] != 0 && board[out[i]] != 3 && board[out[i]] == board[out[i + 3]]
						&& board[out[i + 3]] == board[out[i + 6]]) {
					return board[out[i]];
				}
			}
			// check each row
			for (int i = 0; i < 9; i += 3) {
				if (board[out[i]] != 0 && board[out[i]] != 3 && board[out[i]] == board[out[i + 1]]
						&& board[out[i + 1]] == board[out[i + 2]]) {
					return board[out[i]];
				}
			}
			// check each diag
			if (board[out[0]] != 0 && board[out[0]] != 3 && board[out[0]] == board[out[4]]
					&& board[out[4]] == board[out[8]]) {
				return board[out[0]];
			}
			if (board[out[2]] != 0 && board[out[2]] != 3 && board[out[2]] == board[out[4]]
					&& board[out[4]] == board[out[6]]) {
				return board[out[2]];
			}
            
			return 0;
		}

		public void printBoard() {
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					System.out.print(miniWin(i, j) + " ");
				}
				System.out.println();
			}
			System.out.println();
			for (int i = 0; i < 81; i++) {
				if (i != 0 && i % 9 == 0) {
					System.out.println();
				}
				System.out.print(board[i] + "\t");
			}
			System.out.println();
			System.out.println();
		}

		public void printBoard(String s) throws IOException {
			PrintWriter pw = new PrintWriter(new FileWriter(s + ".out", true));
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					pw.print(miniWin(i, j) + " ");
				}
				pw.println();
			}
			pw.println();
			for (int i = 0; i < 81; i++) {
				if (i != 0 && i % 9 == 0) {
					pw.println();
				}
				pw.print(board[i] + "\t");
			}
			pw.println();
			pw.println();
			pw.close();
		}
	}

	static class Point implements Comparable<Point> {
		int a;
		int b;

		Point(int a, int b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public int compareTo(Point o) {
			return this.a - o.a;
		}

	}
}