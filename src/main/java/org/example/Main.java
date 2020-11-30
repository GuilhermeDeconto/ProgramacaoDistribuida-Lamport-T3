package org.example;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.*;
import java.util.logging.Handler;

public class Main {

    private static int processId;
    private static InetAddress group;
    private static int port;
    private static boolean isServer = false;
    private static boolean isRunning = true;
    private static MulticastSocket socket;
    private static Calendar timestemp;
    private static int lamportClock = 0;
    private static boolean start = false;
    public static ArrayList<Process> process = new ArrayList<Process>();
    public static HashMap<Integer, Process> connectedProcess = new HashMap<Integer, Process>();
    public static String actualMessage;

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
        if(isServer)
            listening();


        int actionCounter = 0;

        while (isRunning) {
            //System.out.println("Is Running ");
            //   timestemp = Calendar.getInstance();
            //   System.out.println(timestemp.getTimeInMillis());
            if (isServer) {

                //server
                if(!start){
                    if(connectedProcess.size()==process.size()) {//recebeu ping de todos
                        Thread.sleep(5000);
                        serverStart(socket);
                    }
                }
            } else {
                //client
                if(!start) {
                    clientPing(socket);
                    clientStart(socket);

                    if(start)

                        listening();
                }else{

                    Random r = new Random();
                    int randomNumber = r.nextInt(10);
                    double chance = process.get(processId - 1).chance * 10;

                    if(randomNumber < chance){
                        send();
                    }else{
                        local();
                    }
                }
                if(lamportClock>=10) {
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            System.out.println("Processo encerrado por 100 acoes");
                            System.exit(1);
                        }
                    }, 100);
                }
            }
            Random r2 = new Random();
            int randomNumber = r2.nextInt(6);
            double sleepTime = 0.5 + 0.1 * (randomNumber);
            long sleepTimeMilli = (new Double(sleepTime * 1000)).longValue();
            Thread.sleep(sleepTimeMilli);

        }
        socket.leaveGroup(group);
        socket.close();
    }

    private static void send() {
        //System.out.println("send");
        lamportClock++;

        int randomProcess = processId - 1;
        while (randomProcess == processId - 1) {
            Random r = new Random();
            randomProcess = r.nextInt(process.size());
        }

        System.out.println(timestemp.getTimeInMillis() + " " + processId + " " + lamportClock + "" + processId + " s " + (randomProcess + 1));
        String message = "s " + processId + " " + (randomProcess + 1) + " " + lamportClock;
        try {
            byte[] start;
            start = message.getBytes();
            DatagramPacket udp = new DatagramPacket(start, start.length, group, port);
            socket.send(udp);
        } catch (Exception e) {
            //e.printStackTrace();
        }
        waitResponse(randomProcess + 1);

    }

    private static void waitResponse(int receiver) {

        Thread thread1 = new Thread() {
            @Override
            public void run() {
                int counter = 0;
                while (counter < 10){
                    try {

                        Thread.sleep(1000);
                        counter++;
                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                    }
                }
                timestemp = Calendar.getInstance();
                System.out.println();
                System.out.println("Processo encerrado pela ausencia de resposta de "+receiver+" tempo: "+ timestemp.getTimeInMillis());
                System.exit(1);
            }
        };

        Thread thread2 = new Thread() {
            @Override
            public void run() {

                while (true){

                    try{
                        String data = actualMessage;
                        if(data.split(" ")[0].equals("r")){
                            if(data.split(" ")[2].equals(processId + "")){// resposta eh para mim
                                if(data.split(" ")[1].equals(receiver + "")){// resposta eh para quem eu enviei
                                    System.out.println("1Retorno " + actualMessage);
                                    thread1.interrupt();
                                    break;
                                }
                            }
                        }

                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                }
            }
        };

        thread1.start();
        thread2.start();

        //try {thread1.join();
        //} catch (InterruptedException e) {e.printStackTrace();}


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
                        actualMessage= data;
                        //   System.out.println(data);
                        if(isServer){
                            if (data.contains("ping:")) {
                                int newProcess = Integer.parseInt(data.split(":")[1]);
                                connectedProcess.put(newProcess - 1, process.get(newProcess - 1));
                            }
                            //if (data.contains("client:")) {
                            //  int newProcess = Integer.parseInt(data.split(":")[1]);
                            //connectedProcess.add(newProcess - 1, process.get(newProcess - 1));
                            //}

                        }else{//client
                            if(data.split(" ")[0].equals("s")){
                                if(data.split(" ")[2].equals(processId + "")){//Recebimento de mensagem
                                    Integer largerLamport = Math.max(lamportClock, Integer.parseInt(data.split(" ")[3]));
                                    lamportClock = largerLamport++;
                                    System.out.println(timestemp.getTimeInMillis() + " " + processId + " " + lamportClock + "" + processId + " r " + data.split(" ")[1] + " " + data.split(" ")[3]);

                                    String message = "r " + processId + " " + data.split(" ")[1];
                                    try {
                                        byte[] start;
                                        start = message.getBytes();
                                        DatagramPacket udp = new DatagramPacket(start, start.length, group, port);
                                        socket.send(udp);
                                    } catch (Exception e) {
                                        //e.printStackTrace();
                                    }
                                }
                            }else if(data.split(" ")[0].equals("r")){
                                if(data.split(" ")[2].equals(processId + "")){// resposta eh para mim
                                    actualMessage= data;
                                    System.out.println("Retorno " + data);

                                }
                            }
                        }



                    } catch (Exception e) {
                        //e.printStackTrace();
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
            //   System.out.println("try ");
            socket.receive(res);

            String data = new String(res.getData(), 0, res.getLength());
            ///System.out.println(data); //start

            if(data.equals("start")){
                timestemp = Calendar.getInstance();
                System.out.println(processId + " Start " +timestemp.getTimeInMillis());
                byte[] mesResponse;
                String message = "client:" + processId;
                mesResponse = message.getBytes();
                //DatagramPacket udp = new DatagramPacket(mesResponse, mesResponse.length, group, port);
                //socket.send(udp);
                start = true;
            }
        } catch (Exception e) {

            //e.printStackTrace();
        }
    }

    public static void clientPing(MulticastSocket socket) {
        String message = "ping:" + processId;
        try {
            byte[] start;
            start = message.getBytes();
            DatagramPacket udp = new DatagramPacket(start, start.length, group, port);
            socket.send(udp);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public static void serverStart(MulticastSocket socket) {
        startServer();
        String message = "start";
        try {
            byte[] start;
            start = message.getBytes();
            DatagramPacket udp = new DatagramPacket(start, start.length, group, port);
            socket.send(udp);
            System.out.println("Start");
        } catch (Exception e) {
            //e.printStackTrace();
        }
        start = true;
    }

    public static void startServer() {
        System.out.println("Starting server");
        System.out.println("Esperando clients se registrarem");
    }

}
