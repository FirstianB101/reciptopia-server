package kr.reciptopia.reciptopiaserver.business.service;

import java.util.List;
import kr.reciptopia.reciptopiaserver.business.service.authorizer.AbstractAuthorizer;
import kr.reciptopia.reciptopiaserver.business.service.helper.RepositoryHelper;
import kr.reciptopia.reciptopiaserver.business.service.helper.ServiceErrorHelper;
import kr.reciptopia.reciptopiaserver.domain.dto.PostDto.Create;
import kr.reciptopia.reciptopiaserver.domain.dto.PostDto.Result;
import kr.reciptopia.reciptopiaserver.domain.dto.PostDto.Update;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import kr.reciptopia.reciptopiaserver.persistence.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PostService {

	private final PasswordEncoder passwordEncoder;
	private final PostRepository postRepository;
	private final RepositoryHelper repoHelper;
	private final ServiceErrorHelper errorHelper;
	private final AbstractAuthorizer authorizer;

	@Autowired

	public PostService(PasswordEncoder passwordEncoder,
		PostRepository postRepository,
		RepositoryHelper repoHelper,
		ServiceErrorHelper errorHelper,
		AbstractAuthorizer authorizer) {
		this.passwordEncoder = passwordEncoder;
		this.postRepository = postRepository;
		this.repoHelper = repoHelper;
		this.errorHelper = errorHelper;
		this.authorizer = authorizer;
	}

	@Transactional
	public Result create(Create dto, Authentication authentication) {
		Post post = dto.asEntity();

		Account dtoOwner = repoHelper.findAccountOrThrow(dto.getOwnerId());
		authorizer.requireByOneself(authentication, dtoOwner);

		post.setOwner(dtoOwner);

//		Recipe dtoRecipe = repoHelper.findRecipeOrThrow(dto.getRecipeId());
//
//		post.setRecipe(dtoRecipe);

		return Result.of(postRepository.save(post));
	}

	public Result read(Long id) {
		return Result.of(repoHelper.findPostOrThrow(id));
	}

	public List<Result> search(Specification<Post> spec, Pageable pageable) {
		Page<Post> entities = postRepository.findAll(spec, pageable);
		return Result.of(entities);
	}

	@Transactional
	public Result update(Long id, Update dto, Authentication authentication) {
		Post entity = repoHelper.findPostOrThrow(id);
		authorizer.requireByOneself(authentication, entity.getOwner());

		if (dto.getPictureUrls() != null) {
			entity.setPictureUrls(dto.getPictureUrls());
		}
		if (dto.getTitle() != null) {
			entity.setTitle(dto.getTitle());
		}
		if (dto.getContent() != null) {
			entity.setContent(dto.getContent());
		}

		return Result.of(postRepository.save(entity));
	}

	@Transactional
	public void delete(Long id, Authentication authentication) {
		Post post = repoHelper.findPostOrThrow(id);
		authorizer.requireByOneself(authentication, post.getOwner());
		post.removeAllCollections();

		postRepository.delete(post);
	}
}
