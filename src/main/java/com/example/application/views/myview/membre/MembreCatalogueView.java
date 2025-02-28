package com.example.application.views.myview.membre;

import com.example.application.entity.DTO.CopyDto;
import com.example.application.entity.DTO.ItemDto;
import com.example.application.entity.DTO.LoanSettingDto;
import com.example.application.entity.DTO.ReservationDto;
import com.example.application.entity.DTO.SpecialLimitDto;
import com.example.application.entity.DTO.UserDto;
import com.example.application.entity.SpecialLimit;
import com.example.application.entity.User;
import com.example.application.objectcustom.MoisOption;
import com.example.application.security.Roles;
import com.example.application.service.implementation.*;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.icon.Icon;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.*;

@PageTitle("Catalogue des Documents")
@Route(value = "member/catalogue", layout = MainLayout.class)
@RolesAllowed("ROLE_MEMBRE")
public class MembreCatalogueView extends VerticalLayout {

    public MembreCatalogueView() {

    }
}