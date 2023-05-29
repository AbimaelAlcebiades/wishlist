package abimael.wishlist.domain.repositories;

import abimael.wishlist.domain.models.ClientWishList;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface WishListRepository extends MongoRepository<ClientWishList, String> {

  Optional<ClientWishList> findByClientId(Long clientId);
}
