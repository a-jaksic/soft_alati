package rs.ac.bg.fon.aleksa_jaksic.sa.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.bg.fon.aleksa_jaksic.sa.files.FileSystemStorageService;
import rs.ac.bg.fon.aleksa_jaksic.sa.files.StorageConfiguration;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.domain.Restaurant;
import rs.ac.bg.fon.aleksa_jaksic.sa.review.domain.Review;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileSystemStorageServiceTest {

    private StorageConfiguration storageConfiguration;
    private FileSystemStorageService storageService;

    @TempDir
    Path sharedTempDir;

    private Restaurant restaurant;
    private Review review;

    @BeforeEach
    void setUp() {

        storageConfiguration = new StorageConfiguration();
        storageConfiguration.setBaseDir(sharedTempDir.toString());
        storageConfiguration.setAllowedContentTypes(List.of("image/jpeg", "image/png"));
        storageConfiguration.setMaxSizeBytes(512000L);

        storageService = new FileSystemStorageService(storageConfiguration);
        storageService.init();

        restaurant = new Restaurant();
        restaurant.setId(10L);

        review = new Review();
        review.setId(200L);
        review.setRestaurant(restaurant);
    }

    @Test
    @DisplayName("Should successfully store valid review photos and return matching relative paths")
    void saveReviewPhotos_Success() throws IOException {
        MultipartFile validFile = mock(MultipartFile.class);
        when(validFile.isEmpty()).thenReturn(false);
        when(validFile.getContentType()).thenReturn("image/jpeg");
        when(validFile.getSize()).thenReturn(10000L);
        when(validFile.getOriginalFilename()).thenReturn("pizza.jpg");
        when(validFile.getInputStream()).thenReturn(new ByteArrayInputStream("fake-jpeg-data".getBytes()));

        List<String> paths = storageService.saveReviewPhotos(review, List.of(validFile));

        assertEquals(1, paths.size());
        assertTrue(paths.get(0).startsWith("restaurants/10/reviews/200/"));
        assertTrue(paths.get(0).endsWith("_pizza.jpg"));
        assertTrue(Files.exists(sharedTempDir.resolve(paths.get(0))));
    }

    @Test
    @DisplayName("Should successfully store valid restaurant gallery photos and return matching relative paths")
    void saveRestaurantPhotos_Success() throws IOException {
        MultipartFile validFile = mock(MultipartFile.class);
        when(validFile.isEmpty()).thenReturn(false);
        when(validFile.getContentType()).thenReturn("image/png");
        when(validFile.getSize()).thenReturn(25000L);
        when(validFile.getOriginalFilename()).thenReturn("facade.png");
        when(validFile.getInputStream()).thenReturn(new ByteArrayInputStream("fake-png-data".getBytes()));

        List<String> paths = storageService.saveRestaurantPhotos(restaurant, List.of(validFile));

        assertEquals(1, paths.size());
        assertTrue(paths.get(0).startsWith("restaurants/10/gallery/"));
        assertTrue(paths.get(0).endsWith("_facade.png"));
        assertTrue(Files.exists(sharedTempDir.resolve(paths.get(0))));
    }

    @ParameterizedTest
    @CsvSource({
            "true,  image/jpeg, 5000,   empty_file.jpg",     // file is empty
            "false, text/plain, 5000,   malicious.txt",      // illegal mime type
            "false, image/jpeg, 999999, oversized.jpg"       // file exceeds max size limit
    })
    @DisplayName("Should throw IllegalArgumentException when processing files that fail structural validations")
    void savePhotos_InvalidFileConstraints_ThrowsException(boolean isEmpty, String contentType, long size, String filename) {
        MultipartFile invalidFile = mock(MultipartFile.class);
        when(invalidFile.isEmpty()).thenReturn(isEmpty);
        when(invalidFile.getContentType()).thenReturn(contentType);
        when(invalidFile.getSize()).thenReturn(size);
        when(invalidFile.getOriginalFilename()).thenReturn(filename);

        List<MultipartFile> filesList = List.of(invalidFile);

        assertThrows(IllegalArgumentException.class, () -> storageService.saveReviewPhotos(review, filesList));
        assertThrows(IllegalArgumentException.class, () -> storageService.saveRestaurantPhotos(restaurant, filesList));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when the input file array payload is null")
    void savePhotos_NullFileList_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> storageService.saveReviewPhotos(review, null));
        assertThrows(IllegalArgumentException.class, () -> storageService.saveRestaurantPhotos(restaurant, null));
    }

    @Test
    @DisplayName("Should completely clean up and remove matching review folder hierarchies off the storage")
    void deleteReviewFolder_DeletesTargetDirectoryRecursively() throws IOException {
        Path reviewDir = Files.createDirectories(sharedTempDir.resolve("restaurants/10/reviews/200"));
        Path targetPhoto = Files.writeString(reviewDir.resolve("photo.jpg"), "content");

        assertTrue(Files.exists(targetPhoto));

        storageService.deleteReviewFolder(10L, 200L);

        assertFalse(Files.exists(targetPhoto));
        assertFalse(Files.exists(reviewDir));
    }

    @Test
    @DisplayName("Should clean up and remove entire restaurant parent folder structure recursively")
    void deleteRestaurantFolder_DeletesAllNestedContentRecursively() throws IOException {
        Path restaurantDir = Files.createDirectories(sharedTempDir.resolve("restaurants/10"));
        Path galleryDir = Files.createDirectories(restaurantDir.resolve("gallery"));
        Path photo = Files.writeString(galleryDir.resolve("img.jpg"), "content");

        assertTrue(Files.exists(photo));

        storageService.deleteRestaurantFolder(10L);

        assertFalse(Files.exists(photo));
        assertFalse(Files.exists(restaurantDir));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException if storeFile detects directory escape patterns")
    void storeFile_PathTraversalAttempt_ThrowsException() {
        MultipartFile maliciousFile = mock(MultipartFile.class);

        assertThrows(IllegalArgumentException.class, () ->
                storageService.storeFile("restaurants/10", "../../../etc/passwd", maliciousFile)
        );
    }
}