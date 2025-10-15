package org.unibl.etf.pj2.transport.util;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.unibl.etf.pj2.transport.generator.TransportDataGenerator;
import java.io.FileReader;
import java.io.IOException;

public class TransportDataLoader {

    public static TransportDataGenerator.TransportData  load(String filePath) {
        Gson gson = new Gson();
        try (JsonReader reader = new JsonReader(new FileReader(filePath))) {
            return gson.fromJson(reader, TransportDataGenerator.TransportData.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}