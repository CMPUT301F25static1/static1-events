package com.static1.fishylottery.model.entities;

import com.google.firebase.firestore.Exclude;

/**
 * Represents a user in the app that has associated information such as a name, email,
 * phone number. It can belong to a waitlist and each profile in Firestore also has linked
 * notifications that will be sent to them in app.
 */
public class Profile {
    private String uid;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    public Profile(String uid, String firstName, String lastName, String email, String phone) {
        this.uid = uid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Exclude
    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Exclude
    public String getInitials() {
        if (firstName != null && !firstName.isEmpty() && lastName != null && !lastName.isEmpty()) {
            return firstName.substring(0, 1).toUpperCase() + lastName.substring(0, 1).toUpperCase();
        } else if (firstName != null && !firstName.isEmpty()) {
            return firstName.substring(0, 1).toUpperCase();
        } else if (lastName != null && !lastName.isEmpty()) {
            return lastName.substring(0, 1).toUpperCase();
        } else {
            return "";
        }
    }

    @Exclude
    public String getFormattedPhone() {
        if (phone == null) return "";

        // Remove all non-digit characters
        String digits = phone.replaceAll("\\D", "");

        // Check for 10 digits
        if (digits.length() == 10) {
            return String.format("(%s) %s-%s",
                    digits.substring(0, 3),
                    digits.substring(3, 6),
                    digits.substring(6));
        }

        // Return original if not 10 digits
        return phone;
    }

    public Profile() {}
}
