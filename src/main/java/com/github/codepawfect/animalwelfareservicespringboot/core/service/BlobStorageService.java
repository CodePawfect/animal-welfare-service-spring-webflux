package com.github.codepawfect.animalwelfareservicespringboot.core.service;

import com.azure.storage.blob.BlobServiceClient;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BlobStorageService {

  private final BlobServiceClient blobServiceClient;

  /**
   * Upload files to blob storage
   *
   * @param containerName container name
   * @param blobName blob name
   * @param data data
   */
  public void uploadToBlob(String containerName, String blobName, InputStream data)
      throws IOException {
    var blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
    var blobClient = blobContainerClient.getBlobClient(blobName);
    blobClient.upload(data, data.available(), true);
  }

  /**
   * Read from blob storage
   *
   * @param containerName container name
   * @param blobName blob name
   * @return data
   */
  public byte[] readFromBlob(String containerName, String blobName) throws IOException {
    var blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
    var blobClient = blobContainerClient.getBlobClient(blobName);

    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      blobClient.downloadStream(outputStream);
      return outputStream.toByteArray();
    }
  }

  /**
   * Delete from blob storage
   *
   * @param containerName container name
   * @param blobName blob name
   */
  public void deleteFromBlob(String containerName, String blobName) {
    var blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
    var blobClient = blobContainerClient.getBlobClient(blobName);
    blobClient.delete();
  }
}
