package org.thluon.tdrive.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Table("storage_item")
public class StorageItem {
    @Id
    UUID id;
    String name;
    EType type;
    String extension;
    Long size;
    @Column("parent_id")
    UUID parentId;
    UUID owner;
}
