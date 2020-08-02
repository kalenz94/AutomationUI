import base.BaseController;
import core.DeviceInteractor;
import core.TestAction;
import core.TestExecutor;
import core.TestStep;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Capability;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.remote.DesiredCapabilities;
import utils.PreferencesUtils;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DeviceInteractController extends BaseController {
    private static final String ADD_STEP_TITLE = "Add step";
    @FXML
    private AnchorPane screenPane;
    @FXML
    private AnchorPane controlPane;
    @FXML
    private AnchorPane stepPane;
    @FXML
    private Label coordLb;
    @FXML
    private VBox loadingMask;
    @FXML
    public Button backBtn;
    @FXML
    public Button homeBtn;
    @FXML
    public Button recentBtn;
    @FXML
    private ImageView screenImv;
    @FXML
    private Button refreshBtn;
    @FXML
    private Button runBtn;
    @FXML
    private Button recordBtn;
    @FXML
    private Button addStepBtn;
    @FXML
    private ListView<StepRow> stepLv;

    private double screenImvW, screenImvH;
    private double deviceW, deviceH;

    private DeviceInteractor deviceInteractor;
    private TestExecutor testExecutor;

    private Point startP, endP;
    private boolean mousePressed = false;
    private boolean longPressed = false;
    private boolean isRecording = false;
    private long lastActionTime;

    // Flag to check if AddStepInput dialog is displaying.
    private boolean isAddingStep = false;
    private OnCoordsPickedListener listener;

    public void initialize() {
        screenImv.setOnMouseMoved(event -> {
            Point p = getMouseRelativePos(event.getX(), event.getY());
            coordLb.setText(p.x + ":" + p.y);
            if (coordLb.getLayoutBounds().contains(event.getX(), event.getY())) {
                coordLb.setVisible(false);
            } else {
                coordLb.setVisible(true);
            }
        });
        screenImv.setOnMousePressed(event -> {
            if (!isAddingStep) {
                mousePressed = true;
                startP = getMouseRelativePos(event.getX(), event.getY());
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (mousePressed) {
                            longPressed = true;
                            longPress(startP);
                        }
                    }
                }, 1000L);
            }
        });
        screenImv.setOnMouseReleased(event -> {
            if (!isAddingStep) {
                mousePressed = false;
                if (!screenImv.getLayoutBounds().contains(event.getX(), event.getY()) || longPressed) {
                    longPressed = false;
                    return;
                }
                endP = getMouseRelativePos(event.getX(), event.getY());
                if (startP.distance(endP) == 0) {
                    tap(endP);
                } else {
                    swipe(startP, endP);
                }
            } else if (listener != null) {
                Point pickedPoint = getMouseRelativePos(event.getX(), event.getY());
                listener.onCoordPicked(pickedPoint.x, pickedPoint.y);
            }
        });

        refreshBtn.setOnMouseClicked(event -> {
            showLoading();
            waitAndRefreshScreen();
        });
        backBtn.setOnMouseClicked(event -> {
            pressKey(AndroidKey.BACK);
        });
        homeBtn.setOnMouseClicked(event -> {
            pressKey(AndroidKey.HOME);
        });
        recentBtn.setOnMouseClicked(event -> {
            pressKey(AndroidKey.APP_SWITCH);
        });

        runBtn.setOnMouseClicked(event -> {
            testExecutor = new TestExecutor(deviceInteractor);
            testExecutor.setTestSteps(getTestStepList());
            testExecutor.setExecuteStateListener(new TestExecutor.ExecuteStateListener() {
                @Override
                public void onStepExecuteStarted() {
                    runBtn.setDisable(true);
                }

                @Override
                public void onStepExecuted(TestStep step, boolean success) {
                    System.out.println(step.getDescription() + " is " + (success ? "succeed" : "failed"));
                    refreshScreen();
                }

                @Override
                public void onStepExecuteFinished() {
                    runBtn.setDisable(false);
                }
            });
            testExecutor.execute();
        });

        recordBtn.setOnMouseClicked(event -> {
            if (isRecording) {
                isRecording = false;
                recordBtn.setText("Start record");
                runBtn.setDisable(false);
            } else {
                isRecording = true;
                recordBtn.setText("Stop record");
                runBtn.setDisable(true);
            }
            lastActionTime = 0;
        });

        addStepBtn.setOnMouseClicked(event -> {
            toggleAddingStepMode(true);
            AddStepInputController controller = null;
            try {
                controller = (AddStepInputController) openStage("fxml/add_step_input.fxml", ADD_STEP_TITLE);
                listener = controller;
            } catch (Exception e) {
                e.printStackTrace();
                controller.getStage().close();
            }
            controller.getStage().setAlwaysOnTop(true);
            controller.setOnStepAddListener(new AddStepInputController.OnStepAddListener() {
                @Override
                public void onOK(TestStep step) {
                    toggleAddingStepMode(false);
                    stepLv.getItems().add(newStepRow(step));
                }

                @Override
                public void onCancel() {
                    toggleAddingStepMode(false);
                }
            });
        });
    }

    private void toggleAddingStepMode(boolean flag) {
        isAddingStep = flag;
        controlPane.setDisable(flag);
        stepPane.setDisable(flag);
    }

    private Point getMouseRelativePos(double mouseX, double mouseY) {
        Point p = new Point();
        p.x = (int) (mouseX / screenImvW * deviceW);
        p.y = (int) (mouseY / screenImvH * deviceH);
        return p;
    }

    @Override
    protected void onParamSet(Object param) {
        try {
            List<Capability> capabilities = (List<Capability>) param;
            startSession(capabilities);
        } catch (Exception e) {
            showAlert(e.getMessage(), false);
        }
    }

    private void startSession(List<Capability> selectedCapabilities) {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        for (Capability cap : selectedCapabilities) {
            desiredCapabilities.setCapability(cap.getName(), cap.getValue());
        }
        URL remoteUrl = null;
        try {
            remoteUrl = new URL("http://localhost:" + PreferencesUtils.load(PreferencesUtils.KEY_PORT) + "/wd/hub");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            showAlert(e.getMessage(), false);
        }
        AndroidDriver driver = new AndroidDriver(remoteUrl, desiredCapabilities);
        deviceInteractor = new DeviceInteractor(driver);
        refreshScreen();
    }

    private void refreshScreen() {
        Image screenShot = deviceInteractor.getScreenShot();
        screenImv.setImage(screenShot);
        screenImvW = screenImv.getLayoutBounds().getWidth();
        screenImvH = screenImv.getLayoutBounds().getHeight();
        loadingMask.setPrefHeight(screenImvH);
        //System.out.println("screenImvW = " + screenImvW + ", screenImvH = " + screenImvH);
        Dimension d = deviceInteractor.getScreenSize();
        deviceW = d.width;
        deviceH = d.height;
        //System.out.println("deviceW = " + deviceW + ", deviceH = " + deviceH);
        hideLoading();
    }

    private void waitAndRefreshScreen() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                refreshScreen();
            }
        }, 500L);
    }

    private void showLoading() {
        loadingMask.setVisible(true);
    }

    private void hideLoading() {
        loadingMask.setVisible(false);
    }

    private void tap(Point p) {
        System.out.println("tap() called with: p = [" + p + "]");
        showLoading();
        deviceInteractor.tap(p);
        waitAndRefreshScreen();
        if (isRecording) {
            considerAddWaitStep();
            TestStep step = new TestStep(TestAction.TAP, p);
            stepLv.getItems().add(newStepRow(step));
        }
        lastActionTime = System.currentTimeMillis();
    }

    private void swipe(Point startP, Point endP) {
        System.out.println("swipe() called with: startP = [" + startP + "], endP = [" + endP + "]");
        showLoading();
        deviceInteractor.swipe(startP, endP);
        waitAndRefreshScreen();
        if (isRecording) {
            considerAddWaitStep();
            TestStep step = new TestStep(TestAction.SWIPE, new Point[]{startP, endP});
            stepLv.getItems().add(newStepRow(step));
        }
        lastActionTime = System.currentTimeMillis();
    }

    private void longPress(Point p) {
        System.out.println("longPress() called with: p = [" + p + "]");
        showLoading();
        deviceInteractor.longPress(p);
        waitAndRefreshScreen();
        if (isRecording) {
            considerAddWaitStep();
            TestStep step = new TestStep(TestAction.LONG_PRESS, p);
            stepLv.getItems().add(newStepRow(step));
        }
        lastActionTime = System.currentTimeMillis();
    }

    private void pressKey(AndroidKey key) {
        showLoading();
        deviceInteractor.pressKey(key);
        waitAndRefreshScreen();
        if (isRecording) {
            considerAddWaitStep();
            TestStep step = new TestStep(TestAction.PRESS_KEY, key);
            stepLv.getItems().add(newStepRow(step));
        }
        lastActionTime = System.currentTimeMillis();
    }

    private void considerAddWaitStep() {
        if (lastActionTime != 0) {
            TestStep waitStep = new TestStep(TestAction.WAIT, System.currentTimeMillis() - lastActionTime);
            stepLv.getItems().add(newStepRow(waitStep));
        }
    }

    private List<TestStep> getTestStepList() {
        List<TestStep> steps = new ArrayList<>();
        ObservableList<StepRow> rows = stepLv.getItems();
        for (StepRow row : rows) {
            steps.add(row.getTestStep());
        }
        return steps;
    }

    private StepRow newStepRow(TestStep step) {
        StepRow stepRow = new StepRow(step);
        stepRow.setOnStepRowChangedListener(new OnStepRowChangedListener() {
            @Override
            public void onRowEditClicked(StepRow row) {
                toggleAddingStepMode(true);
                AddStepInputController controller = null;
                try {
                    controller = (AddStepInputController) openStage("fxml/add_step_input.fxml", ADD_STEP_TITLE, row.getTestStep());
                    listener = controller;
                } catch (Exception e) {
                    e.printStackTrace();
                    controller.getStage().close();
                }
                controller.getStage().setAlwaysOnTop(true);
                controller.setOnStepAddListener(new AddStepInputController.OnStepAddListener() {
                    @Override
                    public void onOK(TestStep step) {
                        row.setTestStep(step);
                        row.reloadDescription();
                        toggleAddingStepMode(false);
                    }

                    @Override
                    public void onCancel() {
                        toggleAddingStepMode(false);
                    }
                });
            }

            @Override
            public void onRowDeleted(StepRow saveRow) {
                stepLv.getItems().remove(saveRow);
            }
        });
        return stepRow;
    }

    private static class StepRow extends HBox {
        private final Label descriptionLb;
        private final Button editBtn;
        private final Button delBtn;
        private TestStep step;
        OnStepRowChangedListener listener;

        StepRow(TestStep step) {
            super();
            Parent item = null;
            this.step = step;
            try {
                item = FXMLLoader.load(getClass().getResource("fxml/device_interact_step_list_item.fxml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            descriptionLb = (Label) item.lookup("#descriptionLb");
            editBtn = (Button) item.lookup("#editBtn");
            delBtn = (Button) item.lookup("#delBtn");

            reloadDescription();

            editBtn.setOnMouseClicked(event -> {
                if (listener != null) {
                    listener.onRowEditClicked(StepRow.this);
                }
            });
            delBtn.setOnMouseClicked(event -> {
                if (listener != null) {
                    listener.onRowDeleted(StepRow.this);
                }
            });
            this.getChildren().add(item);
        }

        public void reloadDescription() {
            descriptionLb.setText(step.getDescription());
        }

        public TestStep getTestStep() {
            return step;
        }

        public void setTestStep(TestStep step) {
            this.step = step;
        }

        public void setOnStepRowChangedListener(OnStepRowChangedListener listener) {
            this.listener = listener;
        }

    }

    private interface OnStepRowChangedListener {
        void onRowEditClicked(StepRow row);

        void onRowDeleted(StepRow row);
    }

    public interface OnCoordsPickedListener {
        void onCoordPicked(int x, int y);
    }

}
