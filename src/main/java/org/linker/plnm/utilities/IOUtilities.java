package org.linker.plnm.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class IOUtilities {


    public static String readFile(String path) {
        var inputStream = IOUtilities.class.getClassLoader().getResourceAsStream(path);
        if (inputStream == null) {
            throw new IllegalArgumentException("Resource not found: " + path);
        }
        var builder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                builder.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading resource: " + path, e);
        }
        return builder.toString();
    }
}
