package abimael.wishlist.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WishList implements Serializable {

  private int totalProductionInList;
  private List<Long> productsIds;

  public int getTotalProductionInList() {
    return productsIds.size();
  }
}
