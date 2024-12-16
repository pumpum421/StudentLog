package com.example.studentlog;

import javafx.beans.property.*;

import javafx.beans.property.*;
import java.sql.Date;

public class WorkRow {
    private final long id;
    private final String table;
    private final StringProperty type;
    private final StringProperty subject;
    private final BooleanProperty attended;
    private final IntegerProperty grade;
    private final ObjectProperty<Date> date;

    public WorkRow(long id, String table, String type, String subject, Date date, boolean attended, Integer grade) {
        this.id = id;
        this.table = table;
        this.type = new SimpleStringProperty(type);
        this.subject = new SimpleStringProperty(subject);
        this.date = new SimpleObjectProperty<>(date);
        this.attended = new SimpleBooleanProperty(attended);
        this.grade = new SimpleIntegerProperty(grade == null ? 0 : grade);
    }

    public long getId() {
        return id;
    }

    public String getTable() {
        return table;
    }

    public String getType() {
        return type.get();
    }

    public StringProperty typeProperty() {
        return type;
    }

    public String getSubject() {
        return subject.get();
    }

    public StringProperty subjectProperty() {
        return subject;
    }

    public Date getDate() {
        return date.get();
    }

    public ObjectProperty<Date> dateProperty() {
        return date;
    }
    public void setAttended(boolean attended) {
        this.attended.set(attended);
    }

    public void setGrade(int grade) {
        this.grade.set(grade);
    }

    public boolean isAttended() {
        return attended.get();
    }

    public BooleanProperty attendedProperty() {
        return attended;
    }

    public int getGrade() {
        return grade.get();
    }

    public IntegerProperty gradeProperty() {
        return grade;
    }
}
