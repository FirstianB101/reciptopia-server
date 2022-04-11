package kr.reciptopia.reciptopiaserver.domain.model;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.CascadeType.REMOVE;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import kr.reciptopia.reciptopiaserver.domain.error.exception.LikeTagNotFoundException;
import kr.reciptopia.reciptopiaserver.domain.error.exception.ReplyNotFoundException;
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
public class Comment extends TimeEntity {

    @Id
    @Column(name = "comment_id")
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "account_id")
    private Account owner;

    @ToString.Exclude
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @NotBlank
    @Size(min = 1, max = 50, message = "content는 1 ~ 50자 이여야 합니다!")
    private String content;

    @NotNull
    @ToString.Exclude
    @OneToMany(mappedBy = "comment", cascade = ALL, orphanRemoval = true)
    private Set<Reply> replies = new HashSet<>();

    @NotNull
    @ToString.Exclude
    @OneToMany(mappedBy = "comment", cascade = REMOVE, orphanRemoval = true)
    private Set<CommentLikeTag> commentLikeTags = new HashSet<>();

    @Builder
    public Comment(Account owner, Post post, String content) {
        setOwner(owner);
        setPost(post);
        setContent(content);
    }

    public void setOwner(Account owner) {
        if (this.owner != owner) {
            if (this.owner != null && owner != null)
                this.owner.removeComment(this);
            this.owner = owner;
            if (owner != null) {
                owner.addComment(this);
            }
        }
    }

    public void setPost(Post post) {
        if (this.post != post) {
            if (this.post != null && post != null)
                this.post.removeComment(this);
            this.post = post;
            if (post != null) {
                post.addComment(this);
            }
        }
    }

    public void addLikeTag(CommentLikeTag likeTag) {
        commentLikeTags.add(likeTag);
        if (!this.equals(likeTag.getComment())) {
            likeTag.setComment(this);
        }
    }

    public void removeLikeTag(CommentLikeTag likeTag) {
        if (!commentLikeTags.contains(likeTag))
            throw new LikeTagNotFoundException();

        commentLikeTags.remove(likeTag);
        if (this.equals(likeTag.getComment())) {
            likeTag.setComment(null);
        }
    }

    public void removeLikeTags() {
        commentLikeTags.forEach(this::removeLikeTag);
    }

    public void addReply(Reply reply) {
        replies.add(reply);
        if (!this.equals(reply.getComment())) {
            reply.setComment(this);
        }
    }

    public void removeReply(Reply reply) {
        if (!replies.contains(reply))
            throw new ReplyNotFoundException();

        replies.remove(reply);
        if (this.equals(reply.getComment())) {
            reply.setComment(null);
        }
    }

    public void removeReplies() {
        replies.forEach(this::removeReply);
    }

    public void removeAllCollections() {
        removeLikeTags();
        removeReplies();
    }
}
