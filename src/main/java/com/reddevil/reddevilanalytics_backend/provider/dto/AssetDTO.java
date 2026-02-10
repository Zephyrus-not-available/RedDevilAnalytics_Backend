package com.reddevil.reddevilanalytics_backend.provider.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetDTO {
    private String logoUrl;
    private String bannerUrl;
    private String photoUrl;
    private String cutoutUrl;
}
