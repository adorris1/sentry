package top.quantic.sentry.service.util;

import de.androidpit.colorthief.ColorThief;
import de.androidpit.colorthief.MMCQ;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.InetSocketAddress;
import java.net.URL;

import static top.quantic.sentry.service.util.Inflection.pluralize;
import static top.quantic.sentry.service.util.Inflection.singularize;

public class MiscUtil {

    public static String humanizeBytes(long bytes) {
        int unit = 1000; // 1024 for non-SI units
        if (bytes < unit)
            return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), "kMGTPE".charAt(exp - 1));
    }

    public static String inflect(long value, String label) {
        return value + " " + (value == 1 ? singularize(label) : pluralize(label));
    }

    public static Color getDominantColor(String urlStr, Color fallback) {
        try {
            URL url = new URL(urlStr);
            BufferedImage image = ImageIO.read(url);
            MMCQ.CMap result = ColorThief.getColorMap(image, 5);
            MMCQ.VBox vBox = result.vboxes.get(0);
            int[] rgb = vBox.avg(false);
            return new Color(rgb[0], rgb[1], rgb[2]);
        } catch (Exception ignore) {
        }
        return fallback;
    }

    public static InetSocketAddress getSourceServerAddress(String address) {
        int port = 0;
        if (address.indexOf(':') >= 0) {
            String[] tmpAddress = address.split(":", 2);
            port = Integer.parseInt(tmpAddress[1]);
            address = tmpAddress[0];
        }
        if (port == 0) {
            port = 27015;
        }
        return new InetSocketAddress(address, port);
    }

    private MiscUtil() {}
}
