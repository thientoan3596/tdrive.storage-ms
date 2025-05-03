package org.thluon.tdrive.controller;

import com.github.thientoan3596.exception.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.thluon.tdrive.config.SecurityConfig;
import org.thluon.tdrive.dto.FolderInsertDTO;
import org.thluon.tdrive.dto.StorageItemResponseDTO;
import org.thluon.tdrive.entity.EType;
import org.thluon.tdrive.entity.StorageItem;
import org.thluon.tdrive.security.MyPrincipal;
import org.thluon.tdrive.service.StorageItemServiceImpl;
import org.thluon.tdrive.utilities.ValidationMessageExtractor;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

@WebFluxTest(controllers = StorageController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "SPRINGDOC_SECURE=false",
})
class StorageControllerTest {
    @MockBean
    private StorageItemServiceImpl storageItemService;
    @Autowired
    private WebTestClient webTestClient;
    private final MyPrincipal myPrincipal = new MyPrincipal(UUID.randomUUID(), "testUser", "User");

    @Nested
    class Create {
        @Nested
        class InsertFolder {
            @Test
            void happyPath() {
                var request = FolderInsertDTO.builder()
                        .name("test")
                        .parentId(UUID.randomUUID())
                        .build();
                var response = StorageItemResponseDTO.builder()
                        .id(UUID.randomUUID())
                        .children(null)
                        .extension(null)
                        .name(request.getName())
                        .type(EType.Folder)
                        .parentId(request.getParentId())
                        .owner(myPrincipal.id())
                        .build();
                Mockito.when(storageItemService.insertFolder(Mockito.any(), Mockito.any())).thenReturn(Mono.just(response));
                webTestClient.post()
                        .uri("/folder")
                        .header("X-User-Id", myPrincipal.id().toString())
                        .header("X-User-Name", myPrincipal.name())
                        .header("X-User-Role", myPrincipal.role())
                        .bodyValue(request)
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody()
                        .jsonPath("$.id").isEqualTo(response.getId().toString())
                        .jsonPath("$.name").isEqualTo(response.getName())
                        .jsonPath("$.parentId").isEqualTo(response.getParentId().toString())
                        .jsonPath("$.type").isEqualTo("Folder")
                        .jsonPath("$.owner").isEqualTo(myPrincipal.id().toString());
                Mockito.verify(storageItemService, Mockito.times(1)).insertFolder(any(), any());
            }

            @Nested
            class Validation {
                @Test
                void nullName() {
                    var request = FolderInsertDTO.builder()
                            .parentId(UUID.randomUUID())
                            .build();
                    webTestClient.post()
                            .uri("/folder")
                            .header("X-User-Id", myPrincipal.id().toString())
                            .header("X-User-Name", myPrincipal.name())
                            .header("X-User-Role", myPrincipal.role())
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(request)
                            .exchange()
                            .expectStatus().isBadRequest()
                            .expectBody()
                            .jsonPath("$.errors[0].code").isEqualTo("NotNull")
                            .jsonPath("$.errors[0].field").isEqualTo("name")
                            .jsonPath("$.errors[0].defaultMessage").isEqualTo(ValidationMessageExtractor.extractMessage(FolderInsertDTO.class, "name", NotNull.class));
                    Mockito.verify(storageItemService, Mockito.never()).insertFolder(any(), any());
                }

                @Test
                void shortName() {
                    var request = FolderInsertDTO.builder()
                            .name("")
                            .parentId(UUID.randomUUID())
                            .build();
                    webTestClient.post()
                            .uri("/folder")
                            .header("X-User-Id", myPrincipal.id().toString())
                            .header("X-User-Name", myPrincipal.name())
                            .header("X-User-Role", myPrincipal.role())
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(request)
                            .exchange()
                            .expectStatus().isBadRequest()
                            .expectBody()
                            .jsonPath("$.errors[0].code").isEqualTo("Size")
                            .jsonPath("$.errors[0].field").isEqualTo("name")
                            .jsonPath("$.errors[0].defaultMessage").isEqualTo(ValidationMessageExtractor.extractMessage(FolderInsertDTO.class, "name", Size.class));
                    Mockito.verify(storageItemService, Mockito.never()).insertFolder(any(), any());
                }

