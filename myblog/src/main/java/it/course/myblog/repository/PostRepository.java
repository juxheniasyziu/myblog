package it.course.myblog.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.course.myblog.entity.Post;
import it.course.myblog.entity.Tag;
import it.course.myblog.entity.User;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>{
	
	Optional<Post> findByTitle(String title);
	
	// SELECT * FROM post WHERE visible=1 ORDER BY updated_at DESC LIMIT 0,3
	List<Post> findTop3ByVisibleTrueOrderByUpdatedAtDesc();

	List<Post> findAllByApprovedFalse();
	List<Post> findAllByVisibleFalse();
	List<Post> findAllByVisibleFalseOrApprovedFalse();
	List<Post> findAllByVisibleFalseAndApprovedFalse();
	
	List<Post> findByIdIn(Set<Long> ids);
	
	List<Post> findByAuthorAndVisibleTrue(User author);
	
	Optional<Post> findByIdAndVisibleTrue(Long id);
	
	List<Post> findByTagsIn(List<Tag> tags);
	
	List<Post> findAllByVisibleTrueOrderByAvgRatingDesc();
	
	
	
	
}
