package persistence;

import java.io.IOException;
import model.GameState;

public interface PersistenceManager {
    void save(GameState state, String filepath) throws IOException;
    GameState load(String filepath) throws IOException, ClassNotFoundException;
}
