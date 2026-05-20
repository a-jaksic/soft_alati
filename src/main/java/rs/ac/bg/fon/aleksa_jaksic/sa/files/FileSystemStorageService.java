package rs.ac.bg.fon.aleksa_jaksic.sa.files;

import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.domain.Restaurant;
import rs.ac.bg.fon.aleksa_jaksic.sa.review.domain.Review;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

@Service
public class FileSystemStorageService {

    private final Path rootLocation;
    private final StorageConfiguration storageConfiguration;

    public FileSystemStorageService(StorageConfiguration storageConfiguration){
        this.storageConfiguration = storageConfiguration;
        this.rootLocation = Paths.get(storageConfiguration.getBaseDir());
    }

    @PostConstruct
    public void init(){
        try{
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    public List<String> saveReviewPhotos(Review review, List<MultipartFile> files) throws Exception{
        if (files == null) {
            throw new Exception("No files provided!");
        }
        List<String> successfullySavedPaths = new ArrayList<>();
        try {

            Long restId = review.getRestaurant().getId();

            for (MultipartFile file : files) {
                if (!isFileValid(file)) {
                    throw new Exception("One or more files are not valid, check the format and size!");
                }
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                String subPath = "restaurants/" + restId + "/reviews/" + review.getId();
                String fullRelativePath = subPath + "/" + fileName;

                storeFile(subPath, fileName, file);

                successfullySavedPaths.add(fullRelativePath);
            }
            return successfullySavedPaths;

        } catch (Exception e) {
            for (String path : successfullySavedPaths) {
                deleteFile(path);
            }
            throw new Exception("File storage failed. Cleaned up partial uploads. Error: " + e.getMessage());
        }
    }


    public List<String> saveRestaurantPhotos(Restaurant restaurant, List<MultipartFile> files) throws Exception{
        if (files == null) {
            throw new Exception("No files provided!");
        }
        List<String> successfullySavedPaths = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                if (!isFileValid(file)) {
                    throw new Exception("One or more files are not valid, check the format and size!");
                }
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                String subPath = "restaurants/" + restaurant.getId() + "/gallery";
                String fullRelativePath = subPath + "/" + fileName;

                storeFile(subPath, fileName, file);

                successfullySavedPaths.add(fullRelativePath);
            }
            return successfullySavedPaths;

        } catch (Exception e) {
            for (String path : successfullySavedPaths) {
                deleteFile(path);
            }
            throw new Exception("File storage failed. Cleaned up partial uploads. Error: " + e.getMessage());
        }
    }

    public void deleteReviewFolder(Long restaurantId, Long reviewId) throws Exception{
        Path directoryToDelete = rootLocation.resolve("restaurants/" + restaurantId + "/reviews/" + reviewId);
        recursiveDelete(directoryToDelete);

    }


    public void deleteRestaurantFolder(Long id) throws Exception{
        Path directoryToDelete = rootLocation.resolve("restaurants/" + id);
        recursiveDelete(directoryToDelete);
    }

    //HELPER FUNKCIJE

    public boolean isFileValid(MultipartFile file) throws Exception {
        return !file.isEmpty() && storageConfiguration.getAllowedContentTypes().contains(file.getContentType()) && file.getSize() <= storageConfiguration.getMaxSizeBytes();
    }

    public void storeFile(String subPath, String fileName, MultipartFile file) throws Exception {
        if (fileName.contains("..")) {
            throw new Exception("Filename contains invalid path sequences.");
        }

        try {
            Path folderPath = this.rootLocation.resolve(subPath);
            Files.createDirectories(folderPath);

            Path targetLocation = folderPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new Exception("Failed to store file on disk: " + e.getMessage());
        }
    }

    public Resource fetchFile(String path) throws Exception{
        try {
            Path file = rootLocation.resolve(path);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new Exception("Could not read file: " + path);
            }
        } catch (MalformedURLException e) {
            throw new Exception("Could not read file: " + path, e);
        }
    }

    public void deleteFile(String path) {
        try {
            Path fileToDelete = rootLocation.resolve(path);
            Files.deleteIfExists(fileToDelete);
        } catch (IOException e) {
            System.err.println("Failed to delete orphaned file: " + path);
        }
    }

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
