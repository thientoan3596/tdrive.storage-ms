package org.thluon.tdrive.service;

import com.fasterxml.uuid.Generators;
import com.github.thientoan3596.exception.EntityNotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.thluon.tdrive.dto.FolderInsertDTO;
import org.thluon.tdrive.dto.PathEntry;
import org.thluon.tdrive.dto.StorageItemResponseDTO;
import org.thluon.tdrive.entity.EType;
import org.thluon.tdrive.entity.StorageItem;
import org.thluon.tdrive.exception.NonEmptyFolderException;
import org.thluon.tdrive.exception.RootFolderExistedForUser;
import org.thluon.tdrive.mapper.StorageItemMapper;
import org.thluon.tdrive.repository.StorageItemRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
@RequiredArgsConstructor
public class StorageItemServiceImpl implements StorageService {
    private final StorageItemRepository storageItemRepository;
    private final StorageItemMapper storageItemMapper;
    private final ReactiveFileService reactiveFileService;


    @Override
    public Mono<StorageItemResponseDTO> insertNewFile(
            @NonNull FilePart filePart, @NonNull UUID parentId, @NonNull UUID userId)
            throws EntityNotFoundException {
        return existsById(parentId)
                .flatMap(
                        exist -> {
                            if (!exist)
                                return Mono.error(new EntityNotFoundException("No file or folder with id [" + parentId + "]", "id", StorageItem.class.getName(), parentId.toString()));
                            int extensionIndex = filePart.filename().lastIndexOf(".");
                            String extension = filePart.filename().substring(extensionIndex + 1);
                            String fileId = Generators.timeBasedGenerator().generate().toString();
                            String fileName = ReactiveFileService.sanitizeFileName(filePart.filename());
                            return reactiveFileService
                                    .uploadFile(filePart, fileId)
                                    .doOnError(e -> System.out.println("error"))
                                    .then(
                                            filePart.content()
                                                    .reduce(0L, (size, buffer) -> size + buffer.readableByteCount())
                                                    .map(size -> StorageItem.builder()
                                                            .size(size)
                                                            .id(UUID.fromString(fileId))
                                                            .type(EType.File)
                                                            .name(fileName)
                                                            .extension(extension)
                                                            .owner(userId)
                                                            .parentId(parentId)
                                                            .build()))
                                    .flatMap(this::insert);
                        });
    }

    @Override
    public Mono<StorageItemResponseDTO> insertFolder(
            @NonNull FolderInsertDTO request, @NonNull UUID userId)
            throws EntityNotFoundException {
        return findExactById(request.getParentId(), userId)
                .flatMap(result -> {
                    StorageItem storageItem = storageItemMapper.toStorageItem(request, userId);
                    return storageItemRepository.insert(storageItem)
                            .then(Mono.defer(() -> storageItemRepository.findById(storageItem.getId(), userId)))
                            .map(storageItemMapper::toStorageItemResponseDTO);
                });
    }

    @Override
    public Mono<StorageItemResponseDTO> insertRoot(@NonNull UUID user)
            throws RootFolderExistedForUser {
        return getRootFolder(user);
    }

    /**
     * Internal method of inserting item
     */
    private Mono<StorageItemResponseDTO> insert(StorageItem payload) {
        return storageItemRepository.insert(payload).then(storageItemRepository.findById(payload.getId(), payload.getOwner()))
                .map(storageItemMapper::toStorageItemResponseDTO);
    }

    @Override
    public Mono<StorageItemResponseDTO> getRootFolder(@NonNull UUID user) {
        return storageItemRepository
                .findRootFolder(user)
                .switchIfEmpty(
                        Mono.defer(
                                () -> {
                                    StorageItem root =
                                            StorageItem.builder()
                                                    .owner(user)
                                                    .name("root")
                                                    .parentId(null)
                                                    .size(0L)
                                                    .build();
                                    return storageItemRepository
                                            .insert(root)
                                            .then(Mono.defer(() -> storageItemRepository.findRootFolder(user)));
                                }))
                .map(storageItemMapper::toStorageItemResponseDTO);
    }

    @Override
    public Mono<StorageItemResponseDTO> getRootFolderWithDepth(@NonNull UUID user, Integer depth) {
        return getRootFolder(user).flatMap(r -> findDescendants(r.getId(), user, depth));
    }

    /**
     * Finds the item with the given id and its children, up to the given depth. The depth is
     * zero-indexed, so a depth of 1 only returns the item with the given id, a depth of 1 returns the
     * item and its children (no grandchildren), and so on.
     *
     * @param id    the id of the root item
     * @param depth the maximum depth of children to include,
     * @return a flux of items
     * @throws EntityNotFoundException if no item with the given id exists.
     * @apiNote first item is ALWAYS the root (UNION ALL nature).
     */
    public Mono<StorageItemResponseDTO> findDescendants(
            @NonNull UUID id, @NonNull UUID user, Integer depth) {
        if (depth == null || depth < 1) depth = 1;
        return storageItemRepository
                .findByIdWithDepth(id, user, depth)
                .switchIfEmpty(
                        Mono.error(
                                new EntityNotFoundException(
                                        "No file or folder with id [" + id + "]",
                                        "id",
                                        StorageItem.class.getName(),
                                        id.toString())))
                .collectList()
                .map(
                        items -> {
                            StorageItem root = items.get(0);
                            Map<UUID, List<StorageItem>> parentToChildrenMap = new HashMap<>();
                            items.stream()
                                    .skip(1)
                                    .forEach(
                                            item ->
                                                    parentToChildrenMap
                                                            .computeIfAbsent(item.getParentId(), k -> new ArrayList<>())
                                                            .add(item));
                            return storageItemMapper.toStorageItemResponseDTO(root, parentToChildrenMap);
                        });
    }

