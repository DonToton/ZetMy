package calculation;

import parse.ReadSimpleData;
import parse.ReadTelemetryData;
import java.util.*;

import matrix.*;

public class Main {
    //путь файла исходных данных
    private final static String pathFile = "C:/Users/Don_Toton/Desktop/test.TMN";

    //объекс прочитанного файла, содержит исходную матрицу и карту
    public static ReadTelemetryData inputFile;

    //матрица значений прочитанного файла и ее карта пропусков
    public static MatrixTelemetry matrixTelemetry;
    public static MatrixTelemetry mapTelemetry;

    //лист столбцов с нулевыми дисперсиями
    public static ArrayList<Integer> listOfZeroDispersion = new ArrayList<>();

    //компетентная матрица для конкретного пропуска и ее карта
    public static MatrixCompetent matrixCompetent;
    public static MatrixCompetent matrixCompetentMap;

    public static void main(String[] args) {

        ReadSimpleData inputFileTXT = new ReadSimpleData("C:/Users/Don_Toton/Desktop/mapZet.txt");
        matrixTelemetry = inputFileTXT.getInitalMatrix();
        mapTelemetry =inputFileTXT.getMapMatrix();


        /*
        //создаем исходную матрицу значений и карту пропусков, указав путь файла с данными телеметрии
        inputFile = new ReadTelemetryData("pathFile");
        matrixTelemetry = inputFile.getInitalMatrix();
        mapTelemetry=inputFile.getMapMatrix();*/

        //вывод исходных матриц
        matrixTelemetry.show();
         System.out.println("--------------------------------------------------------------------------------------------------------------------");
        mapTelemetry.show();

        //нормализация по дисперсии матрицы значений
/*
        matrixTelemetry.normalizeByDispersion(mapTelemetry);
        System.out.println("Нормализованная по дисперсии");
        matrixTelemetry.show();*/


        //получаем колонки с нулевыми дисперсиями
        for (int j = 0; j <matrixTelemetry.N ; j++)
            if (matrixTelemetry.getDespersionCollum(mapTelemetry,j)==0) listOfZeroDispersion.add(j);
        System.out.println("cтолбцы с нулевой дисперсией: ");
        for(Integer i:listOfZeroDispersion) System.out.print(i+"  ");
        System.out.println();

        //создаем результирующую матрицу и ее карту (матрица, которая будет содержать восстановленные значени)
        MatrixTelemetry resultMatrix = new MatrixTelemetry(matrixTelemetry.M,matrixTelemetry.N);
        MatrixTelemetry resultMap = new MatrixTelemetry(matrixTelemetry.M,matrixTelemetry.N);

        //Перебираем все элементы с пропусками
        for (int i = 0; i < mapTelemetry.M ; i++) {
            for (int j = 0; j < mapTelemetry.N ; j++) {

                //если в карте этот элемент отмечен, как пропущенный, то пытаемся восстановить
                if (mapTelemetry.get(i,j)==1) {

                    //создание компетентной матрицы для конкретного пропуска и его предсказание (в случае успешного создания)
                    System.out.println("получаем компетентную матрицу для элемента ["+i+"] ["+j+"]");
                    if (getCompetentMatrix(matrixTelemetry, mapTelemetry, i, j)) {

                        resultMatrix.set(i, j, matrixCompetent.resultPrediction(matrixCompetentMap));
                        resultMap.set(i,j,1);
                    }
                    System.out.println();
                }
            }
        }// 4 32 пропускаем одну строку

        System.out.println("Значения");
        resultMatrix.show();
        System.out.println("Карта");
        resultMap.show();

        //вставка восстановленных значений в исходную матрицу
        insert(resultMatrix, resultMap, matrixTelemetry);
        System.out.println("Результат работы программы");
        matrixTelemetry.show();


        /*
        System.out.println("Умножаем на дисперсию");
        matrixTelemetry.deNormalizeByDispersion(mapTelemetry);
        matrixTelemetry.show();*/


    }
    
    public static void insert( MatrixTelemetry resultMatrix,MatrixTelemetry resultMap,MatrixTelemetry matrixTelemetry ){
        for (int i = 0; i <matrixTelemetry.M ; i++) {
            for (int j = 0; j < matrixTelemetry.N; j++) {
                if (resultMap.get(i,j)==1) matrixTelemetry.set(i,j,resultMatrix.get(i,j));
            }
        }
    }

