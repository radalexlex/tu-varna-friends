package org.tuvarna.model.dto;


import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("PROFILE")
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        defaultImpl = ProfileDto.class
)
public class ProfileDto {
    private long userId;
    private String urlImage;
    private Long facultyNumber;
    private String name;
    private String surname;
    private String phoneNumber;
    private String specialty;
    private String field;
    private String form;
    private String country;
    private String workplace;
    private String position;

    public ProfileDto(long userId, String urlImage, Long facultyNumber, String name, String surname, String phoneNumber, String specialty, String field, String form, String country, String workplace, String position) {
        this.userId = userId;
        this.urlImage = urlImage;
        this.facultyNumber = facultyNumber;
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;
        this.specialty = specialty;
        this.field = field;
        this.form = form;
        this.country = country;
        this.workplace = workplace;
        this.position = position;
    }

    public ProfileDto() {
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    public Long getFacultyNumber() {
        return facultyNumber;
    }

    public void setFacultyNumber(Long facultyNumber) {
        this.facultyNumber = facultyNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getWorkplace() {
        return workplace;
    }

    public void setWorkplace(String workplace) {
        this.workplace = workplace;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "ProfileDto{" +
                "keyToImage='" + urlImage + '\'' +
                ", facultyNumber=" + facultyNumber +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", specialty='" + specialty + '\'' +
                ", field='" + field + '\'' +
                ", form='" + form + '\'' +
                ", country='" + country + '\'' +
                ", workplace='" + workplace + '\'' +
                ", position='" + position + '\'' +
                '}';
    }
}

