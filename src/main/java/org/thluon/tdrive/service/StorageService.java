package org.thluon.tdrive.service;

import com.github.thientoan3596.exception.EntityNotFoundException;
import lombok.NonNull;
import org.springframework.http.codec.multipart.FilePart;
import org.thluon.tdrive.dto.FolderInsertDTO;
import org.thluon.tdrive.dto.StorageItemResponseDTO;
import org.thluon.tdrive.exception.RootFolderExistedForUser;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface StorageService {
    /**
     * Inserts a new file into storage.
     * Create metadata for the file and save to database
     * Save file to storage
     *
     * @param filePartMono the Mono containing the FilePart to be inserted
     * @param parentId     the parent id of the file
     * @param userId       the user id of the owner
     * @return a Mono containing the inserted StorageItem
     * @throws EntityNotFoundException if the parent id is not found (or is not owner)
     */
    default Mono<StorageItemResponseDTO> insertNewFile(
            @NonNull FilePart filePartMono,
            @NonNull UUID parentId,
            @NonNull UUID userId
    )
    throws EntityNotFoundException {
        throw new IllegalStateException("Not yet implemented");
    }

    /**
     * Inserts a folder into storage.
     * Create metadata for the folder and save to database
     *
     * @param request the Mono containing the FolderInsertDTO to be inserted
     * @return a Mono containing the inserted StorageItemResponseDTO (depth 0)
     * @throws EntityNotFoundException if the parent id is not found (or is not owner)
     */
    default Mono<StorageItemResponseDTO> insertFolder(@NonNull FolderInsertDTO request,@NonNull UUID userId) throws EntityNotFoundException {
        throw new IllegalStateException("Not yet implemented");
    }

    /**
     * Insert root folder.
     * This should only be called once for each user.
     * @param user user
     * @return root folder
     * @throws RootFolderExistedForUser if root folder existed
     */
    default Mono<StorageItemResponseDTO> insertRoot(@NonNull UUID user) throws RootFolderExistedForUser {
        throw new IllegalStateException("Not yet implemented");
    }

    /**
     * Getting root folder of user.
     * @param user user
     * @apiNote if root folder does not exist, will create it
     * @return root folder
     */
    default Mono<StorageItemResponseDTO> getRootFolder(@NonNull UUID user){
        throw new IllegalStateException("Not yet implemented");
    }

    default Mono<StorageItemResponseDTO> getRootFolderWithDepth(@NonNull UUID user, Integer depth){
        throw new IllegalStateException("Not yet implemented");
    }
    /**
     * Retrieves the storage item with the specified id and its children up to the given depth.
     * The depth is zero-indexed, so a depth of 0 returns only the item with the specified id,
     * a depth of 1 returns the item and its immediate children, and so on.
     * If it is file, then depth will be ignored.
     *
     * @param id    the unique identifier of the storage item to retrieve
     * @param user  user who performs the request (if not owner, will ignore)
     * @param depth [default is 1]the maximum depth of children to include in the response (null/negative means 1)
     * @return a Mono containing the StorageItemResponseDTO, which includes the item and its children
     * @throws EntityNotFoundException if no item with the given id exists (or is not owner)
     * @apiNote If you need to retrieve item without children, consider using {@link #findExactById(UUID,UUID)}
     */
    default Mono<StorageItemResponseDTO> findDescendants(@NonNull UUID id,@NonNull UUID user, Integer depth) throws EntityNotFoundException {
        throw new IllegalStateException("Not yet implemented");
    }

    /**
     * Retrieves the storage item with the specified id and NO children
     *
     * @param id the unique identifier of the storage item to retrieve
     * @param user user who performs the request (if not owner, will ignore)
     * @return a Mono containing the StorageItemResponseDTO, which includes the item
     * @throws EntityNotFoundException if no item with the given id exists (or is not owner)
     */
    default Mono<StorageItemResponseDTO> findExactById(@NonNull UUID id,@NonNull UUID user) throws EntityNotFoundException {
        throw new IllegalStateException("Not yet implemented");
    }
    /**
     * Retrieves all storage items with name LIKE.
     * Max 30 items
     * NB! No depth (contain item meta only, no children)
     *
     * @param name the name of the storage items to retrieve
     * @return a Flux containing StorageItemResponseDTOs of matching storage items (no children)
     */
    default Flux<StorageItemResponseDTO> findByName(@NonNull String name) {
        throw new IllegalStateException("Not yet implemented");
    }

    /**
     * Check if storage item with the given id exists.
     *
     * @param id the id of the storage item to check
     * @return a Mono containing true if the item exists, false otherwise
     */
    default Mono<Boolean> existsById(@NonNull UUID id) {throw new IllegalStateException("Not yet implemented");}

    default Mono<Boolean> isEmpty(@NonNull UUID id) {throw new IllegalStateException("Not yet implemented");}



    default Mono<Void> deleteById(@NonNull UUID id,Boolean recursive,UUID userId ) throws EntityNotFoundException {
        throw new IllegalStateException("Not yet implemented");
    }
}
