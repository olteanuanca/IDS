package jnetpcap.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

import static utils.Utils.FILE_SEP;


public class FlowMgr {

    public static final String FLOW_SUFFIX = "_Flow.csv";
    protected static final Logger logger = LoggerFactory.getLogger(FlowMgr.class);
    private static FlowMgr Instance = new FlowMgr();

    private String mFlowSavePath;
    private String mDataPath;

    private FlowMgr() {
        super();
    }

    public static FlowMgr getInstance() {
        return Instance;
    }

    public FlowMgr init() {

        String rootPath = System.getProperty("user.dir");
        StringBuilder sb = new StringBuilder(rootPath);
        sb.append(FILE_SEP).append("data").append(FILE_SEP);

        mDataPath = sb.toString();

        sb.append("daily").append(FILE_SEP);
        mFlowSavePath = sb.toString();

        return Instance;
    }

    public void destroy() {
    }

    public String getSavePath() {
        return mFlowSavePath;
    }

    public String getmDataPath() {
        return mDataPath;
    }

    public String getAutoSaveFile() {
        String filename = LocalDate.now().toString() + FLOW_SUFFIX;
        return mFlowSavePath + filename;
    }
}
