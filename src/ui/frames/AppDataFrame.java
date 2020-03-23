package ui.frames;

import com.jgoodies.forms.factories.Borders;
import database.DB;
import database.IDB;
import guiUtils.*;
import jnetpcap.PcapWrapper;
import jnetpcap.manager.FlowMgr;
import jnetpcap.worker.LoadNetworkAdapterWorker;
import org.jnetpcap.PcapIf;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class AppDataFrame {
    //=== general ===
    IDB db=new DB();
    private int pathloaded=0;
    private int adapterloaded=0;
    private String path1 = FlowMgr.getInstance().getSavePath() + "appSetup.txt";

    //=== frames ====
    private JFrame mainFrame;

    //=== panels ===
    private JPanel appPanel;

    //=== combo box ====
    private JComboBox networkAdapter_comboBox;

    //=== text fields ===
    private JTextField folderPath_txtField;
    private JTextField history_txtField;

    //=== labels ===
    private JButton selFolder_btn;

    private JButton networkAdapterEdit_btn;
    private JLabel networkAdapterInfo_lbl;

    private JLabel delHistory;
    private JLabel delHistory2;
    private JLabel historyInfo_lbl;
    private JLabel selFolderInfo_lbl;
    private JLabel startRadioBtn_label;
    private JLabel networkAdapterCheck_label;
    private JLabel selectFolderCheck_label;
    private JLabel title;
    private JLabel frameTitle_lbl;
    private JLabel startapp;
    private JLabel remRadioBtn_label;
    private JLabel rememberMe_label;
    private JLabel errtxt_lbl;
    private JLabel infotxt_lbl;

    //=== buttons ===
    private JButton applyChanges_btn;

   //=== radio buttons ===
    private JRadioButton yesStart_radioBtn;
    private JRadioButton noStart_radioBtn;

    private JRadioButton yesRem_radioBtn;
    private JRadioButton noRem_radioBtn;


    public AppDataFrame() throws IOException, ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {

        initComponents();
        checkSetup();
        mainFrame.setVisible(true);
    }

    public void checkSetup() throws IOException {
        File app_file = new File(path1);
        if (app_file.length() != 0) {
            BufferedReader reader = new BufferedReader(new FileReader(app_file));

            String buffer = reader.readLine();
            networkAdapter_comboBox.addItem(buffer);
            networkAdapter_comboBox.setSelectedItem(buffer);
            adapterloaded=1;
            buffer = reader.readLine();
            folderPath_txtField.setText(buffer);
            pathloaded=1;
            buffer = reader.readLine();
            history_txtField.setText(buffer);
            reader.close();


        }
    }

    private void historyInfo_lblMouseEntered(MouseEvent e) {
        historyInfo_lbl.setIcon(new ImageIcon("src/resources/info32blue.png"));
        if(errtxt_lbl.isShowing())
        {
            errtxt_lbl.setBounds(200, 650, 655, 255);
        }
        String infotxt="<html>How much time you want to keep the data collected from monitoring the network.<br/>Your selection will be saved for future uses.</html>";
     infotxt_lbl.setText(infotxt);
     infotxt_lbl.setVisible(true);
    }

    private void historyInfo_lblMouseExited(MouseEvent e) {
        historyInfo_lbl.setIcon(new ImageIcon("src/resources/info32.png"));
        if(errtxt_lbl.isShowing())
        {
            errtxt_lbl.setBounds(900, 650, 655, 255);
        }
        infotxt_lbl.setText("");
        infotxt_lbl.setVisible(false);
    }

    private void networkAdapterInfo_lblMouseEntered(MouseEvent e) {
        networkAdapterInfo_lbl.setIcon(new ImageIcon("src/resources/info32blue.png"));
        if(errtxt_lbl.isShowing())
        {
            errtxt_lbl.setBounds(200, 650, 655, 255);
        }
        String infotxt="<html>Which network interface you want to monitor.<br/>Your selection will be saved for future uses.</html>";
        infotxt_lbl.setText(infotxt);
        infotxt_lbl.setVisible(true);
    }

    private void networkAdapterInfo_lblMouseExited(MouseEvent e) {
        networkAdapterInfo_lbl.setIcon(new ImageIcon("src/resources/info32.png"));
        if(errtxt_lbl.isShowing())
        {
            errtxt_lbl.setBounds(900, 650, 655, 255);
        }
        infotxt_lbl.setText("");
        infotxt_lbl.setVisible(false);
    }

    private void selFolderInfo_lblMouseEntered(MouseEvent e) {
        selFolderInfo_lbl.setIcon(new ImageIcon("src/resources/info32blue.png"));
        if(errtxt_lbl.isShowing())
        {
            errtxt_lbl.setBounds(200, 650, 655, 255);
        }
        String infotxt="<html>The path to a direcory where you want to keep the data collected from monitoring the network.<br/>Your selection will be saved for future uses.</html>";
        infotxt_lbl.setText(infotxt);
        infotxt_lbl.setVisible(true);
    }

    private void selFolderInfo_lblMouseExited(MouseEvent e) {
        selFolderInfo_lbl.setIcon(new ImageIcon("src/resources/info32.png"));
        if(errtxt_lbl.isShowing())
        {
            errtxt_lbl.setBounds(900, 650, 655, 255);
        }
        infotxt_lbl.setText("");
        infotxt_lbl.setVisible(false);
    }

    private void startRadioBtn_labelMouseEntered(MouseEvent e) {
        startRadioBtn_label.setIcon(new ImageIcon("src/resources/info32blue.png"));
        if(errtxt_lbl.isShowing())
        {
            errtxt_lbl.setBounds(200, 650, 655, 255);
        }
        String infotxt="<html>You want to start this application automatically when your computer starts?<br/>Your selection will be saved for future uses.</html>";
        infotxt_lbl.setText(infotxt);
        infotxt_lbl.setVisible(true);
    }

    private void startRadioBtn_labelMouseExited(MouseEvent e) {
        startRadioBtn_label.setIcon(new ImageIcon("src/resources/info32.png"));
        if(errtxt_lbl.isShowing())
        {
            errtxt_lbl.setBounds(900, 650, 655, 255);
        }
        infotxt_lbl.setText("");
        infotxt_lbl.setVisible(false);
    }

    private void remRadioBtn_labelMouseEntered(MouseEvent e) {
        remRadioBtn_label.setIcon(new ImageIcon("src/resources/info32blue.png"));
        if(errtxt_lbl.isShowing())
        {
            errtxt_lbl.setBounds(200, 650, 655, 255);
        }
        String infotxt="<html>You want to save your credentials and keep you signed in?<br/>Your selection will be saved for future uses.</html>";
        infotxt_lbl.setText(infotxt);
        infotxt_lbl.setVisible(true);
    }

    private void remRadioBtn_labelMouseExited(MouseEvent e) {
        remRadioBtn_label.setIcon(new ImageIcon("src/resources/info32.png"));
        if(errtxt_lbl.isShowing())
        {
            errtxt_lbl.setBounds(900, 650, 655, 255);
        }
        infotxt_lbl.setText("");
        infotxt_lbl.setVisible(false);
    }

    private void selFolder_btnMouseClicked(MouseEvent e) throws InterruptedException {
        JFileChooser fileChooser = new JFileChooser();
        File file = null;
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int option = fileChooser.showOpenDialog(mainFrame);
        String errtxt="<html>Please insert a valid path</html>";

        if (option == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
            folderPath_txtField.setText(file.getPath());
            selectFolderCheck_label.setIcon(new ImageIcon(getClass().getResource("/resources/check32.png")));
            folderPath_txtField.setBorder(new MatteBorder(0, 0, 2, 0, Color.white));
            folderPath_txtField.setForeground(Color.white);
            errtxt_lbl.setText("");
            errtxt_lbl.setVisible(false);
           pathloaded=1;
        } else {
            if(file!=null)
            {
            if (!file.isDirectory()) {
                selectFolderCheck_label.setIcon(new ImageIcon(getClass().getResource("/resources/remove32.png")));
                folderPath_txtField.setBorder(new MatteBorder(0, 0, 2, 0, Color.red));
                folderPath_txtField.setForeground(Color.red);
                errtxt_lbl.setText(errtxt);
                errtxt_lbl.setVisible(true);
               pathloaded=0;
            } else {
                selectFolderCheck_label.setIcon(new ImageIcon(getClass().getResource("/resources/remove32.png")));
                folderPath_txtField.setBorder(new MatteBorder(0, 0, 2, 0, Color.white));
                folderPath_txtField.setForeground(Color.white);
                errtxt_lbl.setText(errtxt);
                errtxt_lbl.setVisible(true);
                pathloaded=0;
            }}
            else{
                selectFolderCheck_label.setIcon(new ImageIcon(getClass().getResource("/resources/remove32.png")));
                folderPath_txtField.setBorder(new MatteBorder(0, 0, 2, 0, Color.white));
                folderPath_txtField.setForeground(Color.white);
                errtxt_lbl.setText(errtxt);
                errtxt_lbl.setVisible(true);
                pathloaded=0;
            }
        }

    }

    private void networkAdapterEdit_btnMouseClicked(MouseEvent e) {
        networkAdapterCheck_label.setIcon(new ImageIcon(getClass().getResource("/resources/refresh.png")));
        LoadNetworkAdapterWorker task = new LoadNetworkAdapterWorker();
        task.addPropertyChangeListener(event -> {
            if ("state".equals(event.getPropertyName())) {
                LoadNetworkAdapterWorker task1 = (LoadNetworkAdapterWorker) event.getSource();
                switch (task1.getState()) {
                    case STARTED:
                        break;
                    case DONE:
                        try {
                            List<PcapIf> ifs = task1.get();
                            List<PcapWrapper> pcapiflist = PcapWrapper.fromPcapIf(ifs);

                            for (PcapWrapper pcapif : pcapiflist) {
                                networkAdapter_comboBox.addItem(pcapif);
                            }
                            networkAdapterCheck_label.setIcon(new ImageIcon(getClass().getResource("/resources/check32.png")));
                            networkAdapter_comboBox.setBackground(new Color(0, 0, 51));
                           adapterloaded=1;
                        } catch (InterruptedException | ExecutionException err) {
                            //TODO: logger.debug(e.getMessage());
                            String errtxt="<html>Please try again to load a network adapter.</html>";
                            errtxt_lbl.setText(errtxt);
                            errtxt_lbl.setVisible(true);
                            networkAdapterCheck_label.setIcon(new ImageIcon(getClass().getResource("/resources/remove32.png")));
                            networkAdapter_comboBox.setBackground(new Color(255, 102, 135));
                            adapterloaded=0;
                        }
                        break;
                }
            }
        });
        task.execute();
    }

    private void applyChanges_btnMouseClicked(MouseEvent e) throws IOException, UnsupportedLookAndFeelException {
        String errtxt="<html>";
        int radiobloaded=1;
        startapp.setForeground(Color.white);
        rememberMe_label.setForeground(Color.white);
        File tmp=new File(folderPath_txtField.getText());

        if(tmp!=null)
        {
            if( tmp.getName().equals("Select directory path to save network flows")==true)
            {
                selectFolderCheck_label.setIcon(new ImageIcon(getClass().getResource("/resources/remove32.png")));
                folderPath_txtField.setBorder(new MatteBorder(0, 0, 2, 0, Color.red));
                folderPath_txtField.setForeground(Color.red);
                pathloaded=0;
            }
            else if (!tmp.isDirectory()) {
                selectFolderCheck_label.setIcon(new ImageIcon(getClass().getResource("/resources/remove32.png")));
                folderPath_txtField.setBorder(new MatteBorder(0, 0, 2, 0, Color.red));
                folderPath_txtField.setForeground(Color.red);
                errtxt+="&emsp;The path you inserted must be to a directory.<br/>";
                pathloaded=0;
            }
            else{
                pathloaded=1;
                folderPath_txtField.setBorder(new MatteBorder(0, 0, 2, 0, Color.white));
                folderPath_txtField.setForeground(Color.white);
            }
        }
        if ((!yesStart_radioBtn.isSelected() && !noStart_radioBtn.isSelected()) || (!yesRem_radioBtn.isSelected() && !noRem_radioBtn.isSelected())) {
            errtxt+="&emsp;You can't leave fields uncompleted. Please choose an option.<br/>";
            radiobloaded=0;


        }
        if (!yesStart_radioBtn.isSelected() && !noStart_radioBtn.isSelected())
        {
            startapp.setForeground(new Color(255, 102, 135));
        }
        if(!yesRem_radioBtn.isSelected() && !noRem_radioBtn.isSelected())
        {
            rememberMe_label.setForeground(new Color(255, 102, 135));
        }
        if(adapterloaded==0)
        {
                errtxt+="&emsp;Load a network adapter.<br/>";
                networkAdapter_comboBox.setBackground(new Color(255, 102, 135));
                networkAdapterCheck_label.setIcon(new ImageIcon(getClass().getResource("/resources/remove32.png")));
        }
        if(pathloaded==0)
        {
                errtxt+="&emsp;Insert path to save data.<br/>";
                folderPath_txtField.setBorder(new MatteBorder(0, 0, 2, 0, new Color(255, 102, 135)));
                folderPath_txtField.setForeground(new Color(255, 102, 135));
        }

        if(adapterloaded==0 || pathloaded==0 || radiobloaded ==0)
        {
            errtxt+="<br/></html>";
            errtxt_lbl.setText(errtxt);
            errtxt_lbl.setVisible(true);
            return;
        }

        errtxt_lbl.setText("");
        errtxt_lbl.setVisible(false);
        File file = new File(path1);
        if (!file.getParentFile().isDirectory()) {
            file.getParentFile().mkdirs();
        }

        FileWriter writer = new FileWriter(file);
        //Network Adapter Name
        writer.write(networkAdapter_comboBox.getSelectedItem().toString());
        //Network Adapter Description
        writer.append("\n");
        //Directory path to save Network Flows
        writer.append(folderPath_txtField.getText());
        writer.append("\n");
        //Days until archive delete
        writer.append(history_txtField.getText());
        writer.append("\n");
        //start app automatically
        int startAuto=0;
        if (yesStart_radioBtn.isSelected())
        {startAuto=1;
            writer.append("yes");}
        else
        {startAuto=0;
            writer.append("no");}
        writer.append("\n");
        int remMe=0;
        if (yesRem_radioBtn.isSelected())
        {remMe=1;
            writer.append("yes");}
        else
        {remMe=0;
            writer.append("no");}
        writer.flush();
        writer.close();

            //TODO INSERT TO DB
//        db.startDBConn();
//        db.insertToAppdata(networkAdapter_comboBox.getSelectedItem().toString(),folderPath_txtField.getText(),Integer.parseInt(history_txtField.getText()),startAuto,remMe);
        new HomeFrame();
        mainFrame.dispatchEvent(new WindowEvent(mainFrame, WindowEvent.WINDOW_CLOSING));
    }

    private void initComponents() throws IOException {

        mainFrame = new JFrame();
        appPanel = new JPanel()
        { Image panelBackground = ImageIO.read(new File("src/resources/bg1920x1080.jpg"));

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(panelBackground, 0, 0, null);
            }
        };
        networkAdapter_comboBox = new JComboBox();
        folderPath_txtField = new JTextField();
        selFolder_btn = new JButton();
        networkAdapterEdit_btn = new JButton();
        networkAdapterInfo_lbl = new JLabel();
        delHistory = new JLabel();
        history_txtField = new JTextField();
        delHistory2 = new JLabel();
        historyInfo_lbl = new JLabel();
        selFolderInfo_lbl = new JLabel();
        applyChanges_btn = new JButton();
        networkAdapterCheck_label = new JLabel();
        selectFolderCheck_label = new JLabel();
        frameTitle_lbl = new JLabel();
        yesStart_radioBtn = new JRadioButton();
        startapp = new JLabel();
        noStart_radioBtn = new JRadioButton();
        startRadioBtn_label = new JLabel();
        yesRem_radioBtn = new JRadioButton();
        noRem_radioBtn = new JRadioButton();
        remRadioBtn_label = new JLabel();
        rememberMe_label = new JLabel();
        title = new JLabel();
        errtxt_lbl=new JLabel();
        infotxt_lbl=new JLabel();

        //======== mainFrame ========
        {
            ImageIcon img = new ImageIcon("src/resources/logo_border_small.jpg");
            mainFrame.setIconImage(img.getImage());
            mainFrame.setTitle("SecIT Solutions");

            var mainFrameContentPane = mainFrame.getContentPane();
            mainFrameContentPane.setLayout(null);
            Toolkit tk = Toolkit.getDefaultToolkit();
            int xSize = ((int) tk.getScreenSize().getWidth());
            int ySize = ((int) tk.getScreenSize().getHeight());
            mainFrame.setSize(xSize,ySize);


            //======== appPanel ========
            {
                appPanel.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
                appPanel.setSize(new Dimension(1920, 1080));
                appPanel.setLayout(null);

                ImageRotate  logo=new ImageRotate();
                logo.setBounds(30, -80, 800, 800);
                appPanel.add(logo);

                //---- networkAdapter_comboBox ----

                networkAdapter_comboBox.setPrototypeDisplayValue("Select default Network Adapter");
                networkAdapter_comboBox.setBorder(null);
                networkAdapter_comboBox.setBackground(new Color(0, 0, 51));
                networkAdapter_comboBox.setOpaque(true);
                networkAdapter_comboBox.setForeground(Color.white);
                networkAdapter_comboBox.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                appPanel.add(networkAdapter_comboBox);
                networkAdapter_comboBox.setBounds(800, 215, 880, 40);

                //---- folderPath_txtField ----
                folderPath_txtField.setBorder(new MatteBorder(0, 0, 2, 0, Color.white));
                folderPath_txtField.setOpaque(false);
                folderPath_txtField.setForeground(Color.white);
                folderPath_txtField.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                folderPath_txtField.setText("Select directory path to save network flows");
                appPanel.add(folderPath_txtField);
                folderPath_txtField.setBounds(800, 280, 880, 40);

                //---- selFolder_btn ----
                selFolder_btn.setIcon(new ImageIcon(getClass().getResource("/resources/magnifier32.png")));
                selFolder_btn.setBackground(new Color(0, 0, 51));
                selFolder_btn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        try {
                            selFolder_btnMouseClicked(e);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                appPanel.add(selFolder_btn);
                selFolder_btn.setBounds(1690, 280, 40, 40);

                //---- networkAdapterEdit_btn ----
                networkAdapterEdit_btn.setBackground(new Color(0, 0, 51));
                networkAdapterEdit_btn.setIcon(new ImageIcon(getClass().getResource("/resources/magnifier32.png")));
                networkAdapterEdit_btn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        networkAdapterEdit_btnMouseClicked(e);
                    }
                });
                appPanel.add(networkAdapterEdit_btn);
                networkAdapterEdit_btn.setBounds(1690, 215, 40, 40);

                //---- networkAdapterInfo_lbl ----
                networkAdapterInfo_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/info32.png")));
                networkAdapterInfo_lbl.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        networkAdapterInfo_lblMouseEntered(e);
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        networkAdapterInfo_lblMouseExited(e);
                    }
                });
                appPanel.add(networkAdapterInfo_lbl);
                networkAdapterInfo_lbl.setBounds(1735, 215, 40, 40);

                //---- delHistory ----
                delHistory.setText("Delete history in");
                delHistory.setForeground(Color.white);
                delHistory.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                appPanel.add(delHistory);
                delHistory.setBounds(800, 340, 140, 40);

                //---- history_txtField ----
                history_txtField.setText("7");
                history_txtField.setBorder(new MatteBorder(0, 0, 2, 0, Color.white));
                history_txtField.setOpaque(false);
                history_txtField.setForeground(Color.white);
                history_txtField.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                history_txtField.setPreferredSize(new Dimension(65, 40));
                appPanel.add(history_txtField);
                history_txtField.setBounds(945, 340, 35, 40);

                //---- delHistory2 ----
                delHistory2.setText("days.");
                delHistory2.setForeground(Color.white);
                delHistory2.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                appPanel.add(delHistory2);
                delHistory2.setBounds(995, 340, 60, 40);

                //---- historyInfo_lbl ----
                historyInfo_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/info32.png")));
                historyInfo_lbl.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        historyInfo_lblMouseEntered(e);
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        historyInfo_lblMouseExited(e);
                    }
                });
                appPanel.add(historyInfo_lbl);
                historyInfo_lbl.setBounds(1060, 340, 40, 40);

                //---- selFolderInfo_lbl ----
                selFolderInfo_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/info32.png")));
                selFolderInfo_lbl.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        selFolderInfo_lblMouseEntered(e);
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        selFolderInfo_lblMouseExited(e);
                    }
                });
                appPanel.add(selFolderInfo_lbl);
                selFolderInfo_lbl.setBounds(1735, 280, 40, 40);

                //---- applyChanges_btn ----
                applyChanges_btn.setText("Apply changes");
                applyChanges_btn.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                applyChanges_btn.setForeground(Color.white);
                applyChanges_btn.setBackground(new Color(3, 211, 252, 35));
                applyChanges_btn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        try {
                            applyChanges_btnMouseClicked(e);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } catch (UnsupportedLookAndFeelException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                appPanel.add(applyChanges_btn);
                applyChanges_btn.setBounds(1100, 555, 185, 45);

                //---- networkAdapterCheck_label ----
                appPanel.add(networkAdapterCheck_label);
                networkAdapterCheck_label.setBounds(1775, 215, 40, 40);

                //---- selectFolderCheck_label ----
                appPanel.add(selectFolderCheck_label);
                selectFolderCheck_label.setBounds(1775, 280, 40, 40);

                //---- logo ----
                title.setIcon(new ImageIcon("src/resources/title_transp.png"));
                appPanel.add(title);
                title.setBounds(70, 300, 990, 403);

                //---- frameTitle_lbl ----
                frameTitle_lbl.setText("Setup");
                frameTitle_lbl.setForeground(new Color(102, 204, 255));
                frameTitle_lbl.setFont(new Font("JetBrains Mono", Font.PLAIN, 70));
                appPanel.add(frameTitle_lbl);
                frameTitle_lbl.setBounds(800, 50, 800, 110);


                //---- startapp ----
                startapp.setText("Start app automatically:");
                startapp.setForeground(Color.white);
                startapp.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                appPanel.add(startapp);
                startapp.setBounds(800, 400, 205, 45);

                //---- yesStart_radioBtn ----
                yesStart_radioBtn.setText("yes");
                yesStart_radioBtn.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                yesStart_radioBtn.setOpaque(false);
                yesStart_radioBtn.setForeground(Color.white);

                appPanel.add(yesStart_radioBtn);
                yesStart_radioBtn.setBounds(1000, 400, 80, yesStart_radioBtn.getPreferredSize().height);

                //---- noStart_radioBtn ----
                noStart_radioBtn.setText("no");
                noStart_radioBtn.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                noStart_radioBtn.setOpaque(false);
                noStart_radioBtn.setForeground(Color.white);
                appPanel.add(noStart_radioBtn);
                noStart_radioBtn.setBounds(1000, 425, 80, noStart_radioBtn.getPreferredSize().height);

                ButtonGroup group = new ButtonGroup();
                group.add(yesStart_radioBtn);
                group.add(noStart_radioBtn);

                //---- startRadioBtn_label ----
                startRadioBtn_label.setIcon(new ImageIcon(getClass().getResource("/resources/info32.png")));
                startRadioBtn_label.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        startRadioBtn_labelMouseEntered(e);
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        startRadioBtn_labelMouseExited(e);
                    }
                });
                appPanel.add(startRadioBtn_label);
                startRadioBtn_label.setBounds(1060, 400, 40, 40);

                //---- yesRem_radioBtn ----
                yesRem_radioBtn.setText("yes");
                yesRem_radioBtn.setForeground(Color.white);
                yesRem_radioBtn.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                yesRem_radioBtn.setOpaque(false);
                appPanel.add(yesRem_radioBtn);
                yesRem_radioBtn.setBounds(1000, 485, 80, 19);

                //---- noRem_radioBtn ----
                noRem_radioBtn.setText("no");
                noRem_radioBtn.setForeground(Color.white);
                noRem_radioBtn.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                noRem_radioBtn.setOpaque(false);
                appPanel.add(noRem_radioBtn);
                noRem_radioBtn.setBounds(1000, 510, 50, 19);

                ButtonGroup group2 = new ButtonGroup();
                group2.add(yesRem_radioBtn);
                group2.add(noRem_radioBtn);

                //---- remRadioBtn_label ----
                remRadioBtn_label.setIcon(new ImageIcon(getClass().getResource("/resources/info32.png")));
                remRadioBtn_label.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        remRadioBtn_labelMouseEntered(e);
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        remRadioBtn_labelMouseExited(e);
                    }
                });
                appPanel.add(remRadioBtn_label);
                remRadioBtn_label.setBounds(1060, 485, 40, 40);

                //---- rememberMe_label ----
                rememberMe_label.setText("Keep me signed in:");
                rememberMe_label.setForeground(Color.white);
                rememberMe_label.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                appPanel.add(rememberMe_label);
                rememberMe_label.setBounds(800, 485, 185, 45);

                //---- errtxt_lbl ----
                errtxt_lbl.setBackground(new Color(255, 102, 135));
                errtxt_lbl.setOpaque(true);
                errtxt_lbl.setForeground(Color.white);
                errtxt_lbl.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                TitledBorder tb2=new TitledBorder("Errors");
                tb2.setTitleColor(Color.white);
                tb2.setTitleFont(new Font("JetBrains Mono", Font.BOLD, 14));
                errtxt_lbl.setBorder(new CompoundBorder(
                        tb2,
                        Borders.DLU21));
                appPanel.add(errtxt_lbl);
                errtxt_lbl.setBounds(900, 650, 655, 255);
                errtxt_lbl.setVisible(false);

                //---- infotxt_lbl ----
                infotxt_lbl.setBackground(new Color(130, 166, 224));
                infotxt_lbl.setOpaque(true);
                infotxt_lbl.setForeground(Color.white);
                infotxt_lbl.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                TitledBorder tb=new TitledBorder("Requiremets");
                tb.setTitleColor(Color.white);
                tb.setTitleFont(new Font("JetBrains Mono", Font.BOLD, 14));
                infotxt_lbl.setBorder(new CompoundBorder(
                        tb,
                        Borders.DLU21));
                appPanel.add(infotxt_lbl);
                infotxt_lbl.setBounds(900, 650, 655, 255);
                infotxt_lbl.setVisible(false);

                {
                    // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for(int i = 0; i < appPanel.getComponentCount(); i++) {
                        Rectangle bounds = appPanel.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = appPanel.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    appPanel.setMinimumSize(preferredSize);
                    appPanel.setPreferredSize(preferredSize);
                }
            }
            mainFrameContentPane.add(appPanel);
            appPanel.setBounds(0, 0, xSize, ySize);

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
