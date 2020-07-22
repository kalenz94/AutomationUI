package core;

import core.TestAction;

import java.awt.*;

public class TestStep {
    private TestAction action;
    private Object value;


    public TestStep(TestAction action, Object value) {
        this.action = action;
        this.value = value;
    }

    public TestAction getAction() {
        return action;
    }

    public Object getValue() {
        return value;
    }

    public String getDescription() {
        switch (action) {
            case TAP:
                if (value instanceof Point) {
                    return action.name + " (" + ((Point) value).x + ":" + ((Point) value).y + ")";
                }
            case SWIPE:
                if (value instanceof Point[]) {
                    return action.name + " from (" + ((Point[]) value)[0].x + ":" + ((Point[]) value)[0].y + ") to (" + ((Point[]) value)[1].x + ":" + ((Point[]) value)[1].y + ")";
                }
            case LONG_PRESS:
                if (value instanceof Point) {
                    return action.name + " (" + ((Point) value).x + ":" + ((Point) value).y + ")";
                }
        }
        return "Invalid action";
    }

}
