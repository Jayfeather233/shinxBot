package function.winmine;

import java.util.Random;

enum state {
    CONTINUE, LOSE, WIN, OUT_OF_BOUNDARY
}

public class Mine {
    final private int[][] map = new int[256][256];
    final private int[][] mask = new int[256][256];
    int col, row;

    public Mine(int diff) {
        if (diff == 0) {
            generate(9, 9, 10);
        } else if (diff == 1) {
            generate(16, 16, 40);
        } else if (diff == 2) {
            generate(16, 30, 99);
        } else {
            generate(9, 9, 10);
        }
    }

    private void generate(int col, int row, int mines) {
        if (mines > col * row) {
            mines = col * row;
        }
        Random R = new Random();
        this.col = col;
        this.row = row;
        for (int i = 0; i <= col + 1; i++) {
            for (int j = 0; j <= row + 1; j++) {
                map[i][j] = 0;
            }
        }
        int t = 0;
        while (t < mines) {
            int x = R.nextInt(col) + 1;
            int y = R.nextInt(row) + 1;
            if (map[x][y] == -1) continue;
            map[x][y] = -1;
            t++;
        }
        for (int i = 1; i <= col; i++) {
            for (int j = 1; j <= row; j++) {
                if (map[i][j] == 0) {
                    for (int ii = i - 1; ii <= i + 1; ii++) {
                        for (int jj = j - 1; jj <= j + 1; jj++) {
                            if (map[ii][jj] == -1) map[i][j]++;
                        }
                    }
                }
            }
        }
    }

    public state play(int col, int row) {
        if (col <= 0 || row <= 0 || this.col < col || this.row < row) {
            return state.OUT_OF_BOUNDARY;
        }
        if (mask[col][row] == 0) {
            if (map[col][row] == -1) {
                reveal();
                return state.LOSE;
            } else {
                expand(col, row);
                if (check()) {
                    reveal();
                    return state.WIN;
                } else {
                    return state.CONTINUE;
                }
            }
        } else {
            return state.CONTINUE;
        }
    }

    private void reveal() {
        for (int i = 1; i <= this.col; i++) {
            for (int j = 1; j <= this.row; j++) {
                if (map[i][j] == -1) {
                    mask[i][j] = 1;
                }
            }
        }
    }

    private boolean check() {
        for (int i = 1; i <= this.col; i++) {
            for (int j = 1; j <= this.row; j++) {
                if (map[i][j] != -1 && mask[i][j] == 0) return false;
            }
        }
        return true;
    }

    private void expand(int col, int row) {
        if (col <= 0 || row <= 0 || col > this.col || row > this.row) return;
        if (mask[col][row] == 1) return;
        mask[col][row] = 1;
        if (map[col][row] == 0) {
            for (int i = col - 1; i <= col + 1; i++) {
                for (int j = row - 1; j <= row + 1; j++) {
                    expand(i, j);
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= col; i++) {
            for (int j = 1; j <= row; j++) {
                if (mask[i][j] == 1) {
                    if (map[i][j] != -1) sb.append(" ").append(map[i][j]);
                    else sb.append(" X");
                } else sb.append(" #");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
