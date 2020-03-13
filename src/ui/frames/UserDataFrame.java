package ui.frames;

import com.jgoodies.forms.factories.Borders;
import database.DB;
import database.IDB;
import guiUtils.ImageRotate;
import jnetpcap.manager.FlowMgr;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.regex.Pattern;


public class UserDataFrame {

    IDB db = new DB();

    //==== general ====
    private int completed = 0;
    private String path1 = FlowMgr.getInstance().getSavePath() +"profilePic.txt";
    private String path2 = FlowMgr.getInstance().getSavePath() + "userSetup.txt";

    //==== frames ====
    private JFrame mainFrame;

    //==== pannels ====
    private JPanel mainPanel;

    //==== buttons ====
    private JButton applyChanges_btn;

    //==== labels ====
    private JLabel deviceName_lbl;
    private JLabel company_lbl;
    private JLabel companyRole_lbl;
    private JLabel name_lbl;
    private JLabel email_lbl;

    private JLabel profilePic_lbl;
    private JLabel changePic_lbl;

    private JLabel errtxt_lbl;
    private JLabel infotxt_lbl;

    private JLabel title;

    //==== text fields ====
    private JTextField name_txtField;
    private JTextField email_txtField;
    private JTextField company_txtField;
    private JTextField companyRole_txtField;
    private JTextField deviceName_txtField;



    public UserDataFrame() throws IOException {
        initComponents();
        checkSetup();

        mainFrame.setVisible(true);
    }

    /* Check if email is valid*/
    public static boolean isValid(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }

    /* Print default settings if exist */
    public void checkSetup() throws IOException {
        File profilePic_file = new File(path1);
        File userData_file = new File(path2);

        if (profilePic_file.length() != 0) {
            BufferedReader reader = new BufferedReader(new FileReader(profilePic_file));
            String path = reader.readLine();

            reader.close();
            profilePic_lbl.setIcon(ResizeImage(path));
        }


        if (userData_file.length() != 0) {
            BufferedReader reader = new BufferedReader(new FileReader(userData_file));

            String buffer = reader.readLine();
            name_txtField.setText(buffer);
            buffer = reader.readLine();
            email_txtField.setText(buffer);
            buffer = reader.readLine();
            company_txtField.setText(buffer);
            buffer = reader.readLine();
            companyRole_txtField.setText(buffer);
            buffer = reader.readLine();
            deviceName_txtField.setText(buffer);
            reader.close();
        }


    }

    /* Resize profile picture if needed */
    public ImageIcon ResizeImage(String ImagePath) {
        ImageIcon MyImage = new ImageIcon(ImagePath);
        Image img = MyImage.getImage();
        Image newImg = img.getScaledInstance(profilePic_lbl.getWidth(), profilePic_lbl.getHeight(), Image.SCALE_SMOOTH);
        ImageIcon image = new ImageIcon(newImg);
        return image;
    }

