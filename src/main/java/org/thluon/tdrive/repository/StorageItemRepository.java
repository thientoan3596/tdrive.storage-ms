package org.thluon.tdrive.repository;

import org.thluon.tdrive.entity.StorageItem;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;


public interface StorageItemRepository {
    Mono<StorageItem> findRootFolder(UUID owner);
    Mono<Boolean> existsRootFolder(UUID owner);

    Mono<Void> insert(StorageItem item);
    /**
     * Finds the item with the given id and its children, up to the given depth.
     * The depth is zero-indexed, so a depth of 0 only returns the item with the given id,
     * a depth of 1 returns the item and its children, and so on.
     *
     * @param id       the id of the root item
     * @param owner    the owner (user who perform request)
     * @param maxDepth the maximum depth of children to include
     * @return a flux of items
     * @apiNote first item is ALWAYS the root (UNION ALL nature).
     */

    Flux<StorageItem> findByIdWithDepth(UUID id, UUID owner, int maxDepth);
    Flux<StorageItem> findAllDescendantsById(UUID id, UUID owner);

    Mono<StorageItem> findById(UUID id,UUID owner);
    Flux<StorageItem> findAll();
    Mono<Boolean> existsById(UUID id);
    Mono<Boolean> isEmpty(UUID id);

    /**
     * Deletes the item with the given id.
     * @param id the id of the item to delete
     * @return a Mono containing the deleted item
     */
    Mono<StorageItem> deleteById(UUID id);

    /**
     * Deletes the item with the given id and all its descendants.
     * @param id the id of the item to delete
     * @return a Flux containing the deleted items
     */
    Flux<StorageItem> deleteStorageItemsAndDescendants(UUID id);
}
