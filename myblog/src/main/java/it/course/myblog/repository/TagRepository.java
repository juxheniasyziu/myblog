package it.course.myblog.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.course.myblog.entity.Tag;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long>{
	
	List<Tag> findByTagNameIn(Set<String> tagNames);
	//Set<Tag> findByTagNameIn(Set<String> tagNames);
	
	List<Tag> findAllByOrderByTagNameAsc();
	
	List<Tag> findAllByVisibleTrueOrderByTagNameAsc();
	List<Tag> findAllByVisibleFalseOrderByTagNameAsc();
	
	Optional<Tag> findByTagName(String tagName);

}
