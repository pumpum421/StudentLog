package com.example.studentlog.domain;

import java.sql.Date;

public class PracticalWork {
    private long id;
    private long studentId;
    private Date date;
    private Integer grade;
    private String subjectName;
    private boolean attended;

    public PracticalWork(long id, long studentId, Date date, Integer grade, String subjectName, boolean attended) {
        this.id = id;
        this.studentId = studentId;
        this.date = date;
        this.grade = grade;
        this.subjectName = subjectName;
        this.attended = attended;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getStudentId() {
        return studentId;
    }

    public void setStudentId(long studentId) {
        this.studentId = studentId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public boolean isAttended() {
        return attended;
    }

    public void setAttended(boolean attended) {
        this.attended = attended;
    }
}
