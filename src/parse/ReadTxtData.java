package parse;

import matrix.MatrixTelemetry;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/*
    Класс, предназначенный для парсинга данных телеметрии
    из файла формата .TXT
 */

public class ReadTxtData {

    public String inputSimpleData;
    public String pathOfData;
    public int numberOfRows=0;
    public int numberOfCollums;
    private MatrixTelemetry initalMatrix;
    private MatrixTelemetry mapMatrix;

    public ReadTxtData(String pathOfData){
        this.pathOfData = pathOfData;
        readData(pathOfData);
    }

    private void readData (String path) {
        StringBuilder dataFromFile = new StringBuilder();
        String s;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path)))){
            while ((s=reader.readLine())!=null){
                dataFromFile.append(s).append("\n");
                numberOfRows++;
            }
        inputSimpleData=dataFromFile.toString();
        }catch (IOException e){
            System.out.println("Файл не найден");
        }

        String[] arrayOfRow= inputSimpleData.split("\n");
        for (String a:arrayOfRow) {
            System.out.println(a);
        }

        int N = checkQuantityCollum (arrayOfRow[0]);
        numberOfCollums=N;
        initalMatrix = new MatrixTelemetry(numberOfRows,N);
        mapMatrix = new MatrixTelemetry(numberOfRows,N);

        for (int i = 0; i <arrayOfRow.length ; i++) {
            parseOneRow(arrayOfRow[i],i);
        }
    }

    private void parseOneRow(String row, int i){
        String newRow = row.replace(",",".");
        String dataInOneRow []= newRow.split("\\s+");

        double in=0;
        for (int j = 0, k=0; j <dataInOneRow.length ; j++) {

            if (dataInOneRow[j].equals("?????")){
                initalMatrix.set(i, k, 0);
                mapMatrix.set(i, k,1);
                k++;
            }
            else {
                in=Double.parseDouble(dataInOneRow[j]);
                initalMatrix.set(i, k, in);
                k++;
            }
        }
    }

    public MatrixTelemetry getInitalMatrix() {
        return initalMatrix;
    }

    public MatrixTelemetry getMapMatrix() {
        return mapMatrix;
    }

    public int checkQuantityCollum (String s){
        String dataInOneRow []= s.split("\\s+");
        return dataInOneRow.length;
    }
}
