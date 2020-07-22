package core;

public enum TestAction {
    TAP("Tap"),
    SWIPE("Swipe"),
    LONG_PRESS("Long press");
    public final String name;

    TestAction(String name) {
        this.name = name;
    }
}
