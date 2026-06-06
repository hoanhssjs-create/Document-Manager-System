package com.documentmanager.controller;

import com.documentmanager.model.ActivityLog;
import com.documentmanager.model.DashboardStats;
import com.documentmanager.model.Document;
import com.documentmanager.model.Folder;
import com.documentmanager.model.Tag;
import com.documentmanager.service.AuthService;
import com.documentmanager.service.DashboardService;
import com.documentmanager.service.DocumentService;
import com.documentmanager.service.FolderService;
import com.documentmanager.service.TagService;
import com.documentmanager.util.AlertUtil;
import com.documentmanager.util.FormatUtil;
import com.documentmanager.util.SessionManager;
import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class AppController {
    private final Stage stage;
    private final AuthService authService = new AuthService();
    private final DashboardService dashboardService = new DashboardService();
    private final DocumentService documentService = new DocumentService();
    private final FolderService folderService = new FolderService();
    private final TagService tagService = new TagService();
    private final StackPane content = new StackPane();
    private final VBox sidebar = new VBox(8);
    private TextField globalSearch;
    private PauseTransition globalSearchDelay;

    public AppController(Stage stage) {
        this.stage = stage;
    }

    public Parent createLoginView() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("page");

        VBox card = new VBox(16);
        card.setMaxWidth(420);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER_LEFT);

        Label logo = new Label("D");
        logo.getStyleClass().add("brand-mark");
        logo.setMinSize(44, 44);
        logo.setMaxSize(44, 44);

        Label title = new Label("Document Manager System");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Sign in to manage your documents");
        subtitle.getStyleClass().add("muted");

        TextField username = new TextField();
        username.setPromptText("Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        Button login = new Button("Login");
        login.getStyleClass().add("primary-button");
        login.setMaxWidth(Double.MAX_VALUE);
        login.setOnAction(event -> run("Login failed", () -> {
            authService.login(username.getText(), password.getText());
            stage.getScene().setRoot(createMainView());
            showDashboard();
        }));

        Button register = new Button("Create new account");
        register.getStyleClass().add("secondary-button");
        register.setMaxWidth(Double.MAX_VALUE);
        register.setOnAction(event -> stage.getScene().setRoot(createRegisterView()));

        card.getChildren().addAll(logo, title, subtitle, field("Username", username), field("Password", password), login, register);
        root.setCenter(card);
        BorderPane.setAlignment(card, Pos.CENTER);
        return root;
    }

    private Parent createRegisterView() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("page");
        VBox card = new VBox(14);
        card.setMaxWidth(460);
        card.getStyleClass().add("card");

        Label title = new Label("Create Account");
        title.getStyleClass().add("page-title");
        TextField fullName = new TextField();
        fullName.setPromptText("Full name");
        TextField email = new TextField();
        email.setPromptText("Email");
        TextField username = new TextField();
        username.setPromptText("Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        Button create = new Button("Register");
        create.getStyleClass().add("primary-button");
        create.setMaxWidth(Double.MAX_VALUE);
        create.setOnAction(event -> run("Registration failed", () -> {
            authService.register(username.getText(), password.getText(), fullName.getText(), email.getText());
            AlertUtil.info("Success", "Account created. Please login.");
            stage.getScene().setRoot(createLoginView());
        }));

        Button back = new Button("Back to login");
        back.getStyleClass().add("secondary-button");
        back.setMaxWidth(Double.MAX_VALUE);
        back.setOnAction(event -> stage.getScene().setRoot(createLoginView()));

        card.getChildren().addAll(title, field("Full name", fullName), field("Email", email), field("Username", username), field("Password", password), create, back);
        root.setCenter(card);
        return root;
    }

    private Parent createMainView() {
        BorderPane root = new BorderPane();
        root.setTop(topBar());
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(240);
        root.setLeft(sidebar);
        root.setCenter(content);
        buildSidebar();
        return root;
    }

    private HBox topBar() {
        HBox bar = new HBox(16);
        bar.getStyleClass().add("top-bar");
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setMinHeight(64);

        Label mark = new Label("D");
        mark.getStyleClass().add("brand-mark");
        mark.setMinSize(36, 36);
        mark.setMaxSize(36, 36);
        Label title = new Label("DOCUMENT MANAGER");
        title.getStyleClass().add("app-title");

        globalSearch = new TextField();
        globalSearch.setPromptText("Search documents, tags, folders...");
        globalSearch.setMinHeight(40);
        HBox.setHgrow(globalSearch, Priority.ALWAYS);
        globalSearchDelay = new PauseTransition(Duration.millis(350));
        globalSearch.textProperty().addListener((obs, oldValue, newValue) -> {
            globalSearchDelay.stop();
            globalSearchDelay.setOnFinished(event -> showDocuments(newValue));
            globalSearchDelay.playFromStart();
        });

        Label avatar = new Label("U");
        avatar.getStyleClass().add("brand-mark");
        avatar.setMinSize(36, 36);
        avatar.setMaxSize(36, 36);
        Label user = new Label(SessionManager.currentUser().map(u -> u.fullName()).orElse("User"));
        user.getStyleClass().add("muted");

        bar.getChildren().addAll(mark, title, globalSearch, avatar, user);
        return bar;
    }

    private void buildSidebar() {
        sidebar.getChildren().setAll(
                nav("Dashboard", this::showDashboard),
                nav("Documents", () -> showDocuments("")),
                nav("Folders", this::showFolders),
                nav("Tags", this::showTags),
                nav("Upload File", this::showUpload),
                nav("Activity Logs", this::showActivityLogs),
                nav("Profile", this::showProfile),
                spacer(),
                nav("Logout", this::logout)
        );
    }

    private Button nav(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("sidebar-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(event -> {
            sidebar.getChildren().stream()
                    .filter(Button.class::isInstance)
                    .map(Button.class::cast)
                    .forEach(b -> b.getStyleClass().remove("sidebar-button-active"));
            button.getStyleClass().add("sidebar-button-active");
            action.run();
        });
        return button;
    }

    private Region spacer() {
        Region region = new Region();
        VBox.setVgrow(region, Priority.ALWAYS);
        return region;
    }

    private void showDashboard() {
        run("Cannot load dashboard", () -> {
            DashboardStats stats = dashboardService.stats();
            GridPane grid = new GridPane();
            grid.setHgap(16);
            grid.setVgap(16);
            grid.add(statCard("Documents", String.valueOf(stats.totalDocuments())), 0, 0);
            grid.add(statCard("Folders", String.valueOf(stats.totalFolders())), 1, 0);
            grid.add(statCard("Tags", String.valueOf(stats.totalTags())), 2, 0);
            grid.add(statCard("Storage", FormatUtil.fileSize(stats.totalStorageBytes())), 3, 0);

            VBox logs = new VBox(10);
            logs.getStyleClass().add("card");
            logs.getChildren().add(sectionTitle("Recent Activities"));
            for (ActivityLog log : dashboardService.recentActivities()) {
                logs.getChildren().add(new Label(log.action() + " - " + log.description() + " - " + FormatUtil.dateTime(log.logTime())));
            }
            setPage("Dashboard", grid, logs);
        });
    }

    private VBox statCard(String label, String value) {
        VBox card = new VBox(6);
        card.getStyleClass().add("card");
        card.setMinWidth(180);
        Label number = new Label(value);
        number.getStyleClass().add("stat-number");
        Label caption = new Label(label);
        caption.getStyleClass().add("muted");
        card.getChildren().addAll(number, caption);
        return card;
    }

    private void showDocuments(String keyword) {
        run("Cannot load documents", () -> {
            TextField search = new TextField(keyword == null ? "" : keyword);
            search.setPromptText("Search documents...");
            ComboBox<String> typeFilter = new ComboBox<>(FXCollections.observableArrayList("All types", "PDF", "DOCX", "TXT"));
            typeFilter.getSelectionModel().selectFirst();
            ComboBox<Folder> folderFilter = new ComboBox<>(FXCollections.observableArrayList(folderService.list()));
            folderFilter.setPromptText("All folders");
            ComboBox<Tag> tagFilter = new ComboBox<>(FXCollections.observableArrayList(tagService.list()));
            tagFilter.setPromptText("All tags");
            TableView<Document> table = new TableView<>();
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
            table.getColumns().add(column("Title", Document::title));
            table.getColumns().add(column("Type", Document::fileType));
            table.getColumns().add(column("Size", d -> FormatUtil.fileSize(d.fileSize())));
            table.getColumns().add(column("Created", d -> FormatUtil.dateTime(d.createdDate())));
            TableColumn<Document, String> actions = new TableColumn<>("Actions");
            actions.setCellValueFactory(data -> new SimpleStringProperty("Actions"));
            actions.setCellFactory(col -> new TableCell<>() {
                private final Button detail = new Button("Detail");
                private final Button edit = new Button("Edit");
                private final Button open = new Button("Open");
                private final Button delete = new Button("Delete");
                private final HBox box = new HBox(8, detail, edit, open, delete);
                {
                    detail.getStyleClass().add("secondary-button");
                    edit.getStyleClass().add("secondary-button");
                    open.getStyleClass().add("secondary-button");
                    delete.getStyleClass().add("danger-button");
                    detail.setOnAction(event -> showDocumentDetail(getTableView().getItems().get(getIndex())));
                    edit.setOnAction(event -> showDocumentEditor(getTableView().getItems().get(getIndex())));
                    open.setOnAction(event -> run("Cannot open document", () -> documentService.open(getTableView().getItems().get(getIndex()))));
                    delete.setOnAction(event -> run("Cannot delete document", () -> {
                        Document document = getTableView().getItems().get(getIndex());
                        if (confirm("Delete document", "Delete \"" + document.title() + "\"?")) {
                            documentService.delete(document);
                            showDocuments(search.getText());
                        }
                    }));
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : box);
                }
            });
            table.getColumns().add(actions);

            PauseTransition searchDelay = new PauseTransition(Duration.millis(300));
            Runnable refresh = () -> run("Cannot refresh documents", () -> {
                String selectedType = typeFilter.getValue();
                Folder folder = folderFilter.getValue();
                Tag tag = tagFilter.getValue();
                table.setItems(FXCollections.observableArrayList(documentService.search(
                        search.getText(),
                        selectedType == null || selectedType.equals("All types") ? null : selectedType,
                        folder == null ? null : folder.folderId(),
                        tag == null ? null : tag.tagId()
                )));
            });
            search.textProperty().addListener((obs, oldValue, newValue) -> {
                searchDelay.stop();
                searchDelay.setOnFinished(event -> refresh.run());
                searchDelay.playFromStart();
            });
            typeFilter.setOnAction(event -> refresh.run());
            folderFilter.setOnAction(event -> refresh.run());
            tagFilter.setOnAction(event -> refresh.run());
            refresh.run();

            Button upload = new Button("Upload File");
            upload.getStyleClass().add("primary-button");
            upload.setOnAction(event -> showUpload());
            Button clear = new Button("Clear Filters");
            clear.getStyleClass().add("secondary-button");
            clear.setOnAction(event -> {
                search.clear();
                typeFilter.getSelectionModel().selectFirst();
                folderFilter.setValue(null);
                tagFilter.setValue(null);
                refresh.run();
            });
            HBox filters = new HBox(10, search, typeFilter, folderFilter, tagFilter, clear, upload);
            filters.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(search, Priority.ALWAYS);
            setPage("Documents", filters, table);
        });
    }

    private void showFolderDocuments(Folder folder) {
        run("Cannot load folder documents", () -> {
            TableView<Document> table = documentTable(
                    documentService.search("", null, folder.folderId(), null),
                    () -> showFolderDocuments(folder)
            );
            Label description = new Label("Files stored in folder: " + folder.folderName());
            description.getStyleClass().add("muted");
            Button back = new Button("Back to Folders");
            back.getStyleClass().add("secondary-button");
            back.setOnAction(event -> showFolders());
            setPage("Folder Files", description, back, table);
        });
    }

    private void showTagDocuments(Tag tag) {
        run("Cannot load tag documents", () -> {
            TableView<Document> table = documentTable(
                    documentService.search("", null, null, tag.tagId()),
                    () -> showTagDocuments(tag)
            );
            Label description = new Label("Files assigned to tag: " + tag.tagName());
            description.getStyleClass().add("muted");
            Button back = new Button("Back to Tags");
            back.getStyleClass().add("secondary-button");
            back.setOnAction(event -> showTags());
            setPage("Tagged Files", description, back, table);
        });
    }

    private TableView<Document> documentTable(List<Document> documents, Runnable refreshAfterDelete) {
        TableView<Document> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.getColumns().add(column("Title", Document::title));
        table.getColumns().add(column("Type", Document::fileType));
        table.getColumns().add(column("Size", document -> FormatUtil.fileSize(document.fileSize())));
        table.getColumns().add(column("Created", document -> FormatUtil.dateTime(document.createdDate())));
        TableColumn<Document, String> actions = new TableColumn<>("Actions");
        actions.setCellValueFactory(data -> new SimpleStringProperty("Actions"));
        actions.setCellFactory(col -> new TableCell<>() {
            private final Button detail = new Button("Detail");
            private final Button edit = new Button("Edit");
            private final Button open = new Button("Open");
            private final Button delete = new Button("Delete");
            private final HBox box = new HBox(8, detail, edit, open, delete);
            {
                detail.getStyleClass().add("secondary-button");
                edit.getStyleClass().add("secondary-button");
                open.getStyleClass().add("secondary-button");
                delete.getStyleClass().add("danger-button");
                detail.setOnAction(event -> showDocumentDetail(getTableView().getItems().get(getIndex())));
                edit.setOnAction(event -> showDocumentEditor(getTableView().getItems().get(getIndex())));
                open.setOnAction(event -> run("Cannot open document", () -> documentService.open(getTableView().getItems().get(getIndex()))));
                delete.setOnAction(event -> run("Cannot delete document", () -> {
                    Document document = getTableView().getItems().get(getIndex());
                    if (confirm("Delete document", "Delete \"" + document.title() + "\"?")) {
                        documentService.delete(document);
                        refreshAfterDelete.run();
                    }
                }));
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
        table.getColumns().add(actions);
        table.setItems(FXCollections.observableArrayList(documents));
        return table;
    }

    private void showDocumentEditor(Document document) {
        run("Cannot load document editor", () -> {
            TextField title = new TextField(document.title());
            TextArea description = new TextArea(document.description() == null ? "" : document.description());
            description.setPrefRowCount(4);
            ComboBox<Folder> folders = new ComboBox<>(FXCollections.observableArrayList(folderService.list()));
            folderService.list().stream()
                    .filter(folder -> document.folderId() != null && folder.folderId() == document.folderId())
                    .findFirst()
                    .ifPresent(folders::setValue);
            Button save = new Button("Save Changes");
            save.getStyleClass().add("primary-button");
            save.setOnAction(event -> run("Cannot update document", () -> {
                Folder folder = folders.getValue();
                documentService.update(document, title.getText(), description.getText(), folder == null ? null : folder.folderId());
                AlertUtil.info("Success", "Document updated.");
                showDocuments(globalSearch == null ? "" : globalSearch.getText());
            }));
            Button back = new Button("Back");
            back.getStyleClass().add("secondary-button");
            back.setOnAction(event -> showDocuments(globalSearch == null ? "" : globalSearch.getText()));
            VBox form = new VBox(14, field("Title", title), field("Description", description), field("Folder", folders), new HBox(10, save, back));
            form.getStyleClass().add("card");
            form.setMaxWidth(680);
            setPage("Edit Document", form);
        });
    }

    private void showDocumentDetail(Document document) {
        run("Cannot load document detail", () -> {
            FlowPane assigned = new FlowPane(8, 8);
            ComboBox<Tag> tags = new ComboBox<>(FXCollections.observableArrayList(tagService.list()));
            Runnable refreshTags = () -> run("Cannot refresh document tags", () -> {
                assigned.getChildren().clear();
                for (Tag tag : tagService.listByDocument(document.documentId())) {
                    Label chip = new Label(tag.tagName());
                    chip.getStyleClass().add("tag-chip");
                    Button remove = new Button("Remove");
                    remove.getStyleClass().add("secondary-button");
                    remove.setOnAction(event -> run("Cannot remove tag", () -> {
                        tagService.removeFromDocument(document.documentId(), tag);
                        refreshDocumentDetail(document);
                    }));
                    assigned.getChildren().add(new HBox(6, chip, remove));
                }
            });
            Button assign = new Button("Assign Tag");
            assign.getStyleClass().add("primary-button");
            assign.setOnAction(event -> run("Cannot assign tag", () -> {
                tagService.assignToDocument(document.documentId(), tags.getValue());
                refreshDocumentDetail(document);
            }));
            Button edit = new Button("Edit");
            edit.getStyleClass().add("secondary-button");
            edit.setOnAction(event -> showDocumentEditor(document));
            Button back = new Button("Back");
            back.getStyleClass().add("secondary-button");
            back.setOnAction(event -> showDocuments(globalSearch == null ? "" : globalSearch.getText()));
            refreshTags.run();
            VBox card = new VBox(14,
                    sectionTitle(document.title()),
                    new Label("Type: " + document.fileType()),
                    new Label("Size: " + FormatUtil.fileSize(document.fileSize())),
                    new Label("Created: " + FormatUtil.dateTime(document.createdDate())),
                    new Label("Path: " + document.filePath()),
                    sectionTitle("Assigned Tags"),
                    assigned,
                    new HBox(10, tags, assign),
                    new HBox(10, edit, back)
            );
            card.getStyleClass().add("card");
            setPage("Document Detail", card);
        });
    }

    private void refreshDocumentDetail(Document document) {
        showDocumentDetail(document);
    }

    private void showUpload() {
        VBox form = new VBox(14);
        form.getStyleClass().add("card");
        form.setMaxWidth(620);
        TextField filePath = new TextField();
        filePath.setEditable(false);
        filePath.setPromptText("Choose PDF, DOCX, or TXT file");
        TextField title = new TextField();
        title.setPromptText("Document title");
        TextArea description = new TextArea();
        description.setPromptText("Description");
        description.setPrefRowCount(4);
        ComboBox<Folder> folders = new ComboBox<>();
        run("Cannot load folders", () -> folders.setItems(FXCollections.observableArrayList(folderService.list())));
        final File[] selected = new File[1];

        Button choose = new Button("Choose File");
        choose.getStyleClass().add("secondary-button");
        choose.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.docx", "*.txt"));
            selected[0] = chooser.showOpenDialog(stage);
            if (selected[0] != null) filePath.setText(selected[0].getAbsolutePath());
        });

        Button upload = new Button("Upload");
        upload.getStyleClass().add("primary-button");
        upload.setOnAction(event -> run("Upload failed", () -> {
            Folder folder = folders.getValue();
            documentService.upload(selected[0] == null ? null : selected[0].toPath(), title.getText(), description.getText(), folder == null ? null : folder.folderId());
            AlertUtil.info("Success", "Document uploaded successfully.");
            showDocuments("");
        }));

        form.getChildren().addAll(field("File", filePath), choose, field("Title", title), field("Description", description), field("Folder", folders), upload);
        setPage("Upload File", form);
    }

    private void showFolders() {
        run("Cannot load folders", () -> {
            TextField name = new TextField();
            name.setPromptText("New folder name");
            Button create = new Button("Create Folder");
            create.getStyleClass().add("primary-button");
            TableView<Folder> table = new TableView<>();
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
            table.getColumns().add(column("Folder Name", Folder::folderName));
            table.getColumns().add(column("Created", folder -> FormatUtil.dateTime(folder.createdDate())));
            TableColumn<Folder, String> actions = new TableColumn<>("Actions");
            actions.setCellValueFactory(data -> new SimpleStringProperty("Actions"));
            actions.setCellFactory(col -> new TableCell<>() {
                private final Button open = new Button("Open");
                private final Button edit = new Button("Edit");
                private final Button delete = new Button("Delete");
                private final HBox box = new HBox(8, open, edit, delete);
                {
                    open.getStyleClass().add("secondary-button");
                    edit.getStyleClass().add("secondary-button");
                    delete.getStyleClass().add("danger-button");
                    open.setOnAction(event -> showFolderDocuments(getTableView().getItems().get(getIndex())));
                    edit.setOnAction(event -> {
                        Folder folder = getTableView().getItems().get(getIndex());
                        TextInputDialog dialog = new TextInputDialog(folder.folderName());
                        dialog.setTitle("Edit Folder");
                        dialog.setHeaderText(null);
                        dialog.setContentText("Folder name:");
                        dialog.showAndWait().ifPresent(value -> run("Cannot update folder", () -> {
                            folderService.update(folder, value);
                            showFolders();
                        }));
                    });
                    delete.setOnAction(event -> run("Cannot delete folder", () -> {
                        Folder folder = getTableView().getItems().get(getIndex());
                        long count = folderService.documentCount(folder);
                        String detail = count > 0 ? " Documents in this folder will become uncategorized." : "";
                        if (confirm("Delete folder", "Delete \"" + folder.folderName() + "\"?" + detail)) {
                            folderService.delete(folder);
                            showFolders();
                        }
                    }));
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : box);
                }
            });
            table.getColumns().add(actions);
            Runnable refresh = () -> run("Cannot refresh folders", () -> {
                table.setItems(FXCollections.observableArrayList(folderService.list()));
            });
            create.setOnAction(event -> run("Cannot create folder", () -> {
                folderService.create(name.getText());
                name.clear();
                refresh.run();
            }));
            refresh.run();
            HBox createRow = new HBox(10, name, create);
            HBox.setHgrow(name, Priority.ALWAYS);
            VBox card = new VBox(14, sectionTitle("Folder Management"), createRow, table);
            card.getStyleClass().add("card");
            setPage("Folders", card);
        });
    }

    private void showTags() {
        run("Cannot load tags", () -> {
            TextField name = new TextField();
            name.setPromptText("New tag name");
            ColorPicker color = new ColorPicker(javafx.scene.paint.Color.web("#3366CC"));
            Button create = new Button("Create Tag");
            create.getStyleClass().add("primary-button");
            TableView<Tag> table = new TableView<>();
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
            table.getColumns().add(column("Tag Name", Tag::tagName));
            table.getColumns().add(column("Color", Tag::colorHex));
            TableColumn<Tag, String> actions = new TableColumn<>("Actions");
            actions.setCellValueFactory(data -> new SimpleStringProperty("Actions"));
            actions.setCellFactory(col -> new TableCell<>() {
                private final Button view = new Button("View Files");
                private final Button edit = new Button("Edit");
                private final Button delete = new Button("Delete");
                private final HBox box = new HBox(8, view, edit, delete);
                {
                    view.getStyleClass().add("secondary-button");
                    edit.getStyleClass().add("secondary-button");
                    delete.getStyleClass().add("danger-button");
                    view.setOnAction(event -> showTagDocuments(getTableView().getItems().get(getIndex())));
                    edit.setOnAction(event -> {
                        Tag tag = getTableView().getItems().get(getIndex());
                        TextInputDialog dialog = new TextInputDialog(tag.tagName());
                        dialog.setTitle("Edit Tag");
                        dialog.setHeaderText(null);
                        dialog.setContentText("Tag name:");
                        dialog.showAndWait().ifPresent(value -> run("Cannot update tag", () -> {
                            tagService.update(tag, value, tag.colorHex());
                            showTags();
                        }));
                    });
                    delete.setOnAction(event -> run("Cannot delete tag", () -> {
                        Tag tag = getTableView().getItems().get(getIndex());
                        long count = tagService.documentCount(tag);
                        String detail = count > 0 ? " It will be removed from " + count + " document(s)." : "";
                        if (confirm("Delete tag", "Delete \"" + tag.tagName() + "\"?" + detail)) {
                            tagService.delete(tag);
                            showTags();
                        }
                    }));
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : box);
                }
            });
            table.getColumns().add(actions);
            Runnable refresh = () -> run("Cannot refresh tags", () -> {
                table.setItems(FXCollections.observableArrayList(tagService.list()));
            });
            create.setOnAction(event -> run("Cannot create tag", () -> {
                tagService.create(name.getText(), toHex(color.getValue()));
                name.clear();
                refresh.run();
            }));
            refresh.run();
            HBox createRow = new HBox(10, name, color, create);
            HBox.setHgrow(name, Priority.ALWAYS);
            VBox card = new VBox(14, sectionTitle("Tag Management"), createRow, table);
            card.getStyleClass().add("card");
            setPage("Tags", card);
        });
    }

    private void showActivityLogs() {
        run("Cannot load activity logs", () -> {
            List<ActivityLog> logs = dashboardService.recentActivities();
            TableView<ActivityLog> table = new TableView<>();
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
            table.getColumns().add(column("Action", ActivityLog::action));
            table.getColumns().add(column("Description", ActivityLog::description));
            table.getColumns().add(column("Time", log -> FormatUtil.dateTime(log.logTime())));
            table.setItems(FXCollections.observableArrayList(logs));
            setPage("Activity Logs", table);
        });
    }

    private void showProfile() {
        var user = SessionManager.currentUser().orElseThrow();
        TextField fullName = new TextField(user.fullName());
        TextField email = new TextField(user.email());
        Button save = new Button("Save Profile");
        save.getStyleClass().add("primary-button");
        save.setOnAction(event -> run("Cannot update profile", () -> {
            authService.updateProfile(fullName.getText(), email.getText());
            AlertUtil.info("Success", "Profile updated.");
            showProfile();
        }));

        PasswordField current = new PasswordField();
        current.setPromptText("Current password");
        PasswordField next = new PasswordField();
        next.setPromptText("New password");
        Button change = new Button("Change Password");
        change.getStyleClass().add("primary-button");
        change.setOnAction(event -> run("Cannot change password", () -> {
            authService.changePassword(current.getText(), next.getText());
            current.clear();
            next.clear();
            AlertUtil.info("Success", "Password changed.");
        }));

        VBox profileCard = new VBox(12,
                sectionTitle("Account Information"),
                new Label("Username: " + user.username()),
                field("Full name", fullName),
                field("Email", email),
                save
        );
        profileCard.getStyleClass().add("card");
        VBox passwordCard = new VBox(12,
                sectionTitle("Password"),
                field("Current password", current),
                field("New password", next),
                change
        );
        passwordCard.getStyleClass().add("card");
        setPage("Profile", profileCard, passwordCard);
    }

    private void logout() {
        run("Logout failed", () -> {
            authService.logout();
            stage.getScene().setRoot(createLoginView());
        });
    }

    private void setPage(String title, javafx.scene.Node... nodes) {
        VBox page = new VBox(18);
        page.getStyleClass().add("page");
        page.getChildren().add(sectionTitle(title));
        page.getChildren().addAll(nodes);
        content.getChildren().setAll(page);
    }

    private Label sectionTitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add(text.equals("Dashboard") || text.equals("Documents") ? "page-title" : "section-title");
        return label;
    }

    private <T> TableColumn<T, String> column(String title, java.util.function.Function<T, String> mapper) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(data -> new SimpleStringProperty(mapper.apply(data.getValue())));
        return column;
    }

    private VBox field(String label, Control control) {
        VBox box = new VBox(6);
        Label caption = new Label(label);
        caption.getStyleClass().add("muted");
        control.setMaxWidth(Double.MAX_VALUE);
        box.getChildren().addAll(caption, control);
        return box;
    }

    private String toHex(javafx.scene.paint.Color color) {
        return String.format("#%02X%02X%02X", (int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }

    private boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    private void run(String errorTitle, ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (IllegalArgumentException ex) {
            AlertUtil.error(errorTitle, ex.getMessage());
        } catch (SQLException ex) {
            AlertUtil.error(errorTitle, "Database error: " + ex.getMessage());
        } catch (Exception ex) {
            AlertUtil.error(errorTitle, ex.getMessage() == null ? "Unexpected error." : ex.getMessage());
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
