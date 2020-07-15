package it.course.myblog.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import it.course.myblog.entity.Post;
import it.course.myblog.entity.Report;
import it.course.myblog.entity.ReportCompositeKey;
import it.course.myblog.entity.User;

@Repository
public interface ReportRepository extends JpaRepository<Report, ReportCompositeKey>{
	
	
	List<Report> findByReportNoteIsNullOrderByCreatedAtAsc();
	
	List<Report> findAllByOrderByCreatedAtDesc();
	
	List<Report> findByReportCompositeKeyReporter(User u);
	
	Long countByReportCompositeKeyReportedPost(Post p);
	Long countByReportCompositeKeyReporter(User u);

	
	
}
