package com.example.eduaceai.dto.req;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "Mã sinh viên không được để trống")
        String studentCode,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, max = 15, message = "Mật khẩu phải từ 6 đến 15 ký tự")
        String password

) {
}
