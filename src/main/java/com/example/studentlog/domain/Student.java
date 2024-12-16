package com.example.studentlog.domain;

public class Student {
    private long id;
    private String name;
    private String surname;
    private String patronymic;
    private String groupName;

    public Student(long id, String name, String surname, String patronymic, String groupName) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.patronymic = patronymic;
        this.groupName = groupName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getPatronymic() {
        return patronymic;
    }

    public void setPatronymic(String patronymic) {
        this.patronymic = patronymic;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public String toString() {
        return String.format(
                "Student{id=%d, name='%s', surname='%s', patronymic='%s', groupName='%s'}",
                id, name, surname, patronymic, groupName
        );
    }
}
