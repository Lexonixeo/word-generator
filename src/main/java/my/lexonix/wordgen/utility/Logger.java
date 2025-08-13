package my.lexonix.wordgen.utility;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    // мейби перейти на Logback? но нз
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
        String message = "["
                + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                .format(new Date(System.currentTimeMillis()))
                + "]: "
                + s
                + "\n";
        System.out.print(message);
        try {
            FileWriter writer = new FileWriter(pathname, true);
            writer.write(message);
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
        String message = "["
                + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                .format(new Date(System.currentTimeMillis()))
                + "]: ";
        stream.print(message);
        System.out.print(message);

        e.printStackTrace(stream);
        e.printStackTrace(System.out);
        System.out.println();
    }
}
