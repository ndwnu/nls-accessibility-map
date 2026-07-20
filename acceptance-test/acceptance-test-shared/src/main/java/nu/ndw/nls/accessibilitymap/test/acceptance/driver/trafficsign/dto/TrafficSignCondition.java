package nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.dto;

import java.util.Set;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.deserializer.CommaDelimitedEnumSetDeserializer;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionPropertiesDtoV5Json.CategoryEnum;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionPropertiesDtoV5Json.VehicleTypeEnum;
import tools.jackson.databind.annotation.JsonDeserialize;

public record TrafficSignCondition(String name,
                                   @JsonDeserialize(using = CommaDelimitedEnumSetDeserializer.class) Set<VehicleTypeEnum> vehicleType,
                                   @JsonDeserialize(using = CommaDelimitedEnumSetDeserializer.class) Set<CategoryEnum> category,
                                   String timeValidity,
                                   Integer emissionClass,
                                   String fuelType,
                                   Double lengthInM,
                                   Double widthInM,
                                   Double heightInM,
                                   Double weightInTon,
                                   Double axleWeightInTon) { }


