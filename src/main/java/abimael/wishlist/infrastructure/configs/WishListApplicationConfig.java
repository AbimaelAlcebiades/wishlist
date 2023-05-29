package abimael.wishlist.infrastructure.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Component
@Data
@ConfigurationProperties(prefix = "app")
public class WishListApplicationConfig {
  private int maxItemsInWishList;
}
