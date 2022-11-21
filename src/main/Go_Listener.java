package main;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static main.Main.receivePort;

class Proc extends Thread {
    private final Socket socket;

    public Proc(Socket socket) {
        this.socket = socket;
        start();
    }

    @Override
    public void run() {
        try {
            BufferedReader in;
            PrintWriter out;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new DataOutputStream(socket.getOutputStream()), true);
            String str;
            while (true) {
                str = in.readLine();
                //System.out.println("Receive: " + str);
                if (str.length() >= 1 && str.charAt(0) == '{') {
                    NextOutputThread not = new NextOutputThread(str);
                    new Thread(not).start();
                    break;
                }
            }
            //System.out.println(socket + ", session closing....");

            out.write(
                    "HTTP/1.1 200 OK\n"+
                    "Content-Length: 0\n"+
                    "Content-Type: application/json\n"+ "\n"
                    );
            //默认不进行快速操作，返回空的body

            out.flush();
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class Go_Listener implements Runnable {
    @Override
    public void run() {
        try {
            ServerSocket serverSock = new ServerSocket(receivePort);
            while (true) {//端口监听，多线程操作
                Socket socket = serverSock.accept();
                //System.out.println("Accepted.");
                new Proc(socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}