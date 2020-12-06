package io.licensemanager.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class FileDetails {
    private Integer contentLength = 0;
    private String fileName = "";
    private byte[] content;
}
