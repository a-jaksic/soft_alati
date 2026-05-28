package rs.ac.bg.fon.aleksa_jaksic.sa.photo.controller;

import rs.ac.bg.fon.aleksa_jaksic.sa.photo.dtos.PhotoDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.photo.service.PhotoService;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService){
        this.photoService = photoService;
    }

    @GetMapping("/api/reviews/{id}/photos")
    public ResponseEntity<Object> listReviewPhotos(@PathVariable Long id){
        try{
            List<PhotoDTO> photoList = photoService.listByReview(id);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(photoList);
        }catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error, the retrieval of review photos was not successful!");
        }
    }

    @PreAuthorize("@photoSecurity.isReviewOwner(#id, authentication)")
    @PostMapping(
            value = "/api/reviews/{id}/photos",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Object> uploadReviewPhotos(@PathVariable Long id,@RequestParam("files") List<MultipartFile> files){
        try{
            List<PhotoDTO> addedPhotos = photoService.attachPhotosToReview(id, files);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(addedPhotos);
        }catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Review photos posting unsuccessful!");
        }
    }


    @GetMapping("/api/restaurants/{id}/photos")
    public ResponseEntity<Object> listRestaurantPhotos(@PathVariable Long id) {
        try{
            List<PhotoDTO> photoList = photoService.listByRestaurant(id);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(photoList);
        } catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error, the retrieval of restaurant photos was not successful!");
        }
    }

    @PostMapping(value = "/api/admin/restaurants/{id}/photos",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Object> uploadRestaurantPhotos(@PathVariable Long id, @RequestParam("files") List<MultipartFile> files) {
        try {
            List<PhotoDTO> addedPhotos = photoService.attachPhotosToRestaurant(id, files);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(addedPhotos);
        } catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Restaurant photos posting unsuccessful!" );
        }
    }

    /* ===== Služenje fajla i brisanje ===== */

    @GetMapping("/api/photos/{id}")
    public ResponseEntity<Object> getPhoto(@PathVariable Long id) {
        try {
            Resource res = photoService.loadAsResource(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic())
                    .body(res);
        } catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("The retrieval of photo files was not successful!");
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN') or @photoSecurity.isPhotoOwner(#id, authentication)")
    @DeleteMapping("/api/photos/{id}")
    public ResponseEntity<Object> deletePhoto(@PathVariable Long id) {
        try {
            photoService.delete(id);
            return ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build();
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error, the photo deletion was not successful!");
        }
    }
}
