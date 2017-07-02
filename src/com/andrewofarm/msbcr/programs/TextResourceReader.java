package com.andrewofarm.msbcr.programs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Andrew on 6/8/17.
 */
abstract class TextResourceReader {

    static String readFile(String path) {
        try {
            File file = new File(path);
            FileInputStream in = new FileInputStream(file);

            byte[] data = new byte[(int) file.length()];
            //noinspection ResultOfMethodCallIgnored
            in.read(data);

            in.close();

            return new String(data);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
