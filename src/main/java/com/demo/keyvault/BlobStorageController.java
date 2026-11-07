package com.demo.keyvault;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.azure.storage.blob.models.BlobProperties;

@RestController
public class BlobStorageController {

    private final BlobStorageService blobStorageService;

    public BlobStorageController(BlobStorageService blobStorageService) {
        this.blobStorageService = blobStorageService;
    }

    @PostMapping(path = "/api/blobs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String blobName) throws IOException {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file is empty");
        }

        String resolvedBlobName = StringUtils.hasText(blobName) ? blobName : file.getOriginalFilename();
        if (!StringUtils.hasText(resolvedBlobName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A blob name is required");
        }

        try (var inputStream = file.getInputStream()) {
            blobStorageService.upload(
                    resolvedBlobName,
                    inputStream,
                    file.getSize(),
                    file.getContentType());
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("blobName", resolvedBlobName));
    }

    @GetMapping("/api/blobs/{blobName}")
    public ResponseEntity<StreamingResponseBody> download(@PathVariable String blobName) {
        BlobProperties properties = blobStorageService.getProperties(blobName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(blobName).build());
        headers.setContentLength(properties.getBlobSize());
        String contentType = properties.getContentType();
        headers.setContentType(StringUtils.hasText(contentType)
                ? MediaType.parseMediaType(contentType)
                : MediaType.APPLICATION_OCTET_STREAM);

        StreamingResponseBody responseBody = outputStream ->
                blobStorageService.download(blobName, outputStream);
        return new ResponseEntity<>(responseBody, headers, HttpStatus.OK);
    }
}
