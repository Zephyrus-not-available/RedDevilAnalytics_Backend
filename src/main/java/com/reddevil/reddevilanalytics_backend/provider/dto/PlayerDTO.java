package com.reddevil.reddevilanalytics_backend.provider.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDTO {
    private Long id;
    private String name;
    private String position;
    private Integer shirtNumber;
    private String nationality;
    private LocalDate dateOfBirth;
}
