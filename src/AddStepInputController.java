import base.BaseController;
import core.TestAction;
import core.TestStep;
import io.appium.java_client.android.nativekey.AndroidKey;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.WindowEvent;

import java.awt.*;
import java.util.Arrays;

public class AddStepInputController extends BaseController implements DeviceInteractController.OnCoordsPickedListener {
    @FXML
    private AnchorPane tapVarPane;
    @FXML
    private TextField tapXTf, tapYTf;
    @FXML
    private Button pickTapCoordBtn;

    @FXML
    private AnchorPane swipeVarPane;
    @FXML
    private TextField swipeXTf, swipeXTf2, swipeYTf, swipeYTf2;
    @FXML
    private Button pickSwipeCoordBtn1, pickSwipeCoordBtn2;

    @FXML
    private AnchorPane longPressVarPane;
    @FXML
    private TextField longpressXTf, longpressYTf;
    @FXML
    private Button pickLongpressCoordBtn;

    @FXML
    private AnchorPane waitVarPane;
    @FXML
    private TextField waitTimeTf;

    @FXML
    private AnchorPane keyPressVarPane;
    @FXML
    private ChoiceBox<AndroidKey> keyCodeCb;
    private static final AndroidKey[] supportedKeys = {AndroidKey.HOME, AndroidKey.BACK, AndroidKey.APP_SWITCH};

    @FXML
    private ChoiceBox<TestAction> actionCb;
    @FXML
    private Button okBtn, cancelBtn;

    private OnStepAddListener listener;

    private TextField coordPickTfX;
    private TextField coordPickTfY;

