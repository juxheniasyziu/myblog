package it.course.myblog.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import it.course.myblog.entity.Post;
import it.course.myblog.entity.PostViewed;

@Repository
public interface PostViewedRepository extends JpaRepository<PostViewed, Long>{
	
	List<PostViewed> findByCreatedAtBetween(Date from, Date to);
	
	@Query(value="SELECT id, post_id, ip, created_at, viewer, COUNT(post_id) AS total_view "
			+ "FROM post_viewed "
			//+ "WHERE created_at BETWEEN DATE_FORMAT(?1, '%Y-%m-%d 00:00:00') and DATE_FORMAT(?2, '%Y-%m-%d 23:59:59') "
			+ "WHERE created_at BETWEEN ?1 AND ?2 "
			+ "GROUP BY post_id "
			+ "ORDER BY total_view DESC", 
		nativeQuery=true)
	List<PostViewed> postViewedGroupedByPost(Date from, Date to);
	
	
	@Query(value="WITH a1 AS " 
			+ "(SELECT pv.id, pv.post_id, pv.viewer, pv.ip, pv.created_at, COUNT(pv.post_id) " 
			+ "		FROM myblog.post_viewed pv "
			+ "		WHERE pv.post_id=?3 "
			+ "		AND created_at BETWEEN ?1 AND ?2 "
			+ "		GROUP BY pv.ip, pv.viewer) " 
			+ " SELECT a1.id, a1.post_id, a1.ip, a1.created_at, a1.viewer, COUNT(a1.post_id) AS total_view FROM a1",
		nativeQuery=true)
	PostViewed postViewedByPost(Date from, Date to, Post p);
	
	
	boolean existsByPostAndViewerAndIpAndCreatedAtBetween(Post p, String viewer, String ip, Date from, Date to);

	//Long countByPostId(Long postId);
	
	

}
