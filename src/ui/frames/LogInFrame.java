package ui.frames;
//package ui.startup;
//
//import database.DB;
//import database.IDB;
//import utils.PasswordUtils;
import com.jgoodies.forms.factories.Borders;
import guiUtils.AlphaContainer;
import guiUtils.ImageRotate;
import utils.PasswordUtils;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;


public class LogInFrame {

    //IDB db = new DB();

    private JFrame mainFrame;
    private JPanel credentialsPanel;
    private JTextField username_txtField;
    private JPasswordField password_txtField;
    private JLabel username_lbl;
    private JLabel password_lbl;
    private JButton saveCredentials_btn;
    private JLabel title;
    private JLabel frameTitle_lbl;
    private JLabel passwordinfo_lbl;
    private JLabel usernameinfo_lbl;
    private JLabel usernametxtInfo_lbl;
    private JLabel passwordtxtInfo_lbl;
    private JLabel showpass_lbl;
    private JLabel enterusername_lbl;
    private JLabel enterpassword_lbl;
    private JLabel errtxt_lbl;

    public LogInFrame() throws IOException {

        initComponents();
        mainFrame.setVisible(true);
    }

    private void checkCredentials_btnMouseClicked(MouseEvent e) throws IOException, SQLException {

                    String salt = PasswordUtils.getSalt(30);

                    String secPwd = PasswordUtils.generateSecurePassword(String.valueOf(password_txtField.getPassword()), salt);

                    int check = 0;

            //        db.startDBConn();
            //        TODO: GET CREDENTIALS FROM DB AND CHECK


        if(check==0)
          {
                      new UserDataFrame();
                      mainFrame.dispatchEvent(new WindowEvent(mainFrame, WindowEvent.WINDOW_CLOSING));
          }
        else
        {
            String errtxt="<html> Incorrect credentials.";
            errtxt+="</html>";
            errtxt_lbl.setText(errtxt);
            errtxt_lbl.setVisible(true);
            return;
        }

        }


    private void usernameinfo_lblMouseEntered(MouseEvent e) {
        usernameinfo_lbl.setIcon(new ImageIcon("src/resources/info32blue.png"));
        if(errtxt_lbl.isShowing())
        {
            errtxt_lbl.setBounds(200, 650, 655, 255);
        }
        usernametxtInfo_lbl.setVisible(true);
    }

    private void usernameinfo_lblMouseExited(MouseEvent e) {
        usernameinfo_lbl.setIcon(new ImageIcon("src/resources/info32.png"));
        if(errtxt_lbl.isShowing())
        {
            errtxt_lbl.setBounds(1100, 650, 655, 255);
        }
        usernametxtInfo_lbl.setVisible(false);
    }
    private void passwordinfo_lblMouseEntered(MouseEvent e) {
        passwordinfo_lbl.setIcon(new ImageIcon("src/resources/info32blue.png"));
        if(errtxt_lbl.isShowing())
        {
            errtxt_lbl.setBounds(200, 650, 655, 255);
        }
        passwordtxtInfo_lbl.setVisible(true);
    }

    private void passwordinfo_lblMouseExited(MouseEvent e) {
        passwordinfo_lbl.setIcon(new ImageIcon("src/resources/info32.png"));
        if(errtxt_lbl.isShowing())
        {
            errtxt_lbl.setBounds(1100, 650, 655, 255);
        }
        passwordtxtInfo_lbl.setVisible(false);
    }

    private void showpass_lblMousePressed(MouseEvent e) {
        showpass_lbl.setIcon(new ImageIcon("src/resources/eye32.png"));
        password_txtField.setEchoChar((char) 0);
    }

    private void showpass_lblMouseReleased(MouseEvent e) {
        showpass_lbl.setIcon(new ImageIcon("src/resources/eye32.png"));
        password_txtField.setEchoChar('⏺');
    }

