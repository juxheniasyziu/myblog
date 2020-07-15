package it.course.myblog.entity;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.JoinColumn;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.NaturalId;

import com.fasterxml.jackson.annotation.JsonIgnore;

import it.course.myblog.entity.Authority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="USER")
@Data @AllArgsConstructor @NoArgsConstructor
public class User {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@NaturalId(mutable=true)
	@Email
	@NotBlank
	@Size(min=6, max=120)
	@Column(length=120, nullable=false)
	private String email;
	
	@NotBlank
	@Size(min=4, max=15)
	@Column(length=15, unique=true, nullable=false)
	private String username;
	
	@NotBlank
	@Column(length=100, nullable=false)
	@Size(min=5, max=100)
	private String password;
	
	@Column(name="IS_ENABLED", columnDefinition="TINYINT(1)")
	private Boolean enabled = true;
	
	@Column(name="CREATED_AT", 
			updatable=false, insertable=false, 
			columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private Date createdAt;
	
	@Column(name="UPDATED_AT",
			updatable=true, insertable=false, 
			columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private Date updatedAt;
	
	@ManyToMany(fetch=FetchType.EAGER)
	@JoinTable(name="USER_AUTHORITIES",
		joinColumns = {@JoinColumn(name="USER_ID", referencedColumnName="ID")},
		inverseJoinColumns = {@JoinColumn(name="AUTHORITY_ID", referencedColumnName="ID")})
	private Set<Authority> authorities;
	
	@ManyToMany(fetch=FetchType.EAGER)
	@JoinTable(name="USER_FOLLOWERS",
		joinColumns = {@JoinColumn(name="FOLLOWER", referencedColumnName="ID")},
		inverseJoinColumns = {@JoinColumn(name="FOLLOWED", referencedColumnName="ID")})
	private Set<User> followers;
	
	@Size(max=64)
	private String identifierCode;
	
	public User(String email, String username, String password, Set<Authority> authorities) {
		super();
		this.email = email;
		this.username = username;
		this.password = password;
		this.authorities = authorities;
	}
	
	public User(String email, String username, String password) {
		super();
		this.email = email;
		this.username = username;
		this.password = password;
		
	}
	
	public User(String email, String username, String password, Set<Authority> authorities, Set<User> followers) {
		super();
		this.email = email;
		this.username = username;
		this.password = password;
		this.authorities = authorities;
		this.followers = followers;
	}


}
