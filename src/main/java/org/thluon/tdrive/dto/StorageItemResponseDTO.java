package org.thluon.tdrive.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.thluon.tdrive.entity.EType;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StorageItemResponseDTO {
    UUID id;
    String name;
    EType type;
    String extension;
    Long size;
    UUID parentId;
    List<StorageItemResponseDTO> children;
    UUID owner;
}
