package kr.reciptopia.reciptopiaserver.domain.model;

import kr.reciptopia.reciptopiaserver.domain.error.exception.CommentNotFoundException;
import kr.reciptopia.reciptopiaserver.domain.error.exception.LikeTagNotFoundException;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.CascadeType.REMOVE;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@ToString
@With
@Entity
public class Post extends TimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @NotNull
    @ToString.Exclude
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "account_id")
    private Account owner;

    //    @NotNull
    @ToString.Exclude
    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @NotNull
    @ToString.Exclude
    @OneToMany(mappedBy = "post", cascade = ALL, orphanRemoval = true)
    private Set<Comment> comments = new HashSet<>();

    @NotNull
    @ToString.Exclude
    @OneToMany(mappedBy = "post", cascade = REMOVE, orphanRemoval = true)
    private Set<PostLikeTag> postLikeTags = new HashSet<>();

    @ElementCollection
    private List<String> pictureUrls = new ArrayList<>();

    @NotEmpty
    private String title;

    private String content;

    @NotNull
    private Long views;

    @Builder
    public Post(Recipe recipe, Account owner, @Singular List<String> pictureUrls, String title,
                String content) {
        setRecipe(recipe);
        setOwner(owner);
        this.pictureUrls = pictureUrls;
        this.title = title;
        this.content = content;
        this.views = 0L;
    }

    public Post withPictureUrl(String pictureUrl) {
        return Post.builder()
                .title(title)
                .content(content)
                .pictureUrls(pictureUrls)
                .pictureUrl(pictureUrl)
                .build();
    }

    public void setOwner(Account owner) {
        if (this.owner != owner) {
            if (this.owner != null)
                this.owner.removePost(this);
            this.owner = owner;
            if (owner != null) {
                owner.addPost(this);
            }
        }
    }

    public void setRecipe(Recipe recipe) {
        if (this.recipe != recipe) {
            if (this.recipe != null)
                this.recipe.setPost(null);
            this.recipe = recipe;
            if (recipe != null)
                recipe.setPost(this);
        }
    }

    public void addComment(Comment comment) {
        comments.add(comment);
        if (!this.equals(comment.getPost())) {
            comment.setPost(this);
        }
    }

    public void removeComment(Comment comment) {
        if (!comments.contains(comment))
            throw new CommentNotFoundException();

        comments.remove(comment);
        if (this.equals(comment.getPost())) {
            comment.setPost(null);
        }
    }

    public void removeComments() {
        comments.forEach(comment -> removeComment(comment));
    }

    public void addLikeTag(PostLikeTag likeTag) {
        postLikeTags.add(likeTag);
        if (!this.equals(likeTag.getPost())) {
            likeTag.setPost(this);
        }
    }

    public void removeLikeTag(PostLikeTag likeTag) {
        if (!postLikeTags.contains(likeTag))
            throw new LikeTagNotFoundException();

        postLikeTags.remove(likeTag);
        if (this.equals(likeTag.getPost())) {
            likeTag.setPost(null);
        }
    }

    public void removeLikeTags() {
        this.postLikeTags.forEach(this::removeLikeTag);
    }

    public void removeAllCollections() {
        removeComments();
        removeLikeTags();
    }
}