    public void initialize() {
        actionCb.getItems().add(TestAction.TAP);
        actionCb.getItems().add(TestAction.SWIPE);
        actionCb.getItems().add(TestAction.LONG_PRESS);
        actionCb.getItems().add(TestAction.WAIT);
        actionCb.getItems().add(TestAction.PRESS_KEY);

        ObservableList<AndroidKey> keyList = FXCollections.observableArrayList();
        keyList.addAll(Arrays.asList(supportedKeys));
        keyCodeCb.setItems(keyList);

        actionCb.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue) {
                case TAP:
                    tapVarPane.setVisible(true);
                    swipeVarPane.setVisible(false);
                    longPressVarPane.setVisible(false);
                    waitVarPane.setVisible(false);
                    keyPressVarPane.setVisible(false);
                    break;
                case SWIPE:
                    tapVarPane.setVisible(false);
                    swipeVarPane.setVisible(true);
                    longPressVarPane.setVisible(false);
                    waitVarPane.setVisible(false);
                    keyPressVarPane.setVisible(false);
                    break;
                case LONG_PRESS:
                    tapVarPane.setVisible(false);
                    swipeVarPane.setVisible(false);
                    longPressVarPane.setVisible(true);
                    waitVarPane.setVisible(false);
                    keyPressVarPane.setVisible(false);
                    break;
                case WAIT:
                    tapVarPane.setVisible(false);
                    swipeVarPane.setVisible(false);
                    longPressVarPane.setVisible(false);
                    waitVarPane.setVisible(true);
                    keyPressVarPane.setVisible(false);
                    break;
                case PRESS_KEY:
                    tapVarPane.setVisible(false);
                    swipeVarPane.setVisible(false);
                    longPressVarPane.setVisible(false);
                    waitVarPane.setVisible(false);
                    keyPressVarPane.setVisible(true);
                    break;
            }
        });

        okBtn.setOnMouseClicked(event -> {
            TestStep step = null;
            switch (actionCb.getSelectionModel().getSelectedItem()) {
                case TAP:
                    step = addTap();
                    break;
                case SWIPE:
                    step = addSwipe();
                    break;
                case LONG_PRESS:
                    step = addLongPress();
                    break;
                case WAIT:
                    step = addWait();
                    break;
                case PRESS_KEY:
                    step = addKeyPress();
                    break;
            }
            if (listener != null) {
                listener.onOK(step);
                getStage().close();
            }
        });
        cancelBtn.setOnMouseClicked(event -> {
            if (listener != null) {
                listener.onCancel();
                getStage().close();
            }
        });

        coordPickTfX = null;
        coordPickTfY = null;
        pickTapCoordBtn.setOnMouseClicked(event -> {
            coordPickTfX = tapXTf;
            coordPickTfY = tapYTf;
        });
        pickSwipeCoordBtn1.setOnMouseClicked(event -> {
            coordPickTfX = swipeXTf;
            coordPickTfY = swipeYTf;
        });
        pickSwipeCoordBtn2.setOnMouseClicked(event -> {
            coordPickTfX = swipeXTf2;
            coordPickTfY = swipeYTf2;
        });
        pickLongpressCoordBtn.setOnMouseClicked(event -> {
            coordPickTfX = longpressXTf;
            coordPickTfY = longpressYTf;
        });


        actionCb.getSelectionModel().select(0);
        keyCodeCb.getSelectionModel().select(0);
    }

    private TestStep addTap() {
        TestAction action = actionCb.getSelectionModel().getSelectedItem();
        Point value = new Point(Integer.parseInt(tapXTf.getText()), Integer.parseInt(tapYTf.getText()));
        return new TestStep(action, value);
    }

    private TestStep addSwipe() {
        TestAction action = actionCb.getSelectionModel().getSelectedItem();
        Point[] value = new Point[2];
        value[0] = new Point(Integer.parseInt(swipeXTf.getText()), Integer.parseInt(swipeYTf.getText()));
        value[1] = new Point(Integer.parseInt(swipeXTf2.getText()), Integer.parseInt(swipeYTf2.getText()));
        return new TestStep(action, value);
    }

    private TestStep addLongPress() {
        TestAction action = actionCb.getSelectionModel().getSelectedItem();
        Point value = new Point(Integer.parseInt(longpressXTf.getText()), Integer.parseInt(longpressYTf.getText()));
        return new TestStep(action, value);
    }

    private TestStep addWait() {
        TestAction action = actionCb.getSelectionModel().getSelectedItem();
        long value = Long.parseLong(waitTimeTf.getText());
        return new TestStep(action, value);
    }

    private TestStep addKeyPress() {
        TestAction action = actionCb.getSelectionModel().getSelectedItem();
        AndroidKey value = keyCodeCb.getSelectionModel().getSelectedItem();
        return new TestStep(action, value);
    }

    @Override
    protected void onParamSet(Object param) {
        TestStep step = (TestStep) param;
        actionCb.getSelectionModel().select(step.getAction());
        switch (step.getAction()) {
            case TAP:
                Point p = (Point) step.getValue();
                tapXTf.setText(String.valueOf((int) p.getX()));
                tapYTf.setText(String.valueOf((int) p.getY()));
                break;
            case SWIPE:
                Point[] pts = (Point[]) step.getValue();
                Point p1 = pts[0];
                Point p2 = pts[1];
                swipeXTf.setText(String.valueOf((int) p1.getX()));
                swipeYTf.setText(String.valueOf((int) p1.getY()));
                swipeXTf2.setText(String.valueOf((int) p2.getX()));
                swipeYTf2.setText(String.valueOf((int) p2.getY()));
                break;
            case LONG_PRESS:
                p = (Point) step.getValue();
                longpressXTf.setText(String.valueOf((int) p.getX()));
                longpressYTf.setText(String.valueOf((int) p.getY()));
                break;
            case WAIT:
                long l = (long) step.getValue();
                waitTimeTf.setText(String.valueOf(l));
                break;
            case PRESS_KEY:
                AndroidKey k = (AndroidKey) step.getValue();
                keyCodeCb.getSelectionModel().select(k);
                break;
        }
    }

    @Override
    protected void onStageClosed(WindowEvent event) {
        if (listener != null) {
            listener.onCancel();
        }
    }

    public void setOnStepAddListener(OnStepAddListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCoordPicked(int x, int y) {
        if (coordPickTfX != null && coordPickTfY != null) {
            coordPickTfX.setText(String.valueOf(x));
            coordPickTfY.setText(String.valueOf(y));
        }
    }

    public interface OnStepAddListener {
        void onOK(TestStep step);

        void onCancel();
    }
}
