package ui.frames;

import com.jgoodies.forms.factories.Borders;
import connection.SendToServer;
import guiUtils.ImageRotate;
import guiUtils.ImageRotateSmall;
import guiUtils.charts.BarChartPanel;
import guiUtils.notifications.WinNotifications;
import jnetpcap.BasicFlow;
import jnetpcap.FlowFeature;
import jnetpcap.worker.TrafficFlowWorker;
import jnetpcap.manager.FlowMgr;

import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.InsertCsvRow;
import utils.InsertTableRow;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;


public class HomeFrame {

    protected static final Logger logger = LoggerFactory.getLogger(HomeFrame.class);
    private TrafficFlowWorker trafficWorker;
    private ExecutorService csvWriterThread;
    private DefaultTableModel tableModel;
    private String path1 =  FlowMgr.getInstance().getSavePath() + "appSetup.txt";

    // === frames ===
    private JFrame mainFrame;

    // === panels ===
    private JPanel menuPanel;
    private JPanel networkFlow_panel;
    private JPanel mainPanel;

    private JPanel submenu2;

    private JScrollPane notification_scrollpanel;
    private JPanel notification_panel;

    private JPanel protectionsts_panel;

    private JPanel activity_panel;
    private JPanel srcip_panel;
    private JPanel dstip_panel;
    private JPanel dstport_panel;
    private JPanel srcport_panel;
    private JPanel protocol_panel;
    private JPanel timestamp_panel;



    //=== tables ===
    private JTable table;
    private JScrollPane table_panel;

    //=== charts ===
    private static int chartData_ready=0;
    private static List<String> srcip_list = new ArrayList<String>();
    private static List<String> dstip_list = new ArrayList<String>();
    private static List<String> srcport_list = new ArrayList<String>();
    private static List<String> dstport_list = new ArrayList<String>();
    private static List<String> protocol_list = new ArrayList<String>();
    private static List<String> timestamp_list = new ArrayList<String>();
    BarChartPanel srcip_chart;
    BarChartPanel srcport_chart;
    BarChartPanel dstip_chart;
    BarChartPanel dstport_chart;
    BarChartPanel protocol_chart;
    BarChartPanel timestamp_chart;


    //=== labels ===
    private JLabel logo;
    private JLabel status_lbl;
    private JLabel cnt_lbl;
    private JLabel title;
    private JLabel array2_lbl;

    private JLabel circle_lbl;
    private JLabel bar_lbl;
    private JLabel lock_lbl;

    private JLabel sts_lbl;
    private JLabel sts_lbl1;
    private JLabel sts_lbl2;
    private JLabel sts_lbl3;
    private JLabel sts_lbl4;
    private JLabel sts_lbl5;

    private JLabel chartsip_lbl;
    private JLabel chartsp_lbl;
    private JLabel chartdip_lbl;
    private JLabel chartdp_lbl;
    private JLabel chartprot_lbl;
    private JLabel charttim_lbl;

    private JLabel srcip_leglbl;
    private JLabel dstip_leglbl;
    private JLabel dstport_leglbl;
    private JLabel srcport_leglbl;
    private JLabel protocol_leglbl;
    private JLabel timestamp_leglbl;

    //=== buttons ===
    private JButton menuItem1;
    private JButton menuItem2;
    private JButton menuItem3;
    private JButton menuItem4;
    private JButton menuItem5;
    private JButton menuItem6;

    private JButton submenu2_btn1;

    public HomeFrame() throws UnsupportedLookAndFeelException, IOException {

        initComponents();
        mainFrame.setVisible(true);

        //TODO: CHECK IF SCANNING STARTS AUTOMATICALLY AND DATA IS SET
        //IF YES:
        init();
        startTrafficScan();
    }

    /* Constructor for csvWriter thread*/
    private void init()
    {

        csvWriterThread = Executors.newSingleThreadExecutor();
    }

    /* Destructor for csvWriter thread*/
    public void destory()
    {
        csvWriterThread.shutdown();
    }

