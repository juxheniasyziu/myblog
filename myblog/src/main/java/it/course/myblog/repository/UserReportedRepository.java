package it.course.myblog.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import it.course.myblog.entity.UserReported;
import it.course.myblog.entity.UserReportedId;

public interface UserReportedRepository extends JpaRepository<UserReported, UserReportedId>{
	
	Optional<UserReported> findByUserReportedId(UserReportedId urId);
	

}
