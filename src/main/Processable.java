package main;

public interface Processable {
    void process(String message_type, String message, long group_id, long user_id, int message_id);

    boolean check(String message_type, String message, long group_id, long user_id);

    String help();
}
