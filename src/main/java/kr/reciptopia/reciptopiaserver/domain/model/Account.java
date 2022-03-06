package kr.reciptopia.reciptopiaserver.domain.model;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.CascadeType.REMOVE;
import static javax.persistence.GenerationType.IDENTITY;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import kr.reciptopia.reciptopiaserver.domain.error.exception.PostNotFoundException;
import kr.reciptopia.reciptopiaserver.domain.error.exception.CommentNotFoundException;
import kr.reciptopia.reciptopiaserver.domain.error.exception.FavoriteNotFoundException;
import kr.reciptopia.reciptopiaserver.domain.error.exception.SearchHistoryNotFoundException;
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
public class Account extends TimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "account_id")
    private Long id;

    @Email(message = "이메일 형식이 아닙니다.")
    private String email;

    @NotEmpty
    @Size(min = 8, max = 16, message = "password는 8 ~ 16자 이여야 합니다!")
    private String password;

    @NotBlank
    @Size(min = 5, max = 16, message = "nickname은 5 ~ 16자 이여야 합니다!")
    private String nickname;

    @NotNull
    @ToString.Exclude
    @OneToMany(mappedBy = "owner", cascade = ALL, orphanRemoval = true)
    private Set<Post> posts = new HashSet<>();

    @NotNull
    @ToString.Exclude
    @OneToMany(mappedBy = "owner", cascade = ALL, orphanRemoval = true)
    private Set<Comment> comments = new HashSet<>();

    @NotNull
    @ToString.Exclude
    @OneToMany(mappedBy = "owner", cascade = ALL, orphanRemoval = true)
    private Set<Reply> replies = new HashSet<>();

    @NotNull
    @ToString.Exclude
    @OneToMany(mappedBy = "owner", cascade = REMOVE, orphanRemoval = true)
    private Set<PostLikeTag> postLikeTags = new HashSet<>();

    @NotNull
    @ToString.Exclude
    @OneToMany(mappedBy = "owner", cascade = REMOVE, orphanRemoval = true)
    private Set<CommentLikeTag> commentLikeTags = new HashSet<>();

    @NotNull
    @ToString.Exclude
    @OneToMany(mappedBy = "owner", cascade = REMOVE, orphanRemoval = true)
    private Set<ReplyLikeTag> replyLikeTags = new HashSet<>();

    @NotNull
    @ToString.Exclude
    @OneToMany(mappedBy = "owner", cascade = ALL, orphanRemoval = true)
    private Set<Favorite> favorites = new HashSet<>();

    @NotNull
    @ToString.Exclude
    @OneToMany(mappedBy = "owner", cascade = ALL, orphanRemoval = true)
    private Set<SearchHistory> searchHistories = new HashSet<>();

    private String profilePictureUrl;

    @Builder
    public Account(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }


    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public void addPost(Post post) {
        posts.add(post);
        if (!this.equals(post.getOwner())) {
            post.setOwner(this);
        }
    }

    public void removePost(Post post) {
        if (!posts.contains(post))
            throw new PostNotFoundException();

        posts.remove(post);
        if (this.equals(post.getOwner())) {
            post.setOwner(null);
        }
    }

    public void addComment(Comment comment) {
        comments.add(comment);
        if (!this.equals(comment.getOwner())) {
            comment.setOwner(this);
        }
    }

    public void removeComment(Comment comment) {
        if (!comments.contains(comment))
            throw new CommentNotFoundException();

        comments.remove(comment);
        if (this.equals(comment.getOwner())) {
            comment.setOwner(null);
        }
    }

    public void addReply(Reply reply) {
        replies.add(reply);
        if (!this.equals(reply.getOwner())) {
            reply.setOwner(this);
        }
    }

    public void removeReply(Reply reply) {
        if (!replies.contains(reply))
            throw new ReplyNotFoundException();

        replies.remove(reply);
        if (this.equals(reply.getOwner())) {
            reply.setOwner(null);
        }
    }

    public void addLikeTag(LikeTag likeTag) {
        if (likeTag instanceof PostLikeTag) {
            postLikeTags.add((PostLikeTag) likeTag);
        }
        else if (likeTag instanceof CommentLikeTag) {
            commentLikeTags.add((CommentLikeTag) likeTag);
        }
        else if (likeTag instanceof ReplyLikeTag) {
            replyLikeTags.add((ReplyLikeTag) likeTag);
        }

        if (!this.equals(likeTag.getOwner())) {
            likeTag.setOwner(this);
        }
    }

    public void removeLikeTag(LikeTag likeTag) {
        if (likeTag instanceof PostLikeTag) {
            if (!postLikeTags.contains(likeTag))
                throw new LikeTagNotFoundException();

            postLikeTags.remove(likeTag);
        }
        else if (likeTag instanceof CommentLikeTag) {
            if (!commentLikeTags.contains(likeTag))
                throw new LikeTagNotFoundException();

            commentLikeTags.remove(likeTag);
        }
        else if (likeTag instanceof ReplyLikeTag) {
            if (!replyLikeTags.contains(likeTag))
                throw new LikeTagNotFoundException();

            replyLikeTags.remove(likeTag);
        }

        if (this.equals(likeTag.getOwner())) {
            likeTag.setOwner(null);
        }
    }

    public void addFavorite(Favorite favorite) {
        favorites.add(favorite);
        if (!this.equals(favorite.getOwner())) {
            favorite.setOwner(this);
        }
    }

    public void removeFavorite(Favorite favorite) {
        if (!favorites.contains(favorite))
            throw new FavoriteNotFoundException();

        favorites.remove(favorite);
    }

    public void addSearchHistory(SearchHistory searchHistory) {
        searchHistories.add(searchHistory);
        if (!this.equals(searchHistory.getOwner())) {
            searchHistory.setOwner(this);
        }
    }

    public void removeSearchHistory(SearchHistory searchHistory) {
        if (!searchHistories.contains(searchHistory))
            throw new SearchHistoryNotFoundException();

        searchHistories.remove(searchHistory);
    }
}
