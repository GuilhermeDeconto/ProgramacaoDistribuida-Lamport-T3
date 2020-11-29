package org.example;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.Scanner;

public class Main {

    private static int processId;
    private static InetAddress group;
    private static int port;
    private static boolean isServer = false;
    private static boolean isRunning = true;
    private static MulticastSocket socket;
    private static Calendar timestemp;
    private static int lamportClock = 0;
    private static boolean start = true;
    public static ArrayList<Process> process = new ArrayList<Process>();

    public static void main(String[] args) throws IOException, InterruptedException {

        if (args.length < 1) {
            System.out.println("Invalid arguments");
            System.exit(1);
        }

        processId = Integer.parseInt(args[0]);
        port = 8888;
        socket = new MulticastSocket(port);
        group = InetAddress.getByName("224.0.0.1");
        socket.joinGroup(group);
        String nickname = args[1];

        if (nickname.equals("instanceserver")) {
            isServer = true;
        }

        ReadFile readFile = new ReadFile();
        readFile.readFile();
        process = readFile.getListOfProcess();

        while (isRunning) {
            System.out.println("Is Running ");
            timestemp = Calendar.getInstance();
            System.out.println("Data1 em milesegundos: " + timestemp.getTimeInMillis());
            if (isServer) {
                //server
                if(!start)
                    serverStart(socket);
            } else {
                //client
                if(!start) {
                    clientStart(socket);
                    listening();
                }else{
                    Random r = new Random();
                    int randomNumber = r.nextInt(10);
                    double chance = process.get(processId - 1).chance * 10;
                    System.out.println("randomNumber " + randomNumber + " chance " + chance);
                    if(randomNumber < chance){
                        send();
                    }else{
                        local();
                    }


                }
            }
            Random r2 = new Random();
            int randomNumber = r2.nextInt(6);
            double sleepTime = 0.5 + 0.1 * (randomNumber);
            long sleepTimeMilli = (new Double(sleepTime * 1000)).longValue();
            Thread.sleep(sleepTimeMilli);
            System.out.println("sleepTime " + sleepTime);

        }
        socket.leaveGroup(group);
        socket.close();
    }

    private static void send() {
        System.out.println("send");
        lamportClock++;

        int randomProcess = processId - 1;
        while (randomProcess == processId - 1) {
            Random r = new Random();
            randomProcess = r.nextInt(process.size());
        }

        System.out.println(timestemp.getTimeInMillis() + " " + processId + " " + lamportClock + "" + processId + " s " + (randomProcess + 1));
    }

    private static void local() {
        lamportClock++;
        System.out.println(timestemp.getTimeInMillis() + " " + processId + " " + lamportClock + "" + processId + " l");
    }

    private static void listening() {
        Thread thread1 = new Thread() {
            @Override
            public void run() {
                while (true){
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
            }
        };

        thread1.start();

        //try {thread1.join();
        //} catch (InterruptedException e) {e.printStackTrace();}
    }

    private static void clientStart(MulticastSocket socket) {
        byte[] response = new byte[2048];
        DatagramPacket res = new DatagramPacket(response, response.length);
        try{
            System.out.println("try ");
            socket.receive(res);
            String data = new String(res.getData(), 0, res.getLength());
            System.out.println(data); //start
        } catch (Exception e) {
            System.out.println("catch ");
            e.printStackTrace();
        }
        start = true;
    }

    public static void serverStart(MulticastSocket socket) {
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
        start = true;
    }

    public static void startServer() {
        System.out.println("Starting server");
        System.out.println("Esperando clients se registrarem");
    }

}
