package main;

public class NextOutputThread implements Runnable {
    final String s;

    public NextOutputThread(String input) {
        s = input;
    }

    @Override
    public void run() {
        Main.setNextOutput(s);
    }
}
