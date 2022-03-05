package kr.reciptopia.reciptopiaserver.domain.model;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.CascadeType.REMOVE;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import kr.reciptopia.reciptopiaserver.domain.error.exception.CommentNotFoundException;
import kr.reciptopia.reciptopiaserver.domain.error.exception.LikeTagNotFoundException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@ToString
@With
@Entity
public class Board extends TimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "board_id")
    private Long id;

    @NotNull
    @ToString.Exclude
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "account_id")
    private Account owner;

    @NotNull
    @ToString.Exclude
    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @NotNull
    @ToString.Exclude
    @OneToMany(mappedBy = "board", cascade = ALL, orphanRemoval = true)
    private Set<Comment> comments = new HashSet<>();

    @NotNull
    @ToString.Exclude
    @OneToMany(mappedBy = "board", cascade = REMOVE, orphanRemoval = true)
    private Set<BoardLikeTag> boardLikeTags = new HashSet<>();

    @ElementCollection
    private List<String> pictureUrls = new ArrayList<>();

    @NotEmpty
    private String title;

    private String content;

    private Long views;

    @Builder
    public Board(Recipe recipe, Account owner, List<String> pictureUrls, String title,
        String content) {
        setRecipe(recipe);
        setOwner(owner);
        this.pictureUrls = pictureUrls;
        this.title = title;
        this.content = content;
    }

    public void setOwner(Account owner) {
        if (this.owner != owner) {
            if (this.owner != null)
                this.owner.removeBoard(this);
            this.owner = owner;
            if (owner != null) {
                owner.addBorad(this);
            }
        }
    }

    public void setRecipe(Recipe recipe) {
        if (this.recipe != recipe) {
            if (this.recipe != null)
                this.recipe.setBoard(null);
            this.recipe = recipe;
            if (recipe != null)
                recipe.setBoard(this);
        }
    }

    public void addComment(Comment comment) {
        comments.add(comment);
        if (!this.equals(comment.getBoard())) {
            comment.setBoard(this);
        }
    }

    public void removeComment(Comment comment) {
        if (!comments.contains(comment))
            throw new CommentNotFoundException();

        comments.remove(comment);
        if (this.equals(comment.getBoard())) {
            comment.setBoard(null);
        }
    }

    public void addLikeTag(BoardLikeTag likeTag) {
        boardLikeTags.add(likeTag);
        if (!this.equals(likeTag.getBoard())) {
            likeTag.setBoard(this);
        }
    }

    public void removeLikeTag(BoardLikeTag likeTag) {
        if (!boardLikeTags.contains(likeTag))
            throw new LikeTagNotFoundException();

        boardLikeTags.remove(likeTag);
        if (this.equals(likeTag.getBoard())) {
            likeTag.setBoard(null);
        }
    }
}
