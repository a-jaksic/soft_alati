package rs.ac.bg.fon.aleksa_jaksic.sa.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.bg.fon.aleksa_jaksic.sa.files.FileSystemStorageService;
import rs.ac.bg.fon.aleksa_jaksic.sa.photo.domain.Photo;
import rs.ac.bg.fon.aleksa_jaksic.sa.photo.dtos.PhotoDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.photo.mapper.PhotoMapper;
import rs.ac.bg.fon.aleksa_jaksic.sa.photo.repository.PhotoRepository;
import rs.ac.bg.fon.aleksa_jaksic.sa.photo.service.PhotoService;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.domain.Restaurant;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.repository.RestaurantRepository;
import rs.ac.bg.fon.aleksa_jaksic.sa.review.domain.Review;
import rs.ac.bg.fon.aleksa_jaksic.sa.review.repository.ReviewRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhotoServiceTest {

    @Mock
    private PhotoRepository photoRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private PhotoMapper photoMapper;
    @Mock
    private FileSystemStorageService fileSystemStorageService;

    @InjectMocks
    private PhotoService photoService;

    private Photo samplePhoto;
    private PhotoDTO samplePhotoDTO;
    private Review sampleReview;
    private Restaurant sampleRestaurant;

    @BeforeEach
    void setUp() {
        sampleRestaurant = new Restaurant();

        sampleReview = new Review();
        sampleReview.setId(100L);
        sampleReview.setRestaurant(sampleRestaurant);

        samplePhoto = Photo.builder()
                .id(1L)
                .filePath("uploads/review_100/pic.jpg")
                .review(sampleReview)
                .build();

        samplePhotoDTO = new PhotoDTO(1L);
    }

    @Test
    @DisplayName("Should return matching list of PhotoDTOs when photos exist for given review ID")
    void listByReview_PhotosExist_ReturnsPhotoDTOList() {
        Long reviewId = 100L;
        when(photoRepository.findByReviewId(reviewId)).thenReturn(List.of(samplePhoto));
        when(photoMapper.toDTO(samplePhoto)).thenReturn(samplePhotoDTO);

        List<PhotoDTO> result = photoService.listByReview(reviewId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
        verify(photoRepository).findByReviewId(reviewId);
        verify(photoMapper).toDTO(samplePhoto);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when review has no associated photos")
    void listByReview_NoPhotos_ThrowsEntityNotFoundException() {
        Long reviewId = 100L;
        when(photoRepository.findByReviewId(reviewId)).thenReturn(Collections.emptyList());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                photoService.listByReview(reviewId)
        );

        assertEquals("Couldn't retrieve photos for given review!", exception.getMessage());
        verifyNoInteractions(photoMapper);
    }

    @Test
    @DisplayName("Should save files and persist Photo entities when attaching photos to valid review")
    void attachPhotosToReview_ValidReview_Success() throws Exception {
        Long reviewId = 100L;
        List<MultipartFile> mockFiles = List.of(mock(MultipartFile.class));
        List<String> paths = List.of("uploads/review_100/pic.jpg");

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(sampleReview));
        when(fileSystemStorageService.saveReviewPhotos(sampleReview, mockFiles)).thenReturn(paths);
        when(photoRepository.saveAll(anyList())).thenReturn(List.of(samplePhoto));
        when(photoMapper.toDTO(any(Photo.class))).thenReturn(samplePhotoDTO);

        List<PhotoDTO> result = photoService.attachPhotosToReview(reviewId, mockFiles);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reviewRepository).findById(reviewId);
        verify(fileSystemStorageService).saveReviewPhotos(sampleReview, mockFiles);
        verify(photoRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when review does not have an associated restaurant")
    void attachPhotosToReview_ReviewMissingRestaurant_ThrowsIllegalArgumentException() {
        Long reviewId = 100L;
        sampleReview.setRestaurant(null);
        List<MultipartFile> mockFiles = List.of(mock(MultipartFile.class));

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(sampleReview));

        assertThrows(IllegalArgumentException.class, () ->
                photoService.attachPhotosToReview(reviewId, mockFiles)
        );
        verifyNoInteractions(fileSystemStorageService, photoRepository);
    }

    @Test
    @DisplayName("Should trigger storage file cleanup if database save action fails when attaching photos")
    void attachPhotosToReview_DatabaseException_TriggersStorageCleanup() {
        Long reviewId = 100L;
        List<MultipartFile> mockFiles = List.of(mock(MultipartFile.class));
        List<String> paths = List.of("uploads/review_100/broken_persistence.jpg");

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(sampleReview));
        when(fileSystemStorageService.saveReviewPhotos(sampleReview, mockFiles)).thenReturn(paths);
        when(photoRepository.saveAll(anyList())).thenThrow(new RuntimeException("DB Connection Timeout"));

        assertThrows(RuntimeException.class, () ->
                photoService.attachPhotosToReview(reviewId, mockFiles)
        );

        verify(fileSystemStorageService).deleteFile("uploads/review_100/broken_persistence.jpg");
    }

    @Test
    @DisplayName("Should return matching list of PhotoDTOs when photos exist for given restaurant ID")
    void listByRestaurant_PhotosExist_ReturnsPhotoDTOList() {
        Long restaurantId = 55L;
        when(photoRepository.findByRestaurantId(restaurantId)).thenReturn(List.of(samplePhoto));
        when(photoMapper.toDTO(samplePhoto)).thenReturn(samplePhotoDTO);

        List<PhotoDTO> result = photoService.listByRestaurant(restaurantId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(photoRepository).findByRestaurantId(restaurantId);
    }

    @Test
    @DisplayName("Should save files and persist Photo entities when gallery photos attach to restaurant")
    void attachPhotosToRestaurant_ValidRestaurant_Success() throws Exception {
        Long restaurantId = 55L;
        List<MultipartFile> mockFiles = List.of(mock(MultipartFile.class));
        List<String> paths = List.of("uploads/restaurant_55/gallery.jpg");

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(sampleRestaurant));
        when(fileSystemStorageService.saveRestaurantPhotos(sampleRestaurant, mockFiles)).thenReturn(paths);
        when(photoRepository.saveAll(anyList())).thenReturn(List.of(samplePhoto));
        when(photoMapper.toDTO(any(Photo.class))).thenReturn(samplePhotoDTO);

        List<PhotoDTO> result = photoService.attachPhotosToRestaurant(restaurantId, mockFiles);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(restaurantRepository).findById(restaurantId);
        verify(fileSystemStorageService).saveRestaurantPhotos(sampleRestaurant, mockFiles);
    }

    @Test
    @DisplayName("Should execute file rollback if database save fails when processing restaurant gallery uploads")
    void attachPhotosToRestaurant_DatabaseException_TriggersStorageCleanup() {
        Long restaurantId = 55L;
        List<MultipartFile> mockFiles = List.of(mock(MultipartFile.class));
        List<String> paths = List.of("uploads/restaurant_55/failed_gallery.jpg");

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(sampleRestaurant));
        when(fileSystemStorageService.saveRestaurantPhotos(sampleRestaurant, mockFiles)).thenReturn(paths);
        when(photoRepository.saveAll(anyList())).thenThrow(new RuntimeException("DB Out Of Memory"));

        assertThrows(RuntimeException.class, () ->
                photoService.attachPhotosToRestaurant(restaurantId, mockFiles)
        );

        verify(fileSystemStorageService).deleteFile("uploads/restaurant_55/failed_gallery.jpg");
    }

    @Test
    @DisplayName("Should load file as Spring Resource when database record is verified")
    void loadAsResource_ValidId_ReturnsResource() throws Exception {
        Long photoId = 1L;
        Resource mockResource = mock(Resource.class);
        when(photoRepository.findById(photoId)).thenReturn(Optional.of(samplePhoto));
        when(fileSystemStorageService.fetchFile(samplePhoto.getFilePath())).thenReturn(mockResource);

        Resource result = photoService.loadAsResource(photoId);

        assertNotNull(result);
        verify(photoRepository).findById(photoId);
        verify(fileSystemStorageService).fetchFile(samplePhoto.getFilePath());
    }

    @Test
    @DisplayName("Should wrap storage exception into EntityNotFoundException if loading resource fails")
    void loadAsResource_StorageException_ThrowsEntityNotFoundException() throws Exception {
        Long photoId = 1L;
        when(photoRepository.findById(photoId)).thenReturn(Optional.of(samplePhoto));
        when(fileSystemStorageService.fetchFile(anyString())).thenThrow(new RuntimeException("File corrupted"));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                photoService.loadAsResource(photoId)
        );

        assertEquals("Could not fetch photo with given id!", exception.getMessage());
    }

    @Test
    @DisplayName("Should completely drop physical file and database entry when executing delete")
    void delete_ValidId_DeletesFromStorageAndDatabase() {
        Long photoId = 1L;
        when(photoRepository.findById(photoId)).thenReturn(Optional.of(samplePhoto));

        assertAll(() -> photoService.delete(photoId));

        verify(fileSystemStorageService).deleteFile(samplePhoto.getFilePath());
        verify(photoRepository).delete(samplePhoto);
    }

    @ParameterizedTest
    @ValueSource(longs = {2L, 404L})
    @DisplayName("Should throw EntityNotFoundException across generic missing photo lookups")
    void common_PhotoNotFound_ThrowsEntityNotFoundException(Long missingId) {
        when(photoRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> photoService.loadAsResource(missingId));
        assertThrows(EntityNotFoundException.class, () -> photoService.delete(missingId));

        verifyNoInteractions(fileSystemStorageService);
    }
}
