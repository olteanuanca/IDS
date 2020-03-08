package ui.frames;

import com.jgoodies.forms.factories.Borders;
import guiUtils.ImageRotate;
import guiUtils.ImageRotateSmall;
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
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HomeFrame {

    protected static final Logger logger = LoggerFactory.getLogger(HomeFrame.class);
    private TrafficFlowWorker trafficWorker;
    private ExecutorService csvWriterThread;
    private DefaultTableModel tableModel;
    private String path1 = "C:\\WORK\\Licenta\\Project\\AppData\\appSetup.txt";

    private JFrame mainFrame;
    private JPanel menuPanel;
    private JLabel logo;
    private JButton menuItem1;
    private JButton menuItem2;
    private JButton menuItem3;
    private JButton menuItem4;
    private JButton menuItem5;
    private JButton menuItem6;
    private JPanel mainPanel;
    private JPanel submenu2;
    private JButton submenu2_btn1;
    private JPanel networkFlow_panel;
    private JScrollPane table_panel;
    private JTable table;
    private JLabel status_lbl;
    private JLabel cnt_lbl;
    private JLabel title;
    private JLabel arr_mi2_lbl;

    //----Notification----
    private JScrollPane notification_scrollpanel;
    private JPanel notification_panel;

    //----Protection Status----
    private JPanel protectionsts_panel;
    private JLabel circle_lbl;
    private JLabel bar_lbl;
    private JLabel lock_lbl;

    //-----Activity------

    private JPanel activity_panel;
    private JPanel srcip_panel;
    private JPanel dstip_panel;
    private JPanel dstport_panel;
    private JPanel srcport_panel;
    private JPanel protocol_panel;
    private JPanel timestamp_panel;
    private JLabel sts_lbl;
    private JLabel sts_lbl1;
    private JLabel sts_lbl2;
    private JLabel sts_lbl3;
    private JLabel sts_lbl4;
    private JLabel sts_lbl5;

    public HomeFrame() throws UnsupportedLookAndFeelException, IOException {

        initComponents();
        mainFrame.setVisible(true);

        //TODO: CHECK IF SCANNING STARTS AUTOMATICALLY AND DATA IS SET
        //IF YES:
        init();
        startTrafficScan();
    }

    private void init() {
        csvWriterThread = Executors.newSingleThreadExecutor();
    }

    public void destory() {
        csvWriterThread.shutdown();
    }

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
                insertFlow((BasicFlow) event.getNewValue());
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

    private void insertFlow(BasicFlow flow) {
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

        //insert flows to JTable
        SwingUtilities.invokeLater(new InsertTableRow(tableModel, tableflowDataList, cnt_lbl));
        tableflowDataList=null;
        //  btnSave.setEnabled(true);
    }


    private void menuItem2MouseClicked(MouseEvent e) {
        if(submenu2.isShowing()) {
            arr_mi2_lbl.setIcon(new ImageIcon("src/resources/downarrow32.png"));
            submenu2.setVisible(false);
        }
        else{
            arr_mi2_lbl.setIcon(new ImageIcon("src/resources/leftarrow32.png"));
            submenu2.setVisible(true);

        }
    }
    private void setSrcipchart()
    {
        srcip_panel.removeAll();        // clear panel before add new chart
        srcip_panel.setLayout(new java.awt.CardLayout());
        srcip_panel.add(loadSrcipchart());
        srcip_panel.validate();
    }
    private ChartPanel loadSrcipchart()
    {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("TV",20);
        dataset.setValue("DVD",20);
        dataset.setValue("Mobile phone",40);
        dataset.setValue("Accessories",10);

        JFreeChart chart =ChartFactory.createPieChart3D("", dataset,true,false,false);

        final PiePlot3D plot = (PiePlot3D) chart.getPlot();
        plot.setStartAngle(270);
        plot.setForegroundAlpha(0.60f);
        plot.setInteriorGap(0.02);

        ChartPanel chartPanel = new ChartPanel(chart);


        return chartPanel;
    }
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

        notif_lbl.setBounds(100, 100+157*index, 1200, 155);
        notif_lbl.setVisible(true);

        return notif_lbl;
    }
    private void menuItem3MouseClicked(MouseEvent e) {
        if(notification_scrollpanel.isShowing())
            notification_scrollpanel.setVisible(false);
        if(networkFlow_panel.isShowing())
             networkFlow_panel.setVisible(false);
        activity_panel.setVisible(true);

    }


    private void menuItem5MouseClicked(MouseEvent e) throws IOException {
       new UserDataFrame();

    }

    private void menuItem6MouseClicked(MouseEvent e) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException, IOException {
        new AppDataFrame();
    }


    private void submenu2MouseExited(MouseEvent e) {
        submenu2.setVisible(false);
    }

    private void submenu2_btn1MouseEntered(MouseEvent e) {
        submenu2.setVisible(true);
    }

    private void submenu2_btn1MouseClicked(MouseEvent e) {
        if(notification_scrollpanel.isShowing())
            notification_scrollpanel.setVisible(false);
        if(activity_panel.isShowing())
            activity_panel.setVisible(false);
        submenu2.setVisible(false);
        networkFlow_panel.setVisible(true);
        setProtectionsts_yellow();
    }
    private void menuItem4MouseClicked(MouseEvent e) {

        if(networkFlow_panel.isShowing())
            networkFlow_panel.setVisible(false);
        if(activity_panel.isShowing())
            activity_panel.setVisible(false);

        notification_scrollpanel.setVisible(true);
        setProtectionsts_red();
    }

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
    private void initComponents() throws IOException {

        mainFrame = new JFrame();
        menuPanel = new JPanel() { Image panelBackground = ImageIO.read(new File("src/resources/5766.jpg"));

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(panelBackground, 0, 0, null);
            }
        };

        logo = new JLabel();
        menuItem1 = new JButton();
        menuItem2 = new JButton();
        arr_mi2_lbl=new JLabel();
        menuItem3 = new JButton();
        menuItem4 = new JButton();
        menuItem5 = new JButton();
        menuItem6 = new JButton();

        mainPanel = new JPanel(){ Image panelBackground = ImageIO.read(new File("src/resources/5766.jpg"));

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(panelBackground, 0, 0, null);
            }
        };
        submenu2 = new JPanel();
        submenu2_btn1 = new JButton();
        networkFlow_panel = new JPanel();
        table_panel = new JScrollPane();
        String tableHeader="Flow ID,Src IP,Src Port,Dst IP,Dst Port,Protocol,Timestamp,Flow Duration";
        String[] arrayHeader = StringUtils.split(tableHeader, ",");
        //String[] arrayHeader = StringUtils.split(FlowFeature.getHeader(), ",");
        tableModel = new DefaultTableModel(arrayHeader,0);
        table = new JTable(tableModel);
        status_lbl = new JLabel();
        cnt_lbl = new JLabel();
        title = new JLabel();

        //-----Notification panel-----
        notification_panel= new JPanel();
        notification_scrollpanel=new JScrollPane(notification_panel);

        //-----Protection Status panel----
        protectionsts_panel=new JPanel();
        bar_lbl=new JLabel();
        lock_lbl=new JLabel();
        circle_lbl=new JLabel();

        //-----Activity panel-----
        activity_panel=new JPanel();
        srcip_panel=new JPanel();
        dstip_panel=new JPanel();
        dstport_panel=new JPanel();
        srcport_panel=new JPanel();
        protocol_panel=new JPanel();
        timestamp_panel=new JPanel();
        sts_lbl=new JLabel();
        sts_lbl1=new JLabel();
        sts_lbl2=new JLabel();
        sts_lbl3=new JLabel();
        sts_lbl4=new JLabel();
        sts_lbl5=new JLabel();



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

                //---- title ----
                title.setIcon(new ImageIcon("src/resources/title_trans_small2.png"));
                menuPanel.add(title);
                title.setBounds(50, 135, 400, 60);

                //---- logo ----
                ImageRotateSmall logo=new ImageRotateSmall();
                logo.setBounds(30, -80, 250, 250);
                menuPanel.add(logo);

                //---- menuItem1 ----
                menuItem1.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                menuItem1.setForeground(Color.white);
                menuItem1.setBackground(new Color(3, 211, 252, 35));
                menuItem1.setIcon(new ImageIcon(getClass().getResource("/resources/protection_icon.png")));
                menuItem1.setSelectedIcon(null);
                menuItem1.setText("Protection");
                menuItem1.setHorizontalAlignment(SwingConstants.LEFT);
                menuItem1.setIconTextGap(30);
                menuItem1.setPressedIcon(null);
                menuItem1.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
                menuItem1.setMargin(new Insets(0, 15, 0, 0));
                menuItem1.setBorderPainted(false);
                menuItem1.setBorder(null);
                menuPanel.add(menuItem1);
                menuItem1.setBounds(0, 215, 400, 40);

                //---- menuItem2 ----
                menuItem2.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                menuItem2.setForeground(Color.white);
                menuItem2.setBackground(new Color(3, 211, 252, 35));
                menuItem2.setIcon(new ImageIcon(getClass().getResource("/resources/tools_icon.png")));
                menuItem2.setHorizontalAlignment(SwingConstants.LEFT);
                menuItem2.setText("Tools");
                menuItem2.setIconTextGap(30);
                menuItem2.setMargin(new Insets(0, 15, 0, 0));
                menuItem2.setBorderPainted(false);
                menuItem2.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        menuItem2MouseClicked(e);
                    }

                });
                menuPanel.add(menuItem2);
                menuItem2.setBounds(0, 265, 363, 40);
                arr_mi2_lbl.setIcon(new ImageIcon("src/resources/leftarrow32.png"));
                arr_mi2_lbl.setBounds(363, 265, 32, 32);
                menuPanel.add(arr_mi2_lbl);


                //---- menuItem3 ----
                menuItem3.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                menuItem3.setForeground(Color.white);
                menuItem3.setBackground(new Color(3, 211, 252, 35));
                menuItem3.setIcon(new ImageIcon(getClass().getResource("/resources/activity_icon.png")));
                menuItem3.setHorizontalAlignment(SwingConstants.LEFT);
                menuItem3.setText("Activity");
                menuItem3.setIconTextGap(30);
                menuItem3.setMargin(new Insets(0, 15, 0, 0));
                menuItem3.setBorderPainted(false);
                menuItem3.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        menuItem3MouseClicked(e);
                    }

                });
                menuPanel.add(menuItem3);
                menuItem3.setBounds(0, 315, 400, 40);

                //---- 4 ----
                menuItem4.setSelectedIcon(null);
                menuItem4.setIcon(new ImageIcon(getClass().getResource("/resources/notifications_icon.png")));
                menuItem4.setHorizontalAlignment(SwingConstants.LEFT);
                menuItem4.setText("Notifications");
                menuItem4.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                menuItem4.setForeground(Color.white);
                menuItem4.setBackground(new Color(3, 211, 252, 35));
                menuItem4.setMargin(new Insets(0, 15, 0, 0));
                menuItem4.setIconTextGap(30);
                menuItem4.setBorderPainted(false);
                menuItem4.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        menuItem4MouseClicked(e);
                    }
                });
                menuPanel.add(menuItem4);
                menuItem4.setBounds(0, 365, 400, 40);

                //---- menuItem5 ----
                menuItem5.setIcon(new ImageIcon(getClass().getResource("/resources/account_icon.png")));
                menuItem5.setHorizontalAlignment(SwingConstants.LEFT);
                menuItem5.setText("Account");
                menuItem5.setMargin(new Insets(0, 15, 0, 0));
                menuItem5.setIconTextGap(30);
                menuItem5.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                menuItem5.setForeground(Color.white);
                menuItem5.setBackground(new Color(3, 211, 252, 35));
                menuItem5.setBorderPainted(false);
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
                menuItem5.setBounds(0, 415, 400, 40);
                //---- menuItem6 ----
                menuItem6.setIcon(new ImageIcon(getClass().getResource("/resources/settings_icon.png")));
                menuItem6.setHorizontalAlignment(SwingConstants.LEFT);
                menuItem6.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                menuItem6.setForeground(Color.white);
                menuItem6.setBackground(new Color(3, 211, 252, 35));
                menuItem6.setText("Settings");
                menuItem6.setMargin(new Insets(0, 15, 0, 0));
                menuItem6.setIconTextGap(30);
                menuItem6.setBorderPainted(false);
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
                menuItem6.setBounds(0, 465, 400, 40);

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
                mainPanel.setBackground(new Color(51, 51, 51));
                mainPanel.setLayout(null);

                //======== submenu2 ========
                {
                    submenu2.setBackground(new Color(3, 211, 252, 35));
                    submenu2.setVisible(false);
                    submenu2.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseExited(MouseEvent e) {
                            submenu2MouseExited(e);
                        }
                    });
                    submenu2.setLayout(null);

                    //---- submenu2_btn1 ----
                    submenu2_btn1.setText("Network Flow Viewer");
                    submenu2_btn1.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                    submenu2_btn1.setForeground(Color.white);
                    submenu2_btn1.setBackground(new Color(3, 211, 252, 95));
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
                    submenu2_btn1.setBounds(25, 5, 200, 40);

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
                activity_panel.setBackground(new Color(3, 211, 252,72));
                activity_panel.setLayout(null);

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

                    sts_lbl.setText("Loading ...");
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
                    sts_lbl5.setForeground(Color.white);

                        //===== srcip_panel =====
                    srcip_panel.setBackground(new Color(3, 211, 252,72));
                    srcip_panel.setLayout(null);
                    TitledBorder tb=new TitledBorder("Source IP");
                    tb.setTitleColor(Color.white);
                    tb.setTitleFont(new Font("JetBrains Mono", Font.BOLD, 14));
                    srcip_panel.setBorder(new CompoundBorder(
                            tb,
                            Borders.DLU21));
                    srcip_panel.add(sts_lbl);
                    sts_lbl.setBounds(50, 10, 150, 100);
                    setSrcipchart();
                    activity_panel.add(srcip_panel);
                    srcip_panel.setBounds(140, 50, 280, 280);

                        //===== dstip_panel =====
                    dstip_panel.setBackground(new Color(3, 211, 252,72));
                    dstip_panel.setLayout(null);
                    TitledBorder tb1=new TitledBorder("Destination IP");
                    tb1.setTitleColor(Color.white);
                    tb1.setTitleFont(new Font("JetBrains Mono", Font.BOLD, 14));
                    dstip_panel.setBorder(new CompoundBorder(
                            tb1,
                            Borders.DLU21));
                    dstip_panel.add(sts_lbl1);
                    sts_lbl1.setBounds(50, 10, 150, 100);
                    activity_panel.add(dstip_panel);
                    dstip_panel.setBounds(530, 50, 280, 280);

                        //===== protocol_panel =====
                    protocol_panel.setBackground(new Color(3, 211, 252,72));
                    protocol_panel.setLayout(null);
                    TitledBorder tb2=new TitledBorder("Protocol");
                    tb2.setTitleColor(Color.white);
                    tb2.setTitleFont(new Font("JetBrains Mono", Font.BOLD, 14));
                    protocol_panel.setBorder(new CompoundBorder(
                            tb2,
                            Borders.DLU21));
                    protocol_panel.add(sts_lbl2);
                    sts_lbl2.setBounds(50, 10, 150, 100);
                    activity_panel.add(protocol_panel);
                    protocol_panel.setBounds(920, 50, 280, 280);

                    //===== srcport_panel =====
                    srcport_panel.setBackground(new Color(3, 211, 252,72));
                    srcport_panel.setLayout(null);
                    TitledBorder tb3=new TitledBorder("Source Port");
                    tb3.setTitleColor(Color.white);
                    tb3.setTitleFont(new Font("JetBrains Mono", Font.BOLD, 14));
                    srcport_panel.setBorder(new CompoundBorder(
                            tb3,
                            Borders.DLU21));
                    srcport_panel.add(sts_lbl3);
                    sts_lbl3.setBounds(50, 10, 150, 100);
                    activity_panel.add(srcport_panel);
                    srcport_panel.setBounds(140, 350, 280, 280);

                    //===== dstport_panel =====
                    dstport_panel.setBackground(new Color(3, 211, 252,72));
                    dstport_panel.setLayout(null);
                    TitledBorder tb4=new TitledBorder("Destination Port");
                    tb4.setTitleColor(Color.white);
                    tb4.setTitleFont(new Font("JetBrains Mono", Font.BOLD, 14));
                    dstport_panel.setBorder(new CompoundBorder(
                            tb4,
                            Borders.DLU21));
                    dstport_panel.add(sts_lbl4);
                    sts_lbl4.setBounds(50, 10, 150, 100);
                    activity_panel.add(dstport_panel);
                    dstport_panel.setBounds(530, 350, 280, 280);

                    //===== timestamp_panel =====
                    timestamp_panel.setBackground(new Color(3, 211, 252,72));
                    timestamp_panel.setLayout(null);
                    TitledBorder tb5=new TitledBorder("Timestamp");
                    tb5.setTitleColor(Color.white);
                    tb5.setTitleFont(new Font("JetBrains Mono", Font.BOLD, 14));
                    timestamp_panel.setBorder(new CompoundBorder(
                            tb5,
                            Borders.DLU21));
                    timestamp_panel.add(sts_lbl5);
                    sts_lbl5.setBounds(50, 10, 150, 100);
                    activity_panel.add(timestamp_panel);
                    timestamp_panel.setBounds(920, 350, 280, 280);

                    mainPanel.add(activity_panel);
                    activity_panel.setBounds(50, 220, 1400, 700);
                    activity_panel.setVisible(false);
                }

                //======== networkFlow_panel ========
                {
                    networkFlow_panel.setBackground(new Color(3, 211, 252,72));
                    networkFlow_panel.setVisible(false);
                    networkFlow_panel.setLayout(null);

                    //======== table_panel ========
                    {
                        //---- table ----
                        table.getTableHeader().setBackground(new Color(0, 0, 51,150));
                        table.getTableHeader().setForeground(Color.white);
                        table.getTableHeader().setOpaque(true);
                        table.getTableHeader().setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                        table.setFont(new Font("JetBrains Mono", Font.PLAIN, 14));
                        table.setGridColor(new Color(0, 0, 51));
                        table.setBackground(new Color(3, 211, 252,72));
                        table_panel.setBackground(new Color(3, 211, 252,72));
                        table_panel.setViewportView(table);
                    }
                    networkFlow_panel.add(table_panel);
                    table_panel.setBounds(50, 70, 1300, 600);

                    //---- status_lbl ----
                    status_lbl.setForeground(Color.white);
                    networkFlow_panel.add(status_lbl);
                    status_lbl.setBounds(20, 670, 755, 25);

                    //---- cnt_lbl ----
                    cnt_lbl.setText("0");
                    cnt_lbl.setForeground(Color.white);
                    networkFlow_panel.add(cnt_lbl);
                    cnt_lbl.setBounds(1150, 670, 55, cnt_lbl.getPreferredSize().height);

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
                mainPanel.add(networkFlow_panel);
                networkFlow_panel.setBounds(50, 220, 1400, 700);

                //======== protectionsts_panel =======

                protectionsts_panel.setLayout(null);
                protectionsts_panel.setOpaque(false);
                lock_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/lock_green124x200.png")));
                lock_lbl.setBounds(120, 10, 124, 200);
                protectionsts_panel.add(lock_lbl);

                circle_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/circle_green200x200.png")));
                circle_lbl.setBounds(1100, 10, 400, 200);
                protectionsts_panel.add(circle_lbl);

                bar_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/bar_green1000x300.png")));
                bar_lbl.setBounds(180, -134, 1000, 500);
                protectionsts_panel.add(bar_lbl);





                mainPanel.add(protectionsts_panel);
                protectionsts_panel.setBounds(50, 10, 1400, 200);
                //======== notification_panel ========
                {
                    notification_panel.setBackground(new Color(3, 211, 252, 72));
                    //notification_panel.setVisible(true);
                    notification_panel.setLayout(null);


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

                    notification_scrollpanel.setBackground(new Color(3, 211, 252, 72));
                    notification_scrollpanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    notification_scrollpanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                    notification_scrollpanel.setViewportView(notification_panel);
                    mainPanel.add(notification_scrollpanel);
                    notification_scrollpanel.setBounds(50, 220, 1400, 700);


                    notification_scrollpanel.add(add_notification("Notification demo",0));
                    notification_scrollpanel.add(add_notification("Notification demo",1));
//                    notification_scrollpanel.add(add_notification("Notification demo",2));
//                    notification_scrollpanel.add(add_notification("Notification demo",3));
//                    notification_scrollpanel.add(add_notification("Notification demo",4));
//                    notification_scrollpanel.add(add_notification("Notification demo",5));
//                    notification_scrollpanel.add(add_notification("Notification demo",6));

                    notification_scrollpanel.setVisible(false);
                }
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
