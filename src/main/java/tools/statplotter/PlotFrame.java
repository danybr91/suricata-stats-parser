package tools.statplotter;

import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import tools.statplotter.parser.FileParser;
import tools.statplotter.parser.FileParserFactory;
import tools.statplotter.parser.StatTimeSerie;
import tools.statplotter.parser.Time;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PlotFrame extends JFrame {

    private static final int LEGEND_COLOR_COMPONENT_POSITION = 0;
    private static final int LEGEND_TOOGLE_POSITION = 1;

    private JLabel statusLabel;
    private JLabel fileInfoLabel;
    private final JPanel legendPanel, sidebarDataSheet;
    private final List<JComponent> legendEntries;
    private LegendTitle chartLegend;
    private JTextField searchField;
    private final ButtonGroup showDataButtonGroup;
    private ButtonModel currentDataButtonModel;
    private final JTable dataSheetTable;
    private final JLabel dataSheetTitle;
    private final SimpleDateFormat dateFormatter;

    JFreeChart chart;
    XYSeriesCollection collection;

    File currentFile;
    PlotOptions plotOptions;

    public PlotFrame () {
        legendEntries = new ArrayList<>();

        collection = new XYSeriesCollection();
        plotOptions = new PlotOptions();

        dateFormatter = new SimpleDateFormat(plotOptions.getDateformat());
        dateFormatter.setTimeZone(plotOptions.getTimeZone());

        // Window settings
        setWindowConfig();
        paintWindowIcon();

        // Main Window
        JPanel windowContent;
        windowContent = new JPanel();
        windowContent.setBorder(new EmptyBorder(0, 0, 0, 0));
        windowContent.setLayout(new BorderLayout(0,0));
        //windowContent.setBackground(Color.WHITE);
        add(windowContent);
        setDropTarget(new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {

            }

            @Override
            public void dragOver(DropTargetDragEvent dtde) {

            }

            @Override
            public void dropActionChanged(DropTargetDragEvent dtde) {

            }

            @Override
            public void dragExit(DropTargetEvent dte) {

            }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                Transferable transferable = dtde.getTransferable();
                if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    dtde.acceptDrop(dtde.getDropAction());
                    try {
                        List transferData = (List) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                        if (transferData.size() == 1) {
                            loadFromFile((File) transferData.get(0));
                            dtde.dropComplete(true);
                        }
                        else{
                            onError("Error. Solo se puede abrir un fichero.");
                        }
                    } catch (Exception ex) {
                        onError(ex);
                    }
                } else {
                    dtde.rejectDrop();
                }
            }
        }, true, null));

        // Main Panel
        JPanel windowPanel;
        windowPanel = new JPanel();
        windowPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        windowPanel.setLayout(new BorderLayout(30,10));
        windowPanel.setBackground(Color.WHITE);
        windowContent.add(windowPanel, BorderLayout.CENTER);

        // MenuBar
        setJMenuBar(createMenuBar());

        // Chart
        JPanel chartArea = new JPanel();
        chartArea.setLayout(new BorderLayout(0,0));

        chart = createStatChart(collection);
        ChartPanel plotPanel = new ChartPanel(chart);
        chartArea.add(plotPanel, BorderLayout.CENTER);

        windowPanel.add(chartArea, BorderLayout.CENTER);

        // Legend sidebar
        JPanel legendSidebar = new JPanel();
        legendSidebar.setBorder(new EmptyBorder(0, 0, 0, 0));
        legendSidebar.setLayout(new BorderLayout());
        // Limita el tamaño a un 30% de la ventana
        Dimension dimension = new Dimension(340, this.getHeight());
        legendSidebar.setPreferredSize(dimension);
        legendSidebar.setMaximumSize(dimension);

        legendSidebar.add(createLegendSearchSidebar(), BorderLayout.NORTH);

        legendPanel = createLegendSidebar();
        JScrollPane legendPanelContainer = new JScrollPane(legendPanel);
        legendPanelContainer.setBorder(new EmptyBorder(0, 9, 0, 0));
        legendPanelContainer.setLayout(new ScrollPaneLayout());
        //legendPanelContainer.setBackground(Color.WHITE);
        legendSidebar.add(legendPanelContainer, BorderLayout.CENTER);

        windowContent.add(legendSidebar, BorderLayout.WEST);

        // Data sidebar
        showDataButtonGroup = new ButtonGroup();
        sidebarDataSheet = new JPanel();
        sidebarDataSheet.setBorder(new EmptyBorder(0, 0, 0, 0));
        sidebarDataSheet.setLayout(new BorderLayout());
        // Limita el tamaño a un 30% de la ventana
        dimension = new Dimension(340, this.getHeight());
        sidebarDataSheet.setPreferredSize(dimension);
        sidebarDataSheet.setMaximumSize(dimension);

        dataSheetTitle = new JLabel();
        dataSheetTable = new JTable();
        dataSheetTable.setAutoCreateRowSorter(true);
        dataSheetTable.setDragEnabled(false);

        JPanel sidebarDataSheetContainer = createDataSheetSidebar(dataSheetTitle, dataSheetTable);
        sidebarDataSheetContainer.setBorder(new EmptyBorder(0, 9, 0, 0));
        //sidebarDataSheetContainer.setLayout(new ScrollPaneLayout());
        sidebarDataSheet.add(sidebarDataSheetContainer, BorderLayout.CENTER);
        sidebarDataSheet.setVisible(false);
        windowContent.add(sidebarDataSheet, BorderLayout.EAST);

        // StatusBar
        windowContent.add(createStatusBar(), BorderLayout.SOUTH);
        setStatusMessage("Aplicación iniciada");

        windowPanel.validate();
    }

    private JFreeChart createStatChart(XYSeriesCollection dataset) {
        JFreeChart chart = ChartFactory.createTimeSeriesChart("", // Null title prevents of showing
                "Fecha", // x-axis Label
                "Valor", // y-axis Label
                dataset, // Dataset
                true, // Show Legend
                true, // Use tooltips
                false // Configure chart to generate URLs?
        );
        chartLegend = chart.getLegend();
        chart.removeLegend();
        chart.getXYPlot().getRangeAxis().setAutoTickUnitSelection(true);
        chart.getPlot().setBackgroundPaint(Color.WHITE);
        chart.getPlot().setOutlinePaint(Color.BLACK);

        chart.getXYPlot().setDomainGridlinePaint(Color.GRAY);
        chart.getXYPlot().setDomainGridlinesVisible(true);
        chart.getXYPlot().setRangeGridlinePaint(Color.GRAY);
        chart.getXYPlot().setRangeGridlinesVisible(true);
        chart.getXYPlot().setRenderer(new XYLineAndShapeRenderer());

        setDateAxis(chart.getXYPlot());
        setValueAxis(chart.getXYPlot());
        return chart;
    }

    private void setDateAxis(XYPlot plot){
        DateAxis dateAxis = new DateAxis();
        dateAxis.setDateFormatOverride(dateFormatter);
        dateAxis.setAutoRange(true);
        plot.setDomainAxis(dateAxis);
    }

    private void setValueAxis(XYPlot plot){
        if (plotOptions.isLogScaleEnabled()){
            LogAxis logAxis = new LogAxis("Total");
            logAxis.setBase(10);
            logAxis.setTickUnit(new NumberTickUnit(plotOptions.getLogScale()));
            logAxis.setMinorTickMarksVisible(true);
            logAxis.setAutoRange(true);
            plot.setRangeAxis(logAxis);
        }
        else{
            NumberAxis numAxis = new NumberAxis("Total");
            numAxis.setMinorTickMarksVisible(true);
            numAxis.setAutoRange(true);
            plot.setRangeAxis(numAxis);
        }
    }

    private JPanel createLegendSearchSidebar(){
        JPanel searchPanel= new JPanel();
        //statusPanel.setMinimumSize(new Dimension(0, 50));
        searchPanel.setBorder(new EmptyBorder(6, 10, 6, 20));
        searchPanel.setLayout(new BorderLayout(6,0));

        searchField = new JTextField();
        searchField.setToolTipText("Pulsar ENTER para efectuar el filtro");
        searchField.addActionListener(e -> filterLegendEntries(searchField.getText().toLowerCase()));
        searchPanel.add(new JLabel("Filtro:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        return searchPanel;
    }

    private JPanel createLegendSidebar(){
        // Legend panel
        JPanel legendPanel = new JPanel();
        //statusPanel.setMinimumSize(new Dimension(0, 50));
        legendPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));

        return legendPanel;
    }

    private boolean addLegendEntry(JComponent parent, int statIndex, String statName, String tooltip){
        if (statIndex < 0) {
            return false;
        }
        // Ya que el estado por defecto es mostrar, nos aseguramos de que se muestra en el gŕafico.
        chart.getXYPlot().getRenderer().setSeriesVisible(statIndex, true);

        JPanel entry = new JPanel();
        entry.setLayout(new FlowLayout(FlowLayout.LEFT));
        entry.setBorder(new EmptyBorder(0, 0, 0, 0));
        Dimension d = new Dimension(parent.getWidth(), 35);
        entry.setMaximumSize(d);

        JLabel color = new JLabel();
        d = new Dimension(15, 15);
        color.setBorder(new EmptyBorder(0, 0, 0, 2));
        color.setMinimumSize(d);
        color.setPreferredSize(d);
        color.setMaximumSize(d);
        // Para recoger el color, la serie debe de estar pintada en el gráfico.
        Paint paint = chart.getXYPlot().getLegendItems().get(statIndex).getFillPaint();
        color.setBackground((Color) paint);
        color.setOpaque(true);
        entry.add(color);

        JToggleButton clickableLabel = new JToggleButton(statName);
        clickableLabel.setSelected(true);
        clickableLabel.setHorizontalAlignment(SwingConstants.LEFT);
        d = new Dimension(parent.getWidth() - 85, 30);
        clickableLabel.setMinimumSize(d);
        clickableLabel.setPreferredSize(d);
        clickableLabel.setMaximumSize(d);
        clickableLabel.addActionListener(e -> {
            chart.getXYPlot().getRenderer().setSeriesVisible(statIndex, clickableLabel.isSelected());
            color.setOpaque(clickableLabel.isSelected());
            color.updateUI();
            /*if (legendEntries.size() > 1) {
                parent.remove(entry);
                if (clickableLabel.isSelected()) {
                    parent.add(entry, 0);
                } else {
                    parent.add(entry);
                }
                parent.validate();
            }*/
        });
        clickableLabel.setToolTipText(tooltip);
        entry.add(clickableLabel);

        JToggleButton showDataButton = new JToggleButton();
        showDataButton.setBorder(new EmptyBorder(0,7,0,0));
        showDataButton.setIconTextGap(0);
        showDataButton.setVerticalTextPosition(SwingConstants.CENTER);
        showDataButton.setHorizontalTextPosition(SwingConstants.CENTER);
        try {
            Image selected = ImageIO.read(Objects.requireNonNull(getClass().getResource("/datasheet_show.png")));
            Image unselected = ImageIO.read(Objects.requireNonNull(getClass().getResource("/datasheet_hide.png")));
            showDataButton.setSelectedIcon(new ImageIcon(selected));
            showDataButton.setIcon(new ImageIcon(unselected));
            unselected.flush();
            selected.flush();
        } catch (Exception ex) {
            System.out.println(ex);
        }
        showDataButton.setHorizontalAlignment(SwingConstants.LEFT);
        d = new Dimension(30, 30);
        showDataButton.setMinimumSize(d);
        showDataButton.setPreferredSize(d);
        showDataButton.setMaximumSize(d);
        showDataButton.addActionListener(e -> {
            if (currentDataButtonModel == null || !showDataButtonGroup.getSelection().equals(currentDataButtonModel)){
                // Show his data
                sidebarDataSheet.setVisible(true);
                currentDataButtonModel = ((JToggleButton) e.getSource()).getModel();
                showDataButtonGroup.clearSelection();
                showDataButtonGroup.setSelected(currentDataButtonModel, true);
                setDataSheetValues(statIndex);
            } else {
                closeDataSheetTableAndClearSelection();
            }
        });
        showDataButtonGroup.add(showDataButton);
        entry.add(showDataButton);

        legendEntries.add(entry);
        parent.add(entry);
        return true;
    }

    private JPanel createDataSheetSidebar(JLabel dataSheetTitle, JTable dataSheetTable){
        // Legend panel
        JPanel dataPanel = new JPanel();
        //statusPanel.setMinimumSize(new Dimension(0, 50));
        dataPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));

        JPanel headerPanel = new JPanel();
        //statusPanel.setMinimumSize(new Dimension(0, 50));
        headerPanel.setBorder(new EmptyBorder(6, 6, 6, 6));
        headerPanel.setLayout(new BorderLayout(6,0));
        headerPanel.setMaximumSize(new Dimension(500, 30));

        headerPanel.add(dataSheetTitle);
        /*JButton closeButton = new JButton("X");
        Dimension d = new Dimension(30, 30);
        closeButton.setMinimumSize(d);
        closeButton.setPreferredSize(d);
        closeButton.setMaximumSize(d);
        closeButton.addActionListener(e -> closeDataSheetTableAndClearSelection());
        headerPanel.add(closeButton, BorderLayout.EAST);*/
        dataPanel.add(headerPanel, BorderLayout.NORTH);

        JScrollPane tableScrollPane = new JScrollPane(dataSheetTable);
        tableScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        dataPanel.add(tableScrollPane);
        return dataPanel;
    }

    private void setDataSheetValues(int statIndex){
        Object[][] data;
        if (statIndex == -1){
            data = new Object[][]{};
            dataSheetTitle.setText("");
        }
        else {
            XYSeries serie = collection.getSeries(statIndex);
            data = new Object[serie.getItemCount()][2];
            for (int i = 0; i < serie.getItemCount(); i++){
                data[i][0] = dateFormatter.format(new Date(serie.getX(i).longValue()));
                data[i][1] = serie.getY(i).longValue();
            }
            dataSheetTitle.setText(serie.getKey().toString());
        }
        String[] columnNames = {"Time", "Value"};
        DefaultTableModel model = new DefaultTableModel(data,columnNames) {
            @Override
            public Class getColumnClass(int column) {
                if (column == 1) {
                    return Long.class;
                }
                return String.class;
            }
        };
        dataSheetTable.setModel(model);
        dataSheetTable.updateUI();
    }

    private void closeDataSheetTableAndClearSelection() {
        sidebarDataSheet.setVisible(false);
        currentDataButtonModel = null;
        showDataButtonGroup.clearSelection();
        setDataSheetValues(-1);
    }

    private void checkAllLegendEntries(boolean check, boolean excludeFiltered){
        XYItemRenderer renderer = chart.getXYPlot().getRenderer();
        JComponent component;
        JToggleButton button;
        JLabel color;
        for  (int index = 0; index < collection.getSeriesCount(); index++){
            component = legendEntries.get(index);
            if (!excludeFiltered || component.isVisible())
            {
                renderer.setSeriesVisible(index, check);
                color = (JLabel) component.getComponent(LEGEND_COLOR_COMPONENT_POSITION);
                button = (JToggleButton) component.getComponent(LEGEND_TOOGLE_POSITION);
                color.setOpaque(check);
                color.updateUI();
                button.setSelected(check);
            }
        }
    }

    private void clearLegendEntries(){
        for (JComponent component : legendEntries){
            legendPanel.remove(component);
        }
        legendEntries.clear();
    }

    private void filterLegendEntries(String pattern){
        JComponent component;
        JToggleButton button;
        for  (int index = 0; index < collection.getSeriesCount(); index++){
            component = legendEntries.get(index);
            button = (JToggleButton) component.getComponent(LEGEND_TOOGLE_POSITION);
            component.setVisible(button.getText().toLowerCase().contains(pattern));
        }
    }

    private JMenuBar createMenuBar(){
        JMenuBar menuBar = new JMenuBar();
        // Archivo
        JMenu archivo = new JMenu("Archivo");

        JMenuItem abrir = new JMenuItem("Abrir");
        abrir.addActionListener(e -> loadFromFile(openFile()));
        abrir.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
        archivo.add(abrir);

        JMenuItem cerrar = new JMenuItem("Cerrar");
        archivo.add(cerrar);
        cerrar.addActionListener(e -> clearData());

        archivo.add(new JSeparator(JSeparator.HORIZONTAL));

        JMenuItem salir = new JMenuItem("Salir");
        salir.addActionListener(e -> this.dispose());
        salir.setAccelerator(KeyStroke.getKeyStroke("alt F4"));
        archivo.add(salir);

        menuBar.add(archivo);

        // Opciones
        JMenu options = new JMenu("Opciones");

        JMenuItem seleccionarTodo = new JMenuItem("Seleccionar todo");
        seleccionarTodo.addActionListener(e -> checkAllLegendEntries(true, false));
        options.add(seleccionarTodo);

        JMenuItem deseleccionarTodo = new JMenuItem("Deseleccionar todo");
        deseleccionarTodo.addActionListener(e -> checkAllLegendEntries(false, false));
        options.add(deseleccionarTodo);

        options.add(new JSeparator(JSeparator.HORIZONTAL));

        JMenuItem seleccionarFiltrados = new JMenuItem("Seleccionar filtrados");
        seleccionarFiltrados.addActionListener(e -> checkAllLegendEntries(true, true));
        options.add(seleccionarFiltrados);

        JMenuItem deseleccionarFiltrados = new JMenuItem("Deseleccionar filtrados");
        deseleccionarFiltrados.addActionListener(e -> checkAllLegendEntries(false, true));
        options.add(deseleccionarFiltrados);

        options.add(new JSeparator(JSeparator.HORIZONTAL));

        JCheckBoxMenuItem ocultarLeyenda = new JCheckBoxMenuItem("Ocultar leyenda");
        ocultarLeyenda.setState(true);
        ocultarLeyenda.setToolTipText("Muestra o oculta la leyenda del gŕafico. Útil para exportarlo.");
        ocultarLeyenda.addActionListener(e -> {
            if (chart != null){
                if (ocultarLeyenda.getState()){
                    chartLegend = chart.getLegend();
                    chart.removeLegend();
                }
                else{
                    chart.addLegend(chartLegend);
                }
            }
        });
        options.add(ocultarLeyenda);

        options.add(new JSeparator(JSeparator.HORIZONTAL));

        JCheckBoxMenuItem logScale = new JCheckBoxMenuItem("Escala logaritmica", plotOptions.isLogScaleEnabled());
        logScale.addActionListener(e -> {
            plotOptions.setLogAxis(logScale.getState());
            setValueAxis(chart.getXYPlot());
        });
        options.add(logScale);



        menuBar.add(options);
        return menuBar;
    }

    private JPanel createStatusBar(){
        // Status Bar
        JPanel statusPanel = new JPanel();
        //statusPanel.setMinimumSize(new Dimension(0, 50));
        statusPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        statusPanel.setBorder(new EmptyBorder(2, 9, 2, 9));
        statusPanel.setLayout(new BorderLayout(6,0));

        statusLabel = new JLabel();
        statusLabel.setBorder(new EmptyBorder(1, 1, 1, 1));
        statusPanel.add(statusLabel, BorderLayout.WEST);

        fileInfoLabel = new JLabel();
        fileInfoLabel.setBorder(new EmptyBorder(1, 1, 1, 1));
        fileInfoLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        statusPanel.add(fileInfoLabel, BorderLayout.EAST);
        statusPanel.add(fileInfoLabel, BorderLayout.EAST);

        return statusPanel;
    }

    private void setWindowConfig() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setMinimumSize(new Dimension(10, 0));
        setSize(new Dimension(1024, 600));
        setTitle("Stat Plotter");
    }

    private void paintWindowIcon() {
        BufferedImage image;
        try {
            image = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/icon.png")));
            setIconImage(new ImageIcon(image).getImage());
        } catch (IOException e) {
            onError(e);
        }
    }

    private File openFile(){
        JFileChooser openFileDialog = new JFileChooser(Paths.get(".").toString());
        openFileDialog.setFileFilter(new FileNameExtensionFilter("Archivos de texto", "txt", "log"));
        if(openFileDialog.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            return openFileDialog.getSelectedFile();
        }
        else{
            return null;
        }
    }

    protected void onError(@NotNull String message){
        System.err.println(message);
        setStatusMessage("Error: " + message);
    }

    protected void onError(@NotNull Exception e){
        System.err.println(e.getMessage());
        e.printStackTrace();
        setStatusMessage("Error: " + e.getMessage());
    }

    void setStatusMessage(String message){
        if (message.length() > 100) {
            statusLabel.setText(message.substring(0, 100) + "...");
        }
        statusLabel.setToolTipText(message);
    }

    void setFileMessage(File file){
        if (file != null){
            String filePath = file.getAbsolutePath();
            if (filePath.length() > 200) {
                fileInfoLabel.setText(file.getAbsolutePath().substring(200));
            }
            fileInfoLabel.setText(file.getAbsolutePath());
        }
        else{
            fileInfoLabel.setText("");
            fileInfoLabel.setToolTipText("");
        }
    }

    void loadFromFile(File file){
        if (file != null && file.canRead()) {
            try {
                chart.setNotify(false);
                clearData();
                try (InputStream stream = new FileInputStream(file)) {
                    currentFile = file;
                    setStatusMessage("Cargando fichero...");
                    setFileMessage(file);
                    FileParser parser = FileParserFactory.getParser(stream, "log");
                    for (StatTimeSerie stat : parser.readStats().stream().sorted().collect(Collectors.toCollection(ArrayList::new))) {
                        addStatSerie(stat);
                    }
                    setStatusMessage("Fichero cargado");
                }
            } catch (Exception e) {
                onError(e);
            } finally {
                chart.setNotify(true);
            }
        }
    }

    XYSeries addStatSerie(StatTimeSerie stat){
        //time.add(stats.get)
        XYSeries statSerie = new XYSeries(stat.getName());
        for (Time time : stat.getTimes()) {
            statSerie.add(time.getDate().getTime(), stat.getValue(time));
            statSerie.setDescription(time.getDate().toString());
        }
        collection.addSeries(statSerie);
        int serieIndex = collection.getSeriesCount() - 1;
        if(addLegendEntry(legendPanel, serieIndex, stat.getName(), "Min: " + stat.getMinValue() + " / Max: " + stat.getMaxValue())) {
            chart.getXYPlot().getRenderer().setSeriesToolTipGenerator(
                    serieIndex,
                    (xyDataset, i, i1) -> "Valor: " + xyDataset.getYValue(i, i1) + " - Fecha: " + new Date((long) xyDataset.getXValue(i, i1))
            );
            setStatusMessage("Añadido nueva serie: " + stat.getName());
            return statSerie;
        }
        else{
            collection.removeSeries(statSerie);
            onError("Error al añadir serie: " + stat.getName());
            return null;
        }
    }

    void clearData(){
        collection.removeAllSeries();
        clearLegendEntries();
        //chart = createStatChart(collection);
        closeDataSheetTableAndClearSelection();
        currentFile = null;
        searchField.setText("");
        setStatusMessage("Datos limpiados");
        setFileMessage(null);
    }

    public static void main(String[] args){
        try {
            if (UIManager.getSystemLookAndFeelClassName().equals(UIManager.getCrossPlatformLookAndFeelClassName())) {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            }
            else {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            System.err.println("Warning: " + e.getMessage());
            e.printStackTrace();
        }
        PlotFrame frame = new PlotFrame();
        frame.setVisible(true);
    }
}
