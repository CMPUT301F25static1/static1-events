package com.static1.fishylottery.services;

import com.static1.fishylottery.model.entities.Profile;
import com.static1.fishylottery.model.entities.WaitlistEntry;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * This is the CSV exporter class which allows the exporting of data to CSV files in the form of
 * an output stream.
 */
public class CsvExporter {

    /**
     * Export the final accepted entrants on a waitlist to an OutputStream in the CSV file format.
     * @param entries The list of entrants on the waitlist.
     * @param out The created OutputStream.
     * @throws IOException An exception if there is a issue with the file.
     */
    public void exportWaitlist(List<WaitlistEntry> entries, OutputStream out) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out))) {

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
