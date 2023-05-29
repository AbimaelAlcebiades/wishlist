package abimael.wishlist.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@AllArgsConstructor
@Builder
@Document(collection = "clients-wish-lists")
@NoArgsConstructor
public class ClientWishList implements Serializable {

  @Id private String id;

  private Long clientId;

  private WishList wishList;

  private String createdAt;

  private String updatedAt;
}
