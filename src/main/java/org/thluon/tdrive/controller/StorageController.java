package org.thluon.tdrive.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.RestController;
import org.thluon.tdrive.dto.FolderInsertDTO;
import org.thluon.tdrive.dto.StorageItemResponseDTO;
import org.thluon.tdrive.entity.EType;
import org.thluon.tdrive.security.MyPrincipal;
import org.thluon.tdrive.service.ReactiveFileService;
import org.thluon.tdrive.service.StorageService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class StorageController implements StorageAPI {
  private final StorageService storageItemService;
  private final ReactiveFileService reactiveFileService;

  // region CREATE
  @Override
  public Mono<ResponseEntity<StorageItemResponseDTO>> insertFolder(
          Mono<FolderInsertDTO> requestMono, MyPrincipal myPrincipal) {
    return requestMono.flatMap(request->storageItemService.insertFolder(request, myPrincipal.id())).map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<StorageItemResponseDTO>> insertFile(
      Mono<FilePart> filePartMono, String parent, MyPrincipal myPrincipal) {
    return filePartMono
            .flatMap(filePart -> storageItemService.insertNewFile(filePart, UUID.fromString(parent), myPrincipal.id()))
            .map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<StorageItemResponseDTO>> insertRoot(MyPrincipal myPrincipal) {
    return storageItemService.insertRoot(myPrincipal.id()).map(ResponseEntity::ok);
  }

  // endregion CREATE

  // region READ


  @Override
  public Mono<ResponseEntity<Flux<DataBuffer>>> downloadItem(String id, MyPrincipal myPrincipal) {
    return storageItemService
            .findExactById(UUID.fromString(id), myPrincipal.id())
            .flatMap(i->{
              if(i.getType() == EType.File)
                return Mono.just(
                        ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+i.getName()+(i.getExtension()!=null?"."+i.getExtension():"" )+"\"")
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .body(reactiveFileService.streamFile(i.getId().toString())));
              return Mono.just(
                      ResponseEntity.ok()
                              .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+i.getName()+".zip\"")
                              .contentType(MediaType.APPLICATION_OCTET_STREAM)
                              .body(storageItemService.streamItem(i.getId(),myPrincipal.id())));
            });
  }

  @Override
  public Mono<ResponseEntity<StorageItemResponseDTO>> findRootFolder(
      Integer depth, MyPrincipal myPrincipal) {
    if (depth <= 0)
      return storageItemService.getRootFolder(myPrincipal.id()).map(ResponseEntity::ok);
    return storageItemService
        .getRootFolderWithDepth(myPrincipal.id(), depth)
        .map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<StorageItemResponseDTO>> findById(
      String id, Integer depth, MyPrincipal myPrincipal) {
    if (depth == 0)
      return storageItemService
          .findExactById(UUID.fromString(id), myPrincipal.id())
          .map(ResponseEntity::ok);
    return storageItemService
        .findDescendants(UUID.fromString(id), myPrincipal.id(), depth)
        .map(ResponseEntity::ok);
  }

  // endregion READ

  // region UPDATE
  // endregion UPDATE

  // region DEL
  @Override
  public Mono<ResponseEntity<Void>> delete(String id, Boolean recursive, MyPrincipal myPrincipal) {
    return storageItemService
        .deleteById(UUID.fromString(id), recursive, myPrincipal.id())
        .map(unused -> ResponseEntity.noContent().build());
  }
  // endregion DEL
}
