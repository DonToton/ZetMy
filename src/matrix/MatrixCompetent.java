package matrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

//Объект компетентной матрицы. Создается для каждого пропуска.
public class MatrixCompetent extends MatrixTelemetry{

    //координаты пропуска
    public int iSkip, jSkip;

    //результирующая ошибка предсказания по столбцу и строке
    public double jMistake, iMistake;

    //заголовки для удобного вывода
    public ArrayList<String> captions = new ArrayList<>();
    public ArrayList<Integer> numeration=new ArrayList<>();

    //списки компетентностей текущей компетентной матрицы для пропуска iSkip, jSkip
    public Map<Integer,Double> mapOfCollumCompetents;
    Map<Integer,Double> mapOfRowsCompetents;

    MatrixCompetent map;

    public MatrixCompetent(int M, int N){
        super(M,N);
        Collections.addAll(captions, MatrixTelemetry.captions);
    }

    public void setISkipJSkip(int iSkip, int jSkip){
        this.iSkip=iSkip; this.jSkip=jSkip;
    }

    public void setCaptions(ArrayList<Integer> list){
        ArrayList<String> newListCaptions = new ArrayList<>(list.size());
        for (Integer i : list){
            newListCaptions.add(captions.get(i));
        }
        captions=newListCaptions;
    }

    public void setCompetents(Map<Integer,Double> mapOfCollumCompetents, Map<Integer,Double> mapOfRowsCompetents){
        this.mapOfCollumCompetents=mapOfCollumCompetents;
        this. mapOfRowsCompetents= mapOfRowsCompetents;
    }

    @Override
    public void show() {
        /*
        System.out.print("   ");
        for (String s:captions) System.out.printf("%10.9s",s);
        System.out.print("\n");*/
        for (int i = 0; i < M; i++) {
            System.out.format("%2d)",numeration.get(i));
            for (int j = 0; j < N; j++)
                //System.out.printf("%10.4f", data[i][j]);
                System.out.printf("%15.2f", data[i][j]);
            System.out.println();
        }
    }

    public  double resultPrediction(MatrixCompetent map){

        this.map=map;

        double resultDegree, resultPredictJ, resultPredictI;

       // System.out.println("Координаты восстанавливаемого пропуска: iSkip= "+iSkip+" jSkip= "+ jSkip);

        //Получаем подсказки для столбцов
        Map<Integer,Map<Integer, Double>> mapOfMapOfCollumHints = getMapOfCollumHints(map,jSkip);
        //System.out.println("Подсказки для столбцов");
        //showMapOfMap(mapOfMapOfCollumHints);

        //Находим наилучшую степень влияния компетентности для столбцов
        resultDegree=findBeterDegreeCollum(mapOfMapOfCollumHints,mapOfCollumCompetents,0.01);

        //Получаем конечный прогноз
        resultPredictJ= getPredictionOfI(mapOfMapOfCollumHints,mapOfCollumCompetents,resultDegree,iSkip);
        System.out.println("Конечный прогноз по столбцам "+resultPredictJ+" c погрешностью "+jMistake);

        //Получаем подсказки для строк
        Map<Integer,Map<Integer, Double>> mapOfMapOfRowHints = getMapOfRowHints(map,iSkip);
        //System.out.println("Подсказки для строк");
        //showMapOfMap(mapOfMapOfRowHints);

        //Получаем наилучшую степень влияния компетентности для строк
        resultDegree=findBeterDegreeRow(mapOfMapOfRowHints,mapOfRowsCompetents,0.01);

        //Получаем конечный прогноз по строкам
        resultPredictI=getPredictionOfJ(mapOfMapOfRowHints,mapOfRowsCompetents,resultDegree, jSkip);
        System.out.println("Конечный прогноз по строкам "+resultPredictI+" c погрешностью "+iMistake);

        //Итог
        double final1= getFinalPrediction1(resultPredictJ,resultPredictI);
        System.out.println("FINAL1 = "+final1);
        //System.out.println("FINAL2 = "+getFinalPrediction2(resultPredictJ,resultPredictI));

        return final1;
    }

    /* Возвращает карту вида: <Индекс столбца, совершившего предсказание <Индекс строки, Значение предсказания>>
       (Кождый столбец компетентной матрицы предсказывает элементы столбца jSkip)    */
    public Map<Integer,Map<Integer, Double>> getMapOfCollumHints(Matrix map, int jSkip) {
        Map<Integer,Map<Integer, Double>> mapOfMapOfHints = new LinkedHashMap<>();
        for (int j = 0; j <N ; j++) {
            if (j==jSkip) continue;   // столбец с пропуском не предсказывает значения самого себя
            mapOfMapOfHints.put(j,getMapOfCollumHints(map,j,jSkip));
        }
        return mapOfMapOfHints;
    }

