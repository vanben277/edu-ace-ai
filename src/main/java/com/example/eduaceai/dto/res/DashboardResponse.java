package com.example.eduaceai.dto.res;

import lombok.*;
import java.util.List;

@Data
@Builder
public class DashboardResponse {
    private long totalDocuments;
    private long totalQuizzesTaken;
    private double averageScore;
    private List<ChartData> progressChart;
    public record ChartData(String date, Double score) {}
}