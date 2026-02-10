package com.reddevil.reddevilanalytics_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetResponse {
    private String logoUrl;
    private String bannerUrl;
    private String photoUrl;
    private String cutoutUrl;
}
