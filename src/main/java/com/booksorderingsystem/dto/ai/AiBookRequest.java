package com.booksorderingsystem.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiBookRequest {

    @NotBlank(message = "問題不可為空")
    @Size(max = 300, message = "問題不可超過 300 字")
    private String question;
}
