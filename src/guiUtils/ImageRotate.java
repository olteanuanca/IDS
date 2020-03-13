package guiUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageRotate extends JLabel {
    String path="src/resources/logo_transp2.png";
    double i=0;


    BufferedImage LoadImage(String path) throws IOException {
        BufferedImage img=null;
        img= ImageIO.read(new File(path));
        return img;
    }
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        BufferedImage  img=null;
        try {
             img = LoadImage(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        AffineTransform at= AffineTransform.getTranslateInstance(50,80);
        at.rotate(Math.toRadians(i),img.getWidth()/2,img.getHeight()/2);
       // at.scale(2,2);

        Graphics2D g2d=(Graphics2D)g;
        g2d.drawImage(img,at,null);
        i+=0.1;

        repaint();
    }
}
