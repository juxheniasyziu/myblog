package it.course.myblog.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="REPORT")
@Data @AllArgsConstructor @NoArgsConstructor
public class Report implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private ReportCompositeKey reportCompositeKey;
	
	@NotBlank
	private String reportReason;
	
	@Column(name="CREATED_AT", 
			updatable=false, insertable=false, 
			columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private Date createdAt;
	
	private String reportNote;

}
