package org.thluon.tdrive.controller;

import com.github.thientoan3596.dto.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.thluon.tdrive.dto.FolderInsertDTO;
import org.thluon.tdrive.dto.StorageItemInsertDTO;
import org.thluon.tdrive.dto.StorageItemResponseDTO;
import org.thluon.tdrive.security.MyPrincipal;
import reactor.core.publisher.Mono;

@Tag(name = "Storage API")
@ApiResponses(value = {@ApiResponse(responseCode = "500", description = "Internal Server Error")})
public interface StorageAPI {
    //region CREATE
    //region POST /folder
    @Operation(summary = "Thêm folder")
    //region @Body
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true, content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = StorageItemInsertDTO.class)))
    //endregion  @Body
    @ApiResponse(responseCode = "201", description = "Thêm thành công",
            content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = FolderInsertDTO.class)))
    @ApiResponse(responseCode = "403", description = "Bad request",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ErrorResponseDTO.class))))
    @PostMapping("/folder")
    default Mono<ResponseEntity<StorageItemResponseDTO>> insertFolder(
            @Valid @RequestBody Mono<FolderInsertDTO> requestMono,
            @AuthenticationPrincipal MyPrincipal myPrincipal
    ) {
        throw new IllegalStateException("Method not implemented");
    }
    //endregion POST /folder
    //region POST /file
    @Operation(summary = "Insert file")
    @ApiResponse(responseCode = "201", description = "Insert file successfully", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = StorageItemResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Bad request",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ErrorResponseDTO.class))))
    @ApiResponse(responseCode = "404", description = "Not found",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ErrorResponseDTO.class))))
    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    default Mono<ResponseEntity<StorageItemResponseDTO>> insertFile(
            @Parameter(description = "File to upload", content = @Content(mediaType = "application/octet-stream"), required = true)
            @RequestPart(value = "file")
            Mono<FilePart> filePartMono,
            @RequestPart(value = "parent")
            @Parameter(description = "Parent directory for the file", required = true)
            @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$", message = "Invalid UUID format")
            @NotNull(message = "Can't be null")
            String parent,
            @AuthenticationPrincipal MyPrincipal myPrincipal
    ) {
        throw new IllegalStateException("Method not implemented");
    }
    //endregion  POST /file
    //region POST /root
    @Operation(summary = "Tạo folder root")
    @ApiResponse(responseCode = "201", description = "Success",
            content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = StorageItemResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Bad request",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ErrorResponseDTO.class))))
    @PostMapping("/root")
    default Mono<ResponseEntity<StorageItemResponseDTO>> insertRoot(@AuthenticationPrincipal MyPrincipal myPrincipal) {
        throw new IllegalStateException("Method not implemented");
    }

    //endregion
    //endregion CREATE

    //region READ
    //region GET /root?[depth=0]
    @Operation(summary = "lấy root")
    @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công",
            content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = StorageItemResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Not found",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ErrorResponseDTO.class))))
    @GetMapping("/root")
    default Mono<ResponseEntity<StorageItemResponseDTO>> findRootFolder(
            @Parameter(name = "depth", description = "depth", schema = @Schema(type = "integer", example = "1", defaultValue = "1"))
            @RequestParam(value = "depth", defaultValue = "1") Integer depth,
            @AuthenticationPrincipal MyPrincipal myPrincipal
    ) {
        throw new IllegalStateException("Method not implemented");
    }
    //endregion READ /{id}?[depth=1]
    //region GET /{id}?[depth=1]
    @Operation(summary = "Tìm file/folder theo id")
    @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công",
            content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = StorageItemResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Not found",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ErrorResponseDTO.class))))
    @GetMapping("/{id}")
    default Mono<ResponseEntity<StorageItemResponseDTO>> findById(
            @Parameter(name = "id", description = "id", schema = @Schema(type = "integer", example = "1"), in = ParameterIn.PATH, required = true)
            @PathVariable String id,
            @Parameter(name = "depth", description = "depth", schema = @Schema(type = "integer", example = "1", defaultValue = "1"))
            @RequestParam(value = "depth", defaultValue = "1") Integer depth,
            @AuthenticationPrincipal MyPrincipal myPrincipal
    ) {
        throw new IllegalStateException("Method not implemented");
    }
    //endregion READ /{id}?[depth=1]
    //endregion READ

    //region UPDATE
    //endregion UPDATE

    //region DEL
    //region DELETE /{id}?[recursive=false]
    @Operation(summary = "Xóa file/folder")
    @ApiResponse(responseCode = "204", description = "Xóa thanh công")
    @ApiResponse(responseCode = "403", description = "Bad request",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ErrorResponseDTO.class))))
    @ApiResponse(responseCode = "404", description = "Not found",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ErrorResponseDTO.class))))
    @DeleteMapping("/{id}")
    default Mono<ResponseEntity<Void>> delete(
            @PathVariable String id,
            @Parameter(name = "recursive", description = "if recursive, then delete all children, if not then might fail if there are children", schema = @Schema(type = "boolean", example = "false", defaultValue = "false"))
            @RequestParam(value = "recursive", defaultValue = "false") Boolean recursive,
            @AuthenticationPrincipal MyPrincipal myPrincipal
    ) {
        throw new IllegalStateException("Method not implemented");
    }
    //endregion DELETE /{id}?[recursive=false]
    //endregion DEL
}
