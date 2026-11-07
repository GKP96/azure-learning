package com.demo.keyvault;

import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobStorageException;

@Service
public class BlobStorageService {

    private final BlobContainerClient containerClient;

    public BlobStorageService(
            @Value("${azure.storage.connection-string}") String connectionString,
            @Value("${azure.storage.container-name}") String containerName) {
        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
        this.containerClient = serviceClient.getBlobContainerClient(containerName);
    }

    public void upload(String blobName, InputStream inputStream, long size, String contentType) {
        containerClient.createIfNotExists();
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        blobClient.upload(inputStream, size, true);
        blobClient.setHttpHeaders(new BlobHttpHeaders()
                .setContentType(contentType == null || contentType.isBlank()
                        ? "application/octet-stream"
                        : contentType));
    }

    public BlobProperties getProperties(String blobName) {
        try {
            return containerClient.getBlobClient(blobName).getProperties();
        } catch (BlobStorageException exception) {
            if (exception.getStatusCode() == 404) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Blob not found", exception);
            }
            throw exception;
        }
    }

    public void download(String blobName, OutputStream outputStream) {
        containerClient.getBlobClient(blobName).downloadStream(outputStream);
    }
}
