package com.fakezone.fakezone.ui.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.Route;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.icon.VaadinIcon;
import ApplicationLayer.Enums.PCategory;


@Route(value = "search", layout = MainLayout.class)
public class SearchResultsView extends VerticalLayout {

    public SearchResultsView() {
        Button filterButton = new Button(VaadinIcon.FILTER.create());
        filterButton.addClickListener(e -> openFilterDialog());

        add(filterButton); // Add to the top of results view

        // TODO: Add logic to render results based on query/filter params
    }

    private void openFilterDialog() {
        Dialog filterDialog = new Dialog();
        VerticalLayout layout = new VerticalLayout();
        filterDialog.open();
        NumberField minPrice = new NumberField("Min Price");
        NumberField maxPrice = new NumberField("Max Price");

        ComboBox<Integer> productRating = new ComboBox<>("Product Rating", List.of(1, 2, 3, 4, 5));
        productRating.setItemLabelGenerator(r -> "★".repeat(r) + "☆".repeat(5 - r));

        ComboBox<Integer> storeRating = new ComboBox<>("Store Rating", List.of(1, 2, 3, 4, 5));
        storeRating.setItemLabelGenerator(r -> "★".repeat(r) + "☆".repeat(5 - r));

        ComboBox<String> category = new ComboBox<>("Category");
        category.setItems(Arrays.stream(PCategory.values())
                        .map(Enum::name)
                        .collect(Collectors.toList()));

        Button apply = new Button("Apply Filters", e -> {
            // presenter.onFiltersApplied(
            //     minPrice.getValue(),
            //     maxPrice.getValue(),
            //     productRating.getValue(),
            //     storeRating.getValue(),
            //     category.getValue()
            // );
            filterDialog.close();
        });

        layout.add(minPrice, maxPrice, productRating, storeRating, category, apply);
        filterDialog.add(layout);
        filterDialog.open();
    
    }
    //TODO Optional: add fucntion to system service to get categories and add them to the combo box
    // public List<String> getAllCategories() {
    //     return Arrays.stream(PCategory.values())
    //                  .map(Enum::name)
    //                  .collect(Collectors.toList());
    // }
}