    /* Capture traffic*/
    private void startTrafficScan() throws IOException {

        String networkAdapter = null;
        File app_file = new File(path1);
        if (app_file.length() != 0) {
            BufferedReader reader = new BufferedReader(new FileReader(app_file));
            networkAdapter = reader.readLine();
            reader.close();
        }
        int tmp = networkAdapter.indexOf("}");
        if (tmp != -1) {
            networkAdapter = networkAdapter.substring(0, tmp + 1);
        }

        if (trafficWorker != null && !trafficWorker.isCancelled()) {
            return;
        }

        trafficWorker = new TrafficFlowWorker(networkAdapter);

        trafficWorker.addPropertyChangeListener(event -> {
            TrafficFlowWorker task = (TrafficFlowWorker) event.getSource();
            if ("progress".equals(event.getPropertyName())) {
                status_lbl.setText((String) event.getNewValue());
                status_lbl.validate();
            } else if (TrafficFlowWorker.PROPERTY_FLOW.equalsIgnoreCase(event.getPropertyName())) {
                try {
                    insertFlow((BasicFlow) event.getNewValue());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if ("state".equals(event.getPropertyName())) {
                switch (task.getState()) {
                    case STARTED:
                        break;
                    case DONE:
                        try {
                            status_lbl.setText(task.get());
                            status_lbl.validate();
                        } catch (CancellationException e) {

                            status_lbl.setText("stop listening");
                            status_lbl.setForeground(SystemColor.GRAY);
                            status_lbl.validate();
                            logger.info("Pcap stop listening");

                        } catch (InterruptedException | ExecutionException e) {
                            logger.debug(e.getMessage());
                        }
                        break;
                }
            }
        });
        trafficWorker.execute();
        // status_lbl.setForeground(SystemColor.desktop);
    }

    /* Insert to table and csv captured traffic*/
    private void insertFlow(BasicFlow flow) throws IOException {
        List<String> flowStringList = new ArrayList<>();
        List<String[]> flowDataList = new ArrayList<>();
        List<String[]> tableflowDataList;
        String[] tmp=new String[8];

        String flowDump = flow.dumpFlowBasedFeaturesEx();
        flowStringList.add(flowDump);
        flowDataList.add(StringUtils.split(flowDump, ","));

        //write flows to csv file

        tableflowDataList = new ArrayList<String[]>();
        for(int i=0;i<8;i++)
        {
            tmp[i]=flowDataList.get(0)[i];
        }
        tableflowDataList.add(tmp);

        String header = FlowFeature.getHeader();
        String path = FlowMgr.getInstance().getSavePath();
        String filename = LocalDate.now().toString() + FlowMgr.FLOW_SUFFIX;
        csvWriterThread.execute(new InsertCsvRow(header, flowStringList, path, filename));
        processActivityCharts(header,flowStringList);
        chartData_ready=1;
        //TODO ENABLE SERVER
        //Send data to server
        //SwingUtilities.invokeLater(new SendToServer(flowStringList));
        //insert flows to JTable
        SwingUtilities.invokeLater(new InsertTableRow(tableModel, tableflowDataList, cnt_lbl));

        tableflowDataList=null;
        //  btnSave.setEnabled(true);
    }

    /* Show submenu for menu item 2 (TOOLS) */
    private void menuItem2MouseClicked(MouseEvent e) {
        if(submenu2.isShowing()) {
            array2_lbl.setIcon(new ImageIcon("src/resources/downarrow32.png"));
            submenu2.setVisible(false);
        }
        else{
            array2_lbl.setIcon(new ImageIcon("src/resources/leftarrow32.png"));
            submenu2.setVisible(true);

        }
    }

    /*Close submenu 2 (TOOLS)*/
    private void submenu2MouseExited(MouseEvent e) {
            array2_lbl.setIcon(new ImageIcon("src/resources/downarrow32.png"));
            submenu2.setVisible(false);
    }

    /* Show submenu 2 (TOOLS) */
    private void submenu2_btn1MouseEntered(MouseEvent e) {
            array2_lbl.setIcon(new ImageIcon("src/resources/leftarrow32.png"));
            submenu2.setVisible(true);
    }

    /* Show captured traffic (TOOLS - TRAFFIC VIEWER)*/
    private void submenu2_btn1MouseClicked(MouseEvent e) {
        if(notification_scrollpanel.isShowing())
            notification_scrollpanel.setVisible(false);
        if(activity_panel.isShowing())
            activity_panel.setVisible(false);
        submenu2.setVisible(false);
        networkFlow_panel.setVisible(true);
        setProtectionsts_yellow();
    }

    /* Show item 3 (ACTIVITY)*/
    private void menuItem3MouseClicked(MouseEvent e) throws AWTException {
        if (SystemTray.isSupported()) {
            WinNotifications td = new WinNotifications();
            td.displayNotif("Test","Notification test");
        }
        if(notification_scrollpanel.isShowing())
            notification_scrollpanel.setVisible(false);
        if(networkFlow_panel.isShowing())
             networkFlow_panel.setVisible(false);

        if(srcip_list.stream().distinct().collect(Collectors.toList()).size()>30)
            resetActivityCharts();



        if(chartData_ready==0)
        {
            if(Arrays.asList(activity_panel.getComponents()).contains(srcip_chart))
                activity_panel.remove(srcip_chart);
            if(Arrays.asList(activity_panel.getComponents()).contains(dstip_chart))
                activity_panel.remove(dstip_chart);
            if(Arrays.asList(activity_panel.getComponents()).contains(protocol_chart))
                activity_panel.remove(protocol_chart);
            if(Arrays.asList(activity_panel.getComponents()).contains(srcport_chart))
                activity_panel.remove(srcport_chart);
            if(Arrays.asList(activity_panel.getComponents()).contains(timestamp_chart))
                activity_panel.remove(timestamp_chart);
            if(Arrays.asList(activity_panel.getComponents()).contains(dstport_chart))
                activity_panel.remove(dstport_chart);

            activity_panel.revalidate();

            createLoadingCharts();

            activity_panel.add(srcip_panel);
            activity_panel.add(dstip_panel);
            activity_panel.add(protocol_panel);
            activity_panel.add(srcport_panel);
            activity_panel.add(timestamp_panel);
            activity_panel.add(dstport_panel);

        }
        else
        {
            if(Arrays.asList(activity_panel.getComponents()).contains(srcip_panel))
                activity_panel.remove(srcip_panel);
            if(Arrays.asList(activity_panel.getComponents()).contains(dstip_panel))
                activity_panel.remove(dstip_panel);
            if(Arrays.asList(activity_panel.getComponents()).contains(protocol_panel))
                activity_panel.remove(protocol_panel);
            if(Arrays.asList(activity_panel.getComponents()).contains(srcport_panel))
                activity_panel.remove(srcport_panel);
            if(Arrays.asList(activity_panel.getComponents()).contains(timestamp_panel))
                activity_panel.remove(timestamp_panel);
            if(Arrays.asList(activity_panel.getComponents()).contains(dstport_panel))
                activity_panel.remove(dstport_panel);

            if(Arrays.asList(activity_panel.getComponents()).contains(srcip_chart))
                activity_panel.remove(srcip_chart);
            if(Arrays.asList(activity_panel.getComponents()).contains(dstip_chart))
                activity_panel.remove(dstip_chart);
            if(Arrays.asList(activity_panel.getComponents()).contains(protocol_chart))
                activity_panel.remove(protocol_chart);
            if(Arrays.asList(activity_panel.getComponents()).contains(srcport_chart))
                activity_panel.remove(srcport_chart);
            if(Arrays.asList(activity_panel.getComponents()).contains(timestamp_chart))
                activity_panel.remove(timestamp_chart);
            if(Arrays.asList(activity_panel.getComponents()).contains(dstport_chart))
                activity_panel.remove(dstport_chart);

            activity_panel.revalidate();

            createActivityCharts();

            activity_panel.add(srcip_chart);
            srcip_chart.setBounds(140, 50, 280, 280);
            activity_panel.add(chartsip_lbl);
            chartsip_lbl.setBounds(422, 170, 64, 64);

            activity_panel.add(dstip_chart);
            dstip_chart.setBounds(530, 50, 280, 280);
            activity_panel.add(chartdip_lbl);
            chartdip_lbl.setBounds(812, 170, 64, 64);

            activity_panel.add(protocol_chart);
            protocol_chart.setBounds(920, 50, 280, 280);
            activity_panel.add(chartprot_lbl);
            chartprot_lbl.setBounds(1202, 160, 64, 64);

            activity_panel.add(srcport_chart);
            srcport_chart.setBounds(140, 350, 280, 280);
            activity_panel.add(chartsp_lbl);
            chartsp_lbl.setBounds(422, 460, 64, 64);

            activity_panel.add(timestamp_chart);
            timestamp_chart.setBounds(920, 350, 280, 280);
            activity_panel.add(charttim_lbl);
            charttim_lbl.setBounds(1202, 460, 64, 64);

            activity_panel.add(dstport_chart);
            dstport_chart.setBounds(530, 350, 280, 280);
            activity_panel.add(chartdp_lbl);
            chartdp_lbl.setBounds(812, 460, 64, 64);

        }
        activity_panel.setVisible(true);
    }

    /* Show item 4 (NOTIFICATIONS)*/
    private void menuItem4MouseClicked(MouseEvent e) {

        if(networkFlow_panel.isShowing())
            networkFlow_panel.setVisible(false);
        if(activity_panel.isShowing())
            activity_panel.setVisible(false);

        notification_scrollpanel.setVisible(true);
        setProtectionsts_red();
    }

    /* Show item 5 (USER DATA)*/
    private void menuItem5MouseClicked(MouseEvent e) throws IOException {
       new UserDataFrame();

    }

    /* Show item 6 (APP DATA)*/
    private void menuItem6MouseClicked(MouseEvent e) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException, IOException {
        new AppDataFrame();
    }
    /* print notifications */
    private JLabel add_notification(String txt,int index)
    {
        JLabel notif_lbl=new JLabel();

        //---- notif_lbl ----
        notif_lbl.setBackground(new Color(3, 211, 252,100));
        notif_lbl.setOpaque(true);
        notif_lbl.setForeground(Color.white);
        notif_lbl.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
        TitledBorder tb=new TitledBorder("Notification");
        tb.setTitleColor(Color.white);
        tb.setTitleFont(new Font("JetBrains Mono", Font.BOLD, 14));
        notif_lbl.setBorder(new CompoundBorder(
                tb,
                Borders.DLU21));
        notif_lbl.setText(txt);

        notif_lbl.setBounds(5, 0+157*index, 1360, 155);
        notif_lbl.setVisible(true);

        return notif_lbl;
    }

    /* Set protection status color*/
    private void setProtectionsts_green()
    {
        lock_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/lock_green124x200.png")));
        circle_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/circle_green200x200.png")));
        bar_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/bar_green1000x300.png")));

    }
    private void setProtectionsts_yellow()
    {
        lock_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/lock_yellow124x200.png")));
        circle_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/circle_yellow200x200.png")));
        bar_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/bar_yellow1000x300.png")));
    }
    private void setProtectionsts_red()
    {
        lock_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/lock_red124x200.png")));
        circle_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/circle_red200x200.png")));
        bar_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/bar_red1000x300.png")));
    }
    private void setProtectionsts_pink()
    {
        lock_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/lock_pink124x200.png")));
        circle_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/circle_pink200x200.png")));
        bar_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/bar_pink1000x300.png")));
    }

    private void createLoadingCharts()
    {
        /* charts status labels */
            {sts_lbl.setText("Loading ...");
            sts_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/refresh.png")));
            sts_lbl.setFont(new Font("JetBrains Mono", Font.BOLD, 14));
            sts_lbl.setForeground(Color.white);

            sts_lbl1.setText("Loading ...");
            sts_lbl1.setIcon(new ImageIcon(getClass().getResource("/resources/refresh.png")));
            sts_lbl1.setFont(new Font("JetBrains Mono", Font.BOLD, 14));
            sts_lbl1.setForeground(Color.white);

            sts_lbl2.setText("Loading ...");
            sts_lbl2.setIcon(new ImageIcon(getClass().getResource("/resources/refresh.png")));
            sts_lbl2.setFont(new Font("JetBrains Mono", Font.BOLD, 14));
            sts_lbl2.setForeground(Color.white);

            sts_lbl3.setText("Loading ...");
            sts_lbl3.setIcon(new ImageIcon(getClass().getResource("/resources/refresh.png")));
            sts_lbl3.setFont(new Font("JetBrains Mono", Font.BOLD, 14));
            sts_lbl3.setForeground(Color.white);

            sts_lbl4.setText("Loading ...");
            sts_lbl4.setIcon(new ImageIcon(getClass().getResource("/resources/refresh.png")));
            sts_lbl4.setFont(new Font("JetBrains Mono", Font.BOLD, 14));
            sts_lbl4.setForeground(Color.white);

            sts_lbl5.setText("Loading ...");
            sts_lbl5.setIcon(new ImageIcon(getClass().getResource("/resources/refresh.png")));
            sts_lbl5.setFont(new Font("JetBrains Mono", Font.BOLD, 14));
            sts_lbl5.setForeground(Color.white);}

        //===== srcip_panel =====
        { srcip_panel.setBackground(new Color(3, 21, 64));
            srcip_panel.setLayout(null);
            TitledBorder tb=new TitledBorder("Source IP");
            tb.setTitleColor(Color.white);
            tb.setTitleFont(new Font("JetBrains Mono", Font.BOLD, 14));
            srcip_panel.setBorder(new CompoundBorder(
                    tb,
                    Borders.DLU21));
            srcip_panel.add(sts_lbl);
            sts_lbl.setBounds(50, 10, 150, 100);

            srcip_panel.setBounds(140, 50, 280, 280);}

        //===== dstip_panel =====
        { dstip_panel.setBackground(new Color(3, 21, 64));
            dstip_panel.setLayout(null);
            TitledBorder tb1=new TitledBorder("Destination IP");
            tb1.setTitleColor(Color.white);
            tb1.setTitleFont(new Font("JetBrains Mono", Font.BOLD, 14));
            dstip_panel.setBorder(new CompoundBorder(
                    tb1,
                    Borders.DLU21));
            dstip_panel.add(sts_lbl1);
            sts_lbl1.setBounds(50, 10, 150, 100);

            dstip_panel.setBounds(530, 50, 280, 280);}

        //===== protocol_panel =====
        { protocol_panel.setBackground(new Color(3, 21, 64));
            protocol_panel.setLayout(null);
            TitledBorder tb2=new TitledBorder("Protocol");
            tb2.setTitleColor(Color.white);
            tb2.setTitleFont(new Font("JetBrains Mono", Font.BOLD, 14));
            protocol_panel.setBorder(new CompoundBorder(
                    tb2,
                    Borders.DLU21));
            protocol_panel.add(sts_lbl2);
            sts_lbl2.setBounds(50, 10, 150, 100);

            protocol_panel.setBounds(920, 50, 280, 280);}

        //===== srcport_panel =====
        { srcport_panel.setBackground(new Color(3, 21, 64));
            srcport_panel.setLayout(null);
            TitledBorder tb3=new TitledBorder("Source Port");
            tb3.setTitleColor(Color.white);
            tb3.setTitleFont(new Font("JetBrains Mono", Font.BOLD, 14));
            srcport_panel.setBorder(new CompoundBorder(
                    tb3,
                    Borders.DLU21));
            srcport_panel.add(sts_lbl3);
            sts_lbl3.setBounds(50, 10, 150, 100);
            srcport_panel.setBounds(140, 350, 280, 280);}

        //===== dstport_panel =====
        { dstport_panel.setBackground(new Color(3, 21, 64));
            dstport_panel.setLayout(null);
            TitledBorder tb4=new TitledBorder("Destination Port");
            tb4.setTitleColor(Color.white);
            tb4.setTitleFont(new Font("JetBrains Mono", Font.BOLD, 14));
            dstport_panel.setBorder(new CompoundBorder(
                    tb4,
                    Borders.DLU21));
            dstport_panel.add(sts_lbl4);
            sts_lbl4.setBounds(50, 10, 150, 100);

            dstport_panel.setBounds(530, 350, 280, 280);}

        //===== timestamp_panel =====
        {timestamp_panel.setBackground(new Color(3, 21, 64));
            timestamp_panel.setLayout(null);
            TitledBorder tb5=new TitledBorder("Timestamp");
            tb5.setTitleColor(Color.white);
            tb5.setTitleFont(new Font("JetBrains Mono", Font.BOLD, 14));
            timestamp_panel.setBorder(new CompoundBorder(
                    tb5,
                    Borders.DLU21));
            timestamp_panel.add(sts_lbl5);
            sts_lbl5.setBounds(50, 10, 150, 100);

            timestamp_panel.setBounds(920, 350, 280, 280);}
    }
    private void createActivityCharts()
    {
        String[] srcip_id=new String[srcip_list.stream().distinct().collect(Collectors.toList()).size()];
        String[] srcport_id=new String[srcport_list.stream().distinct().collect(Collectors.toList()).size()];
        String[] dstip_id=new String[dstip_list.stream().distinct().collect(Collectors.toList()).size()];
        String[] dstport_id=new String[dstport_list.stream().distinct().collect(Collectors.toList()).size()];
        String[] protocol_id=new String[protocol_list.stream().distinct().collect(Collectors.toList()).size()];
        String[] timestamp_id=new String[timestamp_list.stream().distinct().collect(Collectors.toList()).size()];


        double[] srcip_cnt=new double[srcip_list.stream().distinct().collect(Collectors.toList()).size()];
        double[] srcport_cnt=new double[srcport_list.stream().distinct().collect(Collectors.toList()).size()];
        double[] dstip_cnt=new double[dstip_list.stream().distinct().collect(Collectors.toList()).size()];
        double[] dstport_cnt=new double[dstport_list.stream().distinct().collect(Collectors.toList()).size()];
        double[] protocol_cnt=new double[protocol_list.stream().distinct().collect(Collectors.toList()).size()];
        double[] timestamp_cnt=new double[timestamp_list.stream().distinct().collect(Collectors.toList()).size()];


        String[] srcip_legend=new String[srcip_list.stream().distinct().collect(Collectors.toList()).size()];
        String[] srcport_legend=new String[srcport_list.stream().distinct().collect(Collectors.toList()).size()];
        String[] dstip_legend=new String[dstip_list.stream().distinct().collect(Collectors.toList()).size()];
        String[] dstport_legend=new String[dstport_list.stream().distinct().collect(Collectors.toList()).size()];
        String[] protocol_legend=new String[protocol_list.stream().distinct().collect(Collectors.toList()).size()];
        String[] timestamp_legend=new String[timestamp_list.stream().distinct().collect(Collectors.toList()).size()];



        Map<String, Long> frequencyMap =srcip_list.stream().collect(Collectors.groupingBy(Function.identity(),Collectors.counting()));
        int cnt=0;
        for (Map.Entry<String, Long> entry : frequencyMap.entrySet()) {
            srcip_id[cnt]=Integer.toString(cnt+1);
            srcip_cnt[cnt]=entry.getValue();
            srcip_legend[cnt]=entry.getKey();
            cnt++;
        }
        frequencyMap =srcport_list.stream().collect(Collectors.groupingBy(Function.identity(),Collectors.counting()));
        cnt=0;
        for (Map.Entry<String, Long> entry : frequencyMap.entrySet()) {
            srcport_id[cnt]=Integer.toString(cnt+1);
            srcport_cnt[cnt]=entry.getValue();
            srcport_legend[cnt]=entry.getKey();
            cnt++;
        }
        frequencyMap =dstip_list.stream().collect(Collectors.groupingBy(Function.identity(),Collectors.counting()));
        cnt=0;
        for (Map.Entry<String, Long> entry : frequencyMap.entrySet()) {
            dstip_id[cnt]=Integer.toString(cnt+1);
            dstip_cnt[cnt]=entry.getValue();
            dstip_legend[cnt]=entry.getKey();
            cnt++;
        }
        frequencyMap =dstport_list.stream().collect(Collectors.groupingBy(Function.identity(),Collectors.counting()));
        cnt=0;
        for (Map.Entry<String, Long> entry : frequencyMap.entrySet()) {
            dstport_id[cnt]=Integer.toString(cnt+1);
            dstport_cnt[cnt]=entry.getValue();
            dstport_legend[cnt]=entry.getKey();
            cnt++;
        }
        frequencyMap =protocol_list.stream().collect(Collectors.groupingBy(Function.identity(),Collectors.counting()));
        cnt=0;
        for (Map.Entry<String, Long> entry : frequencyMap.entrySet()) {
            protocol_id[cnt]=Integer.toString(cnt+1);
            protocol_cnt[cnt]=entry.getValue();
            protocol_legend[cnt]=entry.getKey();
            cnt++;
        }
        frequencyMap =timestamp_list.stream().collect(Collectors.groupingBy(Function.identity(),Collectors.counting()));
        cnt=0;
        for (Map.Entry<String, Long> entry : frequencyMap.entrySet()) {
            timestamp_id[cnt]=Integer.toString(cnt+1);
            timestamp_cnt[cnt]=entry.getValue();
            timestamp_legend[cnt]=entry.getKey();
            cnt++;
        }

        srcip_chart=new BarChartPanel(srcip_cnt,srcip_id,"Source IP",141, 199, 240);
        srcport_chart=new BarChartPanel(srcport_cnt,srcport_id,"Source Port",141, 199, 240);
        dstip_chart=new BarChartPanel(dstip_cnt,dstip_id,"Destination IP",141, 199, 240);
        dstport_chart=new BarChartPanel(dstport_cnt,dstport_id,"Destination Port",141, 199, 240);
        protocol_chart=new BarChartPanel(protocol_cnt,protocol_id,"Protocol",141, 199, 240);
        timestamp_chart=new BarChartPanel(timestamp_cnt,timestamp_id,"Timestamp IP",141, 199, 240);


        {
            chartsip_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/result64.png")));
            chartsip_lbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                chartsip_lblMouseEntered(e);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                chartsip_lblMouseExited(e);
            }
            });



            chartsp_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/result64.png")));
            chartsp_lbl.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    chartsp_lblMouseEntered(e);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    chartsp_lblMouseExited(e);
                }
            });

            chartdip_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/result64black.png")));
            chartdip_lbl.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    chartdip_lblMouseEntered(e);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    chartdip_lblMouseExited(e);
                }
            });

            chartdp_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/result64.png")));
            chartdp_lbl.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    chartdp_lblMouseEntered(e);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    chartdp_lblMouseExited(e);
                }
            });

            chartprot_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/result64.png")));
            chartprot_lbl.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    chartprot_lblMouseEntered(e);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    chartprot_lblMouseExited(e);
                }
            });

            charttim_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/result64.png")));
            charttim_lbl.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    charttim_lblMouseEntered(e);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    charttim_lblMouseExited(e);
                }
            });
        }

        TitledBorder tb1=new TitledBorder("Legend SOURCE IP");
        tb1.setTitleColor(Color.white);
        /*Create Source IP text*/
        String tmp="<html>";

        for(int i=0;i<srcip_list.stream().distinct().collect(Collectors.toList()).size();i++)
        {
            tmp+=srcip_id[i]+ " " + srcip_legend[i] + "<br/>";
        }
        tmp+="</html>";

        srcip_leglbl.setBackground(new Color(41, 101, 196));
        srcip_leglbl.setOpaque(true);
        srcip_leglbl.setFont(new Font("JetBrains Mono", Font.BOLD, 10));
        srcip_leglbl.setForeground(Color.white);
        srcip_leglbl.setText(tmp);
        srcip_leglbl.setBorder(new CompoundBorder(tb1,Borders.DLU21));
        activity_panel.add(srcip_leglbl);
        srcip_leglbl.setBounds(530, 60, 280, 500);
        srcip_leglbl.setVisible(false);

        TitledBorder tb2=new TitledBorder("Legend SOURCE PORT");
        tb2.setTitleColor(Color.white);
        tmp="<html>";

        for(int i=0;i<srcport_list.stream().distinct().collect(Collectors.toList()).size();i++)
        {
            tmp+=srcport_id[i]+ " " + srcport_legend[i] + "<br/>";
        }
        tmp+="</html>";
        srcport_leglbl.setBackground(new Color(41, 101, 196));
        srcport_leglbl.setOpaque(true);
        srcport_leglbl.setFont(new Font("JetBrains Mono", Font.BOLD, 10));
        srcport_leglbl.setForeground(Color.white);
        srcport_leglbl.setText(tmp);
        srcport_leglbl.setBorder(new CompoundBorder(tb2,Borders.DLU21));
        activity_panel.add(srcport_leglbl);
        srcport_leglbl.setBounds(530, 60, 280, 500);
        srcport_leglbl.setVisible(false);

        TitledBorder tb3=new TitledBorder("Legend DESTINATION IP");
        tb3.setTitleColor(Color.white);
        tmp="<html>";

        for(int i=0;i<dstip_list.stream().distinct().collect(Collectors.toList()).size();i++)
        {
            tmp+=dstip_id[i]+ " " + dstip_legend[i] + "<br/>";
        }
        tmp+="</html>";

        dstip_leglbl.setBackground(new Color(41, 101, 196));
        dstip_leglbl.setOpaque(true);
        dstip_leglbl.setFont(new Font("JetBrains Mono", Font.BOLD, 10));
        dstip_leglbl.setForeground(Color.white);
        dstip_leglbl.setText(tmp);
        dstip_leglbl.setBorder(new CompoundBorder(tb3,Borders.DLU21));
        activity_panel.add(dstip_leglbl);
        dstip_leglbl.setBounds(920, 60, 280, 500);
        dstip_leglbl.setVisible(false);


        TitledBorder tb4=new TitledBorder("Legend DESTINATION PORT");
        tb4.setTitleColor(Color.white);
        tmp="<html>";

        for(int i=0;i<dstport_list.stream().distinct().collect(Collectors.toList()).size();i++)
        {
            tmp+=dstport_id[i]+ " " + dstport_legend[i] + "<br/>";
        }
        tmp+="</html>";
        dstport_leglbl.setBackground(new Color(41, 101, 196));
        dstport_leglbl.setOpaque(true);
        dstport_leglbl.setFont(new Font("JetBrains Mono", Font.BOLD, 10));
        dstport_leglbl.setForeground(Color.white);
        dstport_leglbl.setText(tmp);
        dstport_leglbl.setBorder(new CompoundBorder(tb4,Borders.DLU21));
        activity_panel.add( dstport_leglbl);
        dstport_leglbl.setBounds(920, 60, 280, 500);
        dstport_leglbl.setVisible(false);

        TitledBorder tb5=new TitledBorder("Legend PROTOCOL");
        tb5.setTitleColor(Color.white);
        tmp="<html>";

        for(int i=0;i<protocol_list.stream().distinct().collect(Collectors.toList()).size();i++)
        {
            tmp+=protocol_id[i] + " " + protocol_legend[i] + "<br/>";
        }
        tmp+="</html>";
        protocol_leglbl.setBackground(new Color(41, 101, 196));
        protocol_leglbl.setOpaque(true);
        protocol_leglbl.setFont(new Font("JetBrains Mono", Font.BOLD, 10));
        protocol_leglbl.setForeground(Color.white);
        protocol_leglbl.setText(tmp);
        protocol_leglbl.setBorder(new CompoundBorder(tb5,Borders.DLU21));
        activity_panel.add(protocol_leglbl);
        protocol_leglbl.setBounds(530, 60, 280, 500);
        protocol_leglbl.setVisible(false);

        TitledBorder tb6=new TitledBorder("Legend TIMESTAMP");
        tb6.setTitleColor(Color.white);

        tmp="<html>";

        for(int i=0;i<timestamp_list.stream().distinct().collect(Collectors.toList()).size();i++)
        {
            tmp+=timestamp_id[i]+ " " + timestamp_legend[i] + "<br/>";
        }
        tmp+="</html>";
        timestamp_leglbl.setBackground(new Color(41, 101, 196));
        timestamp_leglbl.setOpaque(true);
        timestamp_leglbl.setFont(new Font("JetBrains Mono", Font.BOLD, 10));
        timestamp_leglbl.setForeground(Color.white);
        timestamp_leglbl.setText(tmp);
        timestamp_leglbl.setBorder(new CompoundBorder(tb6,Borders.DLU21));
        activity_panel.add(timestamp_leglbl);
        timestamp_leglbl.setBounds(530, 60, 280, 500);
        timestamp_leglbl.setVisible(false);




    }

    private void charttim_lblMouseExited(MouseEvent e) {
        timestamp_leglbl.setVisible(false);
    }

    private void charttim_lblMouseEntered(MouseEvent e) {
        timestamp_leglbl.setVisible(true);
    }

    private void chartprot_lblMouseExited(MouseEvent e) {
        protocol_leglbl.setVisible(false);
    }

    private void chartprot_lblMouseEntered(MouseEvent e) {
        protocol_leglbl.setVisible(true);
    }

    private void chartdp_lblMouseExited(MouseEvent e) {
        dstport_leglbl.setVisible(false);
    }

    private void chartdp_lblMouseEntered(MouseEvent e) {
        dstport_leglbl.setVisible(true);
    }

    private void chartdip_lblMouseExited(MouseEvent e) {
        dstip_leglbl.setVisible(false);
    }

    private void chartdip_lblMouseEntered(MouseEvent e) {
        dstip_leglbl.setVisible(true);
    }

    private void chartsp_lblMouseExited(MouseEvent e) {
        srcport_leglbl.setVisible(false);
    }

    private void chartsp_lblMouseEntered(MouseEvent e) {
        srcport_leglbl.setVisible(true);
    }

    private void chartsip_lblMouseExited(MouseEvent e) {
        srcip_leglbl.setVisible(false);
    }

    private void chartsip_lblMouseEntered(MouseEvent e) {
        srcip_leglbl.setVisible(true);
    }

    private void processActivityCharts(String header,List<String> flowStringList)
    {
        List<String> list = new ArrayList<String>();
        for(int i=0 ; i<flowStringList.size();i++)
          {
            list= Arrays.asList((flowStringList.get(i)).split(","));
            srcip_list.add(list.get(1));
            dstip_list.add(list.get(3));
            srcport_list.add(list.get(2));
            dstport_list.add(list.get(4));
            protocol_list.add(list.get(5));
            timestamp_list.add(list.get(6).substring(0,10));
          }

    }

    private void resetActivityCharts()
    {
        srcip_list.clear();
        dstip_list.clear();
        srcport_list.clear();
        dstport_list.clear();
        protocol_list.clear();
        timestamp_list.clear();
        chartData_ready=0;
    }
    /* init GUI components */
    private void initComponents() throws IOException {
        //=== general ===
        String tableHeader="Flow ID,Src IP,Src Port,Dst IP,Dst Port,Protocol,Timestamp,Flow Duration";
        String[] arrayHeader = StringUtils.split(tableHeader, ",");

        //=== frames ===
        mainFrame = new JFrame();

        //=== panels ===
        menuPanel = new JPanel() { Image menupanelBackground = ImageIO.read(new File("src/resources/bg8menu.jpg"));

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage( menupanelBackground, 0, 0, null);
            }
        };
        mainPanel = new JPanel(){ Image mainpanelBackground = ImageIO.read(new File("src/resources/bg8resized.jpg"));

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(mainpanelBackground, 0, 0, null);
            }
        };
        submenu2 = new JPanel(){ Image submenu2panelBackground = ImageIO.read(new File("src/resources/bg8submenu.jpg"));

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(submenu2panelBackground, 0, 0, null);
            }
        };
        networkFlow_panel = new JPanel();
        table_panel = new JScrollPane();

        notification_panel= new JPanel(){ Image panelBackground = ImageIO.read(new File("src/resources/bg8notif.jpg"));

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(panelBackground, 0, 0, null);
            }
        };
        notification_scrollpanel=new JScrollPane(notification_panel);

        protectionsts_panel=new JPanel();

        activity_panel=new JPanel();
        srcip_panel=new JPanel();
        dstip_panel=new JPanel();
        dstport_panel=new JPanel();
        srcport_panel=new JPanel();
        protocol_panel=new JPanel();
        timestamp_panel=new JPanel();

        //=== labels ===
        logo = new JLabel();
        array2_lbl=new JLabel();
        status_lbl = new JLabel();
        cnt_lbl = new JLabel();
        title = new JLabel();

        bar_lbl=new JLabel();
        lock_lbl=new JLabel();
        circle_lbl=new JLabel();

        sts_lbl=new JLabel();
        sts_lbl1=new JLabel();
        sts_lbl2=new JLabel();
        sts_lbl3=new JLabel();
        sts_lbl4=new JLabel();
        sts_lbl5=new JLabel();

        srcip_leglbl=new JLabel();
        srcport_leglbl=new JLabel();
        dstip_leglbl=new JLabel();
        dstport_leglbl=new JLabel();
        protocol_leglbl=new JLabel();
        timestamp_leglbl=new JLabel();

        chartsip_lbl=new JLabel();
        chartsp_lbl=new JLabel();
        chartdip_lbl=new JLabel();
        chartdp_lbl=new JLabel();
        chartprot_lbl=new JLabel();
        charttim_lbl=new JLabel();

        //=== buttons ====
        menuItem1 = new JButton();
        menuItem2 = new JButton();
        menuItem3 = new JButton();
        menuItem4 = new JButton();
        menuItem5 = new JButton();
        menuItem6 = new JButton();
        submenu2_btn1 = new JButton();

       //=== tables ===
        tableModel = new DefaultTableModel(arrayHeader,0);
        table = new JTable(tableModel);

        //=== charts===


        //======== mainFrame ========
        {
            mainFrame.setMinimumSize(new Dimension(900, 39));
            mainFrame.setVisible(true);
            var mainFrameContentPane = mainFrame.getContentPane();
            mainFrameContentPane.setLayout(null);
            Toolkit tk = Toolkit.getDefaultToolkit();
            int xSize = ((int) tk.getScreenSize().getWidth());
            int ySize = ((int) tk.getScreenSize().getHeight());
            mainFrame.setSize(xSize,ySize);
            mainFrame.setExtendedState(Frame.MAXIMIZED_BOTH);

            //======== menuPanel ========
            {
                menuPanel.setSize(new Dimension(xSize, ySize));
                menuPanel.setForeground(Color.black);
                menuPanel.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
                menuPanel.setLayout(null);

                //---- logo ----
                ImageRotateSmall logo=new ImageRotateSmall();
                logo.setBounds(30, -80, 250, 250);
                menuPanel.add(logo);

                //---- title ----
                title.setIcon(new ImageIcon("src/resources/title_trans_small2.png"));
                title.setBounds(50, 150, 400, 60);
                menuPanel.add(title);

                //---- menuItem1 ----
               { menuItem1.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                menuItem1.setForeground(Color.white);
                menuItem1.setBackground(new Color(0, 20, 48));
                menuItem1.setIcon(new ImageIcon(getClass().getResource("/resources/protection_icon.png")));
                menuItem1.setSelectedIcon(null);
                menuItem1.setText("Protection");
                menuItem1.setHorizontalAlignment(SwingConstants.LEFT);
                menuItem1.setIconTextGap(30);
                menuItem1.setPressedIcon(null);
                menuItem1.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
                menuItem1.setMargin(new Insets(0, 15, 0, 0));
                menuPanel.add(menuItem1);
                menuItem1.setBounds(0, 215, 400, 40);}

                //---- menuItem2 ----
               { menuItem2.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                menuItem2.setForeground(Color.white);
                menuItem2.setBackground(new Color(0, 20, 48));
                menuItem2.setIcon(new ImageIcon(getClass().getResource("/resources/tools_icon.png")));
                menuItem2.setHorizontalAlignment(SwingConstants.LEFT);
                menuItem2.setText("Tools");
                menuItem2.setIconTextGap(30);
                menuItem2.setMargin(new Insets(0, 15, 0, 0));
                menuItem2.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        menuItem2MouseClicked(e);
                    }

                });
                menuPanel.add(menuItem2);
                menuItem2.setBounds(0, 265, 363, 40);
                array2_lbl.setIcon(new ImageIcon("src/resources/downarrow32.png"));
                menuPanel.add(array2_lbl);
                array2_lbl.setBounds(363, 265, 32, 32);}


                //---- menuItem3 ----
                {menuItem3.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                menuItem3.setForeground(Color.white);
                menuItem3.setBackground(new Color(0, 20, 48));
                menuItem3.setIcon(new ImageIcon(getClass().getResource("/resources/activity_icon.png")));
                menuItem3.setHorizontalAlignment(SwingConstants.LEFT);
                menuItem3.setText("Activity");
                menuItem3.setIconTextGap(30);
                menuItem3.setMargin(new Insets(0, 15, 0, 0));
                menuItem3.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        try {
                            menuItem3MouseClicked(e);
                        } catch (AWTException ex) {
                            ex.printStackTrace();
                        }
                    }

                });
                menuPanel.add(menuItem3);
                menuItem3.setBounds(0, 315, 400, 40);}

                //---- menuItem4 ----
                {menuItem4.setSelectedIcon(null);
                menuItem4.setIcon(new ImageIcon(getClass().getResource("/resources/notifications_icon.png")));
                menuItem4.setHorizontalAlignment(SwingConstants.LEFT);
                menuItem4.setText("Notifications");
                menuItem4.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                menuItem4.setForeground(Color.white);
                menuItem4.setBackground(new Color(0, 20, 48));
                menuItem4.setMargin(new Insets(0, 15, 0, 0));
                menuItem4.setIconTextGap(30);
                menuItem4.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        menuItem4MouseClicked(e);
                    }
                });
                menuPanel.add(menuItem4);
                menuItem4.setBounds(0, 365, 400, 40);}

                //---- menuItem5 ----
                {menuItem5.setIcon(new ImageIcon(getClass().getResource("/resources/account_icon.png")));
                menuItem5.setHorizontalAlignment(SwingConstants.LEFT);
                menuItem5.setText("Account");
                menuItem5.setMargin(new Insets(0, 15, 0, 0));
                menuItem5.setIconTextGap(30);
                menuItem5.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                menuItem5.setForeground(Color.white);
                menuItem5.setBackground(new Color(0, 20, 48));
                menuItem5.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        try {
                            menuItem5MouseClicked(e);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                menuPanel.add(menuItem5);
                menuItem5.setBounds(0, 415, 400, 40);}

                //---- menuItem6 ----
               { menuItem6.setIcon(new ImageIcon(getClass().getResource("/resources/settings_icon.png")));
                menuItem6.setHorizontalAlignment(SwingConstants.LEFT);
                menuItem6.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                menuItem6.setForeground(Color.white);
                menuItem6.setBackground(new Color(0, 20, 48));
                menuItem6.setText("Settings");
                menuItem6.setMargin(new Insets(0, 15, 0, 0));
                menuItem6.setIconTextGap(30);
                menuItem6.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        try {
                            menuItem6MouseClicked(e);
                        } catch (IOException | ClassNotFoundException | UnsupportedLookAndFeelException | InstantiationException | IllegalAccessException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                menuPanel.add(menuItem6);
                menuItem6.setBounds(0, 465, 400, 40);}

                /* menupanel size*/
                {
                    // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for(int i = 0; i < menuPanel.getComponentCount(); i++) {
                        Rectangle bounds = menuPanel.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = menuPanel.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    menuPanel.setMinimumSize(preferredSize);
                    menuPanel.setPreferredSize(preferredSize);
                }
            }
            mainFrameContentPane.add(menuPanel);
            menuPanel.setBounds(0, 0, 400, ySize);

            //======== mainPanel ========
            {
                mainPanel.setLayout(null);
                //======== submenu2 ========
                {

                    submenu2.setVisible(false);
                    submenu2.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseExited(MouseEvent e) {
                            submenu2MouseExited(e);
                        }
                    });
                    submenu2.setLayout(null);

                    //---- submenu2_btn1 ----
                   { submenu2_btn1.setText("Network Flow Viewer");
                    submenu2_btn1.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                    submenu2_btn1.setForeground(Color.white);
                    submenu2_btn1.setBackground(new Color(0, 20, 48));
                    submenu2_btn1.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            submenu2_btn1MouseClicked(e);
                        }
                        @Override
                        public void mouseEntered(MouseEvent e) {
                            submenu2_btn1MouseEntered(e);
                        }
                    });
                    submenu2.add(submenu2_btn1);
                    submenu2_btn1.setBounds(25, 5, 200, 40);}

                    /*submenu2 size*/
                    {
                        // compute preferred size
                        Dimension preferredSize = new Dimension();
                        for(int i = 0; i < submenu2.getComponentCount(); i++) {
                            Rectangle bounds = submenu2.getComponent(i).getBounds();
                            preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                            preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                        }
                        Insets insets = submenu2.getInsets();
                        preferredSize.width += insets.right;
                        preferredSize.height += insets.bottom;
                        submenu2.setMinimumSize(preferredSize);
                        submenu2.setPreferredSize(preferredSize);
                    }
                }
                mainPanel.add(submenu2);
                submenu2.setBounds(-10, 265, 260, 235);

                //======== activity_panel ======
                {

                activity_panel.setOpaque(false);
                activity_panel.setLayout(null);

                    /*activity panel size*/
                    {
                        // compute preferred size
                        Dimension preferredSize = new Dimension();
                        for(int i = 0; i < activity_panel.getComponentCount(); i++) {
                            Rectangle bounds = activity_panel.getComponent(i).getBounds();
                            preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                            preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                        }
                        Insets insets = activity_panel.getInsets();
                        preferredSize.width += insets.right;
                        preferredSize.height += insets.bottom;
                        activity_panel.setMinimumSize(preferredSize);
                        activity_panel.setPreferredSize(preferredSize);
                    }



                    mainPanel.add(activity_panel);
                    activity_panel.setBounds(50, 220, 1400, 700);
                    activity_panel.setVisible(false);
                }

                //======== networkFlow_panel ========
                {
                    networkFlow_panel.setOpaque(false);
                    networkFlow_panel.setVisible(false);
                    networkFlow_panel.setLayout(null);

                    //======== table_panel ========
                   { {
                        //---- table ----
                        table.getTableHeader().setBackground(new Color(0, 0, 51));
                        table.getTableHeader().setForeground(Color.white);
                        table.getTableHeader().setOpaque(true);
                        table.getTableHeader().setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                        table.setFont(new Font("JetBrains Mono", Font.PLAIN, 14));
                        table.setGridColor(new Color(255,255,255));
                        table_panel.setViewportView(table);
                    }
                    networkFlow_panel.add(table_panel);
                    table_panel.setBounds(50, 50, 1300, 600);

                    //---- status_lbl ----
                   { status_lbl.setForeground(Color.white);
                    networkFlow_panel.add(status_lbl);
                    status_lbl.setBounds(30, 670, 755, 25);}

                    //---- cnt_lbl ----
                    {cnt_lbl.setText("0");
                    cnt_lbl.setForeground(Color.white);
                    networkFlow_panel.add(cnt_lbl);
                    cnt_lbl.setBounds(1150, 670, 55, cnt_lbl.getPreferredSize().height);}

                    /*networkflow panel size*/
                    {
                        // compute preferred size
                        Dimension preferredSize = new Dimension();
                        for(int i = 0; i < networkFlow_panel.getComponentCount(); i++) {
                            Rectangle bounds = networkFlow_panel.getComponent(i).getBounds();
                            preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                            preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                        }
                        Insets insets = networkFlow_panel.getInsets();
                        preferredSize.width += insets.right;
                        preferredSize.height += insets.bottom;
                        networkFlow_panel.setMinimumSize(preferredSize);
                        networkFlow_panel.setPreferredSize(preferredSize);
                    }
                   }
                }
                mainPanel.add(networkFlow_panel);
                networkFlow_panel.setBounds(50, 220, 1400, 700);

                //======== notification_panel ========
                {
                    notification_panel.setBorder(null);
                    notification_panel.setLayout(null);

                    //notification_scrollpanel.setBackground(new Color(3, 211, 252));

                    notification_scrollpanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    notification_scrollpanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                    notification_scrollpanel.setViewportView(notification_panel);
                    notification_scrollpanel.setBorder(null);
                    mainPanel.add(notification_scrollpanel);
                    notification_scrollpanel.setBounds(50, 220, 1400, 700);


                    notification_panel.add(add_notification("Notification demo",0));
                    notification_panel.add(add_notification("Notification demo",1));
                    notification_panel.add(add_notification("Notification demo",2));
                    notification_panel.add(add_notification("Notification demo",3));
                    notification_panel.add(add_notification("Notification demo",4));


                    /*notification panel size */
                    {
                        // compute preferred size
                        Dimension preferredSize = new Dimension();
                        for(int i = 0; i < notification_panel.getComponentCount(); i++) {
                            Rectangle bounds = notification_panel.getComponent(i).getBounds();
                            preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                            preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                        }
                        Insets insets = notification_panel.getInsets();
                        preferredSize.width += insets.right;
                        preferredSize.height += insets.bottom;
                        notification_panel.setMinimumSize(preferredSize);
                        notification_panel.setPreferredSize(preferredSize);
                    }
                    notification_scrollpanel.setVisible(false);
                }

                //======== protectionsts_panel =======

                {protectionsts_panel.setLayout(null);
                protectionsts_panel.setOpaque(false);
                lock_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/lock_green124x200.png")));
                lock_lbl.setBounds(120, 10, 124, 200);
                protectionsts_panel.add(lock_lbl);

                circle_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/circle_green200x200.png")));
                circle_lbl.setBounds(1100, 10, 400, 200);
                protectionsts_panel.add(circle_lbl);

                bar_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/bar_green1000x300.png")));
                bar_lbl.setBounds(180, -134, 1000, 500);
                protectionsts_panel.add(bar_lbl);}

                mainPanel.add(protectionsts_panel);
                protectionsts_panel.setBounds(50, 10, 1400, 200);

                /* mainpanel size*/
                {
                    // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for(int i = 0; i < mainPanel.getComponentCount(); i++) {
                        Rectangle bounds = mainPanel.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = mainPanel.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    mainPanel.setMinimumSize(preferredSize);
                    mainPanel.setPreferredSize(preferredSize);
                }
            }
            mainFrameContentPane.add(mainPanel);
            mainPanel.setBounds(400, 0, xSize-400, ySize);



                /*mainFrame size*/
                {
                // compute preferred size
                Dimension preferredSize = new Dimension();
                for(int i = 0; i < mainFrameContentPane.getComponentCount(); i++) {
                    Rectangle bounds = mainFrameContentPane.getComponent(i).getBounds();
                    preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                    preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                }
                Insets insets = mainFrameContentPane.getInsets();
                preferredSize.width += insets.right;
                preferredSize.height += insets.bottom;
                mainFrameContentPane.setMinimumSize(preferredSize);
                mainFrameContentPane.setPreferredSize(preferredSize);
            }
            mainFrame.pack();
            mainFrame.setLocationRelativeTo(mainFrame.getOwner());
        }

    }



}
