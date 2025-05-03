package org.thluon.tdrive.repository;

import com.github.thientoan3596.UUIDToBytesConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import org.thluon.tdrive.entity.StorageItem;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@Repository
public class StorageItemRepositoryImpl implements StorageItemRepository {
    private final R2dbcEntityTemplate template;

    @Override
    public Mono<StorageItem> findRootFolder(UUID owner) {
        return template.selectOne(Query.query(Criteria.where("parent_id").isNull().and("owner").is(owner)), StorageItem.class);
    }
    @Override
    public Mono<Boolean> existsRootFolder(UUID owner){
        return template.exists(Query.query(Criteria.where("parent_id").isNull().and("owner").is(owner)), StorageItem.class);
    }

    @Override
    public Mono<Void> insert(StorageItem item) {
        return template.insert(StorageItem.class)
                .using(item)
                .then();
    }

    @Override
    public Flux<StorageItem> findByIdWithDepth(UUID id,UUID owner, int maxDepth) {
        return template.getDatabaseClient()
                .sql("""
                            WITH RECURSIVE item_tree AS (
                                SELECT id, name, type, size, extension, parent_id, owner, created_at, updated_at, 0 AS depth
                                FROM storage_item
                                WHERE id = :id AND owner = :owner
                                UNION ALL
                                SELECT si.id, si.name, si.type, si.size, si.extension, si.parent_id, si.owner, si.created_at, si.updated_at, it.depth + 1
                                FROM storage_item si
                                JOIN item_tree it ON si.parent_id = it.id
                                WHERE it.depth < :maxDepth AND si.owner = :owner
                            )
                            SELECT id, name, type, size, extension, parent_id, owner, created_at, updated_at
                            FROM item_tree
                        """)
                .bind("id", UUIDToBytesConverter.CONVERT(id))
                .bind("owner", UUIDToBytesConverter.CONVERT(owner))
                .bind("maxDepth", maxDepth)
                .map((row, meta) -> template.getConverter().read(StorageItem.class, row))
                .all();
    }

    @Override
    public Flux<StorageItem> findAllDescendantsById(UUID id,UUID owner) {
        return template.getDatabaseClient()
                .sql("""
                            WITH RECURSIVE item_tree AS (
                                SELECT id, name, type, size, extension, parent_id, owner, created_at, updated_at
                                FROM storage_item
                                WHERE id = :id AND owner = :owner
                                UNION ALL
                                SELECT si.id, si.name, si.type, si.size, si.extension, si.parent_id, si.owner, si.created_at, si.updated_at
                                FROM storage_item si
                                JOIN item_tree it ON si.parent_id = it.id
                                WHERE si.owner = :owner
                            )
                            SELECT id, name, type, size, extension, parent_id, owner, created_at, updated_at
                            FROM item_tree
                        """)
                .bind("id", UUIDToBytesConverter.CONVERT(id))
                .bind("owner", UUIDToBytesConverter.CONVERT(owner))
                .map((row, meta) -> template.getConverter().read(StorageItem.class, row))
                .all();
    }

    @Override
    public Mono<StorageItem> findById(UUID id, UUID owner) {
        return template.selectOne(
                Query.query(Criteria.where("id").is(id).and("owner").is(owner)),
                StorageItem.class);
    }

    @Override
    public Flux<StorageItem> findAll() {
        return template.select(StorageItem.class).all();
    }

    @Override
    public Mono<Boolean> existsById(UUID id) {
        return template.exists(
                Query.query(Criteria.where("id").is(id)), StorageItem.class);
    }

    @Override
    public Mono<Boolean> isEmpty(UUID id) {
        return template.exists(
                        Query.query(Criteria.where("parent_id").is(id)), StorageItem.class)
                .map(b -> !b);

    }

    @Override
    public Mono<StorageItem> deleteById(UUID id) {
        return template.selectOne(Query.query(Criteria.where("id").is(id)), StorageItem.class)
                .flatMap(item -> Mono.defer(() -> template
                        .delete(Query.query(Criteria.where("id").is(id)), StorageItem.class)
                        .then(Mono.defer(() -> Mono.just(item)))));
    }

    @Override
    public Flux<StorageItem> deleteStorageItemsAndDescendants(UUID id) {
        return findAllDescendants(id)
                .collectList()
                .flatMapMany(descendants ->
                        template.delete(Query.query(Criteria.where("id").in(descendants.stream().map(StorageItem::getId).toList())), StorageItem.class)
                                .thenMany(Flux.fromIterable(descendants))
                );
    }

    private Flux<StorageItem> findAllDescendants(UUID id) {
        return template.getDatabaseClient()
                .sql("""
                            WITH RECURSIVE item_tree AS (
                                SELECT id, name, type, size, extension, parent_id, owner, created_at, updated_at
                                FROM storage_item
                                WHERE id = :id
                                UNION ALL
                                SELECT si.id, si.name, si.type, si.size, si.extension, si.parent_id, si.owner, si.created_at, si.updated_at
                                FROM storage_item si
                                JOIN item_tree it ON si.parent_id = it.id
                            )
                            SELECT id, name, type, size, extension, parent_id, owner, created_at, updated_at
                            FROM item_tree
                        """)
                .bind("id", UUIDToBytesConverter.CONVERT(id))
                .map((row, meta) -> template.getConverter().read(StorageItem.class, row))
                .all();
    }
}
