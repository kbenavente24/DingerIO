package com.kobe.dinger.DTOs;

import java.util.List;

public class ScheduleResponseDTO {
    private List<DateDTO> dates;

    public List<DateDTO> getDates() { return dates; }
    public void setDates(List<DateDTO> dates) { this.dates = dates; }
}