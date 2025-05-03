package org.thluon.tdrive.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.thluon.tdrive.dto.PathEntry;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class ReactiveFileServiceImpl implements ReactiveFileService {
    private final FileSystem fileSystem;
    private final String uploadDirectory;

    @Override
    public Mono<Void> uploadFile(@NonNull FilePart filePart, String fileName) {
        return filePart.transferTo(fileSystem.getPath(uploadDirectory, fileName));
    }

    @Override
    public Mono<Void> deleteFile(@NonNull String fileName) {
        if (fileName.isEmpty()) return Mono.empty();
        return Mono.fromCallable(() -> {
            Path p = fileSystem.getPath(uploadDirectory, fileName);
            Files.deleteIfExists(p);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Flux<DataBuffer> streamFile(@NonNull String fileName) {
        Path p = fileSystem.getPath(uploadDirectory, fileName);
        return DataBufferUtils.read(p, new DefaultDataBufferFactory(), 4096);
    }

    @Override
    public Flux<DataBuffer> streamZip(@NonNull List<PathEntry> paths) {
        return DataBufferUtils.readInputStream(
                () -> {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ZipOutputStream zos = new ZipOutputStream(baos);
                    for (PathEntry entry : paths) {
                        if (entry.fileName() == null) {
                            ZipEntry zipEntry = new ZipEntry(entry.path() + "/");
                            zos.putNextEntry(zipEntry);
                            continue;
                        }
                        try {
                            Path p = fileSystem.getPath(uploadDirectory, entry.fileName());
                            BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(p));
                            ZipEntry zipEntry = new ZipEntry(entry.path());
                            zos.putNextEntry(zipEntry);
                            byte[] buffer = new byte[2048];
                            int len;
                            while ((len = bis.read(buffer)) > 0) {
                                zos.write(buffer, 0, len);
                            }
                            zos.closeEntry();
                            bis.close();
                        } catch (Exception ignored) {
                            System.out.println("File "+entry.path()+" under name " + entry.fileName() + " does not exist");
                        }
                    }
                    zos.close();
                    return new ByteArrayInputStream(baos.toByteArray());
                }
                , new DefaultDataBufferFactory(), 4096);
    }
}
