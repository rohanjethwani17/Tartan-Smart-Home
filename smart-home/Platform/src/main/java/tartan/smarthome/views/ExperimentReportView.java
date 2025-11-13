package tartan.smarthome.views;

import io.dropwizard.views.common.View;
import tartan.smarthome.core.TartanHome;
import java.util.List;

public class ExperimentReportView extends View {
    private final List<TartanHome> homes;
    private final String configPath;

    public ExperimentReportView(List<TartanHome> homes, String configPath) {
        super("experimentReport.ftl");
        this.homes = homes;
        this.configPath = configPath;
    }

    public List<TartanHome> getHomes() {
        return homes;
    }

    public String getConfigPath() {
        return configPath;
    }
}