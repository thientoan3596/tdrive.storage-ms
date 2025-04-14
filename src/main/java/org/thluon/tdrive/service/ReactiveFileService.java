package org.thluon.tdrive.service;

import lombok.NonNull;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

import java.nio.file.Paths;

public interface ReactiveFileService {

    /**
     * Uploads a file. (Reactive)
     * Save the filePart as fileName (no extension)
     * @param filePart the file to be uploaded
     * @param fileName the name of the file
     * @return a Mono that completes when the file has been uploaded
     */
    default Mono<Void> uploadFile(@NonNull FilePart filePart, String fileName){
        throw new IllegalStateException("Not yet implemented");
    }


    /**
     * Deletes a file. (Reactive)
     * Does not throw an exception if the file does not exist
     * @param fileName the name of the file to be deleted
     * @return a Mono that completes when the file has been deleted
     */
    default Mono<Void> deleteFile(@NonNull String fileName){throw new IllegalStateException("Not yet implemented");}


    /**
     * Sanitize fileName to remove illegal characters (path traversal, etc.)
     */
    static String sanitizeFileName(String fileName) {
        String name = Paths.get(fileName).getFileName().toString();
        return name.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
    }
}
