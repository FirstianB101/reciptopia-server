package kr.reciptopia.reciptopiaserver.helper;

import kr.reciptopia.reciptopiaserver.domain.dto.PostDto.Update;
import kr.reciptopia.reciptopiaserver.domain.model.Post;

import java.util.ArrayList;
import java.util.List;

import static kr.reciptopia.reciptopiaserver.helper.AccountHelper.anAccount;

public class PostHelper {

	private static final String ARBITRARY_TITLE = "가문어 오징어 튀김 만들기";
	private static final String ARBITRARY_CONTENT = "가문어 오징어 다리를 잘라서 튀긴다.";

	public static List<String> getArbitraryPictureUrl() {
		List<String> arbitraryPictureUrls = new ArrayList<>();
		arbitraryPictureUrls.add("C:\\Users\\eunsung\\Desktop\\temp\\picture");
		arbitraryPictureUrls.add("C:\\Users\\tellang\\Desktop\\temp\\picture");
		return arbitraryPictureUrls;
	}

	// 임의의 지정된 필드 값으로 채워진 Post 반환
	public static Post aPost() {
		Post post = Post.builder()
				.title(ARBITRARY_TITLE)
				.content(ARBITRARY_CONTENT)
				.pictureUrls(getArbitraryPictureUrl())
				.build();
		post.setId(0L);
		post.setOwner(anAccount());
//		post.setRecipe(anRecipe());
		return post;
	}

	public static Update aPostUpdateDto() {
		return Update.builder()
				.title(ARBITRARY_TITLE)
				.content(ARBITRARY_CONTENT)
				.pictureUrls(getArbitraryPictureUrl())
				.build();
	}


}
