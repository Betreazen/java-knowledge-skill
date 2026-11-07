// Создаёт пустую структуру Minecraft (.nbt) для GameTest: одни блоки воздуха заданного размера.
// Нужна, когда тесту не важна исходная постройка, а загрузчик не даёт своей пустой структуры
// (в Fabric API она есть — fabric-gametest-api-v1:empty, в NeoForge — нет).
//
// Запуск (JDK 17+, без сборки и зависимостей):
//   java EmptyStructure.java <out.nbt> [dataVersion=5023] [size=3]
// Положите файл в src/main/resources/data/<modid>/structure/<name>.nbt и укажите
// "structure": "<modid>:<name>" в test_instance/*.json.
// dataVersion — world_version из version.json серверного jar нужной версии игры
// (26.3 — 5023). Если указать старее, игра обновит структуру через DataFixerUpper при загрузке.

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

public class EmptyStructure {
    private static final byte END = 0;
    private static final byte INT = 3;
    private static final byte STRING = 8;
    private static final byte LIST = 9;
    private static final byte COMPOUND = 10;

    private static final String USAGE = "usage: java EmptyStructure.java <out.nbt> [dataVersion=5023] [size=3]";

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            usage("missing output file");
        }
        Path out = Path.of(args[0]);
        int dataVersion = args.length > 1 ? positiveInt(args[1], "dataVersion") : 5023;
        int size = args.length > 2 ? positiveInt(args[2], "size") : 3;
        if (out.getParent() != null) {
            Files.createDirectories(out.getParent());
        }
        try (OutputStream file = Files.newOutputStream(out);
             DataOutputStream nbt = new DataOutputStream(new GZIPOutputStream(file))) {
            nbt.writeByte(COMPOUND);
            name(nbt, "");

            nbt.writeByte(LIST);
            name(nbt, "size");
            nbt.writeByte(INT);
            nbt.writeInt(3);
            nbt.writeInt(size);
            nbt.writeInt(size);
            nbt.writeInt(size);

            nbt.writeByte(LIST);
            name(nbt, "palette");
            nbt.writeByte(COMPOUND);
            nbt.writeInt(1);
            nbt.writeByte(STRING);
            name(nbt, "Name");
            name(nbt, "minecraft:air");
            nbt.writeByte(END);

            // пустой список блоков: незаполненные позиции структуры остаются воздухом
            emptyList(nbt, "blocks");
            emptyList(nbt, "entities");

            nbt.writeByte(INT);
            name(nbt, "DataVersion");
            nbt.writeInt(dataVersion);

            nbt.writeByte(END);
        }
        System.out.println("wrote " + out + " (" + size + "x" + size + "x" + size + ", DataVersion " + dataVersion + ")");
    }

    private static int positiveInt(String value, String what) {
        try {
            int n = Integer.parseInt(value);
            if (n > 0) {
                return n;
            }
        } catch (NumberFormatException e) {
            // ниже — то же сообщение, что и для нуля или отрицательного числа
        }
        usage(what + " must be a positive integer, got: " + value);
        return 0; // недостижимо: usage завершает процесс
    }

    private static void usage(String problem) {
        System.err.println(problem);
        System.err.println(USAGE);
        System.exit(2);
    }

    private static void emptyList(DataOutputStream nbt, String name) throws IOException {
        nbt.writeByte(LIST);
        name(nbt, name);
        nbt.writeByte(COMPOUND);
        nbt.writeInt(0);
    }

    private static void name(DataOutputStream nbt, String s) throws IOException {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        nbt.writeShort(bytes.length);
        nbt.write(bytes);
    }
}
