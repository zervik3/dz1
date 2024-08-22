import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the file URL: ");
        String fileURL = scanner.nextLine();

        System.out.print("Enter the output file path: ");
        String outputFilePath = scanner.nextLine();

        System.out.print("Enter the number of threads: ");
        int numThreads = scanner.nextInt();

        FileDownloader fileDownloader = new FileDownloader(fileURL, outputFilePath, numThreads);

        try {
            fileDownloader.download();
        } catch (Exception e) {
            System.err.println("Download failed: " + e.getMessage());
        }
    }
}