    // возвращает компетентную матрицу для указанного элемента
    public static boolean getCompetentMatrix(MatrixTelemetry matrix, MatrixTelemetry map, int iSkip, int jSkip){

        //если дисперсия столбца восстанавливаемого пропуска=0, то не обрабатываем его
        if (listOfZeroDispersion.indexOf(jSkip)!=-1) {
            System.out.println("Дисперсия столбца восстанавлимаемого пропуска =0 ");
            return false;
        }

        //задаем диапазон поиска компетентный строк
        int searchRange=4; //количество строк сверху и снизу для поиска компетентных строк; если 4, то всего 8
        int competentRows=7, competentCollum=7;

        //предотвращаем выход за диапазон матрицы
        int topBorder=0, bottomBorder=0, temp;
        topBorder=iSkip-searchRange;
        bottomBorder=iSkip+searchRange+1;

        //предотвращаем выход за диапазон матрицы
        if (topBorder<0){
            temp=Math.abs(topBorder);
            topBorder=0;
            bottomBorder+=temp;

        } else if (bottomBorder>matrix.M){
            temp=bottomBorder-matrix.M;
            bottomBorder=matrix.M;
            topBorder-=temp;
        }

        //считаем компетентности строк searchRange строк снизу и сверху указанной в iSkip
        Map<Integer,Double> listOfCompetentRows = new LinkedHashMap<>();
        double competent=0;
        for (int i = topBorder; i <bottomBorder ; i++) {
            // Компетентная строка не может иметь пропуска в j
            if ((i==iSkip) ||(map.get(i,jSkip)==1)) continue;

            competent = matrix.getCompetentOfRow(map,iSkip,i);
            listOfCompetentRows.put(i,competent);
        }

        // Сортируем список по убыванию величины компетентности строк
        Integer [] keys = getSortKeys(listOfCompetentRows);

        // Отбираем в матрицу указанное competentRows число строк с наибольшей компетентностью
        ArrayList<Integer> selectedRow = new ArrayList<>();
        selectedRow.add(iSkip);
        for (int i = 0; i <competentRows ; i++)
            selectedRow.add(keys[i]);
        Collections.sort(selectedRow);

        // Теперь у нас поменялся индекс строки, в которой находится пропуск
        int newISkip=selectedRow.indexOf(iSkip);

        //Создаем карту компетентностей строк для компетентной матрицы (нужно будет при подборе
        // степени влияния компетентности)
        Map<Integer,Double> competentOfRowsOfCompetentMatrix = new LinkedHashMap<>();
        for (int i = 0; i <competentRows+ 1; i++) {
            if (i==newISkip) continue;
            competentOfRowsOfCompetentMatrix.put(i, listOfCompetentRows.get(selectedRow.get(i)));
        }

        //создаем матрицу и ее карту с выбранными компетентными строками
        MatrixCompetent matrixCompetentRows = new MatrixCompetent(competentRows+1,matrixTelemetry.N);
        MatrixCompetent matrixCompetentRowsMap  = new MatrixCompetent(competentRows+1,matrixTelemetry.N);

        //внесение в объекты матриц индексов строк для удобного вывода
        matrixCompetentRows.numeration=selectedRow;
        matrixCompetentRowsMap.numeration=selectedRow;

        //копирование указанного списка индексов строк в новую матрицу
        matrixCompetentRows.copyRow(matrix,selectedRow);
        matrixCompetentRowsMap.copyRow(map,selectedRow);

        //вывод матриц на экран
        //matrixCompetentRows.show();
        // matrixCompetentRowsMap.show();

        /*****************************************************************************************************************/
        /************************************         Для столбцов  ******************************************************/
        /*****************************************************************************************************************/

        //получаем лист нежелательных для предсказания колонок
        ArrayList<Integer> listOfDepricateCollum=getIndexOfDepricateCollum(matrixCompetentRowsMap,competentRows );

        //добавляем к списку нежелательных строк строки с нулевой дисперсий
        for (int j = 0; j <matrixCompetentRows.N ; j++) {

            if (listOfDepricateCollum.contains(j)) continue;

            if (matrixCompetentRows.getDespersionCollum(matrixCompetentRowsMap, j) == 0){
                listOfDepricateCollum.add(j);
            }
        }
        System.out.println("Нежелательные столбцы компетентной матрицы ");
        for (Integer i: listOfDepricateCollum) System.out.print(i+" ");
        System.out.println();


        // получаем карту <Индекс, Значение> компетентностей столбцов                                         !CHEKED+!
        Map<Integer,Double> listOfCompetentCollum = new LinkedHashMap<>();
        for (int j = 0; j <matrixCompetentRows.N ; j++) {
            // Компетентный столбец не может иметь пропуска в i строке
            if ((j==jSkip) ||(matrixCompetentRowsMap.get(newISkip,j)==1)) {  // iSKIP стало другим
                continue;
            }
            // пропускаем нежелательные столбцы
            if (listOfDepricateCollum.contains(j)) {
                continue;
            }
            // получаем карту компетентнностей столбцов в формате <Индекс, Значение>
            competent = matrixCompetentRows.getCompetentOfCollum(matrixCompetentRowsMap,j,jSkip);
            listOfCompetentCollum.put(j,competent);
        }

        // Сортируем список по убыванию величины компетентности столбцов
        Integer [] keysCollum = getSortKeys(listOfCompetentCollum);

        // Отбираем в матрицу указанное competentCollum число столбцов с наибольшей компетентностью
        ArrayList<Integer> selectedCollum = new ArrayList<>();
        selectedCollum.add(jSkip);
        for (int i = 0; i <competentCollum ; i++)
            selectedCollum.add(keysCollum[i]);
        Collections.sort(selectedCollum);

        // Теперь у нас поменялся индекс столбца, в которой находится пропуск
        int newJSkip=selectedCollum.indexOf(jSkip);

        //создаем карту компетентностей столбцов для компетентной матрицы (нужно будет пи подборе
        // степени влияния компетентности)
        Map<Integer,Double> competentOfCollumOfCompetentMatrix = new LinkedHashMap<>();
        for (int i = 0; i <competentRows+ 1; i++) {
            if (i==newJSkip) continue;
            competentOfCollumOfCompetentMatrix.put(i,  listOfCompetentCollum.get(selectedCollum .get(i)));
        }

        //создаем компетентную матрицу и ее карту с выбранными компетентными столбцами
        MatrixCompetent matrixCompetentCollum = new MatrixCompetent(competentRows+1,competentCollum+1);
        MatrixCompetent matrixCompetentCollumMap  = new MatrixCompetent(competentRows+1,competentCollum+1);

        //внесение в объекты матриц индексов строк для удобного вывода
        matrixCompetentCollum.numeration=selectedRow;
        matrixCompetentCollumMap.numeration=selectedRow;

        //внесение в объекты матриц названий столбцов для удобного вывода
        matrixCompetentCollum.setCaptions(selectedCollum);
        matrixCompetentCollumMap.setCaptions(selectedCollum);

        //копирование указанного списка индексов столбцов в новую матрицу
        matrixCompetentCollum.copyCollum(matrixCompetentRows,selectedCollum);
        matrixCompetentCollumMap.copyCollum(matrixCompetentRowsMap,selectedCollum);

        //устанавливаем компетентности компетентной матрицы
        matrixCompetentCollum.setCompetents(competentOfCollumOfCompetentMatrix,competentOfRowsOfCompetentMatrix);

        //вывод матриц на экран
         matrixCompetentCollum.show();
         // matrixCompetentCollumMap.show();

        ////внесение в объекты матриц измененных коордиат пропуска
        matrixCompetentCollum.setISkipJSkip(newISkip, newJSkip);
        matrixCompetentCollumMap.setISkipJSkip(newISkip, newJSkip);

        //установка полей для дальнейшего доступа к созданной компетентной матрице
        matrixCompetent=matrixCompetentCollum;
        matrixCompetentMap=matrixCompetentCollumMap;

        return true;
    }

