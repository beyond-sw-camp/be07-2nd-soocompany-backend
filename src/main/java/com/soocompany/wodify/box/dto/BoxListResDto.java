package com.soocompany.wodify.box.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoxListResDto {
    private String name;
    private String logo;
    private String operatingHours;
    private String address;
}