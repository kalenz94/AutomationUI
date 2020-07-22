import base.BaseController;
import core.DeviceInteractor;
import core.TestAction;
import core.TestExecutor;
import core.TestStep;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    @FXML
    public Label coordLb;
    @FXML
    public VBox loadingMask;
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
    private ListView<StepRow> stepLv;

    private double screenImvW, screenImvH;
    private double deviceW, deviceH;

    private DeviceInteractor deviceInteractor;
    private TestExecutor testExecutor;

    Point startP, endP;
    boolean mousePressed = false;
    boolean longPressed = false;

    public void initialize() {
        ObservableList<StepRow> stepList = FXCollections.observableArrayList();
        TestStep step1 = new TestStep(TestAction.SWIPE, new Point[]{new Point(357, 1642), new Point(359, 657)});
        TestStep step2 = new TestStep(TestAction.TAP, new Point(747, 780));
        TestStep step3 = new TestStep(TestAction.TAP, new Point(150, 1829));
        TestStep step4 = new TestStep(TestAction.TAP, new Point(912, 1815));
        TestStep step5 = new TestStep(TestAction.TAP, new Point(414, 1821));
        TestStep step6 = new TestStep(TestAction.TAP, new Point(933, 2060));
        stepList.add(newStepRow(step1));
        stepList.add(newStepRow(step2));
        stepList.add(newStepRow(step3));
        stepList.add(newStepRow(step4));
        stepList.add(newStepRow(step5));
        stepList.add(newStepRow(step6));
        stepLv.setItems(stepList);

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
            }, 500L);
        });
        screenImv.setOnMouseReleased(event -> {
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
        });

        refreshBtn.setOnMouseClicked(event -> {
            showLoading();
            waitAndRefreshScreen();
        });
        backBtn.setOnMouseClicked(event -> {
            showLoading();
            deviceInteractor.pressKey(AndroidKey.BACK);
            waitAndRefreshScreen();
        });
        homeBtn.setOnMouseClicked(event -> {
            showLoading();
            deviceInteractor.pressKey(AndroidKey.HOME);
            waitAndRefreshScreen();
        });
        recentBtn.setOnMouseClicked(event -> {
            showLoading();
            deviceInteractor.pressKey(AndroidKey.APP_SWITCH);
            waitAndRefreshScreen();
        });

        runBtn.setOnMouseClicked(event -> {
            testExecutor = new TestExecutor(deviceInteractor);
            testExecutor.setTestSteps(getTestStepList());
            testExecutor.setExecuteStateListener((step, succeed) -> {
                System.out.println(step.getDescription() + " is " + (succeed ? "succeed" : "failed"));
            });
            testExecutor.execute();
        });
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
        if (selectedCapabilities.size() == 0) {
            showAlert("Please input some capabilities!", false);
            return;
        }
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
    }

    private void swipe(Point startP, Point endP) {
        System.out.println("swipe() called with: startP = [" + startP + "], endP = [" + endP + "]");
        showLoading();
        deviceInteractor.swipe(startP, endP);
        waitAndRefreshScreen();
    }

    private void longPress(Point p) {
        System.out.println("longPress() called with: p = [" + p + "]");
        showLoading();
        deviceInteractor.longPress(p);
        waitAndRefreshScreen();
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
            public void onRowEditClicked(TestStep step) {

            }

            @Override
            public void onRowDeleted(StepRow saveRow) {

            }
        });
        return stepRow;
    }

    private static class StepRow extends HBox {
        private Label descriptionLb;
        private Button editBtn;
        private Button delBtn;
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

            descriptionLb.setText(step.getDescription());

            editBtn.setOnMouseClicked(event -> {
                if (listener != null) {
                    listener.onRowEditClicked(step);
                }
            });
            delBtn.setOnMouseClicked(event -> {
                if (listener != null) {
                    listener.onRowDeleted(StepRow.this);
                }
            });
            this.getChildren().add(item);
        }

        public TestStep getTestStep() {
            return step;
        }

        public void setOnStepRowChangedListener(OnStepRowChangedListener listener) {
            this.listener = listener;
        }

    }

    private interface OnStepRowChangedListener {

        void onRowEditClicked(TestStep step);

        void onRowDeleted(StepRow saveRow);
    }

}
