package ui.frames;

import com.jgoodies.forms.factories.Borders;
import connection.Client;
import connection.SendToServer;
import database.DB;
import database.IDB;
import guiUtils.CustomPanel;
import guiUtils.ImageRotate;
import guiUtils.ImageRotateSmall;
import guiUtils.charts.BarChartPanel;
import guiUtils.notifications.WinNotifications;
import jnetpcap.BasicFlow;
import jnetpcap.FlowFeature;
import jnetpcap.worker.TrafficFlowWorker;
import jnetpcap.manager.FlowMgr;

import net.proteanit.sql.DbUtils;
import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Attack;
import utils.InsertCsvRow;
import utils.InsertTableRow;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    private static int notification_index=0;
    private static float protectionStatus=100;
    IDB db = new DB();
    protected static final Logger logger = LoggerFactory.getLogger(HomeFrame.class);
    private TrafficFlowWorker trafficWorker;
    private ExecutorService csvWriterThread;
    private DefaultTableModel tableModel;
    private DefaultTableModel iptableModel;
    private String path1 =  FlowMgr.getInstance().getSavePath() + "appSetup.txt";
    private int activityNext=0;
    // === frames ===
    private JFrame mainFrame;

    // === panels ===
    private JPanel homePanel;

    private JPanel menuPanel;
    private JPanel networkFlow_panel;
    private JPanel mainPanel;

    private JPanel submenu2;

    private JScrollPane notification_scrollpanel;
    private JPanel notification_panel;

    private JPanel protectionsts_panel;

    private JPanel activity_panel;



    //=== tables ===
    private JTable table;
    private JScrollPane table_panel;
    //---activity
    private JScrollPane ipscrollPane;
    private JTable ipTable;


    //=== labels ===
    private JLabel logo;
    private JLabel protection_lbl;
    private JLabel protectiontxt_lbl;
    private JLabel status_lbl;
    private JLabel cnt_lbl;
    private JLabel title;
    private JLabel array2_lbl;


    private JLabel bar_lbl;
    private JLabel lock_lbl;

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
                } catch (IOException | AWTException e) {
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
    }

    /* Insert to table and csv captured traffic*/
    private void insertFlow(BasicFlow flow) throws IOException, AWTException {
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

        Client c = new Client(flowStringList);
        new Thread(c).start();

        float oldProtectionSts = protectionStatus;

        protectionStatus= ((Client) c).getProtectionStatus();

        if(oldProtectionSts>protectionStatus)
        {
            protection_lbl.setText(Float.toString(protectionStatus));
            notification_panel.add(add_notification("We discovered some unusual behaviour. Please check yout network traffic!",notification_index));
            notification_index ++;
            if (SystemTray.isSupported()) {
                WinNotifications td = new WinNotifications();
                td.displayNotif("Alert!!!","We discovered some unusual behaviour. Please check yout network traffic!");
            }
        }

        if(protectionStatus<25)
            setProtectionsts_red();
        else if(protectionStatus<50)
            setProtectionsts_pink();
        else if(protectionStatus<75)
            setProtectionsts_yellow();
        else
            setProtectionsts_green();

        //insert flows to JTable
        SwingUtilities.invokeLater(new InsertTableRow(tableModel, tableflowDataList, cnt_lbl));

        tableflowDataList=null;
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
        { notification_scrollpanel.setVisible(false);
            menuItem4.setForeground(Color.white);
            menuItem4.setBackground(new Color(0, 20, 48));
        }
        if(activity_panel.isShowing())

{               activity_panel.setVisible(false);
                menuItem3.setForeground(Color.white);
                menuItem3.setBackground(new Color(0, 20, 48));
}
        submenu2.setVisible(false);
        networkFlow_panel.setVisible(true);
        menuItem2.setForeground(new Color(0, 20, 48));
        menuItem2.setBackground(new Color(163, 194, 204));

    }

    /* Show item 3 (ACTIVITY)*/
    private void menuItem3MouseClicked(MouseEvent e) throws AWTException, SQLException {

        if(notification_scrollpanel.isShowing())
           { notification_scrollpanel.setVisible(false);
               menuItem4.setForeground(Color.white);
               menuItem4.setBackground(new Color(0, 20, 48));
           }
        if(networkFlow_panel.isShowing())
        { networkFlow_panel.setVisible(false);
            menuItem2.setForeground(Color.white);
            menuItem2.setBackground(new Color(0, 20, 48));}

        menuItem3.setForeground(new Color(0, 20, 48));
        menuItem3.setBackground(new Color(163, 194, 204));
         activity_panel.setVisible(true);

        db.startDBConn();
        ArrayList<Attack> attack_list = db.retrieveFromAttacks(db.getUserID());
        DefaultTableModel model=(DefaultTableModel)ipTable.getModel();
        Object[] row = new Object[8];
        for(int i=0;i<attack_list.size();i++)
        {
            row[0]=attack_list.get(i).getSrcIP();
            row[1]=attack_list.get(i).getDstIP();
            row[2]=attack_list.get(i).getSrcPort();
            row[3]=attack_list.get(i).getDstPort();
            row[4]=attack_list.get(i).getProtocol();
            row[5]=attack_list.get(i).getFlowDuration();
            row[6]=attack_list.get(i).getTmstamp();
            row[7]=attack_list.get(i).getResult();
            model.addRow(row);
        }

    }

    /* Show item 4 (NOTIFICATIONS)*/
    private void menuItem4MouseClicked(MouseEvent e) throws AWTException {

        if(networkFlow_panel.isShowing())
           { networkFlow_panel.setVisible(false);
               menuItem2.setForeground(Color.white);
               menuItem2.setBackground(new Color(0, 20, 48));}
        if(activity_panel.isShowing())
            {activity_panel.setVisible(false);
        menuItem3.setForeground(Color.white);
        menuItem3.setBackground(new Color(0, 20, 48));}

        menuItem4.setForeground(new Color(0, 20, 48));
        menuItem4.setBackground(new Color(163, 194, 204));
        notification_scrollpanel.setVisible(true);
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
        bar_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/bar_green1000x300txt.png")));
    }
    private void setProtectionsts_yellow()
    {
        lock_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/lock_yellow124x200.png")));
        bar_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/bar_yellow1000x300txt.png")));
    }
    private void setProtectionsts_red()
    {
        lock_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/lock_red124x200.png")));
        bar_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/bar_red1000x300txt.png")));
    }
    private void setProtectionsts_pink()
    {
        lock_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/lock_pink124x200.png")));
        bar_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/bar_pink1000x300.png")));
    }


    /* init GUI components */
    private void initComponents() throws IOException {
        //=== general ===

        String tableHeader="Flow ID,Src IP,Src Port,Dst IP,Dst Port,Protocol,Timestamp,Flow Duration";
        String[] arrayHeader = StringUtils.split(tableHeader, ",");

        ImageIcon img = new ImageIcon("src/resources/logo_border_small.jpg");

        //=== frames ===
        mainFrame = new JFrame();
        mainFrame.setIconImage(img.getImage());
        mainFrame.setTitle("SecIT Solutions");
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

        //=== labels ===
        logo = new JLabel();
        array2_lbl=new JLabel();
        status_lbl = new JLabel();
        cnt_lbl = new JLabel();
        title = new JLabel();

        bar_lbl=new JLabel();
        lock_lbl=new JLabel();
        protection_lbl=new JLabel();
        protectiontxt_lbl=new JLabel();


        //=== buttons ====
        menuItem2 = new JButton();
        menuItem3 = new JButton();
        menuItem4 = new JButton();
        menuItem5 = new JButton();
        menuItem6 = new JButton();
        submenu2_btn1 = new JButton();

       //=== tables ===
        tableModel = new DefaultTableModel(arrayHeader,0){
            @Override
            public boolean isCellEditable(int row, int column) {
                //all cells false
                return false;
            }
        };
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
                array2_lbl.setBounds(367, 265, 32, 32);}


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
                       } catch (AWTException | SQLException ex) {
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
                        try {
                            menuItem4MouseClicked(e);
                        } catch (AWTException ex) {
                            ex.printStackTrace();
                        }
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

                    String iptableHeader="Src IP,Dst IP,Src Port,Dst Port,Protocol,Flow Duration,Timestamp,Result";
                    String[] iparrayHeader = StringUtils.split(iptableHeader, ",");
                    ipscrollPane = new JScrollPane();
                    iptableModel = new DefaultTableModel(iparrayHeader,0){
                        @Override
                        public boolean isCellEditable(int row, int column) {
                            //all cells false
                            return false;
                        }
                    };
                    ipTable = new JTable(iptableModel);

                    ipTable.setFocusable(false);
                    ipTable.setIntercellSpacing(new java.awt.Dimension(0, 0));
                    ipTable.setRowHeight(25);
                    ipTable.setSelectionBackground(new java.awt.Color(232, 57, 95));
                    ipTable.setShowVerticalLines(false);
                    ipTable.getTableHeader().setReorderingAllowed(false);
                    ipTable.getTableHeader().setBackground(new Color(0, 0, 51));
                    ipTable.getTableHeader().setForeground(Color.white);
                    ipTable.getTableHeader().setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                    ipTable.setFont(new Font("JetBrains Mono", Font.PLAIN, 14));
                    ipscrollPane.setViewportView(ipTable);

                    activity_panel.add(ipscrollPane);
                    ipscrollPane.setBounds(50, 50, 1300, 600);


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
                   {
                       {
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
                    status_lbl.setBounds(50, 670, 755, 25);}

                    //---- cnt_lbl ----
                    {cnt_lbl.setText("0");
                    cnt_lbl.setForeground(Color.white);
                    networkFlow_panel.add(cnt_lbl);
                    cnt_lbl.setBounds(1180, 670, 55, cnt_lbl.getPreferredSize().height);}

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

                    notification_scrollpanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    notification_scrollpanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                    notification_scrollpanel.setViewportView(notification_panel);
                    notification_scrollpanel.setBorder(null);
                    mainPanel.add(notification_scrollpanel);
                    notification_scrollpanel.setBounds(50, 220, 1400, 700);

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

                {
                protectionsts_panel.setLayout(null);
                protectionsts_panel.setOpaque(false);


                lock_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/lock_green124x200.png")));
                lock_lbl.setBounds(120, 10, 124, 200);
                protectionsts_panel.add(lock_lbl);

                bar_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/bar_green1000x300txt.png")));
                bar_lbl.setBounds(180, -134, 1000, 500);
                protectionsts_panel.add(bar_lbl);

                }
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


                {   protectiontxt_lbl.setText("Protection level: ");
                    protectiontxt_lbl.setForeground(new Color(125, 198, 227));
                    protectiontxt_lbl.setFont(new Font("JetBrains Mono", Font.BOLD, 55));
                    mainPanel.add(protectiontxt_lbl);
                    protectiontxt_lbl.setBounds(360, 150, 455, 155);
                }

                {   protection_lbl.setText(Float.toString(protectionStatus));
                    protection_lbl.setForeground(new Color(125, 198, 227));
                    protection_lbl.setFont(new Font("JetBrains Mono", Font.BOLD, 55));
                    mainPanel.add(protection_lbl);
                    protection_lbl.setBounds(795, 150, 155, 155);
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
            mainFrame.setResizable(false);
            mainFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
        }

    }



}
