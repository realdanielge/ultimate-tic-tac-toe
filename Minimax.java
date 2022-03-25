public class Minimax extends Main {
	static State curr;

	public Minimax(State s) {
		curr = s;
	}

	public int next() {
//		if (curr.numMoves < 10) {
//			return curr.random();
//		}
		Point move = prune(curr, 1, 8, new Point(Integer.MIN_VALUE, -1), new Point(Integer.MAX_VALUE, -1));
		// System.out.println("#" + move.a);
		if (move.b == -1) {
			System.out.println(".......");
			return curr.random();
		} else {
			return move.b;
		}
	}

	private Point prune(State s, int p, int d, Point alpha, Point beta) {
		if (s.done() == curr.toMove) {
			return new Point(100000, 0);
		} else if (s.done() == curr.toMove % 2 + 1) {
			return new Point(-100000, 0);
		} else if (s.done() == 3) {
			return new Point(0, 0);
		}
		if (d == 0) {
			return new Point(eval(s), 0);
		}
		int[] possMoves = s.getMoves();
		if (p == 1) {
			Point out = new Point(Integer.MIN_VALUE, -1);
			for (int i : possMoves) {
				if (i == -1)
					continue;
				Point val = new Point(prune(s.next(i), p % 2 + 1, d - 1, alpha, beta).a, i);
				if (val.a > out.a) {
					out = val;
				}
				if (out.a > alpha.a) {
					alpha = out;
				}
				if (alpha.a >= beta.a) {
					break;
				}
			}
			return alpha;
		} else {
			Point out = new Point(Integer.MAX_VALUE, -1);
			for (int i : possMoves) {
				if (i == -1)
					continue;
				Point val = new Point(prune(s.next(i), p % 2 + 1, d - 1, alpha, beta).a, i);
				if (val.a < out.a) {
					out = val;
				}
				if (out.a < beta.a) {
					beta = out;
				}
				if (alpha.a >= beta.a) {
					break;
				}
			}
			return beta;
		}
	}

	private int eval(State s) {
		int m = curr.toMove, o = curr.toMove % 2 + 1;
		int pM = 0, pO = 0;
		int[] out = new int[9];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				out[3 * i + j] = s.miniWin(i, j);
				if (out[3 * i + j] == m) {
					pM += 10;
				} else if (out[3 * i + j] == o) {
					pO += 10;
				}
			}
		}
		// check cols
		for (int i = 0; i < 3; i++) {
			if (out[i] != o && out[i + 3] != o && out[i + 6] != o) {
				pM += 10;
			}
			if (out[i] != m && out[i + 3] != m && out[i + 3] != m) {
				pO += 10;
			}
		}
		// check rows
		for (int i = 0; i < 9; i += 3) {
			if (out[i] != o && out[i + 1] != o && out[i + 2] != o) {
				pM += 10;
			}
			if (out[i] != m && out[i + 1] != m && out[i + 2] != m) {
				pO += 10;
			}
		}
		// check diags
		if (out[0] != o && out[4] != o && out[8] != o) {
			pM += 10;
		}
		if (out[2] != m && out[4] != m && out[6] != m) {
			pO += 10;
		}
		return pM - pO;
	}
}
