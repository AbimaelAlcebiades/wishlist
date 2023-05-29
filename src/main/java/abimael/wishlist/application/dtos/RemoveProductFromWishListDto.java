package abimael.wishlist.application.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RemoveProductFromWishListDto {

  @NotNull(message = "Client id is mandatory")
  private Long clientId;

  @NotNull(message = "Product id is mandatory")
  private Long productId;
}
