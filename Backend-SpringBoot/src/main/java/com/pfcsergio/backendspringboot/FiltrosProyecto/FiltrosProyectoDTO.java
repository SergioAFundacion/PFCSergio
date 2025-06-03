package com.pfcsergio.backendspringboot.FiltrosProyecto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class FiltrosProyectoDTO {

    @JsonProperty("id")
    private Integer id;

    @Getter
    @Setter
    @JsonProperty("nombre")
    private String nombre;

    @Getter
    @Setter
    @JsonProperty("nombreAntiguo")
    private String nombreAntiguo;

    @Setter
    @Getter
    @JsonProperty("archivos")
    private List<String> archivos;

}
