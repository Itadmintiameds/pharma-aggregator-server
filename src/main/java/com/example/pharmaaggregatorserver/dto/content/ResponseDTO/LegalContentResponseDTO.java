package com.example.pharmaaggregatorserver.dto.content.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LegalContentResponseDTO {

    private String contentKey;
    private String title;
    private String content;
    private Integer version;
    private LocalDateTime updatedAt;
}
