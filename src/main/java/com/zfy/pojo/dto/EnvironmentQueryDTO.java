package com.zfy.pojo.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class EnvironmentQueryDTO {

    private Double east=114.09;
    private Double west=114.03;
    private Double north=22.58;
    private Double south=22.52;
    @Pattern(regexp = "^(auto|real|simulation)$", message = "data_mode只能是auto、real、simulation")
    private String dataMode = "auto";
}
