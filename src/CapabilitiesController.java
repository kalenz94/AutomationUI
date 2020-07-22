import base.BaseController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import model.Capability;
import model.CapabilitySet;
import utils.PreferencesUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

public class CapabilitiesController extends BaseController {
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab addNewTab;
    @FXML
    private Tab savedTab;
    @FXML
    private ListView<AddRow> addCapList;
    @FXML
    private ListView<SaveRow> savedCapList;
    @FXML
    private Button addCapBtn;
    @FXML
    private TextArea jsonTa;
    @FXML
    private Button saveAsBtn;
    @FXML
    private Button startSessionBtn;

    private List<Capability> selectedCapabilities = new ArrayList<>();

    public void initialize() {
        ObservableList<AddRow> addItems = FXCollections.observableArrayList();
        addItems.add(newAddRow());
        addCapList.setItems(addItems);

        List<CapabilitySet> savedCap = loadCapabilities();
        savedCapList.getItems().clear();
        for (CapabilitySet capSet : savedCap) {
            savedCapList.getItems().add(newSaveRow(capSet));
        }

        addCapBtn.setOnMouseClicked(event -> {
            addCapList.getItems().add(newAddRow());
        });
        startSessionBtn.setOnMouseClicked(event -> {
            openStage("fxml/device_interact.fxml", "Device interaction", Modality.APPLICATION_MODAL, selectedCapabilities);
        });
        saveAsBtn.setOnMouseClicked(event -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setHeaderText("Input capability set's name:");
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                CapabilitySet cs = new CapabilitySet();
                cs.setName(result.get());
                cs.setCapabilities(getAddNewCapabilities());
                SaveRow sr = newSaveRow(cs);
                savedCapList.getItems().add(sr);
                addCapList.getItems().clear();
                tabPane.getSelectionModel().select(savedTab);
                saveCapabilities();
            }
        });

        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == addNewTab) {
                addCapBtn.setVisible(true);
                saveAsBtn.setVisible(true);
            } else {
                addCapBtn.setVisible(false);
                saveAsBtn.setVisible(false);
            }
        });
    }

    private void saveCapabilities() {
        List<CapabilitySet> saveCap = new ArrayList<>();
        for (SaveRow row : savedCapList.getItems()) {
            saveCap.add(row.getCapabilitySet());
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(bos);
            oos.writeObject(saveCap);
            oos.flush();
            byte[] b = bos.toByteArray();
            oos.close();
            String s = Base64.getEncoder().encodeToString(b);
            PreferencesUtils.save(PreferencesUtils.KEY_CAPABILITIES, s);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(e.getMessage(), false);
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(e.getMessage(), false);
            }
        }
    }

    private List<CapabilitySet> loadCapabilities() {
        List<CapabilitySet> savedCap = new ArrayList<>();
        String s = PreferencesUtils.load(PreferencesUtils.KEY_CAPABILITIES);
        if (s == null) return savedCap;

        byte[] b = Base64.getDecoder().decode(s);
        ByteArrayInputStream bis = new ByteArrayInputStream(b);
        ObjectInputStream in;
        try {
            in = new ObjectInputStream(bis);
            savedCap = (List<CapabilitySet>) in.readObject();
            in.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            showAlert(e.getMessage(), false);
        } finally {
            try {
                bis.close();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(e.getMessage(), false);
            }
        }
        return savedCap;
    }

    private String generateJsonPreview(List<Capability> capabilities) {
        selectedCapabilities = capabilities;
        StringBuilder json = new StringBuilder();
        json.append("{\n");

        for (Capability capability : capabilities) {
            json.append("    \"");
            json.append(capability.getName());
            json.append("\" = \"");
            json.append(capability.getValue());
            json.append("\"\n");
        }

        json.append("}");
        return json.toString();
    }

    private void refreshJsonAddNew() {
        jsonTa.setText(generateJsonPreview(getAddNewCapabilities()));
    }

    private List<Capability> getAddNewCapabilities() {
        List<Capability> capabilities = new ArrayList<>();
        for (AddRow addRow : addCapList.getItems()) {
            capabilities.add(addRow.getCapability());
        }
        return capabilities;
    }

    private AddRow newAddRow() {
        AddRow addRow = new AddRow();
        addRow.setOnAddRowChangedListener(new OnAddRowChangedListener() {

            @Override
            public void onCapabilityInputChanged(Capability capability) {
                refreshJsonAddNew();
            }

            @Override
            public void onRowDeleted(AddRow addRow) {
                addCapList.getItems().remove(addRow);
            }
        });
        return addRow;
    }

    private static class AddRow extends HBox {
        private TextField nameTf;
        private TextField valueTf;
        private Button delBtn;
        private Capability capability;
        private OnAddRowChangedListener listener;

        AddRow() {
            super();
            Parent item = null;
            capability = new Capability();
            try {
                item = FXMLLoader.load(getClass().getResource("fxml/capabilities_add_new_list_item.fxml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            nameTf = (TextField) item.lookup("#addNameTf");
            valueTf = (TextField) item.lookup("#addValueTf");
            delBtn = (Button) item.lookup("#addDelBtn");

            nameTf.textProperty().addListener((obs, oldText, newText) -> {
                capability.setName(newText);
                if (listener != null) {
                    listener.onCapabilityInputChanged(capability);
                }
            });
            valueTf.textProperty().addListener((obs, oldText, newText) -> {
                capability.setValue(newText);
                if (listener != null) {
                    listener.onCapabilityInputChanged(capability);
                }
            });
            delBtn.setOnMouseClicked(event -> {
                listener.onRowDeleted(AddRow.this);
            });
            this.getChildren().add(item);
        }

        public Capability getCapability() {
            return capability;
        }

        public void setOnAddRowChangedListener(OnAddRowChangedListener listener) {
            this.listener = listener;
        }

    }

    private interface OnAddRowChangedListener {

        void onCapabilityInputChanged(Capability capability);

        void onRowDeleted(AddRow addRow);
    }

    private SaveRow newSaveRow(CapabilitySet capabilitySet) {
        SaveRow saveRow = new SaveRow(capabilitySet);
        saveRow.setOnSavedRowChangedListener(new OnSavedRowChangedListener() {

            @Override
            public void onRowSelected(CapabilitySet capabilitySet) {
                jsonTa.setText(generateJsonPreview(capabilitySet.getCapabilities()));
            }

            @Override
            public void onRowEditClicked(CapabilitySet capabilitySet) {
                addCapList.getItems().clear();
                for (Capability cap : capabilitySet.getCapabilities()) {
                    AddRow row = newAddRow();
                    row.nameTf.setText(cap.getName());
                    row.valueTf.setText(cap.getValue());
                    addCapList.getItems().add(row);
                }
                tabPane.getSelectionModel().select(addNewTab);
                refreshJsonAddNew();
            }

            @Override
            public void onRowDeleted(SaveRow saveRow) {
                savedCapList.getItems().remove(saveRow);
                saveCapabilities();
            }
        });
        return saveRow;
    }

    private static class SaveRow extends HBox {
        private Label nameLb;
        private Button editBtn;
        private Button delBtn;
        CapabilitySet capabilitySet;
        OnSavedRowChangedListener listener;

        SaveRow(CapabilitySet capabilitySet) {
            super();
            this.capabilitySet = capabilitySet;
            Parent item = null;
            try {
                item = FXMLLoader.load(getClass().getResource("fxml/capabilities_saved_list_item.fxml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            nameLb = (Label) item.lookup("#savedNameLb");
            editBtn = (Button) item.lookup("#savedEditBtn");
            delBtn = (Button) item.lookup("#savedDelBtn");

            nameLb.setText(capabilitySet.getName());

            item.setOnMouseClicked(event -> {
                if (listener != null) {
                    listener.onRowSelected(capabilitySet);
                }
            });
            editBtn.setOnMouseClicked(event -> {
                if (listener != null) {
                    listener.onRowEditClicked(capabilitySet);
                }
            });
            delBtn.setOnMouseClicked(event -> {
                if (listener != null) {
                    listener.onRowDeleted(SaveRow.this);
                }
            });
            this.getChildren().add(item);
        }

        public CapabilitySet getCapabilitySet() {
            return capabilitySet;
        }

        public void setOnSavedRowChangedListener(OnSavedRowChangedListener listener) {
            this.listener = listener;
        }

    }

    private interface OnSavedRowChangedListener {
        void onRowSelected(CapabilitySet capabilitySet);

        void onRowEditClicked(CapabilitySet capabilitySet);

        void onRowDeleted(SaveRow saveRow);
    }

}
