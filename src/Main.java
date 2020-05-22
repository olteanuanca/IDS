import jnetpcap.manager.FlowMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui.frames.*;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class Main {
    public static final Logger logger = LoggerFactory.getLogger(Main.class);


    public static void main(String[] args) throws IOException, ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        EventQueue.invokeLater(() -> {
            try {
              FlowMgr.getInstance().init();
                 //new SignUpFrame();
                  // new LogInFrame();
                    // new AppDataFrame();
                // new UserDataFrame();
                new HomeFrame();


            } catch (Exception e) {
                logger.debug(e.getMessage());
            }
        });

    }
}