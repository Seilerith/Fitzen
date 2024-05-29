package com.example.lifefit.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.Random;

public class UserModel {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Date birthDate;

    public static Calendar calendar = Calendar.getInstance();

    public UserModel() {
    }

    public UserModel(String username, String firstName, String lastName, Date birthDate, String email) {
        this.username = username;

        this.firstName = firstName;

        this.lastName = lastName;

        this.birthDate = birthDate;

        this.email = email;

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    public Date getBirthDate() {
        return this.birthDate;
    }

    public void setBirthDate(int year, int month, int day) {
        calendar.set(year, month, day);
        this.birthDate = calendar.getTime();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "UserModel{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthDate=" + birthDate + '\'' +
                '}';
    }
}
