package com.lvt.apps.myvote.ms.dtos;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.lvt.apps.myvote.ms.utils.EmptyObjectSerializer;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
@JsonSerialize(using = EmptyObjectSerializer.class)
public class EmptyObject {
}
