package controller;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import model.Enemy;
import model.GameState;
import model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import persistence.PersistenceManager;

class GameControllerTest {
    private boolean saveCalled;
    private boolean loadCalled;
    private GameState dummyState;

    @BeforeEach
    void setUp() {
        saveCalled = false;
        loadCalled = false;
        dummyState = null;
    }

    @Test
    void testConstructorInjection() throws IOException, ClassNotFoundException {
        // Create mock persistence manager
        PersistenceManager mockPM = new PersistenceManager() {
            @Override
            public void save(GameState state, String filepath) throws IOException {
                saveCalled = true;
                dummyState = state;
            }

            @Override
            public GameState load(String filepath) throws IOException, ClassNotFoundException {
                loadCalled = true;
                return dummyState;
            }
        };

        // Inject dependencies
        CombatSystem customCombatSystem = new CombatSystem();
        GameController controller = new GameController(customCombatSystem, mockPM);

        // Inject mock view
        GameView mockView = new GameView() {
            private String logText = "";
            @Override public void setLog(String message) { logText = message; }
            @Override public void appendLog(String message) { logText += message + "\n"; }
            @Override public void refresh() {}
            @Override public void showGameOverDialog(boolean victory) {}
            @Override public String getLogText() { return logText; }
        };
        controller.setGamePanel(mockView);

        // Verify save game uses the injected persistence manager
        controller.restartGame();
        controller.saveGame("test_path");
        assertTrue(saveCalled);
        assertNotNull(dummyState);

        // Verify load game uses the injected persistence manager
        controller.loadGame("test_path");
        assertTrue(loadCalled);
    }
}