      /* возвращает карту вида <Индекс строки, Значение предсказания> всех элементов столбца с пропуском на основе
       уравнения лиейной регрессии ( каждый элемент jCollum столбца предсказывает элемент jSkip столбца)*/
    public Map<Integer,Double> getMapOfCollumHints(Matrix map, int jCollum, int jSkip){

        //вычисляем значения, которые потребуются для определения коэффициентов уравнения лин. регрессии
        double averageJCollum=getAverageCollum(map,jCollum),
                averageJSkip=getAverageCollum(map,jSkip),
                averageJCollumX2=getAverageX2Collum(map,jCollum),
                averageXY=getAverageXYCollum(map,jCollum,jSkip);

        //коэффициенты уравнения линейной регрессии y=a+bx
        double b = (averageXY-averageJCollum*averageJSkip)/(averageJCollumX2-Math.pow(averageJCollum,2));
        double a = (averageJCollumX2*averageJSkip-averageJCollum*averageXY)/(averageJCollumX2-Math.pow(averageJCollum,2));
        double prediction =0;

        // с помощью уравнения линейной регрессии предсказываем все элементы jSkip столбца
        Map<Integer,Double> mapOfPredictions = new LinkedHashMap<>();
        for (int i = 0; i <M ; i++) {
            if ((map.get(i,jCollum)==0)) {  // пропуск в jCollum не может предсказывать элемент jSkip
                prediction = get(i, jCollum) * b + a;
                mapOfPredictions.put(i,prediction);
            }
        }
        /*System.out.println("Jcollum= "+jCollum);
        for (Map.Entry<Integer,Double> pair: mapOfPredictions.entrySet()){
            System.out.println("key= "+pair.getKey()+" value= "+pair.getValue());
        }*/
        return mapOfPredictions;
    }


    /* возвращает наилучшую степень влияния компетентности (усредненую наилучшую степень по всем элементам столбца)
      а также среднюю ошибку восстановления по колонкам */
    public double findBeterDegreeCollum(Map<Integer,Map<Integer, Double>> mapOfMap,Map<Integer,Double> mapCompet, double step){

        // сумма наилучших степеней
        double sumBetterOfDegree = 0;

        //необходимые переменные
        double predictionI,mistake=0 , minMistake=0, betterDegree=0, resultMinMistake=0, count=0;

        //Для каждого элемента столбца с пропуском перебираем мар
        for (int i = 0; i <M ; i++) {

            // В данный момент наш пропуск не предсказывается
            if (i==iSkip) continue;
            //System.out.println("Подбираем degree для "+i+" элемента");

            //подбираем показатель степени только по тем строкам, где пропущено не больше половины
            //  System.out.println("Проверяем строку "+i+" "+ checkRow( map, i));
            if (!checkRow( map, i)) {
                System.out.println("WARNING пропустили строку "+i);
                continue;
            }

            //примем за минимальную ошибку при степени degree=0
            minMistake=Math.abs(get(i,jSkip)-getPredictionOfI(mapOfMap,mapCompet,0,i));
            betterDegree=0;

            //для каждого элемента столбца с пропуском подбираем наидучшую степерь degree
            for (double degree = -50; degree <50 ; degree+=step) {

                //получаем предсказание элемента i (столбца с пропуском) в зависимости от степени degree
                predictionI=getPredictionOfI(mapOfMap,mapCompet,degree,i);

                //определяем ошибку предсказания
                mistake=Math.abs(get(i,jSkip)-predictionI);

                //в конце цикла здесь будет минимальная ошибка и наилучшая степень
                if (mistake<minMistake) {
                    minMistake = mistake;
                    betterDegree=degree;
                }

            }
            // суммируем наилучшие степени ( NAN возникать не должен, но на всякий случай)
            if (!Double.isNaN(minMistake)) {
                sumBetterOfDegree+=betterDegree;
                count++;
            } else System.out.println("появился NAN");
        }

        //находим результирующий показатель влияния компетентности
        double resultDegree;
        resultDegree = sumBetterOfDegree/ count;

        count=0;

        //находим среднюю ошибку предсказания искомого элемента по столбцам
        for (int i = 0; i <M ; i++) {
            // В данный момент наш пропуск не предсказывается
            if (i == iSkip) continue;

            if (!checkRow( map, i)) continue;
            predictionI=getPredictionOfI(mapOfMap, mapCompet, resultDegree, i);
            resultMinMistake = resultMinMistake+Math.abs(get(i, jSkip) - predictionI);
            count++;
        }

        //итоговая ошибка предсказания элемента по столбцам
        jMistake=resultMinMistake/count;
        return resultDegree;
    }

