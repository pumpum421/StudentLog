package com.example.studentlog.domain;

import java.sql.Date;

public class Lecture {
    private long id;
    private long studentId;
    private Date date;
    private String subjectName;
    private boolean attended;

    public Lecture(long id, long studentId, Date date, String subjectName, boolean attended) {
        this.id = id;
        this.studentId = studentId;
        this.date = date;
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
