package com.smit.wheeltime.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class TireChangeTime {
    private String id;
    private String time;
    @Setter
    @Getter
    private boolean available;

}