   /*Возвращает усредненную (результирующую) подсказку i-ого элемента столбца с пропуском в зависимости от степени
    degree (влияния компетенции столбцов),переданной в качестве параметра */
    public double getPredictionOfI(Map<Integer,Map<Integer, Double>> mapOfMap,Map<Integer,Double> mapOfCompetents,
                                   double degree, int i ){

        // счетчик, знаменатель, числитель
        double  denominator = 0, numerator = 0;

        // перебираем все карты и умножаем на компетентности
        for (int allMaps = 0; allMaps < N; allMaps++) {
            if (allMaps == jSkip)  continue;            //Столбец с пропуском не предсказывает сам себя

            // получаем карту <Индекс, Предсказание> которые выполнил столбец = allMaps
            Map<Integer, Double> mapOfHint = mapOfMap.get(allMaps);

            //System.out.println("mapOfHint.containsKey(i) "+mapOfHint.containsKey(i)+" где i= "+i);
            //если столбец  allMaps предсказывал i-ый элемент и он не NAN
            if (mapOfHint.containsKey(i)&&(!Double.isNaN(mapOfHint.get(i)))) {
               // System.out.println("условие выполнилось");
                // умножаем подсказку i-ого элемента на его компетентность в степени degree
                numerator =numerator + mapOfHint.get(i) * Math.pow(mapOfCompetents.get(allMaps),degree);

                //суммируем значение знаменателя согласно формуле
                denominator += Math.pow(mapOfCompetents.get(allMaps),degree);
            }
        }
       // System.out.println("numerator = "+numerator+" denominator= "+denominator);
        return numerator/denominator;
    }

    public boolean checkRow(MatrixCompetent map, int row){

        int count =0;
        for (int j = 0; j <map.N ; j++) {
            if (j==jSkip) continue;
            if (map.get(row, j)==1) count++;
        }
        if (count>3) return false;
        else return true;
    }

    /*****************************************************************************************************************/
    /************************************         Для строк  ******************************************************/
    /*****************************************************************************************************************/

    /*возвращает карту вида <Индекс строки, совершившей предсказание <Индекс столбца, Эначение предсказания >>
    (Кождая строка компетентной матрицы предсказывает элементы строки iSkip)    */
    public Map<Integer,Map<Integer, Double>> getMapOfRowHints(Matrix map, int iSkip) {
        Map<Integer,Map<Integer, Double>> mapOfMapOfHints = new LinkedHashMap<>();
        for (int i = 0; i <M ; i++) {
            if (i==iSkip) continue;
            mapOfMapOfHints.put(i,getMapOfRowHints(map,i,iSkip));
        }
        return mapOfMapOfHints;
    }

    /* возвращает карту вида <Индекс столбца, Значение предсказания> всех элементов строки с пропуском на основе
    уравнения лиейной регрессии ( каждый элемент iRow строки предсказывает элемент iSkip строки)  */
    public Map<Integer,Double> getMapOfRowHints(Matrix map, int iRow, int iSkip){
        double averageIRow=getAverageRow(map,iRow),
                averageISkip=getAverageRow(map,iSkip),
                averageIRowX2=getAverageX2Row(map,iRow),
                averageXY=getAverageXYRow(map,iRow,iSkip);

        //коэффициенты уравнения линейной регрессии y=a+bx
        double b = (averageXY-averageIRow*averageISkip)/(averageIRowX2-Math.pow(averageIRow,2));
        double a = (averageIRowX2*averageISkip-averageIRow*averageXY)/(averageIRowX2-Math.pow(averageIRow,2));
        double prediction =0;

        Map<Integer,Double> mapOfPredictions = new LinkedHashMap<>();
        for (int j = 0; j <N ; j++) {
            if ((map.get(iRow,j)==0)) {
                prediction = get(iRow, j) * b + a;
                mapOfPredictions.put(j,prediction);
            }
        }
        return mapOfPredictions;
    }

