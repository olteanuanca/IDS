package ui.panels;

import jnetpcap.worker.TrafficFlowWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

public class NetworkFlowPanel extends JPanel {

    protected static final Logger logger = LoggerFactory.getLogger(NetworkFlowPanel.class);

    private JPanel networkFlow_panel;
    private JScrollPane table_panel;
    private JTable table;
    private TrafficFlowWorker mWorker;
}
