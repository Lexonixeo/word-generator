package my.lexonix.wordgen.gateway.console;

import java.io.IOException;
import java.util.Scanner;

public class Console {
    private final Scanner scan = new Scanner(System.in);

    public Console() {

    }

    public void run() {
        String cmd;
        while (true) {
            System.out.print("Введите команду: ");
            cmd = scan.nextLine();
            // TODO
        }
    }

    private void clear() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033\143");
            }
        } catch (IOException | InterruptedException ignored) {
        }
    }
}
