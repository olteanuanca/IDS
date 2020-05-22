package utils;

public class Attack {
    private String srcIP;
    private String dstIP;
    private Float srcPort;
    private Float dstPort;
    private int protocol;
    private Float flowDuration;
    private String tmstamp;
    private int result;

    public Attack(String srcIP,String dstIP,Float srcPort,Float dstPort,int protocol,Float flowDuration, String tmstamp,int result)
    {
        this.srcIP=srcIP;
        this.dstIP=dstIP;
        this.srcPort=srcPort;
        this.dstPort=dstPort;
        this.protocol=protocol;
        this.flowDuration=flowDuration;
        this.tmstamp=tmstamp;
        this.result=result;
    }


    public String getSrcIP() {
        return srcIP;
    }

    public void setSrcIP(String srcIP) {
        this.srcIP = srcIP;
    }

    public String getDstIP() {
        return dstIP;
    }

    public void setDstIP(String dstIP) {
        this.dstIP = dstIP;
    }

    public Float getSrcPort() {
        return srcPort;
    }

    public void setSrcPort(Float srcPort) {
        this.srcPort = srcPort;
    }

    public Float getDstPort() {
        return dstPort;
    }

    public void setDstPort(Float dstPort) {
        this.dstPort = dstPort;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public Float getFlowDuration() {
        return flowDuration;
    }

    public void setFlowDuration(Float flowDuration) {
        this.flowDuration = flowDuration;
    }

    public String getTmstamp() {
        return tmstamp;
    }

    public void setTmstamp(String tmstamp) {
        this.tmstamp = tmstamp;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }
}
