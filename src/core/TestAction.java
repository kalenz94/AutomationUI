package core;

public enum TestAction {
    TAP("Tap"),
    SWIPE("Swipe"),
    LONG_PRESS("Long press"),
    WAIT("Wait"),
    PRESS_KEY("Press key");
    public final String name;

    TestAction(String name) {
        this.name = name;
    }
}
