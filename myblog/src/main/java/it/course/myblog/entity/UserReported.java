package it.course.myblog.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="USER_REPORTED")
@Data @AllArgsConstructor @NoArgsConstructor
public class UserReported implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private UserReportedId userReportedId;
	
	@Column(name="UPDATED_AT",
			updatable=true, insertable=false, 
			columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private Date updatedAt;
	
	@NotNull
	@Column(name="REPORT_NUMBER", columnDefinition="TINYINT(1)")
	private int reportNumber;

	public UserReported(UserReportedId userReportedId, int reportNumber) {
		super();
		this.userReportedId = userReportedId;
		this.reportNumber = reportNumber;
	}

	

	
}
