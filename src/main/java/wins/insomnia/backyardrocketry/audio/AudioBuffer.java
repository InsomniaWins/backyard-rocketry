package wins.insomnia.backyardrocketry.audio;

import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.*;
import java.net.URI;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.alcGetIntegerv;
import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_open_memory;
import static org.lwjgl.system.MemoryUtil.NULL;

public class AudioBuffer {


	private final int BUFFER_ID;

	private boolean clean = true;
	private double duration;


	public AudioBuffer(String audioFileName) throws Exception {
		BUFFER_ID = alGenBuffers();

		try (STBVorbisInfo info = STBVorbisInfo.malloc()) {
			ShortBuffer pcm = readVorbis(audioFileName, info);
			alBufferData(BUFFER_ID, info.channels() == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16, pcm, info.sample_rate());
			System.out.println("Loaded audio: " + audioFileName);
		}

		clean = false;
	}

	public int getBufferId() {
		return BUFFER_ID;
	}

	public void clean() {
		alDeleteBuffers(BUFFER_ID);
		clean = true;
	}

	public boolean isClean() {
		return clean;
	}

	public double getDuration() {

		return duration;

	}


	private ShortBuffer readVorbis(String audioFileName, STBVorbisInfo info) {
		try (MemoryStack stack = MemoryStack.stackPush()) {

			InputStream inputStream = this.getClass().getResourceAsStream("/audio/" + audioFileName);


			// get packets of data and total size of file
			ArrayList<byte[]> bytePackets = new ArrayList<>();
			byte[] readBuffer = new byte[512];
			int bytesRead = 0;
			long byteAmount = 0;
			while ((bytesRead = inputStream.read(readBuffer)) > 0){
				byteAmount += bytesRead;
				byte[] bytes = new byte[bytesRead];
				for (int i = 0; i < bytesRead; i++) {
					bytes[i] = readBuffer[i];
				}
				bytePackets.add(bytes);
			}
			inputStream.close();


			// allocate file to heap
			ByteBuffer buffer = MemoryUtil.memAlloc((int) byteAmount);
			for (byte[] packet : bytePackets) {
				buffer.put(packet);
			}

			// prepare for reading data
			buffer.flip();


			// read data
			int[] error = new int[1];
			long decoder = stb_vorbis_open_memory(buffer, error, null);
			if (decoder == NULL) {
				throw new RuntimeException("Failed to open Ogg Vorbis file! Error: " + error[0]);
			}


			stb_vorbis_get_info(decoder, info);

			int channels = info.channels();

			int lengthSamples = stb_vorbis_stream_length_in_samples(decoder);
			duration = stb_vorbis_stream_length_in_seconds(decoder);


			ShortBuffer result = MemoryUtil.memAllocShort(lengthSamples * channels);

			result.limit(stb_vorbis_get_samples_short_interleaved(decoder, channels, result) * channels);
			stb_vorbis_close(decoder);


			// free buffer/file from heap
			MemoryUtil.memFree(buffer);

			return result;

		} catch (BufferOverflowException bufferOverflowException) {

			System.err.println("Could not load vorbis audio file due to buffer-overflow exception!");

			return null;

		} catch (Exception e) {
			System.err.println("Exception reading vorbis: [" + e + "]: ");
			return null;
		}
	}




	// below is garbage code, keeping because it might be useful in the future
	// (doubt it)
	private ShortBuffer readVorbisOld(String audioFileName, STBVorbisInfo info) {
		try (MemoryStack stack = MemoryStack.stackPush()) {

			URI uri = AudioBuffer.class.getResource("/audio/").toURI();

			Path path;
			FileSystem fileSystem = null;
			String filePath;

			if (uri.getScheme().equals("jar")) {
				fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
				path = fileSystem.getPath("audio");
				filePath = "audio/test_sound.ogg";
			} else {
				path = Paths.get(uri);
				filePath = path + "/" + audioFileName;
			}


			System.out.println(filePath);

			IntBuffer error = stack.mallocInt(1);


			long decoder = stb_vorbis_open_filename(filePath, error, null);
			if (decoder == NULL) {
				throw new RuntimeException("Failed to open Ogg Vorbis file: \"" + filePath + "\". Error: " + error.get(0));
			}


			stb_vorbis_get_info(decoder, info);

			int channels = info.channels();

			int lengthSamples = stb_vorbis_stream_length_in_samples(decoder);
			duration = stb_vorbis_stream_length_in_seconds(decoder);


			ShortBuffer result = MemoryUtil.memAllocShort(lengthSamples * channels);

			result.limit(stb_vorbis_get_samples_short_interleaved(decoder, channels, result) * channels);
			stb_vorbis_close(decoder);

			if (fileSystem != null) fileSystem.close();

			return result;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return null;
		}
	}


}