    //возвращает наилучшую степень влияния компетентности
    public double findBeterDegreeRow(Map<Integer,Map<Integer, Double>> mapOfMap,Map<Integer,Double> mapCompet, double step){
        //
        double sumBetterOfDegree = 0;

        //необходимые переменные
        double predictionJ,mistake=0 , minMistake=0, betterDegree=0, resultMinMistake=0, count=0;

        //Для каждого элемента строки с пропуском перебираем мар
        for (int j = 0; j <N ; j++) {

            if (j==jSkip) continue; // Сначала не предсказываем наш пропуск в j
            //System.out.println("Подбираем degree для "+i+" элемента");
            betterDegree=0;
            minMistake=Math.abs(get(iSkip,j)-getPredictionOfJ(mapOfMap,mapCompet,0,j));

            //для каждого элемента строки с пропуском подбираем наидучшую степерь degree
            for (double degree = -50; degree <50 ; degree+=step) {

                //получаем предсказание j (элемента строки с пропуском) в зависимости от степени degree
                predictionJ=getPredictionOfJ(mapOfMap,mapCompet,degree,j);

                //вычисляем модуль ошибки предсказания
                mistake=Math.abs(get(iSkip,j)-predictionJ);

                //System.out.println("Предсказание "+j+" элемента = "+predictionJ+"degree ="+degree+" ошибка = "+mistake);

                //сдесь соберутся минимальная ошибка и наилучшая степень
                if (mistake<minMistake) {
                    minMistake = mistake;
                    betterDegree=degree;
                }
            }

            // суммируем наилучшие степени ( NAN возникать не должен, но на всякий случай)
            if (!Double.isNaN(minMistake)) {
                sumBetterOfDegree+=betterDegree;; // убераем NAN
                count++;
            }
        }

        double resultDegree;
        resultDegree = sumBetterOfDegree/ count;

        count=0;
        //находим среднюю ошибку предсказания искомого элемента по столбцам
        for (int j = 0; j <N ; j++) {
            if (j == jSkip) continue; // В данный момент наш пропуск не предсказывается
            predictionJ=getPredictionOfJ(mapOfMap, mapCompet, resultDegree, j);
            resultMinMistake +=Math.abs(get(iSkip, j) - predictionJ);
            count++;
        }

        //итоговая ошибка предсказания элемента по столбцам
        iMistake=resultMinMistake/count;

        return resultDegree;
    }

    //Возвращает результирующую подсказку j-ого элемента
    public double getPredictionOfJ(Map<Integer,Map<Integer, Double>> mapOfMap,Map<Integer,Double> mapOfCompetents,
                                   double degree, int j ){
        // счетчик, знаменатель, числитель
        double count = 0, denominator = 0, numerator = 0;

        // перебираем все карты и умножаем на компетентности
        for (int allMaps = 0; allMaps < M; allMaps++) {
            if (allMaps == iSkip)  continue;            //Строка с пропуском не предсказывает сама себя

            // получаем карту <Индекс, Предсказание> которые выполнила строка = allMaps
            Map<Integer, Double> mapOfHint = mapOfMap.get(allMaps);

            //если строка allMaps предсказывала j-ый элемент и он не NAN
            if (mapOfHint.containsKey(j)&& (!Double.isNaN(mapOfHint.get(j)))) { //то умножить на компетентность

                // умножаем подсказку j-ого элемента на его компетентность в степени degree
                numerator+= mapOfHint.get(j) * Math.pow(mapOfCompetents.get(allMaps),degree);

                //суммируем значение знаменателя согласно формуле
                denominator += Math.pow(mapOfCompetents.get(allMaps),degree);
            }
        }
        return numerator/denominator;
    }
    //возвращает финальное предсказание за счет усреднения предсказаний с весом, обратно пропорциональным ошибке
    public double getFinalPrediction1( double resultPredictJ, double resultPredictI ){

        double e=0.01;
        return (resultPredictJ/(e+jMistake)+resultPredictI/(e+iMistake))*((e+jMistake)*(e+iMistake))/(2*e+jMistake+iMistake);

    }
    // в качестве финального возвращается предсказание с минимальной ожидаемой ошибкой
    public double getFinalPrediction2( double resultPredictJ, double resultPredictI ) {

        if (iMistake<=jMistake) return resultPredictI;
        else return resultPredictJ;
    }

    // вывод карты MapOfMap
    public static void showMapOfMap(Map<Integer,Map<Integer, Double>> mapOfMap){
        for(Map.Entry<Integer,Map<Integer, Double>> pair: mapOfMap.entrySet()){
            System.out.println("Ключ коллекции"+pair.getKey());
            Map<Integer,Double> mapOfHint = pair.getValue();
            for (Map.Entry<Integer,Double> pair2: mapOfHint.entrySet()){
                System.out.println("Нижний ключ "+pair2.getKey()+" Значение "+pair2.getValue());
            }
        }
    }

    // вывод карты Map
    public static void showMap(Map<Integer, Double> map){
        for (Map.Entry<Integer, Double> pair: map.entrySet()) {
            System.out.println("keys ="+pair.getKey()+" value= "+pair.getValue());
        }
    }
}
