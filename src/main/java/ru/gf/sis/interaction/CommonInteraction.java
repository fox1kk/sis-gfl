package ru.gf.sis.interaction;

import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.spec.EmbedCreateSpec;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.util.ObjectUtils;
import ru.gf.sis.dto.StoredInfo;
import ru.gf.sis.dto.analysis.Analytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class CommonInteraction extends BaseSlideInteraction {
  private StoredInfo entity;
  private InteractionState state;

  public static final String CUSTOM_ID_PREFIXES__STOCK = "stock-";
  public static final String CUSTOM_ID_PREFIXES__MOD_ANALYSIS = "mod-analysis-";
  public static final String CUSTOM_ID_PREFIXES__MOD = "mod-";
  public static final String CUSTOM_ID_PREFIXES__SPEC = "spec-";
  public static final String CUSTOM_ID_PREFIXES__ANALYSIS = "analysis-";

  public void updateState(InteractionState state) {
    this.state = state;
    this.currentPage = 1;
  }

  public EmbedCreateSpec getContent() {
    switch (state) {
      case STOCK:
        return entity.fillFullEmbed(language, false);
      case MOD:
        return entity.fillFullEmbed(language, true);
      case EQUIP:
        return entity
            .getSpecialEquipment()
            .get(currentPage - 1)
            .fillSmallEmbed(language, currentPage + "/" + entity.getSpecialEquipment().size());
      case STOCK_ANALYSIS:
        return getAnalysis(false).get().fillAnalysisEmbed(currentPage - 1);
      case MOD_ANALYSIS:
        return getAnalysis(true).get().fillAnalysisEmbed(currentPage - 1);
      default:
        throw new IllegalStateException("Unexpected value: " + state);
    }
  }

  public List<LayoutComponent> getButtons() {
    List<Button> buttons = new ArrayList<>();
    List<Button> slideButtons = new ArrayList<>();

    switch (state) {
      case STOCK:
        addModButton(buttons);
        addEquipButton(buttons);
        addAnalysisButton(buttons);
        addModAnalysisButton(buttons);
        break;
      case MOD:
        addStockButton(buttons);
        addEquipButton(buttons);
        addAnalysisButton(buttons);
        addModAnalysisButton(buttons);
        break;
      case EQUIP:
        addSlideButtons(slideButtons);
        addStockButton(buttons);
        addModButton(buttons);
        addAnalysisButton(buttons);
        addModAnalysisButton(buttons);
        break;
      case STOCK_ANALYSIS:
        addSlideButtons(slideButtons);
        addStockButton(buttons);
        addModButton(buttons);
        addEquipButton(buttons);
        addModAnalysisButton(buttons);
        break;
      case MOD_ANALYSIS:
        addSlideButtons(slideButtons);
        addStockButton(buttons);
        addModButton(buttons);
        addEquipButton(buttons);
        addAnalysisButton(buttons);
        break;
    }

    if (ObjectUtils.isEmpty(slideButtons) && ObjectUtils.isEmpty(buttons)) {
      return Collections.emptyList();
    }

    if (ObjectUtils.isEmpty(slideButtons)) {
      return List.of(ActionRow.of(buttons));
    }

    return List.of(ActionRow.of(slideButtons), ActionRow.of(buttons));
  }

  public void slideRight() {
    List<?> slides = defineSlides();
    if (currentPage >= slides.size()) {
      return;
    }
    currentPage++;
  }

  public void slideLeft() {
    if (currentPage <= 1) {
      return;
    }
    currentPage--;
  }

  private void addSlideButtons(List<Button> buttons) {
    buttons.addAll(getSlideButtons(defineSlides()));
  }

  private void addStockButton(List<Button> buttons) {
    buttons.add(Button.primary(CUSTOM_ID_PREFIXES__STOCK + interactionId, "Stock"));
  }

  private void addModButton(List<Button> buttons) {
    if (entity.isMod()) {
      buttons.add(Button.primary(CUSTOM_ID_PREFIXES__MOD + interactionId, "Mod"));
    }
  }

  private void addEquipButton(List<Button> buttons) {
    if (!ObjectUtils.isEmpty(entity.getSpecialEquipment())) {
      buttons.add(Button.primary(CUSTOM_ID_PREFIXES__SPEC + interactionId, "SPEQ"));
    }
  }

  private void addAnalysisButton(List<Button> buttons) {
    if (!ObjectUtils.isEmpty(entity.getAnalysis())) {
      if (getAnalysis(false).isPresent()) {
        buttons.add(Button.primary(CUSTOM_ID_PREFIXES__ANALYSIS + interactionId, "Analysis"));
      }
    }
  }

  private void addModAnalysisButton(List<Button> buttons) {
    if (!ObjectUtils.isEmpty(entity.getAnalysis())) {
      if (getAnalysis(true).isPresent()) {
        buttons.add(
            Button.primary(CUSTOM_ID_PREFIXES__MOD_ANALYSIS + interactionId, "Mod Analysis"));
      }
    }
  }

  private List<?> defineSlides() {
    List<?> slides = Collections.emptyList();
    switch (state) {
      case EQUIP:
        slides = entity.getSpecialEquipment();
        break;
      case STOCK_ANALYSIS:
        if (getAnalysis(false).isPresent()) {
          slides = getAnalysis(false).get().getSplitAnalysis();
        }
        break;
      case MOD_ANALYSIS:
        if (getAnalysis(true).isPresent()) {
          slides = getAnalysis(true).get().getSplitAnalysis();
        }
        break;
    }
    return slides;
  }

  private Optional<Analytics> getAnalysis(Boolean mod) {
    return entity.getAnalysis().stream().filter(an -> mod == an.isMod()).findFirst();
  }
}
