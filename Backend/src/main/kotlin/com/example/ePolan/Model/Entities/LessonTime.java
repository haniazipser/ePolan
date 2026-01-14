package com.example.ePolan.Model.Entities;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.util.Objects;

@Embeddable
@Getter @Setter
public class LessonTime {
    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;
    public LessonTime(){};

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LessonTime that = (LessonTime) o;
        return dayOfWeek == that.dayOfWeek ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dayOfWeek);
    }

}