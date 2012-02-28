/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package filesystem;

import filesystem.dataStructures.Node;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public interface FileSystem {
	
	/**
	 * recursively delete directory or single file
	 * @param path path to the file or directory
	 * @return true if file was sucessfully deleted, false otherwise
	 */
	public boolean rm_r(String path);
	/**
	 * find out if path is  file
	 * @param path
	 * @return 
	 */
	public boolean isFile(String path);
	/**
	 * find out if path is  directory
	 * @param path
	 * @return 
	 */
	public boolean isDir(String path);
	/**
	 * find out if path point to object
	 * @param path
	 * @return 
	 */
	public boolean exists(String path);
	/**
	 * list directory as array of Nodes
	 * @param path path to the directory or file. If path pointing to the file,then single Node is returned;
	 * @return names of files or directories
	 */
	public Node[] listDir(String path);
	
	/**
	 * 
	 * @param path path to the file
	 * @return OutputStream, do not forget to close it!!!
	 */
	public OutputStream getOutputStreamToFile(String path);
	/**
	 * 
	 * @param path path to the file
	 * @return InputStream, do not forget to close it!!!
	 */
	public InputStream getInputStreamToFile(String path);
	
	public void umount();
	
}
