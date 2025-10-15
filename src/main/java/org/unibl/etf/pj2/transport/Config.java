package org.unibl.etf.pj2.transport;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private static final Properties props = new Properties();

    static {
        try (var in = Config.class.getResourceAsStream("/config.properties")) {
            if (in != null) {
                props.load(in);
            } else {
                System.err.println("Nije pronađen config.properties - koristi se podrazumijevana podešavanja.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static int getRows() {
        return Integer.parseInt(props.getProperty("rows", "5"));
    }

    public static int getCols() {
        return Integer.parseInt(props.getProperty("cols", "5"));
    }

    public static String getInvoicesDir() {
        return props.getProperty("invoicesDir", "racuni");
    }
}
