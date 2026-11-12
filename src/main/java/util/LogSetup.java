package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class LogSetup {
    private static boolean configured;

    private LogSetup() {}

    public static synchronized void configure() {
        if (configured) return;
        try {
            Path folder = Path.of(System.getProperty("user.home"), "CarRegistry", "logs");
            Files.createDirectories(folder);
            FileHandler handler = new FileHandler(
                    folder.resolve("car-%g.log").toString(),
                    1_000_000, 3, true);
            handler.setEncoding("UTF-8");
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.INFO);
            Logger root = Logger.getLogger("");
            root.addHandler(handler);
            configured = true;
            root.info("Файловий журнал активовано: " + folder);
        } catch (IOException ex) {
            Logger.getLogger(LogSetup.class.getName()).log(
                    Level.WARNING, "Не вдалося створити файл журналу", ex);
        }
    }
}