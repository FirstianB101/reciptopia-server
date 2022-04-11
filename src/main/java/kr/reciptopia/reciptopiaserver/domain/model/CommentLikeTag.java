package kr.reciptopia.reciptopiaserver.domain.model;

import static javax.persistence.FetchType.LAZY;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
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
public class CommentLikeTag extends LikeTag {

    @ToString.Exclude
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "comment_id")
    @NotNull
    private Comment comment;

    @Builder
    public CommentLikeTag(Account owner, Comment comment) {
        setOwner(owner);
        setComment(comment);
    }

    public void setComment(Comment comment) {
        if (this.comment != comment) {
            if (this.comment != null && comment != null)
                this.comment.removeLikeTag(this);
            this.comment = comment;
            if (comment != null) {
                comment.addLikeTag(this);
            }
        }
    }
}
