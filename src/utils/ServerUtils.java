package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ServerUtils {

    public static void startServer(String host, String port) throws Exception {
        System.out.println("Starting server...");
        Runtime runtime = Runtime.getRuntime();
        runtime.exec("cmd.exe /c start cmd.exe /k \"appium -a " + host + " -p " + port + " --session-override -dc \"{\"\"noReset\"\": \"\"false\"\"}\"\"");
    }

    public static void stopServer() throws IOException {
        System.out.println("Stopping server...");
        Runtime runtime = Runtime.getRuntime();
        runtime.exec("taskkill /F /IM node.exe");
        runtime.exec("taskkill /F /IM cmd.exe");
    }
}