    private void initComponents() throws IOException {

        mainFrame = new JFrame();
        credentialsPanel = new JPanel()

        { Image panelBackground = ImageIO.read(new File("src/resources/bg1920x1080.jpg"));

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(panelBackground, 0, 0, null);


            }
        };


        username_txtField = new JTextField();
        password_txtField = new JPasswordField();
        username_lbl = new JLabel();
        password_lbl = new JLabel();
        saveCredentials_btn = new JButton();
        title = new JLabel();
        frameTitle_lbl=new JLabel();
        passwordinfo_lbl = new JLabel();

        usernameinfo_lbl = new JLabel();
        usernametxtInfo_lbl = new JLabel();
        passwordtxtInfo_lbl = new JLabel();
        showpass_lbl = new JLabel();
        enterusername_lbl = new JLabel();
        enterpassword_lbl = new JLabel();
        errtxt_lbl=new JLabel();

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

            //======== credentialsPanel ========
            {
                credentialsPanel.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
                credentialsPanel.setSize(new Dimension(1920, 1080));
                credentialsPanel.setLayout(null);
                credentialsPanel.setOpaque(false);

                ImageRotate logo=new ImageRotate();
                logo.setBounds(30, -80, 800, 800);
                credentialsPanel.add(logo);
                credentialsPanel.add(username_txtField);

                username_txtField.setBounds(1220, 425, 285, 40);
                username_txtField.setBorder(new MatteBorder(0, 0, 2, 0, Color.white));
                username_txtField.setOpaque(false);
                username_txtField.setForeground(Color.white);
                username_txtField.setFont(new Font("JetBrains Mono", Font.BOLD, 16));


                credentialsPanel.add(password_txtField);
                password_txtField.setBounds(1220, 490, 250, 40);
                password_txtField.setBorder(new MatteBorder(0, 0, 2, 0, Color.white));
                password_txtField.setOpaque(false);
                password_txtField.setForeground(Color.white);
                password_txtField.setFont(new Font("JetBrains Mono", Font.BOLD, 16));


                //---- username_lbl ----
                credentialsPanel.add(username_lbl);
                username_lbl.setBounds(1550, 430, 35, 30);

                //---- password_lbl ----
                credentialsPanel.add(password_lbl);
                password_lbl.setBounds(1550, 495, 35, 30);

                //---- passwordinfo_lbl ----
                passwordinfo_lbl.setIcon(new ImageIcon("src/resources/info32.png"));
                passwordinfo_lbl.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        passwordinfo_lblMouseEntered(e);
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        passwordinfo_lblMouseExited(e);
                    }
                });
                credentialsPanel.add(passwordinfo_lbl);
                passwordinfo_lbl.setBounds(1515, 495, 35, 34);

                //---- usernameinfo_lbl ----
                usernameinfo_lbl.setIcon(new ImageIcon("src/resources/info32.png"));
                usernameinfo_lbl.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        usernameinfo_lblMouseEntered(e);
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        usernameinfo_lblMouseExited(e);
                    }
                });
                credentialsPanel.add(usernameinfo_lbl);
                usernameinfo_lbl.setBounds(1515, 430, 35, 34);

                //---- usernametxtInfo_lbl ----
                usernametxtInfo_lbl.setBackground(new Color(130, 166, 224));
                usernametxtInfo_lbl.setOpaque(true);
                usernametxtInfo_lbl.setForeground(Color.white);
                usernametxtInfo_lbl.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                usernametxtInfo_lbl.setText("<html>To meet the required security level, your username must be between 8-32 characters long. <br/>&emsp;-Usernames can contain letters (a-z, A-Z), numbers (0-9).<br/>\n&emsp;-Usernames can't contain special characters except dashes (-), underscores (_) and periods (.).</html>");
                TitledBorder tb=new TitledBorder("Requiremets");
                tb.setTitleColor(Color.white);
                tb.setTitleFont(new Font("JetBrains Mono", Font.BOLD, 14));
                usernametxtInfo_lbl.setBorder(new CompoundBorder(
                        tb,
                        Borders.DLU21));
                credentialsPanel.add(usernametxtInfo_lbl);
                usernametxtInfo_lbl.setBounds(1100, 650, 655, 255);
                usernametxtInfo_lbl.setVisible(false);

                //---- passwordtxtInfo_lbl ----
                passwordtxtInfo_lbl.setBackground(new Color(130, 166, 224));
                passwordtxtInfo_lbl.setOpaque(true);
                passwordtxtInfo_lbl.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                passwordtxtInfo_lbl.setForeground(Color.white);
                passwordtxtInfo_lbl.setText("<html>To meet the required security level, your password must be between 8 - 32 characters long and include following character types: <br/>&emsp;- English alphabet uppercase letter (A-Z)  <br/>&emsp;- English alphabet lowercase letter (a-z)  <br/>&emsp;-Decimal digit number (0-9)<br/>&emsp;- Special characters such as ~!@#$%^&*_-+=`|\\(){}[]:;'<>,.?/</html>");
                passwordtxtInfo_lbl.setBorder(new CompoundBorder(
                        tb,
                        Borders.DLU21));

                credentialsPanel.add(passwordtxtInfo_lbl);

                passwordtxtInfo_lbl.setBounds(1100, 650, 655, 255);
                passwordtxtInfo_lbl.setVisible(false);

                //---- showpass_lbl ----
                showpass_lbl.setIcon(new ImageIcon("src/resources/eye32.png"));
                showpass_lbl.setBorder(new MatteBorder(0, 0, 2, 0, Color.white));
                showpass_lbl.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        showpass_lblMousePressed(e);
                    }
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        showpass_lblMouseReleased(e);
                    }
                });
                credentialsPanel.add(showpass_lbl);
                showpass_lbl.setBounds(1465, 495, 45, 35);

                //---- enterusername_lbl ----
                enterusername_lbl.setText("Username :");
                enterusername_lbl.setForeground(Color.white);
                enterusername_lbl.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                credentialsPanel.add(enterusername_lbl);
                enterusername_lbl.setBounds(1120, 430, 130, 35);

                //---- enterpassword_lbl ----
                enterpassword_lbl.setText("Password :");
                enterpassword_lbl.setForeground(Color.white);
                enterpassword_lbl.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
                credentialsPanel.add(enterpassword_lbl);
                enterpassword_lbl.setBounds(1120, 495, 135, 30);

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
                credentialsPanel.add(errtxt_lbl);
                errtxt_lbl.setBounds(1100, 650, 655, 255);
                errtxt_lbl.setVisible(false);


                //---- saveCredentials_btn ----
                saveCredentials_btn.setText("Register");
                saveCredentials_btn.setFont(new Font("JetBrains Mono", Font.PLAIN, 14));
                saveCredentials_btn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        try {
                            checkCredentials_btnMouseClicked(e);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                credentialsPanel.add(saveCredentials_btn);
                saveCredentials_btn.setBounds(1280, 550, 160, 40);

                //---- frameTitle_lbl ----

                frameTitle_lbl.setText("Log in");
                credentialsPanel.add(frameTitle_lbl);
                frameTitle_lbl.setBounds(1138, 205, 800, 110);
                frameTitle_lbl.setForeground(new Color(102, 204, 255));
                frameTitle_lbl.setFont(new Font("JetBrains Mono", Font.PLAIN, 70));

                //---- logo ----
                title.setIcon(new ImageIcon("src/resources/title_transp.png"));
                credentialsPanel.add(title);
                title.setBounds(70, 300, 990, 403);

                {
                    // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for(int i = 0; i < credentialsPanel.getComponentCount(); i++) {
                        Rectangle bounds = credentialsPanel.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = credentialsPanel.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    credentialsPanel.setMinimumSize(preferredSize);
                    credentialsPanel.setPreferredSize(preferredSize);
                }
            }
            mainFrameContentPane.add(credentialsPanel);
            credentialsPanel.setBounds(0, 0, 1920, 1080);

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
