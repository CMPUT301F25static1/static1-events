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

    /**
     * Creates a new {@link Profile} with all fields initialized.
     *
     * @param uid       unique identifier for the user (usually their auth UID)
     * @param firstName user's first name
     * @param lastName  user's last name
     * @param email     user's email address
     * @param phone     user's phone number (raw string as entered)
     */
    public Profile(String uid, String firstName, String lastName, String email, String phone) {
        this.uid = uid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
    }
    /**
     * Returns the unique identifier for this profile.
     *
     * @return the UID string, or {@code null} if not set
     */
    public String getUid() {
        return uid;
    }
    /**
     * Sets the unique identifier for this profile.
     *
     * @param uid the UID string to associate with this profile
     */
    public void setUid(String uid) {
        this.uid = uid;
    }
    /**
     * Returns the user's first name.
     *
     * @return first name, or {@code null} if not set
     */
    public String getFirstName() {
        return firstName;
    }
    /**
     * Updates the user's first name.
     *
     * @param firstName new first name value
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    /**
     * Returns the user's last name.
     *
     * @return last name, or {@code null} if not set
     */
    public String getLastName() {
        return lastName;
    }
    /**
     * Updates the user's last name.
     *
     * @param lastName new last name value
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    /**
     * Returns the user's email address.
     *
     * @return email address, or {@code null} if not set
     */
    public String getEmail() {
        return email;
    }
    /**
     * Updates the user's email address.
     *
     * @param email new email value
     */
    public void setEmail(String email) {
        this.email = email;
    }
    /**
     * Returns the user's phone number.
     * <p>
     * This is the raw value as stored in Firestore (not formatted).
     *
     * @return phone number string, or {@code null} if not set
     */
    public String getPhone() {
        return phone;
    }
    /**
     * Updates the user's phone number.
     *
     * @param phone new phone number value
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }
    /**
     * Returns the user's full name as "{@code firstName lastName}".
     * <p>
     * Marked with {@link Exclude} so it is not persisted in Firestore; it is a
     * derived convenience property only.
     *
     * @return full name string, or {@code "null null"} if names are not set
     */
    @Exclude
    public String getFullName() {
        return firstName + " " + lastName;
    }
    /**
     * Returns the user's initials in uppercase.
     * <ul>
     *     <li>If both first and last name are present, returns the first letter of each
     *         (e.g., "John Smith" → "JS").</li>
     *     <li>If only one name is present, returns the first letter of that name.</li>
     *     <li>If neither is present, returns an empty string.</li>
     * </ul>
     * This value is derived at runtime and is not stored in Firestore.
     *
     * @return uppercase initials or {@code ""} if no name information is available
     */
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
    /**
     * Returns a nicely formatted version of the user's phone number.
     * <p>
     * Behaviour:
     * <ul>
     *     <li>If {@code phone} is {@code null}, returns an empty string.</li>
     *     <li>All non-digit characters are stripped out.</li>
     *     <li>If exactly 10 digits remain, returns the value formatted as
     *         {@code "(XXX) XXX-XXXX"}.</li>
     *     <li>For any other number of digits, the original {@code phone} value
     *         is returned unchanged.</li>
     * </ul>
     * This is a derived display value and is not stored in Firestore.
     *
     * @return formatted phone number string suitable for display
     */
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
    /**
     * Empty constructor required for Firestore deserialization.
     * <p>
     * Firestore uses the no-arg constructor and then populates the fields via setters
     * or reflection when reading a document into a {@link Profile} instance.
     */
    public Profile() {}
}
