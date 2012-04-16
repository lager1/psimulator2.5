/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package filesystem;

import filesystem.dataStructures.Node;
import filesystem.dataStructures.jobs.InputFileJob;
import filesystem.dataStructures.jobs.OutputFileJob;
import filesystem.exceptions.FileNotFoundException;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public interface FileSystem {

	/**
	 * recursively delete directory or single file
	 *
	 * @param path path to the file or directory
	 * @return true if file was sucessfully deleted, false otherwise
	 * @throws FileNotFoundException  
	 */
	public boolean rm_r(String path) throws FileNotFoundException;

	/**
	 * find out if path is file
	 *
	 * @param path
	 * @return
	 */
	public boolean isFile(String path);

	/**
	 * find out if path is directory
	 *
	 * @param path
	 * @return
	 */
	public boolean isDir(String path);

	/**
	 * find out if path point to object
	 *
	 * @param path
	 * @return
	 */
	public boolean exists(String path);

	/**
	 * list directory as array of Nodes
	 *
	 * @param path path to the directory or file. If path pointing to the file,then single Node is returned;
	 * @return names of files or directories
	 * @throws FileNotFoundException  
	 */
	public Node[] listDir(String path) throws FileNotFoundException;
	
	/**
	 * 
	 * @param path
	 * @return  true = success, false otherwise
	 * @throws FileNotFoundException  if parent directory was not found
	 */
	public boolean createNewFile(String path) throws FileNotFoundException;

	/**
	 * 
	 * @param path path to the file to work on
	 * @param job job to do
	 * @return 0 success  -1 otherwise
	 * @throws FileNotFoundException  
	 */
	public int runInputFileJob(String path, InputFileJob job) throws FileNotFoundException;

	/**
	 * 
	 * @param path path to the file to work on
	 * @param job  job to do with file
	 * @return 
	 */
	public int runOutputFileJob(String path, OutputFileJob job);

	/**
	 * umount filesystem archive
	 */
	public void umount();
}
