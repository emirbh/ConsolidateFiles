package com.ebh.consolidatefiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;

import static java.util.Collections.singletonList;
import static org.apache.commons.codec.digest.DigestUtils.md5Hex;

public class Main {

    private static Stream<Path> getFiles(Path dir) {
        try {
            return Files.list(dir)
                    .flatMap(path -> path.toFile().isDirectory() ? getFiles(path) : singletonList(path).stream());
        } catch(Exception e) {
            System.out.println(e);
        }
        return null;
    }

    private static String getUniqueContent(Path path) {
        try {
            FileInputStream fis = new FileInputStream(path.toFile());
            String md5 = md5Hex(fis);
            fis.close();
            return md5;
        } catch(IOException e) {
            System.out.println(e);
        }
        return null;
    }

    private static String storeUnique(String outputFolder, Path sourcePath, DateFormat dateFormat,
                                      HashMap<String, Integer> counts) {
        try {
            BasicFileAttributes attr = Files.readAttributes(sourcePath, BasicFileAttributes.class);
            String              date = dateFormat.format(attr.creationTime().toMillis());
            counts.putIfAbsent(date, 0);
            FileUtils.copyFile(sourcePath.toFile(),
                    new File(outputFolder+"/" +
                            date + "_" + String.valueOf(counts.put(date, counts.get(date)+1)) + "-" + sourcePath.getFileName()));
            System.out.println(sourcePath.toFile().toString());
        } catch(IOException e) {
            System.out.println(e);
        }
        return sourcePath.toFile().toString();
    }

    public static void main(String[] args) {
        final String     imageRegex = "([^\\s]+(\\.(?i)(jpg|jpeg|png|gif|bmp))$)";
        final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        final Pattern    matchEntry = Pattern.compile(CliArguments.getInstance(args).getFilter());
        HashMap<String, Integer> counts = new HashMap<String, Integer>();
        try {
            getFiles(Paths.get(CliArguments.getInstance(args).getInputFolder()))
                    .filter(item -> matchEntry.matcher(item.toString()).matches())
                    .collect(Collectors.groupingBy(Main::getUniqueContent))
                    .forEach((uid, items) ->
                            storeUnique(CliArguments.getInstance(args).getOutputFolder(), items.get(0), dateFormat, counts));
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