                @Test
                void longName() {

                    var request = FolderInsertDTO.builder()
                            .name("1l".repeat(256))
                            .parentId(UUID.randomUUID())
                            .build();
                    webTestClient.post()
                            .uri("/folder")
                            .header("X-User-Id", myPrincipal.id().toString())
                            .header("X-User-Name", myPrincipal.name())
                            .header("X-User-Role", myPrincipal.role())
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(request)
                            .exchange()
                            .expectStatus().isBadRequest()
                            .expectBody()
                            .jsonPath("$.errors[0].code").isEqualTo("Size")
                            .jsonPath("$.errors[0].field").isEqualTo("name")
                            .jsonPath("$.errors[0].defaultMessage").isEqualTo(ValidationMessageExtractor.extractMessage(FolderInsertDTO.class, "name", Size.class));
                    Mockito.verify(storageItemService, Mockito.never()).insertFolder(any(), any());
                }

                @Test
                void nullParentId() {
                    var request = FolderInsertDTO.builder()
                            .name("Valid name")
                            .build();
                    webTestClient.post()
                            .uri("/folder")
                            .header("X-User-Id", myPrincipal.id().toString())
                            .header("X-User-Name", myPrincipal.name())
                            .header("X-User-Role", myPrincipal.role())
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(request)
                            .exchange()
                            .expectStatus().isBadRequest()
                            .expectBody()
                            .jsonPath("$.errors[0].code").isEqualTo("NotNull")
                            .jsonPath("$.errors[0].field").isEqualTo("parentId")
                            .jsonPath("$.errors[0].defaultMessage").isEqualTo(ValidationMessageExtractor.extractMessage(FolderInsertDTO.class, "parentId", NotNull.class));
                    Mockito.verify(storageItemService, Mockito.never()).insertFolder(any(), any());
                }
            }

            @Test
            void notFoundParentId() {
                var request = FolderInsertDTO.builder()
                        .name("test")
                        .parentId(UUID.randomUUID())
                        .build();
                var ex = new EntityNotFoundException(
                        "No file or folder with id [" + request.getParentId() + "]",
                        "id",
                        StorageItem.class.getName(),
                        request.getParentId().toString());
                Mockito.when(storageItemService.insertFolder(Mockito.any(), Mockito.any())).thenReturn(Mono.error(ex));
                webTestClient.post()
                        .uri("/folder")
                        .header("X-User-Id", myPrincipal.id().toString())
                        .header("X-User-Name", myPrincipal.name())
                        .header("X-User-Role", myPrincipal.role())
                        .bodyValue(request)
                        .exchange()
                        .expectStatus().isNotFound()
                        .expectBody()
                        .jsonPath("$.status").isEqualTo("NOT_FOUND")
                        .jsonPath("$.errors[0].defaultMessage").isEqualTo(ex.getMessage());
                Mockito.verify(storageItemService, Mockito.times(1)).insertFolder(any(), any());
            }
        }

        @Nested
        class InsertNewFile {
            record MyFile(String name, String extension, byte[] content) {
            }

            private MultipartBodyBuilder buildMultipartBody(MyFile myFile, UUID parentId) {
                MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
                multipartBodyBuilder.part("file", new ByteArrayResource(myFile.content))
                        .header("Content-Disposition", "form-data; name=file; filename=" + myFile.name + "." + myFile.extension)
                        .contentType(MediaType.APPLICATION_OCTET_STREAM);
                if (parentId != null)
                    multipartBodyBuilder.part("parent", parentId.toString());
                return multipartBodyBuilder;
            }

