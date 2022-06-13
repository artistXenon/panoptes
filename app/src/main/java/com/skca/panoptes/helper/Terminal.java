package com.skca.panoptes.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Terminal {

    public static String exec(String command) throws IOException {
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        StringBuilder log = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) log.append(line + "\n");

        return log.toString();
    }
}
