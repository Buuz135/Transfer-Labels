package com.buuz135.transfer_labels.filter;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class FilterType {

    public static final List<FilterType> FILTERS = new ArrayList<>();

    public static final FilterType NORMAL = new FilterType("normal", "filter.type.normal", "filter.type.normal.tooltip");
    public static final FilterType REGULATING = new FilterType("regulating", "filter.type.regulating", "filter.type.regulating.tooltip");
    public static final FilterType EXACT_COUNT = new FilterType("exact_count", "filter.type.exact_count", "filter.type.exact_count.tooltip");
    public static final FilterType MOD = new FilterType("mod", "filter.type.mod", "filter.type.mod.tooltip");
    public static final FilterType TAG = new FilterType("tag", "filter.type.tag", "filter.type.tag.tooltip");

    public static FilterType getByName(String name) {
        for (FilterType filterType : FILTERS) {
            if (filterType.getName().equalsIgnoreCase(name)) return filterType;
        }
        return NORMAL;
    }

    public static FilterType getNext(String currentName) {
        FilterType current = getByName(currentName);
        int currentIndex = FILTERS.indexOf(current);
        int nextIndex = (currentIndex + 1) % FILTERS.size();
        return FILTERS.get(nextIndex);
    }

    public static FilterType getPrevious(String currentName) {
        FilterType current = getByName(currentName);
        int currentIndex = FILTERS.indexOf(current);
        int previousIndex = (currentIndex - 1 + FILTERS.size()) % FILTERS.size();
        return FILTERS.get(previousIndex);
    }

    private final String name;
    private final String displayName;
    private final String extraTooltip;

    public FilterType(String name, String displayName, String extraTooltip) {
        this.name = name;
        this.displayName = displayName;
        this.extraTooltip = extraTooltip;
        FILTERS.add(this);
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getExtraTooltip() {
        return extraTooltip;
    }

    public List<String> getTooltip() {
        return List.of(ChatFormatting.YELLOW + Component.translatable(displayName).getString(), "   " + ChatFormatting.GRAY + Component.translatable(extraTooltip).getString());
    }


}
