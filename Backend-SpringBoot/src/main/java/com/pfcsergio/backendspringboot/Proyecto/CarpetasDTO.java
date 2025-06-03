package com.pfcsergio.backendspringboot.Proyecto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class CarpetasDTO {

    @Getter
    @Setter
    @JsonProperty("nombreCarpeta")
    private String nombreCarpeta;

    @Setter
    @Getter
    @JsonProperty("archivosCarpeta")
    private List<String> archivosCarpeta;

}
