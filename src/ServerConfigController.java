import base.BaseController;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import utils.PreferencesUtils;
import utils.ServerUtils;

public class ServerConfigController extends BaseController {
    public TextField hostTf;
    public TextField portTf;
    public Button startServerBtn;
    private static final String DEF_HOST = "0.0.0.0";
    private static final String DEF_PORT = "6969";

    public void initialize() {
        String[] savedConfig = loadServerConfig();
        if (savedConfig == null) {
            saveServerConfig(DEF_HOST, DEF_PORT);
            savedConfig = loadServerConfig();
        }
        hostTf.setText(savedConfig[0]);
        portTf.setText(savedConfig[1]);

        startServerBtn.setOnMouseClicked(event -> {
            String host = hostTf.getText();
            String port = portTf.getText();
            BaseController controller = null;
            try {
                Runnable r = () -> {
                    try {
                        ServerUtils.startServer(host, port);
                    } catch (Exception e) {
                        showAlert(e.getMessage(), true);
                        e.printStackTrace();
                    }
                };
                new Thread(r).start();
                saveServerConfig(hostTf.getText(), portTf.getText());
                Thread.sleep(3000L);
                controller = openStage("fxml/capabilities.fxml", "Capabilities");
                startServerBtn.getScene().getWindow().hide();
            } catch (Exception e) {
                showAlert(e.getMessage(), true);
                e.printStackTrace();
                controller.getStage().close();
            }
        });
    }

    private void saveServerConfig(String host, String port) {
        PreferencesUtils.save(PreferencesUtils.KEY_HOST, host);
        PreferencesUtils.save(PreferencesUtils.KEY_PORT, port);
    }

    private String[] loadServerConfig() {
        String[] ret = null;
        String host = PreferencesUtils.load(PreferencesUtils.KEY_HOST);
        String port = PreferencesUtils.load(PreferencesUtils.KEY_PORT);
        if (host != null && port != null) {
            ret = new String[]{host, port};
        }
        return ret;
    }

}
