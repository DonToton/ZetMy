package form;

import matrix.MatrixTelemetry;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;

/**
 *
 */
public class Graph extends javax.swing.JFrame {

    private MatrixTelemetry startMatrix;        //  начальная матрица
    private MatrixTelemetry mapOfStartMatrix;   // 22.05.2018 карта пропусков начальной матрицы
    private MatrixTelemetry resultMatrix;       //  конечная матрица

    /**
     * Creates new form Graph
     */
    public Graph() {
        initComponents();
    }

    //  конструктор для передачи матриц
    public Graph(MatrixTelemetry startMatrix,MatrixTelemetry mapOfStartMatrix, MatrixTelemetry resultMatrix) {
        initComponents();       //  функция добавления элементов управления, их расположения
        populateComboBox();     //  заполнение выпадающего списка данными из массива captions
        this.startMatrix = startMatrix;     //  передача значения начальной матрице
        this.resultMatrix = resultMatrix;   //  передача значения конечной матрице
        this.mapOfStartMatrix = mapOfStartMatrix;

    }

    //  заполнение выпадающего списка данными из массива captions
    private void populateComboBox() {
        comboBox.removeAllItems();
        for (String caption : MatrixTelemetry.captions) {
            comboBox.addItem(caption);
        }
    }

    //  функция для рисования графика (передача набора данных для графика, названия графика, количества линий на графике)
    private void draw(DefaultCategoryDataset dataset, String name, int nRow) {
        JFreeChart chart = ChartFactory.createLineChart(name,
                "Number", "Value", dataset, PlotOrientation.VERTICAL, true, true, false);       //  построение линейного графика
        chart.setBackgroundPaint(Color.white);
        final CategoryPlot plot = (CategoryPlot) chart.getPlot();       //  добавление сетки графика
        plot.setBackgroundPaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.white);
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();      //  указание осей графика
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setAutoRangeIncludesZero(true);
        final LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();        //  рендеринг графика (необходимо для изменения толщины линий)
        for (int i = 0; i < nRow; i++) {
            renderer.setSeriesStroke(i, new BasicStroke(4.0f));     //  задание толщины линий на графике
        }

        //  передача полученного графика на существующую панель на форме
        ChartPanel chartPanel = new ChartPanel(chart);
        panel.removeAll();
        panel.add(chartPanel, BorderLayout.CENTER);
        panel.validate();
    }

    //  функция добавления элементов управления на форму, их расположения
    private void initComponents() {

        panel = new javax.swing.JPanel();
        comboBox = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Graph");

        panel.setBackground(new Color(153, 153, 153));
        panel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.LINE_AXIS));
//        javax.swing.BoxLayout panelLayout = new javax.swing.BoxLayout(panel);
//        panel.setLayout(panelLayout);
//        panelLayout.
//        panelLayout.setHorizontalGroup(
//                panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                        .addGap(0, 654, Short.MAX_VALUE)
//        );
//        panelLayout.setVerticalGroup(
//                panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                        .addGap(0, 0, Short.MAX_VALUE)
//        );

        comboBox.setFont(new Font("Tahoma", 0, 12)); // NOI18N

        jButton1.setFont(new Font("Tahoma", 0, 12)); // NOI18N
        jButton1.setText("Show");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(panel, javax.swing.GroupLayout.PREFERRED_SIZE, 750, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(comboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap())
                        .addGroup(layout.createSequentialGroup()
                                .addGap(91, 91, 91)
                                .addComponent(comboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1)
                                .addContainerGap(205, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new Component[]{comboBox, jButton1});

        pack();
    }

    //  функция, срабатывающая при нажатии кнопки Show
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        String caption = comboBox.getSelectedItem().toString();     //  взятие выбранного caption
        int col = comboBox.getSelectedIndex();      //  взятие индекса выбранного caption
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();      //  переменная для данных (позже передадим для отображения на графике)
        for (int i = 0; i < startMatrix.M; i++) {
            if (mapOfStartMatrix.get(i,col)!=1) dataset.addValue(startMatrix.get(i, col), "initial", "" + (i + 1));     //  добавление данных в график (начальная матрица)
            else dataset.addValue(0, "initial", "" + (i + 1)); //22.05.2018 если
            dataset.addValue(resultMatrix.get(i, col), "result", "" + (i + 1));     //  добавление данных в график (конечная матрица)
        }
        draw(dataset, "Analyze for column: " + caption, startMatrix.M);     //  вызов функции рисования и отображения графика на форме
    }

    private javax.swing.JComboBox comboBox;
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel panel;
}