    /* Change profile picture */
    private void changePic_lblMouseClicked(MouseEvent e) throws IOException {
        JFileChooser file = new JFileChooser();
        file.setCurrentDirectory(new File(System.getProperty("user.home")));
        //filter the files
        FileNameExtensionFilter filter = new FileNameExtensionFilter("*.Images", "jpg", "gif", "png");
        file.addChoosableFileFilter(filter);
        int result = file.showSaveDialog(null);
        //if the user click on save in Jfilechooser
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = file.getSelectedFile();
            String path = selectedFile.getAbsolutePath();
            profilePic_lbl.setIcon(ResizeImage(path));

            File saveImg = new File(path1);
            if (!saveImg.getParentFile().isDirectory()) {
                saveImg.getParentFile().mkdirs();
            }

            FileWriter writer = new FileWriter(saveImg);
            //ProfilePic
            writer.write(path);
            writer.flush();
            writer.close();

        }
        //if the user click on save in Jfilechooser
        else if (result == JFileChooser.CANCEL_OPTION) {
            System.out.println("No File Select");
        }

    }

    /* Apply changes */
    private void applyChanges_btnMouseClicked(MouseEvent e) throws IOException, UnsupportedLookAndFeelException {
        String errtxt="<html>";
        if (!name_txtField.getText().equals("Lastname Firstname") && name_txtField.getText() != null) {
            completed++;
        } else {
            errtxt+="&emsp;You must enter your name.<br/>";
        }
        if (!email_txtField.getText().equals("Email Address") && email_txtField.getText() != null && isValid(email_txtField.getText())) {
            completed++;
        } else {
            errtxt+="&emsp;You must enter your email address.<br/>";
        }
        if (!company_txtField.getText().equals("Company Name") && company_txtField.getText() != null) {
            completed++;
        } else {
            errtxt+="&emsp;You must enter your company name.<br/>";
        }

        if (!companyRole_txtField.getText().equals("Your Company role / title") && companyRole_txtField.getText() != null) {
            completed++;
        } else {
            errtxt+="&emsp;You must enter your company role.<br/>";
        }

        if (!deviceName_txtField.getText().equals("Your Device name") && deviceName_txtField.getText() != null) {
            completed++;
        } else {
            errtxt+="&emsp;You must enter your device name.<br/>";
        }

        if (completed != 5) {
            errtxt+="&emsp;</html>";
            errtxt_lbl.setText(errtxt);
            errtxt_lbl.setVisible(true);
            return;
        } else {
            errtxt_lbl.setText("");
            errtxt_lbl.setVisible(false);
            File file = new File(path2);
            if (!file.getParentFile().isDirectory()) {
                file.getParentFile().mkdirs();
            }
            errtxt_lbl.setText("");
            errtxt_lbl.setVisible(false);

            FileWriter writer = new FileWriter(file);
            //L F Name
            writer.write(name_txtField.getText());
            //Email
            writer.append("\n");
            writer.append(email_txtField.getText());
            //Company
            writer.append("\n");
            writer.append(company_txtField.getText());
            //Company Role
            writer.append("\n");
            writer.append(companyRole_txtField.getText());
            //Device Name
            writer.append("\n");
            writer.append(deviceName_txtField.getText());
            writer.flush();
            writer.close();

            //TODO print green label for success
        }


        String lname = name_txtField.getText().substring(0, name_txtField.getText().indexOf(" "));
        String fname = name_txtField.getText().substring(name_txtField.getText().indexOf(" ") + 1);

        File profilePic_file = new File(path1);
        String path = null;
        if (profilePic_file.length() != 0) {
            BufferedReader reader = new BufferedReader(new FileReader(profilePic_file));
            path = reader.readLine();

            reader.close();
        }
        db.startDBConn();
        db.insertToUsers(lname, fname, email_txtField.getText(), company_txtField.getText(), companyRole_txtField.getText(), deviceName_txtField.getText(), path);

    }

    /* set up GUI components*/
    private void initComponents() throws IOException {

        //==== frames ====
        mainFrame = new JFrame();

        //==== pannels ====
        mainPanel = new JPanel()
            /* Set background picture */
        { Image panelBackground = ImageIO.read(new File("src/resources/bg1920x1080.jpg"));

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(panelBackground, 0, 0, null);
            }
        };

        // ==== buttons ====
        applyChanges_btn = new JButton();

        //==== text fields =====
        name_txtField = new JTextField();
        email_txtField = new JTextField();
        company_txtField = new JTextField();
        companyRole_txtField = new JTextField();
        deviceName_txtField = new JTextField();

      //==== labels ====
        company_lbl = new JLabel();
        companyRole_lbl = new JLabel();
        name_lbl = new JLabel();
        email_lbl = new JLabel();
        deviceName_lbl = new JLabel();

        profilePic_lbl = new JLabel();
        changePic_lbl = new JLabel();

        title = new JLabel();
        errtxt_lbl=new JLabel();
        infotxt_lbl=new JLabel();


        //======== mainFrame ========
        {
            var mainFrameContentPane = mainFrame.getContentPane();
            mainFrameContentPane.setLayout(null);
            Toolkit tk = Toolkit.getDefaultToolkit();
            int xSize = ((int) tk.getScreenSize().getWidth());
            int ySize = ((int) tk.getScreenSize().getHeight());
            mainFrame.setSize(xSize,ySize);
            mainFrame.setExtendedState(Frame.MAXIMIZED_BOTH);

            //======== mainPanel ========
            {
                    /* set size */
                mainPanel.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
                mainPanel.setSize(new Dimension(xSize, ySize));
                mainPanel.setLayout(null);

                    /* set logo */
                ImageRotate logo=new ImageRotate();
                logo.setBounds(30, -80, 800, 800);
                mainPanel.add(logo);

                //---- applyChanges_btn ----
                {
                applyChanges_btn.setText("Apply changes");
                applyChanges_btn.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                applyChanges_btn.setForeground(Color.white);
                applyChanges_btn.setBackground(new Color(3, 211, 252, 35));
                applyChanges_btn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        try {
                            applyChanges_btnMouseClicked(e);
                        } catch (IOException | UnsupportedLookAndFeelException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                mainPanel.add(applyChanges_btn);
                applyChanges_btn.setBounds(xSize-820, ySize-525, (int) (xSize/10.37), (int) (ySize/24));
                }

                //---- title ----
                {  title.setIcon(new ImageIcon("src/resources/title_transp.png"));
                    mainPanel.add(title);
                    title.setBounds(70, 300, 990, 403);}

                //---- name_txtField ----
                {
                name_txtField.setText("Lastname Firstname");
                name_txtField.setBorder(new MatteBorder(0, 0, 2, 0, Color.white));
                name_txtField.setOpaque(false);
                name_txtField.setForeground(Color.white);
                name_txtField.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                mainPanel.add(name_txtField);
                name_txtField.setBounds(xSize-870, ySize-885, (int)(xSize/8), (int)(ySize/27));
                }

                //---- email_txtField ----
                {
                email_txtField.setText("Email Address");
                email_txtField.setBorder(new MatteBorder(0, 0, 2, 0, Color.white));
                email_txtField.setOpaque(false);
                email_txtField.setForeground(Color.white);
                email_txtField.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                mainPanel.add(email_txtField);
                email_txtField.setBounds(xSize-870, ySize-810, (int) (xSize/6.62), (int)(ySize/27));
                }

                //---- company_txtField ----
                { company_txtField.setText("Company Name");
                company_txtField.setBorder(new MatteBorder(0, 0, 2, 0, Color.white));
                company_txtField.setOpaque(false);
                company_txtField.setForeground(Color.white);
                company_txtField.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                mainPanel.add(company_txtField);
                company_txtField.setBounds(xSize-870, ySize-755, (int) (xSize/6.62), (int)(ySize/27));}

                //---- companyRole_txtField ----
                {companyRole_txtField.setText("Your Company role / title");
                companyRole_txtField.setBorder(new MatteBorder(0, 0, 2, 0, Color.white));
                companyRole_txtField.setOpaque(false);
                companyRole_txtField.setForeground(Color.white);
                companyRole_txtField.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                mainPanel.add(companyRole_txtField);
                companyRole_txtField.setBounds(xSize-870, ySize-695, (int) (xSize/6.62), (int)(ySize/27));}

                //---- deviceName_txtField ----
                {deviceName_txtField.setText("Your Device name");
                    deviceName_txtField.setBorder(new MatteBorder(0, 0, 2, 0, Color.white));
                    deviceName_txtField.setOpaque(false);
                    deviceName_txtField.setForeground(Color.white);
                    deviceName_txtField.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                    mainPanel.add(deviceName_txtField);
                    deviceName_txtField.setBounds(1050, 445, 290, 40);

                    //---- deviceName_lbl ----
                    deviceName_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/info32.png")));
                    deviceName_lbl.addMouseListener(new MouseAdapter()  {
                        @Override
                        public void mouseEntered(MouseEvent e) {
                            deviceName_lblMouseEntered(e);
                        }
                        @Override
                        public void mouseExited(MouseEvent e) {
                            deviceName_lblMouseExited(e);
                        }
                    });
                    mainPanel.add(deviceName_lbl);
                    deviceName_lbl.setBounds(1350, 450, 35, 30);}

                //---- company_lbl ----
                {company_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/info32.png")));
                company_lbl.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        company_lblMouseEntered(e);
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        company_lblMouseExited(e);
                    }
                });
                mainPanel.add(company_lbl);
                company_lbl.setBounds(xSize-570, ySize-750, (int) (xSize/54.85), (int)(ySize/36));}

                //---- companyRole_lbl ----
                {companyRole_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/info32.png")));
                companyRole_lbl.addMouseListener(new MouseAdapter()  {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        companyRole_lblMouseEntered(e);
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        companyRole_lblMouseExited(e);
                    }
                });
                mainPanel.add(companyRole_lbl);
                companyRole_lbl.setBounds(1350, 390, 35, 30);}

                //---- name_lbl ----
                {name_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/info32.png")));
                name_lbl.addMouseListener(new MouseAdapter()  {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        name_lblMouseEntered(e);
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        name_lblMouseExited(e);
                    }
                });
                mainPanel.add(name_lbl);
                name_lbl.setBounds(1350, 200, 35, 35);}

                //---- email_lbl ----
               { email_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/info32.png")));
                email_lbl.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        email_lblMouseEntered(e);
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        email_lblMouseExited(e);
                    }
                });
                mainPanel.add(email_lbl);
                email_lbl.setBounds(1350, 275, 35, 40);}

                //---- profilePic_lbl ----
               { profilePic_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/girl.png")));
                mainPanel.add(profilePic_lbl);
                profilePic_lbl.setBounds((int)(xSize/1.74), (int)(ySize/18), (int)(xSize/16.69), (int)(ySize/8.64));

                //---- changePic_lbl ----
                changePic_lbl.setIcon(new ImageIcon(getClass().getResource("/resources/change.png")));
                changePic_lbl.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        try {
                            changePic_lblMouseClicked(e);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                mainPanel.add(changePic_lbl);
                changePic_lbl.setBounds(xSize-712,  ySize-930, (int)(xSize/48), (int)(ySize/30));}

                //---- errtxt_lbl ----
               { errtxt_lbl.setBackground(new Color(255, 23, 45, 72));
                errtxt_lbl.setOpaque(true);
                errtxt_lbl.setForeground(Color.white);
                errtxt_lbl.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                TitledBorder tb2=new TitledBorder("Errors");
                tb2.setTitleColor(Color.white);
                tb2.setTitleFont(new Font("JetBrains Mono", Font.BOLD, 14));
                errtxt_lbl.setBorder(new CompoundBorder(
                        tb2,
                        Borders.DLU21));
                mainPanel.add(errtxt_lbl);
                errtxt_lbl.setBounds(xSize-1800, ySize-430, (int) (xSize/2.93), (int)(ySize/4.23));
                errtxt_lbl.setVisible(false);}

                //---- infotxt_lbl ----
               { infotxt_lbl.setBackground(new Color(2, 210, 255,150));
                infotxt_lbl.setOpaque(true);
                infotxt_lbl.setForeground(Color.white);
                infotxt_lbl.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                TitledBorder tb=new TitledBorder("Requiremets");
                tb.setTitleColor(Color.white);
                tb.setTitleFont(new Font("JetBrains Mono", Font.BOLD, 14));
                infotxt_lbl.setBorder(new CompoundBorder(
                        tb,
                        Borders.DLU21));
                mainPanel.add(infotxt_lbl);
                infotxt_lbl.setBounds(900, 650, 655, 255);
                infotxt_lbl.setVisible(false);}

                //==== main pannel size ====
                {
                    // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for (int i = 0; i < mainPanel.getComponentCount(); i++) {
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
                mainFrameContentPane.add(mainPanel);
                mainPanel.setBounds(0, 0, xSize, ySize);
            }

            //==== main frame size ====
            {
                // compute preferred size
                Dimension preferredSize = new Dimension();
                for (int i = 0; i < mainFrameContentPane.getComponentCount(); i++) {
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

       /* set info */
    private void deviceName_lblMouseEntered(MouseEvent e) {
        String infotxt="<html>Your device name.<br/>Your selection will be saved for future uses.</html>";
        infotxt_lbl.setText(infotxt);
        infotxt_lbl.setVisible(true);
    }

    /* set info */
    private void email_lblMouseEntered(MouseEvent e) {
        String infotxt="<html>Your email.<br/>Your selection will be saved for future uses.</html>";
        infotxt_lbl.setText(infotxt);
        infotxt_lbl.setVisible(true);
    }

    /* set info */
    private void name_lblMouseEntered(MouseEvent e) {
        String infotxt="<html>Your name.<br/>Your selection will be saved for future uses.</html>";
        infotxt_lbl.setText(infotxt);
        infotxt_lbl.setVisible(true);
    }

    /* set info */
    private void companyRole_lblMouseEntered(MouseEvent e) {
        String infotxt="<html>Your company role.<br/>Your selection will be saved for future uses.</html>";
        infotxt_lbl.setText(infotxt);
        infotxt_lbl.setVisible(true);
    }

    /* set info */
    private void company_lblMouseEntered(MouseEvent e) {
        String infotxt="<html>Your company name.<br/>Your selection will be saved for future uses.</html>";
        infotxt_lbl.setText(infotxt);
        infotxt_lbl.setVisible(true);
    }

    /* unset info */
    private void company_lblMouseExited(MouseEvent e) {
        infotxt_lbl.setText("");
        infotxt_lbl.setVisible(false);
    }

    /* unset info */
    private void companyRole_lblMouseExited(MouseEvent e) {
        infotxt_lbl.setText("");
        infotxt_lbl.setVisible(false);
    }

    /* unset info */
    private void name_lblMouseExited(MouseEvent e) {
        infotxt_lbl.setText("");
        infotxt_lbl.setVisible(false);
    }

    /* unset info */
    private void email_lblMouseExited(MouseEvent e) {
        infotxt_lbl.setText("");
        infotxt_lbl.setVisible(false);
    }

    /* unset info */
    private void deviceName_lblMouseExited(MouseEvent e) {
        infotxt_lbl.setText("");
        infotxt_lbl.setVisible(false);
    }


}
