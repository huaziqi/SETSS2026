package com.LHZ.SETSS2026.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lecture_modules")
public class LectureModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "module_no", nullable = false)
    private Integer moduleNo;

    @Column(name = "lecturer", nullable = false, length = 100)
    private String lecturer;

    @Column(name = "course_topic", nullable = false, length = 200)
    private String courseTopic;

    @Column(name = "introduction", columnDefinition = "TEXT")
    private String introduction;

    @Column(name = "exact_time", length = 200)
    private String exactTime;

    @Column(name = "price")
    private Double price;
}
