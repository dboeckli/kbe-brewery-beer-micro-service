package ch.dboeckli.springframeworkguru.kbe.beer.services.services.inventory;

import ch.guru.springframework.kbe.lib.dto.BeerInventoryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class InventoryFailoverService implements InventoryServiceFeignClient {
    private final InventoryFailoverFeignClient inventoryFailoverFeignClient;

    @Override
    public ResponseEntity<List<BeerInventoryDto>> getOnhandInventory(UUID beerId) {
        log.info("Calling Inventory Failover for Beer Id: " + beerId);
        return inventoryFailoverFeignClient.getOnhandInventory();
    }
}
