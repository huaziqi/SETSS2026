package com.LHZ.SETSS2026.dto.participaton;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LectureModuleDTO {

    private Integer id;

    private Integer moduleNo;

    private String lecturer;

    private String courseTopic;

    private String introduction;

    private String exactTime;

    private Double price;

    private Boolean selected;
}
