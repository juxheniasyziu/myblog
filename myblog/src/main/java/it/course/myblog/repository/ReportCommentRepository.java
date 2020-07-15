package it.course.myblog.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import it.course.myblog.entity.Comment;
import it.course.myblog.entity.ReportComment;
import it.course.myblog.entity.ReportCommentCompositeKey;
import it.course.myblog.entity.User;

@Repository
public interface ReportCommentRepository extends JpaRepository<ReportComment, ReportCommentCompositeKey>{
	
	
	List<ReportComment> findByReportNoteIsNullOrderByCreatedAtAsc();
	
	List<ReportComment> findAllByOrderByCreatedAtDesc();
	
	List<ReportComment> findByReportCommentCompositeKeyReporter(User u);
	
	Long countByReportCommentCompositeKeyReportedComment(Comment c);
	Long countByReportCommentCompositeKeyReporter(User u);

	
}
