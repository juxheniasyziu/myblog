package it.course.myblog.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import it.course.myblog.entity.Post;
import it.course.myblog.entity.Rating;
import it.course.myblog.entity.RatingUserPostCompositeKey;

@Repository
public interface RatingRepository extends JpaRepository<Rating, RatingUserPostCompositeKey>{
	
	List<Rating> findByRatingUserPostCompositeKeyPost(Post p);
	
	@Query(value="SELECT AVG(rating) as rating FROM rating WHERE post_id= ?1", nativeQuery=true)
	double getAvgByPost(Post post);
	
	
	long countByRatingUserPostCompositeKeyPost(Post p);

}
