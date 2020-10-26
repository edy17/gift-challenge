package org.diehl.wedoogift.domain.repository;

import java.io.IOException;

public interface FileRepository<T> {

    T searchByFilePath(String path) throws IOException;

    void saveToFilePath(T t, String path) throws IOException;
}
