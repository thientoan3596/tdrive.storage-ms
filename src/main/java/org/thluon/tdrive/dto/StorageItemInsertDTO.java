package org.thluon.tdrive.dto;

import com.github.thientoan3596.constraint.ValidEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.thluon.tdrive.entity.EType;

import java.util.UUID;
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StorageItemInsertDTO {
    @NotNull(message = "Can't be null")
    @Size(min = 1, max = 255, message = "Size must be between {min} and {max}")
    String name;
    @ValidEnum(enumClass = EType.class, message = "Invalid type. Allowing values: [{allowedValues}]")
    String type;
    @NotNull(message = "Can't be null")
    UUID parentId;
}
