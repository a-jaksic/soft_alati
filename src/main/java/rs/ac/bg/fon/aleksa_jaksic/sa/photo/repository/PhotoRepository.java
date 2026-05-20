package rs.ac.bg.fon.aleksa_jaksic.sa.photo.repository;

import rs.ac.bg.fon.aleksa_jaksic.sa.photo.domain.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhotoRepository extends JpaRepository<Photo, Long> {

    List<Photo> findByReviewId(Long id);

    List<Photo> findByRestaurantId(Long id);
}