    public Flux<StorageItem> findAll() {
        return storageItemRepository.findAll();
    }

    @Override
    public Mono<StorageItemResponseDTO> findExactById(@NonNull UUID id, @NonNull UUID user)
            throws EntityNotFoundException {
        return storageItemRepository
                .findById(id, user)
                .switchIfEmpty(
                        Mono.error(
                                new EntityNotFoundException(
                                        "No file or folder with id [" + id + "]",
                                        "id",
                                        StorageItem.class.getName(),
                                        id.toString())))
                .map(storageItemMapper::toStorageItemResponseDTO);
    }

    @Override
    public Flux<DataBuffer> streamItem(@NonNull UUID id, @NonNull UUID user) throws EntityNotFoundException {
        return storageItemRepository.findAllDescendantsById(id, user)
                .switchIfEmpty(
                        Mono.error(
                                new EntityNotFoundException(
                                        "No file or folder with id [" + id + "]",
                                        "id",
                                        StorageItem.class.getName(),
                                        id.toString())))
                .collectList()
                .flatMapMany(items -> {
                    if (items.get(0).getType().equals(EType.File))
                        return reactiveFileService.streamFile(items.get(0).getId().toString());
                    Map<UUID, List<StorageItem>> parentMap = new HashMap<>();
                    for (StorageItem item : items) {
                        parentMap.computeIfAbsent(item.getParentId(), k -> new ArrayList<>()).add(item);
                    }
                    List<PathEntry> paths = new ArrayList<>();
                    Deque<Map.Entry<StorageItem, String>> stack = new ArrayDeque<>();
                    StorageItem root = items.get(0); // Take the first item as root
                    stack.push(new AbstractMap.SimpleEntry<>(root, root.getName()));
                    while (!stack.isEmpty()) {
                        Map.Entry<StorageItem, String> entry = stack.pop();
                        StorageItem node = entry.getKey();
                        String path = entry.getValue();

                        List<StorageItem> children = parentMap.get(node.getId());
                        boolean isFile = node.getType().equals(EType.File);
                        boolean isEmptyFolder = node.getType().equals(EType.Folder) && children.isEmpty();
                        if (isFile) {
                            if (!node.getExtension().isBlank())
                                paths.add(new PathEntry(path + "." + node.getExtension(), node.getId().toString()));
                            else
                                paths.add(new PathEntry(path, node.getId().toString()));
                        } else if (isEmptyFolder) {
                            paths.add(new PathEntry(path, null));
                        } else {
                            ListIterator<StorageItem> iter = children.listIterator(children.size());
                            while (iter.hasPrevious()) {
                                StorageItem child = iter.previous();
                                stack.push(new AbstractMap.SimpleEntry<>(child, path + "/" + child.getName()));
                            }
                        }
                    }
                    return reactiveFileService.streamZip(paths);
                });
    }

    @Override
    public Mono<Boolean> existsById(@NonNull UUID id) {
        return storageItemRepository.existsById(id);
    }

    @Override
    public Mono<Boolean> isEmpty(@NonNull UUID id) {
        return storageItemRepository.isEmpty(id);
    }

    @Override
    public Mono<Void> deleteById(@NonNull UUID id, Boolean recursive, UUID userId)
            throws EntityNotFoundException {
        return findExactById(id, userId)
                .onErrorResume(e -> Mono.empty())
                .flatMap(
                        entity -> {
                            // If not exist, terminate
                            if (!entity.getOwner().equals(userId)) return Mono.empty();
                            // If is file, delete directly
                            if (entity.getType().equals(EType.File))
                                return deleteStorageItem(entity.getId())
                                        .then(
                                                Mono.defer(
                                                        () -> reactiveFileService.deleteFile(entity.getId().toString())));
                            // If folder, check if isEmpty
                            return storageItemRepository
                                    .isEmpty(entity.getId())
                                    .flatMap(
                                            empty -> {
                                                // If is empty then delete
                                                if (empty)
                                                    return deleteStorageItem(entity.getId())
                                                            .then(
                                                                    Mono.defer(
                                                                            () ->
                                                                                    reactiveFileService.deleteFile(
                                                                                            entity.getId().toString())));
                                                // If not empty, then if recursive, delete recursively
                                                if (recursive)
                                                    return deleteStorageItems(entity.getId())
                                                            .flatMap(
                                                                    storageItem ->
                                                                            reactiveFileService.deleteFile(
                                                                                    storageItem.getId().toString()))
                                                            .then();
                                                // If not empty and not recursive delete, then give error
                                                return Mono.error(
                                                        new NonEmptyFolderException(
                                                                "Folder is not empty!",
                                                                "id",
                                                                StorageItem.class.getName(),
                                                                id.toString()));
                                            });
                        })
                .then();
    }

    private Flux<StorageItem> deleteStorageItems(UUID id) {
        return storageItemRepository.deleteStorageItemsAndDescendants(id);
    }

    private Mono<StorageItem> deleteStorageItem(UUID id) {
        return storageItemRepository.deleteById(id);
    }
}
