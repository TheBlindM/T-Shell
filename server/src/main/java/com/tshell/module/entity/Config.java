package com.tshell.module.entity;

import com.tshell.config.JsonToMapConverter;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author TheBlind
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Config extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy=GenerationType.TABLE,generator="table_gen")
    @TableGenerator(
            name = "table_gen",
            initialValue = 50, //初始化值
            allocationSize=3 //累加值
    )
    private Long id;



    @Convert( converter = JsonToMapConverter.class)
    @Column(columnDefinition = "text")
    private Terminal terminal;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Terminal {
        String fontFamily;
        Integer fontSize;
        Integer fontWeight;
        Integer fontWeightBold;
        boolean enableFontLigatures;
        String cursorShape;
        boolean cursorBlink;
        Integer scrollbackLines;
        String wordSeparator;
        boolean copyOnSelect;
        String rendererType;
    }


}
