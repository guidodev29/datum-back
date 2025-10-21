package com.datum.domain.ports.in;

import com.datum.domain.model.Folder;
import java.util.List;

public interface FolderUseCasePort {
    Folder createFolder(Folder folder);
    Folder getFolderById(Long id);
    List<Folder> getAllFolders();
    List<Folder> getFoldersByUserId(Long userId);
    Folder updateFolder(Long id, Folder folder);
    void deleteFolder(Long id);
}