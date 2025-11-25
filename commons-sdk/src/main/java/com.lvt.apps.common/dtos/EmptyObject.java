package com.lvt.apps.common.dtos;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.lvt.apps.common.utils.EmptyObjectSerializer;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
@JsonSerialize(using = EmptyObjectSerializer.class)
public class EmptyObject {
}
