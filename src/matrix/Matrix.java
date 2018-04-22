package matrix;

import java.util.ArrayList;


public class Matrix {

    public final int M;             // число строк
    public final int N;             // число столбцов
    public final double[][] data;

    public Matrix(int M, int N) {
        this.M = M;
        this.N = N;
        data = new double[M][N];
    }

    public Matrix(double[][] data) {
        M = data.length;
        N = data[0].length;
        this.data = new double[M][N];
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                this.data[i][j] = data[i][j];
    }

    // вывод массива
    public void show() {
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++)
               System.out.printf("%14.2f ", data[i][j]);
            System.out.println();
        }
    }

    //устанавливает значение указанного элемента массива
    public void set(int i, int j, double in){
        if ((i>M-1) ||(j>N-1)) throw new RuntimeException("Illegal matrix dimensions.");
        data[i][j]=in;
    }
    //возвращает указаный элемент массива
    public double get(int i, int j){
        if ((i>M-1) ||(j>N-1)) throw new RuntimeException("Illegal matrix dimensions.");
        return data[i][j];
    }
    //возвращает сумму указаного столбца
    public double sumColumn(int j){
        double sum=0;
        for (int i = 0; i <M ; i++) {
            sum+=data[i][j];
        }
        return sum;
    }
    // копирует из matrix указанные  в list строки
    public void copyRow(Matrix matrix, ArrayList<Integer> list){
        for (int i = 0; i <list.size() ; i++) {
            for (int j = 0; j < N; j++) {
                data[i][j] = matrix.data[list.get(i)][j];
            }
        }
    }
    // копирует из matrix указанные в list столбцы
    public void copyCollum(Matrix matrix, ArrayList<Integer> list){
        for (int j = 0; j <list.size() ; j++) {
            for (int i = 0; i <M ; i++) {
                data[i][j]=matrix.data[i][list.get(j)];
            }

        }

    }
}