            @Test
            void happyPath() {
                final var parentId = UUID.randomUUID();
                final MyFile myFile = new MyFile("test", "txt", "test-content".getBytes());
                MultipartBodyBuilder multipartBodyBuilder = buildMultipartBody(myFile, parentId);
                StorageItemResponseDTO response = StorageItemResponseDTO.builder()
                        .id(UUID.randomUUID())
                        .children(null)
                        .extension(myFile.extension)
                        .name(myFile.name)
                        .type(EType.File)
                        .parentId(parentId)
                        .owner(myPrincipal.id())
                        .build();
                Mockito.when(storageItemService.insertNewFile(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.just(response));
                webTestClient.post()
                        .uri("/file")
                        .header("X-User-Id", myPrincipal.id().toString())
                        .header("X-User-Name", myPrincipal.name())
                        .header("X-User-Role", myPrincipal.role())
                        .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody()
                        .jsonPath("$.id").isEqualTo(response.getId().toString())
                        .jsonPath("$.name").isEqualTo(response.getName())
                        .jsonPath("$.parentId").isEqualTo(response.getParentId().toString())
                        .jsonPath("$.type").isEqualTo("File")
                        .jsonPath("$.extension").isEqualTo(response.getExtension())
                        .jsonPath("$.owner").isEqualTo(myPrincipal.id().toString());
                Mockito.verify(storageItemService, Mockito.times(1)).insertNewFile(any(), any(), any());
//                Mockito.verify(storageItemService, Mockito.never()).insertNewFile(any(),any(), any());
            }

            @Nested
            class Validation {
                @Test
                void invalidParentId() {
                    final var parentId = "invalid id";
                    final MyFile myFile = new MyFile("test", "txt", "test-content".getBytes());
                    MultipartBodyBuilder multipartBodyBuilder = buildMultipartBody(myFile, null);
                    multipartBodyBuilder.part("parent", parentId);
                    webTestClient.post()
                            .uri("/file")
                            .header("X-User-Id", myPrincipal.id().toString())
                            .header("X-User-Name", myPrincipal.name())
                            .header("X-User-Role", myPrincipal.role())
                            .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                            .exchange()
                            .expectStatus().isBadRequest()
                            .expectBody()
                            .jsonPath("$.errors[0].field").isEqualTo("parent")
                            .jsonPath("$.errors[0].defaultMessage").isEqualTo("Invalid UUID format")
                            .jsonPath("$.errors[0].rejectedValue").isEqualTo(parentId);
                    Mockito.verify(storageItemService, Mockito.never()).insertNewFile(any(), any(), any());
                }

                @Test
                void missingParentId() {
                    final MyFile myFile = new MyFile("test", "txt", "test-content".getBytes());
                    MultipartBodyBuilder multipartBodyBuilder = buildMultipartBody(myFile, null);
                    webTestClient.post()
                            .uri("/file")
                            .header("X-User-Id", myPrincipal.id().toString())
                            .header("X-User-Name", myPrincipal.name())
                            .header("X-User-Role", myPrincipal.role())
                            .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                            .exchange()
                            .expectStatus().isBadRequest()
                            .expectBody()
                            .jsonPath("$.errors[0].field").isEqualTo("parent");
                    Mockito.verify(storageItemService, Mockito.never()).insertNewFile(any(), any(), any());
                }

                @Test
                void missingFile() {
                    final var parentId = UUID.randomUUID();
                    MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
                    multipartBodyBuilder.part("parent", parentId.toString());
                    webTestClient.post()
                            .uri("/file")
                            .header("X-User-Id", myPrincipal.id().toString())
                            .header("X-User-Name", myPrincipal.name())
                            .header("X-User-Role", myPrincipal.role())
                            .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                            .exchange()
                            .expectStatus().isBadRequest()
                            .expectBody()
                            .jsonPath("$.errors[0].field").isEqualTo("file");
                    Mockito.verify(storageItemService, Mockito.never()).insertNewFile(any(), any(), any());
                }

                @Test
                void parentNotFound() {
                    final var parentId = UUID.randomUUID();
                    final MyFile myFile = new MyFile("test", "txt", "test-content".getBytes());
                    MultipartBodyBuilder multipartBodyBuilder = buildMultipartBody(myFile, parentId);
                    var ex = new EntityNotFoundException("No file or folder with id [" + parentId + "]", "id", StorageItem.class.getName(), parentId.toString());
                    Mockito.when(storageItemService.insertNewFile(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.error(ex));
                    webTestClient.post()
                            .uri("/file")
                            .header("X-User-Id", myPrincipal.id().toString())
                            .header("X-User-Name", myPrincipal.name())
                            .header("X-User-Role", myPrincipal.role())
                            .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                            .exchange()
                            .expectStatus().isNotFound()
                            .expectBody()
                            .jsonPath("$.errors[0].rejectedValue").isEqualTo(parentId.toString());
                    Mockito.verify(storageItemService, Mockito.times(1)).insertNewFile(any(), any(), any());
                }
            }
        }

        @Nested
        class InsertRoot {
            @Test
            void happyPath() {
                var response = StorageItemResponseDTO.builder()
                        .owner(myPrincipal.id())
                        .parentId(null)
                        .id(UUID.randomUUID())
                        .name("root")
                        .type(EType.Folder)
                        .build();
                Mockito.when(storageItemService.insertRoot(myPrincipal.id())).thenReturn(Mono.just(response));
                webTestClient.post()
                        .uri("/root")
                        .header("X-User-Id", myPrincipal.id().toString())
                        .header("X-User-Name", myPrincipal.name())
                        .header("X-User-Role", myPrincipal.role())
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody()
                        .jsonPath("$.id").isEqualTo(response.getId().toString())
                        .jsonPath("$.name").isEqualTo(response.getName())
                        .jsonPath("$.type").isEqualTo("Folder")
                        .jsonPath("$.owner").isEqualTo(myPrincipal.id().toString())
                        .jsonPath("$.parentId").doesNotExist();
                Mockito.verify(storageItemService, Mockito.times(1)).insertRoot(myPrincipal.id());
            }
        }
    }
}