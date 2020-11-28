package org.example;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Scanner;

public class Main {

    private static int process;
    private static InetAddress group;
    private static int port;
    private static boolean isServer = false;
    private static boolean isRunning = true;
    private static MulticastSocket socket;

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Invalid arguments");
            System.exit(1);
        }

        process = Integer.parseInt(args[0]);
        port = 8888;
        socket = new MulticastSocket(port);
        group = InetAddress.getByName("224.0.0.1");
        socket.joinGroup(group);
        String nickname = args[1];

        if (nickname.equals("instanceserver")) {
            isServer = true;
        }

        while (isRunning) {
            System.out.println("Is Running");
            if (isServer) {
                //server
                server(socket);
            } else {
                //client
                client(socket);
            }
        }

        socket.leaveGroup(group);
        socket.close();
    }

    private static void client(MulticastSocket socket) {
        byte[] response = new byte[2048];
        DatagramPacket res = new DatagramPacket(response, response.length);
        try{
            socket.receive(res);
            String data = new String(res.getData(), 0, res.getLength());
            System.out.println(data);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void server(MulticastSocket socket) {
        ReadFile readFile = new ReadFile();
        readFile.readFile();
        startServer();
        Scanner in = new Scanner(System.in);
        String message = in.nextLine();
        try {
            if (message.equals("start")) {
                byte[] start;
                start = message.getBytes();
                DatagramPacket udp = new DatagramPacket(start, start.length, group, port);
                socket.send(udp);
                System.out.println("Mensagem enviada");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void startServer() {
        System.out.println("Starting server");
        System.out.println("Esperando clients se registrarem");
    }

}
