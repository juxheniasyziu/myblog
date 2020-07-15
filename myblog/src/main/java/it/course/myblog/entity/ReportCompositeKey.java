package it.course.myblog.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data @AllArgsConstructor @NoArgsConstructor
public class ReportCompositeKey implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@NotNull
	@ManyToOne
	@JoinColumn(name="REPORTER")
	private User reporter;
	
	@NotNull
	@ManyToOne
	@JoinColumn(name="REPORTED_POST")
	private Post reportedPost;

}
