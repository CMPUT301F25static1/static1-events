package com.static1.fishylottery.services;

import com.static1.fishylottery.model.entities.Profile;
import com.static1.fishylottery.model.entities.WaitlistEntry;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class CsvExporterTest {
    @Test
    public void testExportEntrants() throws IOException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date joinDate = new Date();
        Date acceptedDate = new Date();

        Profile profile1 = new Profile();
        profile1.setFirstName("John");
        profile1.setLastName("Doe");
        profile1.setEmail("john.doe@example.com");
        profile1.setPhone("1111111111");

        WaitlistEntry entry1 = new WaitlistEntry();
        entry1.setStatus("accepted");
        entry1.setAcceptedAt(acceptedDate);
        entry1.setJoinedAt(joinDate);
        entry1.setProfile(profile1);

        List<WaitlistEntry> entries = new ArrayList<>();
        entries.add(entry1);

        CsvExporter csvExporter =  new CsvExporter();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        csvExporter.exportWaitlist(entries, outputStream);

        String content = outputStream.toString();

        String joinDateString = formatter.format(joinDate);
        String acceptedDateString = formatter.format(acceptedDate);

        assertTrue(content.contains("First Name,Last Name,Email,Phone,Joined At,Accepted At"));
        assertTrue(content.contains("John,Doe,john.doe@example.com,(111) 111-1111," + joinDateString + "," + acceptedDateString));
    }
}
