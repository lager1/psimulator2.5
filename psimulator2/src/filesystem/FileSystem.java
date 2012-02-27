/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package filesystem;

import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public interface FileSystem {
	
	
	public boolean deleteFileOrDir(String path);
	public boolean isFile(String path);
	public boolean isDir(String path);
	public boolean exists(String path);
	
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
