package wins.insomnia.backyardrocketry.util.io;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileIO {

	public static final String ROOT_DIRECTORY_NAME = "backyard_rocketry_files";
	public static final String SAVES_DIRECTORY_NAME = "saves";
	public static final String CHUNKS_DIRECTORY_NAME = "chunks";
	public static final String FILE_SEPARATOR = FileSystems.getDefault().getSeparator();

	private static boolean checkDirectory(Path path, boolean createIfNotExist) {

		if (Files.notExists(path)) {
			if (createIfNotExist) {
				try {
					Files.createDirectory(path);
				} catch (IOException e) {
					return false;
				}
			} else {
				return false;
			}
		}

		return true;
	}

	public static Path getRootPath() {
		Path path = Paths.get(Paths.get("").toAbsolutePath() + FILE_SEPARATOR + ROOT_DIRECTORY_NAME);

		checkDirectory(path, true);

		return path;
	}

	public static Path getSavesPath() {

		Path savesPath = Paths.get(getRootPath() + FILE_SEPARATOR + SAVES_DIRECTORY_NAME);

		checkDirectory(savesPath, true);

		return savesPath;
	}

	public static Path getPathForSave(String saveName, boolean createIfMissing) {
		Path savePath = Paths.get(getSavesPath() + FILE_SEPARATOR + saveName);

		checkDirectory(savePath, createIfMissing);

		return savePath;
	}

	public static Path getChunksPath(Path savePath) {
		Path chunksPath = Paths.get(savePath + FILE_SEPARATOR + CHUNKS_DIRECTORY_NAME);

		checkDirectory(chunksPath, true);

		return chunksPath;
	}

	public static Path getPathForSave(String saveName) {
		return getPathForSave(saveName, true);
	}

}
