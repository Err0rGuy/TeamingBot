package org.linker.plnm.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class IOUtilities {


    public static String readFile(InputStream inputStream) {
        var builder = new StringBuilder();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))){
            String line;
            while((line = br.readLine()) != null)
                builder.append(line).append("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return builder.toString();
    }
}
