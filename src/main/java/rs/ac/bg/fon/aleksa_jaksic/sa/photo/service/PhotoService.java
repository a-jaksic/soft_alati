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
