package core;

import io.appium.java_client.android.nativekey.AndroidKey;

import java.awt.*;
import java.util.List;

public class TestExecutor {
    private Thread executor;
    private DeviceInteractor deviceInteractor;
    private List<TestStep> testSteps;
    private ExecuteStateListener listener;

    public TestExecutor(DeviceInteractor deviceInteractor) {
        this.deviceInteractor = deviceInteractor;
    }

    public TestExecutor(DeviceInteractor deviceInteractor, List<TestStep> testSteps) {
        this.deviceInteractor = deviceInteractor;
        this.testSteps = testSteps;
    }

    public void setTestSteps(List<TestStep> testSteps) {
        this.testSteps = testSteps;
    }

    public void execute() {
        if (listener != null) {
            listener.onStepExecuteStarted();
        }
        Runnable r = () -> {
            for (TestStep step : testSteps) {
                TestAction action = step.getAction();
                Object value = step.getValue();
                switch (action) {
                    case TAP:
                        if (value instanceof Point) {
                            Point p = (Point) value;
                            deviceInteractor.tap(p);
                            notifyExecuteStepSucceed(step);
                        } else {
                            notifyExecuteStepFailed(step);
                        }
                        break;
                    case SWIPE:
                        if (value instanceof Point[]) {
                            Point[] ps = (Point[]) value;
                            if (ps.length == 2) {
                                deviceInteractor.swipe(ps[0], ps[1]);
                                notifyExecuteStepSucceed(step);
                            } else {
                                notifyExecuteStepFailed(step);
                            }
                        } else {
                            notifyExecuteStepFailed(step);
                        }
                        break;
                    case LONG_PRESS:
                        if (value instanceof Point) {
                            Point p = (Point) value;
                            deviceInteractor.longPress(p);
                            notifyExecuteStepSucceed(step);
                        } else {
                            notifyExecuteStepFailed(step);
                        }
                        break;
                    case PRESS_KEY:
                        if (value instanceof AndroidKey) {
                            AndroidKey key = (AndroidKey) value;
                            deviceInteractor.pressKey(key);
                            notifyExecuteStepSucceed(step);
                        } else {
                            notifyExecuteStepFailed(step);
                        }
                    case WAIT:
                        if (value instanceof Long) {
                            try {
                                Thread.sleep((Long) value);
                                notifyExecuteStepSucceed(step);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                notifyExecuteStepFailed(step);
                            }
                        } else {
                            notifyExecuteStepFailed(step);
                        }
                        break;
                    default:
                        notifyExecuteStepFailed(step);
                }
            }
            if (listener != null) {
                listener.onStepExecuteFinished();
            }
        };
        executor = new Thread(r);
        executor.start();
    }

    private void notifyExecuteStepSucceed(TestStep step) {
        if (listener != null) {
            listener.onStepExecuted(step, true);
        }
    }

    private void notifyExecuteStepFailed(TestStep step) {
        if (listener != null) {
            listener.onStepExecuted(step, false);
        }
    }

    public void setExecuteStateListener(ExecuteStateListener listener) {
        this.listener = listener;
    }

    public interface ExecuteStateListener {
        void onStepExecuteStarted();
        void onStepExecuted(TestStep step, boolean success);
        void onStepExecuteFinished();
    }

}
