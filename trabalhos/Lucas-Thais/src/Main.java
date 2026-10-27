import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Main {
    public static void main(String[] args) throws IOException {
        PersistentBST tree = new PersistentBST();
        StringBuilder out = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(Path.of(args[0]))) {
            String line;
            while ((line = reader.readLine()) != null) {
                execute(line.trim().split("\\s+"), tree, out);
            }
        }
        System.out.print(out);
    }

    private static void execute(String[] command, PersistentBST tree, StringBuilder out) {
        switch (command[0]) {
            case "INC" -> tree.insert(Integer.parseInt(command[1]));
            case "REM" -> tree.remove(Integer.parseInt(command[1]));
            case "SUC" -> {
                int key = Integer.parseInt(command[1]);
                int version = Integer.parseInt(command[2]);
                echo(out, "SUC " + key + " " + version, tree.successor(key, version));
            }
            case "IMP" -> {
                int version = Integer.parseInt(command[1]);
                echo(out, "IMP " + version, tree.print(version));
            }
            default -> { }
        }
    }

    private static void echo(StringBuilder out, String command, String result) {
        out.append(command).append('\n').append(result).append('\n');
    }
}
