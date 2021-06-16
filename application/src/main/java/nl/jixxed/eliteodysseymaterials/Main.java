package nl.jixxed.eliteodysseymaterials;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import nl.jixxed.eliteodysseymaterials.parser.FileProcessor;
import nl.jixxed.eliteodysseymaterials.service.PreferencesService;
import nl.jixxed.eliteodysseymaterials.templates.ApplicationLayout;
import nl.jixxed.eliteodysseymaterials.watchdog.GameStateWatcher;
import nl.jixxed.eliteodysseymaterials.watchdog.JournalWatcher;

import java.io.File;

public class Main extends Application {
    private final ApplicationLayout applicationLayout = new ApplicationLayout(this);
    private final GameStateWatcher gameStateWatcher = new GameStateWatcher();
    private final JournalWatcher journalWatcher = new JournalWatcher();

    @Override
    public void start(final Stage primaryStage) {
        primaryStage.setTitle(AppConstants.APP_TITLE);
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream(AppConstants.APP_ICON_PATH)));
        PreferencesService.setPreference(PreferenceConstants.APP_SETTINGS_VERSION, System.getProperty("app.version"));
        final File watchedFolder = new File(AppConstants.WATCHED_FOLDER);

        this.applicationLayout.setWatchedFile("None - No Odyssey journals found at " + watchedFolder.getAbsolutePath());

        this.gameStateWatcher.watch(watchedFolder, this::processShipLockerBackPack, AppConstants.SHIPLOCKER_FILE);
        this.gameStateWatcher.watch(watchedFolder, this::processShipLockerBackPack, AppConstants.BACKPACK_FILE);

        this.journalWatcher.watch(watchedFolder, this::processJournal, FileProcessor::resetAndProcessJournal);

        final Scene scene = new Scene(this.applicationLayout, PreferencesService.getPreference(PreferenceConstants.APP_WIDTH, 800D), PreferencesService.getPreference(PreferenceConstants.APP_HEIGHT, 600D));

        scene.widthProperty().addListener((observable, oldValue, newValue) -> setPreferenceIfNotMaximized(primaryStage, PreferenceConstants.APP_WIDTH, (Double) newValue));
        scene.heightProperty().addListener((observable, oldValue, newValue) -> setPreferenceIfNotMaximized(primaryStage, PreferenceConstants.APP_HEIGHT, (Double) newValue));

        primaryStage.xProperty().addListener((observable, oldValue, newValue) -> setPreferenceIfNotMaximized(primaryStage, PreferenceConstants.APP_X, (Double) newValue));
        primaryStage.yProperty().addListener((observable, oldValue, newValue) -> setPreferenceIfNotMaximized(primaryStage, PreferenceConstants.APP_Y, (Double) newValue));
        primaryStage.maximizedProperty().addListener((observable, oldValue, newValue) -> PreferencesService.setPreference(PreferenceConstants.APP_MAXIMIZED, newValue));

        primaryStage.setX(PreferencesService.getPreference(PreferenceConstants.APP_X, 0D));
        primaryStage.setY(PreferencesService.getPreference(PreferenceConstants.APP_Y, 0D));
        primaryStage.setMaximized(PreferencesService.getPreference(PreferenceConstants.APP_MAXIMIZED, Boolean.FALSE));

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setPreferenceIfNotMaximized(final Stage primaryStage, final String setting, final Double value) {
        // x y are processed before maximized, so excluding setting it if it's -8
        if (!primaryStage.isMaximized() && !Double.valueOf(-8.0D).equals(value)) {
            PreferencesService.setPreference(setting, Double.valueOf(value));
        }
    }

    protected void processJournal(final File file) {
        this.applicationLayout.setWatchedFile(file.getAbsoluteFile().toString());
        final JsonNode message = FileProcessor.processJournal(file);
        this.applicationLayout.updateLastTimeStamp(message);
        this.applicationLayout.updateGui();
    }

    protected void processShipLockerBackPack(final File file) {
        this.applicationLayout.setWatchedFile(file.getAbsoluteFile().toString());
        final JsonNode message = FileProcessor.processShipLockerBackPack(file);
        this.applicationLayout.updateLastTimeStamp(message);
        this.applicationLayout.updateGui();
    }


    public static void main(final String[] args) {
        launch(args);
    }
}