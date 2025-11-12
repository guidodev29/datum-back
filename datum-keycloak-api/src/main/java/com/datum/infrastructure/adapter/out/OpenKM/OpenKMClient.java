package com.datum.infrastructure.adapter.out.openkm;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import java.io.File;

/**
 * REST Client for OpenKM Document Management System
 * Based on OpenKM REST API documentation
 */
@RegisterRestClient(configKey = "openkm-api")
@Path("/services/rest")
public interface OpenKMClient {

    /**
     * Upload a document to OpenKM using the createSimple endpoint
     *
     * @param authorization Basic authentication header
     * @param docPath Target path in OpenKM (e.g., /okm:root/datum/purchases/123/document.pdf)
     * @param file File to upload
     * @return Response from OpenKM
     */
    @POST
    @Path("/document/createSimple")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    Response uploadDocument(
        @HeaderParam("Authorization") String authorization,
        @FormParam("docPath") String docPath,
        @FormParam("content") @PartType(MediaType.APPLICATION_OCTET_STREAM) File file
    );

    /**
     * Download document content from OpenKM
     *
     * @param authorization Basic authentication header
     * @param docId Document UUID or path
     * @return Document binary content
     */
    @GET
    @Path("/document/getContent")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    Response downloadDocument(
        @HeaderParam("Authorization") String authorization,
        @QueryParam("docId") String docId
    );

    /**
     * Delete a document from OpenKM
     *
     * @param authorization Basic authentication header
     * @param docId Document UUID or path
     * @return Response from OpenKM
     */
    @DELETE
    @Path("/document/delete")
    @Produces(MediaType.APPLICATION_JSON)
    Response deleteDocument(
        @HeaderParam("Authorization") String authorization,
        @QueryParam("docId") String docId
    );

    /**
     * Create a folder in OpenKM using JSON body
     *
     * @param authorization Basic authentication header
     * @param folderRequest Folder creation request with path
     * @return Response from OpenKM
     */
    @POST
    @Path("/folder/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response createFolder(
        @HeaderParam("Authorization") String authorization,
        FolderCreateRequest folderRequest
    );

    /**
     * DTO for folder creation request
     */
    class FolderCreateRequest {
        public String path;

        public FolderCreateRequest() {}

        public FolderCreateRequest(String path) {
            this.path = path;
        }
    }
}