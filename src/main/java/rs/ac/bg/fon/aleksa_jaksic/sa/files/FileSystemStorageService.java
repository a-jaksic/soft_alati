package rs.ac.bg.fon.aleksa_jaksic.sa.files;

import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.domain.Restaurant;
import rs.ac.bg.fon.aleksa_jaksic.sa.review.domain.Review;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Service for handling physical storage in the file system.
 * Responsible for verifying incoming multipart images, handling multi-file uploads,
 * resolving paths for assets, and executing recursive directory deletions.
 * @author Aleksa Jakšić (a-jaksic)
 */
@Service
public class FileSystemStorageService {

    private final Path rootLocation;
    private final StorageConfiguration storageConfiguration;

    public FileSystemStorageService(StorageConfiguration storageConfiguration){
        this.storageConfiguration = storageConfiguration;
        this.rootLocation = Paths.get(storageConfiguration.getBaseDir());
    }

    /**
     * Initializes the root storage folder structure upon application startup.
     * @throws java.lang.RuntimeException If directory paths cannot be allocated.
     */
    @PostConstruct
    public void init(){
        try{
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    /**
     * Saves a list of multipart images assigned to a user review, implementing a rollback transaction on failure.
     * @param review Review entity linked to the files.
     * @param files List of MultipartFile images to upload.
     * @return List of relative paths matching saved images.
     * @throws java.lang.IllegalArgumentException If payloads are invalid or validation checks fail.
     * @throws java.lang.IllegalStateException If file storage fails on disk.
     */
    public List<String> saveReviewPhotos(Review review, List<MultipartFile> files) {
        if (files == null) {
            throw new IllegalArgumentException("No files provided!");
        }
        List<String> successfullySavedPaths = new ArrayList<>();
        try {

            Long restId = review.getRestaurant().getId();

            for (MultipartFile file : files) {
                if (!isFileValid(file)) {
                    throw new IllegalArgumentException("One or more files are not valid, check the format and size!");
                }
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                String subPath = "restaurants/" + restId + "/reviews/" + review.getId();
                String fullRelativePath = subPath + "/" + fileName;

                storeFile(subPath, fileName, file);

                successfullySavedPaths.add(fullRelativePath);
            }
            return successfullySavedPaths;

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            for (String path : successfullySavedPaths) {
                deleteFile(path);
            }
            throw new IllegalStateException("File storage failed. Cleaned up partial uploads. Error: " + e.getMessage(), e);
        }
    }


    /**
     * Uploads gallery photos linked to a specific restaurant.
     * @param restaurant Restaurant entity linked to the files.
     * @param files List of MultipartFile images to upload.
     * @return List of relative paths matching saved images.
     * @throws java.lang.IllegalArgumentException If files are missing or formatting checks fail.
     * @throws java.lang.IllegalStateException If file storage fails on disk.
     */
    public List<String> saveRestaurantPhotos(Restaurant restaurant, List<MultipartFile> files) {
        if (files == null) {
            throw new IllegalArgumentException("No files provided!");
        }
        List<String> successfullySavedPaths = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                if (!isFileValid(file)) {
                    throw new IllegalArgumentException("One or more files are not valid, check the format and size!");
                }
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                String subPath = "restaurants/" + restaurant.getId() + "/gallery";
                String fullRelativePath = subPath + "/" + fileName;

                storeFile(subPath, fileName, file);

                successfullySavedPaths.add(fullRelativePath);
            }
            return successfullySavedPaths;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            for (String path : successfullySavedPaths) {
                deleteFile(path);
            }
            throw new IllegalStateException("File storage failed. Cleaned up partial uploads. Error: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes an isolated directory dedicated to a single review.
     * @param restaurantId unique identifier of the restaurant that the review belongs to that is used for tracking the target directory.
     * @param reviewId unique identifier of the review that is also for tracking the target directory.
     */
    public void deleteReviewFolder(Long restaurantId, Long reviewId) {
        Path directoryToDelete = rootLocation.resolve("restaurants/" + restaurantId + "/reviews/" + reviewId);
        recursiveDelete(directoryToDelete);

    }


    /**
     * Deletes an entire restaurant directory pathway along with all nested content.
     * @param id unique identifier of the restaurant that is used to track the target directory.
     */
    public void deleteRestaurantFolder(Long id) {
        Path directoryToDelete = rootLocation.resolve("restaurants/" + id);
        recursiveDelete(directoryToDelete);
    }

    //HELPER FUNKCIJE

    /**
     * Validates an incoming multipart payload against sizing and media type constraints.
     * @param file MultipartFile payload that is being validated.
     * @return true - if the payload has valid size and media types,
     * false - if the payload doesn't use the valid media types or the size is too big.
     */
    public boolean isFileValid(MultipartFile file) {
        return !file.isEmpty() && storageConfiguration.getAllowedContentTypes().contains(file.getContentType()) && file.getSize() <= storageConfiguration.getMaxSizeBytes();
    }

    /**
     * Saves an individual multipart file stream to disk at a target location.
     * @param subPath nested sub-path where the file is saved.
     * @param fileName generated identifier for the file name.
     * @param file MultipartFile payload.
     * @throws java.lang.IllegalArgumentException If path manipulation traces are identified.
     * @throws java.lang.IllegalStateException If the storing of the file fails.
     */
    public void storeFile(String subPath, String fileName, MultipartFile file) {
        if (fileName.contains("..")) {
            throw new IllegalArgumentException("Filename contains invalid path sequences.");
        }

        try {
            Path folderPath = this.rootLocation.resolve(subPath);
            Files.createDirectories(folderPath);

            Path targetLocation = folderPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file on disk: " + e.getMessage());
        }
    }

    /**
     * Locates and maps a system file path into a readable Resource reference.
     * @param path path to the file that is being fetched.
     * @return Resource representation of the file that is being fetched.
     * @throws java.net.MalformedURLException If the file URI cannot be converted to a URL.
     * @throws java.io.FileNotFoundException If the targeted file does not exist or is unreadable.
     */
    public Resource fetchFile(String path) throws MalformedURLException,FileNotFoundException {
        try {
            Path file = rootLocation.resolve(path);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new FileNotFoundException("Could not read file: " + path);
            }
        } catch (MalformedURLException e) {
            throw new MalformedURLException("Could not read file: " + e.getMessage());
        }
    }

    /**
     * Silently deletes an isolated single file off the system.
     * @param path relative file location.
     */
    public void deleteFile(String path) {
        try {
            Path fileToDelete = rootLocation.resolve(path);
            Files.deleteIfExists(fileToDelete);
        } catch (IOException e) {
            System.err.println("Failed to delete orphaned file: " + path);
        }
    }

    /**
     * Cascades down a target file directory and deletes child files prior to deleting the directory.
     * @param directoryToDelete path to the directory that is being deleted.
     */
    private void recursiveDelete(Path directoryToDelete) {
        try (Stream<Path> walk = Files.walk(directoryToDelete)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            System.err.println("Failed to delete: " + path);
                        }
                    });
        } catch (IOException e) {
            System.err.println("Failed to delete folder!");
        }
    }

}
