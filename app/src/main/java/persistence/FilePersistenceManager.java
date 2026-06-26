package persistence;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import model.GameState;

/**
 * Persists game state to disk using Java object serialization.
 * Note: saved files become incompatible if the serialized model classes
 * change structure (a {@link ClassNotFoundException} or stream error on load
 * indicates a save from an incompatible version).
 */
public class FilePersistenceManager implements PersistenceManager {
    @Override
    public void save(GameState state, String filepath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filepath))) {
            oos.writeObject(state);
        }
    }

    @Override
    public GameState load(String filepath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filepath))) {
            return (GameState) ois.readObject();
        }
    }
}
