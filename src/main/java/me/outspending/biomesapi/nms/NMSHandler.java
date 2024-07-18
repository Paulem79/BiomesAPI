package me.outspending.biomesapi.nms;

import me.outspending.biomesapi.annotations.AsOf;
import me.outspending.biomesapi.exceptions.UnknownNMSVersionException;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for handling NMS (Net Minecraft Server) related operations.
 * This class is annotated with @UtilityClass from the Lombok library, which indicates that this is a utility class and hence, cannot be instantiated.
 * It also generates a private no-args constructor, which throws an exception when invoked.
 *
 * @version 0.0.1
 */
@AsOf("0.0.1")
public class NMSHandler {

    /**
     * Holds the NMS version instance.
     */
    private static NMS NMS_VERSION;

    public NMSHandler() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated.");
    }

    /**
     * Static initializer for the NMSHandler class.
     * This method is invoked when the class is loaded by the JVM.
     * It initializes the NMS version based on the server's version.
     * The server's version is retrieved from the package name of the server class.
     * The version is then used in a switch statement to instantiate the appropriate NMS version.
     * If the server's version is not supported, a RuntimeException is thrown.
     *
     * @throws RuntimeException if the server's version is not supported
     * @version 0.0.2
     */
    static {
        init();
    }

    /**
     * Initializes the NMS version based on the server's version.
     * The server's version is retrieved from the package name of the server class.
     * The version is then used in a switch statement to instantiate the appropriate NMS version.
     * If the server's version is not supported, a RuntimeException is thrown.
     *
     * @throws RuntimeException if the server's version is not supported
     * @version 0.0.1
     */
    @AsOf("0.0.1")
    static void init() {
        if (isNMSLoaded()) return;

        String version = getMinecraftVersion();
        switch (version) {
            case "1.19", "1.19.1", "1.19.2" -> NMS_VERSION = new NMS_v1_19_R1();
            case "1.19.3" -> NMS_VERSION = new NMS_v1_19_R2();
            case "1.19.4" -> NMS_VERSION = new NMS_v1_19_R3();
            case "1.20", "1.20.1" -> NMS_VERSION = new NMS_v1_20_R1();
            case "1.20.2" -> NMS_VERSION = new NMS_v1_20_R2();
            case "1.20.3", "1.20.4" -> NMS_VERSION = new NMS_v1_20_R3();
            case "1.20.5", "1.20.6" -> NMS_VERSION = new NMS_v1_20_R4();
            case "1.21" -> NMS_VERSION = new NMS_v1_21_R1();
            default -> throw new UnknownNMSVersionException("The version " + version + " is not supported by BiomesAPI. Make sure you are up-to-date with the latest version of BiomesAPI.");
        }
    }

    private static String minecraftVersion;

    /**
     * Returns the actual running Minecraft version, e.g. 1.20 or 1.16.5
     *
     * @return Minecraft version
     * @version 0.0.2
     */
    @AsOf("0.0.2")
    public static String getMinecraftVersion() {
        if (minecraftVersion != null) {
            return minecraftVersion;
        } else {
            String bukkitGetVersionOutput = Bukkit.getVersion();
            Matcher matcher = Pattern.compile("\\(MC: (?<version>[\\d]+\\.[\\d]+(\\.[\\d]+)?)\\)").matcher(bukkitGetVersionOutput);
            if (matcher.find()) {
                return minecraftVersion = matcher.group("version");
            } else {
                throw new RuntimeException("Could not determine Minecraft version from Bukkit.getVersion(): " + bukkitGetVersionOutput);
            }
        }
    }

    /**
     * Checks if the NMS version has been loaded.
     *
     * @return true if the NMS version has been loaded, false otherwise
     * @version 0.0.1
     */
    @AsOf("0.0.1")
    public static boolean isNMSLoaded() {
        return NMS_VERSION != null;
    }

    /**
     * Retrieves the NMS version instance.
     *
     * @return an Optional containing the NMS version instance if it exists, an empty Optional otherwise
     * @version 0.0.1
     */
    @AsOf("0.0.1")
    public static Optional<NMS> getNMS() {
        return Optional.ofNullable(NMS_VERSION);
    }

    /**
     * Executes the given consumer if the NMS version exists.
     *
     * @param consumer the consumer to execute
     * @version 0.0.2
     */
    @AsOf("0.0.2")
    public static void executeNMS(@NotNull Consumer<NMS> consumer) {
        getNMS().ifPresent(consumer);
    }

}
