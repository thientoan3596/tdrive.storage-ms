package org.thluon.tdrive.mapper;

import com.fasterxml.uuid.Generators;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.thluon.tdrive.dto.FolderInsertDTO;
import org.thluon.tdrive.dto.StorageItemInsertDTO;
import org.thluon.tdrive.dto.StorageItemResponseDTO;
import org.thluon.tdrive.entity.StorageItem;

import java.util.*;

@Mapper(componentModel = "spring")
public interface StorageItemMapper {
    @Mapping(target = "id",expression = "java(timeBasedUuid())")
    @Mapping(target = "extension",source = "request.name",qualifiedByName = "extractExtension")
    @Mapping(target = "parentId",source = "request.parentId")
    @Mapping(target = "type",source = "request.type")
    @Mapping(target = "name",source = "request.name")
    @Mapping(target = "size",constant = "0L")
    @Mapping(target = "owner",source = "owner")
    StorageItem toStorageItem(StorageItemInsertDTO request, UUID owner);
    @Mapping(target = "id",expression = "java(timeBasedUuid())")
    @Mapping(target = "extension",source = "request.name",qualifiedByName = "extractExtension")
    @Mapping(target = "parentId",source = "request.parentId")
    @Mapping(target = "type",constant = "Folder")
    @Mapping(target = "name",source = "request.name")
    @Mapping(target = "size",constant = "0L")
    @Mapping(target = "owner",source = "owner")
    StorageItem toStorageItem(FolderInsertDTO request, UUID owner);

    default UUID timeBasedUuid() {
        return Generators.timeBasedGenerator().generate();
    }
    @Named("extractExtension")
    default String extractExtension(String name) {
        int lastDot = name.lastIndexOf('.');
        return (lastDot != -1 && lastDot < name.length() - 1) ? name.substring(lastDot + 1) : null;
    }
    @Mapping(target = "children", expression = "java(new java.util.ArrayList<>())")
    StorageItemResponseDTO toStorageItemResponseDTO(StorageItem storageItem);
    default StorageItemResponseDTO toStorageItemResponseDTO(StorageItem root, Map<UUID, List<StorageItem>> childrenMap){
        StorageItemResponseDTO tree = toStorageItemResponseDTO(root);
        Queue<StorageItemResponseDTO> queue = new LinkedList<>();
        queue.add(tree);
        while (!queue.isEmpty()) {
            StorageItemResponseDTO current = queue.poll();
            List<StorageItem> children = childrenMap.getOrDefault(current.getId(), Collections.emptyList());
            children.forEach(child ->{
                StorageItemResponseDTO childNode = toStorageItemResponseDTO(child);
                current.getChildren().add(childNode);
                queue.add(childNode);
            });
        }
        return tree;
    }
}
