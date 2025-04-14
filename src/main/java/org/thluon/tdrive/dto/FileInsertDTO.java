package org.thluon.tdrive.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileInsertDTO {
    UUID owner;
    @NotNull(message = "Can't be null")
    @Size(min = 1, max = 255, message = "Size must be between {min} and {max}")
    String name;
    @NotNull(message = "Can't be null")
    UUID parentId;
}
