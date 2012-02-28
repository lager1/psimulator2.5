package filesystem;

import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileInputStream;
import de.schlichtherle.truezip.file.TFileOutputStream;
import de.schlichtherle.truezip.fs.FsSyncException;
import de.schlichtherle.truezip.fs.archive.zip.JarDriver;
import de.schlichtherle.truezip.socket.sl.IOPoolLocator;
import filesystem.dataStructures.Node;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public class ArchiveFileSystem implements FileSystem {

	String pathToFileSystem;
	TFile archive;
	
	public static String fileSystemExtensions = "fsm";

	public ArchiveFileSystem(String pathToFileSystem) {

		this.pathToFileSystem = pathToFileSystem;
		TFile.setDefaultArchiveDetector(
				new TArchiveDetector(
				fileSystemExtensions, // file system file extension
				new JarDriver(IOPoolLocator.SINGLETON)));

		archive = new TFile(pathToFileSystem);

		if (!archive.exists()) {
			archive.mkdirs();
		}

	}

	@Override
	public boolean rm_r(String path) {
		TFile file = new TFile(path);
		
		if(!file.exists())
			return false;
		
		try {
			file.rm_r();
		} catch (IOException ex) {
			return false;
		}
		
		return true;
	}

	@Override
	public boolean isFile(String path) {
		TFile file = new TFile(path);
		
		if(!file.exists())
			return false;
		
		return file.isFile();
	}

	@Override
	public boolean isDir(String path) {
		TFile file = new TFile(path);
		
		if(!file.exists())
			return false;
		
		return file.isDirectory();
	}

	@Override
	public boolean exists(String path) {
		return new TFile(path).exists();
	}

	
	@Override
	public OutputStream getOutputStreamToFile(String path) {

		TFile file = new TFile(archive, path);
		OutputStream ret = null;
		try {

			if (!file.exists()) {
				file.createNewFile();
			}

			ret = new TFileOutputStream(file);

		} catch (IOException ex) {
			Logger.getLogger(ArchiveFileSystem.class.getName()).log(Level.SEVERE, null, ex);
		}

		return ret;

	}

	@Override
	public InputStream getInputStreamToFile(String path) {
		TFile file = new TFile(archive, path);
		InputStream ret = null;
		try {

			if (!file.exists()) {
				file.createNewFile();
			}

			ret = new TFileInputStream(file);

		} catch (IOException ex) {
			Logger.getLogger(ArchiveFileSystem.class.getName()).log(Level.SEVERE, null, ex);
		}

		return ret;		
	}

	@Override
	public void umount() {
		try {
			TFile.umount(archive);
		} catch (FsSyncException ex) {
			//Logger.getLogger(ArchiveFileSystem.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public Node[] listDir(String path) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
