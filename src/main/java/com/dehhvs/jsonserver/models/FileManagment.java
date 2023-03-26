package com.dehhvs.jsonserver.models;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class FileManagment {
    public void writeFile(String content) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("database.json", false));
        writer.write(content);
        writer.close();
    }

    public String readFile() throws FileNotFoundException {
        File myObj = new File("database.json");
        Scanner myReader = new Scanner(myObj);
        String content = "";
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            content += data;
        }
        myReader.close();
        return content;
    }
}
