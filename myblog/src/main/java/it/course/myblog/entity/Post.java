package it.course.myblog.entity;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import it.course.myblog.payload.request.PostRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="POST")
@Data @AllArgsConstructor @NoArgsConstructor
public class Post {
		
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable=false, unique=true)
	@Size(min=1, max=80)
	private String title;
	
	@Column(nullable=true)
	private String overview;
	
	@Column(nullable=false, columnDefinition="TEXT")
	private String content;
	
	@Column(name="CREATED_AT", 
			updatable=false, insertable=false, 
			columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private Date createdAt;
	
	@Column(name="UPDATED_AT",
			updatable=true, insertable=false, 
			columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private Date updatedAt;
	
	@Column(name="IS_APPROVED", columnDefinition="TINYINT(1)")
	private Boolean approved = false;
	
	@Column(name="IS_VISIBLE", columnDefinition="TINYINT(1)")
	private Boolean visible = false;
	
	@ManyToOne
	@JoinColumn(name="USER_ID", nullable=false)
	private User author;
	
	@OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "db_file_id")
    private DBFile dbFile;
	
	@Column(name = "avg_rating", columnDefinition="DECIMAL(3,2)")
	private Double avgRating = 0.00;
	
	@ManyToMany(fetch=FetchType.EAGER)
	@JoinTable(name="POST_TAGS",
		joinColumns = {@JoinColumn(name="POST_ID", referencedColumnName="ID")},
		inverseJoinColumns = {@JoinColumn(name="TAG_ID", referencedColumnName="ID")})
	private Set<Tag> tags;
	
	@ManyToMany(fetch=FetchType.EAGER)
	@JoinTable(name="POST_COMMENTS",
		joinColumns = {@JoinColumn(name="POST_ID", referencedColumnName="ID")},
		inverseJoinColumns = {@JoinColumn(name="COMMENT_ID", referencedColumnName="ID")})
	private List<Comment> comments;

	
	public static Post createFromRequest(PostRequest postRequest) {
		return new Post(
			postRequest.getTitle(),
			postRequest.getOverview(),
			postRequest.getContent()				
			);	
	}

	public Post(String title, String overview, String content) {
		super();
		this.title = title;
		this.overview = overview;
		this.content = content;
	}

	
	
}
