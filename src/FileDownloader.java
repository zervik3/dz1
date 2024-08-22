import java.io.*;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FileDownloader {
    private final String fileURL;
    private final String outputFilePath;
    private final int numThreads;

    public FileDownloader(String fileURL, String outputFilePath, int numThreads) {
        this.fileURL = fileURL;
        this.outputFilePath = outputFilePath;
        this.numThreads = numThreads;
    }

    public void download() throws Exception {
        // Создаем HTTP соединение для получения размера файла
        URL url = new URL(fileURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        int fileSize = connection.getContentLength();
        connection.disconnect();

        // Создаем ExecutorService для управления потоками
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        int chunkSize = fileSize / numThreads;
        Future<?>[] futures = new Future[numThreads];

        try (RandomAccessFile outputFile = new RandomAccessFile(outputFilePath, "rw")) {
            for (int i = 0; i < numThreads; i++) {
                int start = i * chunkSize;
                int end = (i == numThreads - 1) ? fileSize : start + chunkSize - 1;
                futures[i] = executor.submit(new DownloadTask(fileURL, outputFile, start, end));
            }

            // Ждем завершения всех потоков
            for (Future<?> future : futures) {
                future.get();
            }
        } finally {
            executor.shutdown();
        }

        System.out.println("Download complete.");
    }

    private static class DownloadTask implements Runnable {
        private final String fileURL;
        private final RandomAccessFile outputFile;
        private final int start;
        private final int end;

        public DownloadTask(String fileURL, RandomAccessFile outputFile, int start, int end) {
            this.fileURL = fileURL;
            this.outputFile = outputFile;
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            try {
                URL url = new URL(fileURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Range", "bytes=" + start + "-" + end);

                try (InputStream inputStream = connection.getInputStream()) {
                    outputFile.seek(start);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputFile.write(buffer, 0, bytesRead);
                    }
                }
                connection.disconnect();
            } catch (IOException e) {
                System.err.println("Error downloading file: " + e.getMessage());
            }
        }
    }
}