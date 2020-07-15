package it.course.myblog.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="POST_VIEWED")
@Data @AllArgsConstructor @NoArgsConstructor
public class PostViewed {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotNull
	@ManyToOne
	@JoinColumn(name="POST_ID")
	private Post post;
	
	@NotBlank
	private String ip;
	
	@Column(name="CREATED_AT", 
			updatable=false, insertable=false, 
			columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private Date createdAt;
	
	@NotBlank
	@Size(min=4, max=15)
	private String viewer = "anonymous";
	
	@NotNull
	private Long totalView = 0L;

	public PostViewed(Post post, String ip, String viewer, Long totalView) {
		super();
		this.post = post;
		this.ip = ip;
		this.viewer = viewer;
		this.totalView = totalView;
	}
	
	
	

}
