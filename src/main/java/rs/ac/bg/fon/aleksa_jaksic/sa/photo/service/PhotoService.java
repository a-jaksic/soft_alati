package rs.ac.bg.fon.aleksa_jaksic.sa.photo.service;

import jakarta.persistence.EntityNotFoundException;
import rs.ac.bg.fon.aleksa_jaksic.sa.files.FileSystemStorageService;
import rs.ac.bg.fon.aleksa_jaksic.sa.photo.domain.Photo;
import rs.ac.bg.fon.aleksa_jaksic.sa.photo.dtos.PhotoDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.photo.mapper.PhotoMapper;
import rs.ac.bg.fon.aleksa_jaksic.sa.photo.repository.PhotoRepository;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.domain.Restaurant;
import rs.ac.bg.fon.aleksa_jaksic.sa.restaurant.repository.RestaurantRepository;
import rs.ac.bg.fon.aleksa_jaksic.sa.review.domain.Review;
import rs.ac.bg.fon.aleksa_jaksic.sa.review.repository.ReviewRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service managing photo uploads, storage tracking of photos and resource retrieval.
 * Handles assigning photos to either a specific user review or a restaurant's gallery.
 * @author Aleksa Jaksic (a-jaksic)
 */
@Service
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final ReviewRepository reviewRepository;
    private final RestaurantRepository restaurantRepository;
    private final PhotoMapper photoMapper;
    private final FileSystemStorageService fileSystemStorageService;

    public PhotoService(PhotoRepository photoRepository, ReviewRepository reviewRepository, RestaurantRepository restaurantRepository, PhotoMapper photoMapper, FileSystemStorageService fileSystemStorageService){
        this.photoRepository = photoRepository;
        this.reviewRepository = reviewRepository;
        this.restaurantRepository = restaurantRepository;
        this.photoMapper = photoMapper;
        this.fileSystemStorageService = fileSystemStorageService;
    }

    /**
     * Retrieves all photos associated with a specific review.
     * @param id identifier of the review.
     * @return List of PhotoDTO objects.
     * @throws jakarta.persistence.EntityNotFoundException If no photos are associated with the given review.
     */
    public List<PhotoDTO> listByReview(Long id) {
        List<Photo> photos = photoRepository.findByReviewId(id);
        if (!photos.isEmpty()) {
            List<PhotoDTO> photoDTOS = new ArrayList<>();
            photos.forEach(photo -> {
                PhotoDTO photoDTO = photoMapper.toDTO(photo);
                photoDTOS.add(photoDTO);
            });
            return photoDTOS;
        }
        else throw new EntityNotFoundException("Couldn't retrieve photos for given review!");
    }

    /**
     * Uploads and associates a list of images with a specific review.
     * Cleans up storage files if the database persistence fails.
     * @param id identifier of the review.
     * @param files List of multipart image files to be processed and saved.
     * @return List of PhotoDTO records representing the persisted images.
     * @throws jakarta.persistence.EntityNotFoundException If the target review cannot be found.
     * @throws java.lang.IllegalArgumentException If the review is malformed or missing its restaurant association.
     */
    @Transactional
    public List<PhotoDTO> attachPhotosToReview(Long id, List<MultipartFile> files) throws Exception {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("The review for given photos does not exist!"));
        if (review.getRestaurant() == null) {
            throw new IllegalArgumentException("The review does not belong to a restaurant!");
        }
        List<String> savedPhotoPaths = fileSystemStorageService.saveReviewPhotos(review, files);
        try {
            List<Photo> photos = savedPhotoPaths.stream().map(path ->
                    Photo.builder()
                            .filePath(path)
                            .createdAt(LocalDateTime.now())
                            .review(review)
                            .restaurant(null)
                            .build()
            ).toList();
            return photoRepository.saveAll(photos).stream().map(photoMapper::toDTO).toList();
        } catch (Exception e) {
            savedPhotoPaths.forEach(fileSystemStorageService::deleteFile);
            throw e;
        }
    }

    /**
     * Retrieves all photos associated with a specific restaurant.
     * @param id identifier of the restaurant.
     * @return List of PhotoDTO objects.
     * @throws jakarta.persistence.EntityNotFoundException If no photos are found for the given restaurant.
     */
    public List<PhotoDTO> listByRestaurant(Long id){
        List<Photo> photos = photoRepository.findByRestaurantId(id);
        if (photos != null) {
            List<PhotoDTO> photoDTOS = new ArrayList<>();
            photos.forEach(photo -> {
                PhotoDTO photoDTO = photoMapper.toDTO(photo);
                photoDTOS.add(photoDTO);
            });
            return photoDTOS;
        }
        else throw new EntityNotFoundException("Couldn't retrieve photos for given restaurant!");
    }

    /**
     * Uploads and associates a list of images with a restaurant's gallery.
     * Cleans up storage files if the database persistence fails.
     * @param id identifier of the target restaurant.
     * @param files List of multipart image files to be processed and saved.
     * @return List of PhotoDTO records representing the persisted images.
     * @throws jakarta.persistence.EntityNotFoundException If the target restaurant cannot be found.
     */
    @Transactional
    public List<PhotoDTO> attachPhotosToRestaurant(Long id, List<MultipartFile> files) throws Exception {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("The restaurant for given photos does not exist!"));
        List<String> savedPhotoPaths = fileSystemStorageService.saveRestaurantPhotos(restaurant, files);
        try {
            List<Photo> photos = savedPhotoPaths.stream().map(path ->
                    Photo.builder()
                            .filePath(path)
                            .createdAt(LocalDateTime.now())
                            .review(null)
                            .restaurant(restaurant)
                            .build()
            ).toList();
            return photoRepository.saveAll(photos).stream().map(photoMapper::toDTO).toList();
        } catch (Exception e) {
            savedPhotoPaths.forEach(fileSystemStorageService::deleteFile);
            throw e;
        }
    }

    /**
     * Fetches a physical photo file from the storage as a Spring Resource wrapper.
     * @param id identifier of the target photo record.
     * @return Resource representing the file download target.
     * @throws jakarta.persistence.EntityNotFoundException If the record is missing or storage lookup fails.
     */
    public Resource loadAsResource(Long id) {
        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No photo found with given id!"));
        String path = photo.getFilePath();
        try {
            return fileSystemStorageService.fetchFile(path);
        } catch (Exception e) {
            throw new EntityNotFoundException("Could not fetch photo with given id!");
        }

    }

    /**
     * Purges a photo record by deleting its storage file and removing database entries.
     * @param id identifier of the photo to be deleted.
     * @throws jakarta.persistence.EntityNotFoundException If the target photo record cannot be found.
     */
    public void delete(Long id) {
        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No photo found with given id!"));

        String path = photo.getFilePath();
        try {
            fileSystemStorageService.deleteFile(path);
            photoRepository.delete(photo);
        } catch (Exception e) {
            throw new EntityNotFoundException("Could not delete photo with given id!");
        }
    }
}
