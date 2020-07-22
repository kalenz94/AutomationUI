package base;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class BaseController {
    private static final Alert errorAlert = new Alert(Alert.AlertType.ERROR);

    public BaseController() {
    }

    protected void onParamSet(Object param) {
    }

    protected Stage openStage(String fxml, String title) {
        return openStage(fxml, title, null, null);
    }

    protected Stage openStage(String fxml, String title, Modality modality) {
        return openStage(fxml, title, modality, null);
    }

    protected Stage openStage(String fxml, String title, Object param) {
        return openStage(fxml, title, null, param);
    }

    protected Stage openStage(String fxml, String title, Modality modality, Object param) {
        Stage stage = new Stage();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(fxml));
            Parent root = loader.load();
            BaseController controller = loader.getController();
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stage;
    }

    protected void showAlert(String msg, boolean exitWhenClose) {
        Stage stage = (Stage) errorAlert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();
        errorAlert.setContentText(msg);
        if (exitWhenClose) {
            errorAlert.setOnCloseRequest(event -> {
                Platform.exit();
                System.exit(0);
            });
        } else {
            errorAlert.setOnCloseRequest(null);
        }
        errorAlert.show();
    }

}
