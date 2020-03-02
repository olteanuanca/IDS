package jnetpcap.worker;

import jnetpcap.BasicFlow;

public interface FlowGenListener {
    void onFlowGenerated(BasicFlow flow);
}
