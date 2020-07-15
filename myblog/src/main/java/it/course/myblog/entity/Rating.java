package it.course.myblog.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Check;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="RATING")
@Data @AllArgsConstructor @NoArgsConstructor
//@Check(constraints="RATING >= 1 AND RATING < 5.1")
public class Rating implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private RatingUserPostCompositeKey ratingUserPostCompositeKey;
	
	@NotNull
	@Digits(integer=1, fraction=2)  
	//@Column(columnDefinition="DECIMAL(3,2)") // 1.65
	private double rating;

	public Rating(RatingUserPostCompositeKey ratingUserPostCompositeKey) {
		super();
		this.ratingUserPostCompositeKey = ratingUserPostCompositeKey;
	}
	
}
