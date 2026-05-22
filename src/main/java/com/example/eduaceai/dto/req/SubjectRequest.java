package com.example.eduaceai.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SubjectRequest {

    @NotBlank(message = "Tên môn học không được để trống")
    @Size(max = 150, message = "Tên môn học tối đa 150 ký tự")
    private String name;

    @Size(max = 1000, message = "Mô tả tối đa 1000 ký tự")
    private String description;

    @Pattern(regexp = "^(#[0-9a-fA-F]{6})?$", message = "Màu phải là mã hex dạng #RRGGBB")
    private String color;
}
