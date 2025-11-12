package com.datum.infrastructure.adapter.out.openkm;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Service to interact with OpenKM Document Management System
 * Handles authentication and orchestrates document operations
 */
@ApplicationScoped
public class OpenKMService {

    @Inject
    @RestClient
    OpenKMClient openKMClient;

    @ConfigProperty(name = "openkm.username")
    String openkmUsername;

    @ConfigProperty(name = "openkm.password")
    String openkmPassword;

    @ConfigProperty(name = "openkm.base.path")
    String basePath;

    /**
     * Generate Basic Authentication header for OpenKM
     */
    private String getBasicAuthHeader() {
        String credentials = openkmUsername + ":" + openkmPassword;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    /**
     * Create a folder in OpenKM if it doesn't exist
     * If folder already exists, the error is ignored
     *
     * @param folderPath Full folder path in OpenKM
     */
    private void createFolderIfNotExists(String folderPath) {
        try {
            String authHeader = getBasicAuthHeader();
            OpenKMClient.FolderCreateRequest request = new OpenKMClient.FolderCreateRequest(folderPath);

            Response response = openKMClient.createFolder(authHeader, request);

            // 200 = created successfully, ignore other responses (folder might already exist)
            if (response.getStatus() != 200 && response.getStatus() != 201) {
                System.out.println("Folder might already exist: " + folderPath);
            }
        } catch (Exception e) {
            // Ignore errors - folder might already exist
            System.out.println("Folder creation skipped (might exist): " + folderPath);
        }
    }

    /**
     * Upload a document to OpenKM with hierarchical folder structure
     *
     * @param purchaseId ID of the purchase
     * @param purchaseDate Date of the purchase (to extract year/month)
     * @param fileName Name of the file
     * @param fileUpload File to upload
     * @return Full document path in OpenKM
     * @throws RuntimeException if upload fails
     */
    public String uploadDocument(Long purchaseId, java.time.LocalDateTime purchaseDate, String fileName, FileUpload fileUpload) {
        try {
            String authHeader = getBasicAuthHeader();

            // Extract year and month from purchase date
            String year = String.valueOf(purchaseDate.getYear());
            String month = String.format("%02d", purchaseDate.getMonthValue()); // Format: 01, 02, ..., 12

            // Construct hierarchical path: /okm:root/datum/employee/purchase/{year}/{month}/{id}/
            String yearPath = basePath + "/" + year;
            String monthPath = yearPath + "/" + month;
            String idPath = monthPath + "/" + purchaseId;

            // Create folder hierarchy if not exists
            createFolderIfNotExists(yearPath);
            createFolderIfNotExists(monthPath);
            createFolderIfNotExists(idPath);

            // Full document path
            String docPath = idPath + "/" + fileName;

            // Convert FileUpload to File
            java.io.File file = fileUpload.uploadedFile().toFile();

            Response response = openKMClient.uploadDocument(authHeader, docPath, file);

            if (response.getStatus() == 201 || response.getStatus() == 200) {
                return docPath;
            } else {
                throw new RuntimeException("Failed to upload document to OpenKM. Status: " + response.getStatus());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error uploading document to OpenKM: " + e.getMessage(), e);
        }
    }

    /**
     * Download a document from OpenKM
     *
     * @param docPath Document path in OpenKM
     * @return Binary content of the document
     * @throws RuntimeException if download fails
     */
    public byte[] downloadDocument(String docPath) {
        try {
            String authHeader = getBasicAuthHeader();

            Response response = openKMClient.downloadDocument(authHeader, docPath);

            if (response.getStatus() == 200) {
                return response.readEntity(byte[].class);
            } else {
                throw new RuntimeException("Failed to download document from OpenKM. Status: " + response.getStatus());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error downloading document from OpenKM: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a document from OpenKM
     *
     * @param docPath Document path in OpenKM
     * @throws RuntimeException if deletion fails
     */
    public void deleteDocument(String docPath) {
        try {
            String authHeader = getBasicAuthHeader();

            Response response = openKMClient.deleteDocument(authHeader, docPath);

            if (response.getStatus() != 200 && response.getStatus() != 204) {
                throw new RuntimeException("Failed to delete document from OpenKM. Status: " + response.getStatus());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error deleting document from OpenKM: " + e.getMessage(), e);
        }
    }

    /**
     * Update a document in OpenKM (delete old and upload new)
     *
     * @param oldDocPath Old document path in OpenKM (to delete)
     * @param purchaseId ID of the purchase
     * @param purchaseDate Date of the purchase (to extract year/month)
     * @param fileName Name of the new file
     * @param fileUpload New file to upload
     * @return Full document path in OpenKM of the new file
     * @throws RuntimeException if update fails
     */
    public String updateDocument(String oldDocPath, Long purchaseId, java.time.LocalDateTime purchaseDate, String fileName, FileUpload fileUpload) {
        try {
            // 1. Delete old document if exists
            if (oldDocPath != null && !oldDocPath.isEmpty()) {
                try {
                    deleteDocument(oldDocPath);
                } catch (Exception e) {
                    // Log but continue - old document might already be deleted
                    System.err.println("Warning: Could not delete old document: " + e.getMessage());
                }
            }

            // 2. Upload new document
            return uploadDocument(purchaseId, purchaseDate, fileName, fileUpload);

        } catch (Exception e) {
            throw new RuntimeException("Error updating document in OpenKM: " + e.getMessage(), e);
        }
    }

}