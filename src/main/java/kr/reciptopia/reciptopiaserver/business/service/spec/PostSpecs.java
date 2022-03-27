package kr.reciptopia.reciptopiaserver.business.service.spec;

import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Comment;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import kr.reciptopia.reciptopiaserver.domain.model.Recipe;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Join;

public class PostSpecs {

	public static Specification<Post> titleLike(String title) {
		return (root, query, builder) -> builder.like(root.get("title"), "%" + title + "%");
	}

	public static Specification<Post> isOwner(Long ownerId) {
		return (root, query, builder) -> {
			Join<Post, Account> join = root.joinSet("owner");
			return builder.equal(join.get("id"), ownerId);
		};
	}

	public static Specification<Post> hasRecipe(Long recipeId) {
		return (root, query, builder) -> {
			Join<Post, Recipe> join = root.joinSet("recipe");
			return builder.equal(join.get("id"), recipeId);
		};
	}

	public static Specification<Post> hasComment(Long commentId) {
		return (root, query, builder) -> {
			Join<Post, Comment> join = root.joinSet("comments");
			return builder.equal(join.get("id"), commentId);
		};
	}
}
