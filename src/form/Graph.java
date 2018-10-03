package form;

import matrix.MatrixTelemetry;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;

/**
 *
 */
public class Graph extends javax.swing.JFrame {

    private MatrixTelemetry startMatrix;        //  начальная матрица
    private MatrixTelemetry mapOfStartMatrix;   // 22.05.2018 карта пропусков начальной матрицы
    private MatrixTelemetry resultMatrix;       //  конечная матрица

    public Graph() {
        initComponents();
    }

    public Graph(MatrixTelemetry startMatrix, MatrixTelemetry mapOfStartMatrix, MatrixTelemetry resultMatrix) {
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

    //  функция для отрисовки графика (передача набора данных для графика, названия графика, количества линий на графике)
    private void draw(XYDataset dataset, String name, int nRow) {

        final JFreeChart chart = ChartFactory.createXYLineChart(name,
                "Number", "Value", dataset, PlotOrientation.VERTICAL, true, true, false);       //  построение линейного графика
        chart.setBackgroundPaint(Color.white);

        //  добавление сетки графика
        final XYPlot plot = chart.getXYPlot();

        plot.setBackgroundPaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.white);

        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();      //  указание осей графика
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setAutoRangeIncludesZero(true);

        //  рендеринг графика (необходимо для изменения толщины линий)
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();

        renderer.setSeriesStroke(1, new BasicStroke(4f));
        renderer.setSeriesStroke(
                0, new BasicStroke(1.5f, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND,
                        1.0f, new float[]{20.0f, 10.0f}, 0.0f)
        );

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

        //  переменная для данных (позже передадим для отображения на графике)
        XYSeriesCollection dataset = new XYSeriesCollection();
        final XYSeries series1 = new XYSeries("initial");
        final XYSeries series2 = new XYSeries("result");

        for (int i = 0; i < startMatrix.M; i++) {
            //  добавление данных в графики
            if (mapOfStartMatrix.get(i, col) != 1) series1.add((i + 1), startMatrix.get(i, col));
            else series1.add((i + 1), 0);
            series2.add((i + 1), resultMatrix.get(i, col));
        }

        dataset.addSeries(series1);
        dataset.addSeries(series2);

        draw(dataset, "Analyze for column: " + caption, startMatrix.M);     //  вызов функции рисования и отображения графика на форме
    }

    private javax.swing.JComboBox comboBox;
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel panel;
}
