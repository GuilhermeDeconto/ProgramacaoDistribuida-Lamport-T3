package org.example;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class ReadFile {

    private int processQuantity;
    private ArrayList<Process> listOfProcess = new ArrayList<>();

    public void readFile() {
        try {
            File obj = new File("src/main/resources/config.txt");
            Scanner reader = new Scanner(obj);
            while (reader.hasNextLine()) {
                String[] stringArray = reader.nextLine().split(" ");
                Process process = new Process(Integer.parseInt(stringArray[0]), stringArray[1], Integer.parseInt(stringArray[2]), Double.parseDouble(stringArray[3]));
                listOfProcess.add(process);
                System.out.println(Arrays.toString(stringArray));
            }
            this.processQuantity = listOfProcess.size();
            reader.close();
        } catch (Exception e) {
            //error
            e.printStackTrace();
        }
    }

    public int getProcessQuantity() {
        return this.processQuantity;
    }

    public ArrayList<Process> getListOfProcess() {
        return listOfProcess;
    }

    public Process getRandom(int thisProcess) {
        int randomProcess = thisProcess;
        while (randomProcess == thisProcess) {
            Random r = new Random();
            randomProcess = r.nextInt(processQuantity);
        }

        return this.listOfProcess.get(randomProcess);
    }

}
