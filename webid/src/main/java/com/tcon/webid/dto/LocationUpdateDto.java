package com.tcon.webid.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationUpdateDto {
    private String id; // user or vendor id
    private Double latitude;
    private Double longitude;
    private String timestamp; // optional ISO 8601 timestamp
}
