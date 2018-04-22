package matrix;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MatrixTelemetry extends Matrix {

    //заколовки свойств для вывода в методе show()
    public static String [] captions = {
            "Bort,V","Bort,A","Sun,V","Sun,A","IPrd1,A","IPrd2,A","NAP,A",
            "EO,A","DTE,A","TMR,A","Rez,A","N344k1,A","344k1,A","N344k2,A","344k2,A",
            "338km,A","BEM,A","KT,A","Sp1,A","Sp2,A","Sp3,A","Sp4,A","SPrm1,V",
            "SPrm2,V","FPrd1,V","RPrd1,V","FPrd2,V","RPrd2,V","Ux,V","Uy,V","Uz,V",
            "Tbck,C","TPrd1,C","TPrd2,C","Tnap,C","Tab,C","Plat1,C","Plat2,C","Plat3,C",
            "Plat4,C","DT1,C","DT2,C","DT3,C","T1,C","T2,C","T8,C","T7,C","UkOr,O",
            "k338km,O","k344k2,O","k344k1,O","UkNAP,O","СomVP,D","ScСom,D", "Comm,O",
            "God,h","Mes,h","Den,h","Chas,h","Min,h","Sec"};

    public MatrixTelemetry(int M, int N){
        super(M,N);
    }

    public MatrixTelemetry(double[][] data){
        super(data);
    }

    private ArrayList<Double> listOfDispersion = new ArrayList<>();
    private ArrayList<Double> listOfAvarage= new ArrayList<>();

    @Override
    public void show() {
       /* не печатаю заголовки
        System.out.print("   ");
        for (String s:captions) System.out.printf("%10.9s",s);
        System.out.print("\n"); */
        for (int i = 0; i < M; i++) {
            System.out.format("%2d)",i);
            for (int j = 0; j < N; j++)
                //System.out.printf("%10.4f", data[i][j]);
                System.out.printf("%15.2f", data[i][j]);
            System.out.println();
        }
    }

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!ПРОВЕРЕНО!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //нормализует матрицу по дисперсии с учетом пропусков
    //над элементами с пропусками вычисления не производятся
    public  void normalizeByDispersion(Matrix map){
        //считаем дисперсию
        double average, disperse;
        for (int j = 0; j <N ; j++) {

            disperse=getDespersionCollum(map,j);
            listOfDispersion.add(disperse);
            //disperse= BigDecimal.valueOf(disperse).setScale(15,BigDecimal.ROUND_HALF_DOWN).doubleValue();
            if (disperse==0) continue;
            average=getAverageCollum(map,j);
            listOfAvarage.add(average);

            //делим каждый элеметн столбца на дисперсию
            for (int i = 0; i < M; i++) {
                double temp ;
                if (map.get(i,j)==1) continue;
                temp= (get(i, j)-average) / disperse; // ИЗМЕНИЛ ФОРМУЛУ
                set(i, j, temp);
            }
        }
    }

    /*****************************************************************************************************************/
    /************************************         Для столбцов  ******************************************************/
    /*****************************************************************************************************************/

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!ПРОВЕРЕНО!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //возвращает дисперсию j-ого столбца, учитывая пропуски
    //над элементами с пропусками вычисления не производятся
    public double getDespersionCollum(Matrix map, int j){
        double avarage, sum=0;
        avarage =getAverageCollum(map,j);
        int miss=0;
        for (int i = 0; i < M; i++) {
            if( map.get(i,j)==0) sum += Math.pow((get(i, j) - avarage), 2);
            else miss++;
        }
        //return sum / (M-miss);
        return BigDecimal.valueOf(sum/(M-miss)).setScale(20,BigDecimal.ROUND_HALF_DOWN).doubleValue();
    }

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!ПРОВЕРЕНО!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // возвращает среднее по столбцу учитывая пропуски
    //над элементами с пропусками вычисления не производятся
    public double getAverageCollum(Matrix map, int j){
        double sum=0,miss=0;
        for (int i = 0; i <M ; i++) {
            if(map.get(i,j)==0) sum+=get(i,j);
            else miss++;
        }
        //return sum/(M-miss);
        return BigDecimal.valueOf(sum/(M-miss)).setScale(20,BigDecimal.ROUND_HALF_DOWN).doubleValue();
    }

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!ПРОВЕРЕНО!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // возвращает среднее по столбцу значение квадрата элемента
    public  double getAverageX2Collum(Matrix map, int j){
        double sum=0, miss=0;
        for (int i = 0; i <M ; i++) {
            if (map.get(i,j)==0) sum+=Math.pow(get(i,j),2);
            else miss++;
        }
        return sum/(M-miss);
    }

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!ПРОВЕРЕНО!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //возвращает среднее произведение элементов из указанных столбцов
    public  double getAverageXYCollum(Matrix map, int jCollum, int jSkip){
        double sum=0, miss=0;
        for (int i = 0; i <M ; i++) {
            if ((map.get(i,jCollum)==0) && (map.get(i,jSkip)==0)) sum+=get(i,jCollum)*get(i,jSkip);
            else miss++;
        }
        return sum/(M-miss);
    }

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!ПРОВЕРЕНО!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    /* Возвращает компетентность указанного j столбца относительно jSkip столбца
      L(jk)=correlation*t(jk)   ; j меняется, jSkip постоянно для одного пропуска
      (вичисляет корреляцию по Пирсону с учетом пропусков)                      */
    public  double getCompetentOfCollum(MatrixCompetent map, int j, int jSkip){

        double xMinusAvarage, yMinusAvarage, avarageX, avarageY;
        double numerator=0,denominator1=0, denominator2=0,denominator=0;

        //получаем средние значения столбцов
        avarageX = getAverageCollum(map, j);
        avarageY=  getAverageCollum(map, jSkip);

        int numberOfKnownProperties=0;
        for (int i = 0; i <M ; i++) {
            if ((map.get(i,j)==0) && (map.get(i,jSkip)==0)) {
                xMinusAvarage = get(i, j) - avarageX;
                yMinusAvarage = get(i, jSkip) - avarageY;
                numerator = numerator + (xMinusAvarage * yMinusAvarage);
                denominator1 = denominator1 + Math.pow(xMinusAvarage, 2);
                denominator2 = denominator2 + Math.pow(yMinusAvarage, 2);
                numberOfKnownProperties++;
            }
        }
        denominator = Math.sqrt(denominator1*denominator2);
        double correlation = Math.abs(numerator/denominator);
        return correlation*numberOfKnownProperties;
    }

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!ПРОВЕРЕНО!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // перегруженный для удобства метод
    public Map<Integer,Double> getCompetentOfCollum(MatrixCompetent map, int jSkip){
        Map<Integer,Double> mapOfCollumCompetents = new LinkedHashMap<>();
        double competent=0;
        for (int j = 0; j <N ; j++) {
            if (j==jSkip) continue;
            competent=getCompetentOfCollum(map, j, jSkip);
            mapOfCollumCompetents.put(j,competent);
        }
        return mapOfCollumCompetents;
    }
    /*************************         Для строк   *****************************************************************/

    // !!!!!!!!!!!!!!! ПРОВЕРЕНО!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     /* возвращает компетентность строки iRow относительно строки с пропуском iSkip
       L(il)=t(il)/evclid ; evclid = sqrt(SUM(xi-yi)^2)
       над элементами с пропусками вычисления не производятся     */
    public double getCompetentOfRow(Matrix map, int iSkip, int iRow){
        double evclid = 0, sum=0;
        int numberOfKnownProperties=0;
        for (int j = 0; j <N ; j++) {
            if ((map.get(iSkip,j)==0 && map.get(iRow,j)==0)) {
                sum += Math.pow(get(iSkip, j) - get(iRow, j), 2);
                numberOfKnownProperties++;      //количество свойств, учавствующих в вычислении
            }
        }
        evclid = Math.sqrt(sum);
        return numberOfKnownProperties/evclid;
    }

    //возвращает дисперсию i-oй строки, учитывая пропуски
    public double getDespersionRow(Matrix map, int i){
        double avarage, sum=0;
        avarage=getAverageRow(map,i);
        int miss=0;
        for (int j = 0; j < N; j++) {
            if( map.get(i,j)==0) sum += Math.pow((get(i, j) - avarage), 2);
            else miss++;
        }
        return sum / (N-miss);
    }

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!ПРОВЕРЕНО!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // возвращает среднее значение элементов указанной строки (учитывая пропуски)
    public double getAverageRow(Matrix map, int i){
        double sum=0,miss=0;
        for (int j = 0; j <N ; j++) {
            if(map.get(i,j)==0) sum+=get(i,j);
            else miss++;
        }
        //return BigDecimal.valueOf(sum/(matrix.M-miss)).setScale(10,BigDecimal.ROUND_HALF_DOWN).doubleValue();
        return sum/(N-miss);
    }

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!ПРОВЕРЕНО!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // возвращает среднее значение квадрата элемента указанной строки
    public  double getAverageX2Row(Matrix map, int i){
        double sum=0, miss=0;
        for (int j = 0; j <N ; j++) {
            if (map.get(i,j)==0) sum+=Math.pow(get(i,j),2);
            else miss++;
        }
        return sum/(N-miss);
    }

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!ПРОВЕРЕНО!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //возвращает среднее значение произведения элементов из указанных строк
    public  double getAverageXYRow(Matrix map, int iCollum, int iSkip){
        double sum=0, miss=0;
        for (int j = 0; j <N ; j++) {
            if ((map.get(iCollum,j)==0) && (map.get(iSkip,j)==0)) sum+=get(iCollum,j)*get(iSkip,j);
            else miss++;
        }
        return sum/(N-miss);
    }



    public  Map<Integer,Double> getCompetentOfRow(Matrix map, int iSkip){
        Map<Integer,Double> mapOfRowsCompetents = new LinkedHashMap<>();
        for (int i = 0; i <M ; i++) {
            if (i==iSkip) continue;  // Компетентность троки отосительно самой себя не нужно
            mapOfRowsCompetents.put(i,getCompetentOfRow(map,iSkip,i));
        }
        return mapOfRowsCompetents;
    }

    public  void deNormalizeByDispersion(Matrix map){
        //считаем дисперсию
        double average, disperse;
        for (int j = 0; j <N ; j++) {

            //делим каждый элеметн столбца на дисперсию
            for (int i = 0; i < M; i++) {
                double temp ;
                temp= get(i, j) * listOfDispersion.get(j) +listOfAvarage.get(j); // ИЗМЕНИЛ ФОРМУЛУ
                set(i, j, temp);
            }
        }
    }


}
