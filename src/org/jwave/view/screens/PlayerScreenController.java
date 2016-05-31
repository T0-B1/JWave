package org.jwave.view.screens;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jwave.model.player.MetaData;
import org.jwave.model.player.Playlist;
import org.jwave.model.player.Song;
import org.jwave.view.FXEnvironment;
import org.jwave.view.PlayerUI;
import org.jwave.view.PlayerController;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableRow;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * Controller for the Player screen.
 * 
 * @author Alessandro Martignano
 *
 */
public class PlayerScreenController implements PlayerUI {

    private final FXMLScreens FXMLSCREEN = FXMLScreens.PLAYER;
    private final FXEnvironment environment;
    private final PlayerController controller;
    private Stage primaryStage;
    private boolean lockedPositionSlider;

    @FXML
    private Button btnPlay, btnNewPlaylist;
    @FXML
    private volatile Slider positionSlider, volumeSlider;
    @FXML
    private ListView<Playlist> listView;
    @FXML
    private TableView<Song> tableView;
    @FXML
    private TableColumn<Song, String> columnFile, columnTitle, columnAuthor, columnAlbum, columnGenre;

    public PlayerScreenController(FXEnvironment environment, PlayerController controller) {
        this.controller = controller;
        this.environment = environment;
        this.environment.loadScreen(FXMLSCREEN, this);
        this.lockedPositionSlider = false;

        tableView.setPlaceholder(new Label("Nessun brano caricato"));
        tableView.setRowFactory(tr -> {
            TableRow<Song> row = new TableRow<>();
            return row;
        });

        MenuItem addToPlaylist = new MenuItem("Aggiungi a playlist");
        addToPlaylist.setOnAction(e->{
            List<Playlist> choices = controller.getObservablePlaylists().stream().filter(p->p.getName() != "default").collect(Collectors.toList());
            ChoiceDialog<Playlist> dialog = new ChoiceDialog<>(choices.get(0), choices);
            //dialog.
            dialog.setTitle("Aggiungi a playlist");
            dialog.setHeaderText("Scegli la playlist in cui inserire "+tableView.getSelectionModel().getSelectedItem().getName());

            // Traditional way to get the response value.
            Optional<Playlist> result = dialog.showAndWait();
        });
        tableView.setContextMenu(new ContextMenu(addToPlaylist));

        listView.setItems(controller.getObservablePlaylists());
        listView.setOnMouseClicked(e -> {
            System.out.println("SELECTED PLAYLIST: " + listView.getSelectionModel().getSelectedItem().getName());
            // observer.getObservablePlaylistContent(listView.getSelectionModel().getSelectedItem()).forEach(s->System.out.println(s.getName()));
            tableView.setItems(controller.getObservablePlaylistContent(listView.getSelectionModel().getSelectedItem()));
        });

        listView.setCellFactory(new Callback<ListView<Playlist>, ListCell<Playlist>>() {
            @Override
            public ListCell<Playlist> call(ListView<Playlist> lv) {
                ListCell<Playlist> cell = new ListCell<Playlist>() {
                    @Override
                    public void updateItem(Playlist item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null) {
                            setText(null);
                        } else {
                            if (item.getName() == "default") {
                                setText("Tutti i brani");
                            } else {
                                setText(item.getName());
                            }
                        }
                    }
                };
                return cell;
            }
        });

        columnFile.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        columnTitle.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getMetaData().retrieve(MetaData.TITLE)));
        columnAuthor.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getMetaData().retrieve(MetaData.ARTIST)));
        columnAlbum.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getMetaData().retrieve(MetaData.ALBUM)));
        columnGenre.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getMetaData().retrieve(MetaData.GENRE)));

        volumeSlider.valueProperty().addListener((ov, old_val, new_val) -> controller.setVolume(new_val.intValue()));

    }

    @Override
    public void show() {
        this.primaryStage = this.environment.getMainStage();
        // this.primaryStage.setOnCloseRequest(e->observer.terminate());
        this.primaryStage.setOnCloseRequest(e -> System.exit(0));
        this.environment.displayScreen(FXMLSCREEN);
    }

    @Override
    public void setObserver(PlayerController observer) {

        // this.observer = observer;

    }

    public void updatePosition() {
        System.out.println("Pos");
    }

    @FXML
    private void newPlaylist() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nuova playlist");
        dialog.setHeaderText("Inserire il nome della nuova palylist");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            System.out.println("NEW PLAYLIST: " + result.get());
            this.controller.newPlaylist(result.get());
        }
    }

    @FXML
    private void play() {
        controller.play();
    }

    @FXML
    private void stopPlay() {
        controller.stop();
    }

    @FXML
    private void next() {
        System.out.println("next");
        controller.next();
    }

    @FXML
    private void prev() {
        System.out.println("prev");
        controller.previous();
    }

    @FXML
    private void openFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Audio file", "*.mp3", "*.wav"));
        List<File> openedFiles = fileChooser.showOpenMultipleDialog(primaryStage);
        if (openedFiles != null)
            openedFiles.forEach(f -> {
                try {
                    controller.loadSong(f);
                } catch (Exception e) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Errore");
                    alert.setHeaderText("Impossibile aprire il file " + f.getName());
                    alert.setContentText("Il file potrebbe essere danneggiato o in un formato non valido.");
                    alert.showAndWait();
                }
            });
    }

    @FXML
    private void changePosition() {
        controller.moveToMoment(positionSlider.getValue());
        lockedPositionSlider = false;
    }

    @Override
    public void updatePosition(Integer ms, Integer lenght) {
        if (!positionSlider.isValueChanging() && lockedPositionSlider == false)
            positionSlider.setValue((ms * 10000) / lenght);
    }

    @FXML
    private void lockSlider() {
        lockedPositionSlider = true;
    }

}
