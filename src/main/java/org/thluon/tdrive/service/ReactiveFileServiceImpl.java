package org.thluon.tdrive.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class ReactiveFileServiceImpl implements ReactiveFileService {
    private final FileSystem fileSystem;
    private final String uploadDirectory;

    @Override
    public Mono<Void> uploadFile(@NonNull FilePart filePart, String fileName) {
        return filePart.transferTo(fileSystem.getPath(uploadDirectory,fileName));
    }

    @Override
    public Mono<Void> deleteFile(@NonNull String fileName) {
        if(fileName.isEmpty()) return Mono.empty();
        return Mono.fromCallable(()->{
            Path p = fileSystem.getPath(uploadDirectory,fileName);
            Files.deleteIfExists(p);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

}
