package org.thluon.tdrive.service;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.thluon.tdrive.dto.PathEntry;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;

class ReactiveFileServiceImplTest {
    private FileSystem fileSystem;
    private String uploadDirectory;
    private ReactiveFileServiceImpl underTest;

    @BeforeEach
    void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.windows());
        uploadDirectory = "mocked/upload/";
        underTest = new ReactiveFileServiceImpl(fileSystem, uploadDirectory);
    }

    @Nested
    class StreamFile {
        @Test
        void happyPath() throws IOException {
            String fileName = "test.txt";
            Path uploadDirectory_p = fileSystem.getPath(uploadDirectory);
            Files.createDirectories(uploadDirectory_p);
            Path file_p = uploadDirectory_p.resolve(fileName);
            Files.writeString(file_p, "test content");
            Flux<DataBuffer> result = underTest.streamFile(fileName);
            StepVerifier.create(result.map(buf -> {
                        byte[] bytes = new byte[buf.readableByteCount()];
                        buf.read(bytes);
                        DataBufferUtils.release(buf);
                        return new String(bytes, StandardCharsets.UTF_8);
                    }))
                    .expectNext("test content")
                    .verifyComplete();
        }
        @Test
        void streamZip_shouldReturnZippedContent() throws IOException {
            // Arrange
            PathEntry entry2 = new PathEntry("/src/main/java", null); // directory entry
            PathEntry entry1 = new PathEntry("/src/main/resources/file1.txt", "file1.txt");
            List<PathEntry> paths = List.of(entry1, entry2);

            Path uploadDirectory_p = fileSystem.getPath(uploadDirectory);
            Files.createDirectories(uploadDirectory_p);
            Path file_p = uploadDirectory_p.resolve(entry1.fileName());
            Files.writeString(file_p, "test content");
            assertTrue(Files.exists(file_p));

            // Act
            Flux<DataBuffer> result = underTest.streamZip(paths);

            // Assert
            StepVerifier.create(result)
                    .consumeNextWith(dataBuffer -> {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        DataBufferUtils.release(dataBuffer);
                        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes))) {
                            Set<String> zipEntryNames = new HashSet<>();
                            ZipEntry zipEntry;
                            while ((zipEntry = zis.getNextEntry()) != null) {
                                zipEntryNames.add(zipEntry.getName());
                                if ("src/main/resources/file1.txt".equals(zipEntry.getName())) {
                                    byte[] fileContent = zis.readAllBytes();
                                    assertEquals("test content", new String(fileContent, StandardCharsets.UTF_8));
                                }
                            }
                            System.out.println(zipEntryNames);
                            assertTrue(zipEntryNames.contains("/src/main/resources/file1.txt"));
                            assertTrue(zipEntryNames.contains("/src/main/java/"));
                        } catch (IOException e) {
                            fail("Error while reading the zip stream", e);
                        }
                    })
                    .verifyComplete();
        }
    }

}