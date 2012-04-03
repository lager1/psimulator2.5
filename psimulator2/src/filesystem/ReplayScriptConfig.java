package filesystem;

import commands.AbstractCommandParser;
import filesystem.dataStructures.jobs.InputFileJob;
import java.io.InputStream;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public class ReplayScriptConfig {

	FileSystem fileSystem;

	public ReplayScriptConfig(FileSystem fileSystem) {
		this.fileSystem = fileSystem;
	}

	public int replay(String fileName, AbstractCommandParser parser){
	
		fileSystem.runInputFileJob(fileName, new InputFileJob() {

			@Override
			public int workOnFile(InputStream input) throws Exception {
				throw new UnsupportedOperationException("Not supported yet.");
			}
		});
		
	
		return 0;
	}
	
}