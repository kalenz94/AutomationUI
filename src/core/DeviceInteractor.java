package core;

import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.touch.offset.PointOption;
import javafx.scene.image.Image;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;

import java.awt.*;
import java.io.ByteArrayInputStream;

public class DeviceInteractor {
    private AndroidDriver driver;
    private TouchAction touchAction;

    public DeviceInteractor(AndroidDriver driver) {
        this.driver = driver;
        touchAction = new TouchAction(driver);
    }

    public Image getScreenShot() {
        byte[] imgData = driver.getScreenshotAs(OutputType.BYTES);
        ByteArrayInputStream bis = new ByteArrayInputStream(imgData);
        return new Image(bis);
    }

    public Dimension getScreenSize() {
        return driver.manage().window().getSize();
    }

    public void tap(Point p) {
        touchAction.tap(PointOption.point(p.x, p.y)).perform();
    }

    public void swipe(Point startP, Point endP) {
        touchAction.press(PointOption.point(startP.x, startP.y)).waitAction().moveTo(PointOption.point(endP.x, endP.y)).release().perform();
    }

    public void longPress(Point p) {
//        touchAction.longPress(LongPressOptions.longPressOptions().withDuration(Duration.ofMillis(duration)).withPosition(PointOption.point(p.x, p.y))).perform();
        touchAction.longPress(PointOption.point(p.x, p.y)).perform();
    }

    public void pressKey(AndroidKey key) {
        driver.pressKey(new KeyEvent(key));
    }

}
