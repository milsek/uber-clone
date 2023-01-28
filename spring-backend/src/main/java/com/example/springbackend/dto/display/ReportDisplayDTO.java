package com.example.springbackend.dto.display;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class ReportDisplayDTO {
    private List<String> xAxisValues;
    private List<Double> yAxisValues;
    private String xAxisName;
    private String yAxisName;

    private Double sum;
    private Double average;

    public ReportDisplayDTO() {
        this.xAxisValues = new ArrayList<>();
        this.yAxisValues = new ArrayList<>();
    }

    public void addXAxisValue(String x){
        xAxisValues.add(x);
    }
    public void addYAxisValue(Double y){
        yAxisValues.add(y);
    }
}
