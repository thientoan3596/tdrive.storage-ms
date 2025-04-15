package org.thluon.tdrive.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thluon.tdrive.dto.FolderInsertDTO;
import org.thluon.tdrive.dto.StorageItemResponseDTO;
import org.thluon.tdrive.security.MyPrincipal;
import org.thluon.tdrive.service.StorageItemServiceImpl;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("${application.api.endpoint}/${application.api.version}")
@RequiredArgsConstructor
public class StorageController implements StorageAPI {
    private final StorageItemServiceImpl storageItemService;
    //region CREATE
    @Override
    public Mono<ResponseEntity<StorageItemResponseDTO>> insertFolder(Mono<FolderInsertDTO> requestMono, MyPrincipal myPrincipal) {
        return storageItemService.insertFolder(requestMono,myPrincipal.id())
                .map(ResponseEntity::ok);
    }
    @Override
    public Mono<ResponseEntity<StorageItemResponseDTO>> insertFile(Mono<FilePart> filePartMono, String parent, MyPrincipal myPrincipal) {
        return storageItemService.insertNewFile(filePartMono,UUID.fromString(parent), myPrincipal.id())
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<StorageItemResponseDTO>> insertRoot(MyPrincipal myPrincipal) {
        return storageItemService.insertRoot(myPrincipal.id()).map(ResponseEntity::ok);
    }
    //endregion CREATE

    //region READ

    @Override
    public Mono<ResponseEntity<StorageItemResponseDTO>> findRootFolder(Integer depth, MyPrincipal myPrincipal) {
        if(depth<=0) return storageItemService.getRootFolder(myPrincipal.id()).map(ResponseEntity::ok);
        return storageItemService.getRootFolderWithDepth(myPrincipal.id(), depth).map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<StorageItemResponseDTO>> findById(String id, Integer depth, MyPrincipal myPrincipal) {
        if(depth==0) return storageItemService.findExactById(UUID.fromString(id),myPrincipal.id()).map(ResponseEntity::ok);
        return storageItemService.findDescendants(UUID.fromString(id), myPrincipal.id(), depth).map(ResponseEntity::ok);
    }
    //endregion READ

    //region UPDATE
    //endregion UPDATE

    //region DEL
    @Override
    public Mono<ResponseEntity<Void>> delete(String id, Boolean recursive,MyPrincipal   myPrincipal) {
        return storageItemService.deleteById(UUID.fromString(id), recursive,myPrincipal.id())
                .map(unused -> ResponseEntity.noContent().build());
    }
    //endregion DEL
}
