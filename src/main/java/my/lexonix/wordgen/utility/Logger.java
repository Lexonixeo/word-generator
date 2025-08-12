package my.lexonix.wordgen.utility;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private static final String pathname;

    static {
        new File("data").mkdirs();
        new File("data/logs").mkdirs();
        pathname =
                "data/logs/"
                        + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
                        .format(new Date(System.currentTimeMillis()))
                        + ".txt";
    }

    public static void write(String s) {
        try {
            FileWriter writer = new FileWriter(pathname, true);
            writer.write(
                    "["
                            + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                            .format(new Date(System.currentTimeMillis()))
                            + "]: "
                            + s
                            + "\n");
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void write(Exception e) {
        PrintStream stream;
        try {
            stream = new PrintStream(new FileOutputStream(pathname, true));
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        stream.print(
                "["
                        + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                        .format(new Date(System.currentTimeMillis()))
                        + "]: ");
        e.printStackTrace(stream);
    }
}
