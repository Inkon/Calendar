package com.calendar.client.ui;

import com.calendar.client.EventBus;
import com.calendar.client.FilterService;
import com.calendar.client.FilterServiceAsync;
import com.calendar.client.event.*;
import com.calendar.shared.dto.FilterDTO;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.*;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.notify.client.constants.NotifyType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterList extends Composite {

    @UiTemplate("FilterList.ui.xml")
    interface FilterListUiBinder extends UiBinder<HTMLPanel, FilterList> {
    }

    @UiField
    LinkedGroup filterList;

    private static FilterListUiBinder ourUiBinder = GWT.create(FilterListUiBinder.class);
    private final FilterServiceAsync filterService = GWT.create(FilterService.class);
    private Map<LinkedGroupItem, FilterDTO> filterByItem = new HashMap<>();

    public FilterList() {
        initWidget(ourUiBinder.createAndBindUi(this));

        EventBus.getInstance().addHandler(SelectFilterInListEvent.TYPE, event -> {
            for (Widget child : filterList) {
                if (child instanceof LinkedGroupItem) {
                    LinkedGroupItem lgi = (LinkedGroupItem) child;
                    FilterDTO mappedFilterDTO = filterByItem.get(lgi);
                    if (event.getSelectedFilter() == null) {
                        lgi.setActive(mappedFilterDTO == null);
                    } else {
                        lgi.setActive(event.getSelectedFilter().equals(mappedFilterDTO));
                    }
                }
            }
        });

        EventBus.getInstance().addHandler(FilterListChangedEvent.TYPE, event -> {
            filterList.clear();
            fillFilterList();
        });

        fillFilterList();
    }

    private void fillFilterList() {
        Icon spinner = UIUtils.createSpinner(IconSize.TIMES3);
        filterList.add(spinner);

        filterService.getAllFilters(new AsyncCallback<List<FilterDTO>>() {
            @Override
            public void onFailure(Throwable throwable) {
                filterList.remove(spinner);
            }

            @Override
            public void onSuccess(List<FilterDTO> filters) {
                EventBus.getInstance().fireEvent(new FilterListLoadedEvent(filters));

                filterList.remove(spinner);
                ClickHandler onFiltersListClick = clickEvent -> {
                    if (clickEvent.getSource() instanceof LinkedGroupItem) {
                        clickEvent.preventDefault();
                        FilterDTO filterDTO = filterByItem.get(clickEvent.getSource());
                        EventBus.getInstance().fireEvent(new SelectFilterInListEvent(filterDTO));
                    }
                };

                LinkedGroupItem allFiltersItem = makeItemByValues("Все фильтры", "FFFFFF", false);
                allFiltersItem.setActive(true);
                allFiltersItem.addClickHandler(onFiltersListClick);
                filterList.add(allFiltersItem);
                filterByItem.put(allFiltersItem, null);

                for (FilterDTO filter : filters) {
                    LinkedGroupItem lgi = makeItemByValues(filter.getDescription(), filter.getColor(), true);
                    lgi.addClickHandler(onFiltersListClick);

                    // Toggle edit mode
                    EventBus.getInstance().addHandler(ToggleEditFiltersModeEvent.TYPE, event -> {
                        for (Widget child : lgi) {
                            if (child instanceof ButtonGroup) {
                                child.setVisible(event.isInEditMode());
                                break;
                            }
                        }
                    });

                    filterList.add(lgi);
                    filterByItem.put(lgi, filter);
                }
            }
        });
    }

    private LinkedGroupItem makeItemByValues(String name, String colorCode, boolean needControls) {
        LinkedGroupItem item = UIUtils.createBasicItem(name, colorCode);

        if (needControls) {
            // Edit/remove buttons
            Button editButton = new Button();
            editButton.setSize(ButtonSize.EXTRA_SMALL);
            editButton.setIcon(IconType.EDIT);
            editButton.addClickHandler(clickEvent -> {
                clickEvent.preventDefault();
                FilterDTO linkedFilter = filterByItem.get(item);
                if (linkedFilter != null) {
                    EventBus.getInstance().fireEvent(new ShowFilterEditModalEvent(linkedFilter));
                }
            });

            Button removeButton = new Button();
            removeButton.setIcon(IconType.REMOVE);
            removeButton.setType(ButtonType.DANGER);
            removeButton.setSize(ButtonSize.EXTRA_SMALL);
            removeButton.addClickHandler(clickEvent -> {
                clickEvent.preventDefault();
                FilterDTO targetFilter = filterByItem.get(item);
                if (targetFilter != null) {
                    EventBus.getInstance().fireEvent(new EventDeletionBegin());

                    filterService.deleteFilter(targetFilter, new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable throwable) {
                            EventBus.getInstance().fireEvent(new EventDeletionEnd());
                            UIUtils.pushNotify("Серверная ошибка.", NotifyType.DANGER);
                        }

                        @Override
                        public void onSuccess(Void aVoid) {
                            EventBus.getInstance().fireEvent(new EventDeletionEnd());
                            UIUtils.pushNotify("Фильтр успешно удалён.", NotifyType.SUCCESS);
                            EventBus.getInstance().fireEvent(new FilterListChangedEvent());
                        }
                    });
                }
            });

            EventBus.getInstance().addHandler(EventDeletionBegin.TYPE, event -> {
                editButton.setEnabled(false);
                removeButton.setEnabled(false);
            });

            EventBus.getInstance().addHandler(EventDeletionEnd.TYPE, event -> {
                editButton.setEnabled(true);
                removeButton.setEnabled(true);
            });

            ButtonGroup btnGroup = new ButtonGroup();
            btnGroup.add(editButton);
            btnGroup.add(removeButton);
            btnGroup.setVisible(false);
            btnGroup.addStyleName("filterControls");
            item.add(btnGroup);
        }

        return item;
    }
}