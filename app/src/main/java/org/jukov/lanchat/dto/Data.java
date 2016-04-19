package org.jukov.lanchat.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Created by jukov on 07.04.2016.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = MessagingData.class, name = "MessagingData"),
        @JsonSubTypes.Type(value = ServiceData.class, name = "ServiceData")
})
public abstract class Data {

}
