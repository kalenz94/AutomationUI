package base;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class BaseController {
    private static final Alert errorAlert = new Alert(Alert.AlertType.ERROR);
    private Stage stage;

    public BaseController() {
    }

    protected void onParamSet(Object param) {
    }

    private void setStage(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }

    protected void onStageClosed(WindowEvent event) {

    }

    protected BaseController openStage(String fxml, String title) throws Exception {
        return openStage(fxml, title, null, null);
    }

    protected BaseController openStage(String fxml, String title, Modality modality) throws Exception {
        return openStage(fxml, title, modality, null);
    }

    protected BaseController openStage(String fxml, String title, Object param) throws Exception {
        return openStage(fxml, title, null, param);
    }

    protected BaseController openStage(String fxml, String title, Modality modality, Object param) throws Exception {
        BaseController controller;
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(fxml));
        Parent root = loader.load();
        controller = loader.getController();
        if (param != null) {
            controller.onParamSet(param);
        }
        if (modality != null) {
            stage.initModality(modality);
        }
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.show();
        controller.setStage(stage);
        BaseController finalController = controller;
        stage.setOnCloseRequest(finalController::onStageClosed);
        return controller;
    }

    protected void showAlert(String msg, boolean exitWhenClose) {
        errorAlert.setContentText(msg);
        if (exitWhenClose) {
            errorAlert.setOnCloseRequest(event -> {
                Platform.exit();
                System.exit(0);
            });
        } else {
            errorAlert.setOnCloseRequest(null);
        }
        Stage stage = (Stage) errorAlert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();
        errorAlert.show();
    }

}
