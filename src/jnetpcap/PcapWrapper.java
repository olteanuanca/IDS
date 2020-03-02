package jnetpcap;

import org.jnetpcap.PcapIf;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("SpellCheckingInspection")
public class PcapWrapper {

    private PcapIf pcapIf;
    private String prompt;

    public PcapWrapper(PcapIf pcapIf) {
        this.pcapIf = pcapIf;
    }

    public PcapWrapper(String prompt) {
        this.prompt = prompt;
    }

    public static List<PcapWrapper> fromPcapIf(List<PcapIf> ifs) {
        List<PcapWrapper> ifWrappers = new ArrayList<>();
        for (PcapIf pcapif : ifs) {
            ifWrappers.add(new PcapWrapper(pcapif));
        }
        return ifWrappers;
    }

    public String name() {
        return pcapIf.getName();
    }

    public String description() {
        return pcapIf.getDescription();
    }

    @Override
    public String toString() {
        if (pcapIf == null) {
            return prompt;
        } else {
            return String.format("%s (%s)", pcapIf.getName(), pcapIf.getDescription());
        }
    }
}