    //Перебераем столбцы, ищем те, в которых много пропусков
    public static ArrayList<Integer> getIndexOfDepricateCollum(MatrixCompetent matrixCompetentRowsMap, int competentRows){

        ArrayList<Integer> list = new ArrayList<>();
        int count;

        for (int j = 0; j <matrixCompetentRowsMap.N ; j++) {
            count=0;
            for (int i = 0; i< matrixCompetentRowsMap.M; i++) {

                if (matrixCompetentRowsMap.get(i,j)==1) count++;
            }

            if (count>(competentRows/2-1)) {
                list.add(j);
            }
        }
        return list;
    }

    //возвращает индексы строк (столбцов) с наибольшей компетентностью
    public static Integer[] getSortKeys(Map<Integer,Double> map){

        Double [] valueCollum = map.values().toArray(new Double[map.size()]);
        Integer [] keysCollum = map.keySet().toArray(new Integer [map.size()] );
        double c1=0; int c2=0;
        for (int i = map.size()-1; i >0 ; i--) {
            for (int j = 0; j <i ; j++) {
                if (valueCollum[j]<valueCollum[j+1]){
                    c1=valueCollum[j];
                    valueCollum[j]=valueCollum[j+1];
                    valueCollum[j+1]=c1;

                    c2=keysCollum[j];
                    keysCollum[j]=keysCollum[j+1];
                    keysCollum[j+1]=c2;
                }
            }
        }
        return keysCollum;
    }
}











