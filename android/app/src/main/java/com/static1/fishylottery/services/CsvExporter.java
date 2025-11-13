package com.static1.fishylottery.services;

import com.static1.fishylottery.model.entities.Profile;
import com.static1.fishylottery.model.entities.WaitlistEntry;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CsvExporter {

    public void exportWaitlist(List<WaitlistEntry> entries, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

            // Header
            writer.write("First Name,Last Name,Email,Phone,Joined At,Accepted At");
            writer.newLine();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            // Each accepted entrant
            for (WaitlistEntry e : entries) {
                Profile p = e.getProfile();
                String line = escapeCsv(p.getFirstName()) + "," +
                        escapeCsv(p.getLastName()) + "," +
                        escapeCsv(p.getEmail()) + "," +
                        escapeCsv(p.getFormattedPhone()) + "," +
                        escapeCsv(formatter.format(e.getJoinedAt())) + "," +
                        escapeCsv(formatter.format(e.getAcceptedAt()));
                writer.write(line);
                writer.newLine();
            }

            writer.flush();
        }
    }

    private String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }
}
