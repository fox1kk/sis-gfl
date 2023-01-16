package ru.gf.sis.interaction;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InteractionState {
    STOCK,
    MOD,
    EQUIP,
    STOCK_ANALYSIS,
    MOD_ANALYSIS
}
