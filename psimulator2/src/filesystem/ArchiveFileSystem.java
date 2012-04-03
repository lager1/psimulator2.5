package filesystem;

import de.schlichtherle.truezip.file.*;
import de.schlichtherle.truezip.fs.FsSyncException;
import de.schlichtherle.truezip.fs.archive.zip.JarDriver;
import de.schlichtherle.truezip.nio.file.TPath;
import de.schlichtherle.truezip.socket.sl.IOPoolLocator;
import filesystem.dataStructures.Node;
import filesystem.dataStructures.jobs.InputFileJob;
import filesystem.dataStructures.jobs.OutputFileJob;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public class ArchiveFileSystem implements FileSystem {

    String pathToFileSystem;
    TPath archive;

    public static String getFileSystemExtension() {
        return "fsm";
    }

    public ArchiveFileSystem(String pathToFileSystem) {

        this.pathToFileSystem = pathToFileSystem;
        TConfig config = TConfig.get();
        config.setArchiveDetector(new TArchiveDetector(
                getFileSystemExtension(), // file system file extension
                new JarDriver(IOPoolLocator.SINGLETON)));

        TConfig.push();

        archive = new TPath(pathToFileSystem);

        TFile archiveFile = archive.toFile();

        if (!archiveFile.exists() && !archiveFile.mkdirs()) // if archive doesnt exist and cannot create empty one
        {
            System.err.println("mkdir failed");
        }


        if (!archiveFile.isArchive() || !archiveFile.isDirectory()) {
            System.err.println("file: " + pathToFileSystem + " is not compatible archive ");
        }

    }

    @Override
    public boolean rm_r(String path) {
        TFile file = new TFile(path);

        if (!file.exists()) {
            return false;
        }

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

        if (!file.exists()) {
            return false;
        }

        return file.isFile();
    }

    @Override
    public boolean isDir(String path) {
        TFile file = new TFile(path);

        if (!file.exists()) {
            return false;
        }

        return file.isDirectory();
    }

    @Override
    public boolean exists(String path) {
        
        return new TFile(pathToFileSystem+path).exists();
    }

    @Override
    public void umount() {
        try {
            TVFS.umount(archive.toFile());
        } catch (FsSyncException ex) {
            System.err.println("FsSyncException occured when umounting filesystem");
            //Logger.log(Logger.WARNING, LoggingCategory.FILE_SYSTEM, "FsSyncException occured when umounting filesystem");
        }
    }

    @Override
    public Node[] listDir(String path) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int runInputFileJob(String path, InputFileJob job) {

        InputStream input = null;

        try {
            input = Files.newInputStream(archive.resolve(path));
            job.workOnFile(input);
            return 0;
        } catch (Exception ex) {
            System.err.println(ex);
        } finally {

            try {
                input.close();
            } catch (Exception ex) {
            }
        }

        return -1;

    }

    @Override
    public int runOutputFileJob(String path, OutputFileJob job) {
        OutputStream output = null;

        try {
            TPath pat = new TPath(this.pathToFileSystem, path);
            
            output = Files.newOutputStream(pat);
            job.workOnFile(output);
            return 0;
        } catch (Exception ex) {
            System.err.println(ex);
        } finally {

            try {
                output.close();
            } catch (Exception ex) {
            }
        }

        return -1;
    }
}
