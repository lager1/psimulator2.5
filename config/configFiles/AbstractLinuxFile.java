package config.configFiles;

import device.Device;
import filesystem.FileSystem;
import filesystem.dataStructures.jobs.OutputFileJob;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Paths;

/**
 * @author Michal Horacek
 */
public abstract class AbstractLinuxFile {

    protected String filePath;
    protected FileSystem fileSystem;

    public AbstractLinuxFile(FileSystem fs) {
        this.fileSystem = fs;
    }

    /**
     * Creates an empty file in location
     */
    public void createFile() {
        if (fileSystem != null) {
            fileSystem.runOutputFileJob(filePath, new OutputFileJob() {
                @Override
                public int workOnFile(OutputStream output) throws Exception {
                    PrintWriter writer = new PrintWriter(output);
                    writer.flush();
                    return 0;
                }
            });
        }
    }

    /**
     * @return String representing the file path
     */
    public String getFilePath() {
        return Paths.get(this.filePath).normalize().toUri().toString().substring(8);
    }
}
