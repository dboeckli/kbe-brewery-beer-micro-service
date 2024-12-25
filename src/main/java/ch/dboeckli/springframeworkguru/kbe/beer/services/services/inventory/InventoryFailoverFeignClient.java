package ch.dboeckli.springframeworkguru.kbe.beer.services.services.inventory;

import ch.dboeckli.springframeworkguru.kbe.beer.services.dto.BeerInventoryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * Created by jt on 3/14/20.
 */
@FeignClient(name = "inventory-failover")
public interface InventoryFailoverFeignClient {
    @RequestMapping(method = RequestMethod.GET, value = "/inventory-failover")
    ResponseEntity<List<BeerInventoryDto>> getOnhandInventory();
}
