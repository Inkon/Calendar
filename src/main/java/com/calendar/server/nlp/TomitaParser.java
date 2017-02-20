package com.calendar.server.nlp;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class TomitaParser {
    private File tomitaBin;
    private File workDir;

    private class TomitaProcess {
        private Process tomitaProcess;

        TomitaProcess() throws IOException {
            ProcessBuilder processBuilder = new ProcessBuilder(tomitaBin.toString(), "config.proto")
                    .directory(workDir);
            tomitaProcess = processBuilder.start();
        }

        String stdoutRead() throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(tomitaProcess.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }
            return builder.toString();
        }

        void stdinWrite(String target) throws IOException {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(tomitaProcess.getOutputStream()));
            writer.write(target);
            writer.flush();
            writer.close();
        }

        int waitFor() throws InterruptedException {
            return tomitaProcess.waitFor();
        }
    }

    public TomitaParser(File tomita, File workDir) {
        this.tomitaBin = tomita;
        this.workDir = workDir;
    }

    public TomitaParser(String tomitaPath, String workDirPath) {
        this(new File(tomitaPath), new File(workDirPath));
    }

    public String parse(String inputString) {
        String result = "";
        try {
            TomitaProcess tomita = new TomitaProcess();
            tomita.stdinWrite(inputString);
            result = tomita.stdoutRead();

            tomita.waitFor();
        } catch (IOException e) {
            throw new IllegalStateException("Error while running tomita", e);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Error in tomita", e);
        }
        return result;
    }

    public HashMap<String, String> parseFact(String factString) {
        HashMap<String, String> fact = new HashMap<>();

        // Drop input and assume that have only one fact
        if (factString.contains("{") && factString.contains("}")) {
            String factBody = factString.substring(factString.indexOf("{") + 1, factString.indexOf("}") - 1);
            List<String> fields = Arrays.asList(factBody.split(System.getProperty("line.separator")));
            fields = fields.stream()
                    .map(String::trim)
                    .filter(filter -> !filter.isEmpty())
                    .collect(Collectors.toList());

            fields.forEach(field -> {
                String key = field.substring(0, field.indexOf("=")).trim();
                String value = field.substring(field.indexOf("=") + 1).trim();
                fact.put(key, value);
            });
        }

        return fact;
    }
}
