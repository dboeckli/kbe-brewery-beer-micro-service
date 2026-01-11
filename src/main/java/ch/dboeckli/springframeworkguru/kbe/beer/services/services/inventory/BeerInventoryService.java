package ch.dboeckli.springframeworkguru.kbe.beer.services.services.inventory;

import java.util.UUID;

public interface BeerInventoryService {

    Integer getOnhandInventory(UUID beerId);
}
