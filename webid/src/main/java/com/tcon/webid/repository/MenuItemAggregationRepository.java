package com.tcon.webid.repository;

import com.tcon.webid.entity.MenuItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MenuItemAggregationRepository {

    @Autowired
    private MongoTemplate template;

    public List<MenuItem> findAllUniqueMenuItemsByNameAndCategory() {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("available").is(true)),                      // Filter only available
                Aggregation.sort(org.springframework.data.domain.Sort.Direction.DESC, "rating"),   // Optional (prefer highly rated as group rep.)
                Aggregation.group("name", "category").first("$$ROOT").as("item"),
                Aggregation.replaceRoot("item")
        );
        AggregationResults<MenuItem> results = template.aggregate(agg, "menu_items", MenuItem.class);
        return results.getMappedResults();
    }
}
