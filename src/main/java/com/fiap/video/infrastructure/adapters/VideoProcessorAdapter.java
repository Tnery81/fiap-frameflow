package com.fiap.video.infrastructure.adapters;

import com.fiap.video.core.domain.Video;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class VideoProcessorAdapter {

    private static final String FFMPEG_PATH = "C:/Users/hackw/OneDrive/Desktop/hackton/fiap_video_no_db_project/ffmpeg.exe";

    public void extractFrames(Video video, String outputFolder, int intervalSeconds) {
        try {
            // Cria o diretório para os frames, se não existir
            Files.createDirectories(Paths.get(outputFolder));

            // Comando do FFmpeg para extrair frames
            String command = String.format(
                    "\"%s\" -i \"%s\" -vf fps=1/%d \"%s/frame_%%04d.jpg\"",
                    FFMPEG_PATH,
                    video.getPath(),
                    intervalSeconds,
                    outputFolder
            );

            // Exibe o comando no log para depuração
            System.out.println("Executing command: " + command);

            // Executa o comando
            Process process = Runtime.getRuntime().exec(command);

            // Captura a saída de erro do FFmpeg
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    System.err.println(line); // Exibe os erros no console
                }
            }

            // Aguarda o término do comando
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("FFmpeg failed with exit code: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error during frame extraction", e);
        }
    }

    public void compressFrames(String folderPath, String zipFilePath) {
        try (var zos = new java.util.zip.ZipOutputStream(Files.newOutputStream(Paths.get(zipFilePath)))) {
            Files.walk(Paths.get(folderPath))
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            zos.putNextEntry(new java.util.zip.ZipEntry(Paths.get(folderPath).relativize(path).toString()));
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to zip file", e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("Failed to create ZIP file", e);
        }
    }
}
