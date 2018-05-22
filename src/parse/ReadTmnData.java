package parse;

import matrix.MatrixTelemetry;

import java.io.*;

/*
    Класс, предназначенный для парсинга данных телеметрии
    из файла с форматом .TMN
 */

public class ReadTmnData {

    public String inputTelemetryData;
    public String pathOfData;
    public int numberOfRows=0;
    private MatrixTelemetry initalMatrix;
    private MatrixTelemetry mapMatrix;

    public ReadTmnData(String pathOfData){
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
        inputTelemetryData=dataFromFile.toString();
        }catch (IOException e){
            System.out.println("Файл не найден");
        }
        numberOfRows--;
        initalMatrix = new MatrixTelemetry(numberOfRows,61);
        mapMatrix = new MatrixTelemetry(numberOfRows,61);

        String[] arrayOfRow= inputTelemetryData.split("\n");
        for (String a:arrayOfRow) {
            System.out.println(a);
        }

        for (int i = 1; i <arrayOfRow.length ; i++) {
            parseOneRow(arrayOfRow[i],i);
        }
    }

    private void parseOneRow(String row, int i){
        String newRow = row.replace(",",".");
        String dataInOneRow []= newRow.split("\\s+");

        double in=0;
        for (int j = 1, k=0; j <dataInOneRow.length-1 ; j++) {
            if (dataInOneRow[j].equals("---")||(j==47)) continue;

            if (dataInOneRow[j].equals("?????")){
                initalMatrix.set(i-1, k, 0);
                mapMatrix.set(i-1, k,1);
                k++;
            } else if (dataInOneRow[j].equals("FF")) {
                initalMatrix.set(i-1, k, 0);
                mapMatrix.set(i-1, k,1);
                k++;
            } else {
                in=Double.parseDouble(dataInOneRow[j]);
                initalMatrix.set(i-1, k, in);
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
}
