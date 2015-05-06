/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package filesystem.dataStructures.jobs;

import java.io.OutputStream;

public interface InputOutputFileJob {

    public int workOnFile(OutputStream output) throws Exception;

}
