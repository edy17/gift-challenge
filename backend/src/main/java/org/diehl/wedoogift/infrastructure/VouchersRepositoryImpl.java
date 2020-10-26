package org.diehl.wedoogift.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.diehl.wedoogift.domain.model.Vouchers;
import org.diehl.wedoogift.domain.repository.FileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class VouchersRepositoryImpl implements FileRepository<Vouchers> {

    private final Logger logger = LoggerFactory.getLogger(VouchersRepositoryImpl.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Vouchers searchByFilePath(String path) throws IOException {
        Path outputPath = Paths.get(path);
        String content = new String(Files.readAllBytes(outputPath));
        return this.objectMapper
                .readerFor(Vouchers.class)
                .readValue(content);
    }

    @Override
    public void saveToFilePath(Vouchers endowments, String path) throws IOException {
        String content = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(endowments);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write(content);
        }
        logger.info("Successful file writing: {}", path);
    }
